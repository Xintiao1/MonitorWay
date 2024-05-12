package cn.mw.monitor.prometheus.dao;

import cn.mw.monitor.prometheus.dto.PanelQueryRelDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PanelQueryRelDao {

    List<PanelQueryRelDto> getPanelQueryRelListByPanelIds(@Param("panelIds") List<Integer> panelIds);

    void batchInsertPanelQueryRelList(@Param("panelQueryRelList") List<PanelQueryRelDto> panelQueryRelList);

    int deletePanelQueryRelByLayoutId(Integer layoutId);
}
