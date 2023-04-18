package handlers;

import entities.Client;
import operators.*;
import services.DataBaseService;
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
    private Client client;
    private BlackList blackList;
    private MessageSender messageSender;
    private ConnectionsList connectionsList;
    private ChanelListener chanelListener;
    private Server server;
    private DataBaseService dbService;
    private static final Logger LOGGER = Logger.getLogger(MainHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        dbService = new DataBaseService();
        System.out.println("Client connected " + ctx.channel().localAddress());
        if (NettyBootstrap.blackList.contains(ctx.channel().localAddress())) {
            ctx.disconnect();
        }
        NettyBootstrap.connections.add(ctx);
        client = new Client();
        messageSender = new MessageSender(ctx, client);
        blackList = new BlackList(messageSender, client);
        server = new Server(messageSender, client);
        connectionsList = new ConnectionsList(messageSender, blackList, client);
        chanelListener = new ChanelListener(messageSender, connectionsList, blackList, client, server, dbService);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyBootstrap.connections.remove(ctx);
        System.out.println("Client disconnected " + ctx.channel().localAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        chanelListener.listen(ctx, msg);
    }
}

