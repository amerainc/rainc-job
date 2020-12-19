package com.rainc.job.router.strategy;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.router.ExecutorRouter;

import java.util.List;

/**
 * 第一个
 *
 * @Author rainc
 * @create 2020/12/13 21:01
 */
public class ExecutorRouteFirst extends ExecutorRouter {
    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        return new ReturnT<String>(addressList.get(0));
    }
}
