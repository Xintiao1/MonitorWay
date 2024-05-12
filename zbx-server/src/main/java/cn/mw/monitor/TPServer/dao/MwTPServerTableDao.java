package cn.mw.monitor.TPServer.dao;

import cn.mw.monitor.TPServer.dto.AddOrUpdateTPServerParam;
import cn.mw.monitor.TPServer.dto.MwTPServerDTO;
import cn.mw.monitor.TPServer.dto.TPServerDropdownDTO;
import cn.mw.monitor.TPServer.model.MwTPServerTable;
import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import cn.mw.monitor.service.tpserver.dto.MwTpServerCommonsDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/10/30 11:52
 * @Version 1.0
 */
public interface MwTPServerTableDao {

    int delete(List<Integer> id);

    int insert(AddOrUpdateTPServerParam record);

    MwTPServerDTO selectById(Integer id);

    /**
     *
     * @param criteria
     * @return 公有角色查询资产
     */
    List<MwTPServerDTO> selectPubList(Map criteria);


    int update(AddOrUpdateTPServerParam record);

    List<TPServerDropdownDTO> selectDropdownListByType();

    List<DropdownDTO> selectDropdownList();

    List<TPServerDropdownDTO> selectByMainServerIsTrue();

    List<TPServerDropdownDTO> check(@Param("ip") String ip);

    List<String> selectAssetsByMonitorServer(List<Integer> id);

    List<String> selectEngineByMonitorServer(List<Integer> id);

    List<Map<String,String>> fuzzSearchAllFiled(String value);

    List<MwTPServerTable> selectAll();

    List<MwTpServerCommonsDto> selectTpServerByIps(@Param("ips") List<String> ips);
}
