package com.lwb.transport.message;

import java.nio.charset.StandardCharsets;

public class MessageFormatConstant {
    public final static byte[] MAGIC = "brpc".getBytes(StandardCharsets.UTF_8);
    public final static byte VERSION = 1;

    public final static int VERSION_LENGTH = 1;
    //头部信息长度占用的字节数
    public final static int HEADER_FILED_LENGTH = 2;
    //头部信息的长度
    public final static short HEADER_LENGTH = 22;

    public final static int MAX_FRAME_LENGTH = 1024 * 1024;
    //总长度占用的字节数
    public static final int FULL_FILED_LENGTH = 4;
}
