package cn.mw.monitor.assets.param;

import lombok.Data;

@Data
public class MwTangibleAssetsSyncPush {
    //租户Id，默认100
    private Integer tenantId;
    //资产名称
    private String assetName;
    //IP地址
    private String assetPrivateIp;
    //公网Ip 非必填
    private String assetPublicIp;
    //资产状态  0：使用中，1：已下线
    private Integer assetStatus;
    //归属部门ID(根据提供的映射表，获取对应Id)
    private String deptOneId;
    //归属部门Id
    private String deptId;
    //IP类型   非必填
    private Integer ipType;
    //网络类型  1：互联网，2：内网，默认为2内网
    private Integer netType;
    //设备类型：1.云主机，2.物理服务器，3.防火墙（网络设备），4.路由器（网络设备）,
    // 5.交换机（网络设备）,6.IPS（安全设备）,7.WAF（安全设备），8.EDR（安全设备），
    // 9.态势感知，10.堡垒机，11.日志审计，12.数据库审计，13.网管，14.终端安全，15.其他
    private Integer deviceType;
    //直接负责人
    private String principal;
    //负责人电话
    private String principalTel;
    //非必填
    private String principalEmail;
    //根据物理区域映射表，获取对应Id
    private String assetArea;
    //物理位置 字符串(成都市浪潮云、成都市新华三政务云)
    private String assetLocation;
    //删除标识 0：存活，1：删除
    private Integer delFlag;
    //资产Id
    private String firmAssetId;
    //来源厂商名 写死：wlyw
    private String firmName = "wlyw";

}
