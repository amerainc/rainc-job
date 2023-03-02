package com.rainc.job.core.thread;

import cn.hutool.core.date.DateUtil;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.model.JobLogDO;
import com.rainc.job.model.JobLogReportDO;
import com.rainc.job.respository.specification.JobLogSpecification;
import lombok.extern.log4j.Log4j2;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author rainc
 * @create 2021/1/2 11:29
 * 日志报表
 */
@Log4j2
public class JobLogReportHelper {
    private static JobLogReportHelper instance = new JobLogReportHelper();

    public static JobLogReportHelper getInstance() {
        return instance;
    }

    private Thread logrThread;
    private volatile boolean toStop = false;

    public void start() {
        logrThread = new Thread(() -> {

            //最后一次清理日志时间
            long lastCleanLogTime = 0;


            while (!toStop) {

                // 1、更新3天内的日志信息
                try {

                    for (int i = 0; i < 3; i++) {

                        Date today = new Date();
                        // today
                        Date todayFrom = DateUtil.beginOfDay(DateUtil.offsetDay(today,-i));
                        Date todayTo = DateUtil.endOfDay(todayFrom);

                        // 更新日志时间
                        JobLogReportDO jobLogReportDO = RaincJobAdminConfig.getAdminConfig().getJobLogReportRepository().findByTriggerDay(todayFrom)
                                .orElseGet(JobLogReportDO::new);
                        jobLogReportDO.setTriggerDay(todayFrom);
                        jobLogReportDO.setRunningCount(0L);
                        jobLogReportDO.setSucCount(0L);
                        jobLogReportDO.setFailCount(0L);

                        Map<String, Long> triggerCountMap = RaincJobAdminConfig.getAdminConfig().getJobLogRepository().findLogReport(todayFrom, todayTo);
                        if (triggerCountMap != null && triggerCountMap.size() > 0) {
                            long triggerDayCount = triggerCountMap.get("triggerDayCount") != null ? triggerCountMap.get("triggerDayCount") : 0L;
                            long triggerDayCountRunning = triggerCountMap.get("triggerDayCountRunning") != null ? triggerCountMap.get("triggerDayCountRunning") : 0L;
                            long triggerDayCountSuc = triggerCountMap.get("triggerDayCountSuc") != null ? triggerCountMap.get("triggerDayCountSuc") : 0L;
                            long triggerDayCountFail = triggerDayCount - triggerDayCountRunning - triggerDayCountSuc;

                            jobLogReportDO.setRunningCount(triggerDayCountRunning);
                            jobLogReportDO.setSucCount(triggerDayCountSuc);
                            jobLogReportDO.setFailCount(triggerDayCountFail);
                        }

                        // 进行更新
                        RaincJobAdminConfig.getAdminConfig().getJobLogReportRepository().save(jobLogReportDO);
                    }

                } catch (Exception e) {
                    if (!toStop) {
                        log.error(JobLogPrefix.PREFIX+"任务日志报表线程出错:", e);
                    }
                }

                // 2、日志清理，每隔一天一次
                if (RaincJobAdminConfig.getAdminConfig().getAdminProperties().getLogretentiondays() > 0
                        && System.currentTimeMillis() - lastCleanLogTime > 24 * 60 * 60 * 1000) {

                    // 失效时间
                    Date clearBeforeTime = DateUtil.offsetDay(new Date(), -1 * RaincJobAdminConfig.getAdminConfig().getAdminProperties().getLogretentiondays());

                    // 清除失效日志
                    List<JobLogDO> jobLogDOList;
                    do {
                        jobLogDOList = RaincJobAdminConfig.getAdminConfig().getJobLogRepository().findAll(JobLogSpecification.findClearLogIdsSpec(0, 0, clearBeforeTime, 0));
                        if (jobLogDOList.size() > 0) {
                            RaincJobAdminConfig.getAdminConfig().getJobLogRepository().deleteAll(jobLogDOList);
                        }
                    } while (jobLogDOList.size() > 0);

                    // 更新清理时间
                    lastCleanLogTime = System.currentTimeMillis();
                }

                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }
                }

            }

            log.info(JobLogPrefix.PREFIX+"任务日志报表线程停止");

        }
        );
        logrThread.setDaemon(true);
        logrThread.setName("rainc-job, admin JobLogReportHelper");
        logrThread.start();
    }

    public void toStop() {
        toStop = true;
        //中断并等待
        logrThread.interrupt();
        try {
            logrThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

}
