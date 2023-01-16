package com.band.usercenter.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 */
@Data
public class BaseResponse <T> implements Serializable {
    // 响应状态码
    private Integer code;

    // 响应数据
    private T data;

    // 响应信息
    private String message;

    // 响应具体描述
    private String description;

    public BaseResponse(Integer code, T data, String message, String description) {

        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }
}
