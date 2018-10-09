#  Spring Cloud Netflix Eureka

##  Spring Cloud Netflix Eurek

## 传统的服务治理

### 通讯协议

XML-RPC ->XML方法描述、方法参数 -> WSDL

WebServices -> SOAP(HTTP、SMTP) -> 文本协议（头部分、体部分）

REST -> JSON/XML(Schema :  类型、结构) -> 文本

W3C Schema: xsd:string 原子类型，自定义自由组合原子类型

java POJO: int、String

Dubbo: Hession、JSON、Java Serialization(二进制) ，跨语言不变，一般通过Client(Java、C++)

> 二进制的性能是非常好(字节流,免去字符流(字符编码))，免去了字符解释, 对机器友善, 对人不友善

> 序列化: 把编程语言数据结构转换成字节流、反序列化: 把字节流转换成言数据结构（原生类型的组合）



## 高可用架构

URI: 统一资源定位符

URI   用于网络资源定位的描述 

网络是通讯方式

资源是需要消费媒介

定位是路由



Proxy:  一般性代理、路由

Broker:  包括路由, 并且算法 ,老的称谓（MOM）

Message Broker: 消息路由、消息管理（消息是否可达）

### 可用性比率计算

可用性比率: 99.99% 通过时间来计算 (一年或者一月)

比如: 一年99.99%

可用时间： 365 * 24 * 3600 *99.99%

不可用时间:  365 * 24 * 3600 *0.01% = 3153.6 秒 < 一个小时

不可用时间: 1个小时 推算一年 1/24/365 = 0.01%



单台机器不可用性比率: 1%

两台机器不可用性比率: 1% * 1% 

### 可靠性

微服务里面的问题： 

一次调用:

A -> B -> C

99% -> 99% ->99% = 97%

A -> B -> C ->D

99% -> 99% ->99% ->99%= 96%

结论： 增加机器可以提高可用性, 增加服务调用会降低可靠性,同时降低可用性

## Eureka客户端



## Eureka服务器

Eureka 服务器一般不需要自我注册,也不需要注册其他服务器



Euraka 自我注册的问题,服务器本身没有启动



> Fast Fail : 快速失败
>
> Fault-Tolerance: 容错



通常经验,Eureka服务器不需要开启自动注册,也不需要开启检索服务

```properties
# 取消eureka自我注册 功能
eureka.client.register-with-eureka=false
# 不需要检索服务
eureka.client.fetch-registry=false
```

但是这两个设置并不是影响作为服务器的使用,不过建议关闭,为了减少不必要的常堆栈,减少错误的干扰（比如系统异常和业务异常）



## Eureka 高可用架构集群

### 高可用注册中心集群

只需增加Eureka 服务器注册到URL

```properties
# eureka server 服务Url
eureka.client.service-url.defaultZone=http://localhost:9090/eureka,http://localhost:9091/eureka
```

如果Eureka 的客户端配置了多个Eureka注册服务器, 那么默认情况下只有一台可用的服务器,存在注册信息

如果第一台可用的Eureka 服务器宕掉了,  会选择下一台可用的Eureka 服务器

建议使用Ngnix的方式去转到Eureka 服务器集群



### 配置源码 （EurekaClientConfigBean）

配置项`eureka.client.service-url` 实际映射的字段为 `serviceUrl`，它是Map类型, Key为自定义, 默认值‘defaultZone’,value值需要配置Eureka注册服务器URL:

```java
private Map<String, String> serviceUrl = new HashMap<>();

	{
		this.serviceUrl.put(DEFAULT_ZONE, DEFAULT_URL);
	}
```

value 可以是多值字段,通过“,”分割:

```java
if (serviceUrls == null || serviceUrls.isEmpty()) {
			serviceUrls = this.serviceUrl.get(DEFAULT_ZONE);
		}
		if (!StringUtils.isEmpty(serviceUrls)) {
			final String[] serviceUrlsSplit = StringUtils.commaDelimitedListToStringArray(serviceUrls);
			List<String> eurekaServiceUrls = new ArrayList<>(serviceUrlsSplit.length);
			for (String eurekaServiceUrl : serviceUrlsSplit) {
				if (!endsWithSlash(eurekaServiceUrl)) {
					eurekaServiceUrl += "/";
				}
				eurekaServiceUrls.add(eurekaServiceUrl);
			}
			return eurekaServiceUrls;
		}
```



## 获取注册时间的时间间隔

Eureka 客户端获取 Eureka 服务器注册信息，这个方便服务调用。



Eureka 客户端: `EurekaClient` 关联应用集合： `Applications`

单个应用信息：`Application` ,关联多个应用实例

单个应用实例: `InstanceInfo`

当Eureka 客户端要调用某个服务时, 比如 `user-service-consumer` 调`user-service-provider`, `user-service-provider`实际对应对象是 `Application`, 关联了许多应用实例(`InstanceInfo`) 。

如果应用`user-service-provider`实例发生变化是,那么`user-service-consumer`是需要感知的。比如:  `user-service-provider`机器从10->5 ,作为调用方`user-service-consumer`需要感知这个变化情况。可是这个变化过程可能存在一定的延迟, 可以通过调整注册信息时间间隔来减少错误。

#### 具体配置项

```properties
# 获取注册信息的时间间隔，默认30秒
eureka.client.registry-fetch-interval-seconds=5
```

### 实例信息复制时间间隔

具体就是客户端信息上报到Eureka服务器时间。当Eureka客户端应用上报的频率越高， 那么Eureka服务器的数据一致性就越高。

#### 具体配置项

