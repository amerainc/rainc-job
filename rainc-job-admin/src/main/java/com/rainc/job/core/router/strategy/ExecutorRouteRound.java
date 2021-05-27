package com.rainc.job.core.router.strategy;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.router.ExecutorRouter;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询
 *
 * @Author rainc
 * @create 2020/12/13 21:02
 */
public class ExecutorRouteRound extends ExecutorRouter {
    private static final ConcurrentMap<Long, AtomicInteger> routeCountEachJob = new ConcurrentHashMap<>();
    private static long CACHE_VALID_TIME = 0;

    private static int count(long jobId) {
        // 每个24小时重置缓存
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            routeCountEachJob.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        }

        AtomicInteger count = routeCountEachJob.get(jobId);
        if (count == null || count.get() > 1000000) {
            // 初始化时主动Random一次，缓解首次压力
            count = new AtomicInteger(new Random().nextInt(100));
        } else {
            // count++
            count.addAndGet(1);
        }
        routeCountEachJob.put(jobId, count);
        return count.get();
    }

    @Override
    public ReturnT<ExecutorInfo> route(TriggerParam triggerParam, List<ExecutorInfo> executorListList) {
        ExecutorInfo executorInfo = executorListList.get(count(triggerParam.getJobId()) % executorListList.size());
        return new ReturnT<>(executorInfo);
    }
}
