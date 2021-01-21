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
    public void login() {
        JobUserDO login = userService.ifLogin("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJhaW5jIiwicGFzc3dvcmQiOiJlMTBhZGMzOTQ5YmE1OWFiYmU1NmUwNTdmMjBmODgzZSIsImlhdCI6MTYwODc5MzYzMCwiZXhwIjoxNjA5Mzk4NDMwfQ.3uQRuNmoBwnzeihwXioRKxLPiKPEYVsS1TPcaBi67Ik");
        System.out.println(login);
    }

}
