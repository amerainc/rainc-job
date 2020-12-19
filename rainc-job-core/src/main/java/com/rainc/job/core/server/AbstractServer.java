package com.rainc.job.core.server;

/**
 * @Author rainc
 * @create 2020/12/8 18:29
 */
public abstract class AbstractServer {
    public abstract void start(final String address, final int port, final String appname, final String accessToken);

    public abstract void stop();
    
}
