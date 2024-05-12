package cn.mw.monitor.api.param.org;

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
import java.util.List;

@Data
@ApiModel("新增或更新机构数据")
@GroupSequence({Insert.class,Update.class,AddUpdateOrgParam.class})
public class AddUpdateOrgParam {

    // 机构ID
    @ApiModelProperty("机构id")
    @Null(message = "新增机构时机构id必须为空！", groups = {Insert.class})
    @NotNull(message = "更新机构时机构id不为空！", groups = {Update.class})
    private Integer orgId;
    // 机构名称
    @ApiModelProperty("机构名称")
    @NotEmpty(message = "机构名称不能为空！", groups = {Insert.class,Update.class})
    @Size(min = 1, max = 100, message = "机构名称长度为1~100字符！", groups = {Insert.class,Update.class})
    private String orgName;
    // 地址
    @ApiModelProperty("地址")
    @Size(max = 100, message = "地址长度为1~100字符！", groups = {Insert.class,Update.class})
    private String address;
    //经纬度
    @ApiModelProperty("经纬度")
    @Size(max = 200, message = "经纬度长度为1~200字符！", groups = {Insert.class,Update.class})
    private String coordinate;
    // 邮政编码
    @ApiModelProperty("邮政编码")
    @Size(max = 20, message = "邮政编码最大长度不成超过20字符！", groups = {Insert.class,Update.class})
    private String postCode;
    // 联系人
    @ApiModelProperty("联系人")
    @Size(max = 100, message = "联系人长度为1~100字符！", groups = {Insert.class,Update.class})
    private String contactPerson;
    // 联系电话
    @ApiModelProperty("联系电话")
    @Size(max = 20, message = "联系电话最大长度不成超过20字符！", groups = {Insert.class,Update.class})
    private String contactPhone;
    // 机构描述
    @ApiModelProperty("机构描述")
    @Size(max = 200, message = "机构描述最大长度不成超过200字符！", groups = {Insert.class,Update.class})
    private String orgDesc;
    // 机构类型
    @ApiModelProperty("机构类型")
    @NotNull(message = "机构类型不能为空！", groups = {Insert.class})
    private String orgType;
    // 深度
    @ApiModelProperty("深度")
    private Integer deep;
    // 机构上级ID
    @ApiModelProperty("机构上级id")
    private Integer pid;
    // 节点ID
    @ApiModelProperty("节点id")
    private String nodes;
    // 是否子节点
    @ApiModelProperty("是否子节点")
    private Boolean isNode;

    /**
     * 机构ID列表数据
     */
    @ApiModelProperty("机构id列表")
    private List<Integer> orgIdList;
}
