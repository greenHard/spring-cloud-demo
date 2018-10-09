package com.rongshu.feign.client.controller;

import com.rongshu.feign.api.domain.Person;
import com.rongshu.feign.api.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * PersonClientController
 */
@RestController
public class PersonClientController implements PersonService {

    private final PersonService personService;

    @Autowired
    public PersonClientController(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean save(@RequestBody  Person person) {
        return personService.save(person);
    }

    @Override
    public Collection<Person> findAll() {
        return personService.findAll();
    }
}
