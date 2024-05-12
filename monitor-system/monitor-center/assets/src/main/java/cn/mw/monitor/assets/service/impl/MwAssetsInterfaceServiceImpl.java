package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.assets.dao.MwAssetsInterfaceDao;
import cn.mw.monitor.assets.dto.AssetsItemGetDTO;
import cn.mw.monitor.assets.dto.AssetsValueMappingDto;
import cn.mw.monitor.assets.dto.AssetsValuemapDto;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.assets.api.IMWBatchAssetsProcFinListener;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.BatchAddAssetsEvent;
import cn.mw.monitor.service.assets.event.BatchDeleteAssetsEvent;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.assets.param.RefreshInterfaceParam;
import cn.mw.monitor.service.assets.service.MwAssetsInterfaceService;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.configmanage.ConfigManageCommonService;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.model.listener.MWModelAssetsListener;
import cn.mw.monitor.service.model.param.MwModelFilterInterfaceParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.scan.MWSnmpSearchService;
import cn.mw.monitor.service.scan.model.Device;
import cn.mw.monitor.service.scan.model.InterfaceInfo;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.scan.param.RuleParam;
import cn.mw.monitor.service.scan.param.SearchParam;
import cn.mw.monitor.service.scan.param.SnmpSearchAction;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.NetListDto;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2022/5/11
 */
@Component
@Order(value = 1)
@Slf4j
/*
 * 调用snmpSearchService信息,并把结果放到threadlocal
 * 设置监听器优先级,不能超过99999,否则无法清除threadlocal信息
 */
