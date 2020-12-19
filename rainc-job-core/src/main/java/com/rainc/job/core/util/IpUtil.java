package com.rainc.job.core.util;

import cn.hutool.core.net.NetUtil;

/**
 * @Author rainc
 * @create 2020/12/12 14:09
 */
public class IpUtil {
    public static String getIp() {
        return NetUtil.getLocalhostStr();
    }

    public static String getIpPort(String ip, int port){
        if (ip==null) {
            return null;
        }
        return ip.concat(":").concat(String.valueOf(port));
    }
}
