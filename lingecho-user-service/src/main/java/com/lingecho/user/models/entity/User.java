package com.lingecho.user.models.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lingecho.common.core.BaseEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("users")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 名字
     */
    private String firstName;

    /**
     * 姓氏
     */
    private String lastName;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 是否是员工
     */
    private Boolean isStaff;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 激活状态
     */
    private String activated;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLogin;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 来源
     */
    private String source;

    /**
     * 语言
     */
    private String Locale;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 认证令牌
     */
    private String authToken;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 城市
     */
    private String city;

    /**
     * 地区
     */
    private String region;

    /**
     * 是否填写了详细信息
     */
    private Boolean hasFilledDetails;

    /**
     * 是否接收邮件通知
     */
    private Boolean emailNotifications;

    /**
     * 是否接收推送通知
     */
    private Boolean pushNotifications;

    /**
     * 是否接收系统通知
     */
    private Boolean systemNotifications;

    /**
     * 是否自动清理未读邮件
     */
    private Boolean autoCleanUnreadEmails;

    /**
     * 邮箱是否已验证
     */
    private Boolean emailVerified;

    /**
     * 手机号是否已验证
     */
    private Boolean phoneVerified;

    /**
     * 是否启用双因素认证
     */
    private Boolean twoFactorEnabled;

    /**
     * 双因素认证密钥
     */
    private String twoFactorSecret;

    /**
     * 邮箱验证令牌
     */
    private String emailVerifyToken;

    /**
     * 手机验证令牌
     */
    private String phoneVerifyToken;

    /**
     * 密码重置令牌
     */
    private String passwordResetToken;

    /**
     * 密码重置令牌过期时间
     */
    private LocalDateTime passwordResetExpires;

    /**
     * 邮箱验证令牌过期时间
     */
    private String emailVerifyExpires;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 最后修改密码时间
     */
    private LocalDateTime lastPasswordChange;

    /**
     * 角色
     */
    private String role;

    /**
     * 权限
     */
    private String Permissions;

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
