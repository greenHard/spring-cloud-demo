# Spring Cloud Config

## Spring Cloud Config

### 构建Spring Cloud Config Server

#### 实现步骤

1. 在Configuration class 标记 `@EnableConfigServer`

2. 配置文件目录（基于git）

   ```properties
   # 默认环境,跟着代码仓库(默认)
   rongshu.properties 
   # 开发环境（prifile="dev"） 
   rongshu-dev.properties
   # 测试环境 （prifile="test"）
   rongshu-test.properties
   # 预发布环境 （prifile="staging"）
   rongshu-staging.properties 
   # 生产环境 （prifile="prod"）
   rongshu-prod.properties 
   ```

3. 服务端配置版本仓库(本地)

   ```properties
   # 本地仓库git URL 的配置
   spring.cloud.config.server.git.uri = \  file:///D:/workspace/idea_workspace/spring-cloud/
   ```

完整的配置项:

```properties
spring.application.name = config-server

# 定义HTTP 服务端口
server.port = 9090

# 关闭 Actuator 全局配置
management.security.enabled=false
# 细粒度的开放 Actuator Endpoints
# sensitive关注的是敏感性
endpoints.env.sensitive=false

endpoints.health.sensitive=false

# 本地仓库git URL 的配置
spring.cloud.config.server.git.uri = \
file:///D:/workspace/idea_workspace/spring-cloud/

# Cannot pull from remote the working tree is not clean.  强制从远程拉数据
spring.cloud.config.server.git.force-pull=true
```



### 构建Spring Cloud Config Client

#### 实现步骤

1. 创建 `bootstrap.properties`

   ```properties
   # 配置服务端URL
   spring.cloud.config.uri = http://localhost:9090
   
   # 配置客户端应用名称 [application]
   spring.cloud.config.name = rongshu
   
   # profile 是激活配置
   spring.cloud.config.profile = dev
   
   # label 在git中指的是分支的名称
   spring.cloud.config.label = master
   ```

2. 配置 `application.properties`

   ```properties
   # 配置客户端配置项
   spring.application.name = config-client
   
   # 配置客户端端口
   server.port = 8888
   
   # sensitive关注的是敏感性
   endpoints.env.sensitive=false
   endpoints.refresh.sensitive=false
   endpoints.beans.sensitive=false
   
   endpoints.health.sensitive=false
   ```

3. 创建 Controller 测试

   ```java
   @Controller
   public class EchoController {
       @Value("${my.name}")
       private String myName;
   
       @GetMapping("my-name")
       public String getMyName(){
           return myName;
       }
   }
   ```

4.  动态属性Bean , 添加 `@RefreshScope`注解

   ```JAVA
   @RefreshScope
   public class EchoController {
       // ...
   }
   ```

5.  JMX操作（jconsle）

6.  自动刷新实现配置

   ```java
   @SpringBootApplication
   public class SpringCloudConfigClientDemoApplication {
   
   	private final ContextRefresher contextRefresher;
   
   	private final Environment environment;
   
   	@Autowired
       public SpringCloudConfigClientDemoApplication(ContextRefresher contextRefresher, Environment environment) {
           this.contextRefresher = contextRefresher;
           this.environment = environment;
       }
   
       public static void main(String[] args) {
   		SpringApplication.run(SpringCloudConfigClientDemoApplication.class, args);
   	}
   
   	@Scheduled(fixedRate = 5*1000, initialDelay = 3*1000 )
   	public void autoRefresh(){
           Set<String> updatePropertyNames = contextRefresher.refresh();
   
           updatePropertyNames.forEach((propertyName) ->{
               System.err.printf("[Thread:%s] 当前配置已经更新, 具体 key: %s , value: %s \n",
                       Thread.currentThread().getName(),
                       propertyName,
                       environment.getProperty(propertyName));
           } );
       }
   }
   ```


## 健康检查health

### 意义

比如应用可以任意地输出业务健康、系统健康等指标

`HATEOAS`  发现入口   --> `REST`

断点URI:  `/health`

实现类:  `HealthEndpoint`

健康指示器:  `HealthIndicator`

`HealthEndpoint` :  `HealthIndicator`  一对多

###  自定义实现HealthIndicator   

1. 自定义实现

   ```java
   package com.rongshu.springcloudconfigclientdemo.health;
   
   import org.springframework.boot.actuate.health.AbstractHealthIndicator;
   import org.springframework.boot.actuate.health.Health;
   
   public class MyHealthIndicator extends AbstractHealthIndicator {
   
       @Override
       protected void doHealthCheck(Health.Builder builder) throws Exception {
           builder.up().withDetail("My Health Indicator","day day up");
       }
   }
   
   ```

2. 暴露Bean

   ```java
   @Bean
   public MyHealthIndicator myHealthIndicator(){
       return new MyHealthIndicator();
   }
   ```

3. 关闭安全属性

   ```properties
   # 关闭健康敏感度
   endpoints.health.sensitive=false
   ```

### 问答

1.  小马哥,你们服务是基于啥原因采用的springboot的,这么多稳定性的问题?

       答:  `Spring Boot` 业界是比较稳定的微服务中间件 ，不过它使用易学难精！

2.  小马为什么要把配置放到git上， 为什么不放到具体服务的程序里边, git在扮演什么样的角色？是不是和`zookeeper`一样? 

       答: git 文件存储方式, 分布式的管理系统, `Spring Cloud` 官方实现基于Git，它达到的理念和ZK一样

3. 如果发生了配置变更, 怎么处理?

   答: 如果发生了配置变更, 我的解决方案是重启 `Spring Context`  。`@RefreshScope` 最佳实践用于配置Bean, 比如：开关、阈值、文案场景比较多