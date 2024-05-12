package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MwVisualizedAssetsDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/5/6 11:11
 * @Version 1.0
 **/
@Data
@ApiModel("可视化资产信息")
public class MwVisualizedAssetsDto {

    //    资产id
    @ApiModelProperty("资产ID")
    private String id;
    //    资产名称
    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("IP地址")
    private String ipAddress;
    //    监控服务器id
    @ApiModelProperty("监控服务器id")
    private Integer monitorServerId;
    //    主机id
    @ApiModelProperty("主机id")
    private String assetsId;

    @ApiModelProperty("接口名称")
    private String interFaceName;

    @ApiModelProperty("资产类型名称")
    private String assetsTypeName;

    @ApiModelProperty("资产服务器名称")
    private String monitorServerName;

    @ApiModelProperty("资产对应的标签值")
    private List<String> labelValue;

    @ApiModelProperty("资产状态")
    private String assetsStatus;


}