```properties
# 实例信息复制时间间隔 默认40秒
eureka.client.initial-instance-info-replication-interval-seconds=5
```

> Eureka 的应用服务器信息获取的方式: 拉模式
>
> Eureka 的应用信息上报的方式: 推模式



 ### 实例ID

从 Eureka Server Dashboard 里面可以看到某个应用中的实例信息,比如:

```
UP (1) - localhost:user-service-consumer:7777
```

其中,它们的命名模式: `${hostname}:${spring.application.name}:${server.port}`



#### 实例类EurekaInstanceConfigBean

#### 配置项

```properties
# Eureka 实例的id
eureka.instance.instance-id=${spring.application.name}:${server.port}
```



### 实例端点配置

#### 源码位置： `EurekaInstanceConfigBean`

```java
private String statusPageUrlPath = "/info";
```

####  配置项

```properties
# Eureka 客户端 应用实例状态 URL
eureka.instance.statusPageUrlPath=/health
```



## Eureka 服务端的高可用

### 构建Eureka服务器相互注册

####  Eureka Server 1 -> Profile: peer1

#####  配置项

```properties
# eureka 服务应用名称
spring.application.name=spring-cloud-eureka-server-1

# eureka 服务端口号
server.port=9090

# 取消eureka自我注册 功能
eureka.client.register-with-eureka=true

# 不需要检索服务
eureka.client.fetch-registry=true

# eureka server 服务Url
eureka.client.service-url.defaultZone=http://localhost:9091/eureka
```



####  Eureka Server 2 -> Profile: peer2

##### 配置项

```properties
# eureka 服务应用名称
spring.application.name=spring-cloud-eureka-server-2

# eureka 服务端口号
server.port=9091

# 取消eureka自我注册 功能
eureka.client.register-with-eureka=true

# 不需要检索服务
eureka.client.fetch-registry=true

# eureka server 服务Url
eureka.client.service-url.defaultZone=http://localhost:9090/eureka
```

通过`--spring.profiles.active=peer1`和`--spring.profiles.active=peer2`分别激活 Eureka server 1 和Eureka server 2



## Spring RestTemplate

#### HTTP 消息转换器 : HttpMessageConvertor

自定义实现

编码问题

**解决序列化反序列化协议**

#### HTTP Client 适配工厂:  ClientHttpRequestFactory

 这个方面主要考虑大家的使用HttpClient偏好:

- Spring实现
  - SimpleClientHttpRequestFactory
- HttpClient实现
  - HttpComponentsClientHttpRequestFactory
- OkHttp实现
  - OkHttp3ClientHttpRequestFactory
  - OkHttpClientHttpRequestFactory

**切换HTTP通讯实现,提升性能**

####  举例说明（通过构造的方式）

```java
RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());   // Http Client
```



### HTTP请求拦截器： ClientHttpRequestInterceptor

**加深RestTemplate拦截过程的理解**

## 整合Netflix Ribbon

`RestTemplate` 增加一个 `LoadBalancerInterceptor`, 调用Netflix 的 `LoadBalancer`实现, 根据Eureka 客户端应用 获取目标应用 ip + port的信息,轮询的方式调用 

### 实际请求客户端

- LoadBalancerClient
- RibbonLoacBalancerClient

### 负载均衡上下文

- LoadBalancerContext
  - RibbonLoadBalancerContext

### 负载均衡器

- ILoadBalancer

  - BaseLoadBalancer

  - DynamicServerListLoadBalancer

  - ZoneAwareLoadBalancer

  - NoOpLoadBalancer


### 负载均衡规则

#### 核心规则接口

- IRule
  - 随机规则 : RandomRule
  - 最可用规则: BestAvailableRule
  - 轮询规则: RoundRobinRule
  - 重试实现: RetryRule
  - 客户端配置: ClientConfigEnabledRoundRobinRule
  - 可用性过滤规则： AvailabilityFilteringRule
  - RT权重规则： WeightedResponseTimeRule
  - 规避区域规则： ZoneAvoidanceRule



## 问答部分

1. consul 和 Eureka 是一样的么？

     提供功能类似, consul 功能更强大, 广播室服务发现/注册

2. 重启Eureka服务器,客户端应用需要重启么？

   不用,客户端在不停地上报信息, 不过在Eureka服务器会大量报错

3. 生产环境中, consumer 是分别注册成多个服务, 还是统一放在一起注册成一个服务？ 

   大多数情况是需要, 根据应用职责划分。

4. 客户端上报的信息存储在哪里？

   都是在内存里面缓存着，EurekaClient 并不是缓存所有的服务, 都是根据自己的需要缓存应用实例。

5. consumer 调用 provider-a 挂了, 会自动切换 provider-b 么?

   会自动切换, 不过不一定及时。 不及时, 服务端可能存在脏数据, 或者轮询更新时间未达。

6. 为什么要用Eureka?

       目前业界比较稳定云计算的开发中间件,虽然有一些不足,基本上可用

7. Eureka主要功能为啥不能用浮动ip代替呢?

   如果要使用浮动的IP, 也是可以的,不过需要扩展

8. Eureka、Consul、zookeeper 比较?

   https://www.consul.io/intro/vs/zookeeper.html

   https://www.consul.io/intro/vs/eureka.html

9. 通讯不是指注册到defaultZone配置的那个么?

   默认情况是往defaultZone注册

10. 如果服务注册中心都挂了,服务还是能够运行吧？

    服务调用还是可以运行的, 有可能数据会不及时、不一致

11. spring cloud日志收集 有解决方案么?

    一般用HBase、或者TSDB、elk、opentsdb