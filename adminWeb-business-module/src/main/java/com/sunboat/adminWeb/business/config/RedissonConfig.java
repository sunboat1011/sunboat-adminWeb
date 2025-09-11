import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    // 从 Nacos 注入单节点的 host 和 port（对应修正后的 Nacos 配置）
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    // 注入超时时间（可选，保持原逻辑）
    @Value("${spring.redis.timeout}")
    private int redisTimeout;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 拼接地址：必须带 redis:// 前缀（解决报错核心）
        String redisAddress = "redis://" + redisHost + ":" + redisPort;

        // 单节点模式配置（匹配 Nacos 单节点配置）
        config.useSingleServer()
                .setAddress(redisAddress)          // 正确格式的地址
                .setIdleConnectionTimeout(redisTimeout)  // 复用 Nacos 中的超时配置
                .setConnectionPoolSize(16)         // 建议补充连接池配置（可选，优化性能）
                .setConnectTimeout(redisTimeout);  // 连接超时（可选，避免超时问题）

        return Redisson.create(config);
    }
}