package com.rainc.job.core.util;

import com.rainc.job.core.enums.AdminBizConfig;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

/**
 * @Author rainc
 * @create 2020/12/19 12:27
 */
public class BizUtil {
    /**
     * 创建Biz
     *
     * @param address     访问地址
     * @param accessToken 验证秘钥
     * @param t           需要创建的对象类
     * @param <T>         需要创建的对象类型
     * @return 需要创建的对象类
     */
    public static <T> T createBiz(final String address, final String accessToken, Class<T> t) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor((requestTemplate -> {
                    requestTemplate
                            .header("connection", "Keep-Alive")
                            .header("Content-Type", "application/json;charset=UTF-8")
                            .header("Accept-Charset", "application/json;charset=UTF-8");
                    if (accessToken != null && accessToken.trim().length() > 0) {
                        requestTemplate.header(AdminBizConfig.XXL_JOB_ACCESS_TOKEN, accessToken);
                    }
                })).target(t, address);
    }
}
