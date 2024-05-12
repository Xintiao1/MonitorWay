package cn.mw.monitor.engineManage.service.impl;

import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.engineManage.api.exception.DeleteEngineException;
import cn.mw.monitor.engineManage.api.param.engineManage.AddOrUpdateEngineManageParam;
import cn.mw.monitor.engineManage.api.param.engineManage.DeleteEngineManageParam;
import cn.mw.monitor.engineManage.api.param.engineManage.QueryEngineManageParam;
import cn.mw.monitor.engineManage.dao.MwEngineManageTableDao;
import cn.mw.monitor.engineManage.dto.EngineDropdownDTO;
import cn.mw.monitor.engineManage.dto.EngineManageTableDTO;
import cn.mw.monitor.engineManage.dto.EngineProxyDTO;
import cn.mw.monitor.engineManage.dto.ProxyDTO;
import cn.mw.monitor.engineManage.service.MwEngineManageService;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.dropdown.param.SelectCharDropDto;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by baochengbin on 2020/3/17
 */
@Service
@Slf4j
@Transactional
public class MwEngineManageServiceImpl implements MwEngineManageService, MwEngineCommonsService {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/EngineManage");

    @Resource
    private MwEngineManageTableDao mwEngineManageTableDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWCommonService commonService;

    @Autowired
    private MWUserService mwUserService;

    @Value("${asset.scan.proxy.port}")
    private int proxyPort;

    /**
     * 根据引擎ID取引擎信息
     *
     * @param id 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(String id) {
        try {
            MwEngineManageDTO msDto = mwEngineManageTableDao.selectById(id);
            DataPermission dataPermission = commonService.getDataPermissionDetail(DataType.ENGINE, id);
            msDto.setPrincipal(dataPermission.getPrincipal());
            msDto.setDepartment(dataPermission.getDepartment());
            msDto.setGroup(dataPermission.getGroups());

            EngineManageTableDTO engineManageTableDTO = CopyUtils.copy(EngineManageTableDTO.class, msDto);
            // usergroup重新赋值使页面可以显示
            List<Integer> groupIds = new ArrayList<>();
            msDto.getGroup().forEach(
                    groupDTO -> groupIds.add(groupDTO.getGroupId())
            );
            engineManageTableDTO.setGroupIds(groupIds);
            // department重新赋值使页面可以显示
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (msDto.getDepartment() != null) {
                msDto.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
                engineManageTableDTO.setOrgIds(orgNodes);
            }
            // user重新赋值
            List<Integer> userIds = new ArrayList<>();
            msDto.getPrincipal().forEach(
                    userDTO -> userIds.add(userDTO.getUserId())
            );
            engineManageTableDTO.setPrincipal(userIds);
            engineManageTableDTO.setKeyConsistency("********");
            engineManageTableDTO.setSharedKey("********");
            engineManageTableDTO.setPublisher("********");
            engineManageTableDTO.setTitle("********");
            logger.info("EngineManage_LOG[]EngineManage[]扫描引擎管理[]根据自增序列ID取引擎信息[]{}", id);
            return Reply.ok(engineManageTableDTO);
        } catch (Exception e) {
            log.error("fail to selectById with id={}, cause:{}", id, e);
            return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240101, ErrorConstant.ENGINEMANAGE_MSG_240101);
        }
    }

    /**
     * 分页查询引擎信息
     *
     * @param qsParam
     * @return
     */
    @Override
    public Reply selectList(QueryEngineManageParam qsParam) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> typeIdList = mwUserService.getAllTypeIdList(userInfo, DataType.ENGINE);
            List<MwEngineManageDTO> mwEngineManages = new ArrayList();
            PageHelper.startPage(qsParam.getPageNumber(), qsParam.getPageSize());
            Map pubCriteria = PropertyUtils.describe(qsParam);
            pubCriteria.put("list", Joiner.on(",").join(typeIdList));
            pubCriteria.put("systemUser", userInfo.isSystemUser());
            mwEngineManages = mwEngineManageTableDao.selectPubList(pubCriteria);

