package cn.mw.monitor.service.model.param;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/9 14:23
 * @Version 1.0
 */
@Data
public class MwModelEngineDTO {
    //id
    private String id;
    //引擎id
    private String proxyId;
    //引擎名称
    private String engineName;

    private String monitorServerId;

}
