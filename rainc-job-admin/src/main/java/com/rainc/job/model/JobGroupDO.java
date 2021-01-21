package com.rainc.job.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
    @NotBlank(message = "请输入appName")
    @Size(min =4,max = 64,message = "AppName长度限制为4~64")
    private String appName;
    /**
     * 自定义命名
     */
    @NotBlank(message = "请输入名称")
    @Size(min =4,max = 12,message = "名称长度限制为4~12")
    private String title;
    /**
     * 执行器列表，有则手动，空则自动注册
     */
    private String addressList;
}
