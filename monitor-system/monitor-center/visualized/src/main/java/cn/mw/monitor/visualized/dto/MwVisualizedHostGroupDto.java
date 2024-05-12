package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ServerHostGroupDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 可视化主机与主机组DTO
 * @Author gengjb
 * @Date 2023/5/19 21:48
 * @Version 1.0
 **/
@Data
@ApiModel("可视化主机与主机组DTO")
public class MwVisualizedHostGroupDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("主机组ID")
    private String hostGroupId;

    @ApiModelProperty("主机组名称")
    private String hostGroupName;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("主机名称")
    private String hostName;

    @ApiModelProperty("主机状态")
    private String hostStatus;

    @ApiModelProperty("服务器名称")
    private String serverName;

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    //创建人
    @ApiModelProperty("创建人")
    private String creator;

    //创建时间
    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("告警标题")
    private String alertTitle;


    public void extractFrom(String hostGroupId,String hostGroupName,ItemApplication itemApplication,Integer serverId,String serverName){
        this.hostGroupId = hostGroupId;
        this.hostGroupName = hostGroupName;
        this.hostId = itemApplication.getHostid();
        this.hostName = itemApplication.getName();
        this.serverId = serverId;
        this.serverName = serverName;
    }
}
