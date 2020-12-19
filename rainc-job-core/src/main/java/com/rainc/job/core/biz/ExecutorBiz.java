package com.rainc.job.core.biz;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import feign.RequestLine;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/12 15:50
 */
public interface ExecutorBiz {
    @RequestLine("GET /handlers")
    ReturnT<List<String>> handlers();

    @RequestLine("POST /run")
    ReturnT<String> run(TriggerParam triggerParam);

    @RequestLine("GET /beat")
    ReturnT<String> beat();
}
