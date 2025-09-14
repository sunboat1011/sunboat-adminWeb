package com.sunboat.adminWeb.business.provider;

import com.sunboat.adminweb.common.api.HelloService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@DubboService(version = "1.0.0")
@Component
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "Hello, " + (name == null ? "world" : name) + "!";
    }
}
