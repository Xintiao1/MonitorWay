package cn.mw.monitor.engineManage.dao;

import cn.mw.monitor.engineManage.api.param.engineManage.AddOrUpdateEngineManageParam;
import cn.mw.monitor.engineManage.dto.EngineDropdownDTO;
import cn.mw.monitor.engineManage.dto.EngineProxyDTO;
import cn.mw.monitor.service.dropdown.param.SelectCharDropDto;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
import java.util.Map;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
public interface MwEngineManageTableDao {

    int delete(List<String> id);

    int insert(AddOrUpdateEngineManageParam record);

    MwEngineManageDTO selectById(String id);

    List<MwEngineManageDTO> selectByIds(List<String> ids);

    /**
     * @param criteria
     * @return 公有角色查询资产
     */
    List<MwEngineManageDTO> selectPubList(Map criteria);


    int update(AddOrUpdateEngineManageParam record);

    List<EngineDropdownDTO> selectDropdownList(@Param("monitorServerId") int monitorServerId,@Param("proxyIds") List<String> proxyIds);

    List<EngineDropdownDTO> selectDropdownBatchList(List<Integer> monitorServerIds);

    List<SelectCharDropDto> selectDropdown();

    List<MwEngineManageDTO> selectByServerIp(@Param("serverIp") String serverIp);

    EngineProxyDTO selectTPProxyById(@Param("id") String id);

    int updateProxyMonitorNums(@Param("monitorHostNumber") int monitorHostNumber, @Param("monitoringItemsNumber") int monitoringItemsNumber, @Param("id") String id);

    int deleteByMonitorServerIds(List<Integer> monitorServerIds);

    List<String> selectAssetsByEngine(List<String> id);

    List<Map<String,String>> fuzzSearchAllFiled(String value);
}
