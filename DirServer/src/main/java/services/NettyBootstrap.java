package services;

import handlers.ByteBufEncoder;
import handlers.MainHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NettyBootstrap {
    private static EventLoopGroup auth;
    private static EventLoopGroup worker;
    public static HashMap<String, String> serverParams;
    private int PORT;
    private long requestInterval;
    private static final Logger LOGGER = Logger.getLogger(NettyBootstrap.class);
    public static List<ChannelHandlerContext> connections = new ArrayList<>();
    public static List<SocketAddress> blackList = new ArrayList<>();

    public NettyBootstrap() {
        initParams();
        serverStart();
    }

    private void serverStart(){
        auth = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ByteBufEncoder(),
                                    new MainHandler());
                            channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(8096));
                        }
                    });
            ChannelFuture future = bootstrap.bind(PORT).sync();
            LOGGER.info("Server start");
            future.channel().closeFuture().sync();
            System.out.println("Server finished");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private void initParams(){
        serverParams = ServerFileReader.initFileParams("server.ini");
        assert serverParams != null;
        PORT = Integer.parseInt(serverParams.get("Port"));
        LOGGER.info(String.format("???????????? ???????????????????????? ????????: [%s]", PORT));
        requestInterval = Long.parseLong(serverParams.get("Request interval (sec)"));
        LOGGER.info(String.format("???????????????? ???????????? %s sec", requestInterval));
    }

    public static void shutdownServer() {
        auth.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
