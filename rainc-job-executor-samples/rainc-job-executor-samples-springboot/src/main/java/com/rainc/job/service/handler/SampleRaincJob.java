package com.rainc.job.service.handler;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.context.RaincJobHelper;
import com.rainc.job.core.handler.annotation.RaincJob;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * @Author rainc
 * @create 2020/10/24 12:24
 */
@Component
public class SampleRaincJob {
    @RaincJob("testHandler")
    public ReturnT<String> test(String param) throws Exception {
        LocalTime localTime = LocalTime.now();
        System.out.println(Thread.currentThread());
        System.out.println(param + "-时间-" + localTime);

        int i = Integer.parseInt(param);
        TimeUnit.SECONDS.sleep(i);
        return new ReturnT<>(200, "测试成功");
    }


    @RaincJob("mdjcHandler")
    public ReturnT<String> test2(String param) throws Exception {
        LocalTime localTime = LocalTime.now();
        System.out.println(param + "-时间-" + localTime);
        return ReturnT.SUCCESS;
    }

    @RaincJob("shardingHandler")
    public ReturnT<String> test3(String param) throws Exception {
        return RaincJobHelper.runShardTask((index, total) -> {
            System.out.println("分片任务-----" + index + "------" + total);
            return ReturnT.SUCCESS;
        });
    }
}
