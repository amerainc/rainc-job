package com.rainc.job.controller.advice;

import com.rainc.job.exception.RaincJobException;
import com.rainc.job.core.biz.model.ReturnT;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author rainc
 * @create 2020/12/24 18:02
 */
@RestControllerAdvice
@Log4j2
public class WebExecptionAdvice {

    @ExceptionHandler(RaincJobException.class)
    public ReturnT<String> raincJobExceptionHandler(RaincJobException e) {
        //如果是权限错误
        if (e.getCode() == RaincJobException.PERMISSION_CODE) {
            return new ReturnT<>(RaincJobException.PERMISSION_CODE, "没有访问权限");
        }
        //如果是未登录
        if (e.getCode() == RaincJobException.NOT_LOGIN_CODE) {
            return new ReturnT<>(RaincJobException.NOT_LOGIN_CODE, "没有登录或登录已失效");
        }
        //普通错误
        log.warn("[webController error] {}", e.getMessage());
        return new ReturnT<>(RaincJobException.COMMON_CODE, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ReturnT<String> paramValidExceptionHandler(Exception e) {
        String msg = null;
        if (e instanceof MethodArgumentNotValidException) {
            //如果是参数验证失败
            BindingResult bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
            // getFieldError获取的是第一个不合法的参数(P.S.如果有多个参数不合法的话)
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                msg = fieldError.getDefaultMessage();
            }
        } else if (e instanceof HttpMessageNotReadableException) {
            //    如果是数据解析失败
            msg = "数据格式不正确，请建议数据格式:" + e.getMessage();
        }
        return new ReturnT<>(RaincJobException.COMMON_CODE, msg);
    }
}
