package com.band.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 登录账号
     */
    private String userAccount;

    /**
     * 用户登录密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户描述
     */
    private String userProfile;

    /**
     * 用户图像地址
     */
    private String avatarUrl;

    /**
     * 电话
     */
    private String phone;

    /**
     * 用户创建的标签，JSON串
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 用户状态 0-正常
     */
    private Integer userStatus;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 用户角色 0-普通用户  1-管理员
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 0-正常 1-刪除（逻辑删除）
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 判断是否为空(用户账号、性别、电话、邮件为判断标准)
     * @return
     */
    public boolean isEmpty() {
        if (username == null && gender == null && phone == null && email == null) {
            return true;
        }
        return false;
    }
}