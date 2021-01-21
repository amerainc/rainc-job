package com.rainc.job.util;

/**
 * @Author rainc
 * @create 2020/12/29 13:59
 */
public class QueryUtil {
    public static String castToLike(String param) {
        return "%{param}%".replace("{param}", param);
    }
}
