package com.rainc.job.controller;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.model.JobLogDO;
import com.rainc.job.service.JobLogService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author rainc
 * @create 2020/12/30 8:43
 */
@RestController
@RequestMapping("/joblog/")
public class JobLogController {
    @Resource
    JobLogService jobLogService;

    @GetMapping("/")
    public ReturnT<Page<JobLogDO>> list(@RequestParam(required = false, defaultValue = "1") int page,
                                        @RequestParam(required = false, defaultValue = "10") int size,
                                        @RequestParam(required = false, defaultValue = "-1") long jobGroup,
                                        @RequestParam(required = false, defaultValue = "-1") long jobId,
                                        @RequestParam(required = false, defaultValue = "-1") int logStatus,
                                        @RequestParam(required = false, defaultValue = "") String filterTime) {
        //jpa分页默认从0开始
        if (page > 0) {
            page--;
        }
        return new ReturnT<>(jobLogService.pageList(jobGroup, jobId, logStatus, filterTime, page, size));

    }


    @GetMapping("/logkill")
    public ReturnT<String> logKill(long id) {
       return jobLogService.logKill(id);
    }
}
