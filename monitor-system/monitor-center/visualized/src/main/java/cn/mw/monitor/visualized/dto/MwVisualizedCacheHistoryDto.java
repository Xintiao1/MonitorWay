package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName
 * @Description 缓存监控历史数据
 * @Author gengjb
 * @Date 2023/5/21 0:05
 * @Version 1.0
 **/
@Data
@ApiModel("缓存监控历史数据DTO")
public class MwVisualizedCacheHistoryDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("资产ID")
    private String assetsId;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("监控项名称")
    private String itemName;

    @ApiModelProperty("平均值")
    private String avgValue;

    @ApiModelProperty("最大值")
    private String maxValue;

    @ApiModelProperty("最小值")
    private String minValue;

    @ApiModelProperty("单位")
    private String units;

    @ApiModelProperty("监控项产生时间")
    private String clock;

    //创建人
    @ApiModelProperty("创建人")
    private String creator;

    //创建时间
    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("数据产生时间")
    private Date valueDate;

    private String time;

    @ApiModelProperty("监控项分区名称")
    private String name;

    public void extractFrom(ItemApplication itemApplication, MwTangibleassetsTable tangibleassetsTable, ItemTrendApplication trendApplication){
        this.assetsId = tangibleassetsTable.getId();
        this.assetsName = tangibleassetsTable.getAssetsName();
        this.hostId = itemApplication.getHostid();
        this.creator = "admin";
        this.createDate = new Date();
        this.itemName = itemApplication.getName();
        this.avgValue = handlerUnitsChange(trendApplication.getValue_avg(),itemApplication.getUnits());
        this.maxValue = handlerUnitsChange(trendApplication.getValue_max(),itemApplication.getUnits());
        this.minValue = handlerUnitsChange(trendApplication.getValue_min(),itemApplication.getUnits());
        this.clock = trendApplication.getClock();
    }

    //单位转换处理
    public String handlerUnitsChange(String value,String units){
        if(!MwVisualizedUtil.checkStrIsNumber(value)){
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
}
