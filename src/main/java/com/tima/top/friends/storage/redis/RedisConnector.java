package com.tima.top.friends.storage.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
@ComponentScan("com.tima.top.friends.storage.redis")
public class RedisConnector {

    @Value("${spring.redis.host}")
    private String REDIS_HOSTNAME;
    @Value("${spring.redis.port}")
    private int REDIS_PORT;
    @Value("${spring.redis.password}")
    private String REDIS_PASSWORD;

    private Jedis jedis;

    public RedisConnector() {
    }

    public Jedis getJedis() {
        if(jedis != null){
            return jedis;
        }
        jedis = new Jedis(REDIS_HOSTNAME, REDIS_PORT);
        jedis.auth(REDIS_PASSWORD);
        return jedis;
    }

    public void close(){
        jedis.close();
    }

}