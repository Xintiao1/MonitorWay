package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dto.ModelPropertiesDto;
import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.model.param.AddAndUpdateModelParam;
import cn.mw.monitor.service.model.param.PropertiesValueParam;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.PropertyTypeConvert;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/*
 * 修改模型数据结构, 需要把原有的mw_cmdbmd_properties, mw_cmdbmd_properties_value
 * 两个表的数据转化, 存入mw_cmdbmd_manage的prop_info字段
 */
@Service
@Slf4j
public class MwModelTransfer {
    @Resource
    private MwModelManageDao mwModelManageDao;

    @Transactional
    public void transfer(){

        List<ModelInfo> allModelInfo = mwModelManageDao.selectAllModelInfo();
        PropertyInfo currPropertyInfo = null;
        ModelPropertiesDto curModelPropertiesDto = null;

        try {
            for (ModelInfo modelInfo : allModelInfo) {
                AddAndUpdateModelParam addAndUpdateModelParam = new AddAndUpdateModelParam();
                Map map = new HashMap();
                map.put("modelId", modelInfo.getModelId());
                List<ModelPropertiesDto> modelPropertiesDtoList = mwModelManageDao.selectModelPropertiesList(map);
                for (ModelPropertiesDto modelPropertiesDto : modelPropertiesDtoList) {
                    PropertyInfo propertyInfo = new PropertyInfo();

                    currPropertyInfo = propertyInfo;
                    curModelPropertiesDto = modelPropertiesDto;

                    transferProperty(modelPropertiesDto, propertyInfo);
                    addAndUpdateModelParam.addPropertyInfo(propertyInfo);
                }

                addAndUpdateModelParam.setModelId(modelInfo.getModelId());
                mwModelManageDao.updateModel(addAndUpdateModelParam);
            }
        }catch (Exception e){
            if(null != currPropertyInfo){
                log.info(currPropertyInfo.toString());
            }

            if(null != curModelPropertiesDto){
                log.info(curModelPropertiesDto.toString());
            }
            throw e;
        }

        /*
        for(ModelInfo select : allModelInfo){
            Integer modelId = select.getModelId();
            if((null == select.getPropertyInfos() || select.getPropertyInfos().size() == 0)
            && null != modelId){
                Map map = new HashMap();
                map.put("modelId" ,modelId);
                List<ModelPropertiesDto> modelPropertiesDtoList = mwModelManageDao.selectModelPropertiesList(map);
                for(ModelPropertiesDto modelPropertiesDto : modelPropertiesDtoList){
                    PropertyInfo propertyInfo = new PropertyInfo();
                    transferProperty(modelPropertiesDto ,propertyInfo);
                    addAndUpdateModelParam.addPropertyInfo(propertyInfo);
                }

                addAndUpdateModelParam.setModelId(modelId);
                mwModelManageDao.updateModel(addAndUpdateModelParam);
            }
        }
         */
    }

    private void transferProperty(ModelPropertiesDto modelPropertiesDto , PropertyInfo propertyInfo){
        BeanUtils.copyProperties(modelPropertiesDto ,propertyInfo);
        if(null != modelPropertiesDto.getPropertiesValue()){
            BeanUtils.copyProperties(modelPropertiesDto.getPropertiesValue() ,propertyInfo);
        }
        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
        String value = null;
        String defaultValue = null;
        PropertiesValueParam propertiesValueParam = modelPropertiesDto.getPropertiesValue();
        if(null != propertiesValueParam) {
            switch (type) {
                case SINGLE_ENUM:
                case MULTIPLE_ENUM:
                    if (StringUtils.isNotEmpty(propertiesValueParam.getDropOpStr())) {
                        value = propertiesValueParam.getDropOpStr();
                    }
                    break;
                case RELATION_ENUM:
                    if (StringUtils.isNotEmpty(propertiesValueParam.getDropArrObjStr())) {
                        value = propertiesValueParam.getDropArrObjStr();
                    }
                    break;
                case STRUCE:
                    List<ModelPropertiesStructDto> structList = mwModelManageDao.getProperticesStructInfo(modelPropertiesDto.getModelId(), modelPropertiesDto.getIndexId());
                    if (null != structList) {
                        value = type.getConverter().strValue(modelPropertiesDto.getPropertiesStruct());
                    }
                    break;
                default:
                    String pDefaultValue = propertiesValueParam.getDefaultValue();
                    PropertyTypeConvert convert = type.getConverter();
                    if (null != pDefaultValue && null != convert) {
                        if (convert.matchType(pDefaultValue)) {
                            defaultValue = convert.strValue(pDefaultValue);
                        } else {
                            defaultValue = pDefaultValue;
                        }
                    }
            }

            if (StringUtils.isNotEmpty(propertiesValueParam.getGangedValueListStr())) {
                propertyInfo.setGangedValueListStr(propertiesValueParam.getGangedValueListStr());
            }

            if (null == defaultValue) {
                if (StringUtils.isNotEmpty(propertiesValueParam.getDefaultValueListStr())) {
                    defaultValue = propertiesValueParam.getDefaultValueListStr();
                }

                if (StringUtils.isNotEmpty(propertiesValueParam.getDefaultValue())) {
                    defaultValue = propertiesValueParam.getDefaultValue();
                }
            }
        }

        propertyInfo.setPropertyValue(value);
        propertyInfo.setDefaultValue(defaultValue);
    }
}
