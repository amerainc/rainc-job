package com.rainc.job.core.scheduler;

import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.factory.ExecutorBizFactory;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.thread.JobRegistryMonitorHelper;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author rainc
 * @create 2020/12/11 20:44
 */
@Log4j2
public class RaincJobScheduler {
    public void init() {
        JobRegistryMonitorHelper.getInstance().start();
    }

    public void destroy() {
        JobRegistryMonitorHelper.getInstance().toStop();
    }


    //-----------------------------------appInfoRepository----------------------------

    private static final ConcurrentHashMap<String, AppInfo> appInfoRepository = new ConcurrentHashMap<>();

    /**
     * 注册执行器
     *
     * @param appName
     * @param address
     * @return
     */
    public static ExecutorInfo registerExecutor(String appName, String address) {
        AppInfo appInfo = appInfoRepository.get(appName);
        if (appInfo == null) {
            synchronized (appInfoRepository) {
                appInfo = appInfoRepository.get(appName);
                //如果该appName组还未创建，则进行创建
                if (appInfo == null) {
                    appInfo = new AppInfo();
                    appInfo.setAppName(appName);
                    appInfoRepository.put(appName, appInfo);
                }
            }
        }

        ExecutorInfo executorInfo = appInfo.getAddressMap().get(address);
        if (executorInfo != null) {
            executorInfo.setUpdateTime(new Date());
            log.debug(">>>>>>>> rainc-job update executor {}", executorInfo);
        } else {
            executorInfo = ExecutorInfo.builder()
                    .address(address)
                    .updateTime(new Date())
                    .executorBiz(ExecutorBizFactory.createExecutorBiz(address, RaincJobAdminConfig.getAdminConfig().getAccessToken()))
                    .build();
            appInfo.getAddressMap().put(address, executorInfo);
            log.info(">>>>>>>> rainc-job register executor {}", executorInfo);
            CompletableFuture.runAsync(() -> refreshHandlerList(appName, address));
        }
        return executorInfo;
    }

    public static ExecutorInfo getExecutor(String appName, String address) {
        return appInfoRepository.get(appName).getAddressMap().get(address);
    }

    public static AppInfo getAppInfo(String appName) {
        return appInfoRepository.get(appName);
    }

    public static Collection<AppInfo> getAllAppInfo() {
        return appInfoRepository.values();
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


    //------------------------executor-client-------------------------------
    private static ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<String, ExecutorBiz>();

    public static ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (address == null || address.trim().length() == 0) {
            return null;
        }

        // load-cache
        address = address.trim();
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            return executorBiz;
        }
        synchronized (executorBizRepository) {
            executorBiz = executorBizRepository.get(address);
            if (executorBiz != null) {
                return executorBiz;
            }
            // set-cache
            executorBiz = ExecutorBizFactory.createExecutorBiz(address, RaincJobAdminConfig.getAdminConfig().getAccessToken());
            executorBizRepository.put(address, executorBiz);
        }
        return executorBiz;
    }
}
