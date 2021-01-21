package com.rainc.job.core.model;

import com.rainc.job.core.biz.ExecutorBiz;
import lombok.*;

import java.io.Serializable;
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
@EqualsAndHashCode(of = {"address"})
public class ExecutorInfo implements Serializable {
    private static final long serialVersionUID = 1;
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
