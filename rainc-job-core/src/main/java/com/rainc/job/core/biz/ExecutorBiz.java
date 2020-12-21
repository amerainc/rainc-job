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
    /**
     * 获取所有handler信息
     * @return
     */
    @RequestLine("GET /handlers")
    ReturnT<List<String>> handlers();

    /**
     * 触发任务
     * @param triggerParam
     * @return
     */
    @RequestLine("POST /run")
    ReturnT<String> run(TriggerParam triggerParam);

    /**
     * 心跳检测
     * @return
     */
    @RequestLine("GET /beat")
    ReturnT<String> beat();
}
