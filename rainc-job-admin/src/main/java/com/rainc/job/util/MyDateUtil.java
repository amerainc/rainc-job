package com.rainc.job.util;

import cn.hutool.core.date.DateUtil;
import com.rainc.job.core.enums.RegistryConfig;

import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/21 22:13
 */
public class MyDateUtil {
    public static Date calDead(Date date) {
        return DateUtil.offsetSecond(date, RegistryConfig.DEAD_TIMEOUT);
    }
}
