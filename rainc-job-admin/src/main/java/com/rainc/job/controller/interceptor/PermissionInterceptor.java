package com.rainc.job.controller.interceptor;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.exception.RaincJobException;
import com.rainc.job.controller.annotation.PermissionLimit;
import com.rainc.job.model.JobUserDO;
import com.rainc.job.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限前置拦截器
 *
 * @Author rainc
 * @create 2020/12/16 16:10
 */
@Component
public class PermissionInterceptor implements AsyncHandlerInterceptor {
    private static final String LOGIN_IDENTITY_KEY = "rainc-job-identify";
    @Resource
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //拦截所有方法
        HandlerMethod method = (HandlerMethod) handler;
        //查看是否标有权限注解
        PermissionLimit permission = method.getMethodAnnotation(PermissionLimit.class);
        //默认需要登录
        boolean needLogin = true;
        boolean needAdminuser = false;
        if (permission != null) {
            needLogin = permission.limit();
            needAdminuser = permission.admin();
        }

        if (needLogin) {
            //如果需要登录则检查token
            String token = request.getHeader(LOGIN_IDENTITY_KEY);
            if (StrUtil.isNotBlank(token)) {
                //检查token是否能够登陆
                JobUserDO jobUserDO = userService.ifLogin(token);
                //没有登录
                if (jobUserDO == null) {
                    throw RaincJobException.NOT_LOGIN;
                }
                //没有权限
                if (needAdminuser && jobUserDO.getRole() != 1) {
                    throw RaincJobException.PERMISSION;
                }
                request.setAttribute("jobUserDO", jobUserDO);
            } else {
                throw RaincJobException.NOT_LOGIN;
            }
        }
        return true;
    }
}
