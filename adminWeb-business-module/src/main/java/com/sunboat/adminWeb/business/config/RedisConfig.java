package com.sunboat.adminWeb.business.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration // 关键：必须加这个注解，否则 Spring 不会加载该配置
public class RedisConfig {

    // 关键：定义 RedisTemplate Bean，指定序列化方式
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 绑定 Redis 连接工厂（从 Spring 容器自动注入，依赖 Nacos 中的 Redis 连接参数）
        redisTemplate.setConnectionFactory(factory);

        // 配置键（Key）的序列化：String 序列化（避免乱码）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        // 配置值（Value）的序列化：JSON 序列化（支持对象存储，避免乱码）
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        // 初始化 RedisTemplate（必须调用，否则配置不生效）
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}