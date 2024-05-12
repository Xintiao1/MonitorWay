package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2020/3/16
 */
@Data
@ApiModel
public class QueryModelAssetsTriggerParam extends BaseParam {
    /**
     * zabbix模版id
     */
    @ApiModelProperty(value = "templateId")
    private String templateId;

    /**
     * 资产ID
     */
    @ApiModelProperty(value = "assetsId")
    private String assetsId;

    /**
     * 资产名称
     */
    private String instanceName;
    /**
     * 监控服务器id
     */
    private Integer monitorServerId;

    private Integer modelId;

    private String ip;
}
