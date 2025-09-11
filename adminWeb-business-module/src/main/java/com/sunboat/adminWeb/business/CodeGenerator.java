package com.sunboat.adminWeb.business;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/admin_user?characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai", "root", "12345678")
                .globalConfig(builder -> {
                    builder.author("Sunboat") // 设置作者
                            .outputDir("src/main/java/codeGen"); // 输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.tempalte") // 设置父包名
                            .entity("entity") // 设置实体类包名
                            .mapper("maper") // 设置 Mapper 接口包名
                            .service("service") // 设置 Service 接口包名
                            .serviceImpl("service.impl") // 设置 Service 实现类包名
                            .xml("mappers"); // 设置 Mapper XML 文件包名
                })
                .strategyConfig(builder -> {
                    builder.addInclude("permissions",
                                    "role_permissions",
                                    "roles",
                                    "user_action_logs",
                                    "user_login_logs",
                                    "user_roles",
                                    "users") // 设置需要生成的表名
                            .entityBuilder()
                            .enableLombok() // 启用 Lombok
                            .enableTableFieldAnnotation() // 启用字段注解
                            .controllerBuilder()
                            .enableRestStyle(); // 启用 REST 风格
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 Freemarker 模板引擎
                .execute();
    }

}