package com.rainc.job.controller;

import com.rainc.job.controller.annotation.PermissionLimit;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.model.JobUserDO;
import com.rainc.job.service.UserService;
import com.rainc.job.util.QueryUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author rainc
 * @create 2020/12/23 21:49
 */
@Api("用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @ApiOperation("注册")
    @PostMapping("/")
    @PermissionLimit(admin = true)
    public ReturnT<String> add(@Validated @RequestBody JobUserDO jobUserDO) {
        return userService.add(jobUserDO);
    }

    @ApiOperation("更新")
    @PutMapping("/")
    @PermissionLimit(admin = true)
    public ReturnT<String> update(@Validated @RequestBody JobUserDO jobUserDO) {
        return userService.update(jobUserDO);
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    @PermissionLimit(admin = true)
    public ReturnT<String> delete(@PathVariable long id) {
        return userService.delete(id);
    }

    @ApiOperation("查询用户列表")
    @GetMapping("/list")
    @PermissionLimit(admin = true)
    public ReturnT<Page<JobUserDO>> list(@RequestParam(required = false, defaultValue = "1") int page,
                                         @RequestParam(required = false, defaultValue = "10") int size,
                                         @RequestParam(defaultValue = "-1") int role,
                                         @RequestParam String username) {
        //jpa分页默认从0开始
        if (page > 0) {
            page--;
        }
        return new ReturnT<>(userService.list(page, size, role,  QueryUtil.castToLike(username)));
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    @PermissionLimit(limit = false)
    public ReturnT<String> login(@Validated @RequestBody JobUserDO jobUserDO) {
        return userService.login(jobUserDO);
    }

    @ApiOperation("用户信息")
    @GetMapping("/info")
    public ReturnT<JobUserDO> info(@RequestAttribute JobUserDO jobUserDO) {
        return new ReturnT<>(jobUserDO);
    }
}
