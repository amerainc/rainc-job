package com.rainc.job.core.biz.impl;

import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.enums.ExecutorBlockStrategyEnum;
import com.rainc.job.core.executor.RaincJobExecutor;
import com.rainc.job.core.handler.IJobHandler;
import com.rainc.job.core.thread.JobThread;

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
        //载入handler
        JobThread jobThread = RaincJobExecutor.loadJobThread(triggerParam.getJobId());
        IJobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
        String removeOldReason = null;
        // new jobhandler
        IJobHandler newJobHandler = RaincJobExecutor.loadJobHandler(triggerParam.getExecutorHandler());
        // 验证hanlder是否一致

        if (jobThread != null && jobHandler != newJobHandler) {
            // 更换handler需要杀死旧任务线程
            removeOldReason = "change jobhandler or glue type, and terminate the old job thread.";
            jobThread = null;
            jobHandler = null;
        }

        // 验证 handler是否存在
        if (jobHandler == null) {
            jobHandler = newJobHandler;
            if (jobHandler == null) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "job handler [" + triggerParam.getExecutorHandler() + "] not found.");
            }
        }
        ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(triggerParam.getExecutorBlockStrategy(), null);
        //执行器阻塞策略
        if (jobThread != null) {
            if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
                // 如果已经有任务则直接抛弃
                if (jobThread.isRunningOrHasQueue()) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "block strategy effect：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
                }
            } else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
                //如果是覆盖，则杀死运行中的任务线程
                if (jobThread.isRunningOrHasQueue()) {
                    removeOldReason = "block strategy effect：" + ExecutorBlockStrategyEnum.COVER_EARLY.getTitle();
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
            RaincJobExecutor.removeJobThread(id, "scheduling center kill job.");
            return ReturnT.SUCCESS;
        }
        return new ReturnT<>(ReturnT.SUCCESS_CODE, "job thread already killed.");
    }
}
