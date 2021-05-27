package com.rainc.job.service.handler;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.context.RaincJobHelper;
import com.rainc.job.core.handler.annotation.RaincJob;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Author rainc
 * @create 2020/10/24 12:24
 */
@Component
public class SampleRaincJob {

    /**
     * 普通处理器
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RaincJob("demoHandler")
    public ReturnT<String> demoHandler(String param) throws Exception {
        System.out.println(new Date() + ":" + param);
        return ReturnT.SUCCESS;
    }

    /**
     * 异常任务
     * @param param
     * @return
     * @throws Exception
     */
    @RaincJob("exceptionHandler")
    public ReturnT<String> exceptionHandler(String param) throws Exception {
        System.out.println(new Date() + ":" + param);
        int a = 1 / 0;
        return ReturnT.SUCCESS;
    }

    /**
     * 阻塞任务测试
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RaincJob("demoHandler2")
    public ReturnT<String> test2(String param) throws Exception {
        System.out.println("开始执行：" + new Date());
        TimeUnit.SECONDS.sleep(15);
        System.out.println("执行结束：" + new Date());
        return ReturnT.SUCCESS;
    }

    /**
     * 分片任务
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RaincJob("shardingHandler")
    public ReturnT<String> shardingHandler(String param) throws Exception {
        return RaincJobHelper.runShardTask((index, total) -> {
            int length = param.length();
            int step = length / total;
            if (index < total - 1) {
                System.out.println(param.substring(step * index, step * (index + 1)));
            } else {
                System.out.println(param.substring(step * index));
            }
            return ReturnT.SUCCESS;
        });
    }
}
