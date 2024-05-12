package cn.mw.monitor.api.param.aduser;

import cn.mw.monitor.user.model.ADUserDetailDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by zy.quaee on 2021/4/28 15:32.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddADUserParam extends AdCommonParam{

    /**
     * 映射本地 用户组IDs 机构IDs
     */
    @ApiModelProperty("用户组IDs")
    private List<Integer> userGroup;
    @ApiModelProperty("机构IDs")
    private List<List<Integer>> department;

    /**
     * 角色
     */
    @ApiModelProperty("角色id")
    private String roleId;
    /**
     * 查询AD组织机构信息
     */
    @ApiModelProperty("查询AD组织机构信息")
    private List<String> search;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("同步AD用户")
    private List<ADUserDetailDTO> userList;

    @ApiModelProperty("查找节点")
    private String searchNodes;

    @ApiModelProperty("ad 类型")
    private String adType;

    @ApiModelProperty("映射配置新增用户")
    private boolean configAddUser;

    private List<Integer> orgIdList;

    private Integer configId;

    /**
     * 是否开启模糊查询
     */
    @ApiModelProperty("模糊查询")
    private boolean fuzzyQuery;

    /**
     * 模糊查询登录名
     */
    private String fuzzyLoginName;

    /**
     * 模糊查询UserName
     */
    private String fuzzyUserName;

    /**
     * 获取提示语 loginName userName
     */
    private String fuzzyNormal;

    /**
     * 什么都不选
     */
    private String fuzzyNothing;
}

