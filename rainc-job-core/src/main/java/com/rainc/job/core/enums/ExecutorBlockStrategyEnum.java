package com.rainc.job.core.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author rainc
 * @create 2020/12/19 14:48
 * 阻塞策略
 */
public enum ExecutorBlockStrategyEnum {
    /**
     * 连续任务
     */
    SERIAL_EXECUTION("单机串行"),
    /**
     * 并行任务
     */
    CONCURRENT_EXECUTION("并行"),
    /**
     * 抛弃后来
     */
    DISCARD_LATER("丢弃后续调度"),
    /**
     * 覆盖任务
     */
    COVER_EARLY("覆盖之前调度");
    @Getter
    private  String title;

    ExecutorBlockStrategyEnum(String title) {
        this.title = title;
    }

    private static final Map<String,ExecutorBlockStrategyEnum> ENUMS;
    static {
        ENUMS=Arrays.stream(ExecutorBlockStrategyEnum.values()).collect(Collectors.toMap(ExecutorBlockStrategyEnum::name,e->e));
    }

    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
       return ENUMS.getOrDefault(name,defaultItem);
    }
    public static ExecutorBlockStrategyEnum match(String name) {
        return ENUMS.getOrDefault(name,null);
    }
}
