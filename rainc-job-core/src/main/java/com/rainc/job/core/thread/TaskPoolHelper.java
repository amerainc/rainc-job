package com.rainc.job.core.thread;

import com.rainc.job.core.biz.model.HandleCallbackParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.core.context.RaincJobContext;
import com.rainc.job.core.handler.IJobHandler;
import lombok.extern.log4j.Log4j2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

/**
 * @Author rainc
 * @create 2020/12/19 19:38
 */
@Log4j2
public class TaskPoolHelper {
    private static TaskPoolHelper helper = new TaskPoolHelper();

    private ExecutorService taskPool = null;

    private void start(int taskPoolMax) {
        taskPool = new ThreadPoolExecutor(
                10,
                taskPoolMax,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                r -> new Thread(r, "rainc-job, executor TaskPoolHelper-taskPool-" + r.hashCode()));
    }

    /**
     * 添加任务
     *
     * @param triggerParam 任务触发参数
     * @param handler      任务执行者
     * @param semaphore    信号量
     * @return
     */
    private Future<Boolean> addTask(TriggerParam triggerParam, IJobHandler handler, Semaphore semaphore) {
        //如果有锁和条件，表示阻塞任务
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            boolean isCancel = false;
            ReturnT<String> executeResult = null;
            try {
                //初始化上下文
                RaincJobContext raincJobContext = new RaincJobContext(
                        triggerParam.getJobId(),
                        triggerParam.getExecutorParams(),
                        triggerParam.getShardingParam()
                );
                //设置上下文
                RaincJobContext.setRaincJobContext(raincJobContext);
                if (triggerParam.getExecutorTimeout() > 0) {
                    //如果有超时时间则表示超时任务
                    FutureTask<ReturnT<String>> futureTask = new FutureTask<>(() -> {
                        //设置上下文
                        RaincJobContext.setRaincJobContext(raincJobContext);
                        ReturnT<String> returnT;
                        try {
                            returnT = handler.execute(triggerParam.getExecutorParams());
                        } finally {
                            //移除上下文
                            RaincJobContext.removeContext();
                        }
                        return returnT;
                    });

                    taskPool.execute(futureTask);
                    try {
                        //阻塞获取
                        futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        executeResult = new ReturnT<>(IJobHandler.FAIL_TIMEOUT, "任务处理超时");
                        log.info(JobLogPrefix.PREFIX+"任务处理超时");
                    } finally {
                        //中断该任务线程
                        futureTask.cancel(true);
                    }
                } else {
                    //直接执行
                    executeResult = handler.execute(triggerParam.getExecutorParams());
                }

                if (executeResult == null) {
                    executeResult = ReturnT.FAIL;
                } else {
                    executeResult.setMsg(
                            (executeResult.getMsg() != null && executeResult.getMsg().length() > 50000)
                                    ? executeResult.getMsg().substring(0, 50000).concat("...")
                                    : executeResult.getMsg());
                    executeResult.setContent(null);    // limit obj size
                }
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    e = (Exception) ((InvocationTargetException) e).getTargetException();
                    if (e instanceof InterruptedException) {
                        isCancel = true;
                    }
                }
                if (!isCancel) {
                    StringWriter stringWriter = new StringWriter();
                    e.printStackTrace(new PrintWriter(stringWriter));
                    String errorMsg = stringWriter.toString();
                    executeResult = new ReturnT<>(ReturnT.FAIL_CODE, errorMsg);
                    e.printStackTrace();
                }
                return false;
            } finally {
                if (!isCancel) {
                    //进行回调
                    HandleCallbackParam handleCallbackParam = new HandleCallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), executeResult);
                    TaskCallbackThread.pushCallBack(handleCallbackParam);
                }
                //如果是阻塞任务，则通知阻塞线程
                if (semaphore != null) {
                    semaphore.release();
                }
                //移除上下文，防止线程池污染
                RaincJobContext.removeContext();
            }
            return true;
        });
        taskPool.submit(task);
        return task;
    }


    public void stop() {
        taskPool.shutdownNow();
        log.info(JobLogPrefix.PREFIX+"任务线程池关闭");
    }

    public static void toStart(int taskPoolMax) {
        helper.start(taskPoolMax);
    }

    public static void toStop() {
        helper.stop();
    }

    /**
     * 运行任务
     *
     * @param triggerParam
     * @param handler
     * @param semaphore
     * @return
     */
    public static Future<Boolean> runTask(TriggerParam triggerParam, IJobHandler handler, Semaphore semaphore) {
        return helper.addTask(triggerParam, handler, semaphore);
    }
}
