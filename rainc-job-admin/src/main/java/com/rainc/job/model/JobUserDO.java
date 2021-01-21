package com.rainc.job.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author rainc
 * @create 2020/12/23 21:37
 */
@Data
@Entity
@Table(name = "rainc_job_user")
public class JobUserDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    /**
     * 账号
     */
    @NotBlank(message = "请输入用户名")
    @Size(min = 4, max = 20, message = "用户名长度应在[4-20]")
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 0-普通用户，1-管理员
     */
    private Integer role;
    /**
     * 权限：执行器组
     */
    private String permission;
}
