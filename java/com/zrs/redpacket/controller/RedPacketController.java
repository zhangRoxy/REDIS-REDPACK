package com.zrs.redpacket.controller;

import com.zrs.redpacket.service.RedPacketService;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class RedPacketController {
    @Resource
    RedPacketService redPacketService;

    @ResponseBody
    @RequestMapping("/addPacket")
    public String addPacket(Integer uid, Integer redPacketAmount, int redPacketNum) {
        return redPacketService.addPacket(uid,redPacketAmount,redPacketNum);
    }
    @ResponseBody
    @GetMapping("/getPacket")
    public String getPacket(Integer redPacketId) {
        return redPacketService.getPacket(redPacketId);
    }
    @ResponseBody
    @RequestMapping("/openPacket")
    public String openPacket(Integer uid, long redPacketId) {
        return redPacketService.openPacket(uid,redPacketId);
    }

}
