package cn.mw.monitor.wireless.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @Date 2021/6/16
 */
@Data
@ApiModel(value = "查询QueryWirelessDeviceParam实体类")
public class QueryWirelessClientParam extends BaseParam {
    private int monitorServerId;
    private String assetId;

    private String queryName;
    private String queryValue;

    private String sortMW_CLIENTS_TXBYTES;
    private String sortMW_CLIENTS_RXBYTES;
    private String sortMW_CLIENTS_CHANNELS;
    private String sortMW_CLIENTS_RSSI;

    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;
}
