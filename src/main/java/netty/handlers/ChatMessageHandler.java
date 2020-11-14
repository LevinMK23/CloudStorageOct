package netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class ChatMessageHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        Users u = new Users();
        String[] str = s.split(" ");
        if (s.startsWith("/reg")) {
            s = u.addNick(str[1], str[2]);
        } else {
            s = u.getNick(str[0], str[1]);
        }
        ctx.writeAndFlush(s);
    }
}
