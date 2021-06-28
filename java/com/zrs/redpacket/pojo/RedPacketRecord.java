package com.zrs.redpacket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedPacketRecord {
    private Integer id;

    private Integer amount;

    private String nick_name;

    private String img_url;

    private Integer uid;

    private Long red_packet_id;

    private Date create_time;

    private Date update_time;
}
