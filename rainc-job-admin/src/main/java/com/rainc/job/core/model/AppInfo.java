package com.rainc.job.core.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储提供相同功能的执行器组
 *
 * @Author rainc
 * @create 2020/12/11 20:59
 */
@Data
public class AppInfo implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 地址列表
     */
    final private ConcurrentHashMap<String, ExecutorInfo> addressMap = new ConcurrentHashMap<>();

    /**
     * 可执行handler列表
     */
    private List<String> handlerList;
}
