package com.rainc.job.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/19 15:52
 */
@Data
@Entity
@Table(name = "rainc_job_log")
public class JobLogDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    /**
     * 执行器 主键ID
     */
    private Long jobGroup;
    /**
     * 任务 主键ID
     */
    private Long jobId;

    /**
     * 任务本次的执行地址
     */
    private String executorAddress;

    /**
     * 执行器任务handler
     */
    private String executorHandler;

    /**
     * 执行器任务参数
     */
    private String executorParam;
    /**
     * 执行器任务分配参数格式如1/2
     */
    private String executorShardingParam;
    /**
     * 失败重试次数
     */
    private Integer executorFailRetryCount;
    //-----------------调度------------------
    /**
     * 调度时间
     */
    private Date triggerTime;

    /**
     * 调度结果
     */
    private Integer triggerCode;
    /**
     * 调度日志
     */
    @Lob
    private String triggerMsg;

//---------------------执行-----------------
    /**
     * 执行时间
     */
    private Date handleTime;
    /**
     * 执行结果
     */
    private Integer handleCode;

    /**
     * 执行日志
     */
    @Lob
    private String handleMsg;

    /**
     * 告警状态
     */
    private Short alarmStatus;
}
