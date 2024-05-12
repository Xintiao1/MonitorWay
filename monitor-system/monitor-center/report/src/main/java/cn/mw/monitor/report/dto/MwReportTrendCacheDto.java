package cn.mw.monitor.report.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author gengjb
 * @description 报表缓存DTO
 * @date 2023/10/11 14:32
 */
@Data
@ApiModel("报表缓存DTO")
public class MwReportTrendCacheDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("资产IP")
    private String assetIp;

    @ApiModelProperty("资产主键ID")
    private String assetsId;

    @ApiModelProperty("资产服务器ID")
    private Integer serverId;

    @ApiModelProperty("资产主机ID")
    private String hostId;

    @ApiModelProperty("平均值")
    private String avgValue;

    @ApiModelProperty("最大值")
    private String maxValue;

    @ApiModelProperty("最小值")
    private String minValue;

    @ApiModelProperty("单位")
    private String units;

    @ApiModelProperty("监控项名称")
    private String itemName;

    @ApiModelProperty("分区名称")
    private String partitionName;

    @ApiModelProperty("时间戳(秒)")
    private String clock;

    @ApiModelProperty("数据产生时间")
    private Date date;

    @ApiModelProperty("保存数据时间")
    private Date saveTime;

    @ApiModelProperty("最新数据")
    private String lastValue;

    public void extractFrom(ItemApplication itemApplication, MwTangibleassetsTable tangibleassetsTable, ItemTrendApplication trendApplication){
        this.assetsId = tangibleassetsTable.getId();
        this.assetsName = tangibleassetsTable.getAssetsName()==null?tangibleassetsTable.getInstanceName():tangibleassetsTable.getAssetsName();
        this.assetIp = tangibleassetsTable.getInBandIp();
        this.hostId = itemApplication.getHostid();
        this.serverId = tangibleassetsTable.getMonitorServerId();
        handlerItemName(itemApplication.getName());
        if(trendApplication != null){
            this.avgValue = handlerUnitsChange(trendApplication.getValue_avg(),itemApplication.getUnits());
            this.maxValue = handlerUnitsChange(trendApplication.getValue_max(),itemApplication.getUnits());
            this.minValue = handlerUnitsChange(trendApplication.getValue_min(),itemApplication.getUnits());
        }
        this.lastValue = handlerUnitsChange(itemApplication.getLastvalue(),itemApplication.getUnits());
        this.clock = trendApplication != null?trendApplication.getClock():itemApplication.getLastclock();
        this.date = trendApplication != null?new Date(Long.parseLong(trendApplication.getClock())*1000):new Date(Long.parseLong(itemApplication.getLastclock())*1000);
        this.saveTime = new Date();
    }

    public void handlerItemName(String itemName){
        if(itemName.contains("[") && itemName.contains("]")){
            this.itemName = itemName.split("]")[1];
            this.partitionName = itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]"));
            return;
        }
        this.itemName = itemName;
    }


    public String handlerUnitsChange(String value,String units){
        if(!checkStrIsNumber(value)){
            this.units = units;
            return value;
        }
        if(StringUtils.isNotBlank(value) && (value.contains("+") || value.contains("E"))){
            value = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), units);
        if(convertedValue == null || convertedValue.isEmpty()){
            this.units = units;
            return new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        this.units = convertedValue.get("units");
        return new BigDecimal(convertedValue.get("value")).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
    }


    public static boolean checkStrIsNumber(String value){
        if(StringUtils.isBlank(value)){return false;}
        String reg = "^(([0])|([1-9]+[0-9]*.{1}[0-9]+)|([0].{1}[1-9]+[0-9]*)|([1-9][0-9]*)|([0][.][0-9]+[1-9]+))$";
        String scientificNotationRegex = "[-+]?[0-9]+(\\.[0-9]+)?[eE][-+]?[0-9]+";
        if(value.matches(reg) || value.matches(scientificNotationRegex)){
            return true;
        }
        return false;
    }
}
