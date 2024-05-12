package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.ipmanage.IpManageCompare;

import java.util.Date;

public class BaseIpAddressManageListParam extends BaseParam {
    //mAC地址
    private String mac;

    //厂商
    private String vendor;

    //接入设备
    private String accessEquip;

    //接入端口
    private String accessPort;

    //接入端口名称
    private String accessPortName;

    //更新时间
    private Date updateDate;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getAccessEquip() {
        return accessEquip;
    }

    public void setAccessEquip(String accessEquip) {
        this.accessEquip = accessEquip;
    }

    public String getAccessPort() {
        return accessPort;
    }

    public void setAccessPort(String accessPort) {
        this.accessPort = accessPort;
    }

    public String getAccessPortName() {
        return accessPortName;
    }

    public void setAccessPortName(String accessPortName) {
        this.accessPortName = accessPortName;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
