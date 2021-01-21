package com.rainc.job.service.impl;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.model.GroupInfo;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.model.JobGroupDO;
import com.rainc.job.respository.JobGroupRepository;
import com.rainc.job.service.JobGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author rainc
 * @create 2020/12/26 18:23
 */
@Service
public class JobGroupServiceImpl implements JobGroupService {
    @Resource
    JobGroupRepository jobGroupRepository;

    @Override
    public Page<JobGroupDO> pageList(String appName, String title, int page, int size) {
        return jobGroupRepository.findAllByAppNameLikeAndTitleLike(appName, title, PageRequest.of(page, size));
    }

    @Override
    public List<JobGroupDO> all() {
        return jobGroupRepository.findAll();
    }

    @Override
    public ReturnT<String> save(JobGroupDO jobGroupDO) {
        jobGroupRepository.saveAndFlush(jobGroupDO);
        if (jobGroupDO.getId() > 0) {
            return ReturnT.SUCCESS;
        } else {
            return ReturnT.FAIL;
        }

    }

    @Override
    public ReturnT<String> delete(long id) {
        jobGroupRepository.deleteById(id);
        return ReturnT.SUCCESS;
    }

    @Override
    public int executorCount() {
        Set<ExecutorInfo> executorSet = new HashSet<>();
        jobGroupRepository.findAll()
                .stream().map(GroupInfo::castToInfo)
                .forEach((groupInfo -> {
                    if (groupInfo.isAuto()) {
                        AppInfo appInfo = RaincJobScheduler.getAppInfo(groupInfo.getAppName());
                        if (appInfo!=null) {
                            executorSet.addAll(appInfo.getAddressMap().values());
                        }
                    } else {
                        executorSet.addAll(groupInfo.getExecutorList());
                    }
                }));

        return executorSet.size();
    }
}
