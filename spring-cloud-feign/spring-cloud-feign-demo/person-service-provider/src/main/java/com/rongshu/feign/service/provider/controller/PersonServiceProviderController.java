package com.rongshu.feign.service.provider.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.rongshu.feign.api.domain.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * PersonService 提供控制器
 * 可以实现PeronService接口,也可以不实现
 */
@RestController
public class PersonServiceProviderController {
    private Map<Long,Person> persons = new ConcurrentHashMap<>();

    private static final Random random = new Random();

    /**
     * 保存
     */
    @PostMapping(value = "/person/save")
    public boolean save(@RequestBody Person person){
        return persons.put(person.getId(),person) == null;
    }

    /**
     *  查询所有
     */
    @GetMapping(value = "/person/findAll")
    @HystrixCommand (fallbackMethod = "failBackFindAllPersons",
                        commandProperties = {
                        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds"
                                    ,value = "100")
                    })
    public Collection<Person> findAll() throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("findAll() persons costs: " + value +"ms");
        TimeUnit.MILLISECONDS.sleep(value);
        return persons.values();
    }

    public Collection<Person> failBackFindAllPersons(){
        System.err.println("error findAll() 超时...");
        return Collections.emptyList();
    }
}
