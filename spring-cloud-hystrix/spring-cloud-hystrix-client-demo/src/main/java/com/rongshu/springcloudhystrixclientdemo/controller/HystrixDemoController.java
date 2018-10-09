package com.rongshu.springcloudhystrixclientdemo.controller;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class HystrixDemoController {

    private static final Random random = new Random();


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
}
