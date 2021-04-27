package com.rainc.job.core.biz.factory;

import com.rainc.job.core.constant.AdminBizConfig;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

/**
 * @Author rainc
 * @create 2021/2/1 17:28
 * biz工厂
 */
public class BizFactory {
    public static <T> T createBiz(final String address, final String accessToken, final Class<T> t) {
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
