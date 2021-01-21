package com.rainc.job.service;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.model.JobLogDO;
import org.springframework.data.domain.Page;

/**
 * @Author rainc
 * @create 2020/12/30 8:56
 */
public interface JobLogService {
    Page<JobLogDO> pageList(long jobGroup, long jobId, int logStatus, String filterTime, int page, int size);

    ReturnT<String> logKill(long id);
}
