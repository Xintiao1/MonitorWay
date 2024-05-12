package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

@Data
@ApiModel
@ToString
public class MwModelVirtualDataParam {
    private String id;
    private String UUID;
    private String instanceName;
    private String status;
    private String state;
    private String PId;
    private String type;
    private String ip;
    private Integer monitorServerId;
    private Integer modelInstanceId;
    private String monitorServerName;
    private String hostId;
    private String hostName;
}