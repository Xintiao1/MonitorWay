package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 表空间明细数据
 * @Author gengjb
 * @Date 2023/4/18 16:14
 * @Version 1.0
 **/
@Data
@ApiModel("表空间明细数据")
public class DBTableSpaceDetailedDto {

    @ApiModelProperty("表空间名称")
    private String tbsSpaceName;

    @ApiModelProperty("表空间使用率")
    private String value;

    @ApiModelProperty("排序值")
    private Double sortValue;
}
