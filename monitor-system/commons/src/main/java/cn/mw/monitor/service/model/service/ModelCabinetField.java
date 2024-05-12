package cn.mw.monitor.service.model.service;

/**
 * 机房机柜属性字段
 */
public enum ModelCabinetField {
    INSTANCECODE("instanceCode","编号")
    ,DESC("desc","描述")
    ,RELATIONSITEROOM("relationSiteRoom", "所属机房")
    ,RELATIONSITEFLOOR("relationSiteFloor", "所属楼宇")
    ,POSITIONBYROOM("positionByRoom","机房位置")
    ,UNUM("UNum","U位数")
    ,LAYOUTDATA("layoutData" ,"布局")
    ,RELATIONSITECABINET("relationSiteCabinet","所属机柜") //机柜下属设备字段
    ,POSITIONBYCABINET("positionByCabinet" ,"机柜位置")  //机柜下属设备字段
    ,DEVICEHIGH("deviceHigh" ,"设备高度")
    ,DEVICEUNUM("deviceUNum" ,"设备U位数")
    ;

    private String field;
    private String fieldName;

    ModelCabinetField(String field , String fieldName){
        this.field = field;
        this.fieldName = fieldName;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
