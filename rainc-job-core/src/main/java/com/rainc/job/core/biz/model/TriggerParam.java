package com.rainc.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author rainc
 * @create 2020/12/13 20:39
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerParam implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * 触发任务的id
     */
    private long jobId;
    /**
     * 执行器handler
     */
    private String executorHandler;
    /**
     * 执行器参数
     */
    private String executorParams;

    /**
     * 阻塞策略
     */
    private String executorBlockStrategy;

    /**
     * 执行器超时时间
     */
    private int executorTimeout;
    /**
     * 日志id
     */
    private long logId;
    /**
     * 日志时间
     */
    private long logDateTime;

}
