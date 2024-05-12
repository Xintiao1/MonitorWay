package cn.mw.monitor.solarwind.dao;

import cn.mw.module.solarwind.dto.*;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/6/29 10:59
 */
public interface MwMonitorSolarReportDao {

    int selectSolarDayCount(String date);

    int insertAllday(List<SolarDetailDto> allDay);

    int insertAlldayWorkTime(List<SolarDetailDto> allDayWorkTime);

    int insertWorkDay(List<SolarDetailDto> allDay);

    int insertWorkDayWorkTIme(List<SolarDetailDto> allDayWorkTime);

    List<InterfaceReportDto> selectList(Map criteria);

    List<MwHistoryDTO> selectInHistoryList(SolarReportDto solarReportDto);

    List<MwHistoryDTO> selectOutHistoryList(SolarReportDto solarReportDto);

    List<SolarDetailDto> groupSelectList(GroupDto groupDto0);
}
