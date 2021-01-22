package com.rainc.job.core.context;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.ShardingParam;

/**
 * @Author rainc
 * @create 2021/1/22 13:02
 */
public class RaincJobHelper {
    // ---------------------- base info ----------------------

    /**
     * 当前任务 id
     *
     * @return
     */
    public static long getJobId() {
        RaincJobContext raincJobContext = RaincJobContext.getRaincJobContext();
        if (raincJobContext == null) {
            return -1;
        }

        return raincJobContext.getJobId();
    }

    /**
     * 当前任务参数
     *
     * @return
     */
    public static String getJobParam() {
        RaincJobContext raincJobContext = RaincJobContext.getRaincJobContext();
        if (raincJobContext == null) {
            return null;
        }
        return raincJobContext.getJobParam();
    }

    /**
     * 当前分片索引
     *
     * @return
     */
    public static ShardingParam getShardingParam() {
        RaincJobContext raincJobContext = RaincJobContext.getRaincJobContext();
        if (raincJobContext == null) {
            return null;
        }
        return raincJobContext.getShardingParam();
    }

    /**
     * 执行分片函数
     *
     * @param shardFunction
     * @return
     */
    public static ReturnT<String> runShardTask(ShardFunction shardFunction) {
        ShardingParam shardingParam = getShardingParam();
        if (shardingParam == null) {
            return shardFunction.shardTask(0, 1);
        }
        return shardFunction.shardTask(shardingParam.getIndex(), shardingParam.getTotal());
    }

    @FunctionalInterface
    public interface ShardFunction {
        /**
         * 分片函数
         *
         * @param index
         * @param total
         * @return
         */
        ReturnT<String> shardTask(int index, int total);
    }
}
