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
    FIRST("First", new ExecutorRouteFirst()),
    /**
     * 第二个
     */
    ROUND("Round", new ExecutorRouteRound());


    private String title;
    private ExecutorRouter router;

    ExecutorRouteStrategyEnum(String title, ExecutorRouter router) {
        this.title = title;
        this.router = router;
    }

    public static ExecutorRouteStrategyEnum match(String name, ExecutorRouteStrategyEnum defaultItem){
        if (name != null) {
            for (ExecutorRouteStrategyEnum item: ExecutorRouteStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
