package com.rainc.job.service;

import com.rainc.job.model.JobLogReportDO;

import java.util.List;

/**
 * @Author rainc
 * @create 2021/1/3 11:59
 */
public interface JobLogReportService {
    Long count();

    List<JobLogReportDO> list(String startDate, String endDate);
}
