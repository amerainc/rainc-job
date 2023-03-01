package com.rainc.job.controller.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private PermissionInterceptor permissionInterceptor;
    @Resource
    private AccessTokenInterceptor accessTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/biz/**");
        registry.addInterceptor(accessTokenInterceptor)
                .addPathPatterns("/biz/**");
    }

}
