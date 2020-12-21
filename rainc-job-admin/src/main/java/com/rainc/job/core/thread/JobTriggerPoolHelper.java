package com.rainc.job.core.thread;

import com.rainc.job.core.biz.model.ShardingParam;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.trigger.RaincJobTrigger;
import com.rainc.job.core.trigger.TriggerTypeEnum;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务触发线程
 * @Author rainc
 * @create 2020/11/1 15:46
 */
@Log4j2
public class JobTriggerPoolHelper {
    private static JobTriggerPoolHelper helper = new JobTriggerPoolHelper();

    private ThreadPoolExecutor fastTriggerPool = null;
    private ThreadPoolExecutor slowTriggerPool = null;

    private void start() {
        fastTriggerPool = new ThreadPoolExecutor(
                10,
                RaincJobAdminConfig.getAdminConfig().getTriggerPoolFastMax(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                r -> new Thread(r, "rainc-job, admin JobTriggerPoolHelper-fastTriggerPool-" + r.hashCode()));

        slowTriggerPool = new ThreadPoolExecutor(
                10,
                RaincJobAdminConfig.getAdminConfig().gettriggerPoolSlowMax(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                r -> new Thread(r, "rainc-job, admin JobTriggerPoolHelper-slowTriggerPool-" + r.hashCode()));
    }

    /**
     * 分钟
     */
    private long minTim = System.currentTimeMillis() / 60000;
    private final ConcurrentMap<Long, AtomicInteger> jobTimeoutCountMap = new ConcurrentHashMap<>();

    /**
     * 添加触发任务
     *
     * @param jobId                 任务id
     * @param triggerType           触发类型
     * @param failRetryCount        失败重试次数
     * @param executorShardingParam 分片参数
     * @param executorParam         执行参数
     * @param executorList          执行器列表
     */
    private void addTrigger(long jobId,
                            TriggerTypeEnum triggerType,
                            int failRetryCount,
                            ShardingParam executorShardingParam,
                            String executorParam,
                            List<ExecutorInfo> executorList) {
        //线程池
        ThreadPoolExecutor triggerPool = fastTriggerPool;
        AtomicInteger jobTimeoutCount = jobTimeoutCountMap.get(jobId);
        //如果一分钟内超时10次，则进入慢线程池
        if (jobTimeoutCount != null && jobTimeoutCount.get() > 10) {
            triggerPool = slowTriggerPool;
        }

        triggerPool.execute(() -> {
            long start = System.currentTimeMillis();
            try {
                // 触发触发器
                RaincJobTrigger.trigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, executorList);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                // 每个一分钟清空一次超时表
                long minTimNow = System.currentTimeMillis() / 60000;
                if (minTim != minTimNow) {
                    minTim = minTimNow;
                    jobTimeoutCountMap.clear();
                }

                // 添加超时表
                long cost = System.currentTimeMillis() - start;
                if (cost > 500) {       // 超时则次数+1
                    AtomicInteger timeoutCount = jobTimeoutCountMap.putIfAbsent(jobId, new AtomicInteger(1));
                    if (timeoutCount != null) {
                        timeoutCount.incrementAndGet();
                    }
                }

            }
        });
    }


    public void stop() {
        fastTriggerPool.shutdownNow();
        slowTriggerPool.shutdownNow();
        log.info(">>>>>>>> rainc-job trigger thread pool shutdown success.");
    }


    public static void toStart() {
        helper.start();
    }

    public static void toStop() {
        helper.stop();
    }

    public static void trigger(long jobId,
                               TriggerTypeEnum triggerType,
                               int failRetryCount,
                               ShardingParam executorShardingParam,
                               String executorParam,
                               List<ExecutorInfo> executorList) {
        helper.addTrigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, executorList);
    }

}
