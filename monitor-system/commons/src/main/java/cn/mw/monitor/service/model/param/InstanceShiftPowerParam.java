package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/3/8 12:11
 */
@Data
@ApiModel
public class InstanceShiftPowerParam {
    @ApiModelProperty("转移前用户Id")
    private Integer beforeUserId;
    @ApiModelProperty("转移后用户Id")
    private Integer afterUserId;
    //是否忽略数据权限控制  true忽略，可在定时任务时设置为true，避免没有userId导致报错
    private Boolean skipDataPermission;
}
