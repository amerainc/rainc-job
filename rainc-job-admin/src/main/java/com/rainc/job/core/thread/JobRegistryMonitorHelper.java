package com.rainc.job.core.thread;

import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.core.constant.RegistryConfig;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.model.GroupInfo;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.model.JobRegistryDO;
import com.rainc.job.util.MyDateUtil;
import lombok.extern.log4j.Log4j2;

import java.util.*;
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
                     RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository()
                            .deleteAllByUpdateTimeBefore(MyDateUtil.calDead(nowTime));
                    //删除缓存中失效执行器
                    Collection<AppInfo> allAppInfo = RaincJobScheduler.getAllAppInfo();
                    //取得所有手动注册的执行器
                    Set<ExecutorInfo> executorSet = new HashSet<>();
                    RaincJobAdminConfig.getAdminConfig().getJobGroupRepository().findAll()
                            .stream().map(GroupInfo::castToInfo)
                            .forEach((groupInfo -> {
                                if (!groupInfo.isAuto()) {
                                    executorSet.addAll(groupInfo.getExecutorList());
                                }
                            }));
                    for (AppInfo appInfo : allAppInfo) {
                        Collection<ExecutorInfo> executorInfos = appInfo.getAddressMap().values();
                        for (ExecutorInfo executorInfo : executorInfos) {
                            if (executorInfo.getUpdateTime() == null) {
                                //无更新时间的执行器为手动注册执行器
                                if (!executorSet.contains(executorInfo)) {
                                    //如果已经没有手动注册该执行器的信息，则移除该执行器
                                    appInfo.getAddressMap().remove(executorInfo.getAddress());
                                }
                                continue;
                            }
                            //如果是自动注册，则检查执行器是否失效
                            if (nowTime.getTime() - executorInfo.getUpdateTime().getTime() > TimeUnit.SECONDS.toMillis(RegistryConfig.DEAD_TIMEOUT)) {
                                log.info(JobLogPrefix.PREFIX + "移除执行器{}", executorInfo);
                                appInfo.getAddressMap().remove(executorInfo.getAddress());
                            }
                        }
                    }

                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(JobLogPrefix.PREFIX + "执行器监听线程错误:", e);
                    }
                }
            }
            log.info(JobLogPrefix.PREFIX + "执行器监听线程停止");
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
