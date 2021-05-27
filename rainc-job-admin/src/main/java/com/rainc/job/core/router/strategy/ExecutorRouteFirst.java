package com.rainc.job.core.router.strategy;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.router.ExecutorRouter;

import java.util.List;

/**
 * 第一个
 *
 * @Author rainc
 * @create 2020/12/13 21:01
 */
public class ExecutorRouteFirst extends ExecutorRouter {
    @Override
    public ReturnT<ExecutorInfo> route(TriggerParam triggerParam, List<ExecutorInfo> executorInfoListList) {
        return new ReturnT<>(executorInfoListList.get(0));
    }
}
