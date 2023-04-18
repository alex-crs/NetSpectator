package operators;

import entities.Client;
import services.NettyBootstrap;

public class Server {
    private final MessageSender messageSender;
    private final Client client;

    public Server(MessageSender messageSender, Client client) {
        this.messageSender = messageSender;
        this.client = client;
    }

    public void shutdown() {
        if (client.isAuth()) {
            messageSender.sendMessage("Server shutdown");
            NettyBootstrap.shutdownServer();
        } else {
            messageSender.sendMessageWithHeader("You are not authorized");
        }
    }
}