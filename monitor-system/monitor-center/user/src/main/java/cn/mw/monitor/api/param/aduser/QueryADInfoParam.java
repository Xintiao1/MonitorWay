package cn.mw.monitor.api.param.aduser;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zy.quaee on 2021/4/28 14:09.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryADInfoParam extends BaseParam {
    private Integer id;
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
     * 查询节点
     */
    private String searchNodes;

    /**
     * 判断是新增规则 还是新增用户  true 新增规则
     */
    private boolean ruleCreate;

    private Integer configId;

}
