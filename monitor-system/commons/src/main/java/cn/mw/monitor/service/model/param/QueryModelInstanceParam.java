package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2021/2/25 10:14
 */
@Data
@ApiModel
public class QueryModelInstanceParam extends BaseParam {
    @ApiModelProperty("页面id")
    private Integer pageId;
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型实例id")
    private Integer modelInstanceId;
    @ApiModelProperty("依附实例Id")
    private Integer relationInstanceId;
    @ApiModelProperty("实例ids")
    private List<Integer> instanceIdList;
    @ApiModelProperty("实例勾选ids")
    private List<Integer> instanceIdSelectedList;
    @ApiModelProperty("模型实例名称")
    private String modelInstanceName;
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型类型id")
    private Integer modelTypeId;
    @ApiModelProperty("父类型pids")
    private String pids;

    @ApiModelProperty("左侧树点击类型")
    private String type;

    @ApiModelProperty("模型分类id")
    private Integer modelGroupId;

    @ApiModelProperty("字段的查询类型：1等于，2包含，3大于，4小于，5不等于")
    private Integer queryWay;

    @ApiModelProperty("模型的属性和属性值")
    List<AddModelInstancePropertiesParam> propertiesList;

    private List<String> pidList;

    private List<SortFieldParam> sortFiledList;

    private String sortField;

    //排序属性的类型
    private Integer sortFieldType;

    @ApiModelProperty("排序类型  0升序，1降序")
    private Integer sortType;

    //需要做唯一check的字段数据
    private List<String> fieldList;

    //导出的字段
    private List<String> header;
    //导出的字段名
    private List<String> headerName;

    private Boolean isFlag;

    //西藏邮储下，双条件查询
    private Boolean doubleQuery;

    private List<String> modelIndexs;

    @ApiModelProperty("模型实例ids")
    private List<String> modelInstanceIds;

    //是否定时任务启动
    private Boolean isTimeTask;

}
