package cn.mw.xiangtai.plugin.domain.dto;

import lombok.Data;

@Data
public class AttackAddressDataDTO {
    /**
     * 源地址
     */
    private String srcIp;

    /**
     * 目的地址
     */
    private String dstIp;

    /**
     * 攻击次数
     */
    private Integer count;

    private String status;

    private String srcIpArea;

    private String dstIpArea;
}
