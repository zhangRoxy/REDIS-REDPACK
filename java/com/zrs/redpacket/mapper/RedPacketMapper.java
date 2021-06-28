package com.zrs.redpacket.mapper;

import com.zrs.redpacket.pojo.RedPacketInfo;
import com.zrs.redpacket.pojo.RedPacketRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RedPacketMapper {
    Integer addRedPacket(RedPacketInfo redPacketInfo);
    Integer addRedPacketRecord(RedPacketRecord redPacketRecord);
    RedPacketInfo getRedPacket(Integer id);
    Integer updatePacketInfo(RedPacketInfo redPacketInfo);
}
