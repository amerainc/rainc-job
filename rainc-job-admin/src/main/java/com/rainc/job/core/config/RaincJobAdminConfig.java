package com.rainc.job.core.config;

/**
 * @Author rainc
 * @create 2020/12/11 21:36
 */

import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.respository.JobGroupRepository;
import com.rainc.job.respository.JobInfoRepository;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author rainc
 * @create 2020/12/11 20:48
 */
@Log4j2
@Configuration
public class RaincJobAdminConfig implements InitializingBean, DisposableBean {
    private static RaincJobAdminConfig adminConfig;

    public static RaincJobAdminConfig getAdminConfig() {
        return adminConfig;
    }

    private RaincJobScheduler raincJobScheduler;

    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;
        raincJobScheduler = new RaincJobScheduler();
        raincJobScheduler.init();
    }

    @Override
    public void destroy() throws Exception {
        raincJobScheduler.destroy();
    }

    @Resource
    @Getter
    JobGroupRepository jobGroupRepository;

    @Resource
    @Getter
    JobInfoRepository jobInfoRepository;


    //------------------------properties----------------------------

    @Resource
    AdminProperties adminProperties;

    public String getAccessToken() {
        return adminProperties.getAccessToken();
    }

    public int getTriggerPoolFastMax() {
        return Math.max(adminProperties.getTriggerPoolFastMax(), 200);
    }

    public int gettriggerPoolSlowMax() {
        return Math.max(adminProperties.getTriggerPoolSlowMax(), 100);
    }
}
