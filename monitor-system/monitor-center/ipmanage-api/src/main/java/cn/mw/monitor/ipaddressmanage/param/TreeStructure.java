package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("插入Id")
public class TreeStructure {
    @ApiModelProperty(value="id")
    private Integer value;
    @ApiModelProperty(value="名称")
    private String label;
    @ApiModelProperty(value="子节点")
    private List<TreeStructure> children;


}

