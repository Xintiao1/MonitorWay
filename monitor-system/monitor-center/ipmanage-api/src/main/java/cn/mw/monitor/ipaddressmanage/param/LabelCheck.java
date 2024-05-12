package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("属性")
public class LabelCheck {
    //主键
    @ApiModelProperty(value="下拉的对应对应下拉id")
    Integer dropId;


    @ApiModelProperty(value="下拉的对应对应下拉id")
    String labelDrop;


    @ApiModelProperty(value="选项值内容")
    private String dropValue;

    @ApiModelProperty(value="选项值内容")
    private Integer dropKey;
}
