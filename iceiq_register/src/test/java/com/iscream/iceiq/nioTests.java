package com.iscream.iceiq;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Java_nio测试
 * FileChannel文件通道
 * 残存测试:DatagramChannel
 */
public class nioTests {

    Logger logger = LoggerFactory.getLogger(nioTests.class);

    //    write测试

    /**
     * nio->file写出
     *
     * @throws IOException
     */
    @Test
    public void write() throws IOException {
        File file = new File("C:\\Users\\IScreamIce\\Desktop\\1.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        FileChannel channel = fileOutputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 10);     //分配大小

        String str = "JAVA+1";
        byteBuffer.put(str.getBytes());                              //put to buffer inside

        logger.info("mark: {}", byteBuffer.mark());                  //mark汇总输出                   [pos=6 lim=10240 cap=10240]
        logger.info("position: {}", byteBuffer.position());          //当前位置                       6
        logger.info("byteBuffer: {}", byteBuffer.limit());           //设置与获得多可读&可写多少数据  10240
        logger.info("capacity: {}", byteBuffer.capacity());          //buffer容量                     10240

        byteBuffer.flip();      //flip limit置position,position置0。
        // 读取时,按照position标志的0就能从头开始读,最多可读limit(buffer的大小)个数据,保证数据完整

        logger.info("mark1: {}", byteBuffer.mark());                  //mark汇总输出               [pos=0 lim=6 cap=10240]

        channel.write(byteBuffer);      //管道输出buffer缓冲数据,输出时position下标随着输出位而改变++
        logger.info("mark2: {}", byteBuffer.mark());                  //mark汇总输出               [pos=6 lim=6 cap=10240]
        byteBuffer.clear();             //归位,position置0,limit置buffer大小,保证下次读取数据有效性,position不置0,将产生脏数据[上次残留的数据]
        logger.info("mark3: {}", byteBuffer.mark());                  //mark汇总输出               [pos=0 lim=10240 cap=10240]

        logger.info("mark4: {}", byteBuffer.hasRemaining());           //hasRemaining汇总输出       判断缓冲区是否还有可用数据
        //当position下标移动到尾端时,buffer不再有可用数据,返回false.当未填满buffer时返回true

        logger.info("mark5: {}", byteBuffer.remaining());               //remaining       返回limit-position区间内的长度

        str = "+2";
        byteBuffer.put(str.getBytes());
        logger.info("mark6: {}", byteBuffer.mark());                  //mark汇总输出               [pos=0 lim=10240 cap=10240]
        byteBuffer.rewind();        //将position直接置0
        logger.info("mark6: {}", byteBuffer.mark());                  //mark汇总输出               [pos=0 lim=10240 cap=10240]

        channel.close();
        fileOutputStream.close();
    }


    /**
     * nio->file读入
     *
     * @throws IOException
     */
    //    read测试
    @Test
    public void read() throws IOException {
        File file = new File("C:\\Users\\IScreamIce\\Desktop\\1.txt");
        FileInputStream fileInputStream = new FileInputStream(file);

        FileChannel channel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        logger.info("mark1: {}", byteBuffer.mark());                  //mark汇总输出               [pos=0 lim=10240 cap=10240]

        logger.info("mark1: {}", byteBuffer.mark());                  //mark汇总输出               [pos=0 lim=10240 cap=10240]

        channel.read(byteBuffer);                                     //Stream读取内容到buffer缓冲区中,buffer的position下标往后挪动
        byteBuffer.flip();                                            //position下标挪到0,才能保证读取时是从0开始,limit长度为之前position值
        Charset charset = Charset.defaultCharset();                   //字符编码类,提供了能够将buffer内容转换为字符串的方法

        logger.info("content: {}", charset.decode(byteBuffer).toString());       //decode将buffer缓冲的内容解码成字符串

        channel.close();
        fileInputStream.close();

    }


}
