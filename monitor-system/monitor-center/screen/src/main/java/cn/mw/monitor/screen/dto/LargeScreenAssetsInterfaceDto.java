package cn.mw.monitor.screen.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName LargeScreenAssetsInterfaceDto
 * @Description 大屏地图资产接口数据实体
 * @Author gengjb
 * @Date 2022/9/2 9:01
 * @Version 1.0
 **/
@Data
public class LargeScreenAssetsInterfaceDto {

    @ApiModelProperty("资产名称")
    private String name;

    @ApiModelProperty("资产主机ID")
    private String assetsId;

    @ApiModelProperty("资产ID")
    private String id;

    @ApiModelProperty("资产IP地址")
    private String assetsIp;

    @ApiModelProperty("zabbix服务器ID")
    private String assetsServerId;

    @ApiModelProperty("接口名称")
    private List<String> interFaceNames;

    private List<LargeScreenAssetsInterfaceDto> children;

    private Map<String,Object> map;

    private boolean disabled;

}
