package com.band.usercenter.service;

import com.band.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author GrandBand
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-01-06 10:38:33
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount       用户账号
     * @param userPassword      用户密码
     * @param confirmPassword   确认密码
     * @return  用户id
     */
    long userRegister(String userAccount, String userPassword, String confirmPassword);

    /**
     * 用户登录
     * @param userAccount  登录账号
     * @param userPassword 登录密码
     * @param request HttpRequest请求
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 根据用户名查询
     *
     * @param username 查询信息
     * @param request HttpRequest请求
     * @return 符合条件的用户信息
     */
    List<User> searchUsersByUsername(String username, HttpServletRequest request);

    /**
     * 根据id删除用户
     * @param userId    用户id
     * @return
     */
    boolean removeUserById(Integer userId, HttpServletRequest request);

    /**
     * 数据脱敏
     * @param original 原数据
     * @return
     */
    User flushData(User original);

    /**
     * 获取当前用户
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 用户注销
     * @param request
     * @return
     */
    Integer userLoginOut(HttpServletRequest request);

}
