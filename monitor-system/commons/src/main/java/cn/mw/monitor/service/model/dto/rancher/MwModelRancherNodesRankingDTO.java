package cn.mw.monitor.service.model.dto.rancher;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 节点排行DTO
 * @Author gengjb
 * @Date 2023/5/21 14:41
 * @Version 1.0
 **/
@Data
@ApiModel("节点排行DTO")
public class MwModelRancherNodesRankingDTO {

    @ApiModelProperty("节点名称")
    private String nodeName;

    @ApiModelProperty("值")
    private String value;

    @ApiModelProperty("单位")
    private String units;

    public void extractFrom(String nodeName,String value,String units){
       this.nodeName = nodeName;
       this.value = value;
       this.units = units;
    }
}
