package com.rainc.job.controller;


import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author rainc
 * @since 2020-12-11
 */
@RestController
@RequestMapping("/registry")
public class JobRegistryController {
    @Resource
    AdminBiz adminBiz;

    @PostMapping("/")
    public ReturnT<String> registerExecutor(@RequestBody RegistryParam registryParam) {
        return adminBiz.registry(registryParam);
    }
}
