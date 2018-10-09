##  Spring Cloud Feign

###  申明式Web服务客户端: Fegin

 申明式： 接口声明、Annotation驱动

Web服务： HTTP的方式作为通讯协议

客户端: 用于服务调用的存根

Fegin:  原生并不是Spring Web MVC的实现, 基于 JAX_RS(Java REST规范)，实现Spring Cloud 封装了Fegin, 使其支持Spring MVC、`RestTemplate`、`HttpMessageConverter`

> `RestTemplate` 以及Spring Web MVC 可以显示地自定义 `HttpMessageConverter`实现。

假设, 有一个java接口 `PersonService`, Feign 可以将其声明成它是HTTP方式调用的。



####  注册中心(Eureka Server): 服务发现和注册

a. 应用名称: spring-cloud-eureka-server

b. 服务端口： 12345

application.properties

```properties
# eureka 服务应用名称
spring.application.name=spring-cloud-eureka-server

# eureka 服务端口号
server.port=12345

# 取消eureka自我注册 功能
eureka.client.register-with-eureka=false
# 不需要检索服务
eureka.client.fetch-registry=false

management.security.enabled=false
```



#### Feign声明接口(契约): 定义一种Java强类型接口

a. 模块名称: person-api

```java
/**
 * 服务
 */
@FeignClient(value = "person-service")  // 服务提供方应用的名称
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

```

####  Feign客户端(服务消费)端: 调用Feign声明接口

a. 应用名称: person-client

##### 依赖： person-api

##### 创建客户端Controller

```java
package com.rongshu.feign.client.controller;

import com.rongshu.feign.api.domain.Person;
import com.rongshu.feign.api.service.PersonService;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

/**
 * PersonClientController
 */
@RequestController
public class PersonClientController implements PersonService {

    private final PersonService personService;

    public PersonClientController(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean save(@RequestBody Person person) {
        return personService.save(person);
    }

    @Override
    public Collection<Person> findAll() {
        return personService.findAll();
    }
}
```

##### 创建启动类

```java
package com.rongshu.feign.client;

import com.rongshu.feign.api.service.PersonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 */
@SpringBootApplication
@EnableFeignClients(clients = PersonService.class)
public class PersonClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonClientApplication.class,args);
    }
}
```

 

##### 配置application.properties

```properties
spring.application.name=person-client

server.port=8888

# Eureka Server 服务URL
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
```

#### Feign 服务(服务提供)端: 不一定强制实现Feign声明接口

a. 应用名称: person-service

##### 依赖： person-api

##### 创建`PersonServiceProviderController`

```java
package com.rongshu.feign.service.provider.controller;

import com.rongshu.feign.api.domain.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PersonService 提供控制器
 * 可以实现PeronService接口,也可以不实现
 */
@RestController
public class PersonServiceProviderController {
    private Map<Long,Person> persons = new ConcurrentHashMap<>();

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
    public Collection<Person> findAll(){
        return persons.values();
    }
}

```

##### 创建服务端应用

```java
package com.rongshu.feign.service.provider;

import com.rongshu.feign.api.service.PersonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * {@link PersonService} 的provider
 */
@SpringBootApplication
@EnableEurekaClient
public class PersonServiceProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonServiceProviderApplication.class,args);
    }
}

```

##### 配置application.properties

```properties
# 服务端的应用名称需要和 feign客户端的服务名称对应
spring.application.name=person-service

server.port=9999

# Eureka Server 服务URL
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
```

> Feign客户端(服务消费)端、Feign 服务(服务提供)端、Feign声明接口(契约)

##### 调用顺序

postman -> feign client -> feign server 

person-client 和person-service 两个应用注册到了Eureka Server

person-client 可以感知person-service应用存在的, 并且Spring Cloud帮助解析 `PersonService` 中声明的应用名称: "person-service",因此person-client在调用`PersonService`服务时,实际就路由到person-service的URL

### 整合 Netflix Ribbon

官方参考文档:  

#### 关闭Eureka注册

