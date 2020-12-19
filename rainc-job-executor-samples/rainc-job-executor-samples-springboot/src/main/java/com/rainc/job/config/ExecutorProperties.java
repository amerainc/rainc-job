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
    private String adminAddresses;
    private String accessToken;
    private String appName;
    private String address;
    private String ip;
    private int port;
}
