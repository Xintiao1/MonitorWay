package cn.mw.monitor.assets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/5/25 18:26
 * @Version 1.0
 */
@Data
public class AssetsValueMappingDto {
    @ApiModelProperty("原始值")
    private String value;
    @ApiModelProperty("原始值映射到的值")
    private String newvalue;
}
