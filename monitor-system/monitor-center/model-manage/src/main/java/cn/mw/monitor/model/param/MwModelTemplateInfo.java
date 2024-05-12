package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/31
 */
@Data
public class MwModelTemplateInfo {
    private String templateName;
    private String templateId;
    private String serverTemplateId;
    private Integer serverId;
    private Integer monitorMode;
    private String monitorModeName;
    private String brand;
    private String description;
    private String specification;
    //modelId 相当于AssetsSubTypeId
    private Integer modelId;
}
