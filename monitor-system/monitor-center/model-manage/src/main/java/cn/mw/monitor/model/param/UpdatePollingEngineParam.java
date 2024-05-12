package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePollingEngineParam {
    private String proxyId;//代服务Id
    private String engineId;//轮询引擎Id
    private Integer instanceId;
    private String esId;
    private String instanceName;
    private String modelIndex;
    private String modelId;
    private String assetsId;
    private int monitorServerId;

}
