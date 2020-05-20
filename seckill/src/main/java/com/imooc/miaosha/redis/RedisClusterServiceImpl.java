package com.imooc.miaosha.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

/**
 * @author wangql
 * @date 2020/5/5  21:16
 * @描述
 */
@Service
public class RedisClusterServiceImpl  implements RedisClusterService {
    //注入JedisCluster
    @Autowired
    JedisCluster jedisCluster;

    @Override
    public Object getValue(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public String setInfo(String key, String value) {
        String set = jedisCluster.set(key, value);
        return set;
    }

}
