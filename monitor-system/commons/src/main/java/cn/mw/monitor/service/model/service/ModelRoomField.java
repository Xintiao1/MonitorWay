package cn.mw.monitor.service.model.service;

/**
 * 机房机柜属性字段
 */
public enum ModelRoomField {
    INSTANCECODE("instanceCode","编号"),
    DESC("desc","描述"),
    ROWNUM("rowNum", "行数")
    ,COLNUM("colNum","列数")
    ,LAYOUTDATA("layoutData" ,"布局")
    ;

    private String field;
    private String fieldName;

    ModelRoomField(String field , String fieldName){
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
