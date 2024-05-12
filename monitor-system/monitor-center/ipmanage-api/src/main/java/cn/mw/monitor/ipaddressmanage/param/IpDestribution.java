package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lumingming
 * @createTime 2022925 15:20
 * @description
 */
@Data
public class IpDestribution {
    @ApiModelProperty(value = "分组")
    private Integer id;
    @ApiModelProperty(value = "父节点id")
    private Integer primaryIp;
    @ApiModelProperty(value = "父节点类型")
    private Integer primaryType;
    @ApiModelProperty(value = "子节点类型")
    private Integer iplistType;
    @ApiModelProperty(value = "子节点id")
    private Integer iplistId;
    @ApiModelProperty(value = "首节点id")
    private Integer ipgroupId;
    @ApiModelProperty(value = "首节点类型")
    private Integer ipgroupType;
    @ApiModelProperty(value="审核人")
    private String applicanttext; //责任人
    @ApiModelProperty(value="审核人")
    private String bangDistri; //责任人
    @ApiModelProperty(value="用户组")
    private List<String> groupIdsString; //用户组
    @ApiModelProperty(value="机构")
    private String orgtext;  //机构
    @ApiModelProperty(value="oa选项")
    private Integer oa; //用户组
    @ApiModelProperty(value="oa选项test文本")
    private String oatext; //用户组
    @ApiModelProperty(value="oaurl")
    private Integer oaurl; //用户组
    @ApiModelProperty(value="oaurl选项test文本")
    private String oaurltext; //用户组

    @ApiModelProperty(value="机构")
    private String orgIds ;  //机构
}
