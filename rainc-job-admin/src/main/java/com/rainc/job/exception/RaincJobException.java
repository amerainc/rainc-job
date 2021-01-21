package com.rainc.job.exception;

import lombok.Getter;

/**
 * @Author rainc
 * @create 2020/12/24 18:43
 */
@Getter
public class RaincJobException extends RuntimeException {
    /**
     * 普通错误
     */
    public static final int COMMON_CODE = 400;
    /**
     * 权限错误
     */
    public static final int PERMISSION_CODE = 401;
    /**
     * 未登录错误
     */
    public static final int NOT_LOGIN_CODE = 402;

    public static final RaincJobException PERMISSION = new RaincJobException(PERMISSION_CODE);
    public static final RaincJobException NOT_LOGIN = new RaincJobException(NOT_LOGIN_CODE);

    private final int code;

    public RaincJobException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RaincJobException(String message) {
        super(message);
        this.code = COMMON_CODE;
    }

    public RaincJobException(String message, Throwable cause) {
        super(message, cause);
        this.code = COMMON_CODE;
    }

    public RaincJobException(int code) {
        this.code = code;
    }
}
