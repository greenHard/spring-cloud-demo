package com.rongshu.service;

import com.rongshu.user.UserService;
import com.rongshu.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Service
public class UserServiceProxy implements UserService {

    private static final String PROVIDER_SERVER_URL_PREFIX = "http://user-service-provider";

    /**
     * 通过Rest API 调用服务端提供者
     */
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean save(User user) {
        User returnValue = restTemplate.postForObject(PROVIDER_SERVER_URL_PREFIX + "/user/save", user, User.class);
        return returnValue == null;
    }

    @Override
    public Collection findAll() {
        return restTemplate.getForObject(PROVIDER_SERVER_URL_PREFIX + "/user/findAll", Collection.class);
    }
}
