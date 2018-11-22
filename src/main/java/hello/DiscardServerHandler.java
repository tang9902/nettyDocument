package hello;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Handles a server-side channel.
 * 处置一个服务端的通道
 */
// 【ChannelInboundHandlerAdapter】提供了大量【事件处理方法】可以重写，它实现【ChannelInboundHandler】接口
public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

    // 这是一个事件处理器，任何时候服务端接收到新的数据，它都会处理接收到的消息
    // 此例中接收到的信息类型是ByteBuf，它是一种reference-counted类型数据，必须手动释放
    // 在计算机科学中，引用计数是一种将引用，指针或句柄的【数量】存储到诸如对象，存储器块，磁盘空间或其他资源之类的资源的技术。
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // Discard the received data silently.
        // 默不作声地丢弃接收到的数据
        // ((ByteBuf) msg).release(); // (3)

        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // (1)
                System.out.println("=============================");
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }

    // 这是一个事件处理器，当抛出I/O错误或实现时的错误时，会调用此处理器
    // 一般在这里需要做日志记录，以及相关的通道需要关闭，具体要看你想做什么
    // 如果你可以响应一个错误码并关闭连接
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        // 当抛出一个错误时关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}