package cn.mw.monitor.model.param;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/19 14:56
 * 模型分类创建
 */
@Data
@ApiModel
public class AddAndUpdateModelGroupParam {

    @ApiModelProperty("模型类型Id")
    private Integer modelGroupId;

    @ApiModelProperty("模型类型名称")
    private String modelGroupName;

    @ApiModelProperty("是否显示")
    private Boolean isShow;

    @ApiModelProperty("节点深度")
    private Integer deep;

    @ApiModelProperty("节点 ,分割")
    private String  nodes;

    @ApiModelProperty("父节点id")
    private Integer pid;

    @ApiModelProperty("是否根节点")
    private Boolean isNode;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("修改人")
    private String modifier;

    @ApiModelProperty("修改时间")
    private Date modificationDate;

    @ApiModelProperty("分组级别：是否内置（1内置，不可删除；0普通，可删除）")
    private Integer groupLevel;

    @ApiModelProperty("是否同步创建zabbixGroup分组(0:不创建，1:创建)")
    private Integer syncZabbix;

    @ApiModelProperty("zabbixGroup分组标识")
    private String network;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    private List<UserDTO> principal;

    private List<OrgDTO> department;

    private List<GroupDTO> groups;

    //查询使用
    @ApiModelProperty("模型名称")
    private String modelName;
    @ApiModelProperty("图标url")
    private String url;
}
