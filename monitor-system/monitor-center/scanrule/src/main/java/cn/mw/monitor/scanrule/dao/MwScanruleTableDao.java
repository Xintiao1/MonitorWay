package cn.mw.monitor.scanrule.dao;

import cn.mw.monitor.scanrule.api.param.scanrule.AddScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.QueryScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.UpdateScanruleParam;
import cn.mw.monitor.scanrule.dto.*;
import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by baochengbin on 2020/3/17.
 */
public interface MwScanruleTableDao {

    int delete(List<Integer> idList);

    int insert(AddScanruleParam record);

    int insertBatch(List<AddScanruleParam> ruleList);

    MwScanruleDTO selectById(Integer id);

    List<MwScanruleDTO> selectList(QueryScanruleParam record);

    int updateBatch(List<UpdateScanruleParam> updateList);

    int update(UpdateScanruleParam record);

    int createIpRang(List<MwIpRangDTO> mwIpRangDTO);

    int deleteIpRang(List<Integer> ruleId);

    int createIpAddresslist(List<MwIpAddressListDTO> ipAddressListDTO);

    int deleteIpAddresslist(List<Integer> ruleId);

    int createIpAddresses(List<MwIpAddressesDTO> ipAddressesDTO);

    int deleteIpAddresses(List<Integer> ruleId);

    int createAgentRule(MwAgentruleDTO agentruleDTO);

    int deleteAgentRule(List<Integer> ruleId);

    int createSnmpv1Rule(MwRulesnmpv1DTO rulesnmpv1DTO);

    int deleteSnmpv1Rule(List<Integer> ruleId);

    int createSnmpv3Rule(MwRulesnmpDTO rulesnmpDTO);

    int deleteSnmpv3Rule(List<Integer> ruleId);

    int createPortRule(MwPortruleDTO portruleDTO);

    int createIcmpRule(MwIcmpruleDTO icmpruleDTO);

    int deletePortRule(List<Integer> ruleId);

    int deleteIcmpRule(List<Integer> ruleId);

    List<Map<String,String>> fuzzSearchAllFiled(String value);

    List<MwModelAssetsGroupTable> selectGroupServerMap(@Param("assetsSubTypeId") Integer assetsSubTypeId);

    List<MwAssetsScanGroupTable> selectScanGroupServerMap(@Param("assetsSubTypeId") Integer assetsSubTypeId);
}
