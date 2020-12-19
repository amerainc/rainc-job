package com.rainc.job.core.executor;

import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.factory.AdminBizFactory;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.handler.IJobHandler;
import com.rainc.job.core.server.impl.ReactiveServer;
import com.rainc.job.core.util.IpUtil;
import com.rainc.job.core.util.NetUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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


    public void start() throws Exception {
        initServer(address, ip, port, appName, accessToken);
        initAdminBizList(adminAddresses, accessToken);
    }


    // ---------------------- admin-client (rpc invoker) ----------------------
    private static List<AdminBiz> adminBizList = new ArrayList<AdminBiz>();
    ;

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

        ReactiveServer reactiveServer = new ReactiveServer();
        reactiveServer.start(address, port, appname, accessToken);


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

}
