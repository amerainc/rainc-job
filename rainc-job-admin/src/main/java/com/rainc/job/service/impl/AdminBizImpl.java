package com.rainc.job.service.impl;

import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Author rainc
 * @create 2020/12/11 21:43
 */
@Service
public class AdminBizImpl implements AdminBiz {
    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        if (!StringUtils.hasText(registryParam.getAddress())
                || !StringUtils.hasText(registryParam.getAppName())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Illegal Argument.");
        }

        RaincJobScheduler.registerExecutor(registryParam.getAppName(), registryParam.getAddress());
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        if (!StringUtils.hasText(registryParam.getAddress())
                || !StringUtils.hasText(registryParam.getAppName())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Illegal Argument.");
        }

        RaincJobScheduler.removeExecutor(registryParam.getAppName(), registryParam.getAddress());
        return ReturnT.SUCCESS;
    }
}
