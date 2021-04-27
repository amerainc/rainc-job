package com.rainc.job.core.constant;

/**
 * @Author rainc
 * @create 2020/12/8 9:54
 * 注册信息常量
 */
public interface RegistryConfig {
    /**
     * 心跳时间
     */
    int BEAT_TIMEOUT = 30;
    /**
     * 死亡时间
     */
    int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;
}
