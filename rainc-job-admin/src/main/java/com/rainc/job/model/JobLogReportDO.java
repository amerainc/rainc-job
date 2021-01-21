package com.rainc.job.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author rainc
 * @create 2021/1/2 11:26
 */
@Data
@Entity
@Table(name = "rainc_job_log_report")
public class JobLogReportDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    /**
     * 调度-时间
     */
    private Date triggerDay;
    /**
     * 运行中-日志总数
     */
    private Long runningCount;
    /**
     * 成功-日志总数
     */
    private Long sucCount;
    /**
     * 失败-日志总数
     */
    private Long failCount;
}
