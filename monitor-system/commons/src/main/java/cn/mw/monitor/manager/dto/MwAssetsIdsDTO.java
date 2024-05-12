package cn.mw.monitor.manager.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/16 10:46
 * @Version 1.0
 */
@Data
public class MwAssetsIdsDTO {
    //资产id
    private String id;
    //监控服务器id
    private int monitorServerId;
    //主机id
    private String hostId;
    //轮询引擎
    private String pollingEngine;
    //模板Id
    private String templateId;

    public void extractFrom(MwTangibleassetsTable tangibleassetsTable){
        this.id = tangibleassetsTable.getId() == null?tangibleassetsTable.getModelInstanceId().toString():tangibleassetsTable.getId();
        this.monitorServerId = tangibleassetsTable.getMonitorServerId();
        this.hostId = tangibleassetsTable.getAssetsId();
        this.pollingEngine = tangibleassetsTable.getPollingEngine();
        this.templateId = tangibleassetsTable.getTemplateId();
    }
}