public class MwAssetsInterfaceServiceImpl implements MwAssetsInterfaceService, IMWBatchAssetsProcFinListener
        , MWModelAssetsListener, InitializingBean {
    @Value("${interface.batchInsertSize}")
    private int batchInsertSize;

    @Value("${monitor.interface.debug}")
    private boolean debug;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWSnmpSearchService snmpSearchService;

    @Resource
    private MwAssetsInterfaceDao mwAssetsInterfaceDao;

    @Autowired
    private ConfigManageCommonService configManageCommonService;

    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private MwServerService service;
    //设备类型集合（服务器，网络设备，安全设备）
    private static List<Integer> assetsTypeIds= Arrays.asList(1,2,3);

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        if (event instanceof BatchAddAssetsEvent) {
            BatchAddAssetsEvent batchAddAssetsEvent = (BatchAddAssetsEvent) event;
            processBatchAddAseets(batchAddAssetsEvent);
        }

        if (event instanceof AddTangibleassetsEvent) {
            AddTangibleassetsEvent addTangibleassetsEvent = (AddTangibleassetsEvent) event;
            processAddAsset(addTangibleassetsEvent);
        }

        if (event instanceof BatchDeleteAssetsEvent) {
            BatchDeleteAssetsEvent batchDeleteAssetsEvent = (BatchDeleteAssetsEvent) event;
            processDelAssets(batchDeleteAssetsEvent);
        }
        return null;
    }

    @Override
    public void refreshInterfaceInfo(RefreshInterfaceParam refreshInterfaceParam) {
        Map<String, MwTangibleassetsDTO> ipMap = new HashMap<>();
        Reply reply = null;

        InterfaceRefreshType interfaceRefreshType = InterfaceRefreshType.valueOf(refreshInterfaceParam.getRefreshType());
        switch (interfaceRefreshType) {
            case All://所有snmp的资产
                reply = mwModelViewCommonService.findTopoModelAssetsBySNMP();
                break;
            case Cust://指定资产Id（InstanceId）查询接口
                Map map = new HashedMap();
                map.put("ids", refreshInterfaceParam.getAssetIds());
                reply = mwModelViewCommonService.findTopoModelAssets(MwTangibleassetsDTO.class, map);
        }

        if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
            List<SearchParam> searchParams = new ArrayList<>();

            List<MwTangibleassetsDTO> mwTangibleassetsDTOS = (List<MwTangibleassetsDTO>) reply.getData();

            Map<String, String> proxyIpMap = mwEngineCommonsService.genProxyIpMap(mwTangibleassetsDTOS);
            if (null != mwTangibleassetsDTOS) {
                for (MwTangibleassetsDTO mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                    if (debug) {
                        log.info("refreshInterfaceInfo:{},{}",mwTangibleassetsDTO.debugInfo() , mwTangibleassetsDTO.toString());
                    }
                    SearchParam searchParam = new SearchParam();
                    searchParam.setIp(mwTangibleassetsDTO.getInBandIp());

                    boolean hasSNMPInfo = false;
                    RuleParam ruleParam = new RuleParam();
                    MwSnmpv1AssetsDTO snmpv1v2 = mwTangibleassetsDTO.getSnmpv1AssetsDTO();
                    if (null != snmpv1v2 && (null != snmpv1v2.getAssetsId() || StringUtils.isNotEmpty(snmpv1v2.getCommunity()))) {
                        ruleParam.extractFromMwSnmpv1AssetsDTO(mwTangibleassetsDTO.getSnmpv1AssetsDTO());
                        hasSNMPInfo = true;
                    }

                    MwSnmpAssetsDTO snmpv3 = mwTangibleassetsDTO.getSnmpAssetsDTO();
                    if (null != snmpv3 && (null != snmpv3.getAssetsId() || StringUtils.isNotEmpty(snmpv3.getSecName()))) {
                        ruleParam.extractFromMwSnmpAssetsDTO(mwTangibleassetsDTO.getSnmpAssetsDTO());
                        hasSNMPInfo = true;
                    }

                    String proxyServerIp = proxyIpMap.get(mwTangibleassetsDTO.getPollingEngine());
                    searchParam.setProxyServerIp(proxyServerIp);

                    if (hasSNMPInfo) {
                        searchParam.setRuleParam(ruleParam);
                        searchParams.add(searchParam);
                        ipMap.put(mwTangibleassetsDTO.getInBandIp(), mwTangibleassetsDTO);
                    }
                }
            }

            //获取设备数据
            MWSnmpSearchService newMwSnmpSearchService = (MWSnmpSearchService) SpringUtils.getBean(MWSnmpSearchService.class);
            SnmpSearchAction snmpSearchAction = new SnmpSearchAction();
            snmpSearchAction.enableInterfaceMacRouteInfo();
            List<Device> deviceList = newMwSnmpSearchService.findDeviceInterfaceInfoBySearchParam(searchParams, snmpSearchAction);

            Map<String, List<AssetsInterfaceDTO>> newAssetsInterfaceMap = new HashMap<>();
            if (deviceList.size() > 0) {
                for (Device device : deviceList) {
                    List<InterfaceInfo> interfaceInfos = device.getInterfaceInfoList();
                    List<AssetsInterfaceDTO> assetsInterfaceDTOS = new ArrayList<>();
                    String deviceId = "";
                    String hostId = "";
                    if (ipMap != null && ipMap.containsKey(device.getIp())) {
                        MwTangibleassetsDTO assetDto = ipMap.get(device.getIp());
                        if (StringUtils.isNotEmpty(assetDto.getId())) {
                            deviceId = assetDto.getId();//资产实例Id
                            hostId = assetDto.getAssetsId();//主机hostId
                            newAssetsInterfaceMap.put(deviceId, assetsInterfaceDTOS);
                        }
                    }

                    if (null != interfaceInfos && interfaceInfos.size() > 0) {
                        for (InterfaceInfo interfaceInfo : interfaceInfos) {
                            AssetsInterfaceDTO assetsInterfaceDTO = new AssetsInterfaceDTO();
                            assetsInterfaceDTO.extractFromInterfaceInfo(interfaceInfo);
                            assetsInterfaceDTO.setAssetsId(deviceId);//设置资产实例id
                            assetsInterfaceDTO.setHostIp(device.getIp());//设置主机Ip
                            assetsInterfaceDTO.setHostId(hostId);//设置主机hostId
                            assetsInterfaceDTOS.add(assetsInterfaceDTO);
                        }
                    }
                }

                if (debug) {
                    if (null != deviceList) {
                        for (Device device : deviceList) {
                            log.info("refreshInterfaceInfo {}", device.toString());
                            List<InterfaceInfo> interfaceInfos = device.getInterfaceInfoList();
                            if (null != interfaceInfos) {
                                for (InterfaceInfo interfaceInfo : interfaceInfos) {
                                    log.info(interfaceInfo.toString());
                                }
                            }
                        }
                    } else {
                        log.info("refreshInterfaceInfo deviceList is null");
                    }
                }
            }

            //查询已有数据
            List<AssetsInterfaceDTO> oldList = mwAssetsInterfaceDao.getAllInterfaceDTO();
            Map<String, List<AssetsInterfaceDTO>> oldAddAssetsInterfaceMap = new HashMap<>();
            Map<String, List<AssetsInterfaceDTO>> oldDelAssetsInterfaceMap = new HashMap<>();
            Map<String, List<String>> oldDisAssetsInterfaceMap = new HashMap<>();
            //数据库中所有的接口数据根据资产Id分组
            oldAddAssetsInterfaceMap = oldList.stream().filter(s -> s.getAssetsId() != null).collect(Collectors.groupingBy(s -> s.getAssetsId()));
            //没有设置接口描述的数据
            oldDelAssetsInterfaceMap = oldList.stream().filter(s -> s.getAssetsId() != null && s.getEditorDesc() == null).collect(Collectors.groupingBy(s -> s.getAssetsId()));
            //已设置接口描述的数据
            oldDisAssetsInterfaceMap = oldList.stream().filter(s -> s.getAssetsId() != null && (s.getEditorDesc() != null && s.getEditorDesc())).collect(Collectors.groupingBy(s -> s.getAssetsId(), Collectors.mapping(AssetsInterfaceDTO::getName, Collectors.toList())));
            //对比差异
            List<AssetsInterfaceDTO> insertDTOs = new ArrayList<>();
            List<Integer> deleteIds = new ArrayList<>();
            String user = iLoginCacheInfo.getLoginName();
            Date curDate = new Date();

            for (Map.Entry<String, List<AssetsInterfaceDTO>> entry : newAssetsInterfaceMap.entrySet()) {
                Map<String, AssetsInterfaceDTO> newMap = new HashMap<>();
                Map<String, AssetsInterfaceDTO> oldAddMap = new HashMap<>();
                Map<String, AssetsInterfaceDTO> oldDelMap = new HashMap<>();

                for (AssetsInterfaceDTO assetsInterfaceDTO : entry.getValue()) {
                    String id = assetsInterfaceDTO.genID();
                    newMap.put(id, assetsInterfaceDTO);
                }
                //获取genID作为唯一标识
                List<AssetsInterfaceDTO> oldAddAssetList = oldAddAssetsInterfaceMap.get(entry.getKey());
                if (null != oldAddAssetList) {
                    for (AssetsInterfaceDTO assetsInterfaceDTO : oldAddAssetList) {
                        String id = assetsInterfaceDTO.genID();
                        oldAddMap.put(id, assetsInterfaceDTO);
                    }
                }
                //获取genID作为唯一标识
                List<AssetsInterfaceDTO> oldDelAssetList = oldDelAssetsInterfaceMap.get(entry.getKey());
                if (null != oldDelAssetList) {
                    for (AssetsInterfaceDTO assetsInterfaceDTO : oldDelAssetList) {
                        String id = assetsInterfaceDTO.genID();
                        oldDelMap.put(id, assetsInterfaceDTO);
                    }
                }
                //新增数据插入操作，新增数据在原有数据中查询不到，该条数据需要插入新增
                for (Map.Entry<String, AssetsInterfaceDTO> dtoEntry : newMap.entrySet()) {
                    AssetsInterfaceDTO oldAssetsInterfaceDTO = oldAddMap.get(dtoEntry.getKey());
                    if (null == oldAssetsInterfaceDTO) {//新获取的数据唯一标识在原有数据中查询不到，新获取的数据需要插入
                        AssetsInterfaceDTO newAssetsInterfaceDTO = dtoEntry.getValue();//要插入的数据
                        List<String> interfaceNames = oldDisAssetsInterfaceMap.get(newAssetsInterfaceDTO.getAssetsId());
                        //这里是补前面根据genId匹配的数据漏洞（genId中有接口描述，修改接口描述后，genId必定不一样）
                        //接口描述修改后，在根据genId匹配，会导致修改过接口描述得数据重复添加
                        if (CollectionUtils.isNotEmpty(interfaceNames)) {
                            if (!interfaceNames.contains(newAssetsInterfaceDTO.getName())) {//接口名称不相同，才能插入新增
                                newAssetsInterfaceDTO.setCreator(user);
                                newAssetsInterfaceDTO.setCreateDate(curDate);
                                newAssetsInterfaceDTO.setModifier(user);
                                newAssetsInterfaceDTO.setModificationDate(curDate);
                                insertDTOs.add(newAssetsInterfaceDTO);
                            }
                        } else {
                            newAssetsInterfaceDTO.setCreator(user);
                            newAssetsInterfaceDTO.setCreateDate(curDate);
                            newAssetsInterfaceDTO.setModifier(user);
                            newAssetsInterfaceDTO.setModificationDate(curDate);
                            insertDTOs.add(newAssetsInterfaceDTO);
                        }
                    }
                }
                //删除原有数据操作，仅对接口描述标识为null的（接口描述前端页面修改过的，不可删除），
                for (Map.Entry<String, AssetsInterfaceDTO> dtoEntry : oldDelMap.entrySet()) {
                    AssetsInterfaceDTO newAssetsInterfaceDTO = newMap.get(dtoEntry.getKey());
                    if (null == newAssetsInterfaceDTO) {
                        AssetsInterfaceDTO oldAssetsInterfaceDTO = dtoEntry.getValue();
                        if (debug) {
                            log.info("delete {}", oldAssetsInterfaceDTO.genID());
                        }
                        deleteIds.add(oldAssetsInterfaceDTO.getId());
                    }
                }
            }

            //更新信息
            if (deleteIds.size() > 0) {
                mwAssetsInterfaceDao.deleteIntefacesById(deleteIds);
            }

            doBatchInsert(sqlSessionFactory, insertDTOs);
            if (debug) {
                log.info("refreshInterfaceInfo");
                for (AssetsInterfaceDTO interfaceDTO : insertDTOs) {
                    log.info("insert {}", interfaceDTO.toString());
                }
            }
        }
    }

    /**
     * 获取资产的所有接口
     *
     * @param param 参数
     * @return
     */
    @Override
    public Reply getAllInterfaces(QueryAssetsInterfaceParam param) {
        //光口：Te开头的、XG开头的、Ten-G开头的(获取物理接口)
        List<QueryAssetsInterfaceParam> list = mwAssetsInterfaceDao.getAllInterface(param.getAssetsId(), null, param.getVlanFlag());
        return Reply.ok(list);
    }

    @Override
    public List<AssetsInterfaceDTO> getAllAssetsInterfaceByCriteria(Map map) {
        List<AssetsInterfaceDTO> list = mwAssetsInterfaceDao.getAllInterfaceDTOByCriteria(map);
        return list;
    }

    private void processBatchAddAseets(BatchAddAssetsEvent batchAddAssetsEvent) {
        List<ScanResultSuccess> scanResultSuccesses = batchAddAssetsEvent.getScanResultSuccessList();
        List<SearchParam> searchParams = new ArrayList<>();
        Map<String, MwTangibleassetsTable> ipMap = new HashMap<>();
        List<AssetsIdsPageInfoParam> list = new ArrayList<>();
        if (null != scanResultSuccesses && scanResultSuccesses.size() > 0) {
            String proxyIp = null;
            ScanResultSuccess resultSuccess = scanResultSuccesses.get(0);
            //查询引擎
            MwEngineManageDTO mwEngineManageDTO = mwEngineCommonsService.selectEngineByIdNoPerm(resultSuccess.getPollingEngine());
            if (null != mwEngineManageDTO && StringUtils.isNotEmpty(mwEngineManageDTO.getProxyAddress())) {
                proxyIp = mwEngineManageDTO.getProxyAddress();
            }

            List<Integer> scanSuccessIds = new ArrayList<>();
            for (ScanResultSuccess scanResultSuccess : scanResultSuccesses) {
                SearchParam searchParam = new SearchParam();
                searchParam.setProxyServerIp(proxyIp);
                searchParam.setIp(scanResultSuccess.getIpAddress());

                RuleParam ruleParam = new RuleParam(scanResultSuccess);
                searchParam.setRuleParam(ruleParam);
                searchParams.add(searchParam);
                scanSuccessIds.add(scanResultSuccess.getId());
            }

            //查询资产表信息
            Map criteria = new HashMap();
            criteria.put("scanSuccessIds", scanSuccessIds);

            Reply reply = mwModelViewCommonService.findTopoModelAssets(MwTangibleassetsTable.class, criteria);
            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                List<MwTangibleassetsTable> mwTangibleassetsTables = (List<MwTangibleassetsTable>) reply.getData();
                if (null != mwTangibleassetsTables) {
                    for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
                        ipMap.put(mwTangibleassetsTable.getInBandIp(), mwTangibleassetsTable);
                        //服务器、网络设备、交换机类型，需要同步zabbix接口数据
                        if(assetsTypeIds.contains(mwTangibleassetsTable.getAssetsTypeId())){
                            AssetsIdsPageInfoParam param = new AssetsIdsPageInfoParam();
                            param.setMonitorServerId(mwTangibleassetsTable.getMonitorServerId());
                            param.setAssetsId(mwTangibleassetsTable.getAssetsId());
                            param.setAssetsIp(mwTangibleassetsTable.getInBandIp());
                            param.setId(mwTangibleassetsTable.getId());
                            list.add(param);
                        }
                    }
                }
            }

        }

        if (ipMap.size() > 0) {
            doAddInterfaceInfo(searchParams, ipMap);
        }
