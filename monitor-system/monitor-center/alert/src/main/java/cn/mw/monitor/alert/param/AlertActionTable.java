package cn.mw.monitor.alert.param;

import cn.mw.monitor.service.action.param.CommonsParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author xhy
 * @date 2020/8/31 9:25
 */
@Data
public class AlertActionTable extends CommonsParam {
    private String actionId;
    private String actionName;
    @ApiModelProperty("是否是当前登录者的所有默认的资产")
    private Boolean isAllAssets;
    @ApiModelProperty("是否是当前登录者的所有可选的用户")
    private Boolean isAllUser;
    private Boolean enable;
    private Integer successNum;//成功次数
    private Integer failNum;//失败次数
    private Integer count;//匹配总数；
    private String effectTimeSelect;
    private String ruleName;
}
