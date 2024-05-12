package cn.mw.monitor.service.scan.dto;

import cn.mw.monitor.service.scan.model.IpInfo;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class IPInfoDTO{
    public static final String MORE_DATA = "...";
    /*
     * ip地址
     */
    private String ip;

    /*
     * mac地址
     */
    private String mac;

    /*
     * 接入设备名称
     */
    private String linkDeviceName;

    /*
     * 接入设备描述
     */
    private String linkDeviceDesc;

    /*
     * 接入接口
     */
    private int port;

    /*
     * 接入接口名称
     */
    private String portName;

    /*
     * 是否在线
     */
    private boolean online;

    /*
     * ip冲突信息
     */
    private List<IpInfo> conflictsIp = new ArrayList<>();

}
