package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("插入Id")
public class IsInput  extends BaseParam {
    @ApiModelProperty(value="新回收总查询-ip地址id")
    private Integer id;
    @ApiModelProperty(value="新回收总查询-ip地址id")
    private List<Integer> ids;
    @ApiModelProperty(value="新回收总查询-ip地址类型  false表示是ipv4 true是ipv6 ")
    private boolean idType;
    @ApiModelProperty(value="新回收总查询-查询方式  false表示是回收 true是变更 ")
    private boolean sreacType;
    @ApiModelProperty(value="插如的节点ID")
    private Integer IndexId;
    @ApiModelProperty(value="插如的节点ID")
    private Integer dragging;
    @ApiModelProperty(value="插如的节点相对位置ID")
    private Integer dropNode;
    @ApiModelProperty(value="插如的节点ID")
    private boolean draggingType = true;
    @ApiModelProperty(value="插如的节点相对位置ID的种类")
    private boolean dropNodeType;
    @ApiModelProperty(value="传入的位置方式不同")
    private String  type;
    @ApiModelProperty(value="查看其它分组的地址-当前等级")
    private Integer  level;
    @ApiModelProperty(value="查看其它分组的地址-parentId")
    private Integer  parentId;
    @ApiModelProperty(value="ip关系ip")
    private String  bangDistri;
    @ApiModelProperty(value="概览Id")
    private Integer  gaiId;
    @ApiModelProperty(value="signId")
    private Integer  signId;
}

