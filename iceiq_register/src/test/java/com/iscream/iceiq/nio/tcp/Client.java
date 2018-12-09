package com.iscream.iceiq.nio.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {

    private static Logger logger = LoggerFactory.getLogger(Service.class);

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));

        String newData = "Hello Service Date 22:45";

        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.clear();
        buf.put(newData.getBytes());

        buf.flip();
        socketChannel.write(buf);
        logger.info("mark1: {}", buf.mark());

        buf.flip();  //还原position
        buf.limit(100);     //增加接收数据量
        socketChannel.read(buf);

        buf.flip();  //还原position
        // 将缓冲器中接收到的值按localCharset格式编码保存
        String receivedRequestData = Charset.forName("UTF-8").newDecoder().decode(buf).toString();
        logger.info("接收到服务端的请求数据: {}", receivedRequestData);

        buf.flip();  //还原position
        socketChannel.close();

    }

}
