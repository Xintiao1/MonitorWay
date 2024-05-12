package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mwpaas.common.utils.DateUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author gengjb
 * @description 监控项表格DTO
 * @date 2023/12/25 13:57
 */
@Data
public class VisualizedItemTableDto {

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("资产IP")
    private String assetsIp;

    @ApiModelProperty("值")
    private String value;

    @ApiModelProperty("分区名称")
    private String name;

    @ApiModelProperty("接口利用率(入)")
    private String inValue;

    @ApiModelProperty("接口利用率(出)")
    private String outValue;

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("入时间")
    private String inTime;

    @ApiModelProperty("出时间")
    private String outTime;

    public void extractFrom(MwVisualizedCacheHistoryDto cacheDto, MwTangibleassetsDTO mwTangibleassetsDTO){
        this.assetsName = mwTangibleassetsDTO.getAssetsName() == null?mwTangibleassetsDTO.getInstanceName():mwTangibleassetsDTO.getAssetsName();
        this.assetsIp = mwTangibleassetsDTO.getInBandIp();
        this.value = cacheDto.getAvgValue()+cacheDto.getUnits();
        this.name = cacheDto.getName();
        Date date = new Date();
        date.setTime(Long.parseLong(cacheDto.getClock())*1000);
        this.time = DateUtils.formatDateTime(date);
    }
}
