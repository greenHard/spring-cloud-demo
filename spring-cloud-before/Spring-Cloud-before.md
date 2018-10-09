# Sping Cloud Start

## Sping Cloud Start

### 预备知识

#### 发布/订阅

`java.util.Observable` 是一个发布者

`java.util.Observer` 是订阅者

发布者和订阅者 1:N

发布者和订阅者 N:N

Observable:  推的模式

```java
public static class MyObservable extends Observable{
    @Override
    public synchronized void setChanged() {
        super.setChanged();
    }
}
```

测试:

```java
 public static void main(String[] args) {
        MyObservable observable = new MyObservable();

        // 增加订阅者
        observable.addObserver((o, value) -> System.out.println(value));
        observable.setChanged();
        // 发布者通知,订阅者被动感知(推的模式)
        observable.notifyObservers("hello world");

        echoIterator();
    }
```

拉的模式:

```java
public static void echoIterator(){
        List<Integer> values = Arrays.asList(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = values.iterator();
        if(iterator.hasNext()){
            // 通过循环主动去获取 (拉的模式)
            System.out.println(iterator.next());
        }
    }
```

#### 事件/监听模式

`java.util.EventObject`： 事件对象

	* 事件对象总是关系着事件源

`java.util.EventListener`: 事件监听接口(标记接口)

### Spring 事件/监听

`ApplicationEvent`:  应用事件

`ApplicationListener`：应用监听

测试代码:

```java

/**
 * Spring 自定义 事件/监听器
 */
public class SpringEventListenDemo {
    public static void main(String[] args) {
        // Annotation 驱动的Spring的上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // 注册监听器,只关注MyApplicationEvent事件
        context.addApplicationListener((ApplicationListener<MyApplicationEvent>) event -> {
            // 监听器得到这个事件
            System.out.println("接收到事件,事件源: " + event.getSource() + "@" + event.getApplicationContext());
        });

        context.refresh();

        // 发布事件
        context.publishEvent(new MyApplicationEvent(context,"hello world"));
        context.publishEvent(new MyApplicationEvent(context,1));
        context.publishEvent(new MyApplicationEvent(context,100));
    }

    private static class MyApplicationEvent extends ApplicationEvent {

        private final ApplicationContext applicationContext;


        public MyApplicationEvent(ApplicationContext applicationContext, Object source) {
            super(source);
            this.applicationContext = applicationContext;
        }

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }
    }
```

#### Spring Boot 事件/监听器

##### ConfigFileApplicationListener

管理配置文件,比如: `application.properties` 以及 `application.yml`

`application-{profile}.properties`

profile =dev、test  

1. `application-{profile}.properties` (优先加载)
2. `application.properties`



Spring Boot 在相对于 classpath：/META-INF/spring.factories

JAVA SPI : java.util.ServiceLoader

Spring SPI

```properties
org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener,\
org.springframework.boot.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.logging.LoggingApplicationListener
```

###### 如何控制顺序

实现`Ordered` 以及标记 `@Order`d
在Spring里面,  数字越小,  越优先

#### Spring Cloud 事件/监听器

##### BootstrapApplicationListener 

 优先级: 最高优先级+5

Spring Cloud  /META-INF/spring.factories

```properties
# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.cloud.bootstrap.BootstrapApplicationListener,\
org.springframework.cloud.bootstrap.LoggingSystemShutdownListener,\
org.springframework.cloud.context.restart.RestartListener
```

> 加载的优先级高于`ConfigFileApplicationListener`  最高优先级+10 ,所以
>
> `application.properties`即使配置改变文件名称也是配置不到 !
>
> 原因在于:
>
> `BootstrapApplicationListener`  第6优先
>
> `ConfigFileApplicationListener`  第11优先

1. 负责加载`bootstrap.properties` 或者 `bootstrap.yml`
2. 负责初始化 Bootstrap ApplicationContext ID = "bootstrap"

```java
final ConfigurableApplicationContext context = builder.run();
```

Bootstrap  是一个根 Spring 上下文, parent=null

> 联想classloader:
> ExtClassLoader -> AppClassLoader-> System ClassLoader -> BootStrap ClassLoader

#### ConfigurableApplicationContext

标准实现类:  `AnnotationConfigApplicationContext`

### env 端点

` EnvironmentEndpoint`

  Environment 关联多个带名称的`PropertySource`

  `AbstractRefreshableWebApplicationContext`

 ```java
protected void initPropertySources() {
		ConfigurableEnvironment env = getEnvironment();
		if (env instanceof ConfigurableWebEnvironment) {
			((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, this.servletConfig);
		}
	}
 ```

`Enviroment`: 有两种实现方式
	普通类型: `StandardEnvironment`
	Web类型: `StandardServletEnvironment`

`Enviroment`

	- `AbstractEnvironment`
	    - `StandardEnvironment`

`Environment` 关联着一个 `PropertySources` 实例

`PropertySources` 关联着多个 `ProperSource`  并且有优先级

其中比较常用的`PropertySource`实现:
Java System#getProperties实现  名称为"systemProperties",对应的内容为 `System.getProperties()`
Java System#getenv(环境变量)  名称为"systemProperties",对应的内容为 `System.getEnv()`

关于Spring Boot优先级顺序，可以参考: 
	https://docs.spring.io/spring-boot/docs/2.0.5.RELEASE/reference/htmlsingle/

#### 实现自定义的配置

1. 实现 PropertySourceLocator

2. 暴露该实现作为一个Spring Bean

3. 实现ProoertySource方法

  ```java
  @Configuration
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public static class MyPropertySourceLocator implements PropertySourceLocator{
      @Override
      public PropertySource<?> locate(Environment environment) {
          Map<String,Object> source  = new HashMap<>();
          source.put("server.port","9090");
          MapPropertySource propertySource = new MapPropertySource("my-property-source",source);
          return propertySource;
      }
  }
  ```

4. 添加META-INF/spring.factories

   ```properties
   org.springframework.cloud.bootstrap.BootstrapConfiguration=\
    com.rongshu.springcloudbefore.SpringCloudConfigClientApplication.MyPropertySourceLocator
   ```

注意事项:

`Environment` 允许出现相同的配置,优先级高的胜出

内部实现: `MutablePropertySources`关联代码:

```java
private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<PropertySource<?>>();
```

propertySourceList FIFO, 它有顺序
可以通过 `MutablePropertySources#addFirst()`方法,将优先级提高到最高, 相当于调用: 

`List#set(0,PropertySource)`

### Q&A

1. 自定义的配置一般用的多不多,使用场景?
    不多,一般用于中间件开发。

  2. Spring里面有个`@EventListener`和`ApplicationListener`什么区别?
      没有区别, 前者是Annotation编程模式,后者接口编程。

  3. 怎么防止`Order`一样?
     Spring Boot 和Spring Cloud 里面没有办法, 在Spring Security 通过异常实现的。

  4. `/env `  断点的使用场景是什么?
     用于排查问题,比如看 `@Value("server.port")` 的值。

   5. `bootstrapApplicationListener` 是引入Sping Cloud 组件来用的么?
      是的。