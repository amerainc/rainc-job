package com.rainc.job.core.thread;

import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.enums.RegistryConfig;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.model.JobRegistryDO;
import com.rainc.job.util.MyDateUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 执行器注册监听线程
 *
 * @Author rainc
 * @create 2020/12/12 20:24
 */
@Log4j2
public class JobRegistryMonitorHelper {
    private static JobRegistryMonitorHelper instance = new JobRegistryMonitorHelper();

    public static JobRegistryMonitorHelper getInstance() {
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;

    public void start() {
        registryThread = new Thread(() -> {

            while (!toStop) {
                try {

                    Date nowTime = new Date();
                    //删除数据库过期执行器
                    List<JobRegistryDO> idl = RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository()
                            .findAllByUpdateTimeBefore(MyDateUtil.calDead(nowTime));
                    if (idl.size() > 0) {
                        RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository().deleteAll(idl);
                    }
                    //删除缓存中失效执行器
                    Collection<AppInfo> allAppInfo = RaincJobScheduler.getAllAppInfo();
                    for (AppInfo appInfo : allAppInfo) {
                        Collection<ExecutorInfo> executorInfos = appInfo.getAddressMap().values();
                        for (ExecutorInfo executorInfo : executorInfos) {
                            if (executorInfo.getUpdateTime() == null) {
                                //无更新时间的执行器为手动注册执行器，不会失效
                                continue;
                            }
                            if (nowTime.getTime() - executorInfo.getUpdateTime().getTime() > TimeUnit.SECONDS.toMillis(RegistryConfig.DEAD_TIMEOUT)) {
                                log.info(">>>>>>>> rainc-job executor remove{}", executorInfo);
                                appInfo.getAddressMap().remove(executorInfo.getAddress());
                            }
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(">>>>>>>> rainc-job, job registry monitor thread error", e);
                    }
                }


                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.error(">>>>>>>> rainc-job, job registry monitor thread error", e);
                    }
                }
            }
            log.info(">>>>>>>> rainc-job, job registry monitor thread stop");
        });
        registryThread.setDaemon(true);
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        //中断并等待
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
