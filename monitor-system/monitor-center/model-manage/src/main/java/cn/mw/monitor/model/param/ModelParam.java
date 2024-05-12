package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/7 9:25
 */
@Data
@ApiModel
public class ModelParam extends BaseParam {
    @ApiModelProperty("模型ID")
    private Integer modelId;
    @ApiModelProperty("模型名称")
    private String modelName;
    @ApiModelProperty("模型index")
    private String modelIndex;
    @ApiModelProperty("模型分组id")
    private Integer modelGroupId;
    @ApiModelProperty("模型类型id 1普通 2父")
    private Integer modelTypeId;
    private String pids;
    @ApiModelProperty("父模型pidList")
    private List<String> pidList;
    @ApiModelProperty("关系查询的node")
    private String relationQueryNode;
    @ApiModelProperty("模型分组deep")
    private Integer deep;
    @ApiModelProperty("属性级别 0:内置属性，1:自定义属性;其中内置属性不可删除")
    private Integer propertiesLevel;

    @ApiModelProperty("模型indexs")
    private List<String> modelIndexs;

    @ApiModelProperty("模型分组Ids")
    private List<String> modelGroupIds;

    private boolean queryParentModel;

    //用于基础资产数据，新增、修改查看、列表、对应展示的属性信息
    @ApiModelProperty("showType 0:新增，1:查看 2：修改 3：列表展示")
    private Integer showType;

}
