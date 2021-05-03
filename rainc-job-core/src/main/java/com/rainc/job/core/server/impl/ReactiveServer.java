package com.rainc.job.core.server.impl;


import cn.hutool.core.util.StrUtil;
import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.impl.ExecutorBizImpl;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import com.rainc.job.core.constant.AdminBizConfig;
import com.rainc.job.core.server.AbstractServer;
import com.rainc.job.core.thread.ExecutorRegistryThread;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

/**
 * @Author rainc
 * @create 2020/12/9 21:41
 */
@Log4j2
public class ReactiveServer extends AbstractServer {
    private Thread thread;

    @Override
    public void start(final String address, final int port, final String appname, final String accessToken) {
        thread = new Thread(() -> {
            DisposableServer disposableServer = null;
            try {
                //创建服务器
                ReactorHttpHandlerAdapter reactorHttpHandlerAdapter = new ReactorHttpHandlerAdapter(toHttpHandler(router(accessToken)));
                disposableServer = HttpServer.create()
                        .handle(reactorHttpHandlerAdapter)
                        .port(port)
                        .bindNow();
                log.info(">>>>>>>> rainc-job remoting server start nettype={},port={}", ReactiveServer.class, port);
                //开启注册线程
                startRegistry(appname, address);
                //启动服务器并阻塞
                disposableServer.onDispose().block();
            } catch (Exception e) {
                if (InterruptedException.class.getName().equals(e.getMessage())) {
                    log.info(">>>>>>>> rainc-job remoting server stop.");
                } else {
                    log.error(">>>>>>>> rainc-job remoting server error.", e);
                }
            } finally {
                //服务器停止
                try {
                    if (disposableServer != null) {
                        disposableServer.disposeNow();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        });
        thread.setDaemon(true);
        thread.setName("rainc-job ractive server thread");
        thread.start();
    }

    @Override
    public void stop() {
        //销毁服务线程
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        stopRegistry();
    }

    public RouterFunction<ServerResponse> router(String accessToken) {
        ServerHandler serverHandler = new ServerHandler();
        return route(GET("/handlers"), serverHandler::jobHandlers)
                .andRoute(POST("/run"), serverHandler::run)
                .andRoute(GET("/kill/{id}"), serverHandler::kill)
                //accessToken拦截
                .filter((request, next) -> {
                    if (StrUtil.isBlank(accessToken)) {
                        return next.handle(request);
                    }
                    String token = request.headers().firstHeader(AdminBizConfig.XXL_JOB_ACCESS_TOKEN);
                    if (accessToken.equals(token)) {
                        return next.handle(request);
                    }
                    return ServerResponse.ok().bodyValue(new ReturnT<String>(ReturnT.FAIL_CODE, "access token错误"));
                });
    }


    static class ServerHandler {
        private final ExecutorBiz executorBiz = ExecutorBizImpl.getInstance();

        /**
         * 执行器查询服务
         *
         * @param request
         * @return
         */
        public Mono<ServerResponse> jobHandlers(ServerRequest request) {
            Mono<ReturnT<List<String>>> handlersMono = Mono.create((t) -> t.success(executorBiz.handlers()));
            return ServerResponse.ok().body(handlersMono, ReturnT.class);
        }

        /**
         * 运行服务
         *
         * @param request
         * @return
         */
        public Mono<ServerResponse> run(ServerRequest request) {
            Mono<TriggerParam> triggerParamMono = request.bodyToMono(TriggerParam.class);
            return triggerParamMono
                    .map(executorBiz::run)
                    .flatMap((returnT -> ServerResponse.ok().bodyValue(returnT)));
        }

        /**
         * 任务终止服务
         *
         * @param request
         * @return
         */
        public Mono<ServerResponse> kill(ServerRequest request) {
            String id = request.pathVariable("id");
            Mono<ReturnT<String>> killMono = Mono.create((t) -> t.success(executorBiz.kill(Long.parseLong(id))));
            return ServerResponse.ok().body(killMono, ReturnT.class);
        }
    }


    // ---------------------- 注册线程 ----------------------

    public void startRegistry(final String appname, final String address) {
        // start registry
        ExecutorRegistryThread.getInstance().start(appname, address);
    }

    public void stopRegistry() {
        // stop registry
        ExecutorRegistryThread.getInstance().toStop();
    }
}
