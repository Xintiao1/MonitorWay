package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author xhy
 * @date 2021/3/1 11:14
 */
@Data
@ApiModel
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ToString
public class AddModelInstancePropertiesParam {
    ///////////////////////////////////////////////////////////
    //es查询时，搜索字段
    @ApiModelProperty("模型属性类型:1字符串,2整形数字,7Ip地址,8日期类型,11机构/部门,12负责人,13用户组,17开关型")
    private Integer propertiesType;
    @ApiModelProperty("模型属性IndexId")//相当于字段名：assets_id、manufacturer、description...
    private String propertiesIndexId;
    @ApiModelProperty("模型属性的值")
    private String propertiesValue;//查询的值
    @ApiModelProperty("时间类型：开始时间")//查询为日期类型时，需要用到开始时间，结束时间
    private Date startTime;
    @ApiModelProperty("时间类型：结束时间")
    private Date endTime;

    ///////////////////////////////////////////////////////////


    @ApiModelProperty("资产的主键Id")
    private String tangibleId;
    @ApiModelProperty("模型的索引")
    private String modelIndex;
    @ApiModelProperty("模型的名称")
    private String modelName;
    @ApiModelProperty("模型的Id")
    private Integer modelId;
    @ApiModelProperty("模型的实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("模型的实例名称")
    private String modelInstanceName;
    private String modelDesc;

    @ApiModelProperty("模型属性Id")
    private Integer propertiesId;
    @ApiModelProperty("模型属性的名称")
    private String propertiesName;
    @ApiModelProperty("字段的查询类型：1等于，2包含，3大于，4小于，5不等于")
    private Integer queryWay;


    @ApiModelProperty("1且查询，0或查询；默认且查询")
    private Integer queryType;
    //结构体中的表字段Id
    private String propertiesInstanceStruct;

    //是否精准查询
    private boolean filterQuery;

    //是否是树结构查询；或者页面输入框搜索查询。
    private Boolean isTreeQuery;

    public void extractFromPropertyInfo(PropertyInfo propertyInfo) {
        if(propertyInfo!=null){
            this.propertiesId = propertyInfo.getPropertiesId();
            this.propertiesIndexId = propertyInfo.getIndexId();
            this.propertiesName = propertyInfo.getPropertiesName();
            this.propertiesType = propertyInfo.getPropertiesTypeId();
        }
    }

    public void extractFrom(AddUpdateTangAssetsParam addUpdateTangAssetsParam, Integer instanceId) {
        if (null == addUpdateTangAssetsParam) {
            return;
        }


        if (null != addUpdateTangAssetsParam.getSnmpV1AssetsDTO()
                && null != addUpdateTangAssetsParam.getSnmpV1AssetsDTO().getPort()) {
            this.propertiesType = ModelPropertiesType.STRUCE.getCode();
            this.propertiesName = MwModelViewCommonService.SNMPV1V2;
            this.propertiesIndexId = MwModelViewCommonService.SNMPV1V2;
            if (null != instanceId) {
                addUpdateTangAssetsParam.getSnmpV1AssetsDTO().setAssetsId(instanceId.toString());
            }
            this.propertiesValue = genStructData(addUpdateTangAssetsParam.getSnmpV1AssetsDTO());
        }

        if (null != addUpdateTangAssetsParam.getSnmpAssetsDTO()
                && null != addUpdateTangAssetsParam.getSnmpAssetsDTO().getPort()) {
            this.propertiesType = ModelPropertiesType.STRUCE.getCode();
            this.propertiesName = MwModelViewCommonService.SNMPV3;
            this.propertiesIndexId = MwModelViewCommonService.SNMPV3;
            if (null != instanceId) {
                addUpdateTangAssetsParam.getSnmpAssetsDTO().setAssetsId(instanceId.toString());
            }
            this.propertiesValue = genStructData(addUpdateTangAssetsParam.getSnmpAssetsDTO());
        }

        if (null != addUpdateTangAssetsParam.getAgentAssetsDTO()
                && null != addUpdateTangAssetsParam.getAgentAssetsDTO().getPort()) {
            this.propertiesName = MwModelViewCommonService.ZABBIX_AGENT;
            this.propertiesIndexId = MwModelViewCommonService.ZABBIX_AGENT;
            this.propertiesType = ModelPropertiesType.STRUCE.getCode();
            if (null != instanceId) {
                addUpdateTangAssetsParam.getAgentAssetsDTO().setAssetsId(instanceId.toString());
            }
            this.propertiesValue = genStructData(addUpdateTangAssetsParam.getAgentAssetsDTO());
        }

        if (null != addUpdateTangAssetsParam.getPortAssetsDTO()
                && null != addUpdateTangAssetsParam.getPortAssetsDTO().getPort()) {
            this.propertiesName = MwModelViewCommonService.ICMP;
            this.propertiesIndexId = MwModelViewCommonService.ICMP;
            this.propertiesType = ModelPropertiesType.STRUCE.getCode();
            if (null != instanceId) {
                addUpdateTangAssetsParam.getPortAssetsDTO().setAssetsId(instanceId.toString());
            }
            this.propertiesValue = genStructData(addUpdateTangAssetsParam.getPortAssetsDTO());
        }
        if (null != addUpdateTangAssetsParam.getMwIPMIAssetsDTO()
                && !Strings.isNullOrEmpty(addUpdateTangAssetsParam.getMwIPMIAssetsDTO().getPassword())) {
            this.propertiesName = MwModelViewCommonService.IMPI;
            this.propertiesIndexId = MwModelViewCommonService.IMPI;
            this.propertiesType = ModelPropertiesType.STRUCE.getCode();
            if (null != instanceId) {
                addUpdateTangAssetsParam.getMwIPMIAssetsDTO().setAssetsId(instanceId.toString());
            }
            this.propertiesValue = genStructData(addUpdateTangAssetsParam.getMwIPMIAssetsDTO());
        }
    }

    private String genStructData(Object obj) {
        List<Object> ret = new ArrayList<>();
        ret.add(obj);
        return JSON.toJSONString(ret);
    }
}
