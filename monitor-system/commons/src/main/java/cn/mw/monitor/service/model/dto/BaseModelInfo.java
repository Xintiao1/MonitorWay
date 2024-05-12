package cn.mw.monitor.service.model.dto;

import lombok.Data;

/**
 * @ClassName BaseModelInfo
 * @Description 模型数据
 * @Author gengjb
 * @Date 2023/2/12 14:06
 * @Version 1.0
 **/
@Data
public class BaseModelInfo {

    //模型索引
    private String modelIndex;

    private Integer modelId;

    private String groupNodes;

    private Integer modelInstanceId;

    private String instanceName;
    //关联机房名称
    private String relationSiteRoom;
    //关联机柜名称
    private String relationSiteCabinet;
    //关联的业务系统
    private String modelSystem;
    //业务分类
    private String modelClassify;
    //设备U位
    private String positionByCabinet;
    //告警标签
    private String modelTag;
    //告警区域（机房机柜U位数拼接）
    private String modelArea;

    private String relationArea;

    private String FromUser;

    private String AlarmEventName;

    //关联的业务系统
    private String modelSystemName;

    private String modelClassifyName;
    //关联机房名称
    private String relationSiteRoomName;
    //关联机柜名称
    private String relationSiteCabinetName;

    //是否关键设备
    private Boolean isKeyDevices;

    private String webUrl;

    //设备型号
    private String deviceModel;
}
