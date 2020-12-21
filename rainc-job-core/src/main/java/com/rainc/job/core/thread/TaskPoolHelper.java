package com.rainc.job.core.thread;

import com.rainc.job.core.biz.model.HandleCallbackParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.handler.IJobHandler;
import lombok.extern.log4j.Log4j2;

import java.io.PrintWriter;
import java.io.StringWriter;
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
     * 添加
     *
     * @param triggerParam
     * @param handler
     * @return
     */
    private Future<Boolean> addTask(TriggerParam triggerParam, IJobHandler handler, Semaphore semaphore) {
        //如果有锁和条件，表示阻塞任务
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            ReturnT<String> executeResult = null;
            try {
                if (triggerParam.getExecutorTimeout() > 0) {
                    //如果有超时时间则表示超时任务
                    FutureTask<ReturnT<String>> futureTask = new FutureTask<>(() -> handler.execute(triggerParam.getExecutorParams()));
                    taskPool.execute(futureTask);
                    try {
                        //阻塞获取
                        futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        executeResult = new ReturnT<>(IJobHandler.FAIL_TIMEOUT, "job execute timeout ");
                        log.info(">>>>>>>>>>>>>rainc job test time out");
                    } finally {
                        //中断该任务线程
                        futureTask.cancel(true);
                    }
                } else {
                    //直接执行
                    handler.execute(triggerParam.getExecutorParams());
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
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                String errorMsg = stringWriter.toString();
                executeResult = new ReturnT<>(ReturnT.FAIL_CODE, errorMsg);
                e.printStackTrace();
            } finally {
                HandleCallbackParam handleCallbackParam = new HandleCallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), executeResult);
                TaskCallbackThread.pushCallBack(handleCallbackParam);

                if (semaphore != null) {
                    semaphore.release();
                }
            }
            return true;
        });
        taskPool.submit(task);
        return task;
    }

    public void stop() {
        taskPool.shutdownNow();
        log.info(">>>>>>>> rainc-job task thread pool shutdown success.");
    }

    public static void toStart(int taskPoolMax) {
        helper.start(taskPoolMax);
    }

    public static void toStop() {
        helper.stop();
    }

    public static Future<Boolean> runTask(TriggerParam triggerParam, IJobHandler handler, Semaphore semaphore) {
        return helper.addTask(triggerParam, handler, semaphore);
    }
}
