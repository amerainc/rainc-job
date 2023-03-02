package com.rainc.job.core.biz.impl;

import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.enums.ExecutorBlockStrategyEnum;
import com.rainc.job.core.executor.RaincJobExecutor;
import com.rainc.job.core.handler.IJobHandler;
import com.rainc.job.core.thread.JobThread;

import java.util.List;
import java.util.Optional;

/**
 * @Author rainc
 * @create 2020/12/13 21:54
 * 执行器业务接口实现类
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
        //载入旧任务线程
        JobThread jobThread = RaincJobExecutor.loadJobThread(triggerParam.getJobId());
        //载入旧任务处理器
        IJobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
        String removeOldReason = null;

        // 载入新任务处理器
        IJobHandler newJobHandler = RaincJobExecutor.loadJobHandler(triggerParam.getExecutorHandler());
        //验证新任务处理器是否存在
        if (newJobHandler==null){
            return new ReturnT<>(ReturnT.FAIL_CODE, "找不到任务处理器 [" + triggerParam.getExecutorHandler() + "]");
        }

        // 验证任务处理器是否一致
        if (jobHandler != newJobHandler) {
            // 更换任务处理器需要销毁旧任务线程
            removeOldReason = "变更任务处理器并销毁旧任务线程。";
            jobThread = null;
            jobHandler = newJobHandler;
        }
        //载入阻塞策略
        ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(triggerParam.getExecutorBlockStrategy(), null);
        //执行器阻塞策略
        if (jobThread != null) {
            if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
                // 如果已经有任务则直接抛弃
                if (jobThread.isRunningOrHasQueue()) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "阻塞策略：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
                }
            } else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
                //如果是覆盖，则杀死运行中的任务线程
                if (jobThread.isRunningOrHasQueue()) {
                    removeOldReason = "阻塞策略：" + ExecutorBlockStrategyEnum.COVER_EARLY.getTitle();
                    jobThread = null;
                }
            }
        }

        if (jobThread == null) {
            jobThread = RaincJobExecutor.registJobThread(triggerParam.getJobId(), jobHandler, removeOldReason);
        }
        jobThread.setConcurrent(ExecutorBlockStrategyEnum.CONCURRENT_EXECUTION == blockStrategy);
        return jobThread.pushTriggerQueue(triggerParam);
    }

    @Override
    public ReturnT<String> beat() {
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> kill(long id) {
        JobThread jobThread = RaincJobExecutor.loadJobThread(id);
        if (jobThread != null) {
            RaincJobExecutor.removeJobThread(id, "调度中心销毁任务。");
            return ReturnT.SUCCESS;
        }
        return new ReturnT<>(ReturnT.SUCCESS_CODE, "任务线程已经销毁。");
    }
}
