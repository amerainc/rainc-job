package com.rainc.job.service;

import com.rainc.job.model.JobUserDO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @Author rainc
 * @create 2020/12/24 14:57
 */
@SpringBootTest
public class UserServiceTest {
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
