package com.rainc.job.core.thread;

import com.rainc.job.core.biz.model.HandleCallbackParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.executor.RaincJobExecutor;
import com.rainc.job.core.handler.IJobHandler;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @Author rainc
 * @create 2020/12/19 19:02
 */
@Log4j2
public class JobThread extends Thread {
    /**
     * 任务id
     */
    private long jobId;
    /**
     * 任务handler
     */
    private IJobHandler handler;
    /**
     * 触发队列
     */
    private LinkedBlockingQueue<TriggerParam> triggerQueue;
    /**
     * 存储日志id避免重复触发对象
     */
    private Set<Long> triggerLogIdSet;

    private volatile boolean toStop = false;
    /**
     * 停止原因
     */
    private String stopReason;
    /**
     * 是否有任务在运行
     */
    private boolean running = false;
    Future<Boolean> future = null;
    private boolean isConcurrent;
    private Set<Future<Boolean>> concurrentTaskSet;
    private Semaphore semaphore;


    /**
     * 空闲计数
     */
    private int idleTimes = 0;

    public JobThread(long jobId, IJobHandler handler) {
        this.jobId = jobId;
        this.handler = handler;
        this.triggerQueue = new LinkedBlockingQueue<>();
        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<>());
    }

    public void setConcurrent(boolean isConcurrent) {
        this.isConcurrent = isConcurrent;
        if (isConcurrent && concurrentTaskSet == null) {
            semaphore = null;
            concurrentTaskSet = Collections.synchronizedSet(new HashSet<>());
        } else if (!isConcurrent) {
            concurrentTaskSet = null;
            semaphore = new Semaphore(0);
        }
    }

    public IJobHandler getHandler() {
        return handler;
    }

    /**
     * 放入新的触发进入队列
     *
     * @param triggerParam
     * @return
     */
    public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
        // 检查日志id是否已经存在
        if (triggerLogIdSet.contains(triggerParam.getLogId())) {
            log.info(">>>>>>>>>>> repeate trigger job, logId:{}", triggerParam.getLogId());
            return new ReturnT<>(ReturnT.FAIL_CODE, "repeate trigger job, logId:" + triggerParam.getLogId());
        }

        triggerLogIdSet.add(triggerParam.getLogId());
        triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
    }

    /**
     * 停止任务线程
     *
     * @param stopReason
     */
    public void toStop(String stopReason) {
        /**
         * Thread.interrupt只支持终止线程的阻塞状态(wait、join、sleep)，
         * 在阻塞出抛出InterruptedException异常,但是并不会终止运行的线程本身；
         * 所以需要注意，此处彻底销毁本线程，需要通过共享变量方式；
         */
        this.toStop = true;
        this.stopReason = stopReason;
        //如果任务还在执行，则取消任务
        if (this.future != null && !this.future.isDone()) {
            future.cancel(true);
        }
    }


    /**
     * 是否有任务在运行或者在队列中
     *
     * @return
     */
    public boolean isRunningOrHasQueue() {
        return running || triggerQueue.size() > 0 || (concurrentTaskSet != null && concurrentTaskSet.size() > 0);
    }

    @Override
    public void run() {
        while (!toStop) {
            running = false;
            idleTimes++;
            this.future = null;
            TriggerParam triggerParam = null;
            long l = System.currentTimeMillis();
            try {
                // 读取阻塞队列，没有数据阻塞3秒空闲时间
                triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
                if (triggerParam != null) {
                    //如果有触发，修改运行状态，并重置空闲时间
                    running = true;
                    idleTimes = 0;
                    //移除日志id
                    triggerLogIdSet.remove(triggerParam.getLogId());
                    if (isConcurrent) {
                        //如果是并发情况
                        //运行任务
                        this.future = TaskPoolHelper.runTask(triggerParam, handler, null);
                        concurrentTaskSet.add(future);
                        //移除所有已经完成或取消的任务
                        concurrentTaskSet.removeIf(futureTask -> futureTask.isDone() || futureTask.isCancelled());
                    } else {
                        //阻塞情况
                        //运行任务
                        this.future = TaskPoolHelper.runTask(triggerParam, handler, semaphore);
                        //阻塞线程
                        semaphore.acquire();
                    }
                } else {
                    if (idleTimes > 30) {
                        //避免并发问题
                        if (triggerQueue.size() == 0 && (concurrentTaskSet != null && concurrentTaskSet.size() > 0)) {
                            RaincJobExecutor.removeJobThread(jobId, "excutor idel times over limit.");
                        }
                    }
                }
            } catch (Throwable e) {

            } finally {
                if (triggerParam != null) {
                    if (toStop) {
                        ReturnT<String> stopResult = new ReturnT<String>(ReturnT.FAIL_CODE, stopReason + " [job running, killed]");
                        TaskCallbackThread.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), stopResult));
                    }
                }
            }
        }

    }
}
