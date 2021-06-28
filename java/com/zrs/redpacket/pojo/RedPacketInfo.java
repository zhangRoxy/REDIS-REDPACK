package com.zrs.redpacket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedPacketInfo {
    private Integer id;

    private Long red_packet_id;

    private Integer total_amount;

    private Integer total_packet;

    private Integer remaining_amount;

    private Integer remaining_packet;

    private Integer uid;

    private Date create_time;

    private Date update_time;
}
