package com.rainc.job.service;

import com.rainc.job.model.JobInfoDO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/26 10:09
 */
public interface JobInfoService {

    Page<JobInfoDO> pageList(int page, int size,long jobGroup,int triggerStatus,String jobDesc,String executorHandler,String author);

    String save(JobInfoDO jobInfoDO);

    JobInfoDO start(long id);

    JobInfoDO stop(long id);

    List<String> handlers(long groupId);

    boolean delete(long id);

    List<JobInfoDO> all(long jobGroup);

    long count();
}
