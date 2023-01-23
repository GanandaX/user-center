package com.band.usercenter.service.impl;

import com.band.usercenter.exception.UserCenterException;
import com.band.usercenter.mapper.UserMapper;
import com.band.usercenter.model.domain.User;
import com.band.usercenter.utils.ErrorEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.band.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.band.usercenter.constants.UserConstant.*;

/**
 * @author GrandBand
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-01-06 10:38:33
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final String SALT = "Grand_Band";
    @Resource
    UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String confirmPassword) {

        // 1. 校验
        // 非空
        boolean isEmpty = StringUtils.isAnyBlank(userAccount, userPassword, confirmPassword);
        if (isEmpty) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"注册信息项有为空");
        }

        // 账户不小于4位
        if (userAccount.length() < 4) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"注册账号小于四位");
        }

        // 密码不小于8位
        if (userPassword.length() < 8 || confirmPassword.length() < 8) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"注册密码小于四位");
        }

        // 密码和确认密码相同
        if (!userPassword.equals(confirmPassword)) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"注册密码和确认密码不相同");
        }

        // 账号不包含特殊字符
        String regEx = ".*[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*()——+|{}【】‘；：”“’。，、？\\\\]+.*";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(userAccount);
        boolean matches = matcher.matches();

        if (matches) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"注册账号包含非法字符");
        }

        // 账号名不重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        int count = userMapper.selectCount(queryWrapper);
        if (count != 0) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"注册账号名重复");
        }


        // 2.加密
        byte[] userPasswordBytes = (userPassword + SALT).getBytes(StandardCharsets.UTF_8);
        String md5Password = DigestUtils.md5DigestAsHex(userPasswordBytes);

        // 3.保存数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(md5Password);
        Integer save = userMapper.insert(user);

        if (save == 0) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"系统异常");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        // 非空
        boolean isEmpty = StringUtils.isAnyBlank(userAccount, userPassword);
        if (isEmpty) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"登录参数有为空");
        }

        // 账户不小于4位
        if (userAccount.length() < 4) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"登录账户小于4位");
        }

        // 密码不小于8位
        if (userPassword.length() < 8) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"登录密码小于8位");
        }

        // 账号不包含特殊字符
        String regEx = ".*[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*()——+|{}【】‘；：”“’。，、？\\\\]+.*";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(userAccount);
        boolean matches = matcher.matches();

        if (matches) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"登录账号包含非法字符");
        }


        // 2.比对登录信息
        byte[] userPasswordBytes = (userPassword + SALT).getBytes(StandardCharsets.UTF_8);
        String md5Password = DigestUtils.md5DigestAsHex(userPasswordBytes);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User storedUser = userMapper.selectOne(queryWrapper);

        // 无该用户
        if (storedUser == null) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"登录输入的用户不存在");
        }

        // 密码不匹配
        if (!storedUser.getUserPassword().equals(md5Password)) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR,"登录密码有误");
        }

        // 3.信息脱敏
        User safetyUser = flushData(storedUser);


        // 4.存入session
        HttpSession session = request.getSession();
        session.setAttribute(USER_LOGIN_SIGNAL, safetyUser);

        return safetyUser;
    }

    @Override
    public List<User> searchUsersByUsername(String username, HttpServletRequest request) {

        // 比对身份
        boolean result = validRole(request);
        if (!result) {
            throw new UserCenterException(ErrorEnum.NO_AUTH, "查看用户列表但非管理员权限");
        }

        // 查询用户
        if (username == null) {
            username = "";
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", username);
        List<User> users = userMapper.selectList(queryWrapper);

        List<User> userList = users.stream().map(user -> flushData(user)).collect(Collectors.toList());

        return userList;
    }

    @Override
    public boolean removeUserById(Integer userId, HttpServletRequest request) {

        // 比对身份
        boolean result = validRole(request);
        if (!result) {
            throw new UserCenterException(ErrorEnum.NO_AUTH, "删除用户但非管理员权限");
        }

        // 根据id删除
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",userId);
        int delete = userMapper.delete(queryWrapper);

        if (delete < 1) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "删除用户的id有误");
        }

        return true;
    }


    /**
     * 验证是否为管理员
     * @param request HttpRequest请求
     * @return 判断信息
     */
    boolean validRole(HttpServletRequest request) {
        // 获取session 比对身份
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(USER_LOGIN_SIGNAL);
        User validUser = (User)attribute;

        if (validUser == null) {
            throw new UserCenterException(ErrorEnum.NO_LOGIN, "验证用户权限,没登录");
        }
        Integer role = validUser.getUserRole();

        if (!role.equals(USER_ROLE_REGULATOR)) {
            throw new UserCenterException(ErrorEnum.NO_AUTH, "验证用户权限，权限不足");
        }

        return true;

    }

    /**
     * 信息脱敏
     * @param original  含敏感信息的用户对象
     * @return  去除敏感信息的用户对象
     */
    public User flushData(User original) {
        // 3.信息脱敏
        if (original == null) {
            return null;
        }

        User safetyUser = new User();
        safetyUser.setId(original.getId());
        safetyUser.setUserAccount(original.getUserAccount());
        safetyUser.setAvatarUrl(original.getAvatarUrl());
        safetyUser.setPhone(original.getPhone());
        safetyUser.setEmail(original.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setGender(0);
        safetyUser.setCreateTime(original.getCreateTime());
        safetyUser.setUserRole(original.getUserRole());
        safetyUser.setUsername(original.getUsername());

        return safetyUser;
    }

    /**
     * 获取当前用户
     * @param request
     * @return
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute(USER_LOGIN_SIGNAL);
        User user = (User) userObj;
        User cleanUser = flushData(user);

        return cleanUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public Integer userLoginOut(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute(USER_LOGIN_SIGNAL);
        return 1;
    }
}




