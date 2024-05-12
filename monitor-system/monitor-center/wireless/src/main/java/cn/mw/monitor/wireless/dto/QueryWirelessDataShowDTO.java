package cn.mw.monitor.wireless.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author qzg
 * @Date 2021/6/23
 */
@Data
@ApiModel(value = "无线设备查询信息实体")
public class QueryWirelessDataShowDTO extends BaseParam {
    @ApiModelProperty("CPU使用率")
    private String cpuNum;
    @ApiModelProperty("用户数")
    private Integer userNum;
    @ApiModelProperty("AP数量")
    private Integer apNum;
    @ApiModelProperty("更新时间")
    private Date updateData;
    @ApiModelProperty("运行持续时间")
    private String duration;
    @ApiModelProperty("设备型号")
    private String deviceModel;
    @ApiModelProperty("版本信息")
    private String versionInfo;
    @ApiModelProperty("设备MAC")
    private String deviceMAC;
    @ApiModelProperty("设备序列号")
    private String serialNo;

    private Double sortCpuNum;
    private Integer sortUserNum;
    private Integer sortApNum;

}
