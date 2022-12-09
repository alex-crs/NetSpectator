package Handlers;

import Services.nettyBootstrap;
import entities.Device;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
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
        if (nettyBootstrap.blackList.contains(ctx.channel().localAddress())) {
            ctx.disconnect();
        }
        nettyBootstrap.connections.add(ctx);
        System.out.println(UUID.randomUUID());
        for (SocketAddress c :
                nettyBootstrap.blackList) {
            System.out.println(c);
        }
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
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("Welcome to Net Spectator server. " + messageConstructor()).getBytes()));
                break;
            case "/auth":
                if (header.length > 1 && header[1].equals(nettyBootstrap.serverParams.get("admin"))) {
                    isAuth = true;
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("Authorization ok" + messageConstructor()).getBytes()));
                } else {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("Wrong key" + messageConstructor()).getBytes()));
                }
                break;
            case "\\auth":
                if (header.length > 1 && header[1].equals(nettyBootstrap.serverParams.get("publicKey"))) {
                    isAuth = true;
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("getId").getBytes()));
                } else {
                    nettyBootstrap.blackList.add(ctx.channel().localAddress());
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
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("getName").getBytes()));
                break;
            case "\\clientName":
                LOGGER.info(String.format("Имя клиента: [%s]", header[1]));
                break;
            case "/shutdown":
                if (isAuth) {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("Server shutdown" + "\n").getBytes()));
                    nettyBootstrap.shutdownServer();
                } else {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("You are not authorized" + messageConstructor()).getBytes()));
                }
                break;
            case "/connection-test":
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/connection-test-ok" + messageConstructor()).getBytes()));
                break;
            case "\\connection-test":
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/connection-test-ok").getBytes()));
                break;
            case "/remove":
                nettyBootstrap.connections.get(0).close();
                break;
            case "/blacklist":
                if (header.length > 1 && header[1].equals("show")) {
                    StringBuilder response = new StringBuilder();
                    int number = 1;
                    for (SocketAddress address : nettyBootstrap.blackList) {
                        response.append(number).append(". ").append(address).append("\n");
                    }
                    ctx.writeAndFlush(Unpooled.wrappedBuffer((response + messageConstructor()).getBytes()));
                } else {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("empty args " + messageConstructor()).getBytes()));
                }
                break;
            default:
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("Unknown command" + messageConstructor()).getBytes()));
                break;
        }
    }

    private String messageConstructor() {

        return "\n\n" + (isAuth ? "Authorized. " : "N/A. ") + HEAD;
    }

}
