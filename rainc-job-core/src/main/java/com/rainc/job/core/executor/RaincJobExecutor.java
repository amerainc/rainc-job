package com.rainc.job.core.executor;

import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.factory.AdminBizFactory;
import com.rainc.job.core.handler.IJobHandler;
import com.rainc.job.core.server.impl.ReactiveServer;
import com.rainc.job.core.thread.JobThread;
import com.rainc.job.core.thread.TaskCallbackThread;
import com.rainc.job.core.thread.TaskPoolHelper;
import com.rainc.job.core.util.IpUtil;
import com.rainc.job.core.util.NetUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author rainc
 * @create 2020/10/23 17:18
 */
@Log4j2
public class RaincJobExecutor {
    @Setter
    private String adminAddresses;
    @Setter
    private String appName;
    @Setter
    private String accessToken;
    @Setter
    private String address;
    @Setter
    private String ip;
    @Setter
    private int port;
    @Setter
    private int taskPoolMax;


    public void start() throws Exception {
        initServer(address, ip, port, appName, accessToken);
        initAdminBizList(adminAddresses, accessToken);
        TaskCallbackThread.getInstance().start();
        TaskPoolHelper.toStart(taskPoolMax);

    }

    public void destroy() throws Exception {
        stopServer();
        TaskPoolHelper.toStop();
        TaskCallbackThread.getInstance().toStop();
    }

    /**
     * 注册中心远程调用实例
     */
    private static List<AdminBiz> adminBizList = new ArrayList<AdminBiz>();

    private void initAdminBizList(String adminAddresses, String accessToken) throws Exception {
        if (adminAddresses != null && adminAddresses.trim().length() > 0) {
            for (String address : adminAddresses.trim().split(",")) {
                if (address != null && address.trim().length() > 0) {
                    AdminBiz adminBiz = AdminBizFactory.createAdminBiz(address, accessToken);
                    adminBizList.add(adminBiz);
                }
            }
        }
    }

    public static List<AdminBiz> getAdminBizList() {
        return adminBizList;
    }


    //-----------------------------executor-server------------------------------------------------

    private ReactiveServer reactiveServer;

    private void initServer(String address, String ip, int port, String appname, String accessToken) {
        port = port > 0 ? port : NetUtil.findAvailablePort(9999);
        ip = (ip != null && ip.trim().length() > 0) ? ip : IpUtil.getIp();

        if (address == null || address.trim().length() == 0) {
            // registry-address：default use address to registry , otherwise use ip:port if address is null
            String ip_port_address = IpUtil.getIpPort(ip, port);
            address = "http://{ip_port}/".replace("{ip_port}", ip_port_address);
        }

        reactiveServer = new ReactiveServer();
        reactiveServer.start(address, port, appname, accessToken);
    }

    private void stopServer() {
        try {
            reactiveServer.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


//--------------------------------jobHandlerRepository-----------------------------------------------
    /**
     * 管理所有执行handler
     */
    private static final ConcurrentMap<String, IJobHandler> jobHandlerRepository = new ConcurrentHashMap<>();

    public static IJobHandler registJobHandler(String name, IJobHandler jobHandler) {
        log.info(">>>>>>>> rainc-job regist jobhandler success,name:{},jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }

    public static IJobHandler loadJobHandler(String name) {
        return jobHandlerRepository.get(name);
    }

    public static List<String> getHandlerNames() {
        return new ArrayList<>(jobHandlerRepository.keySet());
    }

    //-------------------------------------任务线程管理-------------------------------------------------------------
    private static final ConcurrentMap<Long, JobThread> jobThreadRepository = new ConcurrentHashMap<>();

    public static JobThread registJobThread(long jobId, IJobHandler handler, String removeOldReason) {
        //创建新任务线程并启动
        JobThread newJobThread = new JobThread(jobId, handler);
        newJobThread.start();
        log.info(">>>>>>>>>>> rainc-job regist JobThread success, jobId:{}, handler:{}", new Object[]{jobId, handler});
        //替换线程  hashmap put如果有旧值则返回旧值
        JobThread oldJobThread = jobThreadRepository.put(jobId, newJobThread);

        if (oldJobThread != null) {
            //停止旧线程的运行
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }

        return newJobThread;
    }

    public static JobThread removeJobThread(long jobId, String removeOldReason) {
        JobThread oldJobThread = jobThreadRepository.remove(jobId);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();

            return oldJobThread;
        }
        return null;
    }

    public static JobThread loadJobThread(long jobId) {
        return jobThreadRepository.get(jobId);
    }

}
