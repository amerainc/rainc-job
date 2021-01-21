package com.rainc.job.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限控制注解
 *
 * @Author rainc
 * @create 2020/12/24 17:36
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionLimit {
    /**
     * 是否需要登录权限
     *
     * @return
     */
    boolean limit() default true;

    /**
     * 是否需要管理员权限
     *
     * @return
     */
    boolean admin() default false;
}
