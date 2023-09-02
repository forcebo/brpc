package com.rpc;

import com.rpc.netty.AppClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NettyTest {
    @Test
    public void testCompositeByteBuf() {
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        //通过逻辑组装而不是物理拷贝，实现在jvm中的零拷贝
        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header, body);
    }

    @Test
    public void testWrapper() {
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
        // 共享byte数组的内容，而不是拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
    }

    @Test
    public void testSlice() {
        byte[] buf1 = new byte[1024];
        byte[] buf2 = new byte[1024];

        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf1, buf2);
        ByteBuf buf3 = byteBuf.slice(1,5);
        ByteBuf buf4 = byteBuf.slice(6, 15);
    }

    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("lwb".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);
       //用对象流将对象转为字节数组
        AppClient appClient = new AppClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(appClient);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);
        System.out.println(message);
        printAsBinary(message);
    }

    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);

        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();

        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary.append(binaryString.substring(i, i + 2)).append(" ");
        }

        System.out.println("Binary representation " + formattedBinary);
    }

    @Test
    public void testCompress() throws IOException {
        byte[] buf = new byte[]{12, 12, 12, 12, 12, 25, 25, 26, 12, 12, 12, 12, 12, 25, 25, 26,12, 12, 12, 12, 12, 25, 25, 26
,12, 12, 12, 12, 12, 25, 25, 26,12, 12, 12, 12, 12, 25, 25, 26,12, 12, 12, 12, 12, 25, 25, 26,12, 12, 12, 12, 12, 25, 25, 26,12, 12, 12, 12, 12, 25, 25, 26};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);

        gzipOutputStream.write(buf);
        gzipOutputStream.finish();

        byte[] bytes = baos.toByteArray();
        System.out.println(buf.length + "-->" + bytes.length);
        System.out.println(Arrays.toString(bytes));

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        
        byte[] bytes1 = gzipInputStream.readAllBytes();

        System.out.println(bytes.length + "-->" + bytes1.length);
        System.out.println(Arrays.toString(bytes1));
    }
}