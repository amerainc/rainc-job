package com.rainc.job.core.biz.factory;

import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.util.BizUtil;

/**
 * 简单工厂模式
 *
 * @Author rainc
 * @create 2020/12/12 18:26
 */
public class ExecutorBizFactory {
    /**
     * 创建executorBiz
     *
     * @param address     访问地址
     * @param accessToken 验证秘钥
     * @return executorBiz实例
     */
    public static ExecutorBiz createExecutorBiz(final String address, final String accessToken) {
        return BizUtil.createBiz(address, accessToken, ExecutorBiz.class);
    }
}
