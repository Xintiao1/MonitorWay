package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.param.CabinetLayoutDataParam;
import lombok.Data;

@Data
public class QueryAlertFieldParam {
    private String modelInstanceId;
    //关联机房Id
    private String relationSiteRoom;
    private String relationSiteRoomName;
    //关联机柜Id
    private String relationSiteCabinet;
    private String relationSiteCabinetName;
    //设备U位
    private CabinetLayoutDataParam positionByCabinet;
    private String positionByCabinetName;
    //关联的业务系统
    private String modelSystem;
    private String modelSystemName;
    //关联的业务分类
    private String modelClassify;
    private String modelClassifyName;
    //告警标签
    private String modelTag;
    //告警区域（机房机柜U位数拼接）
    private String modelArea;
}
