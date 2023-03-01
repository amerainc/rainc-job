package com.rainc.job.core.util;

import cn.hutool.core.net.NetUtil;

/**
 * @Author rainc
 * @create 2020/12/12 14:16
 */
public class MyNetUtil {
    /**
     * 找到可用的端口
     *
     * @param defaultPort 默认端口
     * @return
     */
    public static int findAvailablePort(int defaultPort) {
        if (NetUtil.isUsableLocalPort(defaultPort)) {
            return defaultPort;
        }
        return NetUtil.getUsableLocalPort();
    }
}
