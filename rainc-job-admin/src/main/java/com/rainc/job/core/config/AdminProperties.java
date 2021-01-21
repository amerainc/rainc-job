package com.rainc.job.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author rainc
 * @create 2020/12/12 18:55
 */
@Data
@Component
@ConfigurationProperties(prefix = "rainc.job.admin")
public class AdminProperties {
    private String accessToken;
    /**
     * 快线程
     */
    private int triggerPoolFastMax = 200;
    /**
     * 慢线程
     */
    private int triggerPoolSlowMax = 100;
    /**
     * 日志 保留天数
     */
    private int logretentiondays = 30;
}
