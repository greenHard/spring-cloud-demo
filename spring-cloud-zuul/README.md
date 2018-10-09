## Spring Cloud Zuul

### Zuul基本使用

@EnableEurekaClient

@EnableDiscoveryClient



Nginx + Lua

Lua: 控制规则(A/B Test)



Spring Cloud 学习技巧：

善于定位应用: Feign、Config Server、Eureka、Zull、Ribbon

#### 增加@EnableZuulProxy

```java
package com.rongshu.springcloudzuuldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class SpringCloudZuulDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudZuulDemoApplication.class, args);
	}
}
```



#### 配置路由规则

```properties
zuul.routes.${app-name}=/${app-url-prefix}/**
```

#### 关闭Eureka



### 整合Ribbon

#### 启动应用

`spring-cloud-eureka-server`、`person-service`

#### 调用链路

zuul ->person service

#### 配置方式

```properties
# Zuul基本配置方式
# zuul.routes.${app-name}=/${app-url-prefix}/**

# Zuul服务端口
server.port=7070

## Zuul配置person-service 服务调用
zuul.routes.person-service = /person-service/**

## Ribbon 取消Eureka 的整合
ribbon.eureka.enabled = false

# 配置负载均衡服务器列表
person-service.ribbon.listOfServers=http://localhost:9999
```

>  注意: http://localhost:7070/person-service/person/findAll
>
> person-service 的app-url-prefix: /person-service/
>
> /person/findAll 是 person-service 具体的URI

### 整合Eureka

#### 引入 spring-cloud-starter-eureka依赖

```xml
<!-- 引入Eureka客户端的依赖-->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```

#### 激活服务注册、发现客户端

```java
package com.rongshu.springcloudzuuldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class SpringCloudZuulDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudZuulDemoApplication.class, args);
	}
}
```

#### 配置服务注册、发现客户端

```properties
# Zuul基本配置方式
# zuul.routes.${app-name}=/${app-url-prefix}/**

# Zuul服务端口
server.port=7070

## Zuul配置person-service 服务调用
zuul.routes.person-service = /person-service/**

## Ribbon 取消Eureka 的整合
#ribbon.eureka.enabled = false
#
## 配置负载均衡服务器列表
#person-service.ribbon.listOfServers=http://localhost:9999

## 整合Eureka
## 目标应用的serviceId = person-service


# Eureka Server 服务URL
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
```

#### 设置应用的名称

```properties
# 设置应用的名称
spring.application.name=spring-cloud-zuul

# Zuul基本配置方式
# zuul.routes.${app-name}=/${app-url-prefix}/**

# Zuul服务端口
server.port=7070

## Zuul配置person-service 服务调用
zuul.routes.person-service = /person-service/**

## Ribbon 取消Eureka 的整合
#ribbon.eureka.enabled = false
#
## 配置负载均衡服务器列表
#person-service.ribbon.listOfServers=http://localhost:9999

## 整合Eureka
## 目标应用的serviceId = person-service


# Eureka Server 服务URL
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
```

### 整合Hystrix

#### 服务端提供端: person-service

##### 激活Hystrix

```java
package com.rongshu.feign.service.provider;

import com.rongshu.feign.api.service.PersonService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

/**
 * {@link PersonService} 的provider
 */
@SpringBootApplication
@EnableEurekaClient
@EnableHystrix
public class PersonServiceProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonServiceProviderApplication.class,args);
    }
}
```

##### 配置Hystrix规则

```java
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
```

### 整合Feign

#### 服务消费端: person-client

##### 调用链路

spring-cloud-zuul -> person -client->person-service

##### person-client 注册到EurekaServer

> 注意:
>
> spring-cloud-zuul 端口: 7070
>
> person-client 端口: 8888
>
> person-service 端口: 9999
>
> Eureka Server 端口: 12345

启动 `person-client`

```properties
spring.application.name=person-client

server.port=8888

# Eureka Server 服务URL
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka

# 关闭eureka
#ribbon.eureka.enable =false
#
## 配置负载均衡服务器列表
#person-service.ribbon.listOfServers=http://localhost:9999,http://localhost:9999,http://localhost:9999
```

