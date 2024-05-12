package cn.mw.monitor.credential.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * mw_sys_credential
 * @author 
 */
@ApiModel(value="系统凭据")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwSysCredential implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(value="主键")
    private Integer id;

    /**
     * 账号
     */
    @ApiModelProperty(value="账号")
    private String mwAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value="密码")
    private String mwPasswd;

    /**
     * 创建人
     */
    @ApiModelProperty(value="创建人")
    private String creator;

    /**
     * 模块
     */
    @ApiModelProperty(value="模块")
    private String module;

    /**
     * 模块id
     */
    @ApiModelProperty(value="模块id")
    private String moduleId;

    /**
     * 凭据描述
     */
    @ApiModelProperty(value = "凭据描述")
    private String credDesc;

    /**
     * 下拉框展示
     */
    @ApiModelProperty(value = "下拉框展示")
    private String credDetails;

    /**
     * 责任人
     */
    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    /**
     * 机构信息
     */
    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    /**
     * 用户组信息
     */
    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;

    private static final long serialVersionUID = 1L;
}