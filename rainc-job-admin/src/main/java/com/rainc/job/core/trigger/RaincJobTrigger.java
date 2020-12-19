package com.rainc.job.core.trigger;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.ShardingParam;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.model.GroupInfo;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.model.JobGroupDO;
import com.rainc.job.model.JobInfoDO;
import com.rainc.job.router.ExecutorRouteStrategyEnum;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @Author rainc
 * @create 2020/11/7 12:07
 */
@Log4j2
public class RaincJobTrigger {

    public static void trigger(long jobId,
                               TriggerTypeEnum triggerType,
                               int failRetryCount,
                               ShardingParam executorShardingParam,
                               String executorParam,
                               List<String> addressList) {
        Optional<JobInfoDO> jobInfoOptional = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository().findById(jobId);
        if (!jobInfoOptional.isPresent()) {
            log.warn(">>>>>>>>>>>> trigger fail, jobId invalid,jobId={}", jobId);
            return;
        }
        JobInfoDO jobInfo = jobInfoOptional.get();

        Optional<JobGroupDO> jobGroupOptional = RaincJobAdminConfig.getAdminConfig().getJobGroupRepository().findById(jobInfo.getJobGroup());
        if (!jobGroupOptional.isPresent()) {
            log.warn(">>>>>>>>>>>> trigger fail, groupId invalid,groupId={}", jobInfo.getJobGroup());
            return;
        }
        GroupInfo groupInfo = GroupInfo.castToInfo(jobGroupOptional.get());

        if (executorParam != null) {
            jobInfo.setExecutorParam(executorParam);
        }

        /**
         * 如果有addressList则放入
         */
        if (addressList != null && addressList.size() > 0) {
            groupInfo.setAddressList(addressList);
        } else if (groupInfo.getAddressList() == null) {
            //如果为空则表示自动注册，从注册表中读取
            AppInfo appInfo = RaincJobScheduler.getAppInfo(groupInfo.getAppName());
            if (appInfo != null) {
                groupInfo.setAddressList(
                        appInfo.getAddressMap().values()
                                .stream()
                                .map((ExecutorInfo::getAddress))
                                .collect(Collectors.toList()));
            }
        }

        //失败重试次数
        int finalFailRetryCount = failRetryCount >= 0 ? failRetryCount : jobInfo.getExecutorFailRetryCount();

        //分片参数
        ShardingParam shardingParam = null;

        //如果存在执行器分片参数，则解析参数
        if (executorShardingParam != null) {
            shardingParam = new ShardingParam();
            shardingParam.setIndex(executorShardingParam.getIndex());
            shardingParam.setTotal(executorShardingParam.getTotal());
        }
/*        if (ExecutorRouteStrategyEnum.SHARDING_BROADCAST == ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null)
                && group.getRegistryList() != null && !group.getRegistryList().isEmpty()
                && shardingParam == null) {
            for (int i = 0; i < group.getRegistryList().size(); i++) {
                processTrigger(group, jobInfo, finalFailRetryCount, triggerType, i, group.getRegistryList().size());
            }
        } else {*/
        //如果没有分片参数则写入
        if (shardingParam == null) {
            shardingParam = new ShardingParam(0, 1);
        }
        processTrigger(groupInfo, jobInfo, finalFailRetryCount, triggerType, shardingParam);
    }


    private static void processTrigger(GroupInfo groupInfo, JobInfoDO jobInfo, int finalFailRetryCount, TriggerTypeEnum triggerType, ShardingParam shardingParam) {
        //初始化参数
        //路由策略
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null);

        //初始化触发参数
        TriggerParam triggerParam = TriggerParam.builder()
                .executorParams(jobInfo.getExecutorParam())
                .executorHandler(jobInfo.getExecutorHandler())
                .executorTimeout(jobInfo.getExecutorTimeOut())
                .build();

        //初始化路由地址
        String address = null;
        ReturnT<String> routeAddressResult = null;
        if (groupInfo.getAddressList() != null && groupInfo.getAddressList().size() > 0) {
            routeAddressResult = executorRouteStrategyEnum.getRouter().route(triggerParam, groupInfo.getAddressList());
            if (routeAddressResult.getCode() == ReturnT.SUCCESS_CODE) {
                address = routeAddressResult.getContent();
            }
        } else {
            routeAddressResult = new ReturnT<String>(ReturnT.FAIL_CODE, "Trigger Fail：registry address is empty");
        }

        //触发远程执行器

    }


    public static ReturnT<String> runExecutor(TriggerParam triggerParam, String address) {
        ReturnT<String> runResult = null;
        try {
            ExecutorBiz executorBiz = RaincJobScheduler.getExecutorBiz(address);
            runResult = executorBiz.run(triggerParam);
        } catch (Exception e) {
            log.error(">>>>>>>>>>> rainc-job trigger error, please check if the executor[{}] is running.", address, e);
            runResult = new ReturnT<String>(ReturnT.FAIL_CODE, ExceptionUtil.getMessage(e));
        }

        StringBuffer runResultSB = new StringBuffer("Trigger Job:");
        runResultSB.append("<br>address:").append(address);
        runResultSB.append("<br>code:").append(runResult.getCode());
        runResultSB.append("<br>msg:").append(runResult.getMsg());

        runResult.setMsg(runResultSB.toString());
        return runResult;
    }
}
