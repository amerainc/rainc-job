package com.rainc.job.repository;

import cn.hutool.core.date.DateUtil;
import com.rainc.job.core.config.RaincJobAdminConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author rainc
 * @create 2020/12/22 20:49
 */
@SpringBootTest
public class JobLogRepositoryTest {
    @Test
    public void findFailJobLogIds() {
        PageRequest pageSize = PageRequest.of(0, 2);
        List<Long> failJobLogIds = RaincJobAdminConfig.getAdminConfig().getJobLogRepository().findFailJobLogIds(pageSize);
        System.out.println(failJobLogIds);
    }

    @Test
    public void findLogReport() {
        Date todayFrom = DateUtil.beginOfDay(new Date());
        Date todayTo = DateUtil.endOfDay(todayFrom);

        Map<String, Long> logReport = RaincJobAdminConfig.getAdminConfig().getJobLogRepository().findLogReport(todayFrom, todayTo);
        System.out.println(logReport.get("triggerDayCount"));
        System.out.println(logReport.get("triggerDayCountRunning"));
        System.out.println(logReport.get("triggerDayCountSuc"));
    }
}
