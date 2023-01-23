package com.band.usercenter.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.band.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * 用戶服务测试
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    UserService userService;

    @Test
    public void md5Test() {
        String pswd = "123";
        String md5DigestAsHex = DigestUtils.md5DigestAsHex(pswd.getBytes(StandardCharsets.UTF_8));
        System.out.println(md5DigestAsHex);
    }

    /**
     * 保存用户
     */
    @Test
    public void testSave() {
        User user = new User();
        user.setUserAccount("band");
        user.setUserPassword("45673221");
        user.setAvatarUrl("https://profile.csdnimg.cn/9/1/4/2_weixin_45946937");
        user.setPhone("222");
        user.setEmail("7654321");
        user.setGender(0);

        boolean save = userService.save(user);
        Assertions.assertTrue(save);
        System.out.println(user.getId());


    }

    @Test
    void userRegister() {
        String userAccount;
        String userPassword;
        String confirmPassword;

        // 1.非空
        userAccount = "";
        userPassword = "12345678";
         confirmPassword = "12345678";
        long result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

        // 2.账户长度不小于4
        userAccount = "abc";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

        // 3.账户不重复
        userAccount = "grand";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

        // 4.账户不包含特殊字符
        userAccount = "**12313";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

        // 5.密码与校验密码不相同
        userAccount = "563453";
        userPassword = "673421512334512";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

        // 6.密码不小于8
        userPassword = "12345";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

        userPassword = "12345678";
        confirmPassword = "12345";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

        confirmPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);

    }

    @Test
    void searchUsersByTags() {

//        List<String> tagList = Arrays.asList("java","python");
        List<String> tagList = Arrays.asList("c++");
        List<User> users = userService.searchUsersByTags(tagList);
        users.stream().forEach(user -> System.out.println(user));
    }
}