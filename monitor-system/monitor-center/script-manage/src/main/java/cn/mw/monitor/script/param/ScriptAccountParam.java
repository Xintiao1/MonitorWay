package cn.mw.monitor.script.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className ScriptAccountParam
 * @description 账户管理参数
 * @date 2022/4/24
 */
@Data
@ApiModel(value = "账户管理数据")
public class ScriptAccountParam extends DataPermissionParam {

    /**
     * 主键ID
     */
    @ApiModelProperty("主键ID")
    private Integer id;

    /**
     * id列表
     */
    private List<Integer> ids;

    /**
     * 账户名称
     */
    @ApiModelProperty("账户名称")
    @Size(max = 256, message = "账号名称最大长度不能超过256字符！")
    private String account;

    /**
     * 账户别名
     */
    @ApiModelProperty("账户别名")
    private String accountAlias;

    /**
     * 密码
     */
    @Size(max = 64, message = "密码最大长度不能超过64字符！")
    @ApiModelProperty("密码")
    private String password;

    /**
     * port
     */
    @ApiModelProperty("端口")
    private String port;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String creator;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 修改时间
     */
    private Date modificationDate;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String accountDesc;

    /**
     * 系统类别（Linux  Windows  mysql device）
     */
    @ApiModelProperty("系统类别")
    private String systemType;

    /**
     * 父ID，当system_type=mysql的时候，生效
     */
    @ApiModelProperty("父ID，当system_type=mysql的时候，生效")
    private Integer pid;


    /**
     * enable开关
     */
    @ApiModelProperty("enable开关")
    private Boolean enable;

    /**
     * 协议
     */
    @ApiModelProperty("协议")
    private String protocol;

    /**
     * 用户名
     */
    @Size(max = 256, message = "用户名最大长度不能超过256字符！")
    @ApiModelProperty("用户名")
    private String username;


    /**
     * enable命令
     */
    @ApiModelProperty("enable命令")
    private String enableCmd;

    /**
     * enable密码
     */
    @Size(max = 64, message = "enable密码最大长度不能超过64字符！")
    @ApiModelProperty("enable密码")
    private String enablePassword;

    /**
     * 创建开始时间
     */
    @ApiModelProperty("创建开始时间")
    private Date createDateStart;

    /**
     * 创建结束时间
     */
    @ApiModelProperty("创建结束时间")
    private Date createDateEnd;

    /**
     * 更新开始时间
     */
    @ApiModelProperty("更新开始时间")
    private Date modificationDateStart;


    @ApiModelProperty("是否为设备，默认不是0，1是设备")
    private Integer device=0;

    /**
     * 更新结束时间
     */
    @ApiModelProperty("更新结束时间")
    private Date modificationDateEnd;


    @Override
    public DataType getBaseDataType() {
        return DataType.ACCOUNT_MANAGE;
    }

    @Override
    public String getBaseTypeId() {
        return id + "";
    }
}
