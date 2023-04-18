package operators;

import entities.Client;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class MessageSender {
    private final String HEAD = "Net Spectator Server. For help ?\n$ ";
    private final Client client;
    private final ChannelHandlerContext ctx;

    public MessageSender(ChannelHandlerContext ctx, Client client) {
        this.client = client;
        this.ctx = ctx;
    }

    public void sendMessageWithHeader(String message) {
        ctx.writeAndFlush(Unpooled.wrappedBuffer((message + messageConstructor()).getBytes()));
    }

    public void sendMessageWithoutHeader(String message) {
        ctx.writeAndFlush(Unpooled.wrappedBuffer((message + "\n").getBytes()));
    }

    private String messageConstructor() {
        return "\n\n" + (client.isAuth() ? "Authorized. " : "N/A. ") + HEAD;
    }
}
