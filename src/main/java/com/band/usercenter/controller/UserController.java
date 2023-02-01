package com.band.usercenter.controller;

import com.band.usercenter.constants.UserConstant;
import com.band.usercenter.exception.UserCenterException;
import com.band.usercenter.model.domain.User;
import com.band.usercenter.model.request.UserLoginRequest;
import com.band.usercenter.model.request.UserRegisterRequest;
import com.band.usercenter.service.UserService;
import com.band.usercenter.utils.BaseResponse;
import com.band.usercenter.utils.ErrorEnum;
import com.band.usercenter.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户请求处理
 */
@Slf4j
@RestController()
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/"}, allowCredentials = "true")
public class UserController {

    @Resource
    UserService userService;

    /**
     * 获取当前用户
     * @param request Http请求
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> current(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);

        if (currentUser == null) {
            throw new UserCenterException(ErrorEnum.NO_LOGIN, "请先登录");
        }

        Long userId = currentUser.getId();
        User user = userService.searchUserById(userId);

        return ResponseUtils.ok(user);
    }

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return 用户id
     */
    @PostMapping("/register")
    BaseResponse<Long> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {

        if (userRegisterRequest == null) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "注册对象为空");
        }

        String userAccount = userRegisterRequest.getAccount();
        String userPassword = userRegisterRequest.getPassword();
        String confirmPassword = userRegisterRequest.getAffirmPassword();

        boolean blank = StringUtils.isAnyBlank(userAccount, userPassword, confirmPassword);
        if (blank) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "注册信息为空");
        }

        long registerId = userService.userRegister(userAccount, userPassword, confirmPassword);
        return ResponseUtils.ok(registerId);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request HttpRequest请求
     * @return 登录用户信息
     */
    @PostMapping("/login")
    BaseResponse<User> LoginUser(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {

        if (userLoginRequest == null) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "登录对象为空");
        }

        String userAccount = userLoginRequest.getAccount();
        String userPassword = userLoginRequest.getPassword();

        boolean blank = StringUtils.isAnyBlank(userAccount, userPassword);
        if (blank) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "登录账号密码为空");
        }

        User user = userService.userLogin(userAccount, userPassword, request);

        return ResponseUtils.ok(user);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/loginOut")
    BaseResponse<Integer> LoginOut(HttpServletRequest request) {
        Integer result = userService.userLoginOut(request);
        return ResponseUtils.ok(result);
    }

    /**
     * 管理员根据用户昵称查找用户
     * @param username  用户昵称
     * @param request HttpRequest请求
     * @return 查找的用户信息列表
     */
    @GetMapping("/searchUsers")
    BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        log.info("username : " + username);
        List<User> users = userService.searchUsersByUsername(username, request);
        return ResponseUtils.ok(users);
    }

    @GetMapping("/search/tags")
    BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagsNameList) {
        if (CollectionUtils.isEmpty(tagsNameList)) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "未提供搜索的标签");
        }

        List<User> users = userService.searchUsersByTags(tagsNameList);
        return ResponseUtils.ok(users);

    }

    /**
     * 根据用户id删除用户
     * @param map 存储用户id号
     * @param request   HttpServletRequest请求
     * @return
     */
    @PostMapping("/deleteUser")
    BaseResponse<Boolean> deleteUser(@RequestBody Map<String,Object> map, HttpServletRequest request) {
        Object id = map.get("id");
        Integer userId = (Integer) id;

        if (userId == null) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "删除的id为空");
        }
        boolean result = userService.removeUserById(userId, request);
        return ResponseUtils.ok(result);
    }

    /**
     * 修改用户信息
     * @param user 修改的用户
     * @param request
     * @return
     */
    @PostMapping("/modify")
    BaseResponse<Integer> modifyUser(@RequestBody User user, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_SIGNAL);
        if (loginUser == null) {
            throw new UserCenterException(ErrorEnum.NO_LOGIN, "修改数据前请先登录");
        }
        if (user == null) {
            throw new UserCenterException(ErrorEnum.PARAMETER_ERROR, "修改数据不合法");
        }

        int result = userService.amendUser(user, loginUser);
        return ResponseUtils.ok(result);
    }


}
