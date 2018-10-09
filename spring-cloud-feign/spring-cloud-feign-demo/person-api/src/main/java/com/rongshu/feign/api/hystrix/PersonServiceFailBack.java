package com.rongshu.feign.api.hystrix;

import com.rongshu.feign.api.domain.Person;
import com.rongshu.feign.api.service.PersonService;

import java.util.Collection;
import java.util.Collections;

public class PersonServiceFailBack  implements PersonService{
    @Override
    public boolean save(Person person) {
        return false;
    }

    @Override
    public Collection<Person> findAll() {
        return Collections.emptyList();
    }
}
