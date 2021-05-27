package com.rainc.job.core.router;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.model.ExecutorInfo;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/13 20:59
 */
@Log4j2
public abstract class ExecutorRouter {

    /**
     * 路由执行器
     * @param triggerParam 触发参数
     * @param executorList 执行器列表
     * @return
     */
    public abstract ReturnT<ExecutorInfo> route(TriggerParam triggerParam, List<ExecutorInfo> executorList);
}
