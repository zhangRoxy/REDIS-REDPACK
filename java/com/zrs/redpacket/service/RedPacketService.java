package com.zrs.redpacket.service;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.zrs.redpacket.mapper.RedPacketMapper;
import com.zrs.redpacket.pojo.RedPacketInfo;
import com.zrs.redpacket.pojo.RedPacketRecord;
import io.netty.util.internal.StringUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.GenericReactiveTransaction;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class RedPacketService {
    private static final String redPacketAmount = "_redPacketAmount";
    private static final String redPacketNum = "_redPacketNum";
    @Resource
    RedPacketMapper redPacketMapper;
    @Resource
    RedisTemplate<String, Object> redisTemplate;
    private DefaultRedisScript<Boolean> luaScript;

    /**
     * @return java.lang.Integer
     * @throws
     * @Param [uid, redPacketAmount, redPacketNum]
     * @description 新建红包
     **/
    public String addPacket(Integer uid, Integer redPacketAmount, Integer redPacketNum) {
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.setCreate_time(new Date());
        redPacketInfo.setUid(uid);
        redPacketInfo.setTotal_amount(redPacketAmount);
        redPacketInfo.setTotal_packet(redPacketNum);
        redPacketInfo.setUpdate_time(new Date());
        redPacketInfo.setRemaining_amount(redPacketAmount);
        redPacketInfo.setRemaining_packet(redPacketNum);
        long redPacketId = System.currentTimeMillis();   //此时无法保证红包id唯一，最好是用雪花算法进行生成分布式系统唯一键
        redPacketInfo.setRed_packet_id(redPacketId);
        if (redPacketMapper.addRedPacket(redPacketInfo) > 0) {
            redisTemplate.opsForValue().set(String.valueOf(redPacketId + redPacketAmount), redPacketAmount);
            redisTemplate.opsForValue().set(String.valueOf(redPacketId + redPacketNum), redPacketNum);
            return "success";
        }
        return "fail";
    }

    /**
     * @return void
     * @throws
     * @Param [uid, redPacketId]
     * @description 抢红包
     **/
    public String getPacket(long redPacketId) {
        String amount = redPacketId + redPacketAmount;
        String num = redPacketId + redPacketNum;
        if (StringUtil.isNullOrEmpty(String.valueOf(redisTemplate.opsForValue().get(amount))) || StringUtil.isNullOrEmpty(String.valueOf(redisTemplate.opsForValue().get(num)))) {
            return "红包已经领完了";
        }
        return "ok";
    }


    public String openPacket(Integer uid, long redPacketId) {
        int getAmount = 0;
        String amount = redPacketId + redPacketAmount;
        String num = redPacketId + redPacketNum;
        String totalAmount = String.valueOf(redisTemplate.opsForValue().get(amount));
        String totalNum = String.valueOf(redisTemplate.opsForValue().get(num));
        if (StringUtil.isNullOrEmpty(totalAmount) || StringUtil.isNullOrEmpty(totalNum)) {
            return "红包已经领完了";
        }
        if (!StringUtil.isNullOrEmpty(totalAmount)) {
            Integer totalAmountInt = Integer.parseInt(totalAmount);
            Integer totalNumInt = Integer.parseInt(totalNum);
            int maxMoney = totalAmountInt / totalNumInt * 2;
            Random random = new Random();
            getAmount = random.nextInt(maxMoney);
        }
        boolean result = luaResult(num, amount, getAmount);
        //redisTemplate.opsForValue().decrement(num); //由于多用户并发执行，执行到此处时，红包已经被抢了多次，所以用lua 脚本解决，让两个命令一次性执行
        //redisTemplate.opsForValue().decrement(amount, getAmount);
        if (result) {
            updateRacketInDB(uid, redPacketId, getAmount);
            return String.valueOf(getAmount);
        }
        return "失败";

    }

    public boolean luaResult(String num, String amount, Integer getAmount) {
        luaScript = new DefaultRedisScript<>();
        luaScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("redPacket.lua")));
        luaScript.setResultType(Boolean.class);
        // 封装参数
        List<String> keyList = new ArrayList<>();
        keyList.add(num);
        keyList.add(amount);
        //List<Object> valueList = new ArrayList<>();
        //valueList.add(getAmount);
        // 执行脚本
        return redisTemplate.execute(luaScript, keyList, getAmount);

    }

    public void updateRacketInDB(int uid, long redPacketId, int amount) {
        RedPacketRecord redPacketRecord = new RedPacketRecord();
        redPacketRecord.setUid(uid);
        redPacketRecord.setRed_packet_id(redPacketId);
        redPacketRecord.setImg_url("");
        redPacketRecord.setNick_name("aa");
        redPacketRecord.setAmount(amount);
        Date date = new Date();
        redPacketRecord.setCreate_time(date);
        redPacketRecord.setUpdate_time(date);
        redPacketMapper.addRedPacketRecord(redPacketRecord);
        String amountKey = redPacketId + redPacketAmount;
        String numKey = redPacketId + redPacketNum;
        //查出RedPacketInfo的数量，将总数量和总金额减去
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.setRed_packet_id(redPacketId);
        redPacketInfo.setUpdate_time(new Date());
        redPacketInfo.setRemaining_packet((Integer) redisTemplate.opsForValue().get(numKey));
        redPacketInfo.setRemaining_amount((Integer) redisTemplate.opsForValue().get(amountKey));
        redPacketMapper.updatePacketInfo(redPacketInfo);
    }


}
