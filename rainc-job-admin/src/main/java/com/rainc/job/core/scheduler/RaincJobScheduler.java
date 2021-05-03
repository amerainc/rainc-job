package com.rainc.job.core.scheduler;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.factory.BizFactory;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.thread.*;
import com.rainc.job.model.JobRegistryDO;
import com.rainc.job.util.MyDateUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author rainc
 * @create 2020/12/11 20:44
 */
@Log4j2
public class RaincJobScheduler {
    /**
     * 初始化
     */
    public void init() {
        JobRegistryMonitorHelper.getInstance().start();
        JobTriggerPoolHelper.toStart();
        JobScheduleHelper.getInstance().start();
        JobFailMonitorHelper.getInstance().start();
        JobLogReportHelper.getInstance().start();
        initAppInfoRepository();
    }

    /**
     * 销毁
     */
    public void destroy() {
        JobRegistryMonitorHelper.getInstance().toStop();
        JobTriggerPoolHelper.toStop();
        JobScheduleHelper.getInstance().toStop();
        JobFailMonitorHelper.getInstance().toStop();
        JobLogReportHelper.getInstance().toStop();
    }


    //-----------------------------------appInfoRepository----------------------------

    private static final ConcurrentHashMap<String, AppInfo> appInfoRepository = new ConcurrentHashMap<>();

    /**
     * 初始化appInfo
     */
    private static void initAppInfoRepository() {
        Date nowTime = new Date();
        //删除数据库过期执行器
        List<JobRegistryDO> idl = RaincJobAdminConfig.getAdminConfig()
                .getJobRegistryRepository()
                .findAllByUpdateTimeBefore(MyDateUtil.calDead(nowTime));
        //删除过期执行器
        if (idl.size() > 0) {
            RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository().deleteAll(idl);
        }
        //初始化执行器缓存
        List<JobRegistryDO> jobRegistryDOList = RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository().findAll();
        if (jobRegistryDOList.size() > 0) {
            for (JobRegistryDO jobRegistryDO : jobRegistryDOList) {
                registerExecutor(jobRegistryDO.getAppName(), jobRegistryDO.getAppName(), true);
            }
        }
    }


    /**
     * 注册执行器
     *
     * @param appName appName
     * @param address 注册地址
     * @param isAuto  是否为自动注册
     * @return
     */
    public static ExecutorInfo registerExecutor(String appName, String address, boolean isAuto) {
        if (StrUtil.hasBlank(appName, address)) {
            return null;
        }
        AppInfo appInfo = appInfoRepository.get(appName);
        //appInfo处理
        if (appInfo == null) {
            synchronized (appInfoRepository) {
                appInfo = appInfoRepository.get(appName);
                //如果该appName组还未注册，则进行创建
                if (appInfo == null) {
                    appInfo = new AppInfo();
                    appInfo.setAppName(appName);
                    appInfoRepository.put(appName, appInfo);
                }
            }
        }

        //executorInfo处理
        ExecutorInfo executorInfo = appInfo.getAddressMap().get(address);
        if (executorInfo != null) {
            //如果已注册则更新时间
            executorInfo.setUpdateTime(new Date());
            log.debug(JobLogPrefix.PREFIX+"更新执行器 {}", executorInfo);
        } else {
            //否则注册
            executorInfo = ExecutorInfo.builder()
                    .address(address)
                    //自动注册创建时间，否则时间为空
                    .updateTime(isAuto ? new Date() : null)
                    //创建biz实例
                    .executorBiz(BizFactory.createBiz(address, RaincJobAdminConfig.getAdminConfig().getAccessToken(), ExecutorBiz.class))
                    .build();
            appInfo.getAddressMap().put(address, executorInfo);
            log.info(JobLogPrefix.PREFIX+"注册执行器 {}", executorInfo);
            //异步刷新执行器handler信息
            if (isAuto) {
                CompletableFuture.runAsync(() -> refreshHandlerList(appName, address));
            }
        }
        return executorInfo;
    }

    public static AppInfo getAppInfo(String appName) {
        appName = appName.trim();
        return appInfoRepository.get(appName);
    }

    public static Collection<AppInfo> getAllAppInfo() {
        return appInfoRepository.values();
    }

    /**
     * 取得执行器
     *
     * @param appName appName
     * @param address 执行器地址
     * @return 执行器
     */
    public static ExecutorInfo getExecutor(String appName, String address) {
        if (StrUtil.hasBlank(appName, address)) {
            return null;
        }
        //查看本地缓存
        AppInfo appInfo = appInfoRepository.get(appName);
        if (appInfo == null) {
            //没有则注册
            return registerExecutor(appName, address, false);
        }
        //查看本地缓存
        ExecutorInfo executorInfo = appInfoRepository.get(appName).getAddressMap().get(address);
        //没有则注册
        if (executorInfo == null) {
            return registerExecutor(appName, address, false);
        }
        return appInfoRepository.get(appName).getAddressMap().get(address);
    }


    /**
     * 移除执行器
     *
     * @param appName
     * @param address
     * @return
     */
    public static ExecutorInfo removeExecutor(String appName, String address) {
        AppInfo appInfo = appInfoRepository.get(appName);
        if (appInfo == null) {
            return null;
        }
        ExecutorInfo executorInfo = appInfo.getAddressMap().remove(address);
        log.info(JobLogPrefix.PREFIX+"移除执行器 {}", executorInfo);
        return executorInfo;
    }

    /**
     * 刷新handler列表
     *
     * @param appName
     * @param address
     */
    public static void refreshHandlerList(String appName, String address) {
        ExecutorInfo executor = getExecutor(appName, address);
        if (executor == null) {
            return;
        }
        ReturnT<List<String>> handlers = executor.getExecutorBiz().handlers();
        if (handlers.getCode() == ReturnT.FAIL_CODE) {
            log.info(JobLogPrefix.PREFIX+"刷新任务处理器失败 {} 重试...", executor);
            refreshHandlerList(appName, address);
        } else {
            AppInfo appInfo = getAppInfo(appName);
            appInfo.setHandlerList(handlers.getContent());
            log.info(JobLogPrefix.PREFIX+"刷新任务处理器成功 {}", handlers.getContent());
        }
    }
}
