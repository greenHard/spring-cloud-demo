package com.rongshu.web.controller;

import com.rongshu.user.UserService;
import com.rongshu.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * 用户服务 Rest API
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/save")
    public User saveUser(@RequestBody User user) {
        if (userService.save(user)) {
            return user;
        } else {
            return null;
        }
    }

    @GetMapping("/user/findAll")
    public Collection<User> findAll(){
        return userService.findAll();
    }
}
