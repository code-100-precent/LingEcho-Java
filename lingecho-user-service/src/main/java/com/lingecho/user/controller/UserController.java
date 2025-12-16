package com.lingecho.user.controller;

import com.lingecho.common.core.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping
    public Result<List<UserDTO>> listUsers() {
        // TODO: 实现用户列表查询
        List<UserDTO> users = new ArrayList<>();
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        // TODO: 实现用户详情查询
        UserDTO user = new UserDTO();
        user.setId(id);
        return Result.success(user);
    }

    @PostMapping
    public Result<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        // TODO: 实现用户创建
        UserDTO user = new UserDTO();
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        // TODO: 实现用户更新
        UserDTO user = new UserDTO();
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // TODO: 实现用户删除
        return Result.success();
    }

    @Data
    static class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String avatar;
    }

    @Data
    static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
    }

    @Data
    static class UpdateUserRequest {
        private String username;
        private String email;
        private String avatar;
    }
}

