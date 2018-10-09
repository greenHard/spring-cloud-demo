package com.rongshu.repository;

import com.rongshu.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UserRepository {

    boolean save(User user);

    Collection<User> findAll();
}
