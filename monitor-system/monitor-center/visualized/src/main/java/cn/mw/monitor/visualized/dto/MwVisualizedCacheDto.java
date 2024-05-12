package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName
 * @Description 可视化缓存DTO
 * @Author gengjb
 * @Date 2023/5/18 10:41
 * @Version 1.0
 **/
@Data
@ApiModel("可视化缓存DTO")
@Slf4j
public class MwVisualizedCacheDto {

    @ApiModelProperty("主键ID")
    private String cacheId;

    @ApiModelProperty("资产ID")
    private String assetsId;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("监控项名称")
    private String itemName;

    @ApiModelProperty("值")
    private String value;

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

    //修改人
    @ApiModelProperty("修改人")
    private String modifier;

    //修改时间
    @ApiModelProperty("修改时间")
    private Date modificationDate;

    @ApiModelProperty("数据名称")
    private String name;

    @ApiModelProperty("排序值")
    private Double sortValue;


    public void extractFrom(ItemApplication itemApplication, MwTangibleassetsTable tangibleassetsTable){
        this.assetsId = tangibleassetsTable.getId();
        this.assetsName = tangibleassetsTable.getAssetsName();
        this.hostId = itemApplication.getHostid();
        this.creator = "admin";
        this.createDate = new Date();
        this.itemName = itemApplication.getName();
        this.clock = itemApplication.getLastclock();
        handlerUnitsChange(itemApplication.getLastvalue(),itemApplication.getUnits());
    }


    //单位转换处理
    public void handlerUnitsChange(String value,String units){
        try {
            if(!MwVisualizedUtil.checkStrIsNumber(value)){
                this.value = value;
                this.units = units;
                return;
            }
            if(StringUtils.isNotBlank(value) && (value.contains("+") || value.contains("E"))){
                value = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
            }
            if(StringUtils.isNotBlank(value)){
                if(StringUtils.isBlank(units)){
                    log.info("handlerUnitsChange value"+value);
                    this.value = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                    this.units = units;
                    return;
                }
                Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), units);
                if(convertedValue == null || convertedValue.isEmpty()){
                    this.value = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                    this.units = units;
                    return;
                }
                this.value = new BigDecimal(convertedValue.get("value")).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                this.units = convertedValue.get("units");
            }
        }catch (Throwable e){
            log.error("handlerUnitsChange() error",e);
        }

    }
}
