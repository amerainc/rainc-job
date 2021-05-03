package com.rainc.job.core.config;

import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.model.JobUserDO;
import com.rainc.job.respository.JobUserRepository;
import com.rainc.job.service.UserService;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author rainc
 * @date 2021/4/26 17:09
 * @description 数据库数据初始化
 */
@Component
@Log4j2
public class DataInit implements ApplicationRunner {
    @Resource
    UserService userService;
    @Resource
    JobUserRepository jobUserRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long count = jobUserRepository.count();
        if (count > 0) {
            return;
        }
        try {
            JobUserDO jobUserDO = new JobUserDO();
            jobUserDO.setUsername("admin");
            jobUserDO.setPassword("123456");
            jobUserDO.setRole(1);
            userService.add(jobUserDO);
        } catch (Exception e) {
            log.warn(JobLogPrefix.PREFIX+"数据初始化失败",e);
        }
    }
}
