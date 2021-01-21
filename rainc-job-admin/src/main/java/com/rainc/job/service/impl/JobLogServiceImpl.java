package com.rainc.job.service.impl;

import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.exception.RaincJobException;
import com.rainc.job.model.JobGroupDO;
import com.rainc.job.model.JobInfoDO;
import com.rainc.job.model.JobLogDO;
import com.rainc.job.respository.JobGroupRepository;
import com.rainc.job.respository.JobInfoRepository;
import com.rainc.job.respository.JobLogRepository;
import com.rainc.job.respository.specification.JobLogSpecification;
import com.rainc.job.service.JobLogService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/30 8:56
 */
@Service
@Log4j2
public class JobLogServiceImpl implements JobLogService {
    @Resource
    JobLogRepository jobLogRepository;
    @Resource
    JobInfoRepository jobInfoRepository;
    @Resource
    JobGroupRepository jobGroupRepository;

    /**
     * 分页查询列表
     *
     * @param jobGroup   -1查询所有
     * @param jobId      -1查询所有
     * @param logStatus  -1查询所有 0 成功 1失败 2进行中
     * @param filterTime 时间范围
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<JobLogDO> pageList(long jobGroup, long jobId, int logStatus, String filterTime, int page, int size) {
        return jobLogRepository.findAll(JobLogSpecification.pageListSpec(jobGroup, jobId, logStatus, filterTime), PageRequest.of(page, size));
    }

    @Override
    public ReturnT<String> logKill(long id) {
        JobLogDO jobLogDO = jobLogRepository.findById(id).orElseThrow(() -> new RaincJobException("日志ID非法"));
        JobInfoDO jobInfoDO = jobInfoRepository.findById(jobLogDO.getJobId()).orElseThrow(() -> new RaincJobException("任务ID非法"));
        JobGroupDO jobGroupDO = jobGroupRepository.findById(jobInfoDO.getJobGroup()).orElseThrow(() -> new RaincJobException("执行器ID非法"));
        if (ReturnT.SUCCESS_CODE != jobLogDO.getTriggerCode()) {
            throw new RaincJobException("调度失败，无法终止日志");
        }

        ReturnT<String> runResult;
        try {
            ExecutorBiz executorBiz = RaincJobScheduler.getExecutor(jobGroupDO.getAppName(), jobLogDO.getExecutorAddress()).getExecutorBiz();
            runResult = executorBiz.kill(jobInfoDO.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            runResult = new ReturnT<>(500, e.getMessage());
        }

        if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
            jobLogDO.setHandleCode(ReturnT.FAIL_CODE);
            jobLogDO.setHandleMsg("人为操作，主动终止" + ":" + (runResult.getMsg() != null ? runResult.getMsg() : ""));
            jobLogDO.setHandleTime(new Date());
            jobLogRepository.save(jobLogDO);
            return new ReturnT<>(runResult.getMsg());
        } else {
            return new ReturnT<>(500, runResult.getMsg());
        }
    }
}
