package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("新ip地址分配回填 ")
public class ResponIpDistributtionNewParam {
    //主键
    @ApiModelProperty(value="主键")
    private Integer id;
    @ApiModelProperty(value="主id")
    private Integer mainId;
    @ApiModelProperty(value="主id")
    private Integer mainIdType;
    @ApiModelProperty(value="位置")
    private String label;
    @ApiModelProperty(value="分配地址")
    String keyTestValue;
    @ApiModelProperty(value="地址是否为IPv4 false表示是")
    private Boolean idType;
    @ApiModelProperty(value="描述")
    private String desc;
    @ApiModelProperty(value="非关系型id存储位置")
    private List<Integer> ids = new ArrayList<>();
    @ApiModelProperty(value="节点分类 1.关系ip地址 2.描述 3.非关系ip地址 0.分组")
    private Integer isfz;
    @ApiModelProperty(value="childDrop")
    private String childDrop;
    @ApiModelProperty(value="父节点")
    private Integer parentId;
    @ApiModelProperty(value="地址是否为IPv4 false表示是")
    private Boolean isDesc;
    @ApiModelProperty(value="ip关系ip")
    String bangDistri;
    List<ResponIpDistributtionNewParam> children = new ArrayList<>();
    @ApiModelProperty(value="ip关系ip")
    String redomId ;
    List<ResponIpDistributtionNewParam> treeData = new ArrayList<>();
    @ApiModelProperty(value="回收变更前查询")
    List<ResponIpDistributtionNewParam> ipAndAddressList ;
}
