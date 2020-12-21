package com.rainc.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author rainc
 * @create 2020/10/25 12:06
 */
@Data
@Component
@ConfigurationProperties(prefix = "rainc.job.executor")
public class ExecutorProperties {
    /**
     * 调度中心地址 ,分割多地址
     */
    private String adminAddresses;
    /**
     * 秘钥
     */
    private String accessToken;
    /**
     * appName
     */
    private String appName;
    /**
     * 执行器地址
     */
    private String address;
    /**
     * ip地址
     */
    private String ip;
    /**
     * 端口地址
     */
    private int port;
    /**
     * 任务线程池
     */
    private int taskPoolMax;
}
