package com.rainc.job.core.model;

import com.rainc.job.core.biz.ExecutorBiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 执行器信息
 *
 * @Author rainc
 * @create 2020/12/12 10:46
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorInfo {
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 执行器地址
     */
    private String address;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 执行器业务
     */
    private ExecutorBiz executorBiz;
}
