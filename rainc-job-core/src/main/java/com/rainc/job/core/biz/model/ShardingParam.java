package com.rainc.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author rainc
 * @create 2020/12/13 20:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardingParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 索引
     */
    private int index;
    private int total;
}