            //获取数据权限
            List<String> ids = new ArrayList<>();
            for (MwEngineManageDTO engine : mwEngineManages) {
                ids.add(engine.getId());
            }
            List<DataPermission> dataPermissionList = commonService.getDataAuthByIds(DataType.ENGINE, ids);
            Map<String, DataPermission> permissionMap = new HashMap<>();
            for (DataPermission permission : dataPermissionList) {
                permissionMap.put(permission.getId(), permission);
            }
            for (MwEngineManageDTO engine : mwEngineManages) {
                DataPermission dataPermission = permissionMap.get(engine.getId());
                if (dataPermission != null){
                    engine.setGroup(dataPermission.getGroups());
                    engine.setDepartment(dataPermission.getDepartment());
                    engine.setPrincipal(dataPermission.getPrincipal());
                }
            }

            // 直接在zabbix 查询数量
            Map<String, Integer> countMap = new HashMap<>();
            Map<Integer, List<String>> groupMap = mwEngineManages.stream()
                    .collect(Collectors.groupingBy(MwEngineManageDTO::getMonitorServerId, Collectors.mapping(MwEngineManageDTO::getProxyId, Collectors.toList())));
            for (Map.Entry<Integer, List<String>> map : groupMap.entrySet()) {
                MWZabbixAPIResult result = mwtpServerAPI.proxyGet(map.getKey(), map.getValue());
                if (result != null && !result.isFail()) {
                    List<ProxyDTO> proxyDTOS = JSONArray.parseArray(result.getData().toString(), ProxyDTO.class);
                    if (proxyDTOS != null && proxyDTOS.size() > 0) {
                        proxyDTOS.forEach(proxy -> {
                            MWZabbixAPIResult itemResult = mwtpServerAPI.itemGetbyHostId(map.getKey(), proxy.getHosts().stream().map(host -> host.getHostid()).collect(Collectors.toList()));
                            log.info("查询引擎对应主机信息Param"+map.getKey()+":::"+proxy.getHosts().stream().map(host -> host.getHostid()).collect(Collectors.toList()));
                            log.info("查询引擎对应主机信息结果"+itemResult);
                            if (itemResult != null && !itemResult.isFail()) {
                                JsonNode jsonNode = (JsonNode) itemResult.getData();
                                countMap.put(map.getKey() + "_" + proxy.getProxyid() + "_" + "host", proxy.getHosts() != null ? proxy.getHosts().size() : 0);
                                countMap.put(map.getKey() + "_" + proxy.getProxyid() + "_" + "item", jsonNode != null ? jsonNode.size() : 0);
                            }
                        });
                    }
                }
            }
            //共享秘钥一致性\共享秘钥\发行者\主题 加密处理，前台页面不显示，以*替代
            for (MwEngineManageDTO dto : mwEngineManages) {
                dto.setKeyConsistency("********");
                dto.setSharedKey("********");
                dto.setPublisher("********");
                dto.setTitle("********");
                Integer hostNum = countMap.get(dto.getMonitorServerId() + "_" + dto.getProxyId() + "_" + "host");
                Integer itemNum = countMap.get(dto.getMonitorServerId() + "_" + dto.getProxyId() + "_" + "item");
                dto.setMonitorHostNumber(hostNum == null ? 0 : hostNum);
                dto.setMonitoringItemsNumber(itemNum == null ? 0 : itemNum);
            }
            PageInfo pageInfo = new PageInfo<>(mwEngineManages);
            pageInfo.setList(mwEngineManages);
            logger.info("EngineManage_LOG[]EngineManage[]扫描引擎管理[]分页查询扫描引擎信息[]{}[]", mwEngineManages);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectListEngineManage with qsParam={}, cause:{}", qsParam, e);
            return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240102, ErrorConstant.ENGINEMANAGE_MSG_240102);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }


    /**
     * 更新引擎信息
     *
     * @param auParam
     * @return
     */
    @Override
    public Reply update(AddOrUpdateEngineManageParam auParam) {
        if ("5".equals(auParam.getMode())) {
            auParam.setEncryption("0");
        }
        if ("1".equals(auParam.getEncryption()) || "0".equals(auParam.getEncryption())) {
            auParam.setTitle(null);
            auParam.setPublisher(null);
            auParam.setSharedKey(null);
            auParam.setKeyConsistency(null);
            if ("0".equals(auParam.getEncryption())) {
                auParam.setEncryption(null);
                auParam.setPort(null);
            }
        } else if ("2".equals(auParam.getEncryption())) {
            auParam.setTitle(null);
            auParam.setPublisher(null);
        } else {
            auParam.setSharedKey(null);
            auParam.setKeyConsistency(null);
        }
        if (null != auParam.getServerIp() && !"".equals(auParam.getServerIp())) {
            List<MwEngineManageDTO> engineList = mwEngineManageTableDao.selectByServerIp(auParam.getServerIp());
            if (engineList.size() > 0) {
                for (MwEngineManageDTO engine : engineList) {
                    if (!engine.getId().equals(auParam.getId())) {
                        return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240103, "IP地址不可重复！");
                    }
                }
            }
        }
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI
                .updateProxy(auParam.getMonitorServerId(), auParam.getProxyId(), auParam.getProxyName(), auParam.getServerIp(), auParam.getMode(), auParam.getPort(), "", auParam.getProxyAddress());
        if (mwZabbixAPIResult.isFail()) {
            return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240103, "修改代理失败！");
        }
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        mwEngineManageTableDao.update(auParam);
        //删除负责人
        deleteMapperAndPerm(auParam.getId());
        //添加负责人
        addMapperAndPerm(auParam);
        return Reply.ok("更新成功！");
    }

    /**
     * 新增引擎信息
     *
     * @param auParam
     * @return
     */
    @Override
    public Reply insert(AddOrUpdateEngineManageParam auParam) throws Exception {
        auParam.setId(UUIDUtils.getUUID());
        if ("1".equals(auParam.getEncryption())) {
            auParam.setTitle(null);
            auParam.setPublisher(null);
            auParam.setSharedKey(null);
            auParam.setKeyConsistency(null);
        } else if ("2".equals(auParam.getEncryption())) {
            auParam.setTitle(null);
            auParam.setPublisher(null);
        } else {
            auParam.setSharedKey(null);
            auParam.setKeyConsistency(null);
        }
        if (null != auParam.getServerIp() && !"".equals(auParam.getServerIp())) {
            List<MwEngineManageDTO> engineList = mwEngineManageTableDao.selectByServerIp(auParam.getServerIp());
            if (engineList.size() > 0) {
                return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240104, "IP地址不可重复！");
            }
        }
        //先判断引擎名称是否含有中文
        Pattern pattern = Pattern.compile("[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\”|\\“|\\？|\\：|\\；|\\【|\\】]");
        boolean hasChinese = pattern.matcher(auParam.getEngineName()).find();
        if (hasChinese) {
            // 生成代理名
            StringBuffer sb = new StringBuffer().append("PROXY_")
                    .append(auParam.getServerIp()).append(auParam.getId());
            auParam.setProxyName(sb.toString());
        } else {
            //代理名和引擎名相同
            auParam.setProxyName(auParam.getEngineName());
        }
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI
                .createProxy(auParam.getMonitorServerId(), auParam.getProxyName(), auParam.getServerIp(), auParam.getMode(), auParam.getPort(), "", auParam.getProxyAddress());
        if (mwZabbixAPIResult != null && !mwZabbixAPIResult.isFail()) {
            JsonNode proxy_data = (JsonNode) mwZabbixAPIResult.getData();
            String proxyId = proxy_data.get("proxyids").get(0).asText();
            auParam.setProxyId(proxyId);
        } else {
            if(mwZabbixAPIResult != null && mwZabbixAPIResult.getCode() == -32602){
                return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240104, "该引擎名称已存在");
            }else{
                return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240104, "添加代理失败！");
            }
        }
        auParam.setCreator(iLoginCacheInfo.getLoginName());
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setDeleteFlag(false);
        mwEngineManageTableDao.insert(auParam);
        //添加负责人
        addMapperAndPerm(auParam);
        return Reply.ok("新增成功！");
    }

    /**
     * 删除引擎信息
     *
     * @param dParam
     * @return
     */

    @Override
    public Reply delete(DeleteEngineManageParam dParam) throws Throwable {
        //先判断有没有资产关联引擎
        List<String> list = mwEngineManageTableDao.selectAssetsByEngine(dParam.getIdList());
        if (list.size() > 0) {
            throw new Throwable("所删除的引擎中有关联资产，无法删除");
        }
        //删除引擎数据
        mwEngineManageTableDao.delete(dParam.getIdList());
        dParam.getIdList().forEach(
                id -> {
                    //删除负责人
                    deleteMapperAndPerm(id);
                }
        );
        //删除zabbix中的代理
        for (Map.Entry<Integer, List<String>> entry : dParam.getProxyIdList().entrySet()) {
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.proxyDelete(entry.getKey(), entry.getValue());
            if (mwZabbixAPIResult.isFail()) {
                throw new DeleteEngineException(ErrorConstant.ENGINEMANAGE_MSG_240105, "Zabbix服务删除操作异常");
            }
        }
        return Reply.ok("删除成功");
    }

    @Override
    public Reply selectDropdownList(int monitorServerId, boolean selectFlag, boolean addLocalhost) {
        try {
            logger.info("ACCESS_LOG[]org[]引擎管理[]查询引擎信息[]{}[]");
            List<SelectCharDropDto> selectCharDropDtos = null;
            if(selectFlag){
                selectCharDropDtos = mwEngineManageTableDao.selectDropdown();
                List<SelectCharDropDto> newlist = new ArrayList<>();
                SelectCharDropDto localhost = SelectCharDropDto.genLocalhost();
                newlist.add(localhost);
                if(null != selectCharDropDtos){
                    newlist.addAll(selectCharDropDtos);
                }
                return Reply.ok(newlist);
            }else{
                log.info("monitorServerId:"+monitorServerId);
                MWZabbixAPIResult result = mwtpServerAPI.proxyInfoget(monitorServerId);
                log.info("proxyInfoget result:"+result);
                List<String> proxyIds = new ArrayList<>();
                if (result != null && !result.isFail()) {
                    List<ProxyDTO> proxyDTOS = JSONArray.parseArray(result.getData().toString(), ProxyDTO.class);
                    if (proxyDTOS != null && proxyDTOS.size() > 0) {
                        proxyIds = proxyDTOS.stream().map(s->s.getProxyid()).collect(Collectors.toList());
                    }
                }
                List<EngineDropdownDTO> newList = new ArrayList<>();
                log.info("proxyInfoget proxyIds:"+proxyIds);
                if (CollectionUtils.isNotEmpty(proxyIds)){
                    List<EngineDropdownDTO> engineDropdownDTOS = mwEngineManageTableDao.selectDropdownList(monitorServerId,proxyIds);
                    log.info("proxyInfoget engineDropdownDTOS:"+engineDropdownDTOS);
                    newList.addAll(engineDropdownDTOS);
                    log.info("proxyInfoget newList:"+newList);
                }
                if(addLocalhost){
                    EngineDropdownDTO localhost = EngineDropdownDTO.genLocalhost();
                    newList.add(localhost);
                }
                return Reply.ok(newList);
            }
        } catch (Exception e) {
            log.error("fail to selectDropdownList ,monitorServerId:{} cause:{}", monitorServerId, e);

        }
        return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240102, ErrorConstant.ENGINEMANAGE_MSG_240102);
    }

    @Override
    public Reply selectDropdownBatchList(List<Integer> monitorServerIds) {
        try {
            Map<String, List<EngineDropdownDTO>> map = new HashMap<>();
            logger.info("ACCESS_LOG[]org[]引擎管理[]查询引擎信息[]{}[]");
            if (monitorServerIds != null && monitorServerIds.size() > 0) {
                List<EngineDropdownDTO> dropdownDTOS = mwEngineManageTableDao.selectDropdownBatchList(monitorServerIds);
                map = dropdownDTOS.stream().collect(Collectors.groupingBy(EngineDropdownDTO::getMonitorServerId));
            }
            return Reply.ok(map);
        } catch (Exception e) {
            log.error("fail to selectDropdownList ,monitorServerId:{} cause:{}", monitorServerIds, e);
            return Reply.fail(ErrorConstant.ENGINEMANAGECODE_240102, ErrorConstant.ENGINEMANAGE_MSG_240102);
        }
    }

    /**
     * 删除负责人
     *
     * @param id
     */
    private void deleteMapperAndPerm(String id) {
        DeleteDto deleteDto = DeleteDto.builder().typeId(id).type(DataType.ENGINE.getName()).build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 添加负责人
     *
     * @param uParam
     */
    private void addMapperAndPerm(AddOrUpdateEngineManageParam uParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(uParam.getGroupIds())
                .userIds(uParam.getPrincipal())
                .orgIds(uParam.getOrgIds())
                .typeId(uParam.getId())
                .type(DataType.ENGINE.getName())
                .desc(DataType.ENGINE.getDesc()).build();
        //添加负责人
        mwCommonService.addMapperAndPerm(insertDto);
    }

    @Override
    public void updateMonitorNums(boolean isAdd, String engineId, String hostId) {
        EngineProxyDTO engineProxyDTO = mwEngineManageTableDao.selectTPProxyById(engineId);
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyHostId(engineProxyDTO.getMonitorServerId(), hostId);
        int monitorItemsNum = 0;
        int monitorHostNum = (isAdd) ? 1 : -1; //监控主机增加或减少的数量
        if (!result.isFail()) {
            JsonNode items = (JsonNode) result.getData();
            monitorItemsNum = (isAdd) ? items.size() : -items.size();//监控监控项增加或减少的数量
        }
        int hosts = monitorHostNum + engineProxyDTO.getMonitorHostNumber();
        int items = monitorItemsNum + engineProxyDTO.getMonitoringItemsNumber();
        mwEngineManageTableDao.updateProxyMonitorNums(hosts, items, engineId);
    }

    @Override
    public void deleteEngineByMonitorServerIds(List<Integer> monitorServerIds) {
        mwEngineManageTableDao.deleteByMonitorServerIds(monitorServerIds);
    }

    @Override
    public MwEngineManageDTO selectEngineByIdNoPerm(String id) {
        MwEngineManageDTO msDto = mwEngineManageTableDao.selectById(id);
        return msDto;
    }

    @Override
    public List<MwEngineManageDTO> selectEngineByIdsNoPerm(List<String> ids) {
        List<MwEngineManageDTO> msDtos = mwEngineManageTableDao.selectByIds(ids);
        return msDtos;
    }

    @Override
    public Map<String, String> genProxyIpMap(List<MwTangibleassetsDTO> mwTangAssetses) {
        Map<String ,String> ret = new HashMap<>();
        Set<String> pollingEngineSet = new HashSet<>();
        for(MwTangibleassetsDTO mwTangibleassetsDTO:mwTangAssetses){
            if(StringUtils.isNotEmpty(mwTangibleassetsDTO.getPollingEngine())){
                pollingEngineSet.add(mwTangibleassetsDTO.getPollingEngine());
            }
        }

        if(pollingEngineSet.size() > 0){
            //设置代理服务器ip
            return doProxyIpMap(pollingEngineSet);
        }
        return ret;
    }

    @Override
    public Map<String, String> genProxyIpMapByScanSuccess(List<ScanResultSuccess> list) {
        Set<String> pollingEngineSet = new HashSet<>();
        for(ScanResultSuccess scanResultSuccess:list){
            if(StringUtils.isNotEmpty(scanResultSuccess.getPollingEngine())){
                pollingEngineSet.add(scanResultSuccess.getPollingEngine());
            }
        }
        if(CollectionUtils.isEmpty(pollingEngineSet))return new HashMap<>();
        //设置代理服务器ip
        return doProxyIpMap(pollingEngineSet);
    }

    public List<ProxyInfo> genProxyInfoById(String proxyId){
        MwEngineManageDTO mwEngineManageDTO = selectEngineByIdNoPerm(proxyId);
        if(null != mwEngineManageDTO){
            List<ProxyInfo> proxyInfos = new ArrayList<>();
            ProxyInfo proxyInfo = new ProxyInfo(mwEngineManageDTO.getProxyAddress() ,proxyPort);
            proxyInfos.add(proxyInfo);
            return proxyInfos;
        }
        return null;
    }

    @Override
    public List<ProxyInfo> genProxyIp(List<MwTangibleassetsDTO> mwTangAssetses) {
        Set<String> pollingEngineSet = new HashSet<>();
        for(MwTangibleassetsDTO mwTangibleassetsDTO:mwTangAssetses){
            if(StringUtils.isNotEmpty(mwTangibleassetsDTO.getPollingEngine())){
                pollingEngineSet.add(mwTangibleassetsDTO.getPollingEngine());
            }
        }

        List<MwEngineManageDTO> engineManageDTOS = CollectionUtils.isEmpty(pollingEngineSet)
                ?new ArrayList<>():selectEngineByIdsNoPerm(new ArrayList(pollingEngineSet));
        List<ProxyInfo> proxyInfos = new ArrayList<>();
        for(MwEngineManageDTO mwEngineManageDTO : engineManageDTOS){
            ProxyInfo proxyInfo = new ProxyInfo(mwEngineManageDTO.getProxyAddress() ,proxyPort);
            proxyInfos.add(proxyInfo);
        }
        return proxyInfos;
    }

    private Map<String, String> doProxyIpMap(Set<String> pollingEngineSet){
        //设置代理服务器ip
        List<MwEngineManageDTO> engineManageDTOS = selectEngineByIdsNoPerm(new ArrayList(pollingEngineSet));
        Map<String ,String> proxyIpMap = engineManageDTOS.stream().collect(Collectors.toMap(MwEngineManageDTO::getId,
                MwEngineManageDTO::getProxyAddress));
        return proxyIpMap;
    }


    /**
     * 引擎管理模糊搜索所有字段联想
     *
     * @param value
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(String value) {
        //根据值模糊查询数据
        List<Map<String, String>> fuzzSeachAllFileds = mwEngineManageTableDao.fuzzSearchAllFiled(value);
        Set<String> fuzzSeachData = new HashSet<>();
        if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
            for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                String engine_name = fuzzSeachAllFiled.get("engine_name");
                String server_ip = fuzzSeachAllFiled.get("server_ip");
                String monitor_server_name = fuzzSeachAllFiled.get("monitor_server_name");
                String mode = fuzzSeachAllFiled.get("mode");
                String encryption = fuzzSeachAllFiled.get("encryption");
                String compress = fuzzSeachAllFiled.get("compress");
                String creator = fuzzSeachAllFiled.get("creator");
                String modifier = fuzzSeachAllFiled.get("modifier");

                if (StringUtils.isNotBlank(engine_name) && engine_name.contains(value)) {
                    fuzzSeachData.add(engine_name);
                }
                if (StringUtils.isNotBlank(server_ip) && server_ip.contains(value)) {
                    fuzzSeachData.add(server_ip);
                }
                if (StringUtils.isNotBlank(monitor_server_name) && monitor_server_name.contains(value)) {
                    fuzzSeachData.add(monitor_server_name);
                }
                if (StringUtils.isNotBlank(mode) && mode.contains(value)) {
                    fuzzSeachData.add(mode);
                }
                if (StringUtils.isNotBlank(encryption) && encryption.contains(value)) {
                    fuzzSeachData.add(encryption);
                }
                if (StringUtils.isNotBlank(compress) && compress.contains(value)) {
                    fuzzSeachData.add(compress);
                }
                if (StringUtils.isNotBlank(creator) && creator.contains(value)) {
                    fuzzSeachData.add(creator);
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
}
