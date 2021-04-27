package com.rainc.job.core.biz.model;

import lombok.*;

import java.io.Serializable;

/**
 * @Author rainc
 * @create 2020/12/13 20:39
 * 任务触发参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TriggerParam implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * 触发任务的id
     */
    private long jobId;
    /**
     * 任务处理器
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

    /**
     * 分片参数
     */
    private ShardingParam shardingParam;
}
