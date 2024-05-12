package cn.mw.monitor.logManage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "通道搜索参数")
public class VectorChannelSearchParam extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "通道IP")
    private String channelIp;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    /**
     * 通用查询字段，当这个字段不为空的时候表示模糊查询所有
     */
    private String fuzzyQuery;

    @ApiModelProperty(value = "状态, 可以为null, 0: 正常/在线   1: 异常/不在线")
    private Integer status;
}
