package cn.mw.monitor.netflow.entity;

import cn.mw.monitor.netflow.annotation.ClickHouseColumn;
import cn.mwpaas.common.utils.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guiquanwnag
 * @datetime 2023/7/19
 * @Description 流量明细返回实体类（用于clickhouse）
 */
public class CKNetflowCapEntity implements Serializable {


    @ClickHouseColumn(name = "srcMac")
    private String srcMac;

    @ClickHouseColumn(name = "srcIp")
    private IP srcIp;

    @ClickHouseColumn(name = "srcPort", type = "Int32")
    private Integer srcPort;

    @ClickHouseColumn(name = "destMac")
    private String destMac;

    @ClickHouseColumn(name = "destIp")
    private IP destIp;

    @ClickHouseColumn(name = "destPort", type = "Int32")
    private Integer destPort;

    @ClickHouseColumn(name = "length", type = "Int32")
    private Integer length;

    @ClickHouseColumn(name = "application")
    private String application;

    @ClickHouseColumn(name = "uri")
    private String uri;

    @ClickHouseColumn(name = "header")
    private String header;

    @ClickHouseColumn(name = "host")
    private String host;

    @ClickHouseColumn(name = "body")
    private String body;

    //抓取时间
    @ClickHouseColumn(name = "capTime", type = "Int64")
    private Long capTime;

    //协议类型
    @ClickHouseColumn(name = "protoType")
    private String protoType;

    @ClickHouseColumn(name = "createTime", type = "Int64")
    private Long createTime;

    @ClickHouseColumn(name = "delayTime", type = "Int64")
    private Long delayTime;

    private String createTimeString;

    private String capTimeString;


    public String getSrcMac() {
        return srcMac;
    }

    public void setSrcMac(String srcMac) {
        this.srcMac = srcMac;
    }

    public String getSrcIp() {
        return srcIp.getValue();
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = new IP(srcIp);
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public String getDestMac() {
        return destMac;
    }

    public void setDestMac(String destMac) {
        this.destMac = destMac;
    }

    public String getDestIp() {
        return destIp.getValue();
    }

    public void setDestIp(String destIp) {
        this.destIp = new IP(destIp);
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getCapTime() {
        return capTime;
    }

    public void setCapTime(long capTime) {
        Date date = new Date(capTime);
        this.capTime = capTime;
        this.capTimeString = DateUtils.formatDateTime(date);
    }

    public String getProtoType() {
        return protoType;
    }

    public void setProtoType(String protoType) {
        this.protoType = protoType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        Date date = new Date(createTime);
        this.createTime = createTime;
        this.createTimeString = DateUtils.formatDateTime(date);
    }

    public String getCreateTimeString() {
        return createTimeString;
    }

    public void setCreateTimeString(String createTimeString) {
        this.createTimeString = createTimeString;
    }

    public String getCapTimeString() {
        return capTimeString;
    }

    public void setCapTimeString(String capTimeString) {
        this.capTimeString = capTimeString;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }
}
