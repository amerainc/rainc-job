package com.rainc.job.core.thread;

import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.cron.CronExpression;
import com.rainc.job.core.trigger.TriggerTypeEnum;
import com.rainc.job.model.JobInfoDO;
import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author rainc
 * @create 2020/11/1 12:44
 */
@Log4j2
public class JobSchedulerHelper {
    private static JobSchedulerHelper instance = new JobSchedulerHelper();
    public static final long PRE_READ_MS = 5000;//预读时常

    public static JobSchedulerHelper getInstance() {
        return instance;
    }

    private Thread scheduleThread;
    private Thread ringThread;
    private volatile boolean scheduleThreadToStop = false;
    private volatile boolean ringThreadToStop = false;
    private volatile static Map<Integer, List<Long>> ringData = new ConcurrentHashMap<>();

    public void start() {
        scheduleThread = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                if (!scheduleThreadToStop) {
                    log.error(e.getMessage(), e);
                }
            }

            log.info(">>>>>>>>> init rainc-job admin scheduler success.");
            while (!scheduleThreadToStop) {
                long nowTime = 0;
                boolean preSuc = false;
                try {
                    nowTime = System.currentTimeMillis();
                    preSuc = true;
                    List<JobInfoDO> infos = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository().findAllByTriggerNextTimeIsLessThan(nowTime + PRE_READ_MS);
                    if (infos != null && infos.size() > 0) {
                        for (JobInfoDO info : infos) {
                            if (nowTime - PRE_READ_MS > info.getTriggerNextTime()) {//如果是5秒前的任务不执行则直接刷新下一次的任务触发时间
                                refreshNextValidTime(info, new Date());
                            } else if (nowTime > info.getTriggerNextTime()) {//如果是5秒内的任务则立即出发，并刷新任务触发时间
                                JobTriggerPoolHelper.trigger(info.getId(), TriggerTypeEnum.CRON, -1, null, null, null);
                                refreshNextValidTime(info, new Date());
                                if (nowTime + PRE_READ_MS > info.getTriggerNextTime()) {//如果刷新后在未来5秒内，则放入时间环并刷新任务触发时间
                                    // 1、make ring second
                                    int ringSecond = (int) ((info.getTriggerNextTime() / 1000) % 60);

                                    // 2、push time ring
                                    pushTimeRing(ringSecond, info.getId());

                                    // 3、fresh next
                                    refreshNextValidTime(info, new Date(info.getTriggerNextTime()));

                                }
                            } else {
                                //直接放入时间环
                                // 1、make ring second
                                int ringSecond = (int) ((info.getTriggerNextTime() / 1000) % 60);

                                // 2、push time ring
                                pushTimeRing(ringSecond, info.getId());
                                // 3、fresh next
                                refreshNextValidTime(info, new Date(info.getTriggerNextTime()));
                            }
                        }

                        //更新任务信息
                        for (JobInfoDO info : infos) {
                            RaincJobAdminConfig.getAdminConfig().getJobInfoRepository().save(info);
                        }

                    } else {
                        preSuc = false;
                    }

                } catch (Exception e) {
                    if (!scheduleThreadToStop) {
                        log.error(">>>>>>>> rainc-job, JobScheduleHelper#scheduleThread error:", e);
                    }
                }


                long cost = System.currentTimeMillis() - nowTime;
                if (cost < 1000) {
                    try {
                        TimeUnit.MILLISECONDS.sleep((preSuc ? 1000 : PRE_READ_MS) - System.currentTimeMillis() % 1000);
                    } catch (InterruptedException e) {
                        if (!scheduleThreadToStop) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
            log.info(">>>>>>>>>> rainc-job JobSchedulerHelper#scheduleThread stop");
        });
        scheduleThread.setDaemon(true);
        scheduleThread.setName("rainc-job JobSchedulerHelper#scheduleThread");
        scheduleThread.start();

        ringThread = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                if (!ringThreadToStop) {
                    log.error(e.getMessage(), e);
                }
            }

            while (!ringThreadToStop) {

                try {
                    // second data
                    List<Long> ringItemData = new ArrayList<>();
                    int nowSecond = Calendar.getInstance().get(Calendar.SECOND);   // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                    for (int i = 0; i < 2; i++) {
                        List<Long> tmpData = ringData.remove((nowSecond + 60 - i) % 60);
                        if (tmpData != null) {
                            ringItemData.addAll(tmpData);
                        }
                    }

                    // ring trigger
                    log.debug(">>>>>>>>>>> xxl-job, time-ring beat : " + nowSecond + " = " + ringItemData);
                    if (ringItemData.size() > 0) {
                        // do trigger
                        for (long jobId : ringItemData) {
                            // do trigger
                            JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.CRON, -1, null, null, null);
                        }
                        // clear
                        ringItemData.clear();
                    }
                } catch (Exception e) {
                    if (!ringThreadToStop) {
                        log.error(">>>>>>>>>>> xxl-job, JobScheduleHelper#ringThread error:", e);
                    }
                }

                // next second, align second
                try {
                    TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
                } catch (InterruptedException e) {
                    if (!ringThreadToStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            log.info(">>>>>>>>>> rainc-job JobSchedulerHelper#ringThread stop");
        });
        ringThread.setDaemon(true);
        ringThread.setName("rainc-job, admin JobScheduleHelper#ringThread");
        ringThread.start();
    }

    public void stop() {
        scheduleThreadToStop = true;
        ringThreadToStop = true;
        // interrupt and wait
        scheduleThread.interrupt();
        ringThread.interrupt();
        try {
            scheduleThread.join();
            ringThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }


    private void refreshNextValidTime(JobInfoDO jobInfo, Date fromTime) throws ParseException {
        Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(fromTime);
        if (nextValidTime != null) {
            jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
            jobInfo.setTriggerNextTime(nextValidTime.getTime());
        } else {
            jobInfo.setTriggerStatus(false);
            jobInfo.setTriggerLastTime(0L);
            jobInfo.setTriggerNextTime(0L);
        }
    }

    private void pushTimeRing(int ringSecond, long jobId) {
        // push async ring
        List<Long> ringItemData = ringData.get(ringSecond);
        if (ringItemData == null) {
            ringItemData = new ArrayList<>();
            ringData.put(ringSecond, ringItemData);
        }
        ringItemData.add(jobId);

        log.debug(">>>>>>>>>>> rainc-job, schedule push time-ring : " + ringSecond + " = " + ringItemData);
    }
}
