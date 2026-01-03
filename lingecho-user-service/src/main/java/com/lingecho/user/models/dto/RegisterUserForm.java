package com.lingecho.user.models.dto;

import lombok.Data;

@Data
public class RegisterUserForm {

    private String email;

    private String password;

    private String displayName;

    private String firstName;

    private String locale;

    private String timezone;

    private String source;

    private String captchaId;

    private String captchaCode;
}
