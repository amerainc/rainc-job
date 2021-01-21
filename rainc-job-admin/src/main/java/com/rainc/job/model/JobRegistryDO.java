package com.rainc.job.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/21 21:41
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rainc_job_registry")
public class JobRegistryDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private String appName;
    /**
     * 执行器地址
     */
    private String address;
    /**
     * 更新时间
     */
    private Date updateTime;
}
