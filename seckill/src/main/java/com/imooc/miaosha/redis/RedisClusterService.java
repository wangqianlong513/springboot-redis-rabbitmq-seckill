package com.imooc.miaosha.redis;

/**
 * @author wangql
 * @date 2020/5/5  21:16
 * @描述
 */
public interface  RedisClusterService {
    public Object getValue(String key);
    public String setInfo(String key,String value);
}
