package com.qf.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
public class lockUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    //添加分布式锁lua脚本
    private String lockLua = "--使用lua脚本添加分布式锁\n" +
            "--需要的变量:\n" +
            "local lockName = KEYS[1]\n" +
            "local lockValue = ARGV[1]\n" +
            "local lockTimeOut = ARGV[2]\n" +
            "\n" +
            "--设置分布式锁,如果返回结果为0,就代表当前分布式锁正在使用\n" +
            "local result = redis.call('setnx',lockName,lockValue)\n" +
            "\n" +
            "if result == 1 then\n" +
            "\n" +
            "--获得了分布式锁,添加超时时间\n" +
            "\tredis.call('expire',lockName,lockTimeOut)\n" +
            "\t--返回1表示获得了分布式锁\n" +
            "\treturn '1'\n" +
            "else\n" +
            "\t--没有获得分布式锁\n" +
            "\treturn '0'\n" +
            "end\n";

    //删除分布式锁Lua脚本
    private String lockDelLua = "--使用lua脚本添加分布式锁\n" +
            "--使用lua脚本删除分布式锁\n" +
            "--需要的变量\n" +
            "local lockName = KEYS[1]\n" +
            "local uuid = ARGV[1]\n" +
            "\n" +
            "--获得锁的value\n" +
            "local lockValue = redis.call('get',lockName)\n" +
            "\n" +
            "--判断锁的value和添加锁时的uuid是否一致\n" +
            "if lockValue == uuid then\n" +
            "\n" +
            "--说明锁是当前线程添加,删除该锁\n" +
            "\tredis.call('del',lockName)\n" +
            "--返回成功\n" +
            "\treturn '1'\n" +
            "end\n" +
            "\n" +
            "--说明锁不是当前线程添加\n" +
            "\treturn '0'";

    //如果一个业务用的一直是一个线程就可以使用ThreadLocal,它可以确保你取出的值肯定是当前线程的值

    private ThreadLocal<String> threadLocal = new ThreadLocal<>();


    /**
     * 添加分布式锁
     * @param lockName
     * @param timeoutSecond
     * @return
     */

    public boolean lock(String lockName,Integer timeoutSecond){
       //通过uuid获得锁的唯一value
        String uuid = UUID.randomUUID().toString();

        //把uuid加进ThreadLocal里,因为是同一个线程,方便释放锁的时候获取
        threadLocal.set(uuid);

        //调用lua脚本添加分布式锁
        String lueResult = (String) redisTemplate.execute(new DefaultRedisScript(lockLua,String.class), Collections.singletonList(lockName),uuid,timeoutSecond+"");

        return Integer.parseInt(lueResult)==1;
    }

    /**
     * 解除分布式锁
     * @param lockName
     * @return
     */
    public boolean unlock(String lockName){

        //获得存在ThreadLocal中的uuid
        String uuid = threadLocal.get();

        //调用Lua脚本删除分布式锁
        String result = (String) redisTemplate.execute(new DefaultRedisScript(lockDelLua,String.class),
                Collections.singletonList(lockName),uuid);

        return Integer.parseInt(result) == 1;
    }
}
