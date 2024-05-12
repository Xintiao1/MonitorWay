package cn.mw.monitor.service.server.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/8/6 16:20
 * @Version 1.0
 */
@Data
public class RunServiceObjectParam extends BaseParam {
    private String ip;
    @ApiModelProperty("当前详情页所代表的资产唯一标识id")
    private String id;

    @ApiModelProperty("资产类型，区分带外和有形")
    private int type;
}
