package com.rainc.job.core.thread;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.core.constant.RegistryConfig;
import com.rainc.job.core.executor.RaincJobExecutor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

/**
 * @Author rainc
 * @create 2020/12/12 12:48
 */
@Log4j2
public class ExecutorRegistryThread {
    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();

    public static ExecutorRegistryThread getInstance() {
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;

    public void start(final String appname, final String address) {
        // 验证appname
        if (StrUtil.isBlank(appname)) {
            log.warn(JobLogPrefix.PREFIX + "执行器注册配置错误，appname不能为空");
            return;
        }
        registryThread = new Thread(() -> {
            //注册检测
            while (!toStop) {
                try {
                    RegistryParam registryParam = new RegistryParam(appname, address);
                    for (AdminBiz adminBiz : RaincJobExecutor.getAdminBizList()) {
                        try {
                            adminBiz.registry(registryParam);
                            ReturnT<String> registryResult = adminBiz.registry(registryParam);
                            if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = ReturnT.SUCCESS;
                                log.debug(JobLogPrefix.PREFIX + "注册成功, registryParam:{}, registryResult:{}", registryParam, registryResult);
                                break;
                            } else {
                                log.info(JobLogPrefix.PREFIX + "注册失败, registryParam:{}, registryResult:{}", registryParam, registryResult);
                            }
                        } catch (Exception e) {
                            log.warn(JobLogPrefix.PREFIX + "注册出错, registryParam:{},错误信息:{}", registryParam, e);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }

                //睡眠
                try {
                    if (!toStop) {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    }
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.warn(JobLogPrefix.PREFIX+"执行器注册线程中断,错误信息:{}", e.getMessage());
                    }
                }
            }


            // 移除注册
            try {
                RegistryParam registryParam = new RegistryParam(appname, address);
                for (AdminBiz adminBiz : RaincJobExecutor.getAdminBizList()) {
                    try {
                        ReturnT<String> registryResult = adminBiz.registryRemove(registryParam);
                        if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                            registryResult = ReturnT.SUCCESS;
                            log.info(JobLogPrefix.PREFIX+"主动移除成功, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            break;
                        } else {
                            log.info(JobLogPrefix.PREFIX+"主动移除失败,  registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.warn(JobLogPrefix.PREFIX+"主动移除出错,  registryParam:{},错误信息:{}", registryParam, e);
                        }

                    }

                }
            } catch (Exception e) {
                if (!toStop) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info(JobLogPrefix.PREFIX+"执行器注册线程销毁");
        });

        registryThread.setDaemon(true);
        registryThread.setName("rainc-job, executor ExecutorRegistryThread");
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

}
