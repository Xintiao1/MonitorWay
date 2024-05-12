package cn.mw.monitor.service.server.api.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.param.QueryItemTrendParam;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author gengjb
 * @date 2024/1/2 9:35
 */
@Data
public class ItemTrendDto {

    @ApiModelProperty("数据ID")
    private String id;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    @ApiModelProperty("监控项名称")
    private String name;

    @ApiModelProperty("值")
    private String value;

    @ApiModelProperty("单位")
    private String units;

    @ApiModelProperty("时间")
    private String time;

    public void extractFrom(ItemApplication itemApplication, QueryItemTrendParam param,ItemTrendApplication trendApplication,Integer serverId){
        if(param.getDataType() == 1){
            this.id = param.getIdMap().get(serverId+itemApplication.getHostid()+handlerName(itemApplication.getName()));
        }else{
            this.id = param.getIdMap().get(serverId+itemApplication.getHostid());
        }
        this.name = itemApplication.getName();
        this.value = trendApplication.getValue_avg();
        this.units = itemApplication.getUnits();
        Date time = new Date();
        time.setTime(Long.parseLong(trendApplication.getClock())*1000);
        this.time = DateUtils.formatDateTime(time);
    }

    private String handlerName(String itemName){
        if(StringUtils.isBlank(itemName) || !itemName.contains("]")){return "";}
        return itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]"));
    }
}
