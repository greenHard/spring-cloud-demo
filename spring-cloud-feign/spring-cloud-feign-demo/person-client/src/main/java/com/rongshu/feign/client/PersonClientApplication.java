package com.rongshu.feign.client;

import com.rongshu.feign.api.service.PersonService;
import com.rongshu.feign.client.ribbon.FirstServerForeverRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;

/**
 * 启动类
 */
@SpringBootApplication
@EnableEurekaClient   // 关闭Eureka
// @RibbonClient(value = "person-service",configuration =PersonClientApplication.class )
@EnableFeignClients(clients = PersonService.class)
@EnableHystrix
public class PersonClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonClientApplication.class,args);
    }


    @Bean
    public FirstServerForeverRule firstServerForeverRule(){
        return new FirstServerForeverRule();
    }
}
