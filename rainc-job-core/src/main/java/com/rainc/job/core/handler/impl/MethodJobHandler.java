package com.rainc.job.core.handler.impl;

import com.rainc.job.core.handler.IJobHandler;
import com.rainc.job.core.biz.model.ReturnT;

import java.lang.reflect.Method;

/**
 * @Author rainc
 * @create 2020/12/7 22:34
 */
public class MethodJobHandler implements IJobHandler {
    /**
     * 调用实例
     */
    private final Object target;
    /**
     * 调用方法
     */
    private final Method method;

    public MethodJobHandler(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    /**
     * 执行调用方法
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        return (ReturnT<String>) method.invoke(target, param);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + target.getClass() + "#" + method.getName() + "]";
    }
}