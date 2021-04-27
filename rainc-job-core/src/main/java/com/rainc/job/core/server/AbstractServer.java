package com.rainc.job.core.server;

/**
 * @Author rainc
 * @create 2020/12/8 18:29
 * 服务器抽象类
 */
public abstract class AbstractServer {
    /**
     * 启动服务器
     * @param address 地址
     * @param port 端口
     * @param appname 执行器名
     * @param accessToken 秘钥
     */
    public abstract void start(final String address, final int port, final String appname, final String accessToken);

    /**
     * 停止服务器
     */
    public abstract void stop();
    
}
