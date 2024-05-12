package cn.mw.monitor.netflow.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className NetFlowCapResult
 * @description 流量明细返回结果
 * @date 2023/2/14
 */
@Data
public class NetFlowCapResult {

    /**
     * 唯一ID
     */
    private String id;

    /**
     * 源IP
     */
    private String sourceIp;

    /**
     * 目的IP
     */
    private String destIp;

    /**
     * 源端口
     */
    private int sourcePort;

    /**
     * 目的端口
     */
    private int destPort;

    /**
     * 抓取时间
     */
    private long capTime;

    /**
     * 总字节长度
     */
    private int sumBytes;

    /**
     * 总数据包
     */
    private int sumPackage;

    /**
     * 最大时间
     */
    private long maxTime;

    /**
     * 最小时间
     */
    private long minTime;

    /**
     * 最大时间
     */
    private String maxTimeString;

    /**
     * 最小时间
     */
    private String minTimeString;

    /**
     * 请求头集合
     */
    private List<String> headerList;

    /**
     * uri集合
     */
    private List<String> uriList;

    public void addSumBytes(int length) {
        this.sumBytes += length;
    }

    public void addPackage() {
        this.sumPackage += 1;
    }
}
