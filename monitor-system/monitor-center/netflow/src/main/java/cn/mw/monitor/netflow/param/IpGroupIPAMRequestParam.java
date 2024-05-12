package cn.mw.monitor.netflow.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className IpGroupIPAMRequestParam
 * @description IPAM导入请求参数
 * @date 2022/8/24
 */
@Data
public class IpGroupIPAMRequestParam {


    /**
     * 节点类别（grouping：文件夹，iPaddresses：IP地址段）
     */
    @ApiModelProperty("节点类别（grouping：文件夹，iPaddresses：IP地址段）")
    private String itemType;

    /**
     * 节点ID，对应着IP地址管理的树节点ID
     */
    @ApiModelProperty("节点ID，对应着IP地址管理的树节点ID")
    private Integer itemId;

    /**
     * 节点的父ID
     */
    @ApiModelProperty("节点的父ID")
    private Integer itemPid;


    /**
     * 节点名称
     */
    @ApiModelProperty("节点名称")
    private String itemLabel;

    /**
     * 子集合
     */
    @ApiModelProperty("子集合")
    private List<IpGroupIPAMRequestParam> childList;

}
