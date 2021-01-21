package com.rainc.job.core.util;

/**
 * @Author rainc
 * @create 2020/12/12 14:16
 */
public class MyNetUtil {
    /**
     * find avaliable port
     *
     * @param defaultPort
     * @return
     */
    public static int findAvailablePort(int defaultPort) {
        if (cn.hutool.core.net.NetUtil.isUsableLocalPort(defaultPort)) {
            return defaultPort;
        }
        return cn.hutool.core.net.NetUtil.getUsableLocalPort();
    }
}
