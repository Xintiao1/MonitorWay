package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/16
 */
@Data
public class MwAllLabelDTO {
    private Integer id;

    private String prop;

    private String label;

    @ApiModelProperty(value = "1:文本2:时间3:下拉框")
    private String inputFormat;

    private String url;

    private Boolean chooseAdd;

    private Boolean isRequired;

    private List<String> labelValue;


}
