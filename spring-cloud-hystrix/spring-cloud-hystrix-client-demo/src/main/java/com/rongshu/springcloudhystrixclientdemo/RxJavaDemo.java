package com.rongshu.springcloudhystrixclientdemo;

import org.springframework.scheduling.annotation.Schedules;
import rx.Observer;
import rx.Single;
import rx.schedulers.Schedulers;

import java.util.Random;

/**
 * Reactive X Demo
 */
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
