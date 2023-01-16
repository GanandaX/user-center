package com.band.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册信息
 * @author GrandBand
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 3867983565782819102L;

    /**
     * 用户登录账号
     */
    private String account;

    /**
     * 用户登录密码
     */
    private String password;

    /**
     * 用户确认密码
     */
    private String affirmPassword;

}
