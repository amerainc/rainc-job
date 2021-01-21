package com.rainc.job.controller;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.model.JobLogReportDO;
import com.rainc.job.service.JobLogReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author rainc
 * @create 2021/1/3 11:39
 */
@RestController
@RequestMapping("/joblog/report/")
public class JobLogReportController {
    @Resource
    JobLogReportService jobLogReportService;

    @GetMapping("/count")
    public ReturnT<Long> count() {
        return new ReturnT<Long>(jobLogReportService.count());
    }

    @GetMapping("/list")
    public ReturnT<List<JobLogReportDO>> list(String startDate, String endDate) {
        return new ReturnT<>(jobLogReportService.list(startDate, endDate));
    }
}
