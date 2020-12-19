package com.rainc.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author rainc
 * @create 2020/12/11 20:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 执行器地址
     */
    private String address;
}
