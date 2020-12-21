package com.rainc.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * handle回调参数
 * @Author rainc
 * @create 2020/12/20 10:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandleCallbackParam implements Serializable {
    private static final long serialVersionUID = 1;
    private long logId;
    private long logDateTime;

    private ReturnT<String> executeResult;
}
