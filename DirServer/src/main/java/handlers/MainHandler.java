package handlers;

import services.NettyBootstrap;
import entities.Device;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainHandler extends ChannelInboundHandlerAdapter {
    public static String DELIMITER = ";";
    private boolean isAuth;
    private String publicKey;
    private String uuid;
    private final String HEAD = "Net Spectator Server. For help ?\n$ ";
    private Device device;
    private static final Logger LOGGER = Logger.getLogger(MainHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected " + ctx.channel().localAddress());
        if (NettyBootstrap.blackList.contains(ctx.channel().localAddress())) {
            ctx.disconnect();
        }
        NettyBootstrap.connections.add(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected " + ctx.channel().localAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        stringListener(ctx, msg);
    }

    public void stringListener(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf byteBuf = (ByteBuf) msg;
        String[] header = byteBuf.toString(StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "")
                .split(" ", 0);
        LOGGER.info(String.format("Received header with content: [%s]", byteBuf.toString(StandardCharsets.UTF_8)));

        if (header[0].toLowerCase().contains("ssh")) {
            ctx.disconnect();
        }

        /*  \\ - автоматический режим работы агента, / - интерактивный режим работы агента */
        switch (header[0]) {
            case "/hello":
                sendMessageWithHeader(ctx, "Welcome to Net Spectator server. ");
                break;
            case "/auth":
                if (header.length > 1 && header[1].equals(NettyBootstrap.serverParams.get("admin"))) {
                    isAuth = true;
                    sendMessageWithHeader(ctx, "Authorization ok");
                } else {
                    sendMessageWithHeader(ctx, "Wrong key");
                }
                break;
            case "\\auth":
                if (header.length > 1 && header[1].equals(NettyBootstrap.serverParams.get("publicKey"))) {
                    isAuth = true;
                    sendMessage(ctx, "getId");
                } else {
                    NettyBootstrap.blackList.add(ctx.channel().localAddress());
                    ctx.disconnect();
                }
                break;
            case "\\clientID":
                if (header.length < 2) { //не забыть проверить уникальность ID по базе
                    LOGGER.info("Подключается новое устройство. Присваиваю новый ID");
                    uuid = UUID.randomUUID().toString();
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("newID " + uuid).getBytes()));
                    LOGGER.info(String.format("Новому клиенту присвоен ID: [%s]", uuid));
                }
                sendMessage(ctx, "getName");
                break;
            case "\\clientName":
                LOGGER.info(String.format("Имя клиента: [%s]", header[1]));
                break;
            case "/shutdown":
                shutdownServer(ctx);
                break;
            case "/blacklist":
                if (!blackListOperator(ctx, header)) {
                    LOGGER.info("Bad command");
                }
                break;
            default:
                sendMessageWithHeader(ctx, "Unknown command");
                break;
        }
    }


    private void shutdownServer(ChannelHandlerContext ctx) {
        if (isAuth) {
            sendMessage(ctx, "Server shutdown");
            NettyBootstrap.shutdownServer();
        } else {
            sendMessageWithHeader(ctx, "You are not authorized");
        }
    }

    //проводит операции над черным списком
    private boolean blackListOperator(ChannelHandlerContext ctx, String[] args) {
        StringBuilder response = new StringBuilder();

        if (!isAuth) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(("Not authorized " + messageConstructor()).getBytes()));
            return false;
        }

        if (args.length < 2) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(("wrong args" + messageConstructor()).getBytes()));
            return false;
        }

        switch (args[1]) {
            case "show":
                int finalIndex = 1;
                NettyBootstrap.blackList.forEach(socketAddress -> response
                        .append(finalIndex)
                        .append(". ")
                        .append(socketAddress)
                        .append("\n"));
                sendMessageWithHeader(ctx, (response.length() < 1 ? "empty" : response.toString()));
                return true;
            case "remove":
                int index = 0;
                try {
                    index = Integer.parseInt(args[2]);
                    NettyBootstrap.blackList.remove(index - 1);
                } catch (NumberFormatException e) {
                    sendMessageWithHeader(ctx, "Wrong index format");
                    return false;
                } catch (IndexOutOfBoundsException e) {
                    sendMessageWithHeader(ctx, NettyBootstrap.blackList.size() == 0 ? "Empty blacklist" : "wrong index");
                    return false;
                }
                sendMessageWithHeader(ctx, "Operation complete");
                return true;
            default:
                sendMessageWithHeader(ctx, "Bad command");
                break;
        }
        return false;
    }

    private void sendMessageWithHeader(ChannelHandlerContext ctx, String message) {
        ctx.writeAndFlush(Unpooled.wrappedBuffer((message + messageConstructor()).getBytes()));
    }

    private void sendMessage(ChannelHandlerContext ctx, String message) {
        ctx.writeAndFlush(Unpooled.wrappedBuffer((message + "\n").getBytes()));
    }

    private String messageConstructor() {

        return "\n\n" + (isAuth ? "Authorized. " : "N/A. ") + HEAD;
    }

}
