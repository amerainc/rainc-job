package com.rainc.job.core.biz.factory;

import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.enums.AdminBizConfig;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

/**
 * @Author rainc
 * @create 2020/12/12 14:36
 */
public class AdminBizFactory {
    public static AdminBiz createAdminBiz(final String address, final String accessToken) {
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
                })).target(AdminBiz.class, address);
    }
}
