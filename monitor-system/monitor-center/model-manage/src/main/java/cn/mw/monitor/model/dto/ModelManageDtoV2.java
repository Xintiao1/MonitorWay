package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/7 9:51
 */
@Data
public class ModelManageDtoV2 {
    @ApiModelProperty("模型Id")
    private Long modelId;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("模型描述")
    private String modelDesc;

    @ApiModelProperty("模型索引")
    private String modelIndex;

    @ApiModelProperty("模型类型ID  1普通模型 2 父模型 3子模型")
    private Integer modelTypeId;

    @ApiModelProperty("模型类型")
    private String modelTypeName;

    @ApiModelProperty("模型分组ID ")
    private Long modelGroupId;

    @ApiModelProperty("模型分组子ID ")
    private String modelGroupSubId;

    @ApiModelProperty("模型分组名称 ")
    private String modelGroupName;

    @ApiModelProperty("模型分组子ID名称 ")
    private String modelGroupSubName;


    @ApiModelProperty("模型图标")
    private String modelIcon;

    @ApiModelProperty("是否显示")
    private Boolean isShow;

    @ApiModelProperty("节点深度  如果普通和父模型 都是1 子模型根据深度来")
    private Integer deep = 1;

    @ApiModelProperty("节点")
    private String nodes;

    @ApiModelProperty("父节点id  如果普通和父模型 都是0  子模型就是用户自己选的父模型id ")
    private Long pid = 0l;

    @ApiModelProperty("是否根节点  如果普通和父模型 是true ")
    private Boolean isNode;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;
    @ApiModelProperty("模型级别 0:内置模型，1:自定义模型;其中内置模型不可删除")
    private Integer modelLevel;

    @ApiModelProperty("模型分组节点")
    private String groupNodes;

    @ApiModelProperty("父模型ids ")
    private String pids;

    @ApiModelProperty("模型视图（0：默认视图，1：机房视图，2机柜视图）")
    private Integer modelView;

    private List<UserDTO> principal;

    private List<OrgDTO> department;

    private List<GroupDTO> groups;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

}
