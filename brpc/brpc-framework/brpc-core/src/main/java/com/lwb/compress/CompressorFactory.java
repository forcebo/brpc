package com.lwb.compress;

import com.lwb.compress.impl.GzipCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressorFactory {
    private final static ConcurrentHashMap<String, CompressWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, CompressWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        CompressWrapper gzip = new CompressWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte)1, gzip);

    }

    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressorType 序列化的类型
     * @return
     */
    public static CompressWrapper getCompressor(String compressorType) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE.get(compressorType.toLowerCase());
        if (compressWrapper == null) {
            log.error("未找到您配置的【{}】压缩算法，默认使用gzip算法", compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressWrapper;
    }

    public static CompressWrapper getCompressor(byte compressorCode) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        if (compressWrapper == null) {
            log.error("未找到您配置的编号为【{}】压缩算法，默认使用gzip算法", compressorCode);
            return COMPRESSOR_CACHE_CODE.get((byte) 1);
        }
        return compressWrapper;
    }
}
