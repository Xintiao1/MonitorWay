package cn.mw.monitor.model.param;

import cn.mw.monitor.graph.modelAsset.LastData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author qzg
 * @date 2022/3/9
 */
@Data
@ApiModel
public class QueryInstanceRelationToPoParam {
    @ApiModelProperty("Id")
    private Integer id;
    @ApiModelProperty("当前模型实例参数")
    private QueryInstanceRelationsParam owmRelationsParam;

    @ApiModelProperty("关联模型实例参数")
    private List<QueryInstanceRelationsParam> oppoRelationsParamList;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;

    @ApiModelProperty("完整拓扑信息数据")
    private LastData lastData;

    @ApiModelProperty("页面显示拓扑信息数据")
    private LastData data;

    @ApiModelProperty("实例拓扑操作类型")
    private String action;

    @ApiModelProperty("是否删除模型")
    private Boolean isDeleteModel;

    @ApiModelProperty("隐藏模型拓扑模型ids")
    private List<Integer> hideModelIds;

    @ApiModelProperty("显示模型拓扑模型ids")
    private List<Integer> showModelIds;

    @ApiModelProperty("隐藏关联模型")
    private QueryHideModelToPo hideModelToPoListParam;

    @ApiModelProperty("实例拓扑中,添加需要连接的实例节点信息")
    private List<List<Integer>> linkInstanceParams;

    @ApiModelProperty("实例视图id")
    private Long instanceViewId;

    //添加实例拓扑连线用到
    //连线目的节点列表元素格式[模型id, 实例id]
    @ApiModelProperty("对端连线节点")
    List<List<Integer>> targetNodes;
}
