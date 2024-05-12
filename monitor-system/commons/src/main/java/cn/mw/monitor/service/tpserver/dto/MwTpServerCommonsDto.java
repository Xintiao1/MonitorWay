package cn.mw.monitor.service.tpserver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 监控服务器公共DTO
 * @Author gengjb
 * @Date 2023/5/25 19:00
 * @Version 1.0
 **/
@Data
@ApiModel("监控服务器公共DTO")
public class MwTpServerCommonsDto {

    @ApiModelProperty("服务器ID")
    private  int serverId;

    @ApiModelProperty("服务器名称")
    private String monitoringServerName;
}
