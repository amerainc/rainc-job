package com.rainc.job.core.biz.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.executor.RaincJobExecutor;
import com.rainc.job.core.handler.IJobHandler;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/13 21:54
 */
public class ExecutorBizImpl implements ExecutorBiz {
    public static ExecutorBiz instance = new ExecutorBizImpl();

    public static ExecutorBiz getInstance() {
        return instance;
    }

    @Override
    public ReturnT<List<String>> handlers() {
        return new ReturnT<>(RaincJobExecutor.getHandlerNames());
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        IJobHandler iJobHandler = RaincJobExecutor.loadJobHandler(triggerParam.getExecutorHandler());
        ReturnT<String> result;
        try {
            result = iJobHandler.execute(triggerParam.getExecutorParams());
        } catch (Exception e) {
            result = new ReturnT<String>(ReturnT.FAIL_CODE, ExceptionUtil.getMessage(e));
        }
        return result;
    }

    @Override
    public ReturnT<String> beat() {
        return ReturnT.SUCCESS;
    }
}
