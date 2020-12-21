package com.rainc.job.repository;

import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.respository.JobInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author rainc
 * @create 2020/12/13 13:36
 */
@SpringBootTest
public class JobInfoRepositoryTest {

    @Test
    public void findAllByTriggerNextTimeIsLessThan() {
        RaincJobAdminConfig admainConfig = RaincJobAdminConfig.getAdminConfig();
        System.out.println(admainConfig);
        JobInfoRepository jobInfoRepository = admainConfig.getJobInfoRepository();
        //System.out.println(jobInfoRepository.findAllByTriggerNextTimeIsLessThan(50));
    }

    @Test
    public void update() {
        RaincJobAdminConfig admainConfig = RaincJobAdminConfig.getAdminConfig();
        System.out.println(admainConfig);
        JobInfoRepository jobInfoRepository = admainConfig.getJobInfoRepository();
        int i = jobInfoRepository.upDateNextTriggerTime(1, 1L, 1608551410000L);
        System.out.println(i);
    }
}
