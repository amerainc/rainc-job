package com.rainc.job.core.trigger;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.ShardingParam;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.enums.ExecutorBlockStrategyEnum;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.model.GroupInfo;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.core.util.IpUtil;
import com.rainc.job.model.JobGroupDO;
import com.rainc.job.model.JobInfoDO;
import com.rainc.job.model.JobLogDO;
import com.rainc.job.router.ExecutorRouteStrategyEnum;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * @Author rainc
 * @create 2020/11/7 12:07
 */
@Log4j2
public class RaincJobTrigger {

    /**
     * 执行任务
     *
     * @param jobId                 任务id
     * @param triggerType           任务类型
     * @param failRetryCount        失败重试次数
     * @param executorShardingParam 分配参数
     * @param executorParam         执行参数
     * @param executorList          执行器列表
     */
    public static void trigger(long jobId,
                               TriggerTypeEnum triggerType,
                               int failRetryCount,
                               ShardingParam executorShardingParam,
                               String executorParam,
                               List<ExecutorInfo> executorList) {
        //查询任务信息
        Optional<JobInfoDO> jobInfoOptional = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository().findById(jobId);
        if (!jobInfoOptional.isPresent()) {
            log.warn(">>>>>>>>>>>> trigger fail, jobId invalid,jobId={}", jobId);
            return;
        }
        JobInfoDO jobInfo = jobInfoOptional.get();
        //查询任务分组信息
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
        if (executorList != null && executorList.size() > 0) {
            groupInfo.setExecutorList(executorList);
        } else if (groupInfo.isAuto()) {
            //如果为自动注册，从注册表中读取
            AppInfo appInfo = RaincJobScheduler.getAppInfo(groupInfo.getAppName());
            if (appInfo != null) {
                groupInfo.setExecutorList(new ArrayList<>(appInfo.getAddressMap().values()));
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

    /**
     * 触发进程
     *
     * @param groupInfo
     * @param jobInfo
     * @param finalFailRetryCount
     * @param triggerType
     * @param shardingParam
     */
    private static void processTrigger(GroupInfo groupInfo, JobInfoDO jobInfo, int finalFailRetryCount, TriggerTypeEnum triggerType, ShardingParam shardingParam) {
        //初始化参数
        //路由策略
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null);
        //阻塞策略
        ExecutorBlockStrategyEnum executorBlockStrategyEnum = ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), ExecutorBlockStrategyEnum.SERIAL_EXECUTION);
        //初始化日志信息
        JobLogDO jobLogDO = new JobLogDO();
        jobLogDO.setJobGroup(groupInfo.getId());
        jobLogDO.setJobId(jobInfo.getId());
        jobLogDO.setTriggerTime(new Date());
        jobLogDO.setHandleCode(0);
        jobLogDO.setAlarmStatus(0);
        RaincJobAdminConfig.getAdminConfig().getJobLogRepository().saveAndFlush(jobLogDO);
        log.debug(">>>>>>>>>>> rainc-job trigger start, jobId:{}", jobLogDO.getId());

        //初始化触发参数
        TriggerParam triggerParam = TriggerParam.builder()
                //任务参数
                .jobId(jobInfo.getId())
                .executorParams(jobInfo.getExecutorParam())
                .executorBlockStrategy(jobInfo.getExecutorBlockStrategy())
                .executorHandler(jobInfo.getExecutorHandler())
                .executorTimeout(jobInfo.getExecutorTimeOut())
                //日志参数
                .logId(jobLogDO.getId())
                .logDateTime(jobLogDO.getTriggerTime().getTime())
                .build();


        //初始化路由地址
        ExecutorInfo executorInfo = null;
        ReturnT<ExecutorInfo> routeAddressResult = null;
        if (groupInfo.getExecutorList() != null && groupInfo.getExecutorList().size() > 0) {
            routeAddressResult = executorRouteStrategyEnum.getRouter().route(triggerParam, groupInfo.getExecutorList());
            if (routeAddressResult.getCode() == ReturnT.SUCCESS_CODE) {
                executorInfo = routeAddressResult.getContent();
            }
        } else {
            routeAddressResult = new ReturnT<>(ReturnT.FAIL_CODE, "Trigger Fail：registry address is empty");
        }

        //触发远程执行器
        ReturnT<String> triggerResult = null;
        if (executorInfo != null) {
            triggerResult = runExecutor(triggerParam, executorInfo);
        } else {
            triggerResult = new ReturnT<String>(ReturnT.FAIL_CODE, null);
        }

        //收集触发信息
        StringBuffer triggerMsgSb = new StringBuffer();
        triggerMsgSb.append("任务触发类型").append("：").append(triggerType.getTitle());
        triggerMsgSb.append("<br>").append("调度机器").append("：").append(IpUtil.getIp());
        triggerMsgSb.append("<br>").append("执行器-注册方式").append("：").append(groupInfo.isAuto() ? "自动注册" : "手动注册");
        triggerMsgSb.append("<br>").append("执行器-地址列表").append("：").append(groupInfo.getExecutorList());
        triggerMsgSb.append("<br>").append("路由策略").append("：").append(executorRouteStrategyEnum.getTitle());
        if (shardingParam != null) {
            triggerMsgSb.append("(").append(shardingParam).append(")");
        }
        triggerMsgSb.append("<br>").append("阻塞策略").append("：").append(executorBlockStrategyEnum.getTitle());
        triggerMsgSb.append("<br>").append("任务超时时间").append("：").append(jobInfo.getExecutorTimeOut());
        triggerMsgSb.append("<br>").append("失败重试次数").append("：").append(finalFailRetryCount);

        triggerMsgSb.append("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" + "触发调度" + "<<<<<<<<<<< </span><br>")
                .append((routeAddressResult.getMsg() != null) ? routeAddressResult.getMsg() + "<br><br>" : "").append(triggerResult.getMsg() != null ? triggerResult.getMsg() : "");


        // 6、保存触发信息
        jobLogDO.setExecutorAddress(executorInfo != null ? executorInfo.getAddress() : null);
        jobLogDO.setExecutorHandler(jobInfo.getExecutorHandler());
        jobLogDO.setExecutorParam(jobInfo.getExecutorParam());
        jobLogDO.setExecutorShardingParam(shardingParam != null ? shardingParam.toString() : null);
        jobLogDO.setExecutorFailRetryCount(finalFailRetryCount);
        jobLogDO.setTriggerTime(new Date());
        jobLogDO.setTriggerCode(triggerResult.getCode());
        jobLogDO.setTriggerMsg(triggerMsgSb.toString());
        RaincJobAdminConfig.getAdminConfig().getJobLogRepository()
                .upDateTriggerJobLog(jobLogDO.getId(),
                        jobLogDO.getExecutorAddress(),
                        jobLogDO.getExecutorHandler(),
                        jobLogDO.getExecutorParam(),
                        jobLogDO.getExecutorShardingParam(),
                        jobLogDO.getExecutorFailRetryCount(),
                        jobLogDO.getTriggerTime(),
                        jobLogDO.getTriggerCode(),
                        jobLogDO.getTriggerMsg());
        log.debug(">>>>>>>>>>> rainc-job trigger end, jobId:{}", jobLogDO.getId());
    }

    /**
     * 运行执行器
     *
     * @param triggerParam
     * @param executorInfo
     * @return
     */
    public static ReturnT<String> runExecutor(TriggerParam triggerParam, ExecutorInfo executorInfo) {
        ReturnT<String> runResult = null;
        try {
            runResult = executorInfo.getExecutorBiz().run(triggerParam);
        } catch (Exception e) {
            log.error(">>>>>>>>>>> rainc-job trigger error, please check if the executor[{}] is running.", executorInfo, e);
            runResult = new ReturnT<String>(ReturnT.FAIL_CODE, ExceptionUtil.getMessage(e));
        }
        StringBuffer runResultSB = new StringBuffer("Trigger Job:");
        runResultSB.append("<br>address:").append(executorInfo.getAddress());
        runResultSB.append("<br>code:").append(runResult.getCode());
        runResultSB.append("<br>msg:").append(runResult.getMsg());

        runResult.setMsg(runResultSB.toString());
        return runResult;
    }
}
