package com.rainc.job.controller.interceptor;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.enums.AdminBizConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author rainc
 * @create 2021/1/21 19:30
 */

@Component
public class AccessTokenInteceptor implements AsyncHandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (StrUtil.isBlank(RaincJobAdminConfig.getAdminConfig().getAccessToken())) {
            return true;
        }
        String accessToken = request.getHeader(AdminBizConfig.XXL_JOB_ACCESS_TOKEN);
        return RaincJobAdminConfig.getAdminConfig().getAccessToken().equals(accessToken);
    }
}
