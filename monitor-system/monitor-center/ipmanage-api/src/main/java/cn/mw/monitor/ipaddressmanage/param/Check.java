package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("选项")
public class Check {
    //主键
    @ApiModelProperty(value="选项的text文本")
    String label;

    @ApiModelProperty(value="选项值")
    String keyValue;

    @ApiModelProperty(value="选项值")
    boolean radioStatus;

    @ApiModelProperty(value="选项填写值")
    String keyTestValue;

    @ApiModelProperty(value="选项填写类型：0.ipv4 1.ipv6")
    boolean idType;

    @ApiModelProperty(value="随机字符串标识")
    private Integer parentId;

    @ApiModelProperty(value="随机字符串标识")
    private String radom;

    @ApiModelProperty(value="随机字符串标识")
    private Integer id;

    @ApiModelProperty(value="搜索框是否出现：true:false")
    boolean newRadio=true;
}
