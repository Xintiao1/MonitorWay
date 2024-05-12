package cn.mw.monitor.credential.api.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by zy.quaee on 2021/5/31 14:39.
 **/
@Data
@ApiModel(value="系统凭据添加修改参数")
public class AddUpdateCredentialParam {

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Integer id;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String mwAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String mwPasswd;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String creator;

    /**
     * 模块
     */
    @ApiModelProperty(value = "模块")
    private List<String> modules;

    /**
     * 模块id
     */
    @ApiModelProperty(value = "模块id")
    private List<String> moduleIds;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    private String port;

    /**
     * SNMP版本
     */
    @ApiModelProperty(value = "SNMP版本")
    private String snmpVersion;

    /**
     * 团体名
     */
    @ApiModelProperty(value = "团体名")
    private String commName;

    /**
     * 凭据类型
     */
    @ApiModelProperty(value = "凭据类型")
    private String credType;

    /**
     * 凭据描述
     */
    @ApiModelProperty(value = "凭据描述")
    private String credDesc;

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
}