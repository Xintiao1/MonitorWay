package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 数据库session会话数统计DTO
 * @Author gengjb
 * @Date 2023/4/24 15:50
 * @Version 1.0
 **/
@Data
@ApiModel("数据库session会话数统计DTO")
public class MwVisualizedModuleDBSessionStatDto {

    @ApiModelProperty("主机名称")
    private String name;


    @ApiModelProperty("会话数")
    private String sessionCount;

    @ApiModelProperty("排序字段")
    private Integer sortValue;
}
