package com.rainc.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author rainc
 * @create 2020/12/13 20:21
 * 分片参数
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
    /**
     * 总数
     */
    private int total;

    @Override
    public String toString() {
        return index + "/" + total;
    }

    public static ShardingParam parseString(String shardingParam) {
        String[] shardingArr = shardingParam.split("/");
        if (shardingArr.length == 2 && isNumeric(shardingArr[0]) && isNumeric(shardingArr[1])) {
            return new ShardingParam(Integer.parseInt(shardingArr[0]), Integer.parseInt(shardingArr[1]));
        }
        return null;
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
