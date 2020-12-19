package com.rainc.job.core.thread;

import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.enums.RegistryConfig;
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
        // valid
        if (appname == null || appname.trim().length() == 0) {
            log.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appname is null.");
            return;
        }
        registryThread = new Thread(() -> {
            //registry
            while (!toStop) {
                try {
                    RegistryParam registryParam = new RegistryParam(appname, address);
                    for (AdminBiz adminBiz : RaincJobExecutor.getAdminBizList()) {
                        try {
                            adminBiz.registry(registryParam);
                            ReturnT<String> registryResult = adminBiz.registry(registryParam);
                            if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = ReturnT.SUCCESS;
                                log.debug(">>>>>>>>>>> rainc-job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                break;
                            } else {
                                log.info(">>>>>>>>>>> rainc-job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            }
                        } catch (Exception e) {
                            log.info(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, e);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }

                //sleep
                try {
                    if (!toStop) {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    }
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.warn(">>>>>>>>>>> xxl-job, executor registry thread interrupted, error msg:{}", e.getMessage());
                    }
                }
            }


            // registry remove
            try {
                RegistryParam registryParam = new RegistryParam(appname, address);
                for (AdminBiz adminBiz : RaincJobExecutor.getAdminBizList()) {
                    try {
                        ReturnT<String> registryResult = adminBiz.registryRemove(registryParam);
                        if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                            registryResult = ReturnT.SUCCESS;
                            log.info(">>>>>>>>>>> rainc-job registry-remove success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            break;
                        } else {
                            log.info(">>>>>>>>>>> rainc-job registry-remove fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.info(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam, e);
                        }

                    }

                }
            } catch (Exception e) {
                if (!toStop) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info(">>>>>>>>>>> rainc-job, executor registry thread destory.");
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
