package com.band.usercenter.exception;

import com.band.usercenter.utils.ErrorEnum;
import lombok.Data;

/**
 * 自定义异常类
 */
@Data
public class UserCenterException extends RuntimeException {

    // 异常代码
    private final int code;
    // 异常详细介绍
    private final String desciption;


    public UserCenterException(ErrorEnum error , String desciption) {
        super(error.getMessage());
        this.code = error.getCode();
        this.desciption = desciption;
    }
}
