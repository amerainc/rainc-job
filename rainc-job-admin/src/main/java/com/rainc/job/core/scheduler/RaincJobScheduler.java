package com.rainc.job.core.scheduler;

import com.rainc.job.core.biz.factory.ExecutorBizFactory;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.config.RaincJobAdminConfig;
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
     * 初始化
     */
    private static void initAppInfoRepository() {
        Date nowTime = new Date();
        //删除数据库过期执行器
        List<JobRegistryDO> idl = RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository()
                .findAllByUpdateTimeBefore(MyDateUtil.calDead(nowTime));
        if (idl.size() > 0) {
            RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository().deleteAll(idl);
        }
        //初始化执行器缓存
        List<JobRegistryDO> list = RaincJobAdminConfig.getAdminConfig().getJobRegistryRepository().findAll();
        if (list.size() > 0) {
            for (JobRegistryDO jobRegistryDO : list) {
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
        if (validAppNameAndAddress(appName, address)) {
            return null;
        }
        final String addressT = address.trim();
        final String appNameT = appName.trim();
        AppInfo appInfo = appInfoRepository.get(appNameT);
        //appInfo处理
        if (appInfo == null) {
            synchronized (appInfoRepository) {
                appInfo = appInfoRepository.get(appNameT);
                //如果该appName组还未注册，则进行创建
                if (appInfo == null) {
                    appInfo = new AppInfo();
                    appInfo.setAppName(appNameT);
                    appInfoRepository.put(appNameT, appInfo);
                }
            }
        }

        //executorInfo处理
        ExecutorInfo executorInfo = appInfo.getAddressMap().get(addressT);
        if (executorInfo != null) {
            //如果已注册则更新时间
            executorInfo.setUpdateTime(new Date());
            log.debug(">>>>>>>> rainc-job update executor {}", executorInfo);
        } else {
            //否则注册
            executorInfo = ExecutorInfo.builder()
                    .address(addressT)
                    //自动注册创建时间，否则时间为空
                    .updateTime(isAuto ? new Date() : null)
                    //创建biz实例
                    .executorBiz(ExecutorBizFactory.createExecutorBiz(addressT, RaincJobAdminConfig.getAdminConfig().getAccessToken()))
                    .build();
            appInfo.getAddressMap().put(addressT, executorInfo);
            log.info(">>>>>>>> rainc-job register executor {}", executorInfo);
            //异步刷新执行器handler信息
            if (isAuto) {
                CompletableFuture.runAsync(() -> refreshHandlerList(appNameT, addressT));
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
        if (validAppNameAndAddress(appName, address)) {
            return null;
        }
        address = address.trim();
        appName = appName.trim();
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
        log.info(">>>>>>>> rainc-job remove executor {}", executorInfo);
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
        ReturnT<List<String>> handlers = executor.getExecutorBiz().handlers();
        if (handlers.getCode() == ReturnT.FAIL_CODE) {
            log.info(">>>>>>>> rainc-job refresh handlers fail {} retry...", executor);
            refreshHandlerList(appName, address);
        } else {
            AppInfo appInfo = getAppInfo(appName);
            appInfo.setHandlerList(handlers.getContent());
            log.info(">>>>>>>> rainc-job refresh handlers success {}", handlers.getContent());
        }
    }

    /**
     * 验证appName和地址
     *
     * @param appName
     * @param address
     * @return
     */
    private static boolean validAppNameAndAddress(String appName, String address) {
        return address == null || address.trim().length() == 0 || appName == null || appName.trim().length() == 0;
    }
}
