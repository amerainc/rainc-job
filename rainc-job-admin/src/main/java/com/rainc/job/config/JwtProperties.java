package com.rainc.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author rainc
 * @create 2020/12/24 13:05
 */
@Data
@Component
@ConfigurationProperties(prefix = "rainc.job.admin.jwt")
public class JwtProperties {
    /**
     * 秘钥
     */
    private String secret;
    /**
     * 失效时间 秒
     */
    private long expire;
}
