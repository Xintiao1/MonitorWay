package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName PatrolInspectionDto
 * @Description 巡检报告实体
 * @Author gengjb
 * @Date 2022/10/24 15:39
 * @Version 1.0
 **/
@Data
public class PatrolInspectionDto {

    @ApiModelProperty("主机ID")
    private String assetsId;

    @ApiModelProperty("区域标签值")
    private String dropValue;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("资产IP")
    private String assetsIp;

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    @ApiModelProperty("IP地址")
    private String ip;
}
