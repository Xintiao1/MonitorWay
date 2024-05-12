package cn.huaxing.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gengjb
 * @description 华兴数据缓存
 * @date 2023/9/12 14:44
 */
@Data
@ApiModel("华兴数据缓存DTO")
public class HuaxingVisualizedDataDto {

    @ApiModelProperty("图类型")
    private Integer chartType;

    @ApiModelProperty("分区名称")
    private String partitionName;

    @ApiModelProperty("数据字符串")
    private String dataStr;

    @ApiModelProperty("数据结果")
    private List<Map<String,Object>> dataList;
}
