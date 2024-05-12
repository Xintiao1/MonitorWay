package cn.mw.monitor.api.param.role;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("查询角色列表数据")
public class QueryRoleParam extends BaseParam {

    // 角色ID
    @ApiModelProperty("角色id")
    private Integer roleId;
    // 角色名称
    @ApiModelProperty("角色名称")
    private String roleName;
    // 角色描述
    @ApiModelProperty("角色描述")
    private String roleDesc;
    // 创建人
    @ApiModelProperty("创建人")
    private String creator;
    // 创建时间开始
    @ApiModelProperty("创建时间开始")
    private Date createDateStart;
    // 创建时间结束
    @ApiModelProperty("创建时间结束")
    private Date createDateEnd;
    // 修改人
    @ApiModelProperty("修改人")
    private String modifier;
    // 修改时间开始
    @ApiModelProperty("修改时间开始")
    private Date modificationDateStart;
    // 修改时间结束
    @ApiModelProperty("修改时间结束")
    private Date modificationDateEnd;
    // 角色状态
    @ApiModelProperty("角色状态")
    private String enable;

}
