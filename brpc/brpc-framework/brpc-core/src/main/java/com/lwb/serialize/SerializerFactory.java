package com.lwb.serialize;

import com.lwb.serialize.impl.JDKSerializer;
import com.lwb.serialize.impl.JsonSerializer;

import java.util.concurrent.ConcurrentHashMap;

public class SerializerFactory {
    private final static ConcurrentHashMap<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JDKSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SERIALIZER_CACHE.put("jdk", jdk);
        SERIALIZER_CACHE.put("json", json);
        SERIALIZER_CACHE_CODE.put((byte)1, jdk);
        SERIALIZER_CACHE_CODE.put((byte)2,json);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeType 序列化的类型
     * @return
     */
    public static SerializerWrapper getSerializer(String serializeType) {
        return SERIALIZER_CACHE.get(serializeType.toLowerCase());
    }

    public static SerializerWrapper getSerializer(byte serializeCode) {
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }
}
