package cn.mw.monitor.accountmanage.entity;


import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;


@Data
@ApiModel("账号管理主表")
public class AddAccountManageParam extends DataPermissionParam {
    //主键
    private Integer id;

    //账号
    @Size(max = 256, message = "账号名称最大长度不能超过256字符！")
    private String account;

    //enable开关
    private Boolean enable;

    //协议
    private String protocol;

    //端口
    private String port;

    //用户名
    @Size(max = 256, message = "用户名最大长度不能超过256字符！")
    private String username;

    //密码
    @Size(max = 64, message = "密码最大长度不能超过64字符！")
    private String password;

    //enable命令
    private String enableCmd;

    //enable密码
    @Size(max = 64, message = "enable密码最大长度不能超过64字符！")
    private String enablePassword;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    /**
     * 系统类别（1：Linux  2：Windows）
     */
    @ApiModelProperty("系统类别")
    private String systemType;

    /**
     * 父ID
     */
    private Integer pid = 0;

    /**
     * 不存在fTFP
     */
    private Integer TftpType = 1;

    /**
     * 系统类别（1：Linux  2：Windows）
     */
    @ApiModelProperty("IP地址")
    private String IpDown;

    /**
     * 获取数据类别
     *
     * @return
     */
    @Override
    public DataType getBaseDataType() {
        return DataType.ACCOUNT;
    }

    /**
     * 获取绑定的数据ID
     *
     * @return
     */
    @Override
    public String getBaseTypeId() {
        return id + "";
    }
}
