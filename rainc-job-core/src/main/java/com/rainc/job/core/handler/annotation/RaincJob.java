package com.rainc.job.core.handler.annotation;

import java.lang.annotation.*;

/**
 * @Author rainc
 * @create 2020/10/23 15:32
 * rainc-job函数类任务处理器注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RaincJob {
    String value();
}
