package com.rainc.job.repository;

import com.rainc.job.core.config.RaincJobAdminConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

/**
 * @Author rainc
 * @create 2021/1/3 12:09
 */
@SpringBootTest
public class JobLogReportRepositoryTest {
    @Test
    public void JobLogReportDO() {
        Map<String, Long> logReportTotal = RaincJobAdminConfig.getAdminConfig().getJobLogReportRepository().findLogReportTotal();
        System.out.println(logReportTotal.get("runningCount"));
        System.out.println(logReportTotal.get("sucCount"));
        System.out.println(logReportTotal.get("failCount"));
    }
}
