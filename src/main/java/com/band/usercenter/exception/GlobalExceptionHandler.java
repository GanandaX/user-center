package com.band.usercenter.exception;

import com.band.usercenter.utils.BaseResponse;
import com.band.usercenter.utils.ErrorEnum;
import com.band.usercenter.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自定义全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * UserCenter错误处理封装
     * @param centerException
     * @return
     */
    @ExceptionHandler(value = {UserCenterException.class})
    public BaseResponse userCenterException(UserCenterException centerException) {
        log.error("*********** centerException: " + centerException.getMessage(), centerException);
        return ResponseUtils.error(centerException.getCode(), centerException.getMessage(), centerException.getDesciption());
    }

    /**
     * 其他报错处理封装
     * @param runtimeException
     * @return
     */
    @ExceptionHandler(value = {RuntimeException.class})
    public BaseResponse runtimeException(RuntimeException runtimeException) {
        log.error("runtimeException: " + runtimeException.getMessage(), runtimeException);
        return ResponseUtils.error(ErrorEnum.SYSTEM_ERROR.getCode(), runtimeException.getMessage(), "");
    }
}
