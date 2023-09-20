package com.lwb.utils;

import java.util.concurrent.atomic.LongAdder;

public class IdGenerator {
    // 集群模式下生成id唯一
    // 雪花算法
    // 机房号（数据中心）5bit 32
    // 机器号          5bit 32
    // 时间戳 自由选择一个比较近的日期
    // 同一个机房的同一个机器号的同一个时间可能因为并发量很大需要多个id
    // 序列号 12bit  5+5+42+12=64

    //时间戳
    public static final long START_STAMP = DateUtil.get("2022-1-1").getTime();
    //
    public static final long DATA_CENTER_BIT = 5;
    public static final long MACHINE_BIT = 5;
    public static final long SEQUENCE_BIT = 12;

    // 最大值 Math.pow(2,5) - 1
    public static final long DATA_CENTER_MAX = ~(-1L << MACHINE_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;
    //时间戳 （42） 机房号（5）机器号（5）序列号（12）
    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    // 处理时钟回拨问题
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法.");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;

        // 判断时间回拨
        if (timeStamp < lastTimeStamp) {
            throw new RuntimeException("您的服务器进行了时钟回调");
        }
        //对sequenceId做处理, 如果是同一个时间节点，必须自增
        if (timeStamp == lastTimeStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = getNextTimesStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextTimesStamp() {
        // 获取当前的时间戳
        long current = System.currentTimeMillis() - START_STAMP;
        while (current == lastTimeStamp) {
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1, 2);
        for (int i = 0; i < 1000; i++) {
            new Thread( () -> {
                System.out.println(idGenerator.getId());
            }).start();
        }
    }
}
