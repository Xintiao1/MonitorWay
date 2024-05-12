package cn.mw.monitor.wireless.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @Date 2021/6/23
 */
@Data
@ApiModel(value = "查询QueryWirelessDataShowParam实体类")
public class QueryWirelessDataShowParam extends BaseParam {
    @ApiModelProperty("时间类型，1：hour 2:day 3:week 4:month 5:自定义")
    private Integer dateType;
    private int monitorServerId;
    private String assetsId;
    private String itemId;
    private Integer assetsTypeId;
    private Integer assetsTypeSubId;
    private Integer dataType;
    private String queryName;
    private String queryValue;
    private String startDate;
    private String endDate;
    @ApiModelProperty("设备类型 1：SSID端， 2:Client 客户端")
    private Integer deviceType;
    @ApiModelProperty("数据下标 获取前 indexTop 条数据")
    private Integer indexTop;

    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;
}
