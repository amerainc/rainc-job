package com.rainc.job.core.alarm;

import com.rainc.job.model.JobInfoDO;
import com.rainc.job.model.JobLogDO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 任务告警者
 *
 * @Author rainc
 * @create 2020/12/23 10:39
 */
@Component
@Log4j2
public class JobAlarmer implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;
    private List<JobAlarm> jobAlarmList;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, JobAlarm> serviceBeanMap = applicationContext.getBeansOfType(JobAlarm.class);
        if (serviceBeanMap.size() > 0) {
            jobAlarmList = new ArrayList<>(serviceBeanMap.values());
        }
    }

    /**
     * 任务告警
     *
     * @param info
     * @param jobLog
     * @return
     */
    public boolean alarm(JobInfoDO info, JobLogDO jobLog) {

        boolean result = false;
        if (jobAlarmList != null && jobAlarmList.size() > 0) {
            // 所有告警成功才会返回成功
            result = true;
            for (JobAlarm alarm : jobAlarmList) {
                boolean resultItem = false;
                try {
                    resultItem = alarm.doAlarm(info, jobLog);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                if (!resultItem) {
                    result = false;
                }
            }
        }

        return result;
    }
}
