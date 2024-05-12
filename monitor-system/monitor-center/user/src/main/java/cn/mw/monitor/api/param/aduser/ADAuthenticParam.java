package cn.mw.monitor.api.param.aduser;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AD验证信息
 * Created by zy.quaee on 2021/4/27 9:23.
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ADAuthenticParam {
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
    /**
     *  域名
     */
    @ApiModelProperty("域名")
    private String domainName;

}
