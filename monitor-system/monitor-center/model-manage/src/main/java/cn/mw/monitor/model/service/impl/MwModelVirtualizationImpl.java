package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.common.util.QueryHostParam;
import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dao.MwModelVirtualizationDao;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.exception.ModelManagerException;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.param.virtual.ModelVirtualPermControlParam;
import cn.mw.monitor.model.param.virtual.MwInstanceInfoParam;
import cn.mw.monitor.model.param.virtual.QueryVirtualInstanceParam;
import cn.mw.monitor.model.proxy.param.VCenterBaseInfoParam;
import cn.mw.monitor.model.proxy.param.VCenterInfoParam;
import cn.mw.monitor.model.proxy.param.VCenterVirtualInstanceParam;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelRancherService;
import cn.mw.monitor.model.service.MwModelRelationsService;
import cn.mw.monitor.model.service.MwModelVirtualizationService;
import cn.mw.monitor.model.service.demo.*;
import cn.mw.monitor.model.util.ModelOKHttpUtils;
import cn.mw.monitor.model.util.virtual.ConnectedVimServiceBase;
import cn.mw.monitor.model.view.ModelVirtualTreeView;
import cn.mw.monitor.model.view.VirtualView;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.ModelVirtualDeleteContext;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.virtual.dto.*;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.*;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;
import vijava.com.vmware.vim25.*;
import vijava.com.vmware.vim25.mo.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;
import static cn.mw.monitor.service.virtual.dto.VirtualizationType.*;
import static cn.mw.monitor.util.ListMapObjUtils.objectsToMaps;

/**
 * @author qzg
 * @date 2022/9/7
 */
@Service
@Slf4j
public class MwModelVirtualizationImpl implements MwModelVirtualizationService {

    //位异或密钥
    private static final int KEY = 5;
    //redis存储虚拟化列表信息key值
    private static final String hKey = "ModelVirtualization::getList";
    private static final String PID = "0";
    private static final String DEFAULT_ASSETS_ID = "-1";
    private static final String CLUSTERS = "clusters";
    private static final String HOSTS = "hosts";
    private static final String DATACENTERS = "datacenters";

    private static final Integer clusterGroupType = 1;
    @Resource
    private MwModelVirtualizationDao mwModelVirtualizationDao;
    private int pageSize = 10000;
    @Resource
    private MwModelManageDao mwModelManageDao;
    private static final String privateKey = RSAUtils.RSA_PRIVATE_KEY;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelRancherService mwModelRancherService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Value("${VCenter.modelId}")
    private Integer modelId;
    @Value("${vcenterInfo.insertType}")
    private String insertType;
    @Value("${virtual.sync.neo4j}")
    private Boolean syncNeo4j;
    @Value("${model.debug}")
    private boolean debug;
    @Autowired
    private MwModelRelationsService mwModelRelationsService;
    @Autowired
    private ModelServer modelSever;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MWCommonService mwCommonService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWUserService userService;

    @Autowired
    private MwModelVirtualRelationManager mwModelVirtualRelationManager;

    @Autowired(required = false)
    private ConnectionPool connectionPool;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwModelViewServiceImpl mwModelViewServiceImpl;

    private static int insBatchFetchNum = 900;

    @Autowired
    private ProxySearch proxySearch;

