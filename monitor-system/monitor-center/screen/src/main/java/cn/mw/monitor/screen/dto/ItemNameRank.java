package cn.mw.monitor.screen.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.model.dto.DetailPageJumpDto;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/4/14 13:30
 */
@Data
public class ItemNameRank extends DetailPageJumpDto implements Comparator<ItemNameRank> {
    @ApiModelProperty("资产id")
    private String id;
   // private String assetsId;
    @ApiModelProperty("资产名称")
    private String name;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("lastValue")
    private Double lastValue;
    private Double sortlastValue;
    @ApiModelProperty("分区名称")
    private String type;
    @ApiModelProperty("单位")
    private String units;
    @ApiModelProperty("带单位的value值")
    private String value;
    private String assetsId;
    private Integer monitorServerId;

    private Boolean isWebMonitor;

    private String assetsStatus;

    private Integer lossPingValue;

    private String lossPingUnit;

    private Double linkSortValue;

    //发送流量
    private Double sendLastValue;

    //接收流量
    private Double acceptLastValue;


    //发送流量带单位
    private String sendStrLastValue;

    //接收流量带单位
    private String acceptStrLastValue;

    //磁盘总容量
    private String diskTotal;

    //磁盘已使用容量
    private String diskUsed;

    //磁盘未使用容量
    private String diskNotUsed;

  //10:告警次数；11：告警资产分类；12：告警级别
  private int alertType;

  private Double sortTotalValue;

  //用于进度条展示
  private Double progress;




    @Override
    public int compare(ItemNameRank o1, ItemNameRank o2) {

//            if(o1.getSortlastValue()>o2.getSortlastValue()){
//                return -1;
//            }else if(o1.getSortlastValue()==o2.getSortlastValue()){
//                return 0;
//            }else {
//                return 1;
//            }
        return -Double.compare(o1.getSortlastValue(),o2.getSortlastValue());

    }

    public void extractFrom(ItemApplication itemApplication, MwTangibleassetsTable tangibleassetsTable){
        this.assetsId = tangibleassetsTable.getAssetsId();
        this.id = tangibleassetsTable.getId()==null?String.valueOf(tangibleassetsTable.getModelInstanceId()):tangibleassetsTable.getId();
        this.ip = tangibleassetsTable.getInBandIp();
        this.name = tangibleassetsTable.getAssetsName();
        this.progress = itemApplication.getLastvalue() == null?0:Double.parseDouble(itemApplication.getLastvalue());
        this.monitorServerId = tangibleassetsTable.getMonitorServerId();
        this.sortlastValue = itemApplication.getLastvalue() == null?0:Double.parseDouble(itemApplication.getLastvalue());
        handlerUnitsChange(itemApplication.getLastvalue(),itemApplication.getUnits());
    }

    //单位转换处理
    public void handlerUnitsChange(String value,String units){
        if(StringUtils.isNotBlank(value) && (value.contains("+") || value.contains("E"))){
            value = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), units);
        if(convertedValue == null || convertedValue.isEmpty()){
            this.lastValue = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            this.units = units;
            return;
        }
        this.lastValue = new BigDecimal(convertedValue.get("value")).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
        this.units = convertedValue.get("units");
    }
}
