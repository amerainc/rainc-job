package com.rainc.job.core.thread;

import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.core.cron.CronExpression;
import com.rainc.job.core.trigger.TriggerTypeEnum;
import com.rainc.job.model.JobInfoDO;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度线程
 *
 * @Author rainc
 * @create 2020/11/1 12:44
 */
@Log4j2
public class JobScheduleHelper {
    private static JobScheduleHelper instance = new JobScheduleHelper();
    //预读时常
    public static final long PRE_READ_MS = 5000;

    public static JobScheduleHelper getInstance() {
        return instance;
    }

    private Thread scheduleThread;
    private Thread ringThread;
    private volatile boolean scheduleThreadToStop = false;
    private volatile boolean ringThreadToStop = false;
    private static Map<Integer, List<Long>> ringData = new ConcurrentHashMap<>();

    public void start() {
        scheduleThreadStart();
        ringThreadStart();
    }

    private void scheduleThreadStart() {
        scheduleThread = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                if (!scheduleThreadToStop) {
                    log.error(e.getMessage(), e);
                }
            }
            // pre-read count: treadpool-size * trigger-qps (each trigger cost 50ms, qps = 1000/50 = 20)
            int preReadCount = (RaincJobAdminConfig.getAdminConfig().getTriggerPoolFastMax() + RaincJobAdminConfig.getAdminConfig().gettriggerPoolSlowMax()) * 20;
            log.info(JobLogPrefix.PREFIX+"预读数量={}", preReadCount);
            //jpa 分页
            PageRequest preReadPageReq = PageRequest.of(0, preReadCount);
            log.info(JobLogPrefix.PREFIX+"初始化调度线程");
            while (!scheduleThreadToStop) {
                long nowTime = 0;
                boolean preSuc = false;
                try {
                    nowTime = System.currentTimeMillis();
                    preSuc = true;
                    //预读未来5秒内的任务
                    List<JobInfoDO> jobInfoDOList = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository()
                            .findAllByTriggerNextTimeIsLessThanAndTriggerStatusTrue(nowTime + PRE_READ_MS, preReadPageReq);
                    if (jobInfoDOList.size() > 0) {
                        //TimeInterval timer = DateUtil.timer();
                        for (JobInfoDO jobInfoDO : jobInfoDOList) {
                            //1.保存旧时间
                            long oldTriggerNextTime = jobInfoDO.getTriggerNextTime();
                            if (nowTime > oldTriggerNextTime) {
                                //2.计算新时间
                                refreshNextValidTime(jobInfoDO, new Date());
                                //如果时间未变动则更新成功
                                int lockRet = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository()
                                        .upDateNextTriggerTime(jobInfoDO.getId(), oldTriggerNextTime, jobInfoDO.getTriggerNextTime());
                                if (lockRet < 1) {
                                    continue;
                                }
                                //如果是5秒内的任务,而且写入时间成功
                                if (oldTriggerNextTime > nowTime - PRE_READ_MS) {
                                    //立即触发任务
                                    JobTriggerPoolHelper.trigger(jobInfoDO.getId(), TriggerTypeEnum.CRON, -1, null, null, null);
                                    if (nowTime + PRE_READ_MS > jobInfoDO.getTriggerNextTime()) {
                                        //如果刷新后在未来5秒内，则放入时间轮并刷新再次任务触发时间
                                        //1.读取旧时间
                                        oldTriggerNextTime = jobInfoDO.getTriggerNextTime();
                                        //2.创建新时间
                                        refreshNextValidTime(jobInfoDO, new Date(oldTriggerNextTime));
                                        //3.尝试写入时间
                                        lockRet = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository()
                                                .upDateNextTriggerTime(jobInfoDO.getId(), oldTriggerNextTime, jobInfoDO.getTriggerNextTime());
                                        //写入成功则放入时间轮进行时间调度
                                        if (lockRet < 1) {
                                            continue;
                                        }
                                        pushRing(jobInfoDO, oldTriggerNextTime);

                                    }
                                }
                            } else {
                                //2.计算新时间
                                refreshNextValidTime(jobInfoDO, new Date(oldTriggerNextTime));
                                int lockRet = RaincJobAdminConfig.getAdminConfig().getJobInfoRepository().upDateNextTriggerTime(jobInfoDO.getId(), oldTriggerNextTime, jobInfoDO.getTriggerNextTime());
                                //3.直接放入时间轮并更新时间
                                if (lockRet < 1) {
                                    continue;
                                }
                                pushRing(jobInfoDO, oldTriggerNextTime);
                            }
                        }
                        //log.info(">>>>>>>> rainc-job cost time={}", timer.interval());
                    } else {
                        preSuc = false;
                    }
                } catch (Exception e) {
                    if (!scheduleThreadToStop) {
                        log.error(JobLogPrefix.PREFIX+"JobScheduleHelper#scheduleThread error:", e);
                    }
                }

                //计算耗费时间
                long cost = System.currentTimeMillis() - nowTime;
                //如果小于1秒
                if (cost < 1000) {
                    try {
                        //预读成功则下一秒继续预读，预读失败则表示接下来5秒没有事件需要调度，睡眠5秒，%1000保证按秒执行
                        TimeUnit.MILLISECONDS.sleep((preSuc ? 1000 : PRE_READ_MS) - System.currentTimeMillis() % 1000);
                    } catch (InterruptedException e) {
                        if (!scheduleThreadToStop) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
            log.info(JobLogPrefix.PREFIX+"JobSchedulerHelper#scheduleThread stop");
        });
        scheduleThread.setDaemon(true);
        scheduleThread.setName("rainc-job JobSchedulerHelper#scheduleThread");
        scheduleThread.start();
    }

