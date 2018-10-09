package com.rongshu.service;

import com.rongshu.user.UserService;
import com.rongshu.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {

    //@Autowired
    //private UserRepository userRepository;

    private ConcurrentHashMap<Long,User> repository = new ConcurrentHashMap<>();

    private static final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public boolean save(User user) {
        long id = idGenerator.incrementAndGet();
        user.setId(id);
        repository.putIfAbsent(id,user);
        return repository.putIfAbsent(id,user) != null;
    }

    @Override
    public Collection<User> findAll() {
        return repository.values();
    }
}
