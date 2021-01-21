package com.rainc.job.service.impl;

import cn.hutool.core.date.DateUtil;
import com.rainc.job.model.JobLogReportDO;
import com.rainc.job.respository.JobLogReportRepository;
import com.rainc.job.service.JobLogReportService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author rainc
 * @create 2021/1/3 12:00
 */
@Service
public class JobLogReportServiceImpl implements JobLogReportService {
    @Resource
    JobLogReportRepository jobLogReportRepository;

    @Override
    public Long count() {
        Map<String, Long> logReportTotal = jobLogReportRepository.findLogReportTotal();
        return logReportTotal.values().stream().reduce(Long::sum).orElse(0L);
    }

    @Override
    public List<JobLogReportDO> list(String startDate, String endDate) {
        System.out.println(DateUtil.formatDate(DateUtil.parse(startDate)));
        System.out.println(DateUtil.formatDate(DateUtil.parse(endDate)));
        return jobLogReportRepository.findAllByTriggerDayBetweenOrderByTriggerDayAsc(DateUtil.parse(startDate), DateUtil.parse(endDate));
    }
}