    private void ringThreadStart() {
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
                    //取得当前秒时间的数据
                    List<Long> ringItemData = new ArrayList<>();
                    int nowSecond = Calendar.getInstance().get(Calendar.SECOND);
                    // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                    for (int i = 0; i < 2; i++) {
                        List<Long> tmpData = ringData.remove((nowSecond + 60 - i) % 60);
                        if (tmpData != null) {
                            ringItemData.addAll(tmpData);
                        }
                    }

                    // 触发时间轮
                    log.debug(JobLogPrefix.PREFIX+"时间轮信息 : " + nowSecond + " = " + ringItemData);
                    if (ringItemData.size() > 0) {
                        //遍历当前时间的数据列表进行触发
                        for (long jobId : ringItemData) {
                            //进行触发
                            JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.CRON, -1, null, null, null);
                        }
                        // 清空数据表
                        ringItemData.clear();
                    }
                } catch (Exception e) {
                    if (!ringThreadToStop) {
                        log.error(JobLogPrefix.PREFIX+"JobScheduleHelper#ringThread error:", e);
                    }
                }

                //睡眠到下一整秒再次执行
                try {
                    TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
                } catch (InterruptedException e) {
                    if (!ringThreadToStop) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            log.info(JobLogPrefix.PREFIX+"JobSchedulerHelper#ringThread stop");
        });
        ringThread.setDaemon(true);
        ringThread.setName("rainc-job, admin JobScheduleHelper#ringThread");
        ringThread.start();
    }

    private void pushRing(JobInfoDO info, long triggerTime) throws ParseException {
        // 1、计算放入的时间轮区域
        int ringSecond = (int) ((triggerTime / 1000) % 60);

        // 2、放入时间轮
        pushTimeRing(ringSecond, info.getId());


    }

    public void toStop() {
        scheduleThreadToStop = true;
        ringThreadToStop = true;
        //中断两个线程
        scheduleThread.interrupt();
        ringThread.interrupt();
        try {
            //阻塞直到线程结束
            scheduleThread.join();
            ringThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 计算下一次的触发时间
     *
     * @param jobInfo  计算的任务
     * @param fromTime 计算的时间
     * @throws ParseException
     */
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

    /**
     * 将任务放进时间轮
     *
     * @param ringSecond 时间轮分钟位
     * @param jobId      任务id
     */
    private void pushTimeRing(int ringSecond, long jobId) {
        List<Long> ringItemData = ringData.computeIfAbsent(ringSecond, ArrayList::new);
        ringItemData.add(jobId);

        log.debug(JobLogPrefix.PREFIX+"任务放入时间轮 : " + ringSecond + " = " + ringItemData);
    }
}
