package cn.mw.monitor.prometheus.service;

import cn.mw.monitor.prometheus.dto.LayoutConfigDto;
import cn.mw.monitor.prometheus.dto.PanelConfigDto;
import cn.mw.monitor.prometheus.vo.PanelQueryParamVo;
import cn.mwpaas.common.model.Reply;

public interface IPanelConfigService {

    Reply getAllPanelConfigs(Integer userId);

    Reply getPanelData(PanelQueryParamVo panelQueryParamVo) throws Exception;

    Reply insertPanelData(PanelConfigDto panelConfigDto);

    Reply updatePanelData(PanelConfigDto panelConfigDto);

    Reply deletePanelData(Integer panelId);

    Reply insertLayoutConfig(LayoutConfigDto layoutConfigDto);

    Reply getAllQuerySql();

    Reply getAllPrometheusProperties();

    Reply deleteLayoutConfig(Integer id);
}
