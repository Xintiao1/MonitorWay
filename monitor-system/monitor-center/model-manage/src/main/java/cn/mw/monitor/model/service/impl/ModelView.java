package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dto.ModelType;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelCabinetField;
import cn.mw.monitor.service.model.service.ModelRoomField;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;

import java.util.ArrayList;
import java.util.List;

import static cn.mw.monitor.service.model.service.ModelCabinetField.*;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.INSTANCE_NAME_FIELD;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.INSTANCE_NAME_KEY;

public enum ModelView {
    Default(0 ,"默认视图" ,(modelInfo)->{
        List<PropertyInfo> list = new ArrayList<>();
        ModelType type = ModelType.valueOf(modelInfo.getModelTypeId());
        if(type == ModelType.COMMON_MODEL){
            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setPropertiesLevel(0);
            propertyInfo.setSort(0);
            propertyInfo.setIndexId(INSTANCE_NAME_KEY);
            propertyInfo.setPropertiesName(INSTANCE_NAME_FIELD);
            propertyInfo.setPropertiesType("默认属性");
            propertyInfo.setPropertiesTypeId(1);
            propertyInfo.setIsMust(true);
            propertyInfo.setIsOnly(false);
            propertyInfo.setIsRead(false);
            propertyInfo.setIsShow(true);
            propertyInfo.setIsLookShow(true);
            propertyInfo.setIsEditorShow(true);
            propertyInfo.setIsInsertShow(true);
            propertyInfo.setIsListShow(true);
            list.add(propertyInfo);
            setDefaultSettingValue(list,1);
        }

        return list;
    })
    ,MachineRoom(1 ,"机房视图" ,(modelInfo)->{
        List<PropertyInfo> list = new ArrayList<>();
        String[] strName = {INSTANCE_NAME_FIELD, ModelRoomField.INSTANCECODE.getFieldName(), ModelRoomField.DESC.getFieldName(),
                ModelRoomField.ROWNUM.getFieldName(),ModelRoomField.COLNUM.getFieldName(),ModelRoomField.LAYOUTDATA.getFieldName()};
        String[] strField = {INSTANCE_NAME_KEY, ModelRoomField.INSTANCECODE.getField(), ModelRoomField.DESC.getField(),
                ModelRoomField.ROWNUM.getField(),ModelRoomField.COLNUM.getField(),ModelRoomField.LAYOUTDATA.getField()};
        for (int x = 0; x < strName.length; x++) {
            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setPropertiesLevel(0);
            propertyInfo.setSort(x);
            propertyInfo.setIndexId(strField[x]);
            propertyInfo.setPropertiesName(strName[x]);
            propertyInfo.setPropertiesType("默认属性");
            if (ModelRoomField.ROWNUM.getField().equals(strField[x]) || ModelRoomField.COLNUM.getField().equals(strField[x])) {
                //布局数据设为数值整形结构
                propertyInfo.setPropertiesTypeId(2);
            } else if (ModelRoomField.LAYOUTDATA.getField().equals(strField[x])) {
                //布局数据设为数组结构
                propertyInfo.setPropertiesTypeId(16);
            } else {
                propertyInfo.setPropertiesTypeId(1);
            }
            if (ModelRoomField.DESC.getField().equals(strField[x])) {
                propertyInfo.setIsMust(false);
            } else {
                propertyInfo.setIsMust(true);
            }
            setDefaultPropertyValue(propertyInfo);
            list.add(propertyInfo);
        }
        setDefaultSettingValue(list,strName.length);
        return list;
    })
    ,Cabinet(2 ,"机柜视图" ,(modelInfo) ->{
        List<PropertyInfo> list = new ArrayList<>();
        String[] strName = {INSTANCE_NAME_FIELD, ModelCabinetField.INSTANCECODE.getFieldName(), ModelCabinetField.DESC.getFieldName(),
                ModelCabinetField.RELATIONSITEROOM.getFieldName(),ModelCabinetField.POSITIONBYROOM.getFieldName(),
                ModelCabinetField.UNUM.getFieldName(),ModelCabinetField.LAYOUTDATA.getFieldName()};
        String[] strField = {INSTANCE_NAME_KEY, ModelCabinetField.INSTANCECODE.getField(),
                ModelCabinetField.DESC.getField(),ModelCabinetField.RELATIONSITEROOM.getField(),ModelCabinetField.POSITIONBYROOM.getField(),
                ModelCabinetField.UNUM.getField(),ModelCabinetField.LAYOUTDATA.getField()};
        for (int x = 0; x < strName.length; x++) {
            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setPropertiesLevel(0);
            propertyInfo.setSort(x);
            propertyInfo.setIndexId(strField[x]);
            propertyInfo.setPropertiesName(strName[x]);
            propertyInfo.setPropertiesType("默认属性");
            if (ModelCabinetField.UNUM.getField().equals(strField[x])) {
                //布局数据设为整形数值结构
                propertyInfo.setPropertiesTypeId(2);
            } else if (ModelCabinetField.POSITIONBYROOM.getField().equals(strField[x]) || ModelCabinetField.LAYOUTDATA.getField().equals(strField[x])) {
                //布局数据设为数组结构
                propertyInfo.setPropertiesTypeId(16);
            } else if (ModelCabinetField.RELATIONSITEROOM.getField().equals(strField[x])) {
                //所属机房为外部关联类型
                propertyInfo.setPropertiesTypeId(5);
            } else {
                propertyInfo.setPropertiesTypeId(1);
            }
            if (ModelCabinetField.DESC.getField().equals(strField[x])) {
                propertyInfo.setIsMust(false);
            } else {
                propertyInfo.setIsMust(true);
            }
            setDefaultPropertyValue(propertyInfo);
            list.add(propertyInfo);
        }
        setDefaultSettingValue(list,strName.length);
        return list;
    })
    ,CabinetRelationDevice(3 ,"机柜下属设备视图" ,(modelInfo) ->{
        List<PropertyInfo> list = new ArrayList<>();
        String[] strName = {INSTANCE_NAME_FIELD,RELATIONSITEROOM.getFieldName(), POSITIONBYROOM.getFieldName(), RELATIONSITECABINET.getFieldName(), POSITIONBYCABINET.getFieldName()};
        String[] strField = {INSTANCE_NAME_KEY,RELATIONSITEROOM.getField(), POSITIONBYROOM.getField(), RELATIONSITECABINET.getField(), POSITIONBYCABINET.getField()};
        for (int x = 0; x < strName.length; x++) {
            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setPropertiesLevel(0);
            propertyInfo.setIndexId(strField[x]);
            propertyInfo.setPropertiesId(x);
            propertyInfo.setSort(x);
            propertyInfo.setPropertiesName(strName[x]);
            propertyInfo.setPropertiesType("默认属性");
            setDefaultPropertyValue(propertyInfo);
            propertyInfo.setIsMust(false);
            if (POSITIONBYCABINET.getField().equals(strField[x]) || POSITIONBYROOM.getField().equals(strField[x])) {
                //布局数据设为数组结构
                propertyInfo.setPropertiesTypeId(16);
            } else if (RELATIONSITECABINET.getField().equals(strField[x]) || RELATIONSITEROOM.getField().equals(strField[x])) {
                //所属机房,所属机柜为外部关联类型
                propertyInfo.setPropertiesTypeId(5);
            } else {
                propertyInfo.setPropertiesTypeId(1);
            }

            list.add(propertyInfo);
        }
        setDefaultSettingValue(list,strName.length);
        return list;
    })
    ;

