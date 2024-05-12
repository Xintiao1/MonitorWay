package cn.mw.monitor.prometheus.dao;

import cn.mw.monitor.prometheus.dto.PanelConfigDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PanelConfigDao {

    List<PanelConfigDto> getAllPanelConfigsByLayoutIds(@Param("layoutIds") List<Integer> layoutIds);

    PanelConfigDto getPanelConfigById(Integer panelId);

    int deletePanelConfig(Integer panelId);

    int insertPanelConfig(PanelConfigDto panelConfigDto);

    int updatePanelConfig(PanelConfigDto panelConfigDto);

    int deletePanelConfigByLayoutId(Integer layoutId);
}
