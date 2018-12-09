package com.iscream.iceiq.nio.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Service {

    private static Logger logger = LoggerFactory.getLogger(Service.class);

    /*
     * 内部类,负责处理消息
     */
    static class Handler {
        private int bufferSize = 1024;          // 缓冲器容量
        private String localCharset = "UTF-8";  // 编码格式

        public Handler(int bufferSize) {
            this(bufferSize, null);
        }

        public Handler(int bufferSize, String localCharset) {
            if (bufferSize > 0) {
                this.bufferSize = bufferSize;
            }
            if (localCharset != null) {
                this.localCharset = localCharset;
            }
        }

        /*
         * 连接请求处理方法
         */
        public void handleAccept(SelectionKey selectionKey) throws IOException {
            // selectionKey->Iterator迭代的存储选择器上的通道,accept得到接受到此通道套接字的连接
            SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
            socketChannel.configureBlocking(false);     // 设置套接字通道为非阻塞模式
            // OP_READ 用于套接字接受操作的操作集位，设置缓冲区容量大小。连接建立成功后OP_READ建立可读的操作,键值对注册
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
        }

        /*
         * 读请求处理方法
         */
        public void handleRead(SelectionKey selectionKey) throws IOException {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();       // 获取套接字通道
            ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();     //获得已附加到此键的对象
            byteBuffer.clear();     //清除此缓冲区。将位置设置为 0
            logger.info("mark: {}", byteBuffer.mark());
            if (socketChannel.read(byteBuffer) == -1) {     // 没有内容则关闭通道
                socketChannel.close();
            } else {
                byteBuffer.flip();  //还原position
                // 将缓冲器中接收到的值按localCharset格式编码保存
                String receivedRequestData = Charset.forName(localCharset).newDecoder().decode(byteBuffer).toString();
                logger.info("接收到客户端的请求数据: {}", receivedRequestData);
                String responseData = "已接收到你的请求数据，响应数据为：OK !";    // 返回响应数据给客户端
                byteBuffer = ByteBuffer.wrap(responseData.getBytes(localCharset));
                socketChannel.write(byteBuffer);
                socketChannel.close();      // 关闭通道
            }
        }
    }

    public static void main(String[] args) {
        try {
            //ServerSocketChannel：可以监听新进来的TCP连接。对每一个新进来的连接都会创建一个SocketChannel，类比ServerSocket创建的的Socket
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();   //打开套接字通道
            serverSocketChannel.socket().bind(new InetSocketAddress(8080));   //获取与此通道关联的服务器套接字,监听端口
            serverSocketChannel.configureBlocking(false);   //调整此通道的阻塞模式,设置为非阻塞模式
            Selector selector = Selector.open();            //打开选择器
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);     //用于套接字接受操作的操作集位，将通道注册到选择器中
            Handler handler = new Handler(1024);    // 创建处理器

            while (true) {
                // 等待请求，每次等待阻塞3s，超过3s则向下(IF)内执行。若传入0或不传值，则在接收到请求前一直阻塞。向下执行与一直阻塞
                if (selector.select(0) == 0) {
                    logger.info("MSG: {}", "等待请求超时");
                    continue;
                }

                //客户端请求并发送数据过程,经过了两次输出。说明一次是请求连接请求,一次是请求发送数据。2次请求
                logger.info("MSG: {}", "收到请求,开始请求");

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();    // 获取待处理的选择键集合
                while (keyIterator.hasNext()) {     //迭代选择器中的处理者进行请求处理,目前只有一个处理者
                    SelectionKey selectionKey = keyIterator.next();
                    try {
                        if (selectionKey.isAcceptable()) {      // 如果是连接请求，调用处理器的连接处理方法
                            handler.handleAccept(selectionKey);
                        }
                        if (selectionKey.isReadable()) {        // 如果是读请求，调用对应的读方法
                            handler.handleRead(selectionKey);
                        }
                    } catch (IOException e) {
                        keyIterator.remove();
                        continue;
                    }
                }
                // 处理完毕从待处理集合移除该选择键
                keyIterator.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}