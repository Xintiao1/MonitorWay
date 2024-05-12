package cn.mw.monitor.server.serverdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Comparator;

/**
 * @author syt
 * @Date 2021/1/6 12:41
 * @Version 1.0
 */
@Data
public class ItemNameRank implements Comparator<ItemNameRank> {
    @ApiModelProperty("资产id")
    private String id;
    // private String assetsId;
    @ApiModelProperty("资产名称")
    private String name;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("lastValue")
    private Double lastValue;
    @ApiModelProperty("分区名称")
    private String type;
    @ApiModelProperty("单位")
    private String units;
    @ApiModelProperty("带单位的value值")
    private String value;


    @Override
    public int compare(ItemNameRank o1, ItemNameRank o2) {
        if(o1.getLastValue()>o2.getLastValue()){
            return -1;
        }else if(o1.getLastValue()==o2.getLastValue()){
            return 0;
        }else {
            return 1;
        }
    }
}
