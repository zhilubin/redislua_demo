package com.qf.service.impl;

import com.qf.dao.DianJiMapper;
import com.qf.entity.Dianjishu;
import com.qf.service.IDianJiService;
import com.qf.utils.lockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DianJiServiceImpl implements IDianJiService {

    @Autowired
    @SuppressWarnings("all")
    private DianJiMapper dianJiMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private lockUtils lockUtils;

    @Override
    public int insertDianJi() {

        //添加分布式锁的时候可以不用用Lua脚本,因为redis的这个方法的特性就是原子性的
        //redisTemplate.opsForValue().setIfAbsent("", "", 5, TimeUnit.SECONDS);

        //先获得分布式锁
        boolean isLock = lockUtils.lock("mylock", 1);

        //如果为true成功获得锁
        if(isLock){
            //先查出当前点击数,然后加1
            Dianjishu dianjishu = dianJiMapper.selectById(1);
            dianjishu.setDianji(dianjishu.getDianji()+1);

            //更新点击数
            int result = dianJiMapper.updateById(dianjishu);

            //释放分布式锁
            lockUtils.unlock("mylock");

            return result;

            //反正没有获得锁,锁在使用中
        }else {

            try {
                //设置锁的自旋
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            return this.insertDianJi();
    }
}