##### 调整 person-client 关闭 Eureka

```properties
# 关闭Eureka
ribbon.eureka.enabled = false 
```

##### 定义ribbon的服务列表 (服务名称： person-service)

```properties
person-service.ribbon.listOfServers=http://localhost:8888
```

##### 完全取消Eureka注册

```java
//@EnableEurekaClient   // 关闭Eureka
```

#### 实现Ribbon的规则

##### 接口和内部实现

- IRule
  - 随机规则 : RandomRule
  - 最可用规则: BestAvailableRule
  - 轮询规则: RoundRobinRule
  - 重试实现: RetryRule
  - 客户端配置: ClientConfigEnabledRoundRobinRule
  - 可用性过滤规则： AvailabilityFilteringRule
  - RT权重规则： WeightedResponseTimeRule
  - 规避区域规则： ZoneAvoidanceRule

##### 实现IRule

```java
package com.rongshu.feign.client.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

import java.util.List;

public class FirstServerForeverRule extends AbstractLoadBalancerRule {
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        
    }

    @Override
    public Server choose(Object o) {
        Server server = null;

        ILoadBalancer loadBalancer = getLoadBalancer();

        List<Server> allServers = loadBalancer.getAllServers();

        return allServers.get(0);
    }
}

```

##### 暴露为spring 的bean

```java
@Bean
public FirstServerForeverRule firstServerForeverRule(){
  return new FirstServerForeverRule();
}
```

##### 激活配置

```java
package com.rongshu.feign.client;

import com.rongshu.feign.api.service.PersonService;
import com.rongshu.feign.client.ribbon.FirstServerForeverRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;

/**
 * 启动类
 */
@SpringBootApplication
//@EnableEurekaClient   // 关闭Eureka
@RibbonClient(value = "person-service",configuration =PersonClientApplication.class )
@EnableFeignClients(clients = PersonService.class)
public class PersonClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonClientApplication.class,args);
    }


    @Bean
    public FirstServerForeverRule firstServerForeverRule(){
        return new FirstServerForeverRule();
    }
}

```

##### 检验结果

通过测试可知:

```java
 @Override
    public Server choose(Object o) {
        Server server = null;

        ILoadBalancer loadBalancer = getLoadBalancer();

        // 返回三个配置的Server http://localhost:9999,http://localhost:9999,http://localhost:9999
        List<Server> allServers = loadBalancer.getAllServers();

        return allServers.get(0);
    }
```

##### 再次还原Eureka注册的结果

注册三台提供方服务器:

```

```





### 整合Netflix Hystrix

#### 调整Feign接口

```java
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

```

#### 添加Fallback实现

```java
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

```



#### 调整客户端(激活Hystrix)

```java
package com.rongshu.feign.client;

import com.rongshu.feign.api.service.PersonService;
import com.rongshu.feign.client.ribbon.FirstServerForeverRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;

/**
 * 启动类
 */
@SpringBootApplication
//@EnableEurekaClient   // 关闭Eureka
@RibbonClient(value = "person-service",configuration =PersonClientApplication.class )
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

```



### 问答部分

1. 能跟dubbo一样,消费端像调用本地接口方法一样调用服务端提供的服务么？

   答: FeignClient 类似 Dubbo, 不过需要增加以下 @Annotation，和调用本地接口类似。

2. 整合ribbon不是一定要关闭注册中心吧?

   答：Ribbon对于Eureka是不强依赖,不过也补排除

3. 生产环境上也都是Feign的么？

   答： 据我所知, 不少的公司在用, 需要Spring Cloud更多整合:

   Feign 作为客户端

   Ribbon 作为负载均衡

   Eureka作为注册中心

   Zuul 作为网关

   Sercurity 作为安全 OAuth2 认证

4. 无法连接注册中心的老服务,如何调用cloud服务

   答: 可以通过域名的配置Ribbon服务白名单。

5. Eureka 有时候监控不到宕机的服务,正确的启动方式是什么?

   答: 这个可用调整心跳检测的频率。