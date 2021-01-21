package com.rainc.job.service;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.model.JobGroupDO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/26 18:23
 */
public interface JobGroupService {
    Page<JobGroupDO> pageList(String appName,String title,int page, int size);

    List<JobGroupDO> all();

    ReturnT<String> save(JobGroupDO jobGroupDO);

    ReturnT<String> delete(long id);

    int executorCount();
}
