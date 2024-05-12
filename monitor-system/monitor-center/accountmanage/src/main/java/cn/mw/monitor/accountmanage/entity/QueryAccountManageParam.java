package cn.mw.monitor.accountmanage.entity;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
public class QueryAccountManageParam extends DataPermissionParam {
    private Integer id;

    //账号
    private String account;

    //enable开关
    private Boolean enable;

    //协议
    private String protocol;

    //端口
    private String port;

    //用户名
    private String username;

    //密码
    private String password;

    //enable命令
    private String enableCmd;

    //enable密码
    private String enablePassword;

    @ApiModelProperty("创建开始时间")
    private Date createDateStart;
    @ApiModelProperty("创建结束时间")
    private Date createDateEnd;
    @ApiModelProperty("更新开始时间")
    private Date modificationDateStart;
    @ApiModelProperty("更新结束时间")
    private Date modificationDateEnd;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改人")
    private String modifier;

    private String perm;
    private Integer userId;

    private Boolean isAdmin;

    /**
     * 系统类别（1：Linux  2：Windows）
     */
    @ApiModelProperty("系统类别")
    private String systemType;

    /**
     * 父ID
     */
    private Integer pid;

    /**
     * 模糊查询字段
     */
    private String fuzzyQuery;

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
