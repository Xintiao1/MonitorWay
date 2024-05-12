package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/5/21
 */
@Data
@ApiModel(description = "web监测Trigger数据")
public class MwModelWebMonitorTriggerParam {
    private String description;
    private String hostName;
    private String webName;
    private String code;
    private String key;
    private String priority;

}
