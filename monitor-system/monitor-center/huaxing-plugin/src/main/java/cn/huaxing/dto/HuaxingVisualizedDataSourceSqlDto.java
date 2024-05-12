package cn.huaxing.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 华兴可视化数据源SQL DTO
 * @date 2023/8/28 11:32
 */
@Data
@ApiModel("华兴可视化数据源SQL DTO")
public class HuaxingVisualizedDataSourceSqlDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("数据源ID")
    private String dataSourceId;

    @ApiModelProperty("需要查询的SQL")
    private String sqlString;

    @ApiModelProperty("图类型")
    private Integer charType;

    @ApiModelProperty("分区名称")
    private String partitionName;
}
