package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 */
@Data
@ApiModel
public class CancelZabbixAssetsParam {
    private Integer monitorMode;
    private Integer monitorServerId;
    private String assetsId;
    private String modelIndex;
    private String esId;

}
