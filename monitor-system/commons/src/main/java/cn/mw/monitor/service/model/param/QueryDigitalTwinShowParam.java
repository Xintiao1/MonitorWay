package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.visualized.dto.MwDigitalTwinAlertDto;
import cn.mw.monitor.service.visualized.dto.MwDigitalTwinItemDto;
import cn.mw.monitor.service.visualized.param.MwDigitalTwinItemParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class QueryDigitalTwinShowParam {
    //设备Id
    private String id;
    //总机房数
    private Integer roomNum;
    //已使用机房数据
    private Integer roomUsed;
    //总机柜数据
    private Integer cabinetNum;
    //已使用机柜数据
    private Integer cabinetUsed;
    //总U位数
    private Integer allUNum;
    //已使用U位数
    private Integer usedUNum;
    //纳管设备总数量
    private Integer manageDeviceNum;
    //纳管设备类型种类
    private Integer deviceTypeNum;
    //设备类型名称
    private String deviceName;
    //网络设备数量
    private Integer netWorkDeviceNum;
    //服务器设备数量
    private Integer serverDeviceNum;
    //存储设备数量
    private Integer storageDeviceNum;
    //地址位置
    private String address;
    //当前页面位置
    private String currentLocation;
    //温度
    private String temp;
    //天气
    private String weather;
    //页面类型 building、room、cabinet、device
    private String pageType;
    //告警信息
    private MwDigitalTwinAlertDto alarmInfo;

    //资产设备信息
    private MwDigitalTwinItemDto deviceInfo;

}
