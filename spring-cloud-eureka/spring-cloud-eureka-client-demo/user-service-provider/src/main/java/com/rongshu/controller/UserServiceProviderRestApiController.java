package com.rongshu.controller;


import com.rongshu.user.UserService;
import com.rongshu.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class UserServiceProviderRestApiController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/save")
    public User saveUser(@RequestBody User user) {
        if (userService.save(user)) {
            System.out.println("provider 保存对象成功, user: " +user);
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