#### 网关应用： spring-cloud-zuul

##### 增加路由到person-client

```properties
## Zuul配置person-client 服务调用
zuul.routes.person-client = /person-client/**
```

##### 测试链路

http://localhost:7070/person-client/person/findAll

spring-cloud-zuul -> person-client -> person-service

等价的Ribbon(不走注册中心)

```properties
spring.application.name=person-client

server.port=8888

# Eureka Server 服务URL
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka

management.security.enabled=false

# 关闭eureka
ribbon.eureka.enable =false

## 配置负载均衡服务器列表
person-service.ribbon.listOfServers=http://localhost:9999,http://localhost:9999,http://localhost:9999
```



### 整合Config Server

前面的例子展示 Zuul、Hystrix、Eureka 以及Ribbon能力,可是配置相对是固定的,真实线上环境需要一个动态路由, 即需要动态配置。

#### 配置服务器: spring-cloud-config-server

>  端口信息:
>
> spring-cloud-zuul 端口: 7070
>
> person-client 端口: 8888
>
> person-service 端口: 9999
>
> Eureka Server 端口: 12345
>
> Config Server 端口: 10000

##### 调整配置项

```properties
# 配置应用名称
spring.application.name = spring-cloud-config-server

# 定义HTTP 服务端口
server.port = 10000

# 关闭 Actuator 全局配置
management.security.enabled=false

# 本地仓库git URL 的配置
spring.cloud.config.server.git.uri = \
  file:///${user.dir}/src/main/resources/configs

## Cannot pull from remote the working tree is not clean.  强制从远程拉数据
#spring.cloud.config.server.git.force-pull=true
```

##### 为 spring-cloud-zuul增加配置文件

三个profile配置文件:

- zuul.properties
- zuul-test.properties
- zuul-pro.properties

zuul.properties

```properties
# spring cloud zuul 默认配置项 (profile 为空)

# Zuul基本配置方式
# zuul.routes.${app-name}=/${app-url-prefix}/**

## Zuul配置person-service 服务调用
zuul.routes.person-service = /person-service/**
```

zuul-test.properties

```properties
# spring cloud zuul 默认配置项 (profile 为空)

# Zuul基本配置方式
# zuul.routes.${app-name}=/${app-url-prefix}/**

## Zuul配置person-client 服务调用
zuul.routes.person-client = /person-client/**
```

zuul-pro.properties

```properties
# spring cloud zuul 默认配置项 (profile 为空)

# Zuul基本配置方式
# zuul.routes.${app-name}=/${app-url-prefix}/**

## Zuul配置person-service 服务调用
zuul.routes.person-service = /person-service/**

## Zuul配置person-client 服务调用
zuul.routes.person-client = /person-client/**
```

##### 初始化 file:///${user.dir}/src/main/resources/configs为git根目录

1. 初始化

   ```
   Initialized empty Git repository in D:/workspace/idea_workspace/lesson6/spring-cloud-server-demo/src/main/resources/configs/.git/
   ```

2. 增加上述三个配置文件到git仓库

   ```
   git add *.properties
   ```

3. 提交到本地git仓库

   ```
   $ git commit -m 'comiit'
   [master (root-commit) a4bd62f] comiit
    3 files changed, 24 insertions(+)
    create mode 100644 zuul-pro.properties
    create mode 100644 zuul-test.properties
    create mode 100644 zuul.properties
   ```

   > 以上操作为了让Spring Cloud Git 配置服务器实现识别Git仓库,否则添加以上文件也没有效果。

   ##### 注册到Eureka服务器

   ###### 增加Eureka客户端依赖

   ```xml
   <!-- 引入Eureka客户端的依赖-->
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-eureka</artifactId>
   </dependency>
   ```

   ###### 激活服务注册、发现客户端

   ```java
   package com.rongshu.springcloudserverdemo;

   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
   import org.springframework.cloud.config.server.EnableConfigServer;

   @SpringBootApplication
   @EnableConfigServer
   @EnableDiscoveryClient
   public class SpringCloudServerDemoApplication {

   	public static void main(String[] args) {
   		SpringApplication.run(SpringCloudServerDemoApplication.class, args);
   	}
   }
   ```

   ###### 调整配置项:

   ```properties
   # Eureka Server 服务URL
   eureka.client.service-url.defaultZone=\
     http://localhost:12345/eureka
   ```

   ###### 测试配置

   http://localhost:10000/zuul/deafult

   http://localhost:10000/zuul/test

   http://localhost:10000/zuul/pro



