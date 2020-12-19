package com.rainc.job.core.handler.annotation;

import java.lang.annotation.*;

/**
 * @Author rainc
 * @create 2020/10/23 15:32
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RaincJob {
    String value();
}
