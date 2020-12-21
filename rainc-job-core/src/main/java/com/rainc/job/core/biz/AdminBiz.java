package com.rainc.job.core.biz;

import com.rainc.job.core.biz.model.HandleCallbackParam;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import feign.RequestLine;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/11 14:55
 */
public interface AdminBiz {
    // ---------------------- registry ----------------------

    /**
     * 执行器注册
     *
     * @param registryParam
     * @return
     */
    @RequestLine("POST /biz/registry")
    ReturnT<String> registry(RegistryParam registryParam);

    /**
     * 执行器移除
     *
     * @param registryParam
     * @return
     */
    @RequestLine("DELETE /biz/registry")
    ReturnT<String> registryRemove(RegistryParam registryParam);

    /**
     * 回调接口
     *
     * @param callbackParamList
     * @return
     */
    @RequestLine("POST /biz/callback")
    ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);
}
