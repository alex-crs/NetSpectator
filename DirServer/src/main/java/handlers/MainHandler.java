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
    private ChanelListener chanelListener;
    private Client client;
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
        connectionInit(ctx);
        NettyBootstrap.connections.add(client);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyBootstrap.connections.remove(client);
        System.out.println("Client disconnected " + ctx.channel().localAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        chanelListener.listen(ctx, msg);
    }

    private void connectionInit(ChannelHandlerContext ctx){
        client = new Client(ctx);
        DataBaseService dbService = new DataBaseService();
        MessageSender messageSender = new MessageSender(ctx, client);
        BlackList blackList = new BlackList(messageSender, client);
        Server server = new Server(messageSender, client);
        ConnectionsList connectionsList = new ConnectionsList(messageSender, blackList, client);
        chanelListener = new ChanelListener(messageSender, connectionsList, blackList, client, server, dbService);
    }
}

