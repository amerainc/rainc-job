package com.rainc.job.service.handler;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.context.RaincJobHelper;
import com.rainc.job.core.handler.annotation.RaincJob;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author rainc
 * @create 2020/10/24 12:24
 */
@Component
public class SampleRaincJob {

    @RaincJob("demoHandler")
    public ReturnT<String> test(String param) throws Exception {
        System.out.println(new Date() + ":" + param);
        System.out.println();
        int a = 1 / 0;
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
