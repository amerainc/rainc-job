package com.rainc.job.core.alarm;

import com.rainc.job.model.JobInfoDO;
import com.rainc.job.model.JobLogDO;

/**
 * 任务告警
 *
 * @Author rainc
 * @create 2020/12/23 10:11
 */
public interface JobAlarm {
    /**
     * 任务告警
     *
     * @param info 任务信息
     * @param jobLog 任务日志
     * @return
     */
    boolean doAlarm(JobInfoDO info, JobLogDO jobLog);
}
