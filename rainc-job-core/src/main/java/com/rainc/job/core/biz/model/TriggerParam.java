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
    private int jobId;

    private String executorHandler;
    private String executorParams;
    private int executorTimeout;
}
