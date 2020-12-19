package com.rainc.job.router;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.biz.model.TriggerParam;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/13 20:59
 */
@Log4j2
public abstract class ExecutorRouter {
    /**
     * route address
     *
     * @param addressList
     * @return  ReturnT.content=address
     */
    public abstract ReturnT<String> route(TriggerParam triggerParam, List<String> addressList);
}
