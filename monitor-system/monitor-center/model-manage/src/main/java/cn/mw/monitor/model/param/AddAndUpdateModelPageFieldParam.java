package cn.mw.monitor.model.param;

import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/5 14:24
 *
 */
@Data
@ApiModel
public class AddAndUpdateModelPageFieldParam extends BaseProcessParam {
    @ApiModelProperty("Id")
    private Integer id;

    @ApiModelProperty("模型Id")
    @NotNull
    private Integer modelId;

    @ApiModelProperty("模型属性Id")
    @NotNull
    private Integer modelPropertiesId;

    @ApiModelProperty("字段代码")
    @NotNull
    private String prop;

    @ApiModelProperty("字段名称")
    @NotNull
    private String label;

    @ApiModelProperty("是否显示")
    @NotNull
    private boolean visible;

    @ApiModelProperty("排序")
    @NotNull
    private Integer orderNumber;

    @ApiModelProperty("字段显示类型1字符串,2整形数字,3浮点型数据,4布尔型,5日期类型,6结构体，7:Ip地址 ")
    private Integer type;

    @ApiModelProperty("是否可以展开 ")
    private Integer isTree;
}
