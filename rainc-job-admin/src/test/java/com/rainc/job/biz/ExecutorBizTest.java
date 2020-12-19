package com.rainc.job.biz;

import com.rainc.job.core.biz.ExecutorBiz;
import com.rainc.job.core.biz.factory.ExecutorBizFactory;
import com.rainc.job.core.biz.model.ReturnT;
import org.junit.Test;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/12 18:30
 */
public class ExecutorBizTest {
    @Test
    public void testHandlers() {
        ExecutorBiz executorBiz = ExecutorBizFactory.createExecutorBiz("http://169.254.120.65:9999/", null);
        ReturnT<List<String>> handlers = executorBiz.handlers();
        System.out.println(handlers.getContent());
    }
}
