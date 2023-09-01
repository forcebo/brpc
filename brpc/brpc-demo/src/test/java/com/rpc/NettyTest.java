package com.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

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
}
