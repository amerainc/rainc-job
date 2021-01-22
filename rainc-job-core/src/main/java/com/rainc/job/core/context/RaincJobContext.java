package com.rainc.job.core.context;

import com.rainc.job.core.biz.model.ShardingParam;
import lombok.Getter;

/**
 * @Author rainc
 * @create 2021/1/22 12:35
 */
public class RaincJobContext {

    /**
     * job id
     */
    @Getter
    private final long jobId;

    /**
     * job param
     */
    @Getter
    private final String jobParam;

    // ---------------------- for shard ----------------------
    @Getter
    private final ShardingParam shardingParam;


    public RaincJobContext(long jobId, String jobParam, ShardingParam shardingParam) {
        this.jobId = jobId;
        this.jobParam = jobParam;
        this.shardingParam = shardingParam;
    }


    // ---------------------- tool ----------------------

    private static final InheritableThreadLocal<RaincJobContext> contextHolder = new InheritableThreadLocal<RaincJobContext>();

    public static void setRaincJobContext(RaincJobContext raincJobContext) {
        contextHolder.set(raincJobContext);
    }

    public static void removeContext() {
        contextHolder.remove();
    }

    public static RaincJobContext getRaincJobContext() {
        return contextHolder.get();
    }
}
