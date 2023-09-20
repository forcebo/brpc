package com.lwb.serialize;

/**
 * 序列化器
 */
public interface Serializer {

    /**
     * 抽象用来走序列化的方法
     * @param object 待序列化的对象实例
     * @return byte[]
     */
    byte[] serialize(Object object);

    /**
     * 反序列化的方法
     * @param bytes 待反序列化的字节数组
     * @param clazz 目标类的class对象
     * @param <T> 目标类泛型
     * @return 目标实例
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
