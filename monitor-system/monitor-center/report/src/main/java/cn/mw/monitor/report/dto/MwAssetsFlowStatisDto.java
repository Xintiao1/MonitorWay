package cn.mw.monitor.report.dto;

import cn.mw.monitor.report.enums.MwAssetsFlowStatisEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.netflow.entity.NetflowResult;
import cn.mw.monitor.service.netflow.param.NetflowSearchParam;
import cn.mwpaas.common.utils.DateUtils;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author gengjb
 * @description 资产流量统计报表DTO
 * @date 2023/8/28 15:57
 */
@Data
@ApiModel("资产流量统计报表DTO")
public class MwAssetsFlowStatisDto {

    @ApiModelProperty("资产ID")
    private String assetsId;

    @ExcelProperty(value = {"资产名称"},index = 0)
    @ApiModelProperty("资产名称")
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 1)
    @ApiModelProperty("IP地址")
    private String ipAddress;

    @ExcelProperty(value = {"资产状态"},index = 2)
    @ApiModelProperty("资产状态")
    private String assetsStatus;

    @ExcelProperty(value = {"开始时间"},index = 3)
    @ApiModelProperty("开始时间")
    private String startTime;

    @ExcelProperty(value = {"结束时间"},index = 4)
    @ApiModelProperty("结束时间")
    private String endTime;

    @ExcelProperty(value = {"入向总流量"},index = 5)
    @ApiModelProperty("入向总流量")
    private String totalFlowIn;

    @ExcelProperty(value = {"出项总流量"},index = 6)
    @ApiModelProperty("出项总流量")
    private String totalFlowOut;

    @ExcelProperty(value = {"入向(峰值)"},index = 7)
    @ApiModelProperty("入向(峰值)")
    private String maxFlowIn;

    @ExcelProperty(value = {"出向(峰值)"},index = 8)
    @ApiModelProperty("出向(峰值)")
    private String maxFlowOut;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    @ApiModelProperty("模型ID")
    private Integer modelId;

    @ApiModelProperty("模型索引")
    private String modelIndex;

    public void extractFrom(MwTangibleassetsTable tangibleassetsTable, NetflowResult netflowResult, NetflowSearchParam netflowSearchParam){
        this.assetsId = tangibleassetsTable.getId() == null?String.valueOf(tangibleassetsTable.getModelInstanceId()):tangibleassetsTable.getId();
        this.assetsName = tangibleassetsTable.getAssetsName() == null?tangibleassetsTable.getInstanceName():tangibleassetsTable.getAssetsName();
        this.ipAddress = tangibleassetsTable.getInBandIp();
        this.assetsStatus = MwAssetsFlowStatisEnum.getDesc(tangibleassetsTable.getItemAssetsStatus());
        this.startTime = DateUtils.formatDateTime(netflowSearchParam.getStartTime());
        this.endTime = DateUtils.formatDateTime(netflowSearchParam.getEndTime());
        this.totalFlowIn = netflowResult.getInSum();
        this.totalFlowOut = netflowResult.getOutSum();
        this.maxFlowIn = netflowResult.getInMax();
        this.maxFlowOut = netflowResult.getOutMax();
        this.hostId = tangibleassetsTable.getAssetsId();
        this.serverId = tangibleassetsTable.getMonitorServerId();
        this.modelId = tangibleassetsTable.getModelId();
        this.modelIndex = tangibleassetsTable.getModelIndex();
    }
}
