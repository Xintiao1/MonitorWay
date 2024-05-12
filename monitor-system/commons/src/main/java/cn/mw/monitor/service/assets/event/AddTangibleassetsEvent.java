package cn.mw.monitor.service.assets.event;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddTangibleassetsEvent extends Event{

    private AddUpdateTangAssetsParam addTangAssetsParam;

    public String toSystemInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("id:").append(addTangAssetsParam.getId())
                .append(",").append("templateName:").append(addTangAssetsParam.getTemplateId())
                .append(",").append("zabbixHostId:").append(addTangAssetsParam.getAssetsId())
                .append(",").append("instanceName:").append(addTangAssetsParam.getInstanceName())
                .append(",").append("proxyServerId:").append(addTangAssetsParam.getProxyServerId())
        ;
        return stringBuffer.toString();
    }
}
