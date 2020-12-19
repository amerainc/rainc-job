package com.rainc.job.core.biz;

import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import feign.Headers;
import feign.RequestLine;

/**
 * @Author rainc
 * @create 2020/12/11 14:55
 */
public interface AdminBiz {
    // ---------------------- registry ----------------------

    /**
     * registry
     *
     * @param registryParam
     * @return
     */
    @RequestLine("POST /registry/")
    public ReturnT<String> registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    @RequestLine("DELETE /registry/")
    public ReturnT<String> registryRemove(RegistryParam registryParam);
}
