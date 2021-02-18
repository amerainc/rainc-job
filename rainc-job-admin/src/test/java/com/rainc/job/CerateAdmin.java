package com.rainc.job;

import com.rainc.job.model.JobUserDO;
import com.rainc.job.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @Author rainc
 * @create 2021/2/17 19:23
 */
@SpringBootTest
public class CerateAdmin {
    @Resource
    UserService userService;

    @Test
    public void add() {
        JobUserDO jobUserDO = new JobUserDO();
        jobUserDO.setUsername("rainc");
        jobUserDO.setPassword("123456");
        jobUserDO.setRole(1);
        userService.add(jobUserDO);
    }
}
