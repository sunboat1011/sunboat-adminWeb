package com.sunboat.adminWeb.business;


import com.sunboat.adminWeb.business.utils.CommonUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.context.annotation.ComponentScan;


@DubboComponentScan(basePackages = "com.sunboat.adminweb.provider")
@SpringBootApplication
@MapperScan("com.sunboat.adminWeb.business.maper")
@ComponentScan(basePackages = {
        "com.sunboat.adminWeb.business", // user-business 自身的包
        "com.sunboat.common.core" // common-core 中 Bean 所在的根包
})
public class AdminWebApplication implements CommandLineRunner {


//    @Value("${spring.profiles.active}")
//    private String activeProfile;

    public static void main(String[] args) {
        SpringApplication.run(AdminWebApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        System.out.println(activeProfile);
        System.out.println(CommonUtils.getMessage());
    }
}    