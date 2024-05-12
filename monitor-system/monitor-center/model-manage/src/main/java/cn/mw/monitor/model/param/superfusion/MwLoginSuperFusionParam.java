package cn.mw.monitor.model.param.superfusion;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/13
 */
@Data
public class MwLoginSuperFusionParam extends BaseParam {
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("模型实例名称")
    private String modelInstanceName;
    @ApiModelProperty("依附实例Id")
    private Integer relationInstanceId;

    private String url;
    private String userName;
    private String password;
    private String port;

}
