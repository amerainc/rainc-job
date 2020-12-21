package com.rainc.job.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/13 11:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rainc_job_info")
public class JobInfoDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    /**
     * 执行器分组主键
     */
    private Long jobGroup;

    /**
     * 任务执行CRON
     */
    private String jobCron;

    /**
     * 任务说明
     */
    private String jobDesc;

    private Date addTime;
    private Date upDateTime;

    /**
     * 作者
     */
    private String author;

    /**
     * 报警邮箱
     */
    private String alarmEmail;

    /**
     * 路由策略
     */
    private String executorRouteStrategy;

    /**
     * 执行器任务handler
     */
    private String executorHandler;

    /**
     * 执行器任务参数
     */
    @Column(length = 512)
    private String executorParam;

    /**
     * 阻塞策略
     */
    private String executorBlockStrategy;

    /**
     * 超时时间，单位秒
     */
    private Integer executorTimeOut;

    /**
     * 失败重试次数
     */
    private Integer executorFailRetryCount;

    /**
     * 调度状态
     */
    private Boolean triggerStatus;

    /**
     * 上次调度时间
     */
    private Long triggerLastTime;

    /**
     * 下次调度时间
     */
    private Long triggerNextTime;
}
