package com.rainc.job.core.executor.impl;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.core.executor.RaincJobExecutor;
import com.rainc.job.core.handler.annotation.RaincJob;
import com.rainc.job.core.handler.impl.MethodJobHandler;
import com.rainc.job.core.biz.model.ReturnT;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @Author rainc
 * @create 2020/12/8 9:56
 */
@Log4j2
public class RaincJobSpringExecutor extends RaincJobExecutor implements InitializingBean, SmartInstantiationAwareBeanPostProcessor, DisposableBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            super.start();
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }


    @Override
    public void destroy() throws Exception {
        try {
            super.destroy();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 注册所有含@RaincJob标记的方法到jobHandlerRepository
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            //只注册带有RaincJob 且返回值为Result<String>的方法
            RaincJob raincJob = AnnotatedElementUtils.findMergedAnnotation(method, RaincJob.class);
            if (raincJob == null) {
                return;
            }

            String name = raincJob.value().trim();
            //检测handler名是否合法
            if (StrUtil.isBlank(name)) {
                throw new RuntimeException("rainc-job method-jobhandler name invalid, for[" + bean.getClass() + "#" + method.getName() + "] .");
            }
            if (loadJobHandler(name) != null) {
                throw new RuntimeException("rainc-job jobhandler[" + name + "] naming conflicts.");
            }

            //检测method参数是否合法
            if (!(method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(String.class))) {
                throw new RuntimeException("rainc-job method-jobhandler param-classtype invalid, for[" + bean.getClass() + "#" + method.getName() + "] , " +
                        "The correct method format like \" public ReturnT<String> execute(String param) \" .");
            }
            //检测回调参数是否合法
            if (!method.getReturnType().isAssignableFrom(ReturnT.class)) {
                throw new RuntimeException("rainc-job method-jobhandler return-classtype invalid, for[" + bean.getClass() + "#" + method.getName() + "] , " +
                        "The correct method format like \" public ReturnT<String> execute(String param) \" .");
            }

            //注册handler
            method.setAccessible(true);
            registJobHandler(name, new MethodJobHandler(bean, method));
        });
        return true;
    }
}
