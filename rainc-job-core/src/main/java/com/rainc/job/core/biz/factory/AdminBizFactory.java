package com.rainc.job.core.biz.factory;

import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.util.BizUtil;

/**
 * 简单工厂模式
 *
 * @Author rainc
 * @create 2020/12/12 14:36
 */
public class AdminBizFactory {
    /**
     * 创建adminBiz
     *
     * @param address     访问地址
     * @param accessToken 验证秘钥
     * @return adminBiz实例
     */
    public static AdminBiz createAdminBiz(final String address, final String accessToken) {
        return BizUtil.createBiz(address, accessToken, AdminBiz.class);
    }
}
