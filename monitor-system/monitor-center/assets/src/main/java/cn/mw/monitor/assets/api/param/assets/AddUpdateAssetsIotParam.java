package cn.mw.monitor.assets.api.param.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/6/6 10:41
 */
@Data
@ApiModel("IOT资产数据")
public class AddUpdateAssetsIotParam {
    @ApiModelProperty("温湿度传感器设置温湿度的 id")
    private String id;
    @ApiModelProperty("资产主键 id")
    private String assetsId;
    @ApiModelProperty("hostid")
    private String hostid;
    @ApiModelProperty("温度告警阈值")
    private double temThreshold;
    @ApiModelProperty("温度告警条件<>=")
    private String temCondition;
    @ApiModelProperty("告警声音是否开启，1开启 0关闭")
    private String voice;
    @ApiModelProperty("湿度告警阈值")
    private double humThreshold;
    @ApiModelProperty("湿度告警条件<>=")
    private String humCondition;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;

}