#### 配置网关服务: spring-cloud-zuul

> 端口信息:
>
> spring-cloud-zuul 端口: 7070
>
> person-client 端口: 8888
>
> person-service 端口: 9999
>
> Eureka Server 端口: 12345
>
> Config Server 端口: 10000

##### 增加spring-cloud-starter-config依赖

> 将之前 zuul.routes.person-service  、zuul.routes.person-client 注释掉

```xml
<!-- 引入配置客户端config依赖-->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

##### 创建bootstrap.properties

```properties
# bootstrap 上下文配置
# 配置客户端应用名称 [application]
spring.cloud.config.name = zuul

# profile 是激活配置
spring.cloud.config.profile = pro

# label 在git中指的是分支的名称
spring.cloud.config.label = master

# 采用Discovery client 的连接方式
# 激活discovery 连接配置项的方式
spring.cloud.config.discovery.enabled=true

# 配置config server 应用的名称
spring.cloud.config.discovery.service-id=spring-cloud-config-server
        
# Eureka Server 服务URL
# application.properties 会继承bootstrap.properties属性
eureka.client.service-url.defaultZone=\
  http://localhost:12345/eureka
```

##### 调用链路

http://localhost:7070/person-client/person/findAll

spring-cloud-zuul -> person-client -> person-service

http://localhost:7070/person-service/person/findAll

spring-cloud-zuul ->person-service



anc.acme.com -> abc

def.acme.com -> def

需要自定义实现 `ZuulFilter`



### 问答部分

1. `RequestContext` 已经存在`ThreadLocal`中了,为什么还要使用`ConurrentHashMap`?

   `ThreadLocal` 只能管当前线程,不能管理子线程,子线程需要使用 `InheritableThreadLocal` 。`ConcurrentHashMap`实现一下, 如果上下文处于多线程线程的环境，比如传递到子线程。

   比如: T1 在管理`RequestContext`, 但是T1又创建了多个线程(t1,t2)。这个时候,把上下文传递到了子线程t1和t2.

   Java的进程所对应的线程main线程(group: main),main线程是所有子线程的父线程, main线程T1, T1又可以创建t1和t2

   ​

2. `ZuulServlet` 已经管理了`RequestContext`的生命周期了, 为什么`ContextLifecycleFilter`还要在做一遍?

   答:  `ZuulServlet` 最终也会清理掉 `RequestContext`

   ```java
   } finally {
     RequestContext.getCurrentContext().unset();
   }
   ```

   为什么 `ContextLifecycleFilter`也这么干?

   ```java
   } finally {
     RequestContext.getCurrentContext().unset();
   }
   ```

   不要忽略了`ZuulServletFilter`,也有这个处理:

   ```java
   } finally {
     RequestContext.getCurrentContext().unset();
   }
   ```

   `RequestContext`是任何 Serv;et或者Filter都能处理, 那么为了防止不正确的关闭, 那么`ContextLifecycleFilter`相当于兜底操作,就是防止`ThreadLocal`没有被remove掉。

   ​

   `ThreadLocal`对应了一个Thread,那么是不是意味着Thread处理完了,那么`ThreadLocal`也随之GC呢?

   所有Servlet均采用线程池,因此, 不清空的话,可能会出现意想不到的情况,除非,每次都异常！（这种情况也要依赖于线程池的实现）。