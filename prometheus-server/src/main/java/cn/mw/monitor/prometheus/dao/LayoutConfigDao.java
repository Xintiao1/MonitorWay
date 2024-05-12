package cn.mw.monitor.prometheus.dao;

import cn.mw.monitor.prometheus.dto.LayoutConfigDto;
import cn.mw.monitor.prometheus.dto.PanelConfigDto;

import java.util.List;

public interface LayoutConfigDao {

    List<LayoutConfigDto> getAllLayoutConfigsByUserId(Integer userId);

    LayoutConfigDto getLayoutConfigById(Integer layoutId);

    int deleteLayoutConfig(Integer layoutId);

    int insertLayoutConfig(LayoutConfigDto layoutConfigDto);
}
