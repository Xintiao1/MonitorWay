package cn.mw.monitor.screen.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName LargeScreenMapDto
 * @Description 监控大屏地图数据显示实体
 * @Author gengjb
 * @Date 2022/9/2 9:00
 * @Version 1.0
 **/
@Data
public class LargeScreenMapDto {

    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("机构ID")
    private Integer orgId;

    @ApiModelProperty("资产主机")
    private String assetsId;

    @ApiModelProperty("资产IP")
    private String assetsIp;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("接口信息")
    private String interfaceName;

    @ApiModelProperty("显示信息")
    private String showInformation;

    @ApiModelProperty("zabbix服务器ID")
    private Integer monitorServerId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    private Map<String,Object> dataMap;

    //前端传过来的数据
    private List<Object> datas;

    //前端传过来的数据存入字段
    private String showData;

    //接口信息
    private List<Map<String,Object>> interfaceInformation;
}
