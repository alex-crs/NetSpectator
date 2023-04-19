package services;


import org.apache.log4j.Logger;

import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainClientLogic {
    private DataOutputStream out;
    private DataInputStream in;
    private ReadableByteChannel rbc;
    private ExecutorService threadManager;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);
    private HashMap<String, String> connectionParams;
    private Socket socket;
    private String ADDRESS;
    private int PORT;
    private boolean isInteractive;
    private static final Logger LOGGER = Logger.getLogger(MainClientLogic.class);

    public HashMap<String, String> getServerParams() {
        return connectionParams;
    }

    public MainClientLogic() {
        paramsInit();
        tryToConnect();
    }

    private void tryToConnect() {
        int connectionResult = connect();
        if (connectionResult > 0 && isInteractive) {
            interactiveClientWorker();
        } else if (connectionResult > 0) {
            automaticClientWorker();
        }
    }

    private void paramsInit() {
        connectionParams = ClientFileReader.initFileParams("client.ini");
        assert connectionParams != null;
        ADDRESS = connectionParams.get("Address");
        LOGGER.info(String.format("Адрес сервера: [%s]", ADDRESS));
        PORT = Integer.parseInt(connectionParams.get("Port"));
        LOGGER.info(String.format("Порт сервера: [%s]", PORT));
        isInteractive = connectionParams.get("Interactive mode").equals("true");
        if (!isInteractive) {
            LOGGER.info("Активирован автоматический режим");
        } else {
            LOGGER.info("Активирован интерактивный режим");
        }
        if (connectionParams.get("Client name").equals("")) {
            connectionParams.put("Client name", deviceName());
            ClientFileReader.writeFileParams(connectionParams);
        }
    }

    private int connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            if (socket.isConnected()) {
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                rbc = Channels.newChannel(in);
                threadManager = Executors.newFixedThreadPool(5);
                LOGGER.info(String.format("Установлено соединение с сервером по адресу: [%s:%s]", ADDRESS, PORT));
                return 1;
            }
        } catch (IOException e) {
            LOGGER.info(String.format("Невозможно установить соединение с сервером по адресу [%s:%s]", ADDRESS, PORT));
            return -1;
        }
        return 0;
    }

    private void interactiveClientWorker() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            StringBuilder command = new StringBuilder();
            out.write(("/hello").getBytes());
            System.out.print(queryStringListener());
            while (true) {
                command.append(reader.readLine());
                if (command.toString().equals("exit")) {
                    break;
                }
                out.write(("/" + command).getBytes());
                command.delete(0, command.length());
                command.append(queryStringListener());
                System.out.print(command);
                if (command.toString().contains("shutdown")) {
                    break;
                }
                command.delete(0, command.length());
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Сервер разорвал соединение");
            tryToConnect();
        } catch (IOException e) {
            LOGGER.info("Соединение потеряно");
        }
    }

    private void automaticClientWorker() {
        String[] query;
        boolean keepAliveStatus = true;
        try {
            out.write(("\\auth " + connectionParams.get("Public key")).getBytes());
            while (keepAliveStatus) {
                query = queryStringListener().replace("\n", "").split(" ");
                switch (query[0]) {
                    case "getId":
                        out.write(("\\clientID " + connectionParams.get("Client ID")).getBytes());
                        break;
                    case "newID":
                        connectionParams.put("Client ID", query[1]);
                        ClientFileReader.writeFileParams(connectionParams);
                        LOGGER.info(String.format("Клиенту присвоен новый ID: [%s]", query[1]));
                        break;
                    case "getName":
                        out.write(("\\clientName " + connectionParams.get("Client name")).getBytes());
                        break;
                    case "close":
                        keepAliveStatus = false;
                        break;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Connection closed.");
        }
    }

    private String queryStringListener() {
        int readNumberBytes = 0;
        try {
            readNumberBytes = rbc.read(byteBuffer);
        } catch (SocketException e) {
            LOGGER.error("Connection closed");
            return "close";
        } catch (IOException e) {
            LOGGER.error("IO exception detected");
            return "close";
        }

        String queryAnswer = null;
        try {
            queryAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes));
        } catch (Exception e) {
            queryAnswer = "close";
            LOGGER.error("Connection refused");
        }
        byteBuffer.clear();
        return queryAnswer;
    }

    private String deviceName() {
        String computerName = null;
        try {
            computerName = Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return computerName;
    }


}
