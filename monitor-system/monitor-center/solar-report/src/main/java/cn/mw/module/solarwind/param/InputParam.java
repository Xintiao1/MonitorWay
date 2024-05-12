package cn.mw.module.solarwind.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/9/10 9:28
 */
@Data
public class InputParam {
    @ApiModelProperty("yyyyMMdd")
    private String exportDate;
}
