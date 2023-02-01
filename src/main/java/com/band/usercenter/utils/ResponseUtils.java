package com.band.usercenter.utils;

/*
返回工具类
 */
public class ResponseUtils {

    // 响应正常
    public static <T>BaseResponse<T> ok(T data) {
        return new BaseResponse<>(1,data,"ok","");
    }

    // 异常响应
    public static <T>BaseResponse<T> error(Integer code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }
}
