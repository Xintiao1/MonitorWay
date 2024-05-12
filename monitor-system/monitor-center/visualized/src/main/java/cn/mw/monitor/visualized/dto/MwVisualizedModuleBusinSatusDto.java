package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description BC大屏数据标题DTO
 * @Author gengjb
 * @Date 2023/7/4 10:06
 * @Version 1.0
 **/
@Data
@ApiModel("BC大屏数据标题DTO")
public class MwVisualizedModuleBusinSatusDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("业务系统名称")
    private String modelSystemName;

    @ApiModelProperty("标题名称")
    private String titleName;
}
