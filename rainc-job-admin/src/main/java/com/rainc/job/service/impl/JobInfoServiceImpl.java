package com.rainc.job.service.impl;

import com.rainc.job.core.cron.CronExpression;
import com.rainc.job.core.enums.ExecutorBlockStrategyEnum;
import com.rainc.job.core.router.ExecutorRouteStrategyEnum;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.core.thread.JobScheduleHelper;
import com.rainc.job.exception.RaincJobException;
import com.rainc.job.model.JobGroupDO;
import com.rainc.job.model.JobInfoDO;
import com.rainc.job.respository.JobGroupRepository;
import com.rainc.job.respository.JobInfoRepository;
import com.rainc.job.respository.JobLogRepository;
import com.rainc.job.service.JobInfoService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/26 10:09
 */
@Service
@Log4j2
public class JobInfoServiceImpl implements JobInfoService {
    @Resource
    JobInfoRepository jobInfoRepository;
    @Resource
    JobGroupRepository jobGroupRepository;
    @Resource
    JobLogRepository jobLogRepository;

    private final ExampleMatcher pageListExampleMatcher;

    public JobInfoServiceImpl() {
        //初始化查询参数
        this.pageListExampleMatcher = ExampleMatcher
                .matching()
                .withMatcher("jobGroup", ExampleMatcher.GenericPropertyMatcher::exact)
                .withMatcher("triggerStatus", ExampleMatcher.GenericPropertyMatcher::exact)
                .withMatcher("jobDesc", ExampleMatcher.GenericPropertyMatcher::contains)
                .withMatcher("executorHandler", ExampleMatcher.GenericPropertyMatcher::contains)
                .withMatcher("author", ExampleMatcher.GenericPropertyMatcher::contains);
    }

    @Override
    public Page<JobInfoDO> pageList(int page,
                                    int size,
                                    long jobGroup,
                                    int triggerStatus,
                                    String jobDesc,
                                    String executorHandler,
                                    String author) {
        //生成查询
        JobInfoDO jobInfoDO = JobInfoDO.builder()
                .jobGroup(jobGroup)
                .triggerStatus(triggerStatus == -1 ? null : triggerStatus == 1)
                .jobDesc(jobDesc)
                .executorHandler(executorHandler)
                .author(author)
                .build();
        Example<JobInfoDO> example = Example.of(jobInfoDO, pageListExampleMatcher);
        return jobInfoRepository.findAll(example, PageRequest.of(page, size));
    }

    @Override
    public String save(JobInfoDO jobInfoDO) {
        //验证分组
        if (!jobGroupRepository.findById(jobInfoDO.getJobGroup()).isPresent()) {
            throw new RaincJobException("分组不存在");
        }
        //验证cron表达式
        if (!CronExpression.isValidExpression(jobInfoDO.getJobCron())) {
            throw new RaincJobException("cron格式非法");
        }
        //验证阻塞策略
        if (ExecutorBlockStrategyEnum.match(jobInfoDO.getExecutorBlockStrategy(), null) == null) {
            throw new RaincJobException("阻塞策略非法");
        }
        //验证路由策略
        if (ExecutorRouteStrategyEnum.match(jobInfoDO.getExecutorRouteStrategy(), null) == null) {
            throw new RaincJobException("路由策略非法");
        }

        //存入数据库
        jobInfoDO.setAddTime(jobInfoDO.getAddTime() != null ? jobInfoDO.getAddTime() : new Date());
        jobInfoDO.setUpDateTime(new Date());
        jobInfoDO.setTriggerStatus(jobInfoDO.getTriggerStatus()!=null?jobInfoDO.getTriggerStatus():false);
        jobInfoDO.setExecutorTimeOut(jobInfoDO.getExecutorTimeOut() != null ? jobInfoDO.getExecutorTimeOut() : 0);
        jobInfoDO.setTriggerLastTime(jobInfoDO.getTriggerLastTime() != null ? jobInfoDO.getTriggerNextTime() : 0L);
        jobInfoDO.setTriggerNextTime(jobInfoDO.getTriggerNextTime() != null ? jobInfoDO.getTriggerNextTime() : 0L);
        jobInfoDO.setExecutorFailRetryCount(jobInfoDO.getExecutorFailRetryCount() != null ? jobInfoDO.getExecutorFailRetryCount() : 0);
        jobInfoRepository.saveAndFlush(jobInfoDO);
        if (jobInfoDO.getId() < 1) {
            throw new RaincJobException("新增任务失败");
        }
        return jobInfoDO.getId().toString();
    }

    @Override
    public JobInfoDO start(long id) {
        JobInfoDO jobInfoDO = jobInfoRepository.findById(id).orElseThrow(() -> new RaincJobException("任务不存在"));
        // next trigger time (5s后生效，避开预读周期)
        long nextTriggerTime = 0;
        try {
            Date nextValidTime = new CronExpression(jobInfoDO.getJobCron())
                    .getNextValidTimeAfter(new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
            if (nextValidTime == null) {
                throw new RaincJobException("Cron非法，永远不会触发");
            }
            nextTriggerTime = nextValidTime.getTime();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            throw new RaincJobException("Cron格式非法", e);
        }
        //保存
        jobInfoDO.setTriggerStatus(true);
        jobInfoDO.setTriggerLastTime(0L);
        jobInfoDO.setTriggerNextTime(nextTriggerTime);
        jobInfoDO.setUpDateTime(new Date());
        return jobInfoRepository.save(jobInfoDO);
    }

    @Override
    public JobInfoDO stop(long id) {
        JobInfoDO jobInfoDO = jobInfoRepository.findById(id).orElseThrow(() -> new RaincJobException("任务不存在"));
        jobInfoDO.setTriggerStatus(false);
        jobInfoDO.setTriggerLastTime(0L);
        jobInfoDO.setTriggerNextTime(0L);
        jobInfoDO.setUpDateTime(new Date());
        return jobInfoRepository.save(jobInfoDO);
    }

    @Override
    public List<String> handlers(long groupId) {
        JobGroupDO jobGroupDO = jobGroupRepository.findById(groupId).orElseThrow(() -> new RaincJobException("执行器信息错误"));
        if (RaincJobScheduler.getAppInfo(jobGroupDO.getAppName()) == null) {
            return null;
        }
        return RaincJobScheduler.getAppInfo(jobGroupDO.getAppName()).getHandlerList();
    }

    @Override
    @Modifying
    @Transactional(rollbackOn = Exception.class)
    public boolean delete(long id) {
        if (!jobInfoRepository.findById(id).isPresent()) {
            return true;
        }
        jobLogRepository.deleteAllByJobId(id);
        jobInfoRepository.deleteById(id);
        return true;
    }

    @Override
    public List<JobInfoDO> all(long jobGroup) {
        if (jobGroup == -1) {
            return jobInfoRepository.findAll();
        }

        return jobInfoRepository.findAllByJobGroup(jobGroup);
    }

    @Override
    public long count() {
        return jobInfoRepository.count();
    }


}