    private int type;
    private String desc;
    private PropertyAdd propertyAdd;


    ModelView(int type ,String desc ,PropertyAdd propertyAdd){
        this.type = type;
        this.desc = desc;
        this.propertyAdd = propertyAdd;
    }

    public static ModelView valueOf(int type){
        for(ModelView modelView:ModelView.values()){
            if(type == modelView.getType()){
                return modelView;
            }
        }
        return Default;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public PropertyAdd propertyAdd() {
        return propertyAdd;
    }

    private static void setDefaultPropertyValue(PropertyInfo propertyInfo){
        propertyInfo.setIsMust(false);
        propertyInfo.setIsOnly(false);
        propertyInfo.setIsRead(false);
        propertyInfo.setIsShow(true);
        propertyInfo.setIsLookShow(true);
        propertyInfo.setIsEditorShow(true);
        propertyInfo.setIsInsertShow(true);
        propertyInfo.setIsListShow(true);
    }

    private static void setDefaultSettingValue(List<PropertyInfo> list,Integer sortIndex){
        String[] strName = {"运维监控", "自动化", "日志管理", "配置管理"};
        String[] strField = {MwModelViewCommonService.OPERATION_MONITOR,MwModelViewCommonService.AUTO_MANAGE,
                             MwModelViewCommonService.LOG_MANAGE,MwModelViewCommonService.PROP_MANAGE};
        for (int x = 0; x < strName.length; x++) {
            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setPropertiesLevel(0);
            propertyInfo.setSort(sortIndex+x);
            propertyInfo.setIndexId(strField[x]);
            propertyInfo.setPropertiesName(strName[x]);
            propertyInfo.setPropertiesType("默认属性");
            propertyInfo.setPropertiesTypeId(17);
            propertyInfo.setIsMust(false);
            propertyInfo.setIsOnly(false);
            propertyInfo.setIsRead(false);
            propertyInfo.setIsShow(false);
            propertyInfo.setIsLookShow(true);
            propertyInfo.setIsEditorShow(true);
            propertyInfo.setIsInsertShow(true);
            propertyInfo.setIsListShow(false);
            list.add(propertyInfo);
        }
    }
}
