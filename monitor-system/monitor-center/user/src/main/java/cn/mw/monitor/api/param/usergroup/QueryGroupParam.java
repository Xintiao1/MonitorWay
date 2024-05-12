package cn.mw.monitor.api.param.usergroup;


import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("查询用户组列表数据")
public class QueryGroupParam extends BaseParam {

    // 用户组id
    @ApiModelProperty("用户组id")
    private Integer groupId;
    // 用户组名称
    @ApiModelProperty("用户组名称")
    private String groupName;
    // 用户组状态
    @ApiModelProperty("用户组状态")
    private String enable;
    // 创建人
    @ApiModelProperty("创建人")
    private String creator;
    // 修改人
    @ApiModelProperty("修改人")
    private String modifier;
    // 创建时间开始
    @ApiModelProperty("创建时间开始")
    private Date createDateStart;
    // 创建时间结束
    @ApiModelProperty("创建时间结束")
    private Date createDateEnd;
    // 修改时间开始
    @ApiModelProperty("修改时间开始")
    private Date modificationDateStart;
    // 修改时间结束
    @ApiModelProperty("修改时间结束")
    private Date modificationDateEnd;

    private String loginName;

}
