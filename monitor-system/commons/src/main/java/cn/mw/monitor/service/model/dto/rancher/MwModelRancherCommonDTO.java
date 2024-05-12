package cn.mw.monitor.service.model.dto.rancher;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwModelRancherCommonDTO {

    @ApiModelProperty("实例名称")
    private String rancherName;

    @ApiModelProperty("集群数据")
    private List<MwModelRancherClusterCommonDTO> clusterList;

}
