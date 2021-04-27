package com.rainc.job.core.handler;

import com.rainc.job.core.biz.model.ReturnT;

/**
 * 任务处理器接口
 *
 * @Author rainc
 * @create 2020/12/7 21:17
 */
public interface IJobHandler {
    int FAIL_TIMEOUT = 502;

    ReturnT<String> execute(String parms) throws Exception;
}
