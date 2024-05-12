package cn.mw.monitor.netflow.entity;

import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 * @author gui.quanwang
 * @className NetFlowCapEntity
 * @description ES从captcp抓取的数据
 * @date 2023/2/14
 */
@Data
public class NetFlowCapEntity {

    /**
     * ESid
     */
    private String id;


    /**
     * 源IP
     */
    private IP srcIp;

    /**
     * 目的IP
     */
    private IP destIp;

    /**
     * 源端口
     */
    private int srcPort;

    /**
     * 目的端口
     */
    private int destPort;

    /**
     * 抓取时间
     */
    @JSONField(format = "yyyyMMddHHmmss")
    private Date capTime;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyyMMddHHmmss")
    private Date createTime;

    /**
     * 创建时间（字符串）
     */
    private String createTimeString;

    /**
     * 字节长度
     */
    private int length;

    /**
     * 协议名称
     */
    private String protoType;

    /**
     * 应用
     */
    private String application;

    /**
     * 请求头
     */
    private String header;

    /**
     * 请求uri
     */
    private String uri;

    /**
     * 请求体内容
     */
    private String body;

    /**
     * 目标MAC地址
     */
    private String destMac;

    /**
     * 源目标MAC地址
     */
    private String srcMac;

    public String getHeader() {
        return StringUtils.isEmpty(header) ? "" : header;
    }

    public String getUri() {
        return StringUtils.isEmpty(uri) ? "" : uri;
    }

    public String getSrcIp() {
        return srcIp.getValue();
    }

    public String getDestIp() {
        return destIp.getValue();
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = new IP(srcIp);
    }

    public void setDestIp(String destIp) {
        this.destIp = new IP(destIp);
    }
}
