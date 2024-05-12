package cn.mw.monitor.visualized.service;

import cn.mw.monitor.service.visualized.param.MwDigitalTwinItemParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author gengjb
 * @description 数字孪生实现接口
 * @date 2023/8/2 9:42
 */
public interface MwDigitalTwinService {

    /**
     * 查询资产对应的zabbix监控项
     * @param itemParam
     * @return
     */
    Reply getAssetsItemInfo(MwDigitalTwinItemParam itemParam);
}
