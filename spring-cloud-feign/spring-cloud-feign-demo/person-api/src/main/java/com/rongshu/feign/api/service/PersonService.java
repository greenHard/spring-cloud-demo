package com.rongshu.feign.api.service;

import com.rongshu.feign.api.domain.Person;
import com.rongshu.feign.api.hystrix.PersonServiceFailBack;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;

/**
 * 服务
 */
@FeignClient(value = "person-service",fallback = PersonServiceFailBack.class)  // 服务提供方应用的名称
public interface PersonService {

    /**
     * 保存
     */

    @PostMapping(value = "/person/save")
    boolean save(@RequestBody Person person);

    /**
     *  查询所有
     */
    @GetMapping(value = "/person/findAll")
    Collection<Person> findAll();
}
