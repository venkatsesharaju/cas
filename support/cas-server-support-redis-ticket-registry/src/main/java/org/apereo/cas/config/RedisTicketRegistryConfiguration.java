package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.apereo.cas.ticket.registry.RedisTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRedisTemplate;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

/**
 * This is {@link RedisTicketRegistryConfiguration}.
 *
 * @author serv
 * @since 5.0.0
 */
@Configuration("redisTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    private RedisTicketRegistryProperties redisProperties() {
        return casProperties.getTicket().getRegistry().getRedis();
    }

    @Bean
    @RefreshScope
    public RedisConnectionFactory redisConnectionFactory() {
        final JedisPoolConfig poolConfig = this.redisProperties().getPool() != null
                ? jedisPoolConfig() : new JedisPoolConfig();

        final JedisConnectionFactory factory = new JedisConnectionFactory(poolConfig);
        factory.setHostName(this.redisProperties().getHost());
        factory.setPort(this.redisProperties().getPort());
        if (this.redisProperties().getPassword() != null) {
            factory.setPassword(this.redisProperties().getPassword());
        }
        factory.setDatabase(this.redisProperties().getDatabase());
        if (this.redisProperties().getTimeout() > 0) {
            factory.setTimeout(this.redisProperties().getTimeout());
        }
        return factory;
    }

    private JedisPoolConfig jedisPoolConfig() {
        final JedisPoolConfig config = new JedisPoolConfig();
        final RedisTicketRegistryProperties.Pool props = this.redisProperties().getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait());
        return config;
    }


    @Bean
    @RefreshScope
    public TicketRedisTemplate ticketRedisTemplate() {
        return new TicketRedisTemplate(redisConnectionFactory());
    }

    @Bean(name = {"redisTicketRegistry", "ticketRegistry"})
    @RefreshScope
    public TicketRegistry redisTicketRegistry() {
        return new RedisTicketRegistry(ticketRedisTemplate());
    }
}
