package com.rainc.job.router;

import com.rainc.job.router.strategy.ExecutorRouteFirst;
import com.rainc.job.router.strategy.ExecutorRouteRound;
import lombok.Getter;

/**
 * @Author rainc
 * @create 2020/12/13 21:04
 */
@Getter
public enum ExecutorRouteStrategyEnum {
    /**
     * 第一个
     */
    FIRST("第一个", new ExecutorRouteFirst()),
    /**
     * 轮询
     */
    ROUND("轮询", new ExecutorRouteRound()),
    /**
     * 分片
     */
    SHARDING_BROADCAST("分片", null);

    private String title;
    private ExecutorRouter router;

    ExecutorRouteStrategyEnum(String title, ExecutorRouter router) {
        this.title = title;
        this.router = router;
    }

    public static ExecutorRouteStrategyEnum match(String name, ExecutorRouteStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorRouteStrategyEnum item : ExecutorRouteStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
