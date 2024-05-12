package cn.mw.monitor.api.param.role;

import cn.mw.monitor.api.param.org.AddUpdateOrgParam;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

@Data
@GroupSequence({Insert.class,Update.class, AddUpdateModuleParam.class})
@ApiModel("新增或更新模块数据")
public class AddUpdateModuleParam {

    // 模块ID
    @ApiModelProperty("模块id")
    private Integer id;
    @ApiModelProperty("模块上级id")
    private Integer pid;
    // 模块名称
    @ApiModelProperty("模块名称")
    @NotEmpty(message = "模块名称不能为空!",groups = {Insert.class,Update.class})
    @Size(max = 20,message = "模块名称不能超过20字符!",groups = {Insert.class,Update.class})
    private String moduleName;
    // 模块描述
    @ApiModelProperty("模块描述")
    @Size(max = 100,message = "模块描述不能超过100字符!",groups = {Insert.class,Update.class})
    private String moduleDesc;
    @ApiModelProperty("url")
    private String url;
    // 深度
    @ApiModelProperty("深度")
    private Integer deep;
    // 节点ID
    @ApiModelProperty("节点id")
    private String nodes;
    // 是否子节点
    @ApiModelProperty("是否子节点")
    private Boolean isNode;
    @ApiModelProperty("状态")
    private Boolean enable;
    @ApiModelProperty("版本")
    private Integer version;
    //删除标识符
    @ApiModelProperty("删除标识符")
    private Boolean deleteFlag;
}
