package com.rainc.job.controller;


import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.HandleCallbackParam;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * biz层调用控制器
 * </p>
 *
 * @author rainc
 * @since 2020-12-11
 */
@RestController
@RequestMapping("/biz")
@Api("biz层接口")
public class JobBizController {
    @Resource
    AdminBiz adminBiz;

    /**
     * 注册执行器
     *
     * @param registryParam 注册参数
     * @return 注册成功失败
     */
    @ApiOperation("注册执行器接口")
    @PostMapping("/registry")
    public ReturnT<String> registerExecutor(@RequestBody RegistryParam registryParam) {
        return adminBiz.registry(registryParam);
    }

    /**
     * 删除执行器
     *
     * @param registryParam 注册删除
     * @return 删除成功失败
     */
    @ApiOperation("删除执行器接口")
    @DeleteMapping("/registry")
    public ReturnT<String> removeExecutor(@RequestBody RegistryParam registryParam) {
        return adminBiz.registryRemove(registryParam);
    }

    @ApiOperation("任务回调接口")
    @PostMapping("/callback")
    public ReturnT<String> callBack(@RequestBody List<HandleCallbackParam> callbackParamList) {
        return adminBiz.callback(callbackParamList);
    }
}
