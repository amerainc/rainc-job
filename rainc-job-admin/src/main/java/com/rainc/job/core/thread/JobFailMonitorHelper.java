package com.rainc.job.core.thread;

import com.rainc.job.core.biz.model.ShardingParam;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.trigger.TriggerTypeEnum;
import com.rainc.job.model.JobInfoDO;
import com.rainc.job.model.JobLogDO;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 失败任务监听线程
 *
 * @Author rainc
 * @create 2020/12/20 21:27
 */
@Log4j2
public class JobFailMonitorHelper {
    private static JobFailMonitorHelper instance = new JobFailMonitorHelper();

    public static JobFailMonitorHelper getInstance() {
        return instance;
    }

    private Thread monitorThread;
    private volatile boolean toStop = false;


    public void start() {

        monitorThread = new Thread(() -> {
            //jpa 分页
            PageRequest pageSize = PageRequest.of(0, 1000);
            while (!toStop) {
                try {
                    //查找失败任务
                    List<Long> failJobLogIds = RaincJobAdminConfig.getAdminConfig().getJobLogRepository().findFailJobLogIds(pageSize);
                    if (failJobLogIds != null && !failJobLogIds.isEmpty()) {
                        for (Long failJobLogId : failJobLogIds) {
                            //锁日志
                            int lockRet = RaincJobAdminConfig.getAdminConfig().getJobLogRepository().updateAlarmStatus(failJobLogId, 0, -1);
                            if (lockRet < 1) {
                                continue;
                            }

                            Optional<JobLogDO> optionalJobLogDO = RaincJobAdminConfig.getAdminConfig().getJobLogRepository().findById(failJobLogId);
                            if (!optionalJobLogDO.isPresent()) {
                                continue;
                            }
                            JobLogDO jobLogDO = optionalJobLogDO.get();
                            Optional<JobInfoDO> optionalJobInfoDO = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository().findById(jobLogDO.getJobId());

                            // 1、失败重试监控
                            if (jobLogDO.getExecutorFailRetryCount() > 0) {
                                JobTriggerPoolHelper.trigger(jobLogDO.getJobId(), TriggerTypeEnum.RETRY, (jobLogDO.getExecutorFailRetryCount() - 1), ShardingParam.parseString(jobLogDO.getExecutorShardingParam()), jobLogDO.getExecutorParam(), null);
                                String retryMsg = "<br><br><span style=\"color:#F39C12;\" > >>>>>>>>>>>" + "失败重试触发" + "<<<<<<<<<<< </span><br>";
                                jobLogDO.setTriggerMsg(jobLogDO.getTriggerMsg() + retryMsg);
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
                            }

                            //2.失败邮件报警监控
                            int newAlarmStatus = 0;        // 告警状态：0-默认、-1=锁定状态、1-无需告警、2-告警成功、3-告警失败
                            if (optionalJobInfoDO.isPresent() && optionalJobInfoDO.get().getAlarmEmail() != null && optionalJobInfoDO.get().getAlarmEmail().trim().length() > 0) {
                                //如果有info，且邮箱不为空，则触发告警
                                boolean alarmResult = RaincJobAdminConfig.getAdminConfig().getJobAlarmer().alarm(optionalJobInfoDO.get(), jobLogDO);
                                //成功为2,失败为3
                                newAlarmStatus = alarmResult ? 2 : 3;
                            } else {
                                //否则无需告警
                                newAlarmStatus = 1;
                            }
                            //保存告警状态
                            RaincJobAdminConfig.getAdminConfig().getJobLogRepository().updateAlarmStatus(failJobLogId, -1, newAlarmStatus);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(">>>>>>>>>>> rainc-job, job fail monitor thread error:", e);
                    }
                }

                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("rainc-job, admin JobFailMonitorHelper");
        monitorThread.start();
    }

    public void toStop() {
        toStop = true;
        //中断并等待
        monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
