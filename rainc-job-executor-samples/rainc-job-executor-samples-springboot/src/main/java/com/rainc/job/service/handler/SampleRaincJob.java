package com.rainc.job.service.handler;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.context.RaincJobHelper;
import com.rainc.job.core.handler.annotation.RaincJob;
import org.springframework.stereotype.Component;

/**
 * @Author rainc
 * @create 2020/10/24 12:24
 */
@Component
public class SampleRaincJob {
    /**
     * 普通任务
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RaincJob("demoHandler")
    public ReturnT<String> test(String param) throws Exception {
        System.out.println(param);
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
    public ReturnT<String> test3(String param) throws Exception {
        return RaincJobHelper.runShardTask((index, total) -> {
            System.out.println("分片任务-----" + index + "------" + total);
            return ReturnT.SUCCESS;
        });
    }
}
