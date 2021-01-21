package com.rainc.job.service;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.model.JobUserDO;
import org.springframework.data.domain.Page;

/**
 * @Author rainc
 * @create 2020/12/23 22:10
 */
public interface UserService {
    /**
     * 注册新用户
     *
     * @param jobUserDO
     * @return
     */
    ReturnT<String> add(JobUserDO jobUserDO);

    /**
     * 登录
     *
     * @param jobUserDO
     * @return
     */
    ReturnT<String> login(JobUserDO jobUserDO);

    /**
     * 判断token是否能够登陆，成功返回具体jobUser,否则返回
     * @param token
     * @return
     */
    JobUserDO ifLogin(String token);

    Page<JobUserDO> list(int page, int size, int rule, String username);

    ReturnT<String> update(JobUserDO jobUserDO);

    ReturnT<String> delete(long id);
}
