package com.rainc.job.service.impl;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.exception.RaincJobException;
import com.rainc.job.model.JobUserDO;
import com.rainc.job.respository.JobUserRepository;
import com.rainc.job.service.UserService;
import com.rainc.job.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @Author rainc
 * @create 2020/12/23 22:13
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private JobUserRepository jobUserRepository;
    @Resource
    private JwtUtils jwtUtils;

    @Override
    public ReturnT<String> add(JobUserDO jobUserDO) {
        if (StrUtil.isBlank(jobUserDO.getPassword())) {
            throw new RaincJobException("密码不能为空");
        }
        if (jobUserDO.getPassword().length() < 4 || jobUserDO.getPassword().length() > 20) {
            throw new RaincJobException("密码长度应在[4-20]");
        }
        // md5 加密
        jobUserDO.setPassword(DigestUtils.md5DigestAsHex(jobUserDO.getPassword().getBytes()));
        //判断用户是否存在
        Optional<JobUserDO> existUser = jobUserRepository.findByUsername(jobUserDO.getUsername());
        if (existUser.isPresent()) {
            throw new RaincJobException("账号已存在");
        }
        jobUserRepository.save(jobUserDO);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> login(JobUserDO jobUserDO) {
        JobUserDO existJobUserDO = jobUserRepository.findByUsername(jobUserDO.getUsername().trim())
                .orElseThrow(() -> new RaincJobException("账号或密码错误"));
        String passwordMd5 = DigestUtils.md5DigestAsHex(jobUserDO.getPassword().trim().getBytes());
        if (!passwordMd5.equals(existJobUserDO.getPassword())) {
            throw new RaincJobException("账号或密码错误");
        }
        String token = makeToken(existJobUserDO);
        return new ReturnT<>(token);
    }

    @Override
    public JobUserDO ifLogin(String token) {
        //解析秘钥
        JobUserDO jobUserDO = parseToken(token);
        if (jobUserDO != null) {
            Optional<JobUserDO> optionalJobUserDO = jobUserRepository.findByUsername(jobUserDO.getUsername());
            if (optionalJobUserDO.isPresent()) {
                if (optionalJobUserDO.get().getPassword().equals(jobUserDO.getPassword())) {
                    return optionalJobUserDO.get();
                }
            }
        }
        return null;
    }

    @Override
    public Page<JobUserDO> list(int page, int size, int role, String username) {
        if (role == -1) {
            return jobUserRepository.findAllByUsernameLike(username, PageRequest.of(page, size));
        } else {
            return jobUserRepository.findAllByUsernameLikeAndRole(username, role, PageRequest.of(page, size));
        }
    }

    @Override
    public ReturnT<String> update(JobUserDO jobUserDO) {
        Optional<JobUserDO> optionalJobUserDO = jobUserRepository.findById(jobUserDO.getId());
        if (!optionalJobUserDO.isPresent()) {
            throw new RaincJobException("更新失败，用户不存在");
        }
        if (StrUtil.isBlank(jobUserDO.getPassword())) {
            //密码为空则不更新密码
            jobUserDO.setPassword(optionalJobUserDO.get().getPassword());
        } else {
            //否则更新密码
            if (jobUserDO.getPassword().length() < 4 || jobUserDO.getPassword().length() > 20) {
                throw new RaincJobException("密码长度应在[4-20]");
            }
            // md5 加密
            jobUserDO.setPassword(DigestUtils.md5DigestAsHex(jobUserDO.getPassword().getBytes()));
        }
        jobUserRepository.save(jobUserDO);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> delete(long id) {
        jobUserRepository.deleteById(id);
        return ReturnT.SUCCESS;
    }


    /**
     * 生成jwt令牌
     *
     * @param jobUserDO
     * @return
     */
    private String makeToken(JobUserDO jobUserDO) {
        return jwtUtils.generateToken(jobUserDO);
    }

    /**
     * 解析jwt令牌
     *
     * @param token
     * @return
     */
    private JobUserDO parseToken(String token) {
        Claims claim = jwtUtils.getClaimByToken(token);
        String username = claim.get("username", String.class);
        String password = claim.get("password", String.class);
        if (!StrUtil.isAllNotBlank(username, password)) {
            return null;
        }
        JobUserDO jobUserDO = new JobUserDO();
        jobUserDO.setUsername(username);
        jobUserDO.setPassword(password);
        return jobUserDO;
    }
}
