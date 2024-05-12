package cn.mw.monitor.service.link.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 线路参数
 * @date 2023/12/4 15:54
 */
@Data
@ApiModel("线路参数")
public class MwLinkCommonParam extends BaseParam {

    @ApiModelProperty("线路名称")
    private String linkName;

    @ApiModelProperty("线路状态")
    private Integer linkStatus;

}
