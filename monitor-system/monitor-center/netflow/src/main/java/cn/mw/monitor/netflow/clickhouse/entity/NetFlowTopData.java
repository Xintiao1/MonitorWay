package cn.mw.monitor.netflow.clickhouse.entity;

import cn.mw.monitor.netflow.annotation.ClickHouseColumn;
import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/8/1
 * @Description 流量统计TopN数据
 */
@Data
public class NetFlowTopData implements Serializable {


    /**
     * 源IP
     */
    @ClickHouseColumn
    private String sourceIp;

    /**
     * 目的IP
     */
    @ClickHouseColumn
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
//    @ClickHouseColumn
    private Double sumData;

    /**
     * 数据单位
     */
    private String unit;

    /**
     * 比较数据
     */
    @ClickHouseColumn
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
//    @ClickHouseColumn
    private Integer sumPackage;

    /**
     * 比较数据包
     */
    @ClickHouseColumn
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
    @ClickHouseColumn
    private String sourcePort;

    /**
     * 目的port
     */
    @ClickHouseColumn
    private String dstPort;

    /**
     * 协议数据
     */
    @ClickHouseColumn
    private String protocol;

    public NetFlowTopData() {
        this.inData = 0D;
        this.outData = 0D;
        this.sumData = 0D;
        this.inPackage = 0;
        this.outPackage = 0;
        this.sumPackage = 0;
        this.compareData = 0D;
        this.comparePackage = 0;
    }

    public void setSumData(Double sumData) {
        this.sumData = sumData;
    }

    public void setSumPackage(Integer sumPackage) {
        this.sumPackage = sumPackage;
    }

}
