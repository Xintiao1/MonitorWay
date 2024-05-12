package cn.mw.monitor.netflow.param;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className NetFlowTopData
 * @description 流量统计TopN数据
 * @date 2022/8/10
 */
@Data
public class NetFlowTopData {

    /**
     * 源IP
     */
    private String sourceIp;

    /**
     * 目的IP
     */
    private String dstIp;

    /**
     * 数据类型（1:B  2:KB  3:MB  4:GB）
     */
    private Integer dataType;

    /**
     * 入流量
     */
    private Double inData;

    /**
     * 数据单位
     */
    private String inUnit;

    /**
     * 出流量
     */
    private Double outData;

    /**
     * 数据单位
     */
    private String outUnit;

    /**
     * 总数据
     */
    private Double sumData;

    /**
     * 数据单位
     */
    private String unit;

    /**
     * 比较数据
     */
    private Double compareData;

    /**
     * 入数据包
     */
    private Integer inPackage;

    /**
     * 出数据包
     */
    private Integer outPackage;

    /**
     * 数据包合计
     */
    private Integer sumPackage;

    /**
     * 比较数据包
     */
    private Integer comparePackage;

    /**
     * 流量百分比
     */
    private Double netFlowPercent;

    /**
     * 数据包百分比
     */
    private Double packagePercent;

    /**
     * 流量百分比
     */
    private String netFlowPercentString;

    /**
     * 数据包百分比
     */
    private String packagePercentString;

    /**
     * 源port
     */
    private String sourcePort;

    /**
     * 目的port
     */
    private String dstPort;

    /**
     * 协议数据
     */
    private String protocol;
}
