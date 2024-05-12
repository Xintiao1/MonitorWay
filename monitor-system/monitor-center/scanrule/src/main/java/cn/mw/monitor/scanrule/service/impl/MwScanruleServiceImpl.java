package cn.mw.monitor.scanrule.service.impl;

import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.user.dto.MwLoginUserDto;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.scan.dao.ScanResultTableDao;
import cn.mw.monitor.scan.dataview.ScanProcView;
import cn.mw.monitor.scanrule.api.param.scanrule.AddScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.QueryScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.UpdateScanruleParam;
import cn.mw.monitor.scanrule.dao.MwScanruleTableDao;
import cn.mw.monitor.scanrule.dto.*;
import cn.mw.monitor.scanrule.service.MwScanruleService;
import cn.mw.monitor.service.scanrule.model.Perform;
import cn.mw.monitor.snmp.model.ExceuteInfo;
import cn.mw.monitor.snmp.service.IMonitor;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by baochengbin on 2020/3/17.
 */
@Service
@Slf4j
@Transactional
public class MwScanruleServiceImpl implements MwScanruleService {

    @Resource
    private MwScanruleTableDao mwScanruleTableDao;

    @Resource
    ScanResultTableDao scanResultTableDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    IMonitor monitor;

    /**
     * 根据规则ID取规则信息
     *
     * @param id 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(Integer id) {
        try {
            MwScanruleDTO msDto = mwScanruleTableDao.selectById(id);
            log.info("SCANRULE_LOG[]Scanrule[]扫描规则管理[]根据自增序列ID取规则信息[]{}", id);
            log.info(msDto.toString());
            return Reply.ok(msDto);
        } catch (Exception e) {
            log.error("fail to selectById with id={}, cause:{}", id, e.getMessage());
            return Reply.fail(ErrorConstant.SCANRULECODE_220100, ErrorConstant.SCANRULE_MSG_220100);
        }
    }

    /**
     * 分页查询规则信息
     *
     * @param qsParam
     * @return
     */
    @Override
    public Reply selectList(QueryScanruleParam qsParam) {
        try {
            PageHelper.startPage(qsParam.getPageNumber(), qsParam.getPageSize());

            List<MwScanruleDTO> mwScanList = mwScanruleTableDao.selectList(qsParam);
            //设置扫描执行状态信息
            ScanProcView scanProcView = new ScanProcView();
            if(null != mwScanList && mwScanList.size() > 0){
                mwScanList.forEach(value ->{
                    ExceuteInfo exceuteInfo = monitor.getExceuteInfoById(value.getScanruleId());
                    Perform perform = new Perform(false, 0);
                    if(null != exceuteInfo){
                        scanProcView.setExceuteInfo(exceuteInfo);
                        if(!exceuteInfo.isScanDoned()) {
                            perform.setDisappear(true);
                            perform.setPercentage(scanProcView.getProcessCount());
                        }
                        value.setScanEndDate(exceuteInfo.getEndTime());
                    }
                    value.setPerform(perform);
                });
            }

            PageInfo pageInfo = new PageInfo<>(mwScanList);
            pageInfo.setList(mwScanList);

            log.info("SCANRULE_LOG[]SCANRULE[]扫描规则管理[]分页查询扫描规则信息[]{}[]", mwScanList);

            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to selectListscanrule with qsParam={}, cause:{}", qsParam, e.getMessage());
            return Reply.fail(ErrorConstant.SCANRULECODE_220101, ErrorConstant.SCANRULE_MSG_220101);
        }finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
        }
    }

    /**
     * 更新规则信息
     *
     * @param auParam
     * @return
     */
    @Override
    public Reply update(UpdateScanruleParam auParam, boolean updMapping) throws Exception{
        //获取用户登录名
        String userName;
        MwLoginUserDto localTread = iLoginCacheInfo.getLocalTread();
        if(localTread == null){
            userName = iLoginCacheInfo.getLoginName();
        }else{
            userName = localTread.getLoginName();
        }
        auParam.setModifier(userName);
        mwScanruleTableDao.update(auParam);

        if(updMapping) {
            List<Integer> ruleId = new ArrayList<Integer>();
            ruleId.add(auParam.getId());
            mwScanruleTableDao.deleteIpRang(ruleId);
            mwScanruleTableDao.deleteIpAddresses(ruleId);
            mwScanruleTableDao.deleteIpAddresslist(ruleId);
            mwScanruleTableDao.deletePortRule(ruleId);
            mwScanruleTableDao.deleteIcmpRule(ruleId);
            mwScanruleTableDao.deleteAgentRule(ruleId);
            mwScanruleTableDao.deleteSnmpv1Rule(ruleId);
            mwScanruleTableDao.deleteSnmpv3Rule(ruleId);
            addDetailRule(auParam);
        }
        return Reply.ok("更新成功");
    }

    /**
     * 新增规则信息
     *
     * @param auParam
     * @return
     */
    @Override
    public Reply insert(AddScanruleParam auParam) throws Exception{
        String userName;
        MwLoginUserDto localTread = iLoginCacheInfo.getLocalTread();
        if(localTread == null){
            userName = iLoginCacheInfo.getLoginName();
        }else{
            userName = localTread.getLoginName();
        }
        auParam.setCreator(userName);
        auParam.setModifier(userName);
        mwScanruleTableDao.insert(auParam);
        addDetailRule(auParam);
        return Reply.ok(auParam);
    }

    private void addDetailRule(AddScanruleParam auParam) throws Exception{
        if (null != auParam.getMwIpRangDTO() && auParam.getMwIpRangDTO().size() > 0) {
            auParam.getMwIpRangDTO().forEach(mwIpRangDTO -> mwIpRangDTO.setRuleId(auParam.getId()));
            mwScanruleTableDao.createIpRang(auParam.getMwIpRangDTO());
        }
        if (null != auParam.getIpAddressListDTO() && auParam.getIpAddressListDTO().size() > 0) {
            auParam.getIpAddressListDTO().forEach(ipAddressListDTO -> ipAddressListDTO.setRuleId(auParam.getId()));
            mwScanruleTableDao.createIpAddresslist(auParam.getIpAddressListDTO());
        }
        if (null != auParam.getIpAddressesDTO() && auParam.getIpAddressesDTO().size() > 0) {
            auParam.getIpAddressesDTO().forEach(ipAddressDTO -> ipAddressDTO.setRuleId(auParam.getId()));
            mwScanruleTableDao.createIpAddresses(auParam.getIpAddressesDTO());
        }

        List<MwAgentruleDTO> agentruleDTOList = auParam.getAgentruleDTOList();
        if(null != agentruleDTOList){
            agentruleDTOList.forEach(value ->{
                value.setRuleId(auParam.getId());
                mwScanruleTableDao.createAgentRule(value);
            });
        }

        List<MwRulesnmpv1DTO> rulesnmpv1DTOList = auParam.getRulesnmpv1DTOList();
        if(null != rulesnmpv1DTOList){
            rulesnmpv1DTOList.forEach(value ->{
                value.setRuleId(auParam.getId());
                mwScanruleTableDao.createSnmpv1Rule(value);
            });
        }

        List<MwRulesnmpDTO> rulesnmpDTOList = auParam.getRulesnmpDTOList();
        if(null != rulesnmpDTOList){
            rulesnmpDTOList.forEach(value -> {
                value.setRuleId(auParam.getId());
                mwScanruleTableDao.createSnmpv3Rule(value);
            });
        }

        List<MwPortruleDTO> portruleDTOList = auParam.getPortruleDTOList();
        if(null != portruleDTOList){
            portruleDTOList.forEach(value -> {
                value.setRuleId(auParam.getId());
                mwScanruleTableDao.createPortRule(value);
            });
        }

        List<MwIcmpruleDTO> icmpruleDTOList = auParam.getIcmpruleDTOList();
        if(null != icmpruleDTOList){
            icmpruleDTOList.forEach(value -> {
                value.setRuleId(auParam.getId());
                mwScanruleTableDao.createIcmpRule(value);
            });
        }
    }

    /**
     * 删除规则信息
     *
     * @param ids
     * @return
     */
    @Override
    public Reply delete(List<Integer> ids) throws Exception{
        mwScanruleTableDao.delete(ids);
        mwScanruleTableDao.deleteIpRang(ids);
        mwScanruleTableDao.deleteIpAddresses(ids);
        mwScanruleTableDao.deleteIpAddresslist(ids);
        mwScanruleTableDao.deletePortRule(ids);
        mwScanruleTableDao.deleteAgentRule(ids);
        mwScanruleTableDao.deleteSnmpv1Rule(ids);
        mwScanruleTableDao.deleteSnmpv3Rule(ids);
        mwScanruleTableDao.deleteIcmpRule(ids);
        scanResultTableDao.batchDeleteSuccess(ids);
        scanResultTableDao.batchDeleteFail(ids);
        return Reply.ok("删除成功");
    }


    /**
     * 资产发现规则模糊搜索所有字段联想
     *
     * @param value
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(String value) {
        //根据值模糊查询数据
        List<Map<String, String>> fuzzSeachAllFileds = mwScanruleTableDao.fuzzSearchAllFiled(value);
        Set<String> fuzzSeachData = new HashSet<>();
        if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
            for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                String scanruleName = fuzzSeachAllFiled.get("scanruleName");
                String modifier = fuzzSeachAllFiled.get("modifier");
                String scanStartDate = fuzzSeachAllFiled.get("scanStartDate");
                if (StringUtils.isNotBlank(scanruleName) && scanruleName.contains(value)) {
                    fuzzSeachData.add(scanruleName);
                }
                if (StringUtils.isNotBlank(scanStartDate) && scanStartDate.contains(value)) {
                    fuzzSeachData.add(scanStartDate);
                }
                if (StringUtils.isNotBlank(modifier) && modifier.contains(value)) {
                    fuzzSeachData.add(modifier);
                }
            }
        }
        Map<String, Set<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
        return Reply.ok(fuzzyQuery);
    }

    @Override
    public Reply selectGroupServerMap(Integer assetsSubTypeId) {
        try {
            List<MwModelAssetsGroupTable> list = mwScanruleTableDao.selectGroupServerMap(assetsSubTypeId);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to selectGroupServerMap case by {}", e);
            return Reply.fail(500, "数据获取成功失败");
        }
    }

    @Override
    public Reply selectGroupServerMapList() {
        List<MwAssetsScanGroupTable> mwAssetsGroupTables = mwScanruleTableDao.selectScanGroupServerMap(null);
        log.info("selectGroupServerMapList");
        return Reply.ok(mwAssetsGroupTables);
    }

}
