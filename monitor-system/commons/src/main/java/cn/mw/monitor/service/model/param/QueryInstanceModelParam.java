package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/25 16:07
 */
@Data
@ApiModel
public class QueryInstanceModelParam extends BaseParam {
    @ApiModelProperty("页面id")
    private Integer pageId;
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("模型实例id")
    private Integer instanceId;
    @ApiModelProperty("模型实例ids")
    private List<Integer> instanceIds;
    @ApiModelProperty("模型id")
    private Integer modelId;
    @ApiModelProperty("模型Index")
    private String modelIndex;
    @ApiModelProperty("模型Indexs")
    private List<String> modelIndexs;
    //页面勾选时，传入的实例IdList
    @ApiModelProperty("实例勾选ids")
    private List<Integer> instanceIdSelectedList;
    //指定返回字段
    private List<String> fieldList;
    //指定不返回字段
    private List<String> noFieldList;

    private Integer monitorServerId;
    private Boolean isFlag;
    private Integer relationInstanceId;

    private Boolean monitorFlag;
    @ApiModelProperty("左侧树点击类型")
    private String type;
    @ApiModelProperty("模型的属性和属性值")
    List<AddModelInstancePropertiesParam> propertiesList;
    private List<String> pidList;
    //排序属性的类型
    private Integer sortFieldType;

    @ApiModelProperty("排序类型  0升序，1降序")
    private Integer sortType;

    private String sortField;

    private String fuzzyQuery;

    //导出的字段
    private List<String> header;
    //导出的字段名
    private List<String> headerName;
    //是否忽略数据权限控制  true忽略，可在定时任务时设置为true，避免没有userId导致报错
    private Boolean skipDataPermission;

    @ApiModelProperty("是否基础数据，true为基础设施下的数据，否为所有数据")
    private Boolean isBaseData;

    //是否资产视图
    private Boolean isAssetsView;

    //每个资产类型对应的id(资产视图树结构使用)
    private int assetsTypeId;
    @ApiModelProperty("模型实例ids")
    private List<String> modelInstanceIds;
    //西藏邮储下，双条件查询
    private Boolean doubleQuery;

    /**
     * 是否查看接口列表
     */
    private Integer netFlowInterface = 0;


    //外部关联字段是否将Id转为name显示
    private boolean isConvertVal;

    //是否定时任务启动
    private Boolean isTimeTask;
}
