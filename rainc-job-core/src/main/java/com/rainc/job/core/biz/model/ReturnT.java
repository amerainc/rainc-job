package com.rainc.job.core.biz.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @Author rainc
 * @create 2020/12/7 21:54
 * 通用reslut
 */
@Data
@NoArgsConstructor
public class ReturnT<T> implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * 成功码
     */
    public static final int SUCCESS_CODE = 200;
    /**
     * 失败码
     */
    public static final int FAIL_CODE = 500;
    /**
     * 成功
     */
    public static final ReturnT<String> SUCCESS = new ReturnT<>(null);
    /**
     * 失败
     */
    public static final ReturnT<String> FAIL = new ReturnT<>(FAIL_CODE, null);
    /**
     * 返回码
     */
    private int code;
    /**
     * 返回信息
     */
    private String msg;
    /**
     * 返回内容
     */
    private T content;

    public ReturnT(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ReturnT(T content) {
        this.code = SUCCESS_CODE;
        this.content = content;
    }
}
