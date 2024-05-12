package cn.mw.monitor.ipaddressmanage.service.impl;

public enum IpIcmpType {
    //OnlyIcmpDetect: 单个ip通过icmp获取在线信息
    //DeviceInfoDetect: 单个ip通过icmp获取在线信息,通过snmp获取额外信息
    OnlyIcmpDetect, DeviceInfoDetect
}
