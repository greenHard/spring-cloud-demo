package com.rongshu.user;

import com.rongshu.user.domain.User;

import java.util.Collection;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 保存用户对象,成功返回true,失败返回false
     */
    boolean save(User user);


    /**
     * 查询所有的用户对象,不会返回null
     */
    Collection findAll();
}
