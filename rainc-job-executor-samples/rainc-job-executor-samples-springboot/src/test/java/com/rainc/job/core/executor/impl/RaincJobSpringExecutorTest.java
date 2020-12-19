package com.rainc.job.core.executor.impl;

import com.rainc.job.core.executor.SampleRaincJob;
import org.junit.Test;

/**
 * @Author rainc
 * @create 2020/12/8 10:18
 */
public class RaincJobSpringExecutorTest {
    RaincJobSpringExecutor raincJobSpringExecutor = new RaincJobSpringExecutor();

    @Test
    public void findHandler() {
        raincJobSpringExecutor.postProcessAfterInstantiation(new SampleRaincJob(), "aaaa");
    }
}
