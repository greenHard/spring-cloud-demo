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

        // 返回三个配置的Server http://localhost:9999,http://localhost:9999,http://localhost:9999
        List<Server> allServers = loadBalancer.getAllServers();

        return allServers.get(0);
    }
}
