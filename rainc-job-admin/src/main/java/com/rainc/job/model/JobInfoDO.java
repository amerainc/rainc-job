package com.rainc.job.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/13 11:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @NotNull(message = "请选择执行器")
    private Long jobGroup;

    /**
     * 任务执行CRON
     */
    @NotBlank(message = "请输入cron表达式")
    private String jobCron;

    /**
     * 任务描述
     */
    @NotBlank(message = "请输入任务描述")
    private String jobDesc;
    /**
     * 创建时间
     */
    private Date addTime;
    /**
     * 修改时间
     */
    private Date upDateTime;

    /**
     * 负责人
     */
    @NotBlank(message = "请输入负责人")
    private String author;

    /**
     * 报警邮箱
     */
    private String alarmEmail;

    /**
     * 路由策略
     */
    @NotBlank(message = "请选择路由策略")
    private String executorRouteStrategy;

    /**
     * 执行器任务handler
     */
    @NotBlank(message = "选择handler")
    private String executorHandler;

    /**
     * 执行器任务参数
     */
    @Column(length = 512)
    private String executorParam;

    /**
     * 阻塞策略
     */
    @NotBlank(message = "请选择阻塞策略")
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
