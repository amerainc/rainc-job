package com.rainc.job.core.enums;

import lombok.Getter;

/**
 * @Author rainc
 * @create 2020/12/19 14:48
 */
public enum ExecutorBlockStrategyEnum {
    /**
     * 连续任务
     */
    SERIAL_EXECUTION("连续"),
    /**
     * 并行任务
     */
    CONCURRENT_EXECUTION("并行"),
    /**
     * 抛弃后来
     */
    DISCARD_LATER("抛弃后来"),
    /**
     * 覆盖任务
     */
    COVER_EARLY("覆盖");
    @Getter
    private final String title;

    ExecutorBlockStrategyEnum(String title) {
        this.title = title;
    }

    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorBlockStrategyEnum item : ExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
