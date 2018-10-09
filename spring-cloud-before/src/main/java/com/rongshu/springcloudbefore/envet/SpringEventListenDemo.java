package com.rongshu.springcloudbefore.envet;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
}
