package com.sunboat.adminWeb.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性，通过application.yml注入
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    // 签名密钥（生产环境需使用更复杂的密钥）
    private String secret = "dev-secret-key-123456";
    // 令牌过期时间（单位：秒）
    private long expiration = 3600; // 默认1小时

    // Getter和Setter
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
    