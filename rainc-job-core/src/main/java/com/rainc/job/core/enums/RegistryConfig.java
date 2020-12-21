package com.rainc.job.core.enums;

/**
 * @Author rainc
 * @create 2020/12/8 9:54
 */
public class RegistryConfig {
    /**
     * 心跳时间
     */
    public static final int BEAT_TIMEOUT = 30;
    /**
     * 死亡时间
     */
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;
}
