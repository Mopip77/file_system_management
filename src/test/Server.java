package test;

import experiment.os.block.DataBlocks;
import experiment.os.block.base.Block;
import experiment.os.block.base.File;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;

public class Server {
    @Test
    public void client() throws IOException {
        SocketChannel server = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9090));
        // 切换成非阻塞式
        server.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

//        buffer.put(LocalDateTime.now().toString().getBytes());
        buffer.put("asdf".getBytes());
        buffer.flip();

        server.write(buffer);

        server.close();
    }

    @Test
    public void server() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(9090));

        // 切换成非阻塞式
        server.configureBlocking(false);

        // 获取选择器
        Selector selector = Selector.open();

        // 将通道注册到选择器 (监听接收事件)
        server.register(selector, SelectionKey.OP_ACCEPT);

        // 轮训获取通过选择器的事件
        while (true) {
            if (selector.select() == 0) {
                continue;
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey sk = it.next();
                // 丢弃sk, 否则还会被循环到
                it.remove();

                // 判断事件是否是"accept"就绪
                if (sk.isAcceptable()) {
                    // 仍然是之前的accept方法, 只是等真正有accept请求才调用, 并且继续非阻塞得注册写事件
                    SocketChannel accept = server.accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                } else if (sk.isReadable()) {
                    // 获取读就绪的通道, 在sk上
                    SocketChannel readChannel = (SocketChannel) sk.channel();

                    ByteBuffer bb = ByteBuffer.allocate(1024);
                    int num = readChannel.read(bb);
                    if (num > 0) {
                        // 业务代码
                        if (num == 23) {
                            DataBlocks.getInstance().set(10, new File("file".toCharArray(), -1));
                        }
                        File block = (File) DataBlocks.getInstance().get(10);
                        System.out.println(block.getData());
                    } else {
                        readChannel.close();
                    }
                }
            }
        }
    }
}
