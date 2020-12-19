package com.rainc.job.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/13 10:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rainc_job_group")
public class JobGroupDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    /**
     * 应用名称 与appInfo一致
     */
    @Column(nullable = false)
    private String appName;
    /**
     * 自定义命名
     */
    @Column(nullable = false)
    private String title;
    /**
     * 执行器列表，有则手动，空则自动注册
     */
    private String addressList;
}
