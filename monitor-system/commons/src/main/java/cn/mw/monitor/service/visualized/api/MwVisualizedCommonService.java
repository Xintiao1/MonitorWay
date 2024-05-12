package cn.mw.monitor.service.visualized.api;

import cn.mw.monitor.service.visualized.dto.MwDigitalTwinAlertDto;
import cn.mw.monitor.service.visualized.param.MwDigitalTwinItemParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author gengjb
 * @description 可视化公共类
 * @date 2023/8/17 10:24
 */
public interface MwVisualizedCommonService {

    /**
     * 查询资产对应的zabbix监控项
     * @param itemParam
     * @return
     */
    Reply getAssetsItemInfo(MwDigitalTwinItemParam itemParam);


    /**
     * 获取告警信息
     * @param paramList
     * @return
     */
    MwDigitalTwinAlertDto getAssetsAlertInfo(List<MwDigitalTwinItemParam> paramList);
}
