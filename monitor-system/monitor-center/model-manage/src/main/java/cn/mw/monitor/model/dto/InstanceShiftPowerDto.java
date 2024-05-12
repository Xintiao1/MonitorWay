package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/3/8 12:11
 */
@Data
@ApiModel
public class InstanceShiftPowerDto {
    @ApiModelProperty("用户Ids")
    private List<Integer> userIds;
    @ApiModelProperty("实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("esId")
    private String esId;
    private String modelIndex;

}
