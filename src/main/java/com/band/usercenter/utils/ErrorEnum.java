package com.band.usercenter.utils;

/**
 * 标识错误类型
 */
public enum ErrorEnum {
    SUCCESS(0,"sucess"),
    PARAMETER_ERROR(40000,"参数错误"),
    NO_LOGIN(40100,"未登录"),
    NO_AUTH( 40101,"无权限"),
    SYSTEM_ERROR( 50000,"内部错误");

    final int code;
    final String message;

    ErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
