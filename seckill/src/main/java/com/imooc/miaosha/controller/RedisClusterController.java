package com.imooc.miaosha.controller;

import com.imooc.miaosha.redis.RedisClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wangql
 * @date 2020/5/5  21:17
 * @描述
 */
@Controller
@RequestMapping("redisCluster")
public class RedisClusterController {
    @Autowired
    RedisClusterService redisClusterService;

    /**
     * 根据键获取redis中对应的值
     * @return
     */
    @ResponseBody
    @RequestMapping("/get")
    public Object getValue(){
        Object value = redisClusterService.getValue("rabbit");
        return value;
    }

    /**
     * 向redis集群中存入值
     * @return
     */
    @ResponseBody
    @RequestMapping("/set")
    public String setInfo(){
        try {
            String set =  redisClusterService.setInfo("rabbit","bb");
            return "已存入缓存。。。"+set;
        } catch (Exception e) {
            e.printStackTrace();
            return "缓存存入失败。。。";
        }
    }
}
