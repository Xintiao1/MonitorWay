package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 业务系统可用性DTO
 * @Author gengjb
 * @Date 2023/4/17 16:13
 * @Version 1.0
 **/
@Data
@ApiModel("业务系统可用性DTO")
public class MwVisualizedModuleBusinessUsabilityDto {

    @ApiModelProperty("业务分类名称")
    private String name;

    @ApiModelProperty("业务分类实例状态")
    private String status;
}
