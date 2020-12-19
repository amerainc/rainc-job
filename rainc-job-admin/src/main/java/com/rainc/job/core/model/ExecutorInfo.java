package com.rainc.job.core.model;

import com.rainc.job.core.biz.ExecutorBiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/12 10:46
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorInfo {
    private String address;
    private Date updateTime;
    private ExecutorBiz executorBiz;
}