    @Override
    public Reply syncVirtualDeviceInfo(QueryVirtualInstanceParam param) {
        try {
            //根据资产实例获取对应的关联虚拟化模型
            List<AddModelInstancePropertiesParam> mapList = mwModelVirtualizationDao.getModelInfoByModelIndex(param.getModelIndex());
            //将模型名称当成key值，转为map数据
            Map<String, List<AddModelInstancePropertiesParam>> modelInfoByName = mapList.stream().collect(Collectors.groupingBy(AddModelInstancePropertiesParam::getModelName));

            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
            TransferUtils.transferBean(param, qParam);
            //根据VCenter实例获取es数据信息
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(qParam);
            //获取虚拟化VCenter的连接信息，URL、用户名、密码
            String userName = "";
            String url = "";
            String password = "";
            String assetsId = "";
            String monitorServerName = "";
            Integer monitorServerId = 0;
            Integer vcenterModelId = null;
            for (Map<String, Object> m : listInfo) {
                userName = m.get("USERNAME").toString();
                url = m.get("HOST").toString();
                if (m.get("assetsId") != null) {
                    assetsId = m.get("assetsId").toString();
                }
                if (m.get("monitorServerId") != null) {
                    monitorServerId = intValueConvert(m.get("monitorServerId"));
                }
                password = RSAUtils.decryptData(m.get("PASSWORD") != null ? m.get("PASSWORD").toString() : "", RSAUtils.RSA_PRIVATE_KEY);
                vcenterModelId = intValueConvert(m.get("modelId"));
            }

            if (param.getRelationInstanceId() == null) {
                param.setRelationInstanceId(param.getModelInstanceId());
            }
            if (Strings.isNullOrEmpty(assetsId)) {
                assetsId = "-1";
            }
//            getVcenterAllList(userName, password, url, assetsId);
            //获取zabbix服务器名称
            if (monitorServerId.intValue() != 0) {
                monitorServerName = mwModelVirtualizationDao.selectServerNameById(monitorServerId);
            }
            param.setUrl(url);
            param.setUserName(userName);
            param.setPassword(password);
            param.setMonitorServerId(monitorServerId);
            param.setMonitorServerName(monitorServerName);
            List<VirtualizationDataInfo> list = null;
            //通过VCenterAPI获取虚拟化设备
            //查询引擎信息
            Date start1 = new Date();
            List<ProxyInfo> proxyInfos = new ArrayList<>();
            VCenterInfoParam vCenterInfoParam = new VCenterInfoParam(url, userName, password
                    , assetsId, monitorServerId, monitorServerName);

            Type type = new TypeToken<List<VirtualizationDataInfo>>() {
            }.getType();
            list = proxySearch.doProxySearch(List.class, proxyInfos, param.getModelInstanceId(), "mwVCenterService"
                    , "getVCenterInfo", vCenterInfoParam, type);
            long interval1 = DateUtils.between(start1, new Date(), DateUnitEnum.SECOND);
            log.info("end VirtualData::getVCenterInfo interval:{} s", interval1);
            log.info("start getVirtualDataByApi");
            Date start = new Date();
            ConnectedVimServiceBase cs = new ConnectedVimServiceBase();
            if (proxyInfos.size() == 0) {
                log.info("do syncVirtualDeviceInfo local search");

                //VCenterApi登录连接
                cs.connect(url, userName, password);
                if (cs.si == null) {
                    return Reply.fail(500, "虚拟化连接失败");
                }
                list = getVcenterAllList(userName, password, url, assetsId);
                if (list == null) {
                    list = getVirtualDataByApi(cs.si, assetsId, monitorServerId, monitorServerName);
                }
//                list = getVirtualDataByApi(cs.si, assetsId, monitorServerId, monitorServerName);
                cs.disconnect();
            }
            if (CollectionUtils.isEmpty(list)) {
                return Reply.ok("获取数据数量为0");
            }
            long interval = DateUtils.between(start, new Date(), DateUnitEnum.MS);
            log.info("end getVirtualDataByApi interval:{} ms", interval);
            //通过比对找到新增,删除,修改的数据
            List<VCenterInfo> vCenterInfos = new ArrayList<>();
            List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();

            //如果未初始化过数据,更新所有数据
            List<VirtualizationDataCacheInfo> cacheInfos = new ArrayList<>();
            for (VirtualizationDataInfo data : list) {
                VirtualizationDataCacheInfo virtualizationDataCacheInfo = new VirtualizationDataCacheInfo();
                virtualizationDataCacheInfo.setVirtualizationDataInfo(data);
                cacheInfos.add(virtualizationDataCacheInfo);
            }

            fillAddData(instanceInfoList, vCenterInfos, cacheInfos, monitorServerId, modelInfoByName, param
                    , InstanceNotifyType.VirtualSyncIns);
            if (syncNeo4j) {
                VirtualCacheData oldCacheData = mwModelVirtualRelationManager.getVirtualCacheData(qParam.getModelInstanceId());
                List<VirtualizationDataCacheInfo> oldCacheInfo = new ArrayList<>();
                if (null != oldCacheData) {
                    oldCacheInfo = JSON.parseArray(oldCacheData.getData(), VirtualizationDataCacheInfo.class);
                    List<Integer> instanceList = oldCacheInfo.stream().map(s -> s.getIntanceId()).collect(Collectors.toList());
                    List<String> modelIndexList = oldCacheInfo.stream().map(s -> s.getModelIndex()).collect(Collectors.toList());
                    DeleteModelInstanceParam deleteParam1 = new DeleteModelInstanceParam();
                    deleteParam1.setModelIndexs(modelIndexList);
                    deleteParam1.setRelationInstanceIds(instanceList);
                    mwModelInstanceService.batchDeleteInstanceInfo(deleteParam1);
                }
                mwModelRelationsService.deleteRelationByInstances(modelId, Arrays.asList(qParam.getModelInstanceId()));
                log.info("start saveData");
                start = new Date();
                try {
                    mwModelInstanceService.saveData(instanceInfoList, true, true);
                } catch (Exception e) {
                    throw new Exception("该模块新增数量已达许可数量上限！");
                }
                interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
                log.info("saveData interval:{} s", interval);
                //保存虚拟化设备拓扑关系
                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                instanceParam.setInstanceId(qParam.getModelInstanceId());
                instanceParam.setModelIndex(qParam.getModelIndex());
                instanceParam.setModelId(vcenterModelId);
                VirtualizationDataInfo virtualizationDataInfo = new VirtualizationDataInfo();
                virtualizationDataInfo.setType(VCENTER.getType());
                virtualizationDataInfo.setId(assetsId);
                VCenterInfo vCenterInfo = new VCenterInfo(instanceParam, virtualizationDataInfo);
                vCenterInfos.add(vCenterInfo);
                log.info("start updateVCenter");
                start = new Date();
                mwModelVirtualRelationManager.updateVCenter(vCenterInfos, InstanceNotifyType.VirtualSyncInit);
                interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
                log.info("end updateVCenter interval:{} s", interval);
                refreshESCache(qParam, vCenterInfos, new ArrayList<>(), InstanceNotifyType.VirtualSyncInit);

            } else {
                //数据删除
                List<MwModelInstanceParam> virInstanceList = mwModelVirtualizationDao.queryVirualInstanceInfoByModelIndex(param.getModelIndex(), param.getModelInstanceId());
                //获取查询数据的所有modelIndex和instanceId，下面的批量删除使用
                Set<String> modelIndexSet = new HashSet<>();
                Set<Integer> instanceIdSet = new HashSet<>();
                for (MwModelInstanceParam m : virInstanceList) {
                    if (m != null && !Strings.isNullOrEmpty(m.getModelIndex())) {
                        modelIndexSet.add(m.getModelIndex());
                    }
                    if (m != null && m.getInstanceId() != null) {
                        instanceIdSet.add(m.getInstanceId());
                    }
                }
                DeleteModelInstanceParam deleteParam1 = new DeleteModelInstanceParam();
                deleteParam1.setModelIndexs(new ArrayList<>(modelIndexSet));
                deleteParam1.setInstanceIds(new ArrayList<>(instanceIdSet));
                deleteParam1.setRelationInstanceIds(Arrays.asList(param.getModelInstanceId()));
                mwModelInstanceService.batchDeleteInstanceInfo(deleteParam1);

                log.info("start saveData");
                start = new Date();
                try {
                    mwModelInstanceService.saveData(instanceInfoList, true, true);
                } catch (Exception e) {
                    throw new ModelManagerException("该模块新增数量已达许可数量上限！");
                }
                interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
                log.info("saveData interval:{} s", interval);
            }

            ThreadPoolExecutor executorService = new ThreadPoolExecutor(2, 4, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            //keys为serverId，value为assetsId
            Integer finalMonitorServerId = monitorServerId;
            String finalMonitorServerName = monitorServerName;
            System.out.println("saveVirtualInfoToAlert::执行前");
            Callable callable = new Callable() {
                @Override
                public Object call() throws Exception {
                    saveVirtualInfoToAlert(param);
                    return null;
                }
            };
            executorService.submit(callable);
            System.out.println("saveVirtualInfoToAlert::执行后");

        } catch (ModelManagerException e) {
            log.error("fail to syncVirtualDeviceInfo cause:{}", e);
            return Reply.fail(500, e.getMessage());
        } catch (Throwable e) {
            log.error("fail to syncVirtualDeviceInfo cause:{}", e);
            return Reply.fail(500, "同步虚拟化设备信息失败");
        }
        return Reply.ok();
    }
//    @Override
//    public Reply syncVirtualDeviceInfo1(QueryVirtualInstanceParam param) throws Exception {
//        try {
//            //根据资产实例获取对应的关联虚拟化模型
//            List<AddModelInstancePropertiesParam> mapList = mwModelVirtualizationDao.getModelInfoByModelIndex(param.getModelIndex());
//            //将模型名称当成key值，转为map数据
//            Map<String, List<AddModelInstancePropertiesParam>> modelInfoByName = mapList.stream().collect(Collectors.groupingBy(AddModelInstancePropertiesParam::getModelName));
//
//            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
//            TransferUtils.transferBean(param, qParam);
//            //根据VCenter实例获取es数据信息
//            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(qParam);
//            //获取虚拟化VCenter的连接信息，URL、用户名、密码
//            String userName = "";
//            String url = "";
//            String password = "";
//            String assetsId = "";
//            String monitorServerName = "";
//            Integer monitorServerId = 0;
//            Integer vcenterModelId = null;
//            for (Map<String, Object> m : listInfo) {
//                userName = m.get("USERNAME").toString();
//                url = m.get("HOST").toString();
//                if (m.get("assetsId") != null) {
//                    assetsId = m.get("assetsId").toString();
//                }
//                if (m.get("monitorServerId") != null) {
//                    monitorServerId = intValueConvert(m.get("monitorServerId"));
//                }
//                password = RSAUtils.decryptData(m.get("PASSWORD") != null ? m.get("PASSWORD").toString() : "", RSAUtils.RSA_PRIVATE_KEY);
//                vcenterModelId = intValueConvert(m.get("modelId"));
//            }
//            if (Strings.isNullOrEmpty(assetsId)) {
//                assetsId = "-1";
//            }
//            //获取zabbix服务器名称
//            if (monitorServerId.intValue() != 0) {
//                monitorServerName = mwModelVirtualizationDao.selectServerNameById(monitorServerId);
//            }
//            List<VirtualizationDataInfo> list = null;
//            //通过VCenterAPI获取虚拟化设备
//            //查询引擎信息
//            Date start1 = new Date();
//            List<ProxyInfo> proxyInfos = new ArrayList<>();
//            VCenterInfoParam vCenterInfoParam = new VCenterInfoParam(url, userName, password
//                    , assetsId, monitorServerId, monitorServerName);
//
//            Type type = new TypeToken<List<VirtualizationDataInfo>>() {
//            }.getType();
//            list = proxySearch.doProxySearch(List.class, proxyInfos, param.getModelInstanceId(), "mwVCenterService"
//                    , "getVCenterInfo", vCenterInfoParam, type);
//            long interval1 = DateUtils.between(start1, new Date(), DateUnitEnum.SECOND);
//            log.info("end VirtualData::getVCenterInfo interval:{} s", interval1);
//            log.info("start getVirtualDataByApi");
//            Date start = new Date();
//            if (proxyInfos.size() == 0) {
//                log.info("do syncVirtualDeviceInfo local search");
//                ConnectedVimServiceBase cs = new ConnectedVimServiceBase();
//                //VCenterApi登录连接
//                cs.connect(url, userName, password);
//                if (cs.si == null) {
//                    return Reply.fail(500, "虚拟化连接失败");
//                }
//                list = getVirtualDataByApi(cs.si, assetsId, monitorServerId, monitorServerName);
//                cs.disconnect();
//            }
//            if (CollectionUtils.isEmpty(list)) {
//                return Reply.fail("获取数据失败！同步数量为0");
//            }
//            long interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
//            log.info("end getVirtualDataByApi interval:{} s", interval);
//
////            mwModelVirtualRelationManager.setDebug(debug);
//            //通过比对找到新增,删除,修改的数据
//            List<VCenterInfo> vCenterInfos = new ArrayList<>();
//            List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
//            VirtualCacheData oldCacheData = mwModelVirtualRelationManager.getVirtualCacheData(qParam.getModelInstanceId());
//            List<VirtualizationDataCacheInfo> oldCacheInfo = new ArrayList<>();
//            if (null != oldCacheData) {
//                oldCacheInfo = JSON.parseArray(oldCacheData.getData(), VirtualizationDataCacheInfo.class);
//                List<Integer> instanceList = oldCacheInfo.stream().map(s -> s.getIntanceId()).collect(Collectors.toList());
//                List<String> modelIndexList = oldCacheInfo.stream().map(s -> s.getModelIndex()).collect(Collectors.toList());
//                DeleteModelInstanceParam deleteParam1 = new DeleteModelInstanceParam();
//                deleteParam1.setModelIndexs(modelIndexList);
//                deleteParam1.setRelationInstanceIds(instanceList);
//                mwModelInstanceService.batchDeleteInstanceInfo(deleteParam1);
//            }
//
//            //如果未初始化过数据,更新所有数据
//            List<VirtualizationDataCacheInfo> cacheInfos = new ArrayList<>();
//            for (VirtualizationDataInfo data : list) {
//                VirtualizationDataCacheInfo virtualizationDataCacheInfo = new VirtualizationDataCacheInfo();
//                virtualizationDataCacheInfo.setVirtualizationDataInfo(data);
//                cacheInfos.add(virtualizationDataCacheInfo);
//            }
////            mwModelRelationsService.deleteRelationByInstances(modelId, Arrays.asList(qParam.getModelInstanceId()));
//
//            fillAddData(instanceInfoList, vCenterInfos, cacheInfos, monitorServerId, modelInfoByName, param
//                    , InstanceNotifyType.VirtualSyncIns);
//            log.info("start saveData");
//            start = new Date();
//            mwModelInstanceService.saveData(instanceInfoList, true, true);
//            interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
//            log.info("saveData interval:{} s", interval);
//
////            //保存虚拟化设备拓扑关系
////            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
////            instanceParam.setInstanceId(qParam.getModelInstanceId());
////            instanceParam.setModelIndex(qParam.getModelIndex());
////            instanceParam.setModelId(vcenterModelId);
////            VirtualizationDataInfo virtualizationDataInfo = new VirtualizationDataInfo();
////            virtualizationDataInfo.setType(VCENTER.getType());
////            virtualizationDataInfo.setId(assetsId);
////            VCenterInfo vCenterInfo = new VCenterInfo(instanceParam, virtualizationDataInfo);
////            vCenterInfos.add(vCenterInfo);
////
////            log.info("start updateVCenter");
////            start = new Date();
////            mwModelVirtualRelationManager.updateVCenter(vCenterInfos, InstanceNotifyType.VirtualSyncInit);
////            interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
////            log.info("end updateVCenter interval:{} s", interval);
////
////            refreshESCache(qParam, vCenterInfos, new ArrayList<>(), InstanceNotifyType.VirtualSyncInit);
//
//
////
////            VirtualInstanceChangeContext changeContext = mwModelVirtualRelationManager.compareVMWareInfo(qParam.getModelInstanceId(), list);
////
////            //判断数据是否有变更
////            List<VCenterInfo> vCenterInfos = new ArrayList<>();
////            List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
////            if (!changeContext.isHasVirtualData()) {
////                //如果未初始化过数据,更新所有数据
////                List<VirtualizationDataCacheInfo> cacheInfos = new ArrayList<>();
////                for (VirtualizationDataInfo data : list) {
////                    VirtualizationDataCacheInfo virtualizationDataCacheInfo = new VirtualizationDataCacheInfo();
////                    virtualizationDataCacheInfo.setVirtualizationDataInfo(data);
////                    cacheInfos.add(virtualizationDataCacheInfo);
////                }
////                fillAddData(instanceInfoList, vCenterInfos, cacheInfos, monitorServerId, modelInfoByName, param
////                        , InstanceNotifyType.VirtualSyncIns);
////
////                log.info("start saveData");
////                start = new Date();
////                mwModelInstanceService.saveData(instanceInfoList, true, true);
////                interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
////                log.info("saveData interval:{} s", interval);
////
////                //保存虚拟化设备拓扑关系
////                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
////                instanceParam.setInstanceId(qParam.getModelInstanceId());
////                instanceParam.setModelIndex(qParam.getModelIndex());
////                instanceParam.setModelId(vcenterModelId);
////                VirtualizationDataInfo virtualizationDataInfo = new VirtualizationDataInfo();
////                virtualizationDataInfo.setType(VirtualizationType.VCENTER.getType());
////                virtualizationDataInfo.setId(assetsId);
////                VCenterInfo vCenterInfo = new VCenterInfo(instanceParam, virtualizationDataInfo);
////                vCenterInfos.add(vCenterInfo);
////
////                log.info("start updateVCenter");
////                start = new Date();
////                mwModelVirtualRelationManager.updateVCenter(vCenterInfos, InstanceNotifyType.VirtualSyncInit);
////                interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
////                log.info("end updateVCenter interval:{} s", interval);
////
////                refreshESCache(qParam, vCenterInfos, new ArrayList<>(), InstanceNotifyType.VirtualSyncInit);
////            } else {
////                if (changeContext.isAdd()) {
////                    //只更新新增的数据
////                    fillAddData(instanceInfoList, vCenterInfos, changeContext.getAddDatas(), monitorServerId, modelInfoByName
////                            , param, InstanceNotifyType.VirtualSyncIns);
////                    mwModelInstanceService.saveData(instanceInfoList, true, true);
////
////                    refreshESCache(qParam, vCenterInfos, changeContext.getOriData(), InstanceNotifyType.VirtualSyncIns);
////
////                    //增加与新增设备的关联设备信息
////                    fillAddData(instanceInfoList, vCenterInfos, changeContext.getAddRelatedDatas(), monitorServerId
////                            , modelInfoByName, param, InstanceNotifyType.VirtualSyncUpd);
////
////                    mwModelVirtualRelationManager.updateVCenter(vCenterInfos, InstanceNotifyType.VirtualSyncIns);
////                } else if (changeContext.isModify()) {
////                    fillAddData(instanceInfoList, vCenterInfos, changeContext.getDeleteDatas(), monitorServerId, modelInfoByName
////                            , param, InstanceNotifyType.VirtualSyncUpd);
////                    syncChangeNotify(instanceInfoList, InstanceNotifyType.VirtualSyncUpd);
////
////                    refreshESCache(qParam, vCenterInfos, changeContext.getOriData(), InstanceNotifyType.VirtualSyncUpd);
////
////                } else if (changeContext.isDelete()) {
////                    fillAddData(instanceInfoList, vCenterInfos, changeContext.getDeleteDatas(), monitorServerId, modelInfoByName
////                            , param, InstanceNotifyType.VirtualSyncDelete);
////                    syncChangeNotify(instanceInfoList, InstanceNotifyType.VirtualSyncDelete);
////                    List<VirtualizationDataCacheInfo> deleteDatas = changeContext.getDeleteDatas();
////                    Set<Integer> deleteInstanceIdList = deleteDatas.stream().map(s -> s.getIntanceId()).collect(Collectors.toSet());
////                    Set<String> deleteModelIndexList = deleteDatas.stream().map(s -> s.getModelIndex()).collect(Collectors.toSet());
////                    DeleteModelInstanceParam deleteParam1 = new DeleteModelInstanceParam();
////                    deleteParam1.setModelIndexs(new ArrayList<>(deleteModelIndexList));
////                    deleteParam1.setInstanceIds(new ArrayList<>(deleteInstanceIdList));
////                    mwModelInstanceService.batchDeleteInstanceInfo(deleteParam1);
////                    mwModelVirtualRelationManager.updateVCenter(vCenterInfos, InstanceNotifyType.VirtualSyncDelete);
////                    refreshESCache(qParam, vCenterInfos, changeContext.getOriData(), InstanceNotifyType.VirtualSyncDelete);
//
////                }
////            }
//        } catch (Throwable e) {
//            log.error("fail to syncVirtualDeviceInfo cause:{}", e);
//            return Reply.fail(500, "同步虚拟化设备信息失败");
//        }
//        return Reply.ok();
//    }
//

    private void syncChangeNotify(List<AddAndUpdateModelInstanceParam> instanceInfoList, InstanceNotifyType notifyType) {
        List<Integer> intanceIds = instanceInfoList.stream().map(AddAndUpdateModelInstanceParam::getInstanceId).collect(Collectors.toList());
        if (intanceIds.size() > 0) {
            BatchUpdateInstanceParam batchUpdateInstanceParam = new BatchUpdateInstanceParam();
            batchUpdateInstanceParam.setIds(intanceIds);
            batchUpdateInstanceParam.setNotifyInfo(notifyType.name());
            mwModelManageDao.batchUpdModelInstance(batchUpdateInstanceParam);
        }
    }

    private void refreshESCache(QueryModelInstanceParam qParam, List<VCenterInfo> vCenterInfos, List<VirtualizationDataCacheInfo> oriData
            , InstanceNotifyType type) throws Exception {
        VirtualCacheData virtualCacheData = new VirtualCacheData();
        virtualCacheData.setId(qParam.getModelInstanceId().toString());
        List<VirtualizationDataCacheInfo> list = new ArrayList<>();
        for (VCenterInfo vCenterInfo : vCenterInfos) {
            AddAndUpdateModelInstanceParam instanceParam = vCenterInfo.getInstanceParam();
            VirtualizationDataCacheInfo virtualizationDataCacheInfo = new VirtualizationDataCacheInfo(instanceParam.getInstanceId()
                    , vCenterInfo.getVirtualizationDataInfo(), instanceParam.getModelIndex());
            list.add(virtualizationDataCacheInfo);
        }

        switch (type) {
            case VirtualSyncIns:
            case VirtualSyncInit:
                oriData.addAll(list);
                break;
            case VirtualSyncUpd:
                oriData.removeAll(list);
                oriData.addAll(list);
                break;
            case VirtualSyncDelete:
                oriData.removeAll(list);
        }

        String cacheVirtualJson = JSON.toJSONString(oriData);
        virtualCacheData.setData(cacheVirtualJson);
        mwModelVirtualRelationManager.updateVirtualCacheData(virtualCacheData, type);
    }

    private void fillAddData(List<AddAndUpdateModelInstanceParam> fillInstanceInfoList, List<VCenterInfo> fillVCenterInfos
            , final List<VirtualizationDataCacheInfo> cacheInfoListlist, Integer monitorServerId
            , Map<String, List<AddModelInstancePropertiesParam>> modelInfoByName, QueryVirtualInstanceParam param
            , InstanceNotifyType type) {

        log.info("start fillAddData");
//        List<VirtualizationDataInfo> list = cacheInfoListlist.stream().map(VirtualizationDataCacheInfo::getVirtualizationDataInfo).collect(Collectors.toList());
//        List<String> uuidList = new ArrayList<>();
//        for (VirtualizationDataInfo info : list) {
//            if (!Strings.isNullOrEmpty(info.getUUID())) {
//                uuidList.add(info.getUUID());
//            }
//        }
//
//        String hostId = "";
//        String hostUUID = "";
//        String hostName = "";
//        MWZabbixAPIResult itemsInfo = null;
//        //通过虚拟化设备的uuid和zabbix主机中的不可见名称关联，获取对应的hostId
//        if (monitorServerId != 0) {
//            itemsInfo = mwtpServerAPI.hostGetbyFilterByUUID(monitorServerId, uuidList);
//        }
//
//        Map<String, Map<String, String>> hostIdMap = new HashMap();
//        if (itemsInfo != null && !itemsInfo.isFail() && ((ArrayNode) itemsInfo.getData()).size() > 0) {
//            JsonNode data = (JsonNode) itemsInfo.getData();
//            if (data.size() > 0) {
//                for (JsonNode host : data) {
//                    Map<String, String> map = new HashMap();
//                    hostId = host.get("hostid").asText();
//                    hostUUID = host.get("host").asText();
//                    hostName = host.get("name").asText();
//                    map.put("hostId", hostId);
//                    map.put("hostName", hostName);
//                    hostIdMap.put(hostUUID, map);
//                }
//            }
//        }

        Map<String, VirtualizationDataCacheInfo> cacheInfoMap = new HashMap<>();
        if (type == InstanceNotifyType.VirtualSyncUpd || type == InstanceNotifyType.VirtualSyncDelete) {
            for (VirtualizationDataCacheInfo data : cacheInfoListlist) {
                VirtualizationDataInfo virtualizationDataInfo = data.getVirtualizationDataInfo();
                cacheInfoMap.put(virtualizationDataInfo.getId(), data);
            }
        }

        //循环获取到的虚拟化设备
        for (VirtualizationDataCacheInfo dataCacheInfo : cacheInfoListlist) {
            VirtualizationDataInfo info = dataCacheInfo.getVirtualizationDataInfo();
            Integer instanceId = dataCacheInfo.getIntanceId();
//            if (hostIdMap != null && hostIdMap.get(info.getUUID()) != null) {
//                //获取zabbix上的hostId和hostName
//                Map<String, String> hostMap = hostIdMap.get(info.getUUID());
//                info.setHostId(hostMap.get("hostId"));
//                info.setHostName(hostMap.get("hostName"));
//            }
            Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(info));
            //虚拟化设备的type和模型的name相同，则该设备加入到模型中
            String modelIndex = "";
            Integer modelId = 0;
            String modelName = "";
            //通过匹配模型名称，获取对应的模型数据
            List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
            if (info.getType() != null && modelInfoByName != null && modelInfoByName.get(info.getType()) != null) {
                List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>(modelInfoByName.get(info.getType()));
                if (propertiesParamList != null && propertiesParamList.size() > 0) {
                    for (AddModelInstancePropertiesParam p : propertiesParamList) {
                        AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                        TransferUtils.transferBean(p, instanceParam);
                        //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                        instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                        modelIndex = instanceParam.getModelIndex();
                        modelId = instanceParam.getModelId();
                        modelName = instanceParam.getModelName();
                        propertiesParamLists.add(instanceParam);
                    }
                }
            }
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            instanceParam.setModelIndex(modelIndex);
            instanceParam.setModelId(modelId);
            instanceParam.setModelName(modelName);
            instanceParam.setInstanceType(DataType.MODEL_VIRTUAL.getName());
            instanceParam.setInstanceName(info.getInstanceName());
            instanceParam.setRelationInstanceId(param.getRelationInstanceId());
            instanceParam.setPropertiesList(propertiesParamLists);


            if ((!Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                fillInstanceInfoList.add(instanceParam);
                VCenterInfo vCenterInfo = new VCenterInfo(instanceParam, info);
                fillVCenterInfos.add(vCenterInfo);
                if (type == InstanceNotifyType.VirtualSyncUpd || type == InstanceNotifyType.VirtualSyncDelete) {
                    VirtualizationDataCacheInfo cacheInfo = cacheInfoMap.get(info.getId());
                    if (null != cacheInfo) {
                        instanceParam.setInstanceId(cacheInfo.getIntanceId());
                    }
                }
                if (instanceId != null && instanceId.intValue() != 0) {
                    instanceParam.setInstanceId(instanceId);
                }
            }
        }
    }

    private Set<String> getAllParentSourceId(Map<String, String> map, String targetId, Set<String> allSourceIds) {
        if (Strings.isNullOrEmpty(map.get(targetId))) {
            return allSourceIds;
        }
        String sourceId = map.get(targetId);
        allSourceIds.add(sourceId);
        getAllParentSourceId(map, sourceId, allSourceIds);
        return new HashSet<>();
    }

    /**
     * 获取虚拟化树结构
     * 山鹰特殊处理
     *
     * @param param
     * @return
     */
    @Override
    public Reply getVirtualDeviceTree(QueryVirtualInstanceParam param) {
        Map map = new HashMap();
        Object data = new Object();
        if (syncNeo4j) {//是否同步到neo4j中，形成实例拓扑数据
            data = getVirtualDeviceTreeToNeo4j(param).getData();
        } else {
            data = getVirtualDeviceTreeToEs(param).getData();
        }
        map.put("data", data);
        map.put("syncNeo4j", syncNeo4j);
        return Reply.ok(map);
    }

    /**
     * 获取虚拟化树结构
     * 从es实例中获取
     *
     * @param param
     * @return
     */
    public Reply getVirtualDeviceTreeToEs(QueryVirtualInstanceParam param) {
        try {
            //查询数据库中VCenter实例关联的虚拟化设备实例id
            List<MwModelInstanceParam> virInstanceList = mwModelVirtualizationDao.queryVirualInstanceInfoByModelIndex(param.getModelIndex(), param.getRelationInstanceId());
            //获取查询数据的所有modelIndex
            Set<String> modelIndexSet = new HashSet<>();
            for (MwModelInstanceParam m : virInstanceList) {
                modelIndexSet.add(m.getModelIndex());
            }
            //获取所有的es中虚拟化设备信息(不做权限控制)
            QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
            param1.setRelationInstanceId(param.getRelationInstanceId());
            List<Map<String, Object>> list = mwModelViewServiceImpl.selectInstanceInfoByRelationInstanceId(param1);
            List<MwInstanceInfoParam> instanceList = JSONArray.parseArray(JSONArray.toJSONString(list), MwInstanceInfoParam.class);
            //按照集群分组
            if (param.getGroupType() != null && clusterGroupType.equals(param.getGroupType())) {
                for (MwInstanceInfoParam infoParam : instanceList) {
                    if (!Strings.isNullOrEmpty(infoParam.getClusterId())) {
                        infoParam.setPId(infoParam.getClusterId());
                    }
                }
            }
            List<MwInstanceInfoParam> lists = new ArrayList<>();
            String roleId = iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName());
//            if (!joptsimple.internal.Strings.isNullOrEmpty(roleId) && !roleId.equals("0")) {
//                //非管理员权限
//                GlobalUserInfo globalUser = userService.getGlobalUser();
//                //获取用户权限下的虚拟化设备id
//                List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_VIRTUAL);
//
//
//
//                for (String id : allTypeIdList) {
//                    String pId = "";
//                    if (!Strings.isNullOrEmpty(id)) {
//                        for (MwInstanceInfoParam m : instanceList) {
//                            //主机id以host开头：host-31
//                            //如果是主机设备做了权限控制，那么主机下的所有虚拟机也会显示
//                            if (id.indexOf("host") != -1) {
//                                if (id.equals(m.getPId())) {
//                                    lists.add(m);
//                                }
//                            }
//                            if (id.equals(m.getId())) {
//                                pId = m.getId();
//                                //获取父级Id
//                                getParentList(instanceList, pId, lists, true);
//                            }
//                        }
//                    }
//                }
//                instanceList = instanceList.stream().filter(s -> allTypeIdList.contains(s.getId())).collect(Collectors.toList());
//            }
            lists.addAll(instanceList);
            List<MwInstanceInfoParam> allList = new ArrayList<>();
            allList = lists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getId()))), ArrayList::new));

            //根据VCenter实例获取es数据信息
            QueryModelInstanceParam instanceParam = new QueryModelInstanceParam();
            TransferUtils.transferBean(param, instanceParam);
            List<Map<String, Object>> VCenterInfo = mwModelInstanceService.getInfoByInstanceId(instanceParam);
            List<MwInstanceInfoParam> VCenterInstanceList = JSONArray.parseArray(JSONArray.toJSONString(VCenterInfo), MwInstanceInfoParam.class);


            MwInstanceInfoParam vcenterMap = new MwInstanceInfoParam();
            Integer modelId = 0;
            Integer modelInstanceId = 0;
            String instanceName = "";
            String modelIndex = "";
            String esId = "";
            String assetsId = "";
            for (MwInstanceInfoParam m : VCenterInstanceList) {
                modelId = m.getModelId();
                modelInstanceId = m.getModelInstanceId();
                instanceName = m.getInstanceName();
                modelIndex = m.getModelIndex();
                esId = m.getEsId();
                assetsId = Strings.isNullOrEmpty(m.getAssetsId()) ? DEFAULT_ASSETS_ID : m.getAssetsId();
            }
            vcenterMap.setModelId(modelId);
            vcenterMap.setModelInstanceId(modelInstanceId);
            vcenterMap.setInstanceName(instanceName);
            vcenterMap.setModelIndex(modelIndex);
            vcenterMap.setEsId(esId);
            vcenterMap.setPId(PID);
            vcenterMap.setId(assetsId);
            vcenterMap.setType(VCENTER.getType());
            allList.add(vcenterMap);
            //将vcenter数据作为树的根节点，加入list中
            return Reply.ok(allList);
        } catch (Throwable e) {
            log.error("fail to getVirtualDeviceTree cause:{}", e);
            return Reply.fail(500, "获取虚拟化树结构数据失败");
        }
    }


    /**
     * 获取虚拟化树结构
     * 从实例拓扑中获取
     *
     * @param param
     * @return
     */
    public Reply getVirtualDeviceTreeToNeo4j(QueryVirtualInstanceParam param) {
        try {
            //获取模型信息
            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoByIndex(param.getModelIndex());

            //遍历关系库
            Session session = connectionPool.getSession();
            NodeParam nodeParam = new NodeParam(modelInfo.getModelId(), param.getModelInstanceId());
            InstanceNode instanceNode = new InstanceNode(nodeParam);
            int maxLevel = ModelAssetUtils.findTreeLevel(session, instanceNode, ModelAssetUtils.VIRTUAL_SPACE);

            VirtualView parent = null;
            if (maxLevel > 0) {
                parent = new VirtualView();
                GlobalUserInfo globalUser = userService.getGlobalUser();
                List<EdgeParam> edges = ModelAssetUtils.findTreeEdgeBySpace(session, nodeParam, ModelAssetUtils.VIRTUAL_SPACE);
                EdgeParam rootEdge = edges.get(0);
                List<EdgeParam> filterEdges = new ArrayList<>();

                Map<String, String> relationParentMap = edges.stream().collect(Collectors.toMap(s -> String.valueOf(s.getTargetInstanceId()), s -> String.valueOf(s.getSourceInstanceId())));


                if (!globalUser.isSystemUser()) {
                    List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_VIRTUAL);
                    Set<String> idSet = new HashSet<>(allTypeIdList);
                    for (EdgeParam edgeParam : edges) {
                        //和该数据有关联的全部数据
                        if (idSet.contains(String.valueOf(edgeParam.getTargetInstanceId()))) {
                            filterEdges.add(edgeParam);
                        }
                        //获取该数据的全部上级数据
                    }
                    Set<String> sourceIds = new HashSet<>();
                    for (String str : idSet) {
                        String sourceId = relationParentMap.get(str);
                        sourceIds.add(sourceId);
                        getAllParentSourceId(relationParentMap, sourceId, sourceIds);
                    }
                    for (EdgeParam edgeParam : edges) {
                        if (sourceIds.contains(String.valueOf(edgeParam.getTargetInstanceId()))) {
                            filterEdges.add(edgeParam);
                        }
                    }
                    filterEdges = filterEdges.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getTargetInstanceId()
                            + ";" + s.getSourceInstanceId()))), ArrayList::new));
                } else {
                    filterEdges = edges;
                }

                Map<String, List<String>> edgeMap = new HashMap<>();
                List<Integer> modelIds = new ArrayList<>();
                List<Integer> instanceIds = new ArrayList<>();

                for (EdgeParam edgeParam : filterEdges) {
                    List<String> edgeParamList = edgeMap.get(edgeParam.getSource());
                    if (null == edgeParamList) {
                        edgeParamList = new ArrayList<>();
                        edgeMap.put(edgeParam.getSource(), edgeParamList);
                    }
                    edgeParamList.add(edgeParam.getTarget());

                    if (!modelIds.contains(edgeParam.getSourceModelId())) {
                        modelIds.add(edgeParam.getSourceModelId());
                    }

                    if (!instanceIds.contains(edgeParam.getSourceInstanceId())) {
                        instanceIds.add(edgeParam.getSourceInstanceId());
                    }

                    if (!modelIds.contains(edgeParam.getTargetModelId())) {
                        modelIds.add(edgeParam.getTargetModelId());
                    }

                    if (!instanceIds.contains(edgeParam.getTargetInstanceId())) {
                        instanceIds.add(edgeParam.getTargetInstanceId());
                    }
                }

                List<String> removeList = new ArrayList<>();
                //按照集群分组
                if (param.getGroupType() != null && clusterGroupType.equals(param.getGroupType())) {
                    Iterator<Map.Entry<String, List<String>>> iterator = edgeMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, List<String>> entry = iterator.next();
                        String key = entry.getKey();
                        //获取集群对应的key
                        if (key.indexOf(strValueConvert(CLUSTER.getModelId())) != -1) {
                            //根据集群的key找到对应下级主机数据
                            List<String> value = entry.getValue();
                            List<String> list = new ArrayList<>(value);
                            for (String str : value) {
                                //根据主机key获取对应下级的虚拟机数据
                                List<String> items = edgeMap.get(str);
                                removeList.add(str);
                                //将主机数据和虚拟机数据放到一起
                                list.addAll(items);
                            }
                            //将原本集群主机下的虚拟机，放到集群下，和主机同级
                            edgeMap.put(key, list);
                        }
                    }
                    //删除原先的连线结构
                    for (String removeStr : removeList) {
                        edgeMap.remove(removeStr);
                    }
                }

                //查询模型和实例信息
                Map<String, ModelInfo> modelInfoMap = new HashMap<>();
                List<String> modelIndexList = new ArrayList<>();
                if (modelIds.size() > 0) {
                    List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListByIds(modelIds);
                    for (ModelInfo data : modelInfoList) {
                        modelIndexList.add(data.getModelIndex());
                        modelInfoMap.put(data.getModelId().toString(), data);
                    }
                }

                Map<String, ModelInstanceDto> instanceMap = new HashMap<>();
                Map<String, VirtualInstance> esInstanceMap = new HashMap<>();
                if (instanceIds.size() > 0) {
                    List<ModelInstanceDto> modelInstanceDtos = new ArrayList<>();
                    List<List<Integer>> instanceIdLists = new ArrayList<>();
                    instanceIdLists = Lists.partition(instanceIds, insBatchFetchNum);
                    for (List<Integer> instanceIdList : instanceIdLists) {
                        if (CollectionUtils.isNotEmpty(instanceIdList)) {
                            Map critiria = new HashMap();
                            critiria.put("modelInstanceIds", instanceIdList);
                            modelInstanceDtos.addAll(mwModelManageDao.selectModelInstance(critiria));
                        }
                    }
                    for (ModelInstanceDto data : modelInstanceDtos) {
                        instanceMap.put(data.getInstanceId().toString(), data);
                    }

                    QueryInstanceModelParam queryInstanceModelParam = new QueryInstanceModelParam();
                    queryInstanceModelParam.setInstanceIds(instanceIds);
                    queryInstanceModelParam.setModelIndexs(modelIndexList);
                    List<VirtualInstance> virtualInstances = mwModelViewCommonService.getModelListInfoByCommonQuery(VirtualInstance.class, null, queryInstanceModelParam);

                    List<VirtualInstance> collect1 = virtualInstances.stream().filter(s -> Strings.isNullOrEmpty(s.getId())).collect(Collectors.toList());
                    //对虚拟机Id进行去重
                    List<VirtualInstance> collect2 = virtualInstances.stream().filter(s -> !Strings.isNullOrEmpty(s.getId())).collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(VirtualInstance::getId))), ArrayList::new));
                    collect2.addAll(collect1);
                    for (VirtualInstance virtualInstance : collect2) {
                        esInstanceMap.put(virtualInstance.getModelInstanceId().toString(), virtualInstance);
                    }
                }


                Set<String> visitedSet = new HashSet<>();

                String[] rootValues = rootEdge.getSource().split(EdgeParam.SEP);
                parent.setModelId(rootValues[0]);
                parent.setModelInstanceId(rootValues[1]);
                modelInfoMap.put(modelInfo.getModelId().toString(), modelInfo);//加上顶端的Vcenter数据
                ModelInfo pmodelInfo = modelInfoMap.get(parent.getModelId());
                ModelInstanceDto instanceInfo = instanceMap.get(parent.getModelInstanceId());
                VirtualInstance virtualInstance = esInstanceMap.get(parent.getModelInstanceId());
                if (null != modelInfo && null != instanceInfo && null != virtualInstance) {
                    parent.extractInfo(pmodelInfo, instanceInfo, virtualInstance);
                    visitedSet.add(rootEdge.getSource());
                    doVisitEdgeMap(parent, edgeMap, visitedSet, modelInfoMap, instanceMap, esInstanceMap);
                }
            }

            ModelVirtualTreeView view = new ModelVirtualTreeView();
            if (null != parent) {
                VirtualView virtual = cloneAndRemoveFromVirtualTree(parent, VirtualizationType.DATASTORE.getType());
                VirtualView parent01 = cloneAndRemoveFromVirtualTree(parent, VirtualizationType.DATASTORE.getType());
                VirtualView datastore = cloneAndRemoveFromVirtualTree(parent01, VirtualizationType.VIRTUALMACHINE.getType());

                view.addVirtualTree(virtual);
                view.addDatastoreTree(datastore);
            }
            return Reply.ok(view);
        } catch (Throwable e) {
            log.error("fail to getVirtualDeviceTree cause:{}", e);
            return Reply.fail(500, "获取虚拟化树结构数据失败");
        }
    }

    private void doVisitEdgeMap(VirtualView parent, Map<String, List<String>> map, Set<String> visitedSet
            , Map<String, ModelInfo> modelInfoMap, Map<String, ModelInstanceDto> instanceMap, Map<String, VirtualInstance> esInstanceMap) {

        String key = parent.getModelId() + EdgeParam.SEP + parent.getModelInstanceId();
        List<String> list = map.get(key);
        if (null != list && list.size() > 0) {
            List<VirtualView> childList = new ArrayList<>();
            for (String childValue : list) {
                if (!visitedSet.contains(childValue)) {
                    VirtualView child = new VirtualView();
                    String[] values = childValue.split(EdgeParam.SEP);
                    child.setModelId(values[0]);
                    child.setModelInstanceId(values[1]);
                    ModelInfo modelInfo = modelInfoMap.get(child.getModelId());
                    ModelInstanceDto instanceInfo = instanceMap.get(child.getModelInstanceId());
                    VirtualInstance virtualInstance = esInstanceMap.get(child.getModelInstanceId());
                    if (null == modelInfo) {
                        log.warn("doVisitEdgeMap modelinfo {} is null", child.getModelId());
                        continue;
                    }
                    if (null == instanceInfo) {
                        log.warn("doVisitEdgeMap instanceInfo {} is null", child.getModelInstanceId());
                        continue;
                    }
                    if (null == virtualInstance) {
                        log.warn("esInstanceMap instanceInfo {} is null", child.getModelInstanceId());
                        continue;
                    }

                    child.extractInfo(modelInfo, instanceInfo, virtualInstance);

                    parent.addChildren(child);
                    childList.add(child);
                    visitedSet.add(childValue);
                }
            }
            if (CollectionUtils.isNotEmpty(parent.getChildren())) {
                Collections.sort(parent.getChildren(), new Comparator<VirtualView>() {
                    @Override
                    public int compare(VirtualView o1, VirtualView o2) {
                        if (null == o1.getInstanceName()) {
                            return 1;
                        }
                        if (null == o2.getInstanceName()) {
                            return -1;
                        }
                        return Collator.getInstance(Locale.CHINESE).compare(o1.getInstanceName(), o2.getInstanceName());
                    }
                });
            }

            for (VirtualView child : childList) {
                doVisitEdgeMap(child, map, visitedSet, modelInfoMap, instanceMap, esInstanceMap);
            }
        }
    }

    private VirtualView cloneAndRemoveFromVirtualTree(VirtualView tree, String type) {
        VirtualView virtualView = tree.clone();
        removeVirtualType(virtualView, type);
        return virtualView;
    }

    private void removeVirtualType(VirtualView virtualView, String type) {
        List<VirtualView> childs = virtualView.getChildren();
        if (null != childs && childs.size() > 0) {
            List<VirtualView> pendingRemove = new ArrayList<>();
            for (VirtualView child : childs) {
                if (type.equals(child.getType())) {
                    pendingRemove.add(child);
                }
            }
            childs.removeAll(pendingRemove);
            for (VirtualView child : childs) {
                removeVirtualType(child, type);
            }
        }
    }

    /**
     * 获取虚拟化设备基础数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getVirtualDeviceBaseInfo(QueryVirtualInstanceParam param) {
        try {
            //根据VCenter实例获取es数据信息
            QueryModelInstanceParam instanceParam = new QueryModelInstanceParam();
            TransferUtils.transferBean(param, instanceParam);
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(instanceParam);
            //获取虚拟化VCenter的连接信息，URL、用户名、密码
            String userName = "";
            String url = "";
            String password = "";
            String ip = "";
            String instanceName = "";
            for (Map<String, Object> m : listInfo) {
                userName = strValueConvert(m.get("USERNAME"));
                url = strValueConvert(m.get("HOST"));
                password = RSAUtils.decryptData(strValueConvert(m.get("PASSWORD")), RSAUtils.RSA_PRIVATE_KEY);
                String encryptData = RSAUtils.encryptData("Dev20$uiyD7", RSAUtils.RSA_PUBLIC_KEY);
                ip = strValueConvert(m.get("inBandIp"));
                instanceName = strValueConvert(m.get("hostName"));
            }

            List<ProxyInfo> proxyInfos = new ArrayList<>();
            VCenterBaseInfoParam vCenterBaseInfoParam = new VCenterBaseInfoParam(url, userName, password
                    , param.getVirtualType(), param.getVirtualName());

            VirtualizationBaseInfo virInfo = proxySearch.doProxySearch(VirtualizationBaseInfo.class, proxyInfos, param.getModelInstanceId(), "mwVCenterService"
                    , "getVCenterBaseInfo", vCenterBaseInfoParam, null);

            if (null == virInfo) {
                virInfo = new VirtualizationBaseInfo();
            }

            if (proxyInfos.size() == 0) {
                log.info("do getVirtualDeviceBaseInfo local search");
                ConnectedVimServiceBase cs = new ConnectedVimServiceBase();
                cs.connect(url, userName, password);
                if (cs.si == null) {
                    return Reply.fail(500, "虚拟化连接失败！");
                }
                virInfo = getVirtualDeviceBaseInfo(cs.si, param.getVirtualType(), param.getVirtualName());
                cs.disconnect();
            }

            //查询类型为VCenter时,加入Ip，名称作为基础数据
            if (VCENTER.getType().equals(param.getVirtualType())) {
                virInfo.setIp(ip);
                virInfo.setInstanceName(instanceName);
            }
            return Reply.ok(virInfo);
        } catch (Throwable e) {
            log.error("fail to getVirtualDeviceBaseInfo case:{}", e);
            return Reply.fail(500, "获取虚拟化设备基础数据失败");
        }
    }

    /**
     * 获取虚拟化、数据存储列表数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getVirtualDeviceInfoList(QueryVirtualInstanceParam param) {
        //根据VCenter实例获取es数据信息
        try {
            List<VirtualizationMonitorInfo> allList = getVirtualizationMonitorInfos(param);
            redisTemplate.opsForValue().set(hKey, JSONObject.toJSONString(allList), 30, TimeUnit.MINUTES);
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            pageInfo.setTotal(allList.size());
            allList = pageList.getList(allList, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(allList);
            return Reply.ok(pageInfo);
        } catch (ModelManagerException e) {
            log.error("fail to getVirtualDeviceInfoList case: {}", e);
            return Reply.fail(500, e.getMessage());
        } catch (Throwable e) {
            log.error("fail to getVirtualDeviceInfoList case: {}", e);
            return Reply.fail(500, "获取虚拟化监测列表数据失败");
        }
    }

    private List<VirtualizationMonitorInfo> getVirtualizationMonitorInfos(QueryVirtualInstanceParam param) throws ModelManagerException {
        List<VirtualizationMonitorInfo> list;
        QueryModelInstanceParam instanceParam = new QueryModelInstanceParam();
        TransferUtils.transferBean(param, instanceParam);
        instanceParam.setPageSize(param.getPageSize());
        instanceParam.setPageNumber(1);
        List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(instanceParam);

        //获取虚拟化VCenter的连接信息，URL、用户名、密码
        String userName = "";
        String url = "";
        String password = "";
        Integer monitorServerId = 0;

        for (Map<String, Object> m : listInfo) {
            userName = m.get("USERNAME").toString();
            url = m.get("HOST").toString();
            if (m.get("monitorServerId") != null) {
                monitorServerId = intValueConvert(m.get("monitorServerId"));
            }
            password = RSAUtils.decryptData(m.get("PASSWORD").toString(), RSAUtils.RSA_PRIVATE_KEY);
        }


        List<MwModelInstanceParam> virInstanceList = mwModelVirtualizationDao.queryVirualInstanceInfoByModelIndex(param.getModelIndex(), param.getRelationInstanceId());
        //获取查询数据的所有modelIndex和instanceId，下面的批量删除使用
        Set<String> modelIndexSet = new HashSet<>();
        Set<Integer> instanceIdSet = new HashSet<>();
        for (MwModelInstanceParam m : virInstanceList) {
            if (m != null && !Strings.isNullOrEmpty(m.getModelIndex())) {
                modelIndexSet.add(m.getModelIndex());
            }
            if (m != null && m.getInstanceId() != null) {
                instanceIdSet.add(m.getInstanceId());
            }
        }

        QueryInstanceModelParam instanceModelParam = new QueryInstanceModelParam();
        instanceModelParam.setModelIndexs(new ArrayList<>(modelIndexSet));
        instanceModelParam.setInstanceIds(new ArrayList<>(instanceIdSet));
        instanceModelParam.setPageSize(pageSize);
        //获取es中所有VCenter的关联设备（集群主机虚拟机等）
        Map<String, Object> mapInfo = mwModelViewServiceImpl.getModelListInfoByBase(instanceModelParam);
        List<Map<String, Object>> modelListMap = new ArrayList<>();
        if (mapInfo != null && mapInfo.get("data") != null) {
            modelListMap = (List<Map<String, Object>>) mapInfo.get("data");
        }
        Map<String, Integer> modelInstanceIdMap = new HashMap<>();
        //将主机和虚拟机id 和 instanceId做关联
        if (modelListMap != null && modelListMap.size() > 0) {
            for (Map<String, Object> m : modelListMap) {
                String id = "";
                Integer instanceId = 0;
                if (m != null && m.get("id") != null) {
                    id = m.get("id").toString();
                }
                if (m != null && m.get("modelInstanceId") != null) {
                    instanceId = intValueConvert(m.get("modelInstanceId"));
                }
                modelInstanceIdMap.put(id, instanceId);
            }
            log.info("modelListMap size:{},modelInstanceIdMap size:{}", modelListMap.size(), modelInstanceIdMap.size());
            if (debug) {
                log.info("modelInstanceIdMap data :{}", modelInstanceIdMap.toString());
            }
        }

        List<ProxyInfo> proxyInfos = new ArrayList<>();
        VCenterVirtualInstanceParam vCenterVirtualInstanceParam = new VCenterVirtualInstanceParam(url, userName
                , password, param, modelInstanceIdMap);

        Type type = new TypeToken<List<VirtualizationMonitorInfo>>() {
        }.getType();
        List<VirtualizationMonitorInfo> proxyResult = proxySearch.doProxySearch(List.class, proxyInfos, param.getModelInstanceId(), "mwVCenterService"
                , "getVCenterVirtualHostList", vCenterVirtualInstanceParam, type);

        //通过本机获取列表数据
        if (proxyInfos.size() == 0) {
            log.info("do getVirtualizationMonitorInfos local search");
            ConnectedVimServiceBase cs = new ConnectedVimServiceBase();
            cs.connect(url, userName, password);
            if (cs.si == null) {
                throw new ModelManagerException("虚拟化连接失败");
            }
            list = getVirtualHostList(cs.si, param, modelInstanceIdMap);
            cs.disconnect();
        } else {
            list = proxyResult;
        }

        //删除取不到instanceId的数据
        Iterator<VirtualizationMonitorInfo> ite = list.iterator();
        while (ite.hasNext()) {
            VirtualizationMonitorInfo data = ite.next();
            if (null == data.getInstanceId()) {
                ite.remove();
                log.warn("getVirtualizationMonitorInfos find no instanceId {}", data.toDebugString());
            }
        }

        List<VirtualizationMonitorInfo> allList = new ArrayList<>();
        List<VirtualizationMonitorInfo> lists = new ArrayList<>();
        String roleId = iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName());
//        if (!joptsimple.internal.Strings.isNullOrEmpty(roleId) && !roleId.equals("0")) {
//            //非管理员权限
//            GlobalUserInfo globalUser = userService.getGlobalUser();
//            //获取用户权限下的虚拟化设备id
//            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_VIRTUAL);
//            Set<String> idSet = new HashSet<>(allTypeIdList);
//            for (VirtualizationMonitorInfo info : list) {
//                if (idSet.contains(String.valueOf(info.getInstanceId()))) {
//                    lists.add(info);
//                }
//            }
//        } else {
            lists.addAll(list);
//        }

        allList = lists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getInstanceId()))), ArrayList::new));

        //虚拟设备是否纳管
        if (param.getIsConnect() != null) {
            //获取所有基础设施下的资产数据
            List<String> modelIndexs = mwModelVirtualizationDao.getAllModelIndexByBaseDevice();
            //获取所有的es中资产数据
            QueryInstanceModelParam qParam = new QueryInstanceModelParam();
            qParam.setModelIndexs(new ArrayList<>(modelIndexs));
            List<Map<String, Object>> allDeviceList = modelSever.getInstanceInfoByModelId(qParam);
            Map<String, Map<String, Object>> assectByConnectMap = new HashMap<>();
            List<String> assetsNames = new ArrayList<>();
            for (Map<String, Object> map : allDeviceList) {
                assectByConnectMap.put(map.get("inBandIp") + "_" + map.get("monitorServerId"), map);
                if (map.get("hostName") != null) {
                    assetsNames.add(map.get("hostName").toString());
                }
            }
            for (VirtualizationMonitorInfo virInfo : allList) {
                boolean isTrue = false;
                String ip = virInfo.getIp();
                String instanceName = virInfo.getInstanceName();
                if (assectByConnectMap != null && assectByConnectMap.get(ip + "_" + monitorServerId) != null) {
                    virInfo.setIsConnect(true);
                } else {
                    //再次判断多Ip的设备，如果名称前缀重复，也表示已纳管。
                    for (String assetsName : assetsNames) {
                        //名称重复（资产名称包含虚拟化名称时），表示可以跳转
                        if (assetsName.indexOf(instanceName) != -1) {
                            isTrue = true;
                        }
                    }
                    if (isTrue) {
                        virInfo.setIsConnect(true);
                    } else {
                        virInfo.setIsConnect(false);
                    }
                }
                //多IP是否有值
                if (CollectionUtils.isNotEmpty(virInfo.getIpParam())) {
                    for (VirtualizationIpAdressInfo ipParam : virInfo.getIpParam()) {
                        if (!Strings.isNullOrEmpty(ipParam.getIp())) {
                            if (assectByConnectMap != null && assectByConnectMap.containsKey(ip + "_" + monitorServerId)) {
                                virInfo.setIsConnect(true);
                                ipParam.setMatch(true);
                            }
                        }
                    }
                }
            }
            if (param.getIsConnect()) {
                //纳管
                allList = allList.stream().filter(s -> s.getIsConnect() == true).collect(Collectors.toList());
            } else {
                allList = allList.stream().filter(s -> s.getIsConnect() == false).collect(Collectors.toList());
            }
        }
        Collections.sort(allList, new Comparator<VirtualizationMonitorInfo>() {
            @Override
            public int compare(VirtualizationMonitorInfo o1, VirtualizationMonitorInfo o2) {
                if (null == o1.getInstanceName()) {
                    return 1;
                }
                if (null == o2.getInstanceName()) {
                    return -1;
                }
                return Collator.getInstance(Locale.CHINESE).compare(o1.getInstanceName(), o2.getInstanceName());
            }
        });

//        Collections.sort(allList, new AlphanumericComparator());

        if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField())) {
            ListSortUtil<VirtualizationMonitorInfo> finalHostTableDtos = new ListSortUtil<>();
            String sort = "sort" + param.getSortField().substring(0, 1).toUpperCase() + param.getSortField().substring(1);
            //查看当前属性名称是否在对象中
            try {
                Field field = VirtualizationMonitorInfo.class.getDeclaredField(sort);
                finalHostTableDtos.sort(allList, sort, param.getSortMode());
            } catch (NoSuchFieldException e) {
                log.info("getVirtualizationMonitorInfos {}", e);
                finalHostTableDtos.sort(allList, param.getSortField(), param.getSortMode());
            }
        }
        return allList;
    }

    class AlphanumericComparator implements Comparator<VirtualizationMonitorInfo> {
        public int compare(VirtualizationMonitorInfo s1, VirtualizationMonitorInfo s2) {
            String alpha1 = extractAlpha(s1.getInstanceName());
            String alpha2 = extractAlpha(s2.getInstanceName());
            int cmp = alpha1.compareTo(alpha2);
            if (cmp != 0) {
                return cmp;
            }
            String num1 = extractNum(s1.getInstanceName());
            String num2 = extractNum(s2.getInstanceName());
            if (Strings.isNullOrEmpty(num1)) {
                num1 = "0";
            }
            if (Strings.isNullOrEmpty(num2)) {
                num2 = "0";
            }
            return Long.compare(Long.parseLong(num1), Long.parseLong(num2));
        }

        private String extractAlpha(String s) {
            return s.replaceAll("\\d", "");
        }

        private String extractNum(String s) {
            return s.replaceAll("\\D", "");
        }
    }

    /**
     * 获取时间段内的虚拟化监测数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getVirtualMonitorInfoByHistory(QueryVirtualInstanceParam param) {
        try {
            QueryModelInstanceParam instanceParam = new QueryModelInstanceParam();
            TransferUtils.transferBean(param, instanceParam);
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(instanceParam);
            //获取虚拟化VCenter的连接信息，URL、用户名、密码
            String userName = "";
            String url = "";
            String password = "";
            for (Map<String, Object> m : listInfo) {
                userName = m.get("USERNAME").toString();
                url = m.get("HOST").toString();
                password = RSAUtils.decryptData(m.get("PASSWORD").toString(), RSAUtils.RSA_PRIVATE_KEY);
            }

            Map<String, PerformanceManage> map = new HashMap<>();
            String roleId = iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName());
//            if (!joptsimple.internal.Strings.isNullOrEmpty(roleId) && !roleId.equals("0")) {
//                //非管理员权限
//                GlobalUserInfo globalUser = userService.getGlobalUser();
//                //获取用户权限下的虚拟化设备id
//                List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_VIRTUAL);
//                //点击树节点为主机层次时，需要判断可有权限控制,主机下的虚拟机设置了权限，主机没有设置，则主机本体数据不显示，历史记录也不显示
//                if (allTypeIdList != null && allTypeIdList.contains(param.getVirtualId()) && VirtualizationType.HOSTSYSTEM.getType().equals(param.getVirtualType())) {
//                    map = doGetPerfHistoryData(url, userName, password, param);
//                } else if (VirtualizationType.VIRTUALMACHINE.getType().equals(param.getVirtualType())) {
//                    map = doGetPerfHistoryData(url, userName, password, param);
//                }
//            } else {
                map = doGetPerfHistoryData(url, userName, password, param);
//            }
            return Reply.ok(map);
        } catch (ModelManagerException e) {
            log.error("fail to getVirtualMonitorInfoByHistory case:{}", e);
            return Reply.fail(500, e.getMessage());
        } catch (Throwable e) {
            log.error("fail to getVirtualMonitorInfoByHistory case:{}", e);
            return Reply.fail(500, "获取虚拟化历史监测数据失败");
        }
    }

    private Map<String, PerformanceManage> doGetPerfHistoryData(String url, String userName, String password, QueryVirtualInstanceParam param) throws ModelManagerException {
        Map<String, PerformanceManage> ret = new HashMap<>();
        List<ProxyInfo> proxyInfos = new ArrayList<>();
        VCenterVirtualInstanceParam vCenterVirtualInstanceParam = new VCenterVirtualInstanceParam(url, userName, password, param);

        Type type = new TypeToken<HashMap<String, PerformanceManage>>() {
        }.getType();
        ret = proxySearch.doProxySearch(Map.class, proxyInfos, param.getModelInstanceId(), "mwVCenterService"
                , "getVCenterPerfHistoryData", vCenterVirtualInstanceParam, type);

        if (proxyInfos.size() == 0) {
            log.info("do getVirtualMonitorInfoByHistory local search");
            ConnectedVimServiceBase cs = new ConnectedVimServiceBase();
            cs.connect(url, userName, password);
            if (cs.si == null) {
                throw new ModelManagerException("虚拟化连接失败");
            }
            ret = getPerfHistoryData(cs.si, param);
            cs.disconnect();
        }

        return ret;
    }

    /**
     * 获取VCenter,DataCenter,cluster的cpu,memory,Datastore饼状图信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getVirDeviceByPieSimple(QueryVirtualInstanceParam param) {
        List<VirtualizationMonitorInfo> virtualizationMonitorList = new ArrayList<>();
        QueryModelInstanceParam instanceParam = new QueryModelInstanceParam();
        try {
            TransferUtils.transferBean(param, instanceParam);
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(instanceParam);
            log.info("获取虚拟化VCenter的连接信息" + listInfo);
            //获取虚拟化VCenter的连接信息，URL、用户名、密码
            String userName = "";
            String url = "";
            String password = "";
            for (Map<String, Object> m : listInfo) {
                userName = m.get("USERNAME") != null ? m.get("USERNAME").toString() : "";
                url = m.get("HOST") != null ? m.get("HOST").toString() : "";
                password = RSAUtils.decryptData(m.get("PASSWORD") != null ? m.get("PASSWORD").toString() : "", RSAUtils.RSA_PRIVATE_KEY);
            }

            List<ProxyInfo> proxyInfos = new ArrayList<>();
            VCenterVirtualInstanceParam vCenterInfoParam = new VCenterVirtualInstanceParam(url, userName, password
                    , param);

            Type type = new TypeToken<List<VirtualizationMonitorInfo>>() {
            }.getType();
            List<VirtualizationMonitorInfo> proxyResult = proxySearch.doProxySearch(List.class, proxyInfos, param.getModelInstanceId(), "mwVCenterService"
                    , "getVCenterVirDevicePicInfo", vCenterInfoParam, type);

            log.info("getVCenterVirDevicePicInfo::" + proxyResult);
            if (proxyInfos.size() == 0) {
                log.info("do getVirDeviceByPieSimple local search");
                ConnectedVimServiceBase cs = new ConnectedVimServiceBase();
                cs.connect(url, userName, password);
                if (cs.si == null) {
                    return Reply.fail(500, "虚拟化连接失败");
                }
                getVirDevicePicInfo(cs.si, param, virtualizationMonitorList);
                cs.disconnect();
            } else {
                virtualizationMonitorList = proxyResult;
            }

            VirtualizationMonitorInfo pieSimpleInfo = new VirtualizationMonitorInfo();
            long totalCpu = 0l, usageCPU = 0l, totalMemory = 0l, usageMemory = 0l, totalStorage = 0l, freeStorage = 0l;
            //数据统计
            for (VirtualizationMonitorInfo virInfo : virtualizationMonitorList) {
                if (virInfo.getSortTotalCPU() != null && virInfo.getSortTotalCPU() != 0l) {
                    totalCpu += virInfo.getSortTotalCPU();
                }
                if (virInfo.getSortUsageCPU() != null && virInfo.getSortUsageCPU() != 0l) {
                    usageCPU += virInfo.getSortUsageCPU();
                }
                if (virInfo.getSortTotalMemory() != null && virInfo.getSortTotalMemory() != 0l) {
                    totalMemory += virInfo.getSortTotalMemory();
                }
                if (virInfo.getSortUsageMemory() != null && virInfo.getSortUsageMemory() != 0l) {
                    usageMemory += virInfo.getSortUsageMemory();
                }
                if (virInfo.getSortTotalStorage() != null && virInfo.getSortTotalStorage() != 0l) {
                    totalStorage += virInfo.getSortTotalStorage();
                }
                if (virInfo.getSortFreeStorage() != null && virInfo.getSortFreeStorage() != 0l) {
                    freeStorage += virInfo.getSortFreeStorage();
                }

            }
            //cpu
            pieSimpleInfo.setSortTotalCPU((int) totalCpu);
            pieSimpleInfo.setSortFreeCPU((int) (totalCpu - usageCPU));
            pieSimpleInfo.setSortUsageCPU((int) usageCPU);
            String cpuUtilization = "0";
            if (totalCpu != 0) {
                cpuUtilization = String.valueOf(Double.valueOf(usageCPU) / Double.valueOf(totalCpu) * 100);
            }
            pieSimpleInfo.setCpuUtilization(numToStr(cpuUtilization));
            pieSimpleInfo.setSortCpuUtilization(Double.parseDouble(numToStr(cpuUtilization)));
            String unitCPU = "MHz";
            pieSimpleInfo.setTotalCPU(UnitsUtil.getValueWithUnits(String.valueOf(totalCpu), unitCPU));
            pieSimpleInfo.setUsageCPU(UnitsUtil.getValueWithUnits(String.valueOf(usageCPU), unitCPU));
            pieSimpleInfo.setFreeCPU(UnitsUtil.getValueWithUnits(String.valueOf(totalCpu - usageCPU), unitCPU));
            //内存
            pieSimpleInfo.setSortTotalMemory(totalMemory);
            pieSimpleInfo.setSortUsageMemory(usageMemory);
            pieSimpleInfo.setSortFreeMemory(totalMemory - usageMemory);
            String memoryUtilization = "0";
            if (totalMemory != 0) {
                memoryUtilization = String.valueOf(Double.valueOf(usageMemory) / Double.valueOf(totalMemory) * 100);
            }
            pieSimpleInfo.setMemoryUtilization(numToStr(memoryUtilization));
            pieSimpleInfo.setSortMemoryUtilization(Double.parseDouble(numToStr(memoryUtilization)));
            String unitMemory = "B";
            pieSimpleInfo.setTotalMemory(UnitsUtil.getValueWithUnits(String.valueOf(totalMemory), unitMemory));
            pieSimpleInfo.setUsageMemory(UnitsUtil.getValueWithUnits(String.valueOf(usageMemory), unitMemory));
            pieSimpleInfo.setFreeMemory(UnitsUtil.getValueWithUnits(String.valueOf(totalMemory - usageMemory), unitMemory));
            //存储
            pieSimpleInfo.setSortTotalStorage(totalStorage);
            pieSimpleInfo.setSortFreeStorage(freeStorage);
            pieSimpleInfo.setSortUsageStorage(totalStorage - freeStorage);
            String storageUtilization = "0";
            if (totalStorage != 0) {
                storageUtilization = String.valueOf(Double.valueOf(totalStorage - freeStorage) / Double.valueOf(totalStorage) * 100);
            }
            pieSimpleInfo.setStorageUtilization(numToStr(storageUtilization));
            pieSimpleInfo.setSortStorageUtilization(Double.parseDouble(numToStr(storageUtilization)));
            String unitStorage = "B";
            pieSimpleInfo.setTotalStorage(UnitsUtil.getValueWithUnits(String.valueOf(totalStorage), unitStorage));
            pieSimpleInfo.setFreeStorage(UnitsUtil.getValueWithUnits(String.valueOf(freeStorage), unitStorage));
            pieSimpleInfo.setUsageStorage(UnitsUtil.getValueWithUnits(String.valueOf(totalStorage - freeStorage), unitStorage));
            log.info("虚拟化监测信息:" + pieSimpleInfo);
            return Reply.ok(pieSimpleInfo);
        } catch (Throwable e) {
            log.error("fail to getVirDeviceByPieSimple case{}", e);
            return Reply.fail(500, "获取饼状图数据失败");
        }
    }

    /**
     * 获取VCenter,DataCenter,cluster的cpu,memory,Datastore饼状图信息
     *
     * @param serviceInstance
     * @param param
     * @param virtualizationMonitorList
     * @throws RemoteException
     */
    public void getVirDevicePicInfo(ServiceInstance serviceInstance, QueryVirtualInstanceParam param, List<VirtualizationMonitorInfo> virtualizationMonitorList) throws RemoteException {
        //rootFolder-------根文件夹
        Folder rootFolder = serviceInstance.getRootFolder();
        //inventoryNavigator----文件夹目录
        InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
        //点击Vcenter
        if (VCENTER.getType().equals(param.getVirtualType())) {
            //获取所有的数据中心层信息
            ManagedEntity[] managedEntities = inventoryNavigator.searchManagedEntities(VirtualizationType.DATACNETER.getType());
            if (managedEntities != null && managedEntities.length > 0) {
                for (ManagedEntity managedEntity : managedEntities) {
                    Datacenter datacenter = (Datacenter) managedEntity;
                    //获取datacenter下的主机cpu、memory、datastore信息
                    Folder hostFolder = datacenter.getHostFolder();
                    Datastore[] Datastores = datacenter.getDatastores();
                    if (Datastores != null && Datastores.length > 0) {
                        for (Datastore datastore : Datastores) {
                            getDatastoreInfo(datastore, virtualizationMonitorList, new HashMap<>());
                        }

                    }
                    if (hostFolder != null && hostFolder.getChildEntity() != null && hostFolder.getChildEntity().length > 0) {
                        for (ManagedEntity hostManagedEntity : hostFolder.getChildEntity()) {
                            if (hostManagedEntity instanceof ComputeResource) {
                                ComputeResource computeResource = (ComputeResource) hostManagedEntity;
                                if (computeResource != null && computeResource.getHosts().length > 0) {
                                    HostSystem[] hostsystems = computeResource.getHosts();
                                    for (HostSystem hostsystem : hostsystems) {
                                        getVirHostSystemInfo(hostsystem, virtualizationMonitorList, new HashMap<>());
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        //点击dataCenter
        if (VirtualizationType.DATACNETER.getType().equals(param.getVirtualType())) {
            //根据选择的虚拟化类型，获取对应的监控数据
            ManagedEntity managedEntity = inventoryNavigator.searchManagedEntity(VirtualizationType.DATACNETER.getType(), param.getVirtualName());
            if (managedEntity != null) {
                Datacenter datacenter = (Datacenter) managedEntity;
                Folder hostFolder = datacenter.getHostFolder();
                Datastore[] Datastores = datacenter.getDatastores();
                if (Datastores != null && Datastores.length > 0) {
                    for (Datastore datastore : Datastores) {
                        getDatastoreInfo(datastore, virtualizationMonitorList, new HashMap<>());
                    }

                }
                if (hostFolder != null && hostFolder.getChildEntity() != null && hostFolder.getChildEntity().length > 0) {
                    for (ManagedEntity hostManagedEntity : hostFolder.getChildEntity()) {
                        if (hostManagedEntity instanceof ComputeResource) {
                            ComputeResource computeResource = (ComputeResource) hostManagedEntity;
                            if (computeResource != null && computeResource.getHosts().length > 0) {
                                HostSystem[] hostsystems = computeResource.getHosts();
                                for (HostSystem hostsystem : hostsystems) {
                                    getVirHostSystemInfo(hostsystem, virtualizationMonitorList, new HashMap<>());
                                }
                            }
                        }
                    }
                }
            }
        }
        //点击Cluster
        if (VirtualizationType.CLUSTER.getType().equals(param.getVirtualType())) {
            //根据选择的虚拟化类型，获取对应的监控数据
            ManagedEntity managedEntity = inventoryNavigator.searchManagedEntity(VirtualizationType.CLUSTER.getType(), param.getVirtualName());
            if (managedEntity != null) {
                ClusterComputeResource cluster = (ClusterComputeResource) managedEntity;
                HostSystem[] hostSystems = cluster.getHosts();
                for (HostSystem hostSystem : hostSystems) {
                    getVirHostSystemInfo(hostSystem, virtualizationMonitorList, new HashMap<>());
                }
                Datastore[] Datastores = cluster.getDatastores();
                if (Datastores != null && Datastores.length > 0) {
                    for (Datastore datastore : Datastores) {
                        getDatastoreInfo(datastore, virtualizationMonitorList, new HashMap<>());
                    }
                }
            }
        }
    }

    /**
     * @return PerformanceMap 性能数据map对象
     * @description 根据属性名称、类型、对象、采集间隔获取所有的性能数据
     * @date 2022/09/16
     * @author qzg
     */
    public Map<String, PerformanceManage> getPerfHistoryData(ServiceInstance serviceInstance, QueryVirtualInstanceParam param) {
        Map<String, PerformanceManage> performanceMap = new HashMap<>();
        try {
            Folder rootFolder = serviceInstance.getRootFolder();
            ManagedEntity managedEntity = new InventoryNavigator(rootFolder)
                    .searchManagedEntity(param.getVirtualType(), param.getVirtualName());
            Date date = new Date();
            //默认一个小时前
            Date startTime = new Date(date.getTime() - 60 * 60 * 1000);
            Date endTime = date;
            //默认时间间隔为历史数据查询间隔300S
            int interval = 20;
            if (param.getDateType() != null) {
                Date houreDate = DateUtils.addHours(date, -1);
                Date dayDate = DateUtils.addDays(date, -1);
                Date weekDate = DateUtils.addWeeks(date, -1);
                Date monthDate = DateUtils.addMonths(date, -1);
                switch (param.getDateType()) {//1：hour 2:day 3:week 4:month
                    case 1:
                        startTime = houreDate;
                        interval = 20;
                        break;
                    case 2:
                        startTime = dayDate;
                        interval = 300;
                        break;
                    case 3:
                        startTime = weekDate;
                        interval = 1800;
                        break;
                    case 4:
                        startTime = monthDate;
                        interval = 7200;
                        break;
                    case 5:
                        //自定义时间段
                        startTime = DateUtils.parse(param.getDateStart());
                        endTime = DateUtils.parse(param.getDateEnd());
                        //vSphere API 历史数据查询规则：
                        //一小时之内的数据，间隔为20秒
                        if (startTime.getTime() > houreDate.getTime()) {
                            interval = 20;
                            startTime = DateUtils.addSeconds(startTime, -2);
                        }
                        //一小时以外一天之内的，间隔为300秒（5分钟）
                        if (houreDate.getTime() > startTime.getTime() && startTime.getTime() > dayDate.getTime()) {
                            interval = 300;
                            startTime = DateUtils.addSeconds(startTime, -29);
                        }
                        //一天之外一周之内的，间隔1800秒（30分钟）
                        if (dayDate.getTime() > startTime.getTime() && startTime.getTime() > weekDate.getTime()) {
                            interval = 1800;
                        }
                        //一周之外一个月之内的，间隔7200秒（2小时）
                        if (weekDate.getTime() > startTime.getTime() && startTime.getTime() > monthDate.getTime()) {
                            interval = 7200;
                        }
                        //一个月之外的，间隔86400秒 （一天）
                        if (monthDate.getTime() > startTime.getTime()) {
                            interval = 86400;
                        }
                        break;
                    default:
                        break;
                }
            }
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(startTime);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endTime);

            performanceMap = new HashMap<String, PerformanceManage>();
            if (managedEntity != null) {
                PerformanceManager performanceManager = serviceInstance.getPerformanceManager();
                PerfCounterInfo[] cInfo = performanceManager.getPerfCounter();
                Map<Integer, PerfCounterInfo> counters = new HashMap<Integer, PerfCounterInfo>();
                for (PerfCounterInfo pcInfo : cInfo) {
                    counters.put(new Integer(pcInfo.getKey()), pcInfo);
                }
                PerfMetricId[] listpermeid = performanceManager.queryAvailablePerfMetric(managedEntity, calBegin, calEnd, interval);

                ArrayList<PerfMetricId> mMetrics = new ArrayList<PerfMetricId>();
                if (listpermeid != null) {
                    for (int index = 0; index < listpermeid.length; ++index) {
                        if (counters.containsKey(new Integer(listpermeid[index].getCounterId()))) {
                            mMetrics.add(listpermeid[index]);
                        }
                    }
                }

                PerfQuerySpec qSpec = new PerfQuerySpec();
                qSpec.setEntity(managedEntity.getMOR());
                qSpec.setMetricId(listpermeid);
                qSpec.setStartTime(calBegin);
                qSpec.setEndTime(calEnd);
                qSpec.setIntervalId(interval);
                qSpec.setFormat("normal");

                PerfQuerySpec[] arryQuery = {qSpec};

                PerfEntityMetricBase[] pValues = performanceManager.queryPerf(arryQuery);
                if (pValues == null || pValues.length <= 0) {
                    return performanceMap;
                }
                PerfSampleInfo[] listperfsinfo = ((PerfEntityMetric) pValues[0]).getSampleInfo();
                if (listperfsinfo.length > 0) {
                    for (int i = 0; i < pValues.length; i++) {
                        PerfMetricSeries[] listpems = ((PerfEntityMetric) pValues[i]).getValue();
                        for (int vi = 0; vi < listpems.length; ++vi) {
                            PerfCounterInfo pci = (PerfCounterInfo) counters
                                    .get(new Integer(listpems[vi].getId().getCounterId()));
                            if (pci != null) {
                                for (String Info : param.getGroupInfo()) {
                                    PerformanceManage performanceManage = new PerformanceManage();
                                    performanceManage.setStartTime(listperfsinfo[0].getTimestamp().getTime());
                                    performanceManage
                                            .setEndTime((listperfsinfo[listperfsinfo.length - 1]).getTimestamp().getTime());
                                    boolean isFlag = param.getNameInfos().stream().anyMatch(item -> item.equalsIgnoreCase(pci.getNameInfo().getKey()));
                                    if (isFlag && pci.getGroupInfo().getKey().equalsIgnoreCase(Info)) {
                                        if (listpems[vi] instanceof PerfMetricIntSeries) {
                                            PerfMetricIntSeries val = (PerfMetricIntSeries) listpems[vi];
                                            long[] lislon = val.getValue();
                                            List<PerformanceInfo> asList = new ArrayList<PerformanceInfo>();
                                            for (int y = 0; y < lislon.length; y++) {
                                                PerformanceInfo info = new PerformanceInfo();
                                                info.setValue(lislon[y]);
                                                info.setTime(listperfsinfo[y].getTimestamp().getTime());
                                                asList.add(info);
                                            }
                                            performanceManage.setPerformanceValues(asList);
                                            performanceManage.setUnit(pci.getUnitInfo().getLabel());
                                            performanceMap.put(pci.getNameInfo().getKey(), performanceManage);
                                        }
                                    }

//                                    if (pci.getNameInfo().getKey().equalsIgnoreCase(param.getNameInfo()) && pci.getGroupInfo().getKey().equalsIgnoreCase(Info)) {
//                                        if (listpems[vi] instanceof PerfMetricIntSeries) {
//                                            PerfMetricIntSeries val = (PerfMetricIntSeries) listpems[vi];
//                                            long[] lislon = val.getValue();
//                                            List<PerformanceInfo> asList = new ArrayList<PerformanceInfo>();
//                                            for (int y = 0; y < lislon.length; y++) {
//                                                PerformanceInfo info = new PerformanceInfo();
//                                                info.setValue(lislon[y]);
//                                                info.setTime(listperfsinfo[y].getTimestamp().getTime());
//                                                asList.add(info);
//                                            }
//                                            performanceManage.setPerformanceValues(asList);
//                                            performanceManage.setUnit(pci.getUnitInfo().getLabel());
//                                            performanceMap.put(Info, performanceManage);
//                                        }
//                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            log.error("获取指定时间内性能数据失败 case{}", e);
        }
        return performanceMap;
    }


    /**
     * 获取虚拟化主机列表数据
     */
    public List<VirtualizationMonitorInfo> getVirtualHostList(ServiceInstance serviceInstance,
                                                              QueryVirtualInstanceParam param, Map<String, Integer> modelInstanceIdMap) {
        List<VirtualizationMonitorInfo> virtualizationMonitorList = new ArrayList<>();

        try {
            Folder rootFolder = serviceInstance.getRootFolder();
            InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
            if (Strings.isNullOrEmpty(param.getQueryType())) {
                //设置默认值  默认查询所有主机列表
                param.setQueryType(VirtualizationType.HOSTSYSTEM.getType());
            }
            //树结构没有选择层级，或者选择了VCenter层，查询所有数据中心下的所有主机列表
            if (Strings.isNullOrEmpty(param.getVirtualType()) || VCENTER.getType().equals(param.getVirtualType())) {
                //设置树结构层级  默认查询数据中心
                param.setVirtualType(VirtualizationType.DATACNETER.getType());
                ManagedEntity[] managedEntitys = inventoryNavigator.searchManagedEntities(param.getVirtualType());

                long time1 = System.currentTimeMillis();
                //使用多线程处理多个zabbix服务的情况
                int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
                coreSizePool = (coreSizePool < managedEntitys.length) ? coreSizePool : managedEntitys.length;//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
                ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, managedEntitys.length, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                List<List<VirtualizationMonitorInfo>> listInfo = new ArrayList();
                List<Future<List<VirtualizationMonitorInfo>>> futureList = new ArrayList<>();
                //keys为serverId，value为assetsId
                for (ManagedEntity managedEntity : managedEntitys) {
                    Callable<List<VirtualizationMonitorInfo>> callable = new Callable<List<VirtualizationMonitorInfo>>() {
                        @Override
                        public List<VirtualizationMonitorInfo> call() throws Exception {
                            List<VirtualizationMonitorInfo> list = getVirtualDeviceInfo(managedEntity, param, new ArrayList<>(), modelInstanceIdMap);
                            return list;
                        }
                    };
                    Future<List<VirtualizationMonitorInfo>> submit = executorService.submit(callable);
                    futureList.add(submit);
                }
                ;
                if (futureList.size() > 0) {
                    futureList.forEach(f -> {
                        try {
                            List<VirtualizationMonitorInfo> result = f.get(20, TimeUnit.SECONDS);
                            listInfo.add(result);
                        } catch (Exception e) {
                            log.error("fail to getDataInfoBydeviceName:多线程等待数据返回失败cause:{}", e);
                        }
                    });
                }
                executorService.shutdown();
                log.info("关闭线程池");
                long time2 = System.currentTimeMillis();
                virtualizationMonitorList = listInfo.stream().flatMap(List::stream).collect(Collectors.toList());
                log.info("多线程获取vcenterAPi数据耗时:" + (time2 - time1) + "ms;数据量:" + virtualizationMonitorList.size());
//                if (managedEntitys != null && managedEntitys.length > 0) {
//                    for (ManagedEntity managedEntity : managedEntitys) {
//                        getVirtualDeviceInfo(managedEntity, param, virtualizationMonitorList, modelInstanceIdMap);
//                    }
//                }
            } else {
                ManagedEntity managedEntity = inventoryNavigator.searchManagedEntity(param.getVirtualType(), param.getVirtualName());
                if (managedEntity != null) {
                    getVirtualDeviceInfo(managedEntity, param, virtualizationMonitorList, modelInstanceIdMap);
                }
            }
        } catch (Throwable e) {
            log.error("fail to getVirtualMonitorList case:{}", e);
        }
        return virtualizationMonitorList;
    }

    public List<VirtualizationMonitorInfo> getVirtualDeviceInfo(ManagedEntity managedEntity, QueryVirtualInstanceParam param,
                                                                List<VirtualizationMonitorInfo> virtualizationMonitorList, Map<String, Integer> modelInstanceIdMap) {
        try {
            if (managedEntity != null) {
                if (Strings.isNullOrEmpty(param.getQueryType())) {
                    //设置默认值  默认查询数据中心下的所有主机列表
                    param.setQueryType(VirtualizationType.HOSTSYSTEM.getType());
                }
                //查询主机数据
                if (VirtualizationType.HOSTSYSTEM.getType().equals(param.getQueryType())) {
                    //左侧树点击的是数据中心层
                    if (VirtualizationType.DATACNETER.getType().equals(param.getVirtualType())) {
                        Datacenter datacenter = (Datacenter) managedEntity;
                        ManagedEntity[] hosts = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());
                        log.info("左侧树点击的是数据中心层hosts数量::" + hosts.length);
                        for (ManagedEntity host : hosts) {
                            HostSystem hostSystem = (HostSystem) host;
                            getVirHostSystemInfo(hostSystem, virtualizationMonitorList, modelInstanceIdMap);
                        }
                    }
                    //左侧树点击的是集群层
                    if (VirtualizationType.CLUSTER.getType().equals(param.getVirtualType())) {
                        ClusterComputeResource cluster = (ClusterComputeResource) managedEntity;
                        HostSystem[] hostSystems = cluster.getHosts();
                        if (hostSystems != null && hostSystems.length > 0) {
                            for (HostSystem hostSystem : hostSystems) {
                                getVirHostSystemInfo(hostSystem, virtualizationMonitorList, modelInstanceIdMap);
                            }
                        }
                    }
                    //左侧树点击的是主机
                    if (VirtualizationType.HOSTSYSTEM.getType().equals(param.getVirtualType())) {
                        HostSystem hostSystem = (HostSystem) managedEntity;
                        if (hostSystem != null) {
                            getVirHostSystemInfo(hostSystem, virtualizationMonitorList, modelInstanceIdMap);
                        }
                    }
                }
                //查询虚拟机列表数据
                if (VirtualizationType.VIRTUALMACHINE.getType().equals(param.getQueryType())) {
                    //左侧树点击的是数据中心层
                    if (VirtualizationType.DATACNETER.getType().equals(param.getVirtualType())) {
                        Datacenter datacenter = (Datacenter) managedEntity;
                        ManagedEntity[] managedEntities = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                        if (managedEntities != null && managedEntities.length > 0) {
                            for (ManagedEntity virEntity : managedEntities) {
                                VirtualMachine vm = (VirtualMachine) virEntity;
                                if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                                    getVirtualMachineInfo(vm, virtualizationMonitorList, modelInstanceIdMap);
                                }
                            }
                        }
                    }
                    //左侧树点击的是集群层
                    if (VirtualizationType.CLUSTER.getType().equals(param.getVirtualType())) {
                        ClusterComputeResource cluster = (ClusterComputeResource) managedEntity;
                        ManagedEntity[] managedEntities = new InventoryNavigator(cluster).searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                        if (managedEntities != null && managedEntities.length > 0) {
                            for (ManagedEntity virEntity : managedEntities) {
                                VirtualMachine vm = (VirtualMachine) virEntity;
                                if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                                    getVirtualMachineInfo(vm, virtualizationMonitorList, modelInstanceIdMap);
                                }
                            }
                        }
                    }
                    //左侧树点击的是宿主机层
                    if (VirtualizationType.HOSTSYSTEM.getType().equals(param.getVirtualType())) {
                        HostSystem hostSystem = (HostSystem) managedEntity;
                        VirtualMachine[] vms = hostSystem.getVms();
                        if (vms != null && vms.length > 0) {
                            for (VirtualMachine vm : vms) {
                                if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                                    getVirtualMachineInfo(vm, virtualizationMonitorList, modelInstanceIdMap);
                                }
                            }
                        }
                    }
                    //点击单个虚拟机
                    if (VirtualizationType.VIRTUALMACHINE.getType().equals(param.getVirtualType())) {
                        VirtualMachine vm = (VirtualMachine) managedEntity;
                        if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                            getVirtualMachineInfo(vm, virtualizationMonitorList, modelInstanceIdMap);
                        }
                    }

                }
                //查数据存储列表数据
                if (VirtualizationType.DATASTORE.getType().equals(param.getQueryType())) {
                    if (VirtualizationType.DATACNETER.getType().equals(param.getVirtualType())) {
                        Datacenter datacenter = (Datacenter) managedEntity;
                        Datastore[] datastores = datacenter.getDatastores();
                        if (datastores != null && datastores.length > 0) {
                            for (Datastore datastore : datastores) {
                                getDatastoreInfo(datastore, virtualizationMonitorList, modelInstanceIdMap);
                            }
                        }
                    }
                    if (VirtualizationType.CLUSTER.getType().equals(param.getVirtualType())) {
                        ClusterComputeResource cluster = (ClusterComputeResource) managedEntity;
                        Datastore[] datastores = cluster.getDatastores();
                        if (datastores != null && datastores.length > 0) {
                            for (Datastore datastore : datastores) {
                                getDatastoreInfo(datastore, virtualizationMonitorList, modelInstanceIdMap);
                            }
                        }
                    }
                    if (VirtualizationType.HOSTSYSTEM.getType().equals(param.getVirtualType())) {
                        HostSystem hostSystem = (HostSystem) managedEntity;
                        if (hostSystem.getDatastores() != null && hostSystem.getDatastores().length > 0) {
                            for (Datastore datastore : hostSystem.getDatastores()) {
                                getDatastoreInfo(datastore, virtualizationMonitorList, modelInstanceIdMap);
                            }
                        }
                    }
                    if (VirtualizationType.DATASTORE.getType().equals(param.getVirtualType())
                            && managedEntity instanceof Datastore) {
                        Datastore datastore = (Datastore) managedEntity;
                        getDatastoreInfo(datastore, virtualizationMonitorList, modelInstanceIdMap);

                    }
                }
            }
        } catch (Throwable e) {
            log.error("fail to getVirtualMonitorList case:{}", e);
        }
        return virtualizationMonitorList;
    }

    /**
     * 获取主机数据 方法摘出
     *
     * @param hostSystem
     * @param virtualizationMonitorList
     * @throws RemoteException
     */
    public void getVirHostSystemInfo(HostSystem hostSystem, List<VirtualizationMonitorInfo> virtualizationMonitorList,
                                     Map<String, Integer> modelInstanceIdMap) throws RemoteException {
        VirtualizationMonitorInfo virtualizationMonitorInfo = new VirtualizationMonitorInfo();
        virtualizationMonitorInfo.setInstanceName(hostSystem.getName());
        virtualizationMonitorInfo.setId(hostSystem.getMOR().getVal());
        Integer instanceId = null;
        if (modelInstanceIdMap != null) {
            instanceId = modelInstanceIdMap.get(hostSystem.getMOR().getVal());
            virtualizationMonitorInfo.setInstanceId(instanceId);
        }
        log.info("getVirHostSystemInfo" + hostSystem);
        HostListSummary hsummary = hostSystem.getSummary();
        //正常运行时间
        String upTime = "";
        if (hsummary != null && hsummary.getQuickStats() != null && hsummary.getQuickStats().getUptime() != null) {
            upTime = SeverityUtils.getLastTime(Long.valueOf(hsummary.getQuickStats().getUptime()));
        }
        virtualizationMonitorInfo.setUpTime(upTime);
        //主机状态
        if (hostSystem.getOverallStatus() != null) {
            String hostStatus = hostSystem.getOverallStatus().name();
            virtualizationMonitorInfo.setStatus(hostStatus);
        }
        //主机IP地址
        String hostIp = "";
        HostConfigInfo configInfo = hostSystem.getConfig();
        if (configInfo != null) {
            HostNetworkInfo networkInfo = configInfo.getNetwork();
            if (networkInfo != null) {
                HostVirtualNic[] nics = networkInfo.getVnic();
                if (nics != null && nics.length > 0 && nics[0] != null) {
                    HostVirtualNic nic = nics[0];
                    hostIp = nic.getSpec().getIp().getIpAddress();
                }
            }
        }
        virtualizationMonitorInfo.setIp(hostIp);
        //厂商
        String vendor = "";
        //型号
        String model = "";
        if (hostSystem.getHardware() != null && hostSystem.getHardware().getSystemInfo() != null) {
            vendor = hostSystem.getHardware().getSystemInfo().getVendor();
            model = hostSystem.getHardware().getSystemInfo().getModel();
        }
        virtualizationMonitorInfo.setVendor(vendor);
        virtualizationMonitorInfo.setModel(model);
        virtualizationMonitorInfo.setVirtualType(VirtualizationType.HOSTSYSTEM.getType());
        if (hostSystem.getHardware() != null && hostSystem.getHardware().getMemorySize() != 0l) {
            //内存
            Long totalMemory = hostSystem.getHardware().getMemorySize(); //内存总容量(B)
            Long usageMemory = 0l;
            if (hsummary != null && hsummary.getQuickStats() != null && hsummary.getQuickStats().getOverallMemoryUsage() != null) {
                usageMemory = (long) hsummary.getQuickStats().getOverallMemoryUsage() * 1024 * 1024; //内存使用容量(B)
            }
            Long freeMemory = (totalMemory - (usageMemory));//可用内存
            String memoryUtilization = "0";
            if (totalMemory.intValue() != 0) {
                memoryUtilization = String.valueOf(Double.valueOf(usageMemory) / Double.valueOf(totalMemory) * 100);//内存使用率
            }
            virtualizationMonitorInfo.setSortTotalMemory(totalMemory);
            virtualizationMonitorInfo.setSortUsageMemory(usageMemory);
            virtualizationMonitorInfo.setSortFreeMemory(freeMemory);
            virtualizationMonitorInfo.setMemoryUtilization(numToStr(memoryUtilization));
            virtualizationMonitorInfo.setSortMemoryUtilization(Double.parseDouble(numToStr(memoryUtilization)));
            //单位转换后的
            String unitMemory = "B";
            virtualizationMonitorInfo.setTotalMemory(UnitsUtil.getValueWithUnits(totalMemory.toString(), unitMemory));
            virtualizationMonitorInfo.setUsageMemory(UnitsUtil.getValueWithUnits(usageMemory.toString(), unitMemory));
            virtualizationMonitorInfo.setFreeMemory(UnitsUtil.getValueWithUnits(freeMemory.toString(), unitMemory));
        }
        //cpu
        //单位转换后的
        String unitCPU = "MHz";
        if (hsummary != null && hsummary.getHardware() != null) {
            Integer totalCPU = hsummary.getHardware().getNumCpuCores() * hsummary.getHardware().getCpuMhz(); //CPU总容量(MHz)
            Integer usageCPU = 0;
            if (hsummary.getQuickStats() != null && hsummary.getQuickStats().getOverallCpuUsage() != null) {
                usageCPU = hsummary.getQuickStats().getOverallCpuUsage(); //CPU使用容量(MHz)
            }
            Integer freeCPU = totalCPU - usageCPU;//可用CPU
            String cpuUtilization = "";
            if (totalCPU.intValue() == 0) {
                cpuUtilization = "0";//CPU使用率
            } else {
                cpuUtilization = String.valueOf(Double.valueOf(usageCPU) / Double.valueOf(totalCPU) * 100);//CPU使用率
            }
            virtualizationMonitorInfo.setSortTotalCPU(totalCPU);
            virtualizationMonitorInfo.setSortUsageCPU(usageCPU);
            virtualizationMonitorInfo.setSortFreeCPU(freeCPU);
            virtualizationMonitorInfo.setTotalCPU(UnitsUtil.getValueWithUnits(totalCPU.toString(), unitCPU));
            virtualizationMonitorInfo.setUsageCPU(UnitsUtil.getValueWithUnits(usageCPU.toString(), unitCPU));
            virtualizationMonitorInfo.setFreeCPU(UnitsUtil.getValueWithUnits(freeCPU.toString(), unitCPU));
            virtualizationMonitorInfo.setCpuUtilization(numToStr(cpuUtilization));
            virtualizationMonitorInfo.setSortCpuUtilization(Double.parseDouble(numToStr(cpuUtilization)));
        }
        virtualizationMonitorList.add(virtualizationMonitorInfo);
    }


    /**
     * 获取虚拟机数据 方法摘出
     *
     * @param vm
     * @param virtualizationMonitorList
     */
    public void getVirtualMachineInfo(VirtualMachine vm, List<VirtualizationMonitorInfo> virtualizationMonitorList,
                                      Map<String, Integer> modelInstanceIdMap) {
        VirtualizationMonitorInfo virtualizationMonitorInfo = new VirtualizationMonitorInfo();
        virtualizationMonitorInfo.setInstanceName(vm.getName());
        virtualizationMonitorInfo.setId(vm.getMOR().getVal());
        Integer instanceId = null;
        if (modelInstanceIdMap != null) {
            instanceId = modelInstanceIdMap.get(vm.getMOR().getVal());
            virtualizationMonitorInfo.setInstanceId(instanceId);
        }
        if (vm.getRuntime() != null && vm.getRuntime().getHost() != null) {
            virtualizationMonitorInfo.setPId(vm.getRuntime().getHost().getVal());
        }
        VirtualMachineSummary summary = vm.getSummary();
        if (summary != null) {
            if (summary.getRuntime() != null && summary.getRuntime().getPowerState() != null) {
                virtualizationMonitorInfo.setStatus(summary.getRuntime().getPowerState().name());
            }

            VirtualMachineStorageSummary storage = summary.getStorage();
            // ######## 存储统计 ##########//
            String unitStorage = "B";
            if (storage != null) {
                //已用空间
                Long usageStorage = storage.getCommitted();
                //剩余空间
                Long freeStorage = storage.getUncommitted();
                if (usageStorage != null && freeStorage != null) {
                    Long totalStorage = (usageStorage + freeStorage);
                    String storageUtilization = "0";
                    if (totalStorage.intValue() != 0) {
                        storageUtilization = String.valueOf(Double.valueOf(usageStorage) / Double.valueOf(totalStorage) * 100);
                    }
                    virtualizationMonitorInfo.setSortFreeStorage(freeStorage);
                    virtualizationMonitorInfo.setSortUsageStorage(usageStorage);
                    virtualizationMonitorInfo.setSortTotalStorage(totalStorage);
                    virtualizationMonitorInfo.setStorageUtilization(numToStr(storageUtilization));
                    virtualizationMonitorInfo.setSortStorageUtilization(Double.parseDouble(numToStr(storageUtilization)));
                    virtualizationMonitorInfo.setFreeStorage(UnitsUtil.getValueWithUnits(freeStorage.toString(), unitStorage));
                    virtualizationMonitorInfo.setUsageStorage(UnitsUtil.getValueWithUnits(usageStorage.toString(), unitStorage));
                    virtualizationMonitorInfo.setTotalStorage(UnitsUtil.getValueWithUnits(totalStorage.toString(), unitStorage));
                }
            }

            // ######## CPU统计 ##########//
            VirtualMachineQuickStats quickStats = summary.getQuickStats();
            //已使用CPU   除1000 为GHZ
            String unitCPU = "MHZ";
            if (summary.getRuntime() != null && summary.getRuntime().getMaxCpuUsage() != null && summary.getRuntime().getMaxCpuUsage().intValue() != 0) {
                //总CPU  14400 = 6 * 2400
                Integer totalCPU = summary.getRuntime().getMaxCpuUsage();
                Integer usageCPU = 0;
                if (quickStats != null) {
                    usageCPU = quickStats.getOverallCpuUsage();
                }
                Integer freeCPU = totalCPU - usageCPU;
                String cpuUtilization = "0";
                if (totalCPU.intValue() != 0) {
                    cpuUtilization = String.valueOf(Double.valueOf(usageCPU) / Double.valueOf(totalCPU) * 100);
                }
                virtualizationMonitorInfo.setSortTotalCPU(totalCPU);
                virtualizationMonitorInfo.setSortUsageCPU(usageCPU);
                virtualizationMonitorInfo.setSortFreeCPU(freeCPU);
                virtualizationMonitorInfo.setTotalCPU(UnitsUtil.getValueWithUnits(totalCPU.toString(), unitCPU));
                virtualizationMonitorInfo.setUsageCPU(UnitsUtil.getValueWithUnits(usageCPU.toString(), unitCPU));
                virtualizationMonitorInfo.setFreeCPU(UnitsUtil.getValueWithUnits(freeCPU.toString(), unitCPU));
                virtualizationMonitorInfo.setCpuUtilization(numToStr(cpuUtilization));
                virtualizationMonitorInfo.setSortCpuUtilization(Double.parseDouble(numToStr(cpuUtilization)));
            }

            VirtualMachineConfigSummary config = summary.getConfig();
            if (config != null) {
                // ######## 内存统计 ##########//
                String unitMemory = "B";

                //总内存  单位MB转为B
                if (config.getMemorySizeMB() != null && config.getMemorySizeMB().intValue() != 0) {
                    Long totalMemory = (long) config.getMemorySizeMB() * 1024 * 1024;
                    //已用内存
                    Long usageMemory = 0l;
                    if (quickStats != null) {
                        usageMemory = (long) quickStats.getHostMemoryUsage();
                    }
                    Long freeMemory = totalMemory - usageMemory;
                    virtualizationMonitorInfo.setSortTotalMemory(totalMemory);
                    virtualizationMonitorInfo.setSortUsageMemory(usageMemory);
                    virtualizationMonitorInfo.setSortFreeMemory(freeMemory);

                    virtualizationMonitorInfo.setTotalMemory(UnitsUtil.getValueWithUnits(totalMemory.toString(), unitMemory));
                    virtualizationMonitorInfo.setUsageMemory(UnitsUtil.getValueWithUnits(usageMemory.toString(), unitMemory));
                    virtualizationMonitorInfo.setFreeMemory(UnitsUtil.getValueWithUnits(freeMemory.toString(), unitMemory));
                    String memoryUtilization = "0";
                    if (totalMemory.intValue() != 0) {
                        memoryUtilization = String.valueOf(Double.valueOf(usageMemory) / Double.valueOf(totalMemory) * 100);
                    }
                    virtualizationMonitorInfo.setMemoryUtilization(numToStr(memoryUtilization));
                    virtualizationMonitorInfo.setSortMemoryUtilization(Double.parseDouble(numToStr(memoryUtilization)));
                }
            }
        }
        virtualizationMonitorInfo.setVirtualType(VirtualizationType.VIRTUALMACHINE.getType());
        String VMIp = "";
        if (vm.getGuest() != null) {
            GuestInfo guestInfo = vm.getGuest();

            List<VirtualizationIpAdressInfo> ipList = new ArrayList<>();
            if (guestInfo.getNet() != null) {
                GuestNicInfo[] nicInfos = guestInfo.getNet();
                for (GuestNicInfo nicInfo : nicInfos) {
                    if (nicInfo != null && nicInfo.getIpAddress() != null && nicInfo.getIpAddress().length > 0) {
                        String[] ipAddresses = nicInfo.getIpAddress();
                        for (String ipAddress : ipAddresses) {
                            //判断是否是IPV6
                            if (!Strings.isNullOrEmpty(ipAddress)) {
                                boolean isIpV4 = IPAddressUtil.isIPv4LiteralAddress(ipAddress);
                                if (isIpV4) {
                                    VirtualizationIpAdressInfo ipParam = new VirtualizationIpAdressInfo();
                                    ipParam.setIp(ipAddress);
                                    ipList.add(ipParam);
                                }
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(ipList)) {
                virtualizationMonitorInfo.setIpParam(ipList);
            } else {
                virtualizationMonitorInfo.setIpParam(new ArrayList<>());
            }
            if (guestInfo != null && !Strings.isNullOrEmpty(guestInfo.getIpAddress())) {
                VMIp = guestInfo.getIpAddress();
            }
            if (guestInfo != null && !Strings.isNullOrEmpty(guestInfo.getGuestFullName())) {
                virtualizationMonitorInfo.setOperatingSystem(guestInfo.getGuestFullName());
            }
        }
        virtualizationMonitorInfo.setIp(VMIp);
        virtualizationMonitorList.add(virtualizationMonitorInfo);

        if (null == instanceId) {
            log.info("getVirtualMachineInfo find not instanceId {}", virtualizationMonitorInfo.toDebugString());
        }
    }

    /**
     * 获取数据存储数据 方法摘出
     *
     * @param datastore
     * @param virtualizationMonitorList
     */
    public void getDatastoreInfo(Datastore datastore, List<VirtualizationMonitorInfo> virtualizationMonitorList,
                                 Map<String, Integer> modelInstanceIdMap) {
        //获取数据存储监控列表
        VirtualizationMonitorInfo virtualizationMonitorInfo = new VirtualizationMonitorInfo();
        virtualizationMonitorInfo.setInstanceName(datastore.getName());
        virtualizationMonitorInfo.setId(datastore.getMOR().getVal());

        Integer instanceId = null;
        if (modelInstanceIdMap != null) {
            instanceId = modelInstanceIdMap.get(datastore.getMOR().getVal());
            virtualizationMonitorInfo.setInstanceId(instanceId);
        }
        String unitStorage = "B";
        //总存储
        Long totalStorage = datastore.getSummary().getCapacity();
        if (totalStorage != null && totalStorage.intValue() != 0) {
            virtualizationMonitorInfo.setSortTotalStorage(totalStorage);
            //可用存储
            Long freeStorage = datastore.getSummary().getFreeSpace();
            virtualizationMonitorInfo.setSortFreeStorage(freeStorage);
            Long usageStorag = totalStorage - freeStorage;
            virtualizationMonitorInfo.setSortUsageStorage(usageStorag);
            String storageUtilization = "";
            if (totalStorage != 0) {
                storageUtilization = String.valueOf(Double.valueOf(usageStorag) / Double.valueOf(totalStorage) * 100);
            }
            virtualizationMonitorInfo.setStorageUtilization(numToStr(storageUtilization));
            virtualizationMonitorInfo.setSortStorageUtilization(Double.parseDouble(numToStr(storageUtilization)));
            virtualizationMonitorInfo.setUsageStorage(UnitsUtil.getValueWithUnits(usageStorag.toString(), unitStorage));
            virtualizationMonitorInfo.setTotalStorage(UnitsUtil.getValueWithUnits(totalStorage.toString(), unitStorage));
            virtualizationMonitorInfo.setFreeStorage(UnitsUtil.getValueWithUnits(freeStorage.toString(), unitStorage));
            virtualizationMonitorInfo.setVirtualType(VirtualizationType.DATASTORE.getType());
            virtualizationMonitorList.add(virtualizationMonitorInfo);
        }
    }


    /**
     * @param serviceInstance
     * @param type
     * @return
     * @throws Exception
     */
    public VirtualizationBaseInfo getVirtualDeviceBaseInfo(ServiceInstance serviceInstance, String type, String name) throws Exception {

        VirtualizationBaseInfo virtualizationBaseInfo = new VirtualizationBaseInfo();
        //rootFolder-------根文件夹
        Folder rootFolder = serviceInstance.getRootFolder();
        //inventoryNavigator----文件夹目录
        InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
        Integer virNum = 0;

        //获取虚拟机层
        if (VirtualizationType.VIRTUALMACHINE.getType().equals(type)) {
            ManagedEntity virtualManagedEntity = inventoryNavigator.searchManagedEntity(VirtualizationType.VIRTUALMACHINE.getType(), name);
            if (virtualManagedEntity != null) {
                VirtualMachine virtualMachine = (VirtualMachine) virtualManagedEntity;
                if (!virtualMachine.getConfig().isTemplate()) {
                    virtualizationBaseInfo = new VirtualizationBaseInfo();
                    virtualizationBaseInfo.setInstanceName(virtualMachine.getName());
                    GuestInfo guestInfo = virtualMachine.getGuest();
                    String VMIp = guestInfo.getIpAddress();
                    virtualizationBaseInfo.setIp(VMIp);
                    virNum++;
                    VirtualMachineSummary summary = virtualMachine.getSummary();
                    if (summary != null) {
                        if (summary.getConfig() != null) {
                            //cpu核数
                            virtualizationBaseInfo.setCpuNum(summary.getConfig().getNumCpu());
                        }
                        if (summary.getRuntime() != null && summary.getRuntime().getPowerState() != null) {
                            //虚拟机状态
                            virtualizationBaseInfo.setStatus(summary.getRuntime().getPowerState().name());
                        }
                        if (summary.getQuickStats() != null && summary.getQuickStats().getUptimeSeconds() != null) {
                            //虚拟机运行时间
                            virtualizationBaseInfo.setUpTime(SeverityUtils.getLastTime(Long.valueOf(summary.getQuickStats().getUptimeSeconds())));

                        }
                    }
                    log.info("virtualMachine:" + virtualizationBaseInfo);
                }
            }
        }

        //VCenter层
        if (Strings.isNullOrEmpty(type) || VCENTER.getType().equals(type)) {
            //获取数据中心层
            ManagedEntity[] dataCenterManagedEntities = inventoryNavigator.searchManagedEntities(VirtualizationType.DATACNETER.getType());
            log.info("VCenter层" + dataCenterManagedEntities);
            if (dataCenterManagedEntities != null && dataCenterManagedEntities.length > 0) {
                int storesNum = 0;
                int hostNum = 0;
                int clusterNum = 0;
                int vmNum = 0;
                int dataCenterNum = 0;
                virtualizationBaseInfo = new VirtualizationBaseInfo();
                for (ManagedEntity managedEntity : dataCenterManagedEntities) {
                    Datacenter datacenter = (Datacenter) managedEntity;
                    int virNumByDataCenter = 0;
                    ManagedEntity[] vms = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                    if (vms != null && vms.length > 0) {
                        for (ManagedEntity virEntity : vms) {
                            VirtualMachine vm = (VirtualMachine) virEntity;
                            if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                                virNumByDataCenter++;
                            }
                        }
                    }
                    ManagedEntity[] hosts = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());

                    log.info("当前" + datacenter.getName() + "VCenter主机数量:" + hosts.length);
                    dataCenterNum++;
                    if (datacenter.getParent() != null && datacenter.getParent().getServerConnection() != null && datacenter.getParent().getServerConnection().getServiceInstance() != null) {
                        //获取VCenter设备的信息
                        AboutInfo aboutInfo = datacenter.getParent().getServerConnection().getServiceInstance().getAboutInfo();
                        if (aboutInfo != null) {
                            virtualizationBaseInfo.setVersion(aboutInfo.getVersion());
                            virtualizationBaseInfo.setFullName(aboutInfo.getFullName());
                            virtualizationBaseInfo.setVendor(aboutInfo.getVendor());
                        }
                    }
                    //获取集群信息
                    ManagedEntity[] clusterManagedEntities = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.CLUSTER.getType());
                    if (clusterManagedEntities != null && clusterManagedEntities.length > 0) {
                        clusterNum += clusterManagedEntities.length;
                    }

                    //没有集群的按照dataCenter获取数据
                    vmNum += virNumByDataCenter;
                    storesNum += datacenter.getDatastores().length;
                    hostNum += hosts.length;

                }
                virtualizationBaseInfo.setDataStoreNum(storesNum);
                virtualizationBaseInfo.setHostNum(hostNum);
                virtualizationBaseInfo.setVmNum(vmNum);
                virtualizationBaseInfo.setDataCenterNum(dataCenterNum);
                virtualizationBaseInfo.setClusterNum(clusterNum);
                log.info("VCenter:" + virtualizationBaseInfo);
            }
        }


        //获取数据中心层
        if (VirtualizationType.DATACNETER.getType().equals(type)) {
            ManagedEntity dataCenterManagedEntity = inventoryNavigator.searchManagedEntity(VirtualizationType.DATACNETER.getType(), name);
            if (dataCenterManagedEntity != null) {
                virtualizationBaseInfo = new VirtualizationBaseInfo();
                Datacenter datacenter = (Datacenter) dataCenterManagedEntity;
                virtualizationBaseInfo.setInstanceName(datacenter.getName());
                virtualizationBaseInfo.setNetNum(datacenter.getNetworks().length);
                virtualizationBaseInfo.setDataStoreNum(datacenter.getDatastores().length);
                virtualizationBaseInfo.setHostNum(datacenter.getHostFolder().getChildEntity().length);
                int clusterNum = 0;
                int storesNum = 0;
                int hostNum = 0;
                int vmNum = 0;
                int dataCenterNum = 0;
                int virNumByDataCenter = 0;
                ManagedEntity[] vms = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                if (vms != null && vms.length > 0) {
                    for (ManagedEntity virEntity : vms) {
                        VirtualMachine vm = (VirtualMachine) virEntity;
                        if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                            virNumByDataCenter++;
                        }
                    }
                }

                //获取集群信息  如果有集群数据，使用集群获取
                ManagedEntity[] clusterManagedEntities = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.CLUSTER.getType());
                if (clusterManagedEntities != null && clusterManagedEntities.length > 0) {
                    clusterNum = clusterManagedEntities.length;
                }
                ManagedEntity[] hosts = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());


                //没有集群的按照dataCenter获取数据
                vmNum = virNumByDataCenter;
                storesNum = datacenter.getDatastores().length;
                hostNum = hosts.length;
                virtualizationBaseInfo.setDataStoreNum(storesNum);
                virtualizationBaseInfo.setHostNum(hostNum);
                virtualizationBaseInfo.setVmNum(vmNum);
                virtualizationBaseInfo.setDataCenterNum(dataCenterNum);
                virtualizationBaseInfo.setClusterNum(clusterNum);
                log.info("VCenter:" + datacenter.getName() + "hostNum" + hostNum);
                log.info("VCenter:" + virtualizationBaseInfo);
            }
        }
        //获取集群层
        if (VirtualizationType.CLUSTER.getType().equals(type)) {
            ManagedEntity clusterManagedEntity = inventoryNavigator.searchManagedEntity(VirtualizationType.CLUSTER.getType(), name);
            if (clusterManagedEntity != null) {
                int storesNum = 0;
                int hostNum = 0;
                int virNumByCluster = 0;


                virtualizationBaseInfo = new VirtualizationBaseInfo();
                ClusterComputeResource cluster = (ClusterComputeResource) clusterManagedEntity;
                virtualizationBaseInfo.setInstanceName(cluster.getName());
                virtualizationBaseInfo.setNetNum(cluster.getNetworks().length);
                //获取集群下的主机数量
                ManagedEntity[] hostSystemEntities = new InventoryNavigator(cluster).searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());
                if (hostSystemEntities != null) {
                    hostNum = hostSystemEntities.length;
                }
                //获取集群下的虚拟机数量
                ManagedEntity[] virtualEntities = new InventoryNavigator(cluster).searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                if (virtualEntities != null && virtualEntities.length > 0) {
                    for (ManagedEntity managedEntity : virtualEntities) {
                        if (managedEntity instanceof VirtualMachine) {
                            VirtualMachine vm = (VirtualMachine) managedEntity;
                            if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                                virNumByCluster++;
                            }
                        }
                    }
                }
                //获取集群下的数据存储数量
                ManagedEntity[] datastore = new InventoryNavigator(cluster).searchManagedEntities(VirtualizationType.DATASTORE.getType());
                if (datastore != null) {
                    storesNum = datastore.length;
                }
                virtualizationBaseInfo.setHostNum(hostNum);
                virtualizationBaseInfo.setDataStoreNum(cluster.getDatastores().length);
                virtualizationBaseInfo.setVmNum(virNumByCluster);
                log.info("cluster:" + cluster.getName() + ";hostNum:" + hostNum + ";DataStoreNum:" + storesNum);
                log.info("cluster:" + virtualizationBaseInfo);
            }
        }
        //获取宿主机层
        if (VirtualizationType.HOSTSYSTEM.getType().equals(type)) {
            ManagedEntity hostSystemEntity = inventoryNavigator.searchManagedEntity(VirtualizationType.HOSTSYSTEM.getType(), name);
            if (hostSystemEntity != null) {
                virtualizationBaseInfo = new VirtualizationBaseInfo();
                HostSystem hostSystem = (HostSystem) hostSystemEntity;
                virtualizationBaseInfo.setInstanceName(hostSystem.getName());
                virtualizationBaseInfo.setNetNum(hostSystem.getNetworks().length);
                int virNumByHostSystem = 0;
                if (hostSystem.getVms() != null && hostSystem.getVms().length > 0) {
                    for (VirtualMachine vm : hostSystem.getVms()) {
                        if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                            virNumByHostSystem++;
                        }
                    }
                }
                virtualizationBaseInfo.setVmNum(virNumByHostSystem);
                HostListSummary hsummary = hostSystem.getSummary();
                //正常运行时间
                String upTime = "";
                if (hsummary != null && hsummary.getQuickStats() != null && hsummary.getQuickStats().getUptime() != null) {
                    upTime = SeverityUtils.getLastTime(Long.valueOf(hsummary.getQuickStats().getUptime()));
                }
                virtualizationBaseInfo.setUpTime(upTime);
                //主机状态
                String hostStatus = hostSystem.getOverallStatus() != null ? hostSystem.getOverallStatus().name() : "";
                virtualizationBaseInfo.setStatus(hostStatus);
                //主机IP地址
                String hostIp = "";
                HostConfigInfo configInfo = hostSystem.getConfig();
                if (configInfo != null) {
                    HostNetworkInfo networkInfo = configInfo.getNetwork();
                    if (networkInfo != null) {
                        HostVirtualNic[] nics = networkInfo.getVnic();
                        if (nics != null && nics.length > 0 && nics[0] != null) {
                            HostVirtualNic nic = nics[0];
                            hostIp = nic.getSpec().getIp().getIpAddress();
                        }
                    }
                }
                virtualizationBaseInfo.setIp(hostIp);

                if (hostSystem.getHardware() != null && hostSystem.getHardware().getSystemInfo() != null) {
                    //厂商
                    String vendor = hostSystem.getHardware().getSystemInfo().getVendor();
                    virtualizationBaseInfo.setVendor(vendor);
                    //型号
                    String model = hostSystem.getHardware().getSystemInfo().getModel();
                    virtualizationBaseInfo.setModel(model);
                }

                log.info("hostSystem:" + hostSystem.getName());
                log.info("hostSystem:" + virtualizationBaseInfo);
            }

        }

        return virtualizationBaseInfo;
    }


    /**
     * 通过Api获取虚拟化设备数据
     *
     * @param serviceInstance
     * @param pId
     * @return
     * @throws Exception
     */
    public List<VirtualizationDataInfo> getVirtualDataByApi(ServiceInstance serviceInstance, String pId, Integer monitorServerId, String monitorServerName) throws Exception {

        List<VirtualizationDataInfo> listInfo = new ArrayList<>();
        VirtualizationDataInfo virtualizationDataInfo;
        //rootFolder-------根文件夹
        Folder rootFolder = serviceInstance.getRootFolder();
        //inventoryNavigator----文件夹目录
        InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
        //获取数据中心层
        ManagedEntity[] dataCenterManagedEntities = inventoryNavigator.searchManagedEntities(VirtualizationType.DATACNETER.getType());
        Datacenter datacenter = null;
        log.info("数据同步获取dataCenterManagedEntities::" + dataCenterManagedEntities.length);
        if (dataCenterManagedEntities != null && dataCenterManagedEntities.length > 0) {
            for (ManagedEntity managedEntity : dataCenterManagedEntities) {
                virtualizationDataInfo = new VirtualizationDataInfo();
                datacenter = (Datacenter) managedEntity;
                virtualizationDataInfo.setId(datacenter.getMOR().getVal());
                virtualizationDataInfo.setInstanceName(datacenter.getName());
                virtualizationDataInfo.setType(VirtualizationType.DATACNETER.getType());
                virtualizationDataInfo.setPId(pId);
                virtualizationDataInfo.setMonitorServerId(monitorServerId);
                virtualizationDataInfo.setMonitorServerName(monitorServerName);
                listInfo.add(virtualizationDataInfo);
                log.info("datacenter-name:" + datacenter);
                //获取集群层
                ManagedEntity[] clusterEntities = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.CLUSTER.getType());
                if (clusterEntities != null && clusterEntities.length > 0) {
                    for (ManagedEntity clusterEntity : clusterEntities) {
                        ClusterComputeResource cluster = (ClusterComputeResource) clusterEntity;
                        virtualizationDataInfo = new VirtualizationDataInfo();
                        virtualizationDataInfo.setId(cluster.getMOR().getVal());
                        virtualizationDataInfo.setInstanceName(cluster.getName());
                        virtualizationDataInfo.setType(VirtualizationType.CLUSTER.getType());
                        virtualizationDataInfo.setPId(datacenter.getMOR().getVal());
                        virtualizationDataInfo.setMonitorServerId(monitorServerId);
                        virtualizationDataInfo.setMonitorServerName(monitorServerName);
                        listInfo.add(virtualizationDataInfo);

                        //集群关联服务器
                        ManagedEntity[] hostSystemsList = new InventoryNavigator(cluster).searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());
                        if (hostSystemsList != null && hostSystemsList.length > 0) {
                            for (ManagedEntity hostdEntity : hostSystemsList) {
                                virtualizationDataInfo = new VirtualizationDataInfo();
                                HostSystem system = (HostSystem) hostdEntity;
                                virtualizationDataInfo.setId(system.getMOR().getVal());
                                virtualizationDataInfo.setInstanceName(system.getName());
                                virtualizationDataInfo.setType(VirtualizationType.HOSTSYSTEM.getType());
                                virtualizationDataInfo.setPId(cluster.getMOR().getVal());
                                virtualizationDataInfo.setUUID(system.getHardware().getSystemInfo().getUuid());
                                virtualizationDataInfo.setMonitorServerId(monitorServerId);
                                virtualizationDataInfo.setMonitorServerName(monitorServerName);
                                //主机IP地址
                                String hostIp = "";
                                HostConfigInfo configInfo = system.getConfig();
                                if (configInfo != null) {
                                    HostNetworkInfo networkInfo = configInfo.getNetwork();
                                    if (networkInfo != null) {
                                        HostVirtualNic[] nics = networkInfo.getVnic();
                                        if (nics != null && nics.length > 0 && nics[0] != null) {
                                            HostVirtualNic nic = nics[0];
                                            hostIp = nic.getSpec().getIp().getIpAddress();
                                        }
                                    }
                                }
                                virtualizationDataInfo.setIp(hostIp);
                                listInfo.add(virtualizationDataInfo);

                                ManagedEntity[] managedEntities = new InventoryNavigator(system).searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                                if (managedEntities != null && managedEntities.length > 0) {
                                    for (ManagedEntity virEntity : managedEntities) {
                                        VirtualMachine vm = (VirtualMachine) virEntity;
                                        if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                                            virtualizationDataInfo = new VirtualizationDataInfo();
                                            virtualizationDataInfo.setId(vm.getMOR().getVal());
                                            virtualizationDataInfo.setInstanceName(vm.getName());
                                            virtualizationDataInfo.setType(VirtualizationType.VIRTUALMACHINE.getType());
                                            virtualizationDataInfo.setUUID(vm.getConfig().getInstanceUuid());
                                            virtualizationDataInfo.setPId(system.getMOR().getVal());
                                            GuestInfo guestInfo = vm.getGuest();
                                            String VMIp = guestInfo.getIpAddress();
                                            virtualizationDataInfo.setIp(VMIp);
                                            virtualizationDataInfo.setMonitorServerId(monitorServerId);
                                            virtualizationDataInfo.setMonitorServerName(monitorServerName);
                                            listInfo.add(virtualizationDataInfo);
                                        }
                                    }
                                }

                            }
                        }
                        //集群关联DataStore
                        Datastore[] datastoresi = cluster.getDatastores();
                        if (datastoresi != null && datastoresi.length > 0) {
                            for (Datastore datastores : datastoresi) {
                                virtualizationDataInfo = new VirtualizationDataInfo();
                                virtualizationDataInfo.setId(datastores.getMOR().getVal());
                                virtualizationDataInfo.setInstanceName(datastores.getName());
                                virtualizationDataInfo.setType(VirtualizationType.DATASTORE.getType());
                                virtualizationDataInfo.setPId(cluster.getMOR().getVal());
                                virtualizationDataInfo.setMonitorServerId(monitorServerId);
                                virtualizationDataInfo.setMonitorServerName(monitorServerName);

                                DatastoreInfo dsinfo = datastores.getInfo();
                                if (dsinfo instanceof VmfsDatastoreInfo) {
                                    VmfsDatastoreInfo vdinfo = (VmfsDatastoreInfo) dsinfo;
                                    virtualizationDataInfo.setUUID(vdinfo.getVmfs().getUuid());
                                }
                                listInfo.add(virtualizationDataInfo);
                            }
                        }
                    }

                }

                List<VirtualizationDataInfo> disHostList = listInfo.stream().filter(s -> s.getType().equals(VirtualizationType.HOSTSYSTEM.getType())).collect(Collectors.toList());
                log.info("集群下的主机数据:" + disHostList);

                //获取集群下所有宿主机的id
                Set<String> hostId = listInfo.stream().filter(s -> s.getType().equals(VirtualizationType.HOSTSYSTEM.getType())).map(s -> s.getId()).collect(Collectors.toSet());


                List<VirtualizationDataInfo> disdatastoreList = listInfo.stream().filter(s -> s.getType().equals(VirtualizationType.DATASTORE.getType())).collect(Collectors.toList());
                log.info("集群下的数据存储数据:" + disdatastoreList);
                //获取集群下所有数据存储的id
                Set<String> datastoreId = listInfo.stream().filter(s -> s.getType().equals(VirtualizationType.DATASTORE.getType())).map(s -> s.getId()).collect(Collectors.toSet());


                //没有集群，直接获取主机
                ManagedEntity[] hostSystems = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());
                //关联主机
                if (hostSystems != null && hostSystems.length > 0) {
                    List<VirtualizationDataInfo> newHostList = new ArrayList<>();
                    for (ManagedEntity hostManagedEntity : hostSystems) {
                        HostSystem system = (HostSystem) hostManagedEntity;
                        virtualizationDataInfo = new VirtualizationDataInfo();

                        //该宿主机不在集群下，则继续添加
                        if (!hostId.contains(system.getMOR().getVal())) {
                            virtualizationDataInfo.setId(system.getMOR().getVal());

                            HostHardwareInfo hardware = system.getHardware();
                            if (hardware != null && hardware.getSystemInfo() != null) {
                                virtualizationDataInfo.setUUID(system.getHardware().getSystemInfo().getUuid());
                            }
                            virtualizationDataInfo.setInstanceName(system.getName());
                            virtualizationDataInfo.setType(VirtualizationType.HOSTSYSTEM.getType());
                            virtualizationDataInfo.setPId(datacenter.getMOR().getVal());
                            virtualizationDataInfo.setMonitorServerId(monitorServerId);
                            virtualizationDataInfo.setMonitorServerName(monitorServerName);
                            //主机IP地址
                            String hostIp = "";
                            HostConfigInfo configInfo = system.getConfig();
                            if (configInfo != null) {
                                HostNetworkInfo networkInfo = configInfo.getNetwork();
                                if (networkInfo != null) {
                                    HostVirtualNic[] nics = networkInfo.getVnic();
                                    if (nics != null && nics.length > 0 && nics[0] != null) {
                                        HostVirtualNic nic = nics[0];
                                        hostIp = nic.getSpec().getIp().getIpAddress();
                                    }
                                }
                            }
                            virtualizationDataInfo.setIp(hostIp);
                            listInfo.add(virtualizationDataInfo);
                            newHostList.add(virtualizationDataInfo);
                        }
                        log.info("不在集群的主机数据:" + newHostList);


                    }
                }

                ManagedEntity[] managedEntities = inventoryNavigator.searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                if (managedEntities != null && managedEntities.length > 0) {
                    for (ManagedEntity virEntity : managedEntities) {
                        VirtualMachine vm = (VirtualMachine) virEntity;
                        if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                            virtualizationDataInfo = new VirtualizationDataInfo();
                            virtualizationDataInfo.setId(vm.getMOR().getVal());
                            virtualizationDataInfo.setInstanceName(vm.getName());
                            virtualizationDataInfo.setType(VirtualizationType.VIRTUALMACHINE.getType());
                            virtualizationDataInfo.setUUID(vm.getConfig().getInstanceUuid());
                            if (vm.getSummary() != null && vm.getSummary().getRuntime() != null && vm.getSummary().getRuntime().getHost() != null) {
                                virtualizationDataInfo.setPId(vm.getSummary().getRuntime().getHost().getVal());
                            }
                            GuestInfo guestInfo = vm.getGuest();
                            String VMIp = guestInfo.getIpAddress();
                            virtualizationDataInfo.setIp(VMIp);
                            virtualizationDataInfo.setMonitorServerId(monitorServerId);
                            virtualizationDataInfo.setMonitorServerName(monitorServerName);
                            listInfo.add(virtualizationDataInfo);
                        }
                    }
                }

                //关联数据存储
                Datastore[] datastoresi = datacenter.getDatastores();
                if (datastoresi != null && datastoresi.length > 0) {
                    List<VirtualizationDataInfo> newDataStoreList = new ArrayList<>();
                    for (Datastore datastores : datastoresi) {
                        virtualizationDataInfo = new VirtualizationDataInfo();
                        //该宿主机不在集群下，则继续添加
                        if (!datastoreId.contains(datastores.getMOR().getVal())) {
                            virtualizationDataInfo.setId(datastores.getMOR().getVal());
                            virtualizationDataInfo.setInstanceName(datastores.getName());
                            virtualizationDataInfo.setType(VirtualizationType.DATASTORE.getType());
                            virtualizationDataInfo.setPId(datacenter.getMOR().getVal());
                            virtualizationDataInfo.setMonitorServerId(monitorServerId);
                            virtualizationDataInfo.setMonitorServerName(monitorServerName);
                            listInfo.add(virtualizationDataInfo);
                            newDataStoreList.add(virtualizationDataInfo);
                        }
                        log.info("不在集群的数据存储数据:" + newDataStoreList);
                    }
                }
            }
        } else {//没有资源中心，直接获取宿主机
            //没有集群，直接获取主机
            ManagedEntity[] hostSystems = inventoryNavigator.searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());
            log.info("没有资源中心直接获取宿主机::" + hostSystems.length);
            //关联主机
            if (hostSystems != null && hostSystems.length > 0) {
                List<VirtualizationDataInfo> newHostList = new ArrayList<>();
                for (ManagedEntity hostManagedEntity : hostSystems) {
                    HostSystem system = (HostSystem) hostManagedEntity;
                    virtualizationDataInfo = new VirtualizationDataInfo();

                    virtualizationDataInfo.setId(system.getMOR().getVal());

                    HostHardwareInfo hardware = system.getHardware();
                    if (hardware != null && hardware.getSystemInfo() != null) {
                        virtualizationDataInfo.setUUID(system.getHardware().getSystemInfo().getUuid());
                    }
                    virtualizationDataInfo.setInstanceName(system.getName());
                    log.info("没有资源中心直接获取宿主机::name:" + system.getName());
                    virtualizationDataInfo.setType(VirtualizationType.HOSTSYSTEM.getType());
                    virtualizationDataInfo.setPId(pId);
                    virtualizationDataInfo.setMonitorServerId(monitorServerId);
                    virtualizationDataInfo.setMonitorServerName(monitorServerName);
                    //主机IP地址
                    String hostIp = "";
                    HostConfigInfo configInfo = system.getConfig();
                    if (configInfo != null) {
                        HostNetworkInfo networkInfo = configInfo.getNetwork();
                        if (networkInfo != null) {
                            HostVirtualNic[] nics = networkInfo.getVnic();
                            if (nics != null && nics.length > 0 && nics[0] != null) {
                                HostVirtualNic nic = nics[0];
                                hostIp = nic.getSpec().getIp().getIpAddress();
                            }
                        }
                    }
                    virtualizationDataInfo.setIp(hostIp);
                    listInfo.add(virtualizationDataInfo);
                    newHostList.add(virtualizationDataInfo);
                }
            }
            ManagedEntity[] managedEntities = inventoryNavigator.searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
            log.info("没有资源中心直接获取宿主机::" + managedEntities.length);
            if (managedEntities != null && managedEntities.length > 0) {
                for (ManagedEntity virEntity : managedEntities) {
                    VirtualMachine vm = (VirtualMachine) virEntity;
                    if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                        virtualizationDataInfo = new VirtualizationDataInfo();
                        virtualizationDataInfo.setId(vm.getMOR().getVal());
                        virtualizationDataInfo.setInstanceName(vm.getName());
                        log.info("没有资源中心直接获取虚拟机::name:" + vm.getName());
                        virtualizationDataInfo.setType(VirtualizationType.VIRTUALMACHINE.getType());
                        virtualizationDataInfo.setUUID(vm.getConfig().getInstanceUuid());
                        if (vm.getSummary() != null && vm.getSummary().getRuntime() != null && vm.getSummary().getRuntime().getHost() != null) {
                            virtualizationDataInfo.setPId(vm.getSummary().getRuntime().getHost().getVal());
                        }
                        GuestInfo guestInfo = vm.getGuest();
                        String VMIp = guestInfo.getIpAddress();
                        virtualizationDataInfo.setIp(VMIp);
                        virtualizationDataInfo.setMonitorServerId(monitorServerId);
                        virtualizationDataInfo.setMonitorServerName(monitorServerName);
                        listInfo.add(virtualizationDataInfo);
                    }
                }
            }
        }
        return listInfo;
    }

    @Autowired
    public Reply getAllVirtualInfo() {
        try {//获取所有虚拟化下的VCenter设备
            List<Map<String, Object>> list = null;
            if ("mysql".equals(insertType)) {
                //从mysql数据库中获取所有虚拟化设备
                list = mwModelVirtualizationDao.getAllVirtualDeviceInfo();
            } else {
                List<String> vCenterModelIndexs = mwModelVirtualizationDao.getAllVCenterInfo(modelId);
                List<MwModelInstanceParam> virInstanceList = new ArrayList<>();
                //获取查询数据的所有modelIndex
                Set<String> modelIndexSet = new HashSet<>();
                for (String modelIndex : vCenterModelIndexs) {
                    //查询数据库中VCenter实例关联的虚拟化设备实例id
                    virInstanceList = mwModelVirtualizationDao.queryVirualInstanceInfoByModelIndex(modelIndex, null);
                    for (MwModelInstanceParam m : virInstanceList) {
                        if (m != null && m.getModelIndex() != null) {
                            modelIndexSet.add(m.getModelIndex());
                        }
                    }
                }
                //获取所有的es中虚拟化设备信息
                QueryInstanceModelParam qParam = new QueryInstanceModelParam();
                qParam.setModelIndexs(new ArrayList<>(modelIndexSet));
                list = modelSever.getInstanceInfoByModelId(qParam);
            }

            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getAllVirtualInfo case{}", e);
            return Reply.fail(500, "获取所有虚拟化设备信息失败");
        }
    }

    /**
     * 虚拟化设备导出
     *
     * @param param
     * @return
     */
    @Override
    public Reply exportVirtualList(QueryVirtualInstanceParam param, HttpServletResponse response) {
        try {
            List<VirtualizationMonitorInfo> list = new ArrayList<>();
            String hString = redisTemplate.opsForValue().get(hKey);
            //redis中没值，则进行查询
            if (Strings.isNullOrEmpty(hString)) {
                list = getVirtualizationMonitorInfos(param);
                redisTemplate.opsForValue().set(hKey, JSONObject.toJSONString(list), 30, TimeUnit.MINUTES);
            } else {
                list = JSONObject.parseArray(hString, VirtualizationMonitorInfo.class);
            }
            List<Map> mapList = objectsToMaps(list);
            List<String> lable = param.getHeader();
            List<String> lableName = param.getHeaderName();
            ExportExcel.exportExcel("模型虚拟化列表导出", "模型虚拟化列表导出", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
            return Reply.ok();
        } catch (ModelManagerException e) {
            log.error("fail to getVirtualDeviceInfoList case: {}", e);
            return Reply.fail(500, e.getMessage());
        } catch (Throwable e) {
            log.error("fail to exportVirtualList case{}", e);
            return Reply.fail(500, "虚拟化列表信息导出失败");
        }
    }

    @Override
    public Reply setVirtualUser(ModelRelationInstanceUserListParam params) {
        try {
            params.setType(DataType.MODEL_VIRTUAL);
            mwModelRancherService.setModelInstancePerUser(params);
            return Reply.ok("新增关联负责人成功！");
        } catch (Exception e) {
            return Reply.fail(ErrorConstant.SET_VIRTUAL_USER_CODE_307004, ErrorConstant.SET_VIRTUAL_USER_MSG_307004);
        }
    }

    /**
     * 查询虚拟化权限
     *
     * @param param
     * @return
     */
    @Override
    public Reply getVirtualUser(ModelRelationInstanceUserParam param) {
        try {
            List<ModelRelationInstanceUserParam> list = new ArrayList<>();
            param.setType(DataType.MODEL_VIRTUAL);
            list = mwModelRancherService.getModelInstancePerUser(param);
            return Reply.ok(list);
        } catch (Exception e) {
            return Reply.fail(ErrorConstant.SET_VIRTUAL_USER_CODE_307005, ErrorConstant.SET_VIRTUAL_USER_MSG_307005);
        }
    }

    @Override
    public Reply queryAssetsInfo(QueryHostParam qParam) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<Map<String, Object>> newList = new ArrayList<>();
        try {
            //Ip精准查询
            List<Map<String, Object>> ckeckByIpDTO = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                    .inBandIp(qParam.getIp()).monitorServerId(qParam.getMonitorServerId()).filterQuery(true).build());
            //主机名称模糊查询
            List<Map<String, Object>> ckeckByHostNameDTO = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                    .instanceName(qParam.getVHostName()).monitorServerId(qParam.getMonitorServerId()).build());
            if (CollectionUtils.isNotEmpty(ckeckByIpDTO)) {
                mapList.addAll(ckeckByIpDTO);
            }
            if (CollectionUtils.isNotEmpty(ckeckByHostNameDTO)) {
                mapList.addAll(ckeckByHostNameDTO);
            }
            newList = mapList;
            if (CollectionUtils.isNotEmpty(mapList) && mapList.size() > 0) {
                mapList = Arrays.asList(mapList.get(0));
                newList = new ArrayList<>();
                //设置资产状态
                newList = mwModelViewServiceImpl.getAssetsStateByZabbix(mapList);
            }
        } catch (Throwable e) {
            log.error("虚拟化纳管跳转查询失败", e);
        }
        return Reply.ok(newList);
    }

    @Override
    public ModelVirtualDeleteContext deleteVirtualIntance(Integer instanceId) {
        return mwModelVirtualRelationManager.deleteVCenter(instanceId);
    }

    protected void addMapperAndPerm(ModelVirtualPermControlParam param) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(param.getGroupIds())  //用户组
                .userIds(param.getUserIds())  //责任人
                .orgIds(param.getOrgIds())      //机构
                .typeId(String.valueOf(param.getId())) //数据主键
                .type(param.getType())        //模型虚拟化
                .desc(param.getDesc()).build(); //模型虚拟化
        mwCommonService.addMapperAndPerm(insertDto);
    }

    /**
     * 删除负责人，用户组，机构 权限关系
     *
     * @param param
     */
    protected void deleteMapperAndPerm(ModelVirtualPermControlParam param) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(param.getId()))
                .type(param.getType())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 对字符串数字进行格式化
     *
     * @param value
     * @return
     */
    public String numToStr(String value) {
        BigDecimal bValue = new BigDecimal(value);
        bValue = bValue.setScale(2, BigDecimal.ROUND_HALF_UP);
        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
        String valStr = decimalFormat.format(bValue);
        return valStr;
    }


    /**
     * @param pList
     * @param id
     * @param cList
     * @param isAddItself 是否添加父节点数据
     */
    private void getChildList(List<VirtualizationMonitorInfo> pList, String id, List<VirtualizationMonitorInfo> cList, Boolean isAddItself) {
        if (!Strings.isNullOrEmpty(id)) {
            for (VirtualizationMonitorInfo t : pList) {
                if (isAddItself) {
                    if (id.equals(t.getId())) {
                        cList.add(t);
                    }
                }
                if (id.equals(t.getPId())) {
                    cList.add(t);
                    getChildList(pList, t.getId(), cList, false);
                }
            }
        }
    }

    private void getParentLists(List<VirtualizationMonitorInfo> pList, String pId, List<VirtualizationMonitorInfo> cList, Boolean isAddItself) {
        if (!Strings.isNullOrEmpty(pId)) {
            for (VirtualizationMonitorInfo t : pList) {
                if (isAddItself) {
                    if (pId.equals(t.getId())) {
                        cList.add(t);
                    }
                }
                if (pId.equals(t.getId())) {
                    cList.add(t);
                    getParentLists(pList, t.getPId(), cList, false);
                }
            }
        }
    }

    private void getParentList(List<MwInstanceInfoParam> pList, String pId, List<MwInstanceInfoParam> cList, Boolean isAddItself) {
        if (!Strings.isNullOrEmpty(pId)) {
            for (MwInstanceInfoParam m : pList) {
                if (isAddItself) {
                    if (pId.equals(m.getPId())) {
                        cList.add(m);
                    }
                }
                if (pId.equals(m.getId())) {
                    cList.add(m);
                    getParentList(pList, m.getPId(), cList, false);
                }
            }
        }
    }


    /**
     * 虚拟化使用新方法
     */
    public List<VirtualizationDataInfo> getVcenterAllList(String userName, String password, String url, String assetsId) {
        long time1 = System.currentTimeMillis();
        List<VirtualizationDataInfo> instanceList = new ArrayList<>();
        String vCenterURL = "https://" + url;
        String apiUrl = vCenterURL + "/api/session";
        String userPass = userName + ":" + password;
        String basicAuth = "Basic " + java.util.Base64.getEncoder().encodeToString(userPass.getBytes());
        //获取sessionId：vmware-api-session-id
        String jsonText = ModelOKHttpUtils.builder().url(apiUrl)
                .addHeader("Authorization", basicAuth)
                .post(true, true)
                .sync();
        log.info("获取jsonText::" + jsonText);

        if (jsonText != null && jsonText.indexOf("unexpected end of stream on") != -1) {
            return null;
        }

        //获取所有的dataCenter
        String datacenterUrl = vCenterURL + "/api/vcenter/datacenter";
        String datacenter = ModelOKHttpUtils.builder().url(datacenterUrl)
                .addHeader("vmware-api-session-id", jsonText.replaceAll("\"", ""))
                .get()
                .sync();
        log.info("获取vmdatacenterInfo::" + datacenter);
        List<VmDataCenterParamTest> vmDataCenterParamTests = JSONArray.parseArray(datacenter, VmDataCenterParamTest.class);
        if (Strings.isNullOrEmpty(datacenter) || CollectionUtils.isEmpty(vmDataCenterParamTests)) {
            return null;
        }
        for (VmDataCenterParamTest dataCenterParam : vmDataCenterParamTests) {
            String clusterUrl = vCenterURL + "/api/vcenter/cluster";
            String cluster = ModelOKHttpUtils.builder().url(clusterUrl)
                    .addHeader("vmware-api-session-id", jsonText.replaceAll("\"", ""))
                    .addParam("datacenters", dataCenterParam.getDatacenter())
                    .get()
                    .sync();
            log.info("获取clusterInfo::" + cluster);
            List<VirtualizationDataInfo> clusterHostAllInfo = new ArrayList<>();
            List<VmClusterParamTest> vmClusterParamTests = JSONArray.parseArray(cluster, VmClusterParamTest.class);
            if (CollectionUtils.isNotEmpty(vmClusterParamTests)) {
                for (VmClusterParamTest clusterParamTest : vmClusterParamTests) {
                    clusterParamTest.setPId(dataCenterParam.getId());
                    clusterParamTest.setType(CLUSTER.getType());
                    List<VirtualizationDataInfo> clusterHostInfo = getHostInfo(vCenterURL, jsonText, CLUSTERS, clusterParamTest.getId());
                    clusterHostAllInfo.addAll(clusterHostInfo);
                    instanceList.addAll(clusterHostInfo);
                }
                instanceList.addAll(JSONArray.parseArray(JSONArray.toJSONString(vmClusterParamTests), VirtualizationDataInfo.class));
            }
            List<VirtualizationDataInfo> datacenterHostInfo = getHostInfo(vCenterURL, jsonText, DATACENTERS, dataCenterParam.getId());
            //去除集群下的主机
            List<VirtualizationDataInfo> datacenterHostDataInfos = deduplicateLists(datacenterHostInfo, clusterHostAllInfo);
            instanceList.addAll(datacenterHostDataInfos);
            dataCenterParam.setPId(assetsId);
            dataCenterParam.setType(DATACNETER.getType());

            String datastoreUrl = vCenterURL + "/api/vcenter/datastore";
            String datastore = ModelOKHttpUtils.builder().url(datastoreUrl)
                    .addHeader("vmware-api-session-id", jsonText.replaceAll("\"", ""))
                    .addParam("datacenters", dataCenterParam.getDatacenter())
                    .get()
                    .sync();
            log.info("获取vmdatastoreInfo::" + datastore);
            List<VmDataStoreParamTest> vmDatastoreParamTests = JSONArray.parseArray(datastore, VmDataStoreParamTest.class);
            for (VmDataStoreParamTest dataStoreParamTest : vmDatastoreParamTests) {
                dataStoreParamTest.setPId(dataCenterParam.getId());
                dataStoreParamTest.setType(DATASTORE.getType());
            }
            instanceList.addAll(JSONArray.parseArray(JSONArray.toJSONString(vmDatastoreParamTests), VirtualizationDataInfo.class));
        }
        instanceList.addAll(JSONArray.parseArray(JSONArray.toJSONString(vmDataCenterParamTests), VirtualizationDataInfo.class));
        long time2 = System.currentTimeMillis();
        System.out.println("输出长度：" + instanceList.size() + ";耗时：" + (time2 - time1) + "ms");
        return instanceList;
    }

    /**
     * 根据Id去重
     *
     * @param list1 需要去除的集合
     * @param list2
     * @return
     */
    private static List<VirtualizationDataInfo> deduplicateLists(List<VirtualizationDataInfo> list1, List<VirtualizationDataInfo> list2) {
        List<String> idList = list2.stream().filter(s -> s.getId() != null).map(s -> s.getId()).collect(Collectors.toList());
        ;
        List<VirtualizationDataInfo> collect = list1.stream().filter(item -> !idList.contains(item.getId())).collect(Collectors.toList());
        return collect;
    }

    private List<VirtualizationDataInfo> getHostInfo(String vCenterURL, String jsonText, String paramName, String pId) {

        List<VirtualizationDataInfo> instanceListNew = new ArrayList<>();
        String hostUrl = vCenterURL + "/api/vcenter/host";
        String host = ModelOKHttpUtils.builder().url(hostUrl)
                .addHeader("vmware-api-session-id", jsonText.replaceAll("\"", ""))
                .addParam(paramName, pId)
                .get()
                .sync();
        log.info("获取hostInfo::" + host);
        List<VmHostParamTest> vmHostParamTests = JSONArray.parseArray(host, VmHostParamTest.class);
        for (VmHostParamTest hostParamTest : vmHostParamTests) {
            hostParamTest.setPId(pId);
            hostParamTest.setType(HOSTSYSTEM.getType());
            String vmUrl = vCenterURL + "/api/vcenter/vm";
            String vm = ModelOKHttpUtils.builder().url(vmUrl)
                    .addHeader("vmware-api-session-id", jsonText.replaceAll("\"", ""))
                    .addParam("hosts", hostParamTest.getHost())
                    .get()
                    .sync();
            log.info("获取vm::" + vm);
            List<VMParamTest> vmParamTests = JSONArray.parseArray(vm, VMParamTest.class);
            for (VMParamTest vmParamTest : vmParamTests) {
                //集群下的虚拟机
                if (CLUSTERS.equals(paramName)) {
                    vmParamTest.setClusterId(pId);
                } else {
                    vmParamTest.setDatacenterId(pId);
                }
                vmParamTest.setPId(hostParamTest.getId());
                vmParamTest.setType(VIRTUALMACHINE.getType());
            }
            instanceListNew.addAll(JSONArray.parseArray(JSONArray.toJSONString(vmParamTests), VirtualizationDataInfo.class));
        }
        instanceListNew.addAll(JSONArray.parseArray(JSONArray.toJSONString(vmHostParamTests), VirtualizationDataInfo.class));
        return instanceListNew;
    }

    @Override
    public TimeTaskRresult getVCenterInfoByTaskTime() {
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try {
            List<MwModelInstanceCommonParam> mwModelInstanceCommonParams = mwModelInstanceDao.selectModelInstanceInfoById(VCENTER.getModelId());
            Set<String> modelIndexsSet = mwModelInstanceCommonParams.stream().map(MwModelInstanceCommonParam::getModelIndex).collect(Collectors.toSet());
            Set<Integer> instanceIdsSet = mwModelInstanceCommonParams.stream().map(MwModelInstanceCommonParam::getModelInstanceId).collect(Collectors.toSet());

            QueryInstanceModelParam qParam = new QueryInstanceModelParam();
            qParam.setModelIndexs(new ArrayList<>(modelIndexsSet));
            qParam.setInstanceIds(new ArrayList<>(instanceIdsSet));
            List<Map<String, Object>> instanceInfoByModelId = modelSever.getInstanceInfoByModelId(qParam);
            for (Map<String, Object> dto : instanceInfoByModelId) {
                QueryVirtualInstanceParam param = new QueryVirtualInstanceParam();
                param.setModelIndex(strValueConvert(dto.get(MODEL_INDEX)));
                param.setModelInstanceId(intValueConvert(dto.get(INSTANCE_ID_KEY)));
                param.setRelationInstanceId(intValueConvert(dto.get(INSTANCE_ID_KEY)));
                Reply reply = syncVirtualDeviceInfo(param);
                if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                    taskRresult.setSuccess(false);
                } else {
                    taskRresult.setSuccess(true);
                }
            }
        } catch (Throwable e) {
            log.error("fail to getVirtualInfoByTaskTime case:{}", e);
            taskRresult.setSuccess(false);
            taskRresult.setFailReason("定时任务同步Virtual数据执行失败");
        }
        return taskRresult;
    }


    /**
     * 虚拟化数据增加hostId，ip，serverName持久化保存，资产告警需要
     *
     * @param param
     * @throws Exception
     */
    private void saveVirtualInfoToAlert(QueryVirtualInstanceParam param) throws Exception {
        Map<String, VirtualizationDataInfo> map = getAllVirtualInstanceByEs(param.getModelIndex(), param.getRelationInstanceId());
        List<VirtualizationDataInfo> list = setVirtualUUIDByAPI(param, map);
        setVirtualHostIdByZabbix(list, param.getMonitorServerId());
        //先删除，根据唯一的id
        mwModelVirtualizationDao.deleteVCenterInfo(list);
        //在新增
        mwModelVirtualizationDao.saveVCenterInfo(list);
    }


    private Map<String, VirtualizationDataInfo> getAllVirtualInstanceByEs(String modelIndex, Integer relationId) {
        List<MwModelInstanceParam> virInstanceList = mwModelVirtualizationDao.queryVirualInstanceInfoByModelIndex(modelIndex, relationId);
        //获取查询数据的所有modelIndex和instanceId，下面的批量删除使用
        Set<String> modelIndexSet = new HashSet<>();
        Set<Integer> instanceIdSet = new HashSet<>();
        for (MwModelInstanceParam m : virInstanceList) {
            if (m != null && !Strings.isNullOrEmpty(m.getModelIndex())) {
                modelIndexSet.add(m.getModelIndex());
            }
            if (m != null && m.getInstanceId() != null) {
                instanceIdSet.add(m.getInstanceId());
            }
        }
        QueryInstanceModelParam instanceModelParam = new QueryInstanceModelParam();
        instanceModelParam.setModelIndexs(new ArrayList<>(modelIndexSet));
        instanceModelParam.setInstanceIds(new ArrayList<>(instanceIdSet));
        instanceModelParam.setPageSize(pageSize);
        //获取es中所有VCenter的关联设备（集群主机虚拟机等）
        Map<String, Object> mapInfo = mwModelViewServiceImpl.getModelListInfoByBase(instanceModelParam);
        List<Map<String, Object>> modelListMap = new ArrayList<>();
        if (mapInfo != null && mapInfo.get("data") != null) {
            modelListMap = (List<Map<String, Object>>) mapInfo.get("data");
        }
        List<VirtualizationDataInfo> vmInstanceList = JSONArray.parseArray(JSONArray.toJSONString(modelListMap), VirtualizationDataInfo.class);
        Map<String, VirtualizationDataInfo> collect = vmInstanceList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
        return collect;
    }

    public List<VirtualizationDataInfo> setVirtualUUIDByAPI(QueryVirtualInstanceParam param, Map<String, VirtualizationDataInfo> collect) throws Exception {
        ConnectedVimServiceBase cs = new ConnectedVimServiceBase();
        Integer monitorServerId = param.getMonitorServerId();
        String monitorServerName = param.getMonitorServerName();
        log.info("do syncVirtualDeviceInfo local search");
        //VCenterApi登录连接
        cs.connect(param.getUrl(), param.getUserName(), param.getPassword());
        ServiceInstance serviceInstance = cs.si;
        //rootFolder-------根文件夹
        Folder rootFolder = serviceInstance.getRootFolder();
        //inventoryNavigator----文件夹目录
        InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
        //获取数据中心层
        ManagedEntity[] dataCenterManagedEntities = inventoryNavigator.searchManagedEntities(VirtualizationType.DATACNETER.getType());
        Datacenter datacenter = null;
        List<VirtualizationDataInfo> vmInstanceList = new ArrayList<>();
        if (dataCenterManagedEntities != null && dataCenterManagedEntities.length > 0) {
            for (ManagedEntity managedEntity : dataCenterManagedEntities) {
                datacenter = (Datacenter) managedEntity;
                //直接获取主机
                ManagedEntity[] hostSystems = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.HOSTSYSTEM.getType());
                //关联主机
                if (hostSystems != null && hostSystems.length > 0) {
                    for (ManagedEntity hostManagedEntity : hostSystems) {
                        HostSystem system = (HostSystem) hostManagedEntity;
                        //对实例数据进行赋值，UUID、IP、monitorServerId、monitorServerName
                        if (collect != null && collect.containsKey(system.getMOR().getVal())) {
                            VirtualizationDataInfo virtualizationDataInfo = collect.get(system.getMOR().getVal());
                            HostHardwareInfo hardware = system.getHardware();
                            if (hardware != null && hardware.getSystemInfo() != null) {
                                virtualizationDataInfo.setUUID(system.getHardware().getSystemInfo().getUuid());
                            }
                            virtualizationDataInfo.setMonitorServerId(monitorServerId);
                            virtualizationDataInfo.setMonitorServerName(monitorServerName);
                            //主机IP地址
                            String hostIp = "";
                            HostConfigInfo configInfo = system.getConfig();
                            if (configInfo != null) {
                                HostNetworkInfo networkInfo = configInfo.getNetwork();
                                if (networkInfo != null) {
                                    HostVirtualNic[] nics = networkInfo.getVnic();
                                    if (nics != null && nics.length > 0 && nics[0] != null) {
                                        HostVirtualNic nic = nics[0];
                                        hostIp = nic.getSpec().getIp().getIpAddress();
                                    }
                                }
                            }
                            virtualizationDataInfo.setIp(hostIp);
                            vmInstanceList.add(virtualizationDataInfo);
                        }
                    }
                }
                ManagedEntity[] managedEntities = new InventoryNavigator(datacenter).searchManagedEntities(VirtualizationType.VIRTUALMACHINE.getType());
                if (managedEntities != null && managedEntities.length > 0) {
                    for (ManagedEntity virEntity : managedEntities) {
                        VirtualMachine vm = (VirtualMachine) virEntity;
                        if (vm.getConfig() != null && !vm.getConfig().isTemplate()) {
                            if (collect != null && collect.containsKey(vm.getMOR().getVal())) {
                                VirtualizationDataInfo virtualizationDataInfo = collect.get(vm.getMOR().getVal());
                                virtualizationDataInfo.setUUID(vm.getConfig().getInstanceUuid());
                                GuestInfo guestInfo = vm.getGuest();
                                String VMIp = guestInfo.getIpAddress();
                                virtualizationDataInfo.setIp(VMIp);
                                virtualizationDataInfo.setMonitorServerId(monitorServerId);
                                virtualizationDataInfo.setMonitorServerName(monitorServerName);
                                vmInstanceList.add(virtualizationDataInfo);
                            }
                        }
                    }
                }

            }
        }
        return vmInstanceList;
    }

    private void setVirtualHostIdByZabbix(List<VirtualizationDataInfo> vmInstanceList, Integer monitorServerId) {
        List<String> uuidList = new ArrayList<>();
        for (VirtualizationDataInfo info : vmInstanceList) {
            if (!Strings.isNullOrEmpty(info.getUUID())) {
                uuidList.add(info.getUUID());
            }
        }
        MWZabbixAPIResult itemsInfo = null;
        //通过虚拟化设备的uuid和zabbix主机中的不可见名称关联，获取对应的hostId
        if (monitorServerId != 0) {
            itemsInfo = mwtpServerAPI.hostGetbyFilterByUUID(monitorServerId, uuidList);
        }

        Map<String, Map<String, String>> hostIdMap = new HashMap();
        if (itemsInfo != null && !itemsInfo.isFail() && ((ArrayNode) itemsInfo.getData()).size() > 0) {
            JsonNode data = (JsonNode) itemsInfo.getData();
            if (data.size() > 0) {
                for (JsonNode host : data) {
                    Map<String, String> map = new HashMap();
                    String hostId = host.get("hostid").asText();
                    String hostUUID = host.get("host").asText();
                    String hostName = host.get("name").asText();
                    map.put("hostId", hostId);
                    map.put("hostName", hostName);
                    hostIdMap.put(hostUUID, map);
                }
            }
        }

        for (VirtualizationDataInfo info : vmInstanceList) {
            if (hostIdMap != null && hostIdMap.containsKey(info.getUUID())) {
                Map<String, String> map = hostIdMap.get(info.getUUID());
                String hostId = map.get("hostId");
                String hostName = map.get("hostName");
                info.setHostId(hostId);
                info.setHostName(hostName);
            }
        }
    }

}
