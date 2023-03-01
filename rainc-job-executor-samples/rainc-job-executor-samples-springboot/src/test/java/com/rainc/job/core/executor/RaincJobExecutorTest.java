package com.rainc.job.core.executor;

import com.rainc.job.core.handler.impl.MethodJobHandler;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @Author rainc
 * @create 2020/12/7 22:31
 */
public class RaincJobExecutorTest {
    @Test
    public void jobHandlerRepositoryTest() {
        Method jobHandlerRepositoryTest = null;
        try {
            jobHandlerRepositoryTest = this.getClass().getMethod("jobHandlerRepositoryTest");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        RaincJobExecutor.registryJobHandler("aaa", new MethodJobHandler(new Object(), jobHandlerRepositoryTest));
    }
}
