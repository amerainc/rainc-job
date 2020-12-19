package com.rainc.job.service.handler;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.handler.annotation.RaincJob;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * @Author rainc
 * @create 2020/10/24 12:24
 */
@Component
public class SampleRaincJob {
    @RaincJob("testHandler")
    public ReturnT<String> test(String param) {
        LocalTime localTime=LocalTime.now();
        System.out.println(param+"-时间-"+localTime);
        return ReturnT.SUCCESS;
    }


    @RaincJob("mdjcHandler")
    public ReturnT<String> test2(String param) {
        LocalTime localTime=LocalTime.now();
        System.out.println(param+"-时间-"+localTime);
        return ReturnT.SUCCESS;
    }
}
