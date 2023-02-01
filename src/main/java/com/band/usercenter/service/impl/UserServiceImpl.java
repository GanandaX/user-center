package com.band.usercenter.service.impl;

import com.band.usercenter.exception.UserCenterException;
import com.band.usercenter.mapper.UserMapper;
import com.band.usercenter.model.domain.User;
import com.band.usercenter.utils.ErrorEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.band.usercenter.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.band.usercenter.constants.UserConstant.*;

/**
 * @author GrandBand
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
        boolean matches = validSpecialCharacter(userAccount);

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
        int save = userMapper.insert(user);

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
        boolean matches = validSpecialCharacter(userAccount);

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
        User userValid = (User) request.getSession().getAttribute(USER_LOGIN_SIGNAL);
        boolean result = validRole(userValid);
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

        return users.stream().map(this::flushData).collect(Collectors.toList());
    }

    @Override
    public boolean removeUserById(Integer userId, HttpServletRequest request) {

        // 比对身份
        User user = (User) request.getSession().getAttribute(USER_LOGIN_SIGNAL);
        boolean result = validRole(user);
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


    @Override
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute(USER_LOGIN_SIGNAL);
        if (userObj == null) {
            throw new UserCenterException(ErrorEnum.NO_LOGIN, "登录后才能获取当前用户");
        }
        User user = (User) userObj;
        return flushData(user);
    }

    @Override
    public Integer userLoginOut(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute(USER_LOGIN_SIGNAL);
        return 1;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagList) {

        if (CollectionUtils.isEmpty(tagList)) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "标签为空");
        }

        // 内存查询
        QueryWrapper<User> objectQueryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(objectQueryWrapper);

        // 处理Json字符串
        Gson gson = new Gson();

        return users.stream().filter(user -> {
            String tags = user.getTags();

            if (StringUtils.isBlank(tags)) {
                return false;
            }
            Set<String> tagNameSets = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());

            for (String tag : tagList) {
                if (!tagNameSets.contains(tag)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public int amendUser(User user, User loginUser) {
        // 1. 判空
        // 2. 检查权限(管理员和普通对应用户可修改)
        Long id = user.getId();
        if (user.isEmpty() || id == null || id <= 0) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "登录数据不合法");
        }

        // 判断修改权限(仅管理员和对应用户可修改)
        if (!id.equals(loginUser.getId()) && !validRole(loginUser)) {
            throw new UserCenterException(ErrorEnum.NO_AUTH, "修改用户，无修改权限");
        }

        // 判断要修改的用户是否存在
        User storedUser = userMapper.selectById(id);
        if (storedUser == null) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "修改用户信息有误");
        }

        // 修改用户
        return userMapper.updateById(user);
    }

    @Override
    public User searchUserById(Long userId) {

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "查询参数不合法");
        }
        return user;
    }


    /**
     * 验证是否为管理员
     * @param validUser 判断的用户信息
     * @return 判断信息
     */
    boolean validRole(User validUser) {
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
        safetyUser.setUserProfile(original.getUserProfile());

        return safetyUser;
    }

    /**
     * 判断是否包含特殊字符
     * @param validString 判断的字符串
     * @return true 包含特殊字符  false 不包含特殊字符
     */
    private boolean validSpecialCharacter(String validString) {
        // 账号不包含特殊字符
        String regEx = ".*[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*()——+|{}【】‘；：”“’。，、？\\\\]+.*";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(validString);

        return matcher.matches();
    }
}




