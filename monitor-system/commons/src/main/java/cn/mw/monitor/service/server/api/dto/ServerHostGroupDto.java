package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 主机组DTO
 * @Author gengjb
 * @Date 2023/5/19 21:00
 * @Version 1.0
 **/
@Data
@ApiModel("主机组DTO")
public class ServerHostGroupDto {

    @ApiModelProperty("主机组ID")
    private String groupid;

    @ApiModelProperty("主机组名称")
    private String name;

    @ApiModelProperty("主机组下主机")
    private List<ItemApplication> hosts;
}
