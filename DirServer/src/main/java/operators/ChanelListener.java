package operators;

import entities.Client;
import entities.Device;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import services.DataBaseService;
import services.NettyBootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
public class ChanelListener {
    private String uuid;
    private final MessageSender messageSender;
    private final ConnectionsList connections;
    private final BlackList blackList;
    private final Client client;
    private final Server server;
    private final DataBaseService dbService;
    private static final Logger LOGGER = Logger.getLogger(ChanelListener.class);

    public void listen(ChannelHandlerContext ctx, Object msg) throws IOException {
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
                messageSender.sendMessageWithHeader("Welcome to Net Spectator server. ");
                break;
            case "/auth":
                if (header.length > 1 && header[1].equals(NettyBootstrap.serverParams.get("admin"))) {
                    client.setAuth(true);
                    messageSender.sendMessageWithHeader("Authorization ok");
                } else {
                    messageSender.sendMessageWithHeader("Wrong key");
                }
                break;
            case "\\auth":
                if (header.length > 1 && header[1].equals(NettyBootstrap.serverParams.get("publicKey"))) {
                    client.setAuth(true);
                    messageSender.sendMessage("getId");
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
                } else {
                    uuid = header[1];
                }
                messageSender.sendMessage("getName");
                break;
            case "/connections":
                if (!connections.connectionListOperator(ctx, header)) {
                    LOGGER.info("Bad command");
                }
                break;
            case "\\clientName":
                LOGGER.info(String.format("Имя клиента: [%s]", header[1]));
                deviceInit(ctx, header);
                break;
            case "/shutdown":
                server.shutdown();
                break;
            case "/blacklist":
                if (!blackList.blackListOperator(header)) {
                    LOGGER.info("Bad command");
                }
                break;
            default:
                messageSender.sendMessageWithHeader("Unknown command");
                break;
        }
    }

    private void deviceInit(ChannelHandlerContext ctx, String[] args) {
        Device device = new Device();
        device.setTitle(args[1]);
        device.setUUID(uuid);
        device.setOnlineStatus(1);
        device.setIp(ctx.channel().localAddress()
                .toString()
                .replace("/", ""));
        if (dbService.getDeviceByUUID(uuid) == null && dbService.addDevice(device) > 0) {
            LOGGER.info("Operation complete");
        }
    }
}
