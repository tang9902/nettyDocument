package hello;

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Discards any incoming data.
 */
public class DiscardServer {

    private int port;

    private DiscardServer(int port) {
        this.port = port;
    }

    private void run() throws Exception {
        // NioEventLoopGroup is a multithreaded event loop that handles I/O operation
        // NioEventLoopGroup 是一个多线程事件循环，处理I/O操作。
        // Netty对不同的传输协议提供了不同的实现
        // 这里需要有两个事件循环器，一个称为boss，接受连接，一个称为worker，一量boss接受连接并注册给worker，worker便开始处理
        // 多少线程被使用，它们如何映射到通道，取决于NioEventLoopGroup的具体实现或构造方法
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // ServerBootstrap是一个帮助工具类，它启动一个服务。你可以使用一个频道来直接启动一个服务，但那是枯燥冗长的，你不会那么做。
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    // NioServerSocketChannel用来实例化一个新的通道来接收进入的连接
                    .channel(NioServerSocketChannel.class) // (3)
                    // handler由最新接收进来的连接进行评估
                    // ChannelInitializer是一个特殊的处理器，用来配置一个新的通道
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    // 设置一些具体的通道参数。
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // 绑定机器的网络（ NICs (network interface cards) ）端口
        new DiscardServer(port).run();
    }
}