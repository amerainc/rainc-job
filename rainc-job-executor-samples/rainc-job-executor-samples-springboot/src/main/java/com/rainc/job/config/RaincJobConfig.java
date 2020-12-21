package com.rainc.job.config;

import com.rainc.job.core.executor.impl.RaincJobSpringExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author rainc
 * @create 2020/12/12 13:18
 */
@Configuration
@Log4j2
public class RaincJobConfig {
    @Resource
    ExecutorProperties executorProperties;

    @Bean
    public RaincJobSpringExecutor raincJobExecutor() {
        log.info(">>>>>>>> rainc-job config init.");
        RaincJobSpringExecutor raincJobSpringExecutor = new RaincJobSpringExecutor();
        raincJobSpringExecutor.setAddress(executorProperties.getAddress());
        raincJobSpringExecutor.setAdminAddresses(executorProperties.getAdminAddresses());
        raincJobSpringExecutor.setAppName(executorProperties.getAppName());
        raincJobSpringExecutor.setAccessToken(executorProperties.getAccessToken());
        raincJobSpringExecutor.setIp(executorProperties.getIp());
        raincJobSpringExecutor.setPort(executorProperties.getPort());
        raincJobSpringExecutor.setTaskPoolMax(executorProperties.getTaskPoolMax());
        return raincJobSpringExecutor;
    }
}