//        if(CollectionUtils.isNotEmpty(list)){
//            service.getNetDataListByZabbix(list,new ArrayList<>(),new ArrayList<>());
//        }
    }

    private void processAddAsset(AddTangibleassetsEvent addTangibleassetsEvent) {
        AddUpdateTangAssetsParam addUpdateTangAssetsParam = addTangibleassetsEvent.getAddTangAssetsParam();
        //只处理snmp类型资产
        //snmpv1v2和snmpv3的monitorMode相同
        if (addUpdateTangAssetsParam.getMonitorMode().intValue() == RuleType.SNMPv1v2.getMonitorMode()) {
            List<SearchParam> searchParams = transformSearchParam(addUpdateTangAssetsParam);

            //判断是否使用代理agent扫描
            if (StringUtils.isNotEmpty(addUpdateTangAssetsParam.getPollingEngine()) && !"localhost".equals(addUpdateTangAssetsParam.getPollingEngine())) {
                MwEngineManageDTO mwEngineManageDTO = mwEngineCommonsService.selectEngineByIdNoPerm(addUpdateTangAssetsParam.getPollingEngine());
                SearchParam searchParam = searchParams.get(0);
                searchParam.setProxyServerIp(mwEngineManageDTO.getProxyAddress());
            }

            Map<String, MwTangibleassetsTable> ipMap = new HashMap<>();
            MwTangibleassetsTable table = new MwTangibleassetsTable();
            table.setId(addUpdateTangAssetsParam.getId());
            table.setInBandIp(addUpdateTangAssetsParam.getInBandIp());
            table.setAssetsId(addUpdateTangAssetsParam.getAssetsId());
            ipMap.put(addUpdateTangAssetsParam.getInBandIp(), table);
            doAddInterfaceInfo(searchParams, ipMap);
        }
        //服务器、网络设备、交换机类型，需要同步zabbix接口数据
//        if(assetsTypeIds.contains(addUpdateTangAssetsParam.getAssetsTypeId())){
//            AssetsIdsPageInfoParam param = new AssetsIdsPageInfoParam();
//            param.setMonitorServerId(addUpdateTangAssetsParam.getMonitorServerId());
//            param.setAssetsId(addUpdateTangAssetsParam.getAssetsId());
//            param.setAssetsIp(addUpdateTangAssetsParam.getInBandIp());
//            param.setId(addUpdateTangAssetsParam.getId());
//            service.getNetDataListByZabbix(Arrays.asList(param),new ArrayList<>(),new ArrayList<>());
//        }
    }

    private void doAddInterfaceInfo(List<SearchParam> searchParams, Map<String, MwTangibleassetsTable> ipMap) {

        log.info("doAddInterfaceInfo snmp searchDeviceList");

        //获取snmp信息
        //获取设备接口数据
        //设置action为空,线程本地信息
        //相同key名称的可以共享扫描结果
        //每次都要获取新的bean,否则多次请求会积累响应结果,且多线程访问结果会冲突
        MWSnmpSearchService newMwSnmpSearchService = SpringUtils.getBean(MWSnmpSearchService.class);
        newMwSnmpSearchService.setSnmpSearchThreadLocal(SnmpSearchAction.ASSETS);
        SnmpSearchAction snmpSearchAction = new SnmpSearchAction();
        snmpSearchAction.enableInterfaceMacRouteInfo();

        List<Device> deviceList = newMwSnmpSearchService.findDeviceInterfaceInfoBySearchParam(searchParams, snmpSearchAction);
        //保存接口信息
        if (deviceList.size() > 0) {
            for (Device device : deviceList) {
                List<InterfaceInfo> interfaceInfos = device.getInterfaceInfoList();
                String user = iLoginCacheInfo.getLoginName();
                Date curDate = new Date();

                if (null != interfaceInfos && interfaceInfos.size() > 0) {
                    List<AssetsInterfaceDTO> assetsInterfaceDTOS = new ArrayList<>();
                    int count = 0;
                    for (InterfaceInfo interfaceInfo : interfaceInfos) {
                        count++;
                        AssetsInterfaceDTO assetsInterfaceDTO = new AssetsInterfaceDTO();
                        assetsInterfaceDTO.extractFromInterfaceInfo(interfaceInfo);
                        MwTangibleassetsTable tangibleassetsTable = ipMap.get(device.getIp());
                        if (tangibleassetsTable != null) {
                            String deviceId = tangibleassetsTable.getId();
                            String hostIp = tangibleassetsTable.getInBandIp();
                            String hostId = tangibleassetsTable.getAssetsId();
                            assetsInterfaceDTO.setAssetsId(deviceId);
                            assetsInterfaceDTO.setHostId(hostId);
                            assetsInterfaceDTO.setHostIp(hostIp);
                            assetsInterfaceDTO.setCreator(user);
                            assetsInterfaceDTO.setCreateDate(curDate);
                            assetsInterfaceDTO.setModifier(user);
                            assetsInterfaceDTO.setModificationDate(curDate);
                            assetsInterfaceDTOS.add(assetsInterfaceDTO);
                        }
                    }

                    if (assetsInterfaceDTOS.size() > 0) {
                        doBatchInsert(sqlSessionFactory, assetsInterfaceDTOS);
                    }
                }
            }

            if (debug) {
                for (Device device : deviceList) {
                    log.info("doAddInterfaceInfo {}", device.toString());
                    List<InterfaceInfo> interfaceInfos = device.getInterfaceInfoList();
                    if (null != interfaceInfos) {
                        for (InterfaceInfo interfaceInfo : interfaceInfos) {
                            log.info(interfaceInfo.toString());
                        }
                    }
                }
            }
        }
    }


    private void doBatchInsert(SqlSessionFactory sqlSessionFactory, List<AssetsInterfaceDTO> assetsInterfaceDTOS) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        MwAssetsInterfaceDao mapper = session.getMapper(MwAssetsInterfaceDao.class);
        int count = 0;
        try {
            for (AssetsInterfaceDTO assetsInterfaceDTO : assetsInterfaceDTOS) {
                if(Strings.isNullOrEmpty(assetsInterfaceDTO.getName())){
                    continue;
                }
                count++;
                if (assetsInterfaceDTO.getDescription()!=null && assetsInterfaceDTO.getDescription().length() > 254) {//山鹰环境，描述信息太长，导致插入失败，限制长度
                    assetsInterfaceDTO.setDescription("");
                }
                if(assetsInterfaceDTO.getState()!=null && "up".equals(assetsInterfaceDTO.getState())){
                    assetsInterfaceDTO.setShowFlag(true);
                }
                mapper.insertInterface(assetsInterfaceDTO);
                if (0 == (count % batchInsertSize) || count == assetsInterfaceDTOS.size()) {
                    log.info("MwAssetsInterfaceDao doBatchInsert:" + count);
                    session.commit();
                    session.clearCache();
                }
            }
        } catch (Exception e) {
            //没有提交的数据可以回滚
            session.rollback();
            log.error("batchSuccInsert", e);
        } finally {
            session.close();
        }
    }

    private List<SearchParam> transformSearchParam(AddUpdateTangAssetsParam addUpdateTangAssetsParam) {
        List<SearchParam> searchParams = new ArrayList<>();
        SearchParam searchParam = new SearchParam();
        searchParams.add(searchParam);
        searchParam.setIp(addUpdateTangAssetsParam.getInBandIp());

        RuleParam ruleParam = new RuleParam();
        searchParam.setRuleParam(ruleParam);
        MwSnmpv1AssetsDTO snmpv1AssetsDTO = addUpdateTangAssetsParam.getSnmpV1AssetsDTO();
        MwSnmpAssetsDTO snmpAssetsDTO = addUpdateTangAssetsParam.getSnmpAssetsDTO();
        if (null != snmpv1AssetsDTO && null != snmpv1AssetsDTO.getPort()) {
            ruleParam.extractFromMwSnmpv1AssetsDTO(snmpv1AssetsDTO);
        }

        if (null != snmpAssetsDTO && null != snmpAssetsDTO.getPort()) {
            ruleParam.extractFromMwSnmpAssetsDTO(snmpAssetsDTO);
        }
        return searchParams;
    }

    private void processDelAssets(BatchDeleteAssetsEvent batchDeleteAssetsEvent) {
        List<DeleteTangAssetsID> ids = batchDeleteAssetsEvent.getDeleteTangAssetsIDList();
        if (null != ids && ids.size() > 0) {
            mwAssetsInterfaceDao.deleteIntefaces(ids);
        }
        log.info("processDelAssets {}", ids.toString());
    }

    /**
     * 通过资产id获取所有的接口类型信息
     *
     * @param aparam
     * @return
     */
    @Override
    public Reply getAllInterface(QueryAssetsInterfaceParam aparam) {
        try {
            List<Map> allList = new ArrayList();
            //光口：Te开头的、XG开头的、Ten-G开头的
            List<QueryAssetsInterfaceParam> list = mwAssetsInterfaceDao.getAllInterface(aparam.getAssetsId(), aparam.getName(), false);

            List<String> disNameList = new ArrayList<>();
            List<String> noStartWithList = new ArrayList<>();
            List<String> cableStartWithList = new ArrayList<>();
            MwModelFilterInterfaceParam filterInfo = mwAssetsInterfaceDao.getFilterInfo();
            if (filterInfo != null) {
                if (!Strings.isNullOrEmpty(filterInfo.getFilterField())) {
                    disNameList = Arrays.asList(filterInfo.getFilterField().split(","));
                }
                if (!Strings.isNullOrEmpty(filterInfo.getNoStartWith())) {
                    noStartWithList = Arrays.asList(filterInfo.getNoStartWith().split(","));
                }
                if (!Strings.isNullOrEmpty(filterInfo.getCableStartWith())) {
                    cableStartWithList = Arrays.asList(filterInfo.getCableStartWith().split(","));
                }
            }


            if (list != null && list.size() > 0) {
                log.info("接口信息数据库获取数据数量：" + list.size() + "；资产id：AssetsId()" + aparam.getAssetsId());
                for (String name : disNameList) {
                    Iterator<QueryAssetsInterfaceParam> it = list.iterator();
                    while (it.hasNext()) {
                        QueryAssetsInterfaceParam m = it.next();
                        int x = (m.getName().toLowerCase()).indexOf(name.toLowerCase());
                        if (x != -1) {
                            it.remove();
                        }
                    }
                }
                Iterator<QueryAssetsInterfaceParam> it = list.iterator();
                while (it.hasNext()) {
                    QueryAssetsInterfaceParam m = it.next();
                    String lowerName = m.getName().toLowerCase();
                    if (myStartWith(lowerName, noStartWithList)) {
                        it.remove();
                    }
                }
                List<NetListDto> listDtos = new ArrayList<>();
                List<String> itemNames = Arrays.asList("MW_INTERFACE_STATUS");

                long time1 = System.currentTimeMillis();
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(aparam.getMonitorServerId(), itemNames, aparam.getZabbixAssetsId());
                long time2 = System.currentTimeMillis();
                long time3 = 0l;
                long time4 = 0l;
                if (result != null && !result.isFail()) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode != null && jsonNode.size() > 0) {
                        List<AssetsItemGetDTO> assetsItemGetDTOS = JSONObject.parseArray(result.getData().toString(), AssetsItemGetDTO.class);
                        List<String> valuemapIds = new ArrayList<>();
                        for (AssetsItemGetDTO item : assetsItemGetDTOS) {
                            String valuemapid = item.getValuemapid();
                            valuemapIds.add(valuemapid);
                        }
                        List<String> valuemapIdList = valuemapIds.stream().distinct().collect(Collectors.toList());
                        time3 = System.currentTimeMillis();
                        Map<String, Map> valueMapByIdMap = getValueMapByIdList(aparam.getMonitorServerId(), valuemapIdList);
                        time4 = System.currentTimeMillis();
                        Map<String, List<AssetsItemGetDTO>> collect = assetsItemGetDTOS.stream().collect(Collectors.groupingBy(AssetsItemGetDTO::getOriginalType));
                        for (Map.Entry<String, List<AssetsItemGetDTO>> value : collect.entrySet()) {
                            List<AssetsItemGetDTO> dtos = value.getValue();
                            NetListDto netListDto = new NetListDto();
                            netListDto.setInterfaceName(value.getKey().substring(1, value.getKey().length() - 1));
                            dtos.forEach(AssetsItemGetDTO -> {
                                String name = AssetsItemGetDTO.getName();
                                String lastvalue = AssetsItemGetDTO.getLastvalue();

                                if ("MW_INTERFACE_STATUS".equals(name.substring(name.indexOf("]") + 1))) {
                                    String valuemapid = AssetsItemGetDTO.getValuemapid();
                                    if (valueMapByIdMap != null && valueMapByIdMap.size() > 0 &&
                                            valueMapByIdMap.get(valuemapid) != null && valueMapByIdMap.get(valuemapid).get(lastvalue) != null) {
                                        String newvalue = valueMapByIdMap.get(valuemapid).get(lastvalue).toString();
                                        if (!Strings.isNullOrEmpty(newvalue)) {
                                            netListDto.setState(newvalue);
                                        }
                                    }
                                }
                            });
                            listDtos.add(netListDto);
                        }
                    }
                }
                long time5 = System.currentTimeMillis();
                log.info("接口状态请求zabbix耗时时间1：" + (time2 - time1) + "ms；时间2：" + (time3 - time2) + "ms；" + "时间3：" + (time4 - time3) + "ms；");
                for (NetListDto dto : listDtos) {
                    for (QueryAssetsInterfaceParam param : list) {
                        if (dto.getInterfaceName().equals(param.getName())) {
                            param.setState(dto.getState());
                        }
                    }
                }

                Map<String, List<QueryAssetsInterfaceParam>> m = new HashMap();
                //按名称区分，相同名称前缀的放一个list中
                if (list != null && list.size() > 0) {
                    List<QueryAssetsInterfaceParam> otherList = new ArrayList<>();
                    //对名称中含有“-”的进行过滤，eth0-1，eth0-2，eth0-3表示同一个接口eth0，只保留一条数据。
                    Iterator<QueryAssetsInterfaceParam> its = list.iterator();
                    Set<String> set = new HashSet<>();
                    while (its.hasNext()) {
                        QueryAssetsInterfaceParam ms = its.next();
                        String lowerName = ms.getName().toLowerCase();
                        //名称中含有“-”且含有“.”的，表示同一个接口不同分接口，如eth3-5.145、eth3-5.148，都表示eth3-5这个接口，需要过滤
                        if (lowerName.indexOf(".") != -1) {
                            its.remove();
                        }
                    }
                    for (QueryAssetsInterfaceParam param : list) {
                        int index = 0;
                        String name = "";
                        if (param.getName().indexOf("-") != -1) {
                            index = param.getName().lastIndexOf("-");
                            name = param.getName().substring(0, index);
                        } else if (param.getName().indexOf("/") != -1) {
                            index = param.getName().lastIndexOf("/");
                            name = param.getName().substring(0, index);
                        } else {
                            //提取名称中的字母，如果字母相同，放入同一个list中
                            name = param.getName().replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
                        }
                        if (m.containsKey(name)) {
                            List<QueryAssetsInterfaceParam> pList = m.get(name);
                            pList.add(param);
                            m.put(name, pList);
                        } else {
                            List<QueryAssetsInterfaceParam> pList = new ArrayList<>();
                            pList.add(param);
                            m.put(name, pList);
                        }
                    }
                }
                List onlyList = new ArrayList();

                //排序，从小到大，保证list.size()为1的集合在最前面；list数量为1时，不要单独占一行面板。并入到其他list中
                List<Map.Entry<String, List<QueryAssetsInterfaceParam>>> lsEntry = new ArrayList<>(m.entrySet());
                Collections.sort(lsEntry, new Comparator<Map.Entry<String, List<QueryAssetsInterfaceParam>>>() {
                    @Override
                    public int compare(Map.Entry<String, List<QueryAssetsInterfaceParam>> o1, Map.Entry<String, List<QueryAssetsInterfaceParam>> o2) {
                        return o1.getValue().size() - o2.getValue().size();
                    }
                });
                LinkedHashMap<String, List<QueryAssetsInterfaceParam>> linkedHashMap = new LinkedHashMap<>();
                for (Map.Entry<String, List<QueryAssetsInterfaceParam>> e : lsEntry) {
                    linkedHashMap.put(e.getKey(), e.getValue());
                }
                linkedHashMap.size();
                List<String> finalCableStartWithList = cableStartWithList;
                linkedHashMap.forEach((k, v) -> {
                    Map map = new HashMap<>();
                    if (v != null && v.size() > 0) {
                        if (v.size() == 1 && onlyList.size() != (linkedHashMap.size() - 1)) {
                            //list数量为1时，不要单独占一行面板。
                            onlyList.addAll(v);
                        } else {
                            //将数量仅为1的集合，并入到其他list中
                            v.addAll(onlyList);
                            //电口
                            List<QueryAssetsInterfaceParam> electronList = new ArrayList<>();
                            //光口
                            List<QueryAssetsInterfaceParam> cableList = new ArrayList<>();
                            String name = "";
                            map.put("interfaceSize", v.size());
                            for (QueryAssetsInterfaceParam param : v) {
                                name = param.getName().toLowerCase();
                                //判断是否为光口
                                if ((name.indexOf("sfp") != -1) || (myStartWith(name, finalCableStartWithList))) {
                                    cableList.add(param);
                                } else {
                                    electronList.add(param);
                                }
                            }
                            sort(electronList, QueryAssetsInterfaceParam.class, "name");
                            sort(cableList, QueryAssetsInterfaceParam.class, "name");
                            map.put("allList", v);
                            map.put("electronList", electronList);
                            map.put("cableList", cableList);
                            allList.add(map);
                            onlyList.clear();
                        }
                    }
                });
                //排序，list从大到小的顺序
                Collections.sort(allList, new Comparator<Map>() {
                    @Override
                    public int compare(Map o1, Map o2) {
                        return ((List) o2.get("allList")).size() - ((List) o1.get("allList")).size();
                    }
                });
            }

            return Reply.ok(allList);
        } catch (Throwable e) {
            log.error("fail to getAllInterface param{}, case by {}", aparam, e);
            return Reply.fail(500, "资产id获取所有的接口信息失败");
        }
    }


    private Boolean myStartWith(String nameStr, List<String> strList) {
        for (String str : strList) {
            if (nameStr.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //注册SnmpSearchAction,在相同线程内共享snmp信息
        SnmpSearchAction snmpSearchAction = new SnmpSearchAction();
        snmpSearchAction.enableInterfaceMacRouteInfo();
        snmpSearchService.registerThreadLocalAction(SnmpSearchAction.ASSETS, snmpSearchAction);
    }

    @Override
    public Reply setInterfaceStatus(QueryAssetsInterfaceParam param) {
        try {
            //获取资产的启动配置状态
            Integer settingFlag = mwAssetsInterfaceDao.getSettingByAssets(param.getAssetsId());
            Boolean isSucces = false;
            if (settingFlag != null && settingFlag.intValue() == 1) {
                //只有配置过的资产，才可以下发接口状态
                isSucces = configManageCommonService.switchInterface(param.getAssetsId(), param.getName(), param.getInterfaceSetState());
                if (isSucces) {
                    mwAssetsInterfaceDao.updataInterfaceStatus(param.getId(), param.getInterfaceSetState());
                    return Reply.ok("设置成功");
                } else {
                    return Reply.fail(500, "调用下发接口状态命令失败");
                }
            } else {
                return Reply.fail(500, "资产配置状态未开启！");
            }
        } catch (Throwable e) {
            log.error("fail to setInterfaceStatus param{}, case by {}", param.getAssetsId(), e);
            return Reply.fail(500, "接口状态下发修改失败");
        }
    }

    //根据item中valuemapid属性，转换成对应的value值
    public Map<String, Map> getValueMapByIdList(int monitorServerId, List<String> valuemapId) {
        MWZabbixAPIResult valueMapById = mwtpServerAPI.getValueMapById(monitorServerId, valuemapId);
        List<AssetsValuemapDto> dataList = new ArrayList<>();
        if (null != valueMapById && valueMapById.getCode() == 0) {
            dataList = JSONArray.parseArray(String.valueOf(valueMapById.getData()), AssetsValuemapDto.class);
        }
        Map<String, Map> map = new HashMap();
        for (AssetsValuemapDto dto : dataList) {
            Map map1 = new HashMap();
            for (AssetsValueMappingDto val : dto.getMappings()) {
                val.getValue();
                val.getNewvalue();
                map1.put(val.getValue(), val.getNewvalue());
            }
            map.put(dto.getValuemapid(), map1);
        }
        return map;
    }

    public Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (null != clazz) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }

    /**
     * 字符串混合排序
     *
     * @param list  需要排序的集合
     * @param clazz 当前类对象
     * @param field 排序字段
     */
    public void sort(List<?> list, Class<?> clazz, String field) {
        int index = -1;
        Field[] fields = null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(field)) {
            //获取本类及其父类的所有字段
            fields = getAllFields(clazz);
            int pos;
            for (pos = 0; pos < fields.length; pos++) {
                if (field.equals(fields[pos].getName())) {
                    index = pos;
                    break;
                }
            }
            //判断是否存在排序字段
            if (-1 == index) return;
        }
        Field[] finalFields = fields;
        int finalIndex = index;
        list.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(field)) {
                    finalFields[finalIndex].setAccessible(true);
                    try {
                        o1 = finalFields[finalIndex].get(o1);
                        o2 = finalFields[finalIndex].get(o2);
                    } catch (IllegalAccessException e) {
                        log.error("fail to sort with Param={}, cause:{}", e);
                    }
                }
                Pattern pattern = Pattern.compile("[0-9]+|[a-z]+|[A-Z]+|[\\u4E00-\\u9FFF]");
                Matcher matcher01 = pattern.matcher(String.valueOf(o1));
                Matcher matcher02 = pattern.matcher(String.valueOf(o2));
                List o1List = new ArrayList();
                List o2List = new ArrayList();
                while (matcher01.find()) {
                    o1List.add(matcher01.group());
                }
                while (matcher02.find()) {
                    o2List.add(matcher02.group());
                }
                int size = o1List.size() > o2List.size() ? o2List.size() : o1List.size();
                for (int i = 0; i < size; i++) {
                    int o1Code = o1List.get(i).hashCode();
                    int o2Code = o2List.get(i).hashCode();
                    if (o1Code != o2Code) return o1Code - o2Code;
                }
                return 0;
            }
        });


    }

}

