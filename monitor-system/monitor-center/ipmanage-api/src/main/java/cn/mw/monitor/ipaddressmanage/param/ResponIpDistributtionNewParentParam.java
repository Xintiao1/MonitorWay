package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("大标签分配回填 ")
public class ResponIpDistributtionNewParentParam {
    //主键
    @ApiModelProperty(value="属性")
    private List<Label> attrParam;
    @ApiModelProperty(value="属性二")
    private List<MwAssetsLabelDTO> attrData;
    @ApiModelProperty(value="上节点")
    private List<String> upTree;
    @ApiModelProperty(value="下节点")
    private List<String> downTree;
    @ApiModelProperty(value="上节点")
    private List<Integer> upTreeIds;
    @ApiModelProperty(value="下节点")
    private List<Integer> downTreeIds;
    @ApiModelProperty(value="子节点")
    List<ResponIpDistributtionNewParam> treeData;

    @ApiModelProperty(value="内部节点数量")
    Integer num ;

    @ApiModelProperty(value="概览Id")
    private Integer  gaiId;
}
