package com.sunboat.adminweb.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;

@DubboComponentScan(basePackages = "com.sunboat.adminweb.provider")
@SpringBootApplication
public class AdminWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminWebApplication.class, args);
    }
}
