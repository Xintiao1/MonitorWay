package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.visualized.constant.VisualizedConstant;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName
 * @Description 容器排行DTO
 * @Author gengjb
 * @Date 2023/6/7 15:52
 * @Version 1.0
 **/
@Data
@ApiModel("容器排行DTO")
public class MwVisualizedContaineRankingDto {

    @ApiModelProperty("节点名称")
    private String nodeName;

    @ApiModelProperty("pod名称")
    private String podName;

    @ApiModelProperty("值")
    private String value;

    @ApiModelProperty("值")
    private String name;

    @ApiModelProperty("单位")
    private String units;

    @ApiModelProperty("排序值")
    private Double sortValue;

    public void extractFrom(MwPromMetricInfo info, String value,String units){
        this.nodeName = info.getNode() == null?info.getInstance():info.getNode();
        this.podName = info.getPod() == null?info.getInstance():info.getPod();
        this.name = nodeName == null?podName:nodeName;
        if(StringUtils.isNotBlank(name) && name.contains(VisualizedConstant.EXPORTER_NODE_CLUSTER)){
            this.name = name.replace(VisualizedConstant.EXPORTER_NODE_CLUSTER,"");
        }
        this.units = units;
        if(MwVisualizedUtil.checkStrIsNumber(value)){
            this.value = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
            this.sortValue = new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
        }else{
            this.value = value;
            this.sortValue = 0.0;
        }
    }

}
