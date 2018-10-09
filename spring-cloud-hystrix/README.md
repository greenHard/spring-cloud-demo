# Spring Cloud Netflix Hystrix

## Spring Cloud Netflix Hystrix

### 服务短路（CircuitBreaker）

QPS: Query Per Second

TPS: Transaction Per Second



QPS: 经过全链路压测， 计算单机极限QPS, 集群QPS = 单机 QPS * 集群机器数量 * 可靠比率

全链路压测 除了压 极限QPS， 还有错误数量

全链路: 一个完整的业务流程操作



JMeter:  可调整型比较灵活

### Spring Cloud Hystrix Client

####  

官网: https://github.com/Netflix/Hystrix

> Reactive Java 框架
>
> - Java 9 Flow API
> - Reactor
> - RxJava (Reactive X)

#### 激活Hystrix

通过`@EnableHystrix`激活



> 配置信息wiki: https://github.com/Netflix/Hystrix/wiki/Configuration

#### Hystrix

##### 注解方式

```java
    private final Random random = new Random();

    /**
     * 当{@link #helloWorld()} 方法超时或者失败时
     * fallback 方法 {@link #errorContent()} 作为替代返回
     */
    @GetMapping("hello-world")
    @HystrixCommand(
            fallbackMethod = "errorContent",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds"
                            ,value = "100")
            })
    public String helloWorld() throws InterruptedException {
        // 如果随机时间 大于100，那么触发容错
        int value = random.nextInt(200);
        System.out.println("helloWorld() costs " + value +" ms");
        Thread.sleep(value);
        return "hello world";
    }

    public String errorContent(){
        return "Fault";
    }
```

##### 编程方式

```java
 /**
     * 当{@link #helloWorld()} 方法超时或者失败时
     * fallback 方法 {@link #errorContent()} 作为替代返回
     */
    @GetMapping("hello-world-2")

    public String helloWorld2(){
       return new HelloWorldCommand().execute();
    }


    public static class HelloWorldCommand extends com.netflix.hystrix.HystrixCommand<String>{

        protected HelloWorldCommand() {
            super(HystrixCommandGroupKey.Factory.asKey("hello world"),100);
        }

        @Override
        protected String run() throws Exception {
            // 如果随机时间 大于100，那么触发容错
            int value = random.nextInt(200);
            System.out.println("helloWorld() costs " + value +" ms");
            Thread.sleep(value);
            return "hello , World";
        }

        @Override
        protected String getFallback() {
            return "Fault";
        }
    }
```

#### RXJava

```java
public class RxJavaDemo {

    private static final Random random = new Random();


    public static void main(String[] args) {
        Single.just("hello world")  // just 发布订阅
                .subscribeOn(Schedulers.immediate())  // 订阅的线程池 immediate = Thread.currentThread()
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() { // 正常结束
                        System.out.println("执行结束!");
                    }

                    @Override
                    public void onError(Throwable throwable) { // 异常流程(结束)
                        System.out.println("熔断保护！ ");
                    }

                    @Override
                    public void onNext(String s) {  // 数据消费
                        // 如果随机时间 大于100，那么触发容错
                        int value = random.nextInt(200);
                        System.out.println("helloWorld() costs " + value +" ms");
                        if(value>100){
                            throw new RuntimeException();
                        }
                    }
                });
    }
}

```

#### Health Endpoint(`/health`)

```json
{
  description: "Composite Discovery Client",
  status: "UP",
  discoveryComposite: {
  description: "Composite Discovery Client",
  status: "UP",
  discoveryClient: {
  description: "Composite Discovery Client",
  status: "UP",
  services: [ ]
  },
  eureka: {
  description: "Remote status from Eureka server",
  status: "UNKNOWN",
  applications: { }
  }
  },
  diskSpace: {
  status: "UP",
  total: 51534360576,
  free: 11575095296,
  threshold: 10485760
  },
  hystrix: {
  status: "UP"
  }
}
```

#### 激活熔断保护

`@EnableCircuitBreaker`  激活: `@EnableHystrix `+ Spring Cloud 的功能

`@EnableHystrix`激活, 没有一些Spring Cloud功能

#### Hystrix Endpoint(`/hystrix.stream`)

```json
{
    "type": "HystrixCommand", 
    "name": "helloWorld", 
    "group": "HystrixDemoController", 
    "currentTime": 1538638955829, 
    "isCircuitBreakerOpen": false, 
    "errorPercentage": 0, 
    "errorCount": 0, 
    "requestCount": 1, 
    "rollingCountBadRequests": 0, 
    "rollingCountCollapsedRequests": 0, 
    "rollingCountEmit": 0, 
    "rollingCountExceptionsThrown": 0, 
    "rollingCountFailure": 0, 
    "rollingCountFallbackEmit": 0, 
    "rollingCountFallbackFailure": 0, 
    "rollingCountFallbackMissing": 0, 
    "rollingCountFallbackRejection": 0, 
    "rollingCountFallbackSuccess": 0, 
    "rollingCountResponsesFromCache": 0, 
    "rollingCountSemaphoreRejected": 0, 
    "rollingCountShortCircuited": 0, 
    "rollingCountSuccess": 0, 
    "rollingCountThreadPoolRejected": 0, 
    "rollingCountTimeout": 0, 
    "currentConcurrentExecutionCount": 0, 
    "rollingMaxConcurrentExecutionCount": 0, 
    "latencyExecute_mean": 0, 
    "latencyExecute": {
        "0": 0, 
        "25": 0, 
        "50": 0, 
        "75": 0, 
        "90": 0, 
        "95": 0, 
        "99": 0, 
        "100": 0, 
        "99.5": 0
    }, 
    "latencyTotal_mean": 0, 
    "latencyTotal": {
        "0": 0, 
        "25": 0, 
        "50": 0, 
        "75": 0, 
        "90": 0, 
        "95": 0, 
        "99": 0, 
        "100": 0, 
        "99.5": 0
    }, 
    "propertyValue_circuitBreakerRequestVolumeThreshold": 20, 
    "propertyValue_circuitBreakerSleepWindowInMilliseconds": 5000, 
    "propertyValue_circuitBreakerErrorThresholdPercentage": 50, 
    "propertyValue_circuitBreakerForceOpen": false, 
    "propertyValue_circuitBreakerForceClosed": false, 
    "propertyValue_circuitBreakerEnabled": true, 
    "propertyValue_executionIsolationStrategy": "THREAD", 
    "propertyValue_executionIsolationThreadTimeoutInMilliseconds": 100, 
    "propertyValue_executionTimeoutInMilliseconds": 100, 
    "propertyValue_executionIsolationThreadInterruptOnTimeout": true, 
    "propertyValue_executionIsolationThreadPoolKeyOverride": null, 
    "propertyValue_executionIsolationSemaphoreMaxConcurrentRequests": 10, 
    "propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests": 10, 
    "propertyValue_metricsRollingStatisticalWindowInMilliseconds": 10000, 
    "propertyValue_requestCacheEnabled": true, 
    "propertyValue_requestLogEnabled": true, 
    "reportingHosts": 1, 
    "threadPool": "HystrixDemoController"
}

```

### Spring Cloud Hystrix Dashboard

#### 激活

`@EnableHystrixDashboard`

```java
@SpringBootApplication
@EnableHystrixDashboard
public class SpringCloudHystrixDashboardDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudHystrixDashboardDemoApplication.class, args);
	}
}
```

### 整合 Netflix Turbine





? @PropertySource 和@PropertySources   -->  编码

