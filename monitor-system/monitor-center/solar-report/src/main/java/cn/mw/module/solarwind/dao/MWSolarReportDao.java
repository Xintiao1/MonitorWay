package cn.mw.module.solarwind.dao;


import cn.mw.module.solarwind.dto.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/6/23 10:21
 */
@Qualifier("solarwindSqlSessionTemplate")
public interface MWSolarReportDao {
    int selectInterface();

    List<SolarDetailDto> selectInterfaceDetail(SolarReportDto solarReportDto);

    List<String> selectCarrierName();

    List<Integer> selectInterfaceIds();

    List<Map> selectCaption(@Param("carrierName") String carrierName);

    List<InterfaceTable> selectList(Map criteria);

    ProportionDto newSelectProportion(SolarReportDto solarReportDto);

    List<MwHistoryDTO> selectInHistory(SolarReportDto solarReportDto);

    List<MwHistoryDTO> selectOutHistory(SolarReportDto solarReportDto);

    List<SolarDetailDto>  groupSelectList(GroupDto groupDto0);

    String getCaption(Integer interfaceID);


    void selectAll();
}
