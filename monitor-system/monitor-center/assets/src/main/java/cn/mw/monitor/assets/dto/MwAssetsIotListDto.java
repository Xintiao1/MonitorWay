package cn.mw.monitor.assets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/6/12 11:53
 */
@Data
public class MwAssetsIotListDto {
    @ApiModelProperty("资产主键ID")
    private String assetsId;
    @ApiModelProperty("hostId")
    private String hostId;
    @ApiModelProperty("监控服务器id")
    private Integer monitorServerId;
    @ApiModelProperty("资产名称")
    private String assetsName;
    @ApiModelProperty("温度")
    private Double tem;
    @ApiModelProperty("湿度")
    private Double hum;
    @ApiModelProperty("温度是否报警 1报警 0正常")
    private Integer temAlarm;
    @ApiModelProperty("湿度是否报警 1报警 0正常")
    private Integer humAlarm;
    @ApiModelProperty("是否有声音")
    private Boolean voice;
    @ApiModelProperty("最后一次更新时间")
    private String updateTime;
    @ApiModelProperty("传感器名称")
    private String devName;
    @ApiModelProperty("传感器状态")
    private String devStatus;

    @ApiModelProperty("告警状态 humAlarm temAlarm  allAlarm")
    private String alarmState;


}
