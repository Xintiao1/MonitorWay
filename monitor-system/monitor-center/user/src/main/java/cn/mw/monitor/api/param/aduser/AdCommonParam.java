package cn.mw.monitor.api.param.aduser;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/5/7 16:58.
 **/
@Data
public class AdCommonParam extends BaseParam {
    /**
     * AD域服务器名称
     */
    @ApiModelProperty("AD域服务器名称")
    private String adServerName;

    /**
     * 服务器IP地址
     */
    @ApiModelProperty("服务器IP地址")
    private String adServerIpAdd;

    /**
     * 端口
     */
    @ApiModelProperty("端口")
    private String adPort;

    /**
     * AD域管理员账号
     */
    @ApiModelProperty("AD域管理员账号")
    private String adAdminAccount;

    /**
     * AD域管理员密码
     */
    @ApiModelProperty("AD域管理员密码")
    private String adAdminPasswd;

    /**
     *  OU  GROUP USER
     */
    @ApiModelProperty("OU | GROUP | USER")
    private String adType;

    @ApiModelProperty("保存配置")
    private boolean saveConfig;

    @ApiModelProperty("域名")
    private String domainName;

    /**
     * 配置备注
     */
    @ApiModelProperty("配置备注")
    private String configDesc;
}
