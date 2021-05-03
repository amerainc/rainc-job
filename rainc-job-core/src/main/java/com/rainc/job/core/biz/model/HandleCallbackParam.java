package com.rainc.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @Author rainc
 * @create 2020/12/20 10:46
 * 任务处理回调参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandleCallbackParam implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * 日志id
     */
    private long logId;
    /**
     * 日志时间
     */
    private long logDateTime;
    /**
     * 执行结果
     */
    private ReturnT<String> executeResult;
}
