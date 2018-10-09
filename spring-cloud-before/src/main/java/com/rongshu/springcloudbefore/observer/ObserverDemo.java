package com.rongshu.springcloudbefore.observer;

import java.util.*;

/**
 * 观察者模式 --  发布/订阅
 */
public class ObserverDemo {

    public static void main(String[] args) {
        MyObservable observable = new MyObservable();

        // 增加订阅者
        observable.addObserver((o, value) -> System.out.println(value));
        observable.setChanged();
        // 发布者通知,订阅者被动感知(推的模式)
        observable.notifyObservers("hello world");

        echoIterator();
    }

    public static class MyObservable extends Observable{
        @Override
        public synchronized void setChanged() {
            super.setChanged();
        }
    }

    public static void echoIterator(){
        List<Integer> values = Arrays.asList(1, 2, 3, 4, 5);
        Iterator<Integer> iterator = values.iterator();
        if(iterator.hasNext()){
            // 通过循环主动去获取 (拉的模式)
            System.out.println(iterator.next());
        }
    }
}
