package com.cms.permissions.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 缓存优化配置类
 * 提供高性能的Redis连接池、序列化器和缓存管理器配置
 */
@Configuration
@EnableCaching
public class CacheOptimizationConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${cache.permissions.ttl:300}")
    private int permissionsTtl;

    @Value("${cache.document-public.ttl:600}")
    private int documentPublicTtl;

    @Value("${cache.document-assignments.ttl:180}")
    private int documentAssignmentsTtl;

    @Value("${cache.optimization.connection-pool.max-active:20}")
    private int maxActive;

    @Value("${cache.optimization.connection-pool.max-idle:10}")
    private int maxIdle;

    @Value("${cache.optimization.connection-pool.min-idle:5}")
    private int minIdle;

    @Value("${cache.optimization.connection-pool.max-wait:3000}")
    private long maxWait;

    @Value("${cache.optimization.socket.connect-timeout:5000}")
    private long connectTimeout;

    @Value("${cache.optimization.socket.command-timeout:3000}")
    private long commandTimeout;

    /**
     * 优化的Redis连接工厂
     * 配置高性能连接池和网络参数
     */
    @Bean
    @Primary
    public LettuceConnectionFactory optimizedRedisConnectionFactory() {
        // Redis服务器配置
        RedisStandaloneConfiguration redisConfig =
            new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);
        if (!redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // 连接池配置
        GenericObjectPoolConfig<io.lettuce.core.api.StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWait));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
        poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(60000));
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);

        // Socket配置优化
        SocketOptions socketOptions = SocketOptions.builder()
            .connectTimeout(Duration.ofMillis(connectTimeout))
            .keepAlive(true)
            .tcpNoDelay(true)
            .build();

        // 超时配置
        TimeoutOptions timeoutOptions = TimeoutOptions.builder()
            .fixedTimeout(Duration.ofMillis(commandTimeout))
            .build();

        // 客户端配置
        ClientOptions clientOptions = ClientOptions.builder()
            .socketOptions(socketOptions)
            .timeoutOptions(timeoutOptions)
            .autoReconnect(true)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .pingBeforeActivateConnection(true)
            .build();

        // Lettuce客户端配置
        LettucePoolingClientConfiguration clientConfig =
            LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofMillis(commandTimeout))
                .shutdownTimeout(Duration.ofMillis(100))
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * 优化的Jackson ObjectMapper
     * 配置高性能JSON序列化
     */
    @Bean
    public ObjectMapper optimizedObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 性能优化配置
        mapper.setVisibility(
            PropertyAccessor.ALL,
            JsonAutoDetect.Visibility.ANY
        );
        mapper.findAndRegisterModules();

        // 减少序列化开销
        mapper.configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        );
        mapper.configure(
            com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS,
            false
        );
        mapper.configure(
            com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            true
        );

        return mapper;
    }

    /**
     * 优化的String序列化器
     */
    @Bean
    public StringRedisSerializer optimizedStringSerializer() {
        return new StringRedisSerializer();
    }

    /**
     * 优化的Set序列化器
     */
    @Bean
    public Jackson2JsonRedisSerializer<Set<String>> optimizedSetSerializer() {
        ObjectMapper mapper = optimizedObjectMapper();
        Jackson2JsonRedisSerializer<Set<String>> serializer =
            new Jackson2JsonRedisSerializer<>(
                mapper,
                mapper
                    .getTypeFactory()
                    .constructCollectionType(Set.class, String.class)
            );
        return serializer;
    }

    /**
     * 优化的List序列化器
     */
    @Bean
    public Jackson2JsonRedisSerializer<List<Long>> optimizedListSerializer() {
        ObjectMapper mapper = optimizedObjectMapper();
        Jackson2JsonRedisSerializer<List<Long>> serializer =
            new Jackson2JsonRedisSerializer<>(
                mapper,
                mapper
                    .getTypeFactory()
                    .constructCollectionType(List.class, Long.class)
            );
        return serializer;
    }

    /**
     * 优化的Boolean序列化器
     */
    @Bean
    public Jackson2JsonRedisSerializer<Boolean> optimizedBooleanSerializer() {
        ObjectMapper mapper = optimizedObjectMapper();
        Jackson2JsonRedisSerializer<Boolean> serializer =
            new Jackson2JsonRedisSerializer<>(mapper, Boolean.class);
        return serializer;
    }

    /**
     * 高性能RedisTemplate配置
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> optimizedRedisTemplate(
        LettuceConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用优化的序列化器
        StringRedisSerializer stringSerializer = optimizedStringSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
            new GenericJackson2JsonRedisSerializer(optimizedObjectMapper());

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 启用事务支持
        template.setEnableTransactionSupport(true);

        // 设置默认序列化器
        template.setDefaultSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 专用的权限缓存RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Set<String>> permissionRedisTemplate(
        LettuceConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Set<String>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = optimizedStringSerializer();
        Jackson2JsonRedisSerializer<Set<String>> setSerializer =
            optimizedSetSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(setSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(setSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 专用的文档分配缓存RedisTemplate
     */
    @Bean
    public RedisTemplate<String, List<Long>> assignmentRedisTemplate(
        LettuceConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, List<Long>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = optimizedStringSerializer();
        Jackson2JsonRedisSerializer<List<Long>> listSerializer =
            optimizedListSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(listSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(listSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 专用的文档公开状态缓存RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Boolean> publicStatusRedisTemplate(
        LettuceConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Boolean> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = optimizedStringSerializer();
        Jackson2JsonRedisSerializer<Boolean> booleanSerializer =
            optimizedBooleanSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(booleanSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(booleanSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 优化的缓存管理器
     */
    @Bean
    @Primary
    public CacheManager optimizedCacheManager(
        LettuceConnectionFactory connectionFactory
    ) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig =
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(permissionsTtl))
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        optimizedStringSerializer()
                    )
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(
                            optimizedObjectMapper()
                        )
                    )
                )
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "cms:cache:" + cacheName + ":");

        // 特定缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations =
            new HashMap<>();

        // 用户权限缓存配置
        cacheConfigurations.put(
            "userPermissions",
            defaultConfig
                .entryTtl(Duration.ofSeconds(permissionsTtl))
                .computePrefixWith(cacheName -> "cms:permissions:user:")
        );

        // 文档权限缓存配置
        cacheConfigurations.put(
            "documentPermissions",
            defaultConfig
                .entryTtl(Duration.ofSeconds(permissionsTtl))
                .computePrefixWith(cacheName -> "cms:permissions:document:")
        );

        // 文档公开状态缓存配置
        cacheConfigurations.put(
            "documentPublic",
            defaultConfig
                .entryTtl(Duration.ofSeconds(documentPublicTtl))
                .computePrefixWith(cacheName -> "cms:public:document:")
        );

        // 文档分配缓存配置
        cacheConfigurations.put(
            "documentAssignments",
            defaultConfig
                .entryTtl(Duration.ofSeconds(documentAssignmentsTtl))
                .computePrefixWith(cacheName -> "cms:assignments:user:")
        );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    /**
     * 缓存性能监控配置
     */
    @Configuration
    @ConditionalOnProperty(
        name = "cache.optimization.monitoring.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public static class CacheMonitoringConfig {

        /**
         * 缓存性能监控器
         */
        @Bean
        public CachePerformanceMonitor cachePerformanceMonitor() {
            return new CachePerformanceMonitor();
        }
    }

    /**
     * 缓存性能监控器
     */
    public static class CachePerformanceMonitor {

        private final Map<String, CacheMetrics> metricsMap = new HashMap<>();

        public void recordCacheOperation(
            String cacheName,
            boolean hit,
            long responseTime
        ) {
            metricsMap
                .computeIfAbsent(cacheName, k -> new CacheMetrics())
                .recordOperation(hit, responseTime);
        }

        public CacheMetrics getMetrics(String cacheName) {
            return metricsMap.get(cacheName);
        }

        public void resetMetrics(String cacheName) {
            metricsMap.remove(cacheName);
        }

        public void resetAllMetrics() {
            metricsMap.clear();
        }
    }

    /**
     * 缓存指标
     */
    public static class CacheMetrics {

        private long totalOperations = 0;
        private long hitCount = 0;
        private long missCount = 0;
        private long totalResponseTime = 0;
        private long maxResponseTime = 0;
        private long minResponseTime = Long.MAX_VALUE;

        public synchronized void recordOperation(
            boolean hit,
            long responseTime
        ) {
            totalOperations++;
            if (hit) {
                hitCount++;
            } else {
                missCount++;
            }

            totalResponseTime += responseTime;
            maxResponseTime = Math.max(maxResponseTime, responseTime);
            minResponseTime = Math.min(minResponseTime, responseTime);
        }

        // Getters
        public long getTotalOperations() {
            return totalOperations;
        }

        public long getHitCount() {
            return hitCount;
        }

        public long getMissCount() {
            return missCount;
        }

        public double getHitRate() {
            return totalOperations > 0
                ? (double) hitCount / totalOperations
                : 0.0;
        }

        public double getAverageResponseTime() {
            return totalOperations > 0
                ? (double) totalResponseTime / totalOperations
                : 0.0;
        }

        public long getMaxResponseTime() {
            return maxResponseTime;
        }

        public long getMinResponseTime() {
            return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime;
        }
    }
}
