package com.lingecho.user.dto;

import lombok.Data;

/**
 * 更新用户请求
 */
@Data
public class UpdateUserRequest {
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String displayName;
    private String locale;
    private String timezone;
    private Integer gender;
    private String avatar;
    private String extra;
}

