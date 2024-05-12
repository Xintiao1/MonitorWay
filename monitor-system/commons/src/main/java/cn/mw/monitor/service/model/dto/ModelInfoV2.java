package cn.mw.monitor.service.model.dto;

import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lijubo
 * @date 2023/1/11
 *
 */

@Data
@ApiModel
public class ModelInfoV2 extends BaseProcessParam {
    @ApiModelProperty("模型Id")
    private Long modelId;

    @ApiModelProperty("模型名称")
    @NotNull
    private String modelName;

    @ApiModelProperty("模型描述")
    private String modelDesc;

    @ApiModelProperty("模型索引")
    private String modelIndex;

    @ApiModelProperty("模型类型ID  1普通模型 2 父模型")
    @NotNull
    private Integer modelTypeId;

    @ApiModelProperty("模型分组ID ")
    private Long modelGroupId;

    @ApiModelProperty("模型图标")
    @NotNull
    private String modelIcon;

    @ApiModelProperty("是否显示")
    private Boolean isShow;

    @ApiModelProperty("节点深度  如果普通和父模型 都是1 子模型根据深度来")
    private Integer deep=1;

    @ApiModelProperty("节点")
    private String nodes;

    @ApiModelProperty("模型分组节点")
    private String groupNodes;

    @ApiModelProperty("父节点id  如果普通和父模型 都是0  子模型就是用户自己选的父模型id ")
    private Long pid=0l;

    @ApiModelProperty("是否根节点  如果普通和父模型 是true ")
    private Boolean isNode;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    @ApiModelProperty("模型级别 0:内置模型，1:自定义模型;其中内置模型不可删除")
    private Integer modelLevel;

    private List<String> pidList;

    private String pids;

    @ApiModelProperty("模型视图（1：机房视图，2机柜视图）")
    private Integer modelView;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    //属性信息
    private String propertyInfoJsonStr;
    private List<PropertyInfoV2> propertyInfos;

    public String getPropertyInfoJsonStr(){
        String json = JSON.toJSONString(propertyInfos);
        return json;
    }

    public void setPropertyInfoJsonStr(String json){
        propertyInfos = JSONArray.parseArray(json ,PropertyInfoV2.class);
    }

    public Map<String ,PropertyInfoV2> getPropertyInfoMapByName(){
        if(null != propertyInfos){
            return propertyInfos.stream().collect(Collectors.toMap(PropertyInfoV2::getPropertiesName, Function.identity()));
        }
        return null;
    }

    public Map<String ,PropertyInfoV2> getPropertyInfoMapByIndexId(){
        if(null != propertyInfos){
            return propertyInfos.stream().collect(Collectors.toMap(PropertyInfoV2::getIndexId, Function.identity()));
        }
        return null;
    }
    public Map<String ,PropertyInfoV2> getPropertyInfoMapByPropertiesType(){
        if(null != propertyInfos){
            return propertyInfos.stream().collect(Collectors.toMap(PropertyInfoV2::getPropertiesType, Function.identity()));
        }
        return null;
    }
    public int getMaxPropertySort(){
        int maxSort = 0;
        if(null != propertyInfos){
            for(PropertyInfoV2 propertyInfo: propertyInfos){
                if(propertyInfo.getSort() > maxSort){
                    maxSort = propertyInfo.getSort();
                }
            }
        }

        return maxSort;
    }

    public void addPropertyInfo(PropertyInfoV2 propertyInfo){
        if(null == propertyInfos){
            propertyInfos = new ArrayList();
        }
        propertyInfos.add(propertyInfo);
    }

    public void addAllPropertyInfo(List<PropertyInfoV2> propertyInfoList){
        if(null == propertyInfos){
            propertyInfos = new ArrayList();
        }

        if(null != propertyInfoList){
            propertyInfos.addAll(propertyInfoList);
        }
    }

    public List<PropertyInfoV2> findGangedList(){
        List<PropertyInfoV2> list = new ArrayList<>();
        if(null != propertyInfos){
            for(PropertyInfoV2 propertyInfo : propertyInfos){
                ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
                switch (type){
                    case RELATION_ENUM:
                        list.add(propertyInfo);
                        break;
                    default:
                }
            }
        }
        return list;
    }

    public List<PropertyInfoV2> findPropertiesByInstanceFuzzyQuery(){
        List<PropertyInfoV2> list = new ArrayList<>();
        if(null != propertyInfos){
            for(PropertyInfoV2 propertyInfo : propertyInfos){
                ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
                if(propertyInfo.getIsShow() && type == ModelPropertiesType.STRING){
                    list.add(propertyInfo);
                }
            }
        }
        return list;
    }

    public List<PropertyInfoV2> filterProperties(){
        List<PropertyInfoV2> list = new ArrayList<>();
        if(null != propertyInfos){
            for(PropertyInfoV2 propertyInfo : propertyInfos){
                ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
                if(propertyInfo.getIsShow() && type == ModelPropertiesType.STRING){
                    list.add(propertyInfo);
                }
            }
        }
        return list;
    }

    public Map<String ,PropertyInfoV2> genPropertyMapByIndexId(){
        Map<String ,PropertyInfoV2> map = new HashMap<>();
        if(null != propertyInfos){
            for(PropertyInfoV2 propertyInfo : propertyInfos){
                map.put(propertyInfo.getIndexId() ,propertyInfo);
            }
        }
        return map;
    }
}
