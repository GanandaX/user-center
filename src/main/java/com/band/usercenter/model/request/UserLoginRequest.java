package com.band.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录信息
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -4397974552248503621L;

    /**
     * 用户登录账号
     */
    private String account;

    /**
     * 用户登录密码
     */
    private String password;
}
