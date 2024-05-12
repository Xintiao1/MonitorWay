package cn.mw.monitor.accountmanage.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
public class MwQueryAccountManageTable {

    //主键
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

    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
    @ApiModelProperty("修改人")
    private String modifier;

    private List<Integer> principal;

    private List<Integer> orgIdss;
    private List<List<Integer>> orgIds = new ArrayList<>();

    private List<Integer> groupIds;

    /**
     * 系统类别（1：Linux  2：Windows）
     */
    private String systemType;

    /**
     * 父ID
     */
    private Integer pid;

    /**
     * 不存在fTFP
     */
    private Integer TftpType = 1;

    /**
     * 系统类别（1：Linux  2：Windows）
     */
    @ApiModelProperty("IP地址")
    private String IpDown;
}
