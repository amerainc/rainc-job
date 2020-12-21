package com.rainc.job.core.thread;

import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.HandleCallbackParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.executor.RaincJobExecutor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 回调线程
 *
 * @Author rainc
 * @create 2020/12/20 10:07
 */
@Log4j2
public class TaskCallbackThread {
    private static TaskCallbackThread instance = new TaskCallbackThread();

    public static TaskCallbackThread getInstance() {
        return instance;
    }

    /**
     * 任务回调队列
     */
    private LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<>();

    public static void pushCallBack(HandleCallbackParam callback) {
        TaskCallbackThread.getInstance().callBackQueue.add(callback);
        log.debug(">>>>>>>>>>> rainc-job, push callback request, logId:{}", callback.getLogId());
    }

    /**
     * 回调线程
     */
    private Thread taskCallBackThread;
    private volatile boolean toStop = false;

    public void start() {
        // valid
        if (RaincJobExecutor.getAdminBizList() == null) {
            log.warn(">>>>>>>>>>> rainc-job, executor callback config fail, adminAddresses is null.");
            return;
        }
        taskCallBackThread = new Thread(() -> {
            // 普通回调
            while (!toStop) {
                try {
                    HandleCallbackParam callback = callBackQueue.take();

                    // 回调参数列表
                    List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                    int drainToNum = getInstance().callBackQueue.drainTo(callbackParamList);
                    callbackParamList.add(callback);

                    // 进行回调
                    if (callbackParamList.size() > 0) {
                        doCallback(callbackParamList);
                    }

                } catch (Exception e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }

            // 最后回调
            try {
                List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                int drainToNum = getInstance().callBackQueue.drainTo(callbackParamList);
                if (callbackParamList.size() > 0) {
                    doCallback(callbackParamList);
                }
            } catch (Exception e) {
                if (!toStop) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info(">>>>>>>>>>> rainc-job, executor callback thread destory.");

        });
        taskCallBackThread.setDaemon(true);
        taskCallBackThread.setName("rainc-job, executor TaskCallbackThread");
        taskCallBackThread.start();
    }

    public void toStop() {
        toStop = true;
        // 停止中断停止线程
        if (taskCallBackThread != null) {
            taskCallBackThread.interrupt();
            try {
                taskCallBackThread.join();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 进行回调
     *
     * @param callbackParamList
     */
    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        for (AdminBiz adminBiz : RaincJobExecutor.getAdminBizList()) {
            try {
                ReturnT<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult != null && ReturnT.SUCCESS_CODE == callbackResult.getCode()) {
                    log.debug(">>>>>> rainc-job log callback finish{}",callbackParamList);
                    //callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                    //callbackRet = true;
                    break;
                } else {
                    //callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Exception e) {
                //callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
            }
        }
    }


}
