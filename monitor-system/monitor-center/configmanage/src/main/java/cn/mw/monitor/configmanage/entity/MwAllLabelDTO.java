package cn.mw.monitor.configmanage.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author bkc
 * @date 2020/9/7
 */
@Data
public class MwAllLabelDTO {
    private Integer id;

    private String prop;

    private String label;

    @ApiModelProperty(value = "1:文本2:时间3:下拉框")
    private String inputFormat;

    private String url;
}
