package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName
 * @Description 分区资产数量统计DTO
 * @Author gengjb
 * @Date 2023/6/12 9:30
 * @Version 1.0
 **/
@Data
@ApiModel("分区资产数量统计DTO")
public class MwVisualizedAeestsCountDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("分区名称")
    private String partitionName;

    @ApiModelProperty("分区数量")
    private Integer partitionNumber;

    @ApiModelProperty("存储时间")
    private String time;


    public void extractFrom(String partitionName,Integer partitionNumber,String time){
       this.partitionName = partitionName;
       this.partitionNumber = partitionNumber;
       this.time = time;
    }
}
