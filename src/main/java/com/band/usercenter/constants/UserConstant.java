package com.band.usercenter.constants;

public interface UserConstant {
    // 登录用户身份   1-管理员   0-普通用户
    public static final Integer USER_ROLE_REGULATOR = 1;
    public static final Integer USER_ROLE_USER = 0;

    // 登录成功后存入session名
    public static final String USER_LOGIN_SIGNAL = "userLoginSignal";
}
