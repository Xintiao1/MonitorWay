package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.model.dao.MwModelCitrixDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.model.dto.ModelInstanceDto;
import cn.mw.monitor.model.dto.MwModelInfoDTO;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.DeleteModelInstanceParam;
import cn.mw.monitor.model.param.MwModelMacrosParam;
import cn.mw.monitor.model.param.citrix.*;
import cn.mw.monitor.model.service.MwModelCitrixService;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.model.util.ModelIPUtil;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.service.model.dto.*;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.util.ExcelMergeUtils;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.monitor.util.TransferUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cn.mw.monitor.model.param.citrix.ModelCitrixType.*;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.RELATION_INSTANCE_ID;
import static cn.mw.monitor.util.ListMapObjUtils.objectsToMaps;

/**
 * @author qzg
 * @date 2022/10/8
 */
@Service
@Slf4j
public class MwModelCitrixServiceImpl implements MwModelCitrixService {
    @Resource
    private MwModelCitrixDao mwModelCitrixDao;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelInstanceServiceImplV1 mwModelInstanceServiceImpl;
    @Autowired
    private MwModelCitrixRelationManager mwModelCitrixRelationManager;
    @Value("${citrix.modelId}")
    private String citrixModelId;
    @Value("${mw.graph.enable}")
    private boolean graphEnable;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private MwModelViewService mwModelViewService;

    @Autowired
    private ProxySearch proxySearch;

    //位异或密钥
    private static final int KEY = 5;
    private static final String PORT = "PORT";
    private static final String IP = "IP";
    private static int lbTreeId = 1;
    private static int gslbTreeId = 2;
    private int pageSize = 10000;

    private static final String GSLBVSERVER_GSLBSERVICE_BINDING = "gslbvserver_gslbservice_binding";
    private static final String GSLBDOMAIN_GSLBSERVICE_BINDING = "gslbdomain_gslbservice_binding";
    private static final String SERVER_SERVICE_BINDING = "server_service_binding";
    private static final String LBVSERVER_SERVICE_BINDING = "lbvserver_service_binding";


    @Override
    public Reply loginClientGetData(MwQueryCitrixParam mParam) {
        try {
            List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
            List<String> modelIndexs = new ArrayList<>();
            Map<String, List<MwModelCitrixInfoParam>> mapAll = new HashMap();
            //获取登录信息
            MwModelCitrixLoginParam param = getLoginInfo(mParam);
            //TODO 前端直接传值过来
            mParam.setModelId(param.getModelId());
            //通过APi获取LB Virtual Servers
            param.setType(ModelCitrixType.LB_VIRTUAL_SERVERS.getDesc());
            addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.LB_VIRTUAL_SERVERS.getType(), instanceInfoList, modelIndexs, mapAll);

            //获取LB Services
            param.setType(ModelCitrixType.LB_SERVICES.getDesc());
            addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.LB_SERVICES.getType(), instanceInfoList, modelIndexs, mapAll);

            //获取LB Server
            param.setType(LB_SERVER.getDesc());
            addInstanceToModel(param, mParam.getRelationInstanceId(), LB_SERVER.getType(), instanceInfoList, modelIndexs, mapAll);

            //获取GSLB Virtual Servers
            param.setType(ModelCitrixType.GSLB_VIRTUAL_SERVERS.getDesc());
            addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.GSLB_VIRTUAL_SERVERS.getType(), instanceInfoList, modelIndexs, mapAll);

            //获取GSLB Services
            param.setType(ModelCitrixType.GSLB_SERVICES.getDesc());
            addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.GSLB_SERVICES.getType(), instanceInfoList, modelIndexs, mapAll);

            //获取GSLB Domain
            param.setType(ModelCitrixType.GSLB_DOMAIN.getDesc());
            addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.GSLB_DOMAIN.getType(), instanceInfoList, modelIndexs, mapAll);

            //LB关联列表数据
            List<MwModelCitrixInfoParam> mwModelCitrixListDatas = syncSaveLBRelationList(mParam, mapAll);
            addInstanceToModel(mwModelCitrixListDatas, mParam.getRelationInstanceId(), LOAD_BALANCING.getType(), instanceInfoList, modelIndexs);

            //GSLB关联列表数据
            List<MwModelCitrixInfoParam> mwModelGSLBDatas = syncSaveGSLBRelationList(mParam, mapAll);
            addInstanceToModel(mwModelGSLBDatas, mParam.getRelationInstanceId(), ModelCitrixType.GSLB.getType(), instanceInfoList, modelIndexs);

            if (CollectionUtils.isEmpty(instanceInfoList)) {
                return Reply.fail(500, "负载均衡数据数量为0");
            }


            List<InstanceLine> lines = new ArrayList<>();
            Set<InstanceNode> nodes = new HashSet<>();
            DeleteModelInstanceParam deleteParam1 = new DeleteModelInstanceParam();
            deleteParam1.setModelIndexs(modelIndexs);
            deleteParam1.setRelationInstanceIds(Arrays.asList(mParam.getRelationInstanceId()));
            mwModelInstanceService.batchDeleteInstanceInfo(deleteParam1);
            if (instanceInfoList != null && instanceInfoList.size() > 0) {
                mwModelInstanceService.saveData(instanceInfoList, true, true);
            }
            //是否使用neo4j保存关系
            if (graphEnable) {
                List<VirtualGroup> virtualGroups = new ArrayList<>();
                //设置根节点CitrixADC实例和server的nodes和lines
                setRootLBVServerNodesAndLines(mParam, instanceInfoList, lines, nodes, virtualGroups);
                //先删除之前的neo4j数据
                mwModelCitrixRelationManager.deleteInstanceTopo(mParam.getModelInstanceId());
                //获取LB的关系数据
                Map<String, Map<String, List<MwModelCitrixRelationParam>>> mwCitrixRelationMap = getLBRelationList(mParam);
                //获取节点和线数据，存入neo4j数据
                mwModelCitrixRelationManager.doVisiteCitrixInfo(mParam.getModelInstanceId(), instanceInfoList, mwCitrixRelationMap, InstanceNotifyType.VirtualSyncInit, virtualGroups);
            }
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to loginClientGetData case:{}", e);
            return Reply.fail(500, "获取负载均衡数据失败");
        }
    }


    private void setRootLBVServerNodesAndLines(MwQueryCitrixParam mParam, List<AddAndUpdateModelInstanceParam> instanceInfoList,
                                               List<InstanceLine> lines, Set<InstanceNode> nodes, List<VirtualGroup> virtualGroups) {

        //获取LBVserver的IndexModel
        MwModelInfoDTO modelInfo = mwModelCitrixDao.getModelIndexByModelName(ModelCitrixType.LB_VIRTUAL_SERVERS.getType());
        Map<String, List<AddAndUpdateModelInstanceParam>> map = instanceInfoList.stream().collect(Collectors.groupingBy(AddAndUpdateModelInstanceParam::getModelIndex));
        List<AddAndUpdateModelInstanceParam> list = map.get(modelInfo.getModelIndex());

        NodeParam startNodeParam = new NodeParam(mParam.getModelId(), mParam.getModelInstanceId());
        InstanceNode startNode = new InstanceNode(startNodeParam);
        nodes.add(startNode);
        VirtualGroup virtualGroup = new VirtualGroup();

        for (AddAndUpdateModelInstanceParam param : list) {
            NodeParam endNodeParam = new NodeParam(param.getModelId() != null ? Integer.valueOf(param.getModelId()) : 0,
                    param.getInstanceId() != null ? Integer.valueOf(param.getInstanceId()) : 0);
            InstanceNode endNode = new InstanceNode(endNodeParam);
            nodes.add(endNode);
            InstanceLine line = new InstanceLine(startNode, endNode);
            lines.add(line);
        }
        virtualGroup.setLineList(lines);
        virtualGroup.setNodes(new ArrayList<>(nodes));
        virtualGroups.add(virtualGroup);
    }

    @Override
    public Reply getCitrixTreeInfo() {
        try {

            List<MwModelCitrixTreeParam> list = new ArrayList<>();
            List<MwModelCitrixTreeParam> LBChildList = new ArrayList<>();
            List<MwModelCitrixTreeParam> GSLBChildList = new ArrayList<>();
            List<MwModelInfoDTO> modelInfo = mwModelCitrixDao.getModelIndexByName();
            MwModelCitrixTreeParam tParam1 = new MwModelCitrixTreeParam();
            tParam1.setId(lbTreeId);
            tParam1.setType(LOAD_BALANCING.getDesc());
            tParam1.setLabel(LOAD_BALANCING.getType());

            MwModelCitrixTreeParam tParam2 = new MwModelCitrixTreeParam();
            tParam2.setId(gslbTreeId);
            tParam2.setLabel(ModelCitrixType.GSLB.getType());
            tParam2.setType(ModelCitrixType.GSLB.getDesc());
            for (MwModelInfoDTO dto : modelInfo) {
                if (LOAD_BALANCING.getType().equals(dto.getModelName())) {
                    tParam1.setModelIndex(dto.getModelIndex());
                    tParam1.setModelId(Integer.valueOf(dto.getModelId()));
                }
                if (ModelCitrixType.GSLB.getType().equals(dto.getModelName())) {
                    tParam2.setModelIndex(dto.getModelIndex());
                    tParam2.setModelId(Integer.valueOf(dto.getModelId()));
                }
                MwModelCitrixTreeParam tParam = new MwModelCitrixTreeParam();
                tParam.setId(Integer.valueOf(dto.getModelId()));
                tParam.setModelId(Integer.valueOf(dto.getModelId()));
                tParam.setLabel(dto.getModelName());
                tParam.setModelIndex(dto.getModelIndex());
                if (dto.getModelName().startsWith(LOAD_BALANCING.getDesc())) {
                    tParam.setType(LOAD_BALANCING.getDesc());
                    LBChildList.add(tParam);
                }
                if (dto.getModelName().startsWith(ModelCitrixType.GSLB.getDesc()) && !dto.getModelName().equals(ModelCitrixType.GSLB.getDesc())) {
                    tParam.setType(ModelCitrixType.GSLB.getDesc());
                    GSLBChildList.add(tParam);
                }
            }
            tParam1.setChildren(LBChildList);
            list.add(tParam1);
            tParam2.setChildren(GSLBChildList);
            list.add(tParam2);
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getCitrixTreeInfo case:{}", e);
            return Reply.fail(500, "获取负载均衡树形结构失败");
        }
    }

    /**
     * LB列表数据展示
     *
     * @param mParam
     * @return
     */
    @Override
    public Reply getLBCitrixRelationList(MwQueryCitrixParam mParam) {
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
            qParam.setModelIndex(mParam.getModelIndex());
            List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
            List<MwModelLBCitrixListData> mwModelCitrixLists = new ArrayList<>();
            List<MwModelCitrixInfoParam> mwModelCitrixListDatas = new ArrayList<>();
            AddModelInstancePropertiesParam instancePropertiesParam = new AddModelInstancePropertiesParam();
            instancePropertiesParam.setPropertiesIndexId("relationInstanceId");
            instancePropertiesParam.setPropertiesValue(mParam.getRelationInstanceId() + "");
            instancePropertiesParam.setPropertiesType(ModelPropertiesType.INTEGER.getCode());
            propertiesList.add(instancePropertiesParam);
            qParam.setPropertiesList(propertiesList);
            //TODO 暂时不对citrix关联数据做权限，后面需要对每条数据加上权限（部门负责人）
            qParam.setIsTimeTask(true);
            List<Map<String, Object>> citrixRelationInfo = mwModelInstanceService.getInstanceInfoByExport(qParam);
            List<MwModelLBCitrixListData> citrixLBInfoList = JSONArray.parseArray(JSONObject.toJSONString(citrixRelationInfo), MwModelLBCitrixListData.class);
            mwModelCitrixLists = LBDataHandle(mParam, citrixLBInfoList);
            pageInfo.setTotal(mwModelCitrixLists.size());
            mwModelCitrixListDatas = pageList.getList(mwModelCitrixLists, mParam.getPageNumber(), mParam.getPageSize());
            pageInfo.setList(mwModelCitrixListDatas);
            return Reply.ok(pageInfo);
        } catch (Throwable e) {
            log.error("fail to getLBCitrixRelationList case:{}", e);
            return Reply.fail(500, "获取负载均衡数据失败");
        }
    }


    /**
     * GSLB列表数据展示
     *
     * @param mParam
     * @return
     */
    @Override
    public Reply getGSLBCitrixRelationList(MwQueryCitrixParam mParam) {
        try {
            List<MwModelGSLBCitrixListData> mwModelGSLBDatas = new ArrayList<>();
            List<MwModelGSLBCitrixListData> mwModelGSLBLists = new ArrayList<>();
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
            qParam.setModelIndex(mParam.getModelIndex());
            List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
            AddModelInstancePropertiesParam instancePropertiesParam = new AddModelInstancePropertiesParam();
            instancePropertiesParam.setPropertiesIndexId("relationInstanceId");
            instancePropertiesParam.setPropertiesValue(mParam.getRelationInstanceId() + "");
            instancePropertiesParam.setPropertiesType(ModelPropertiesType.INTEGER.getCode());
            propertiesList.add(instancePropertiesParam);
            qParam.setPropertiesList(propertiesList);
            //TODO 暂时不对citrix关联数据做权限，后面需要对每条数据加上权限（部门负责人）
            qParam.setIsTimeTask(true);
            List<Map<String, Object>> citrixRelationInfo = mwModelInstanceService.getInstanceInfoByExport(qParam);
            List<MwModelGSLBCitrixListData> citrixGSLBInfoList = JSONArray.parseArray(JSONObject.toJSONString(citrixRelationInfo), MwModelGSLBCitrixListData.class);
            mwModelGSLBLists = GSLBDataHandle(mParam, citrixGSLBInfoList);
            pageInfo.setTotal(mwModelGSLBLists.size());
            mwModelGSLBDatas = pageList.getList(mwModelGSLBLists, mParam.getPageNumber(), mParam.getPageSize());
            pageInfo.setList(mwModelGSLBDatas);
            return Reply.ok(pageInfo);
        } catch (Throwable e) {
            log.error("fail to getGSLBCitrixRelationList case:{}", e);
            return Reply.fail(500, "获取负载均衡GSLB关联数据失败");
        }
    }


    /**
     * LB关联列表数据处理
     *
     * @param mParam
     * @param citrixLBInfoList
     * @return
     */
    public List<MwModelLBCitrixListData> LBDataHandle(MwQueryCitrixParam mParam, List<MwModelLBCitrixListData> citrixLBInfoList) {
        List<MwModelLBCitrixListData> mwModelCitrixLists = new ArrayList<>();
        if (!Strings.isNullOrEmpty(mParam.getSearchName())) {
            mwModelCitrixLists = citrixLBInfoList.stream().filter(s -> s.getName().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelLBCitrixListData> list1 = citrixLBInfoList.stream().filter(s -> s.getVirtualServerName().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelLBCitrixListData> list2 = citrixLBInfoList.stream().filter(s -> s.getVirtualServiceName().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelLBCitrixListData> list3 = citrixLBInfoList.stream().filter(s -> s.getServiceIpPort().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelLBCitrixListData> list4 = citrixLBInfoList.stream().filter(s -> s.getVirtualServerIpPort().contains(mParam.getSearchName())).collect(Collectors.toList());
            mwModelCitrixLists.addAll(list1);
            mwModelCitrixLists.addAll(list2);
            mwModelCitrixLists.addAll(list3);
            mwModelCitrixLists.addAll(list4);
            //去重
            mwModelCitrixLists = mwModelCitrixLists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getName()
                    + ";" + s.getVirtualServerName() + ";" + s.getVirtualServiceName() + ";" + s.getServiceIpPort() + ";" + s.getVirtualServerIpPort()))), ArrayList::new));
        } else {
            mwModelCitrixLists = citrixLBInfoList;
        }
        mwModelCitrixLists = mwModelCitrixLists.stream().sorted(Comparator.comparing(MwModelLBCitrixListData::getName).
                thenComparing(MwModelLBCitrixListData::getVirtualServerName).
                thenComparing(MwModelLBCitrixListData::getVirtualServerIpPort).
                thenComparing(MwModelLBCitrixListData::getVirtualServiceName).
                thenComparing(MwModelLBCitrixListData::getServiceIpPort).reversed()).collect(Collectors.toList());
        return mwModelCitrixLists;
    }

    /**
     * GSLB关联列表数据处理
     *
     * @param mParam
     * @param citrixGSLBInfoList
     * @return
     */
    public List<MwModelGSLBCitrixListData> GSLBDataHandle(MwQueryCitrixParam mParam, List<MwModelGSLBCitrixListData> citrixGSLBInfoList) {
        List<MwModelGSLBCitrixListData> mwModelGSLBLists = new ArrayList<>();
        if (!Strings.isNullOrEmpty(mParam.getSearchName())) {
            mwModelGSLBLists = citrixGSLBInfoList.stream().filter(s -> s.getVirtualServerName().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelGSLBCitrixListData> list1 = citrixGSLBInfoList.stream().filter(s -> s.getServicename().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelGSLBCitrixListData> list2 = citrixGSLBInfoList.stream().filter(s -> s.getDomainname().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelGSLBCitrixListData> list3 = citrixGSLBInfoList.stream().filter(s -> s.getServiceIp().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelGSLBCitrixListData> list4 = citrixGSLBInfoList.stream().filter(s -> s.getServicetype().contains(mParam.getSearchName())).collect(Collectors.toList());
            List<MwModelGSLBCitrixListData> list5 = citrixGSLBInfoList.stream().filter(s -> s.getServicePort().contains(mParam.getSearchName())).collect(Collectors.toList());
            mwModelGSLBLists.addAll(list1);
            mwModelGSLBLists.addAll(list2);
            mwModelGSLBLists.addAll(list3);
            mwModelGSLBLists.addAll(list4);
            mwModelGSLBLists.addAll(list5);
            //去重
            mwModelGSLBLists = mwModelGSLBLists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getVirtualServerName()
                    + ";" + s.getServicename() + ";" + s.getDomainname() + ";" + s.getServiceIp() + ";" + s.getServicetype() + ";" + s.getServicePort()))), ArrayList::new));
        } else {
            mwModelGSLBLists = citrixGSLBInfoList;
        }
        //排序
        mwModelGSLBLists = mwModelGSLBLists.stream().sorted(Comparator.comparing(MwModelGSLBCitrixListData::getVirtualServerName).
                thenComparing(MwModelGSLBCitrixListData::getServicename).
                thenComparing(MwModelGSLBCitrixListData::getServicetype).
                thenComparing(MwModelGSLBCitrixListData::getServicePort).
                thenComparing(MwModelGSLBCitrixListData::getDomainname).reversed()).collect(Collectors.toList());
        return mwModelGSLBLists;
    }

    public List<MwModelCitrixInfoParam> syncSaveGSLBRelationList(MwQueryCitrixParam mParam, Map<String, List<MwModelCitrixInfoParam>> mapAll) {
        //获取登录信息
        MwModelCitrixLoginParam param = getLoginInfo(mParam);
        //通过APi获取GSLB Virtual Servers和GSLB service关联关系
        param.setType(GSLBVSERVER_GSLBSERVICE_BINDING);
        //是否是关联关系查询
        param.setIsRelationQuery(true);
        param.setInstanceId(mParam.getModelInstanceId());
        String gslbVServerServiceInfo = getCitrixDataInfo(param);
        log.info("citrix 模型gslbvserver_gslbservice_binding：" + gslbVServerServiceInfo);
        JSONObject gslbvserverServiceJson = JSONObject.parseObject(gslbVServerServiceInfo != null ? gslbVServerServiceInfo : "");
        List<MwModelCitrixRelationParam> gslbvserverServiceList = new ArrayList<>();
        if (gslbvserverServiceJson != null && gslbvserverServiceJson.get(param.getType()) != null) {
            gslbvserverServiceList = JSONArray.parseArray(gslbvserverServiceJson.get(param.getType()).toString(), MwModelCitrixRelationParam.class);
        }

        //通过APi获取GSLB Domain和GSLB service关联关系
        param.setType(GSLBDOMAIN_GSLBSERVICE_BINDING);
        //是否是关联关系查询
        param.setIsRelationQuery(true);
        String gslbDomainServiceInfo = getCitrixDataInfo(param);
        log.info("citrix 模型gslbdomain_gslbservice_binding：" + gslbDomainServiceInfo);
        JSONObject gslbDomainServiceJson = JSONObject.parseObject(gslbDomainServiceInfo != null ? gslbDomainServiceInfo : "");
        List<MwModelCitrixRelationParam> gslbDomainServiceList = new ArrayList<>();
        if (gslbDomainServiceJson != null && gslbDomainServiceJson.get(param.getType()) != null) {
            gslbDomainServiceList = JSONArray.parseArray(gslbDomainServiceJson.get(param.getType()).toString(), MwModelCitrixRelationParam.class);
        }

        MwModelCitrixInfoParam mwModelGSLBCitrixListData;
        List<MwModelCitrixInfoParam> mwModelGSLBDatas = new ArrayList<>();
        List<MwModelInfoDTO> modelInfo = mwModelCitrixDao.getModelIndexByName();
        Map<String, String> m = new HashMap();
        for (MwModelInfoDTO mwModelInfoDTO : modelInfo) {
            m.put(mwModelInfoDTO.getModelName(), mwModelInfoDTO.getModelIndex());
        }
        //获取 GSLB Virtual Servers实例数据信息
        List<MwModelCitrixInfoParam> citrixInfoByGSLBvserverList = mapAll.get(m.get(ModelCitrixType.GSLB_VIRTUAL_SERVERS.getType()));

        List<MwModelCitrixInfoParam> citrixInfoByGSLBserviceList = mapAll.get(m.get(ModelCitrixType.GSLB_SERVICES.getType()));

        //将实例名称当成key值，转为map数据
        Map<String, List<MwModelCitrixInfoParam>> modelInfoByName = null;
        if (CollectionUtils.isNotEmpty(citrixInfoByGSLBserviceList)) {
            modelInfoByName = citrixInfoByGSLBserviceList.stream().collect(Collectors.groupingBy((MwModelCitrixInfoParam h) -> h.getInstanceName()));
        }

        //将ES中的GSLBVirtualServers实例数据和 GSLBVirtualServers、GSLB service关联关系进行循环匹配
        if (CollectionUtils.isNotEmpty(gslbvserverServiceList)) {
            for (MwModelCitrixInfoParam param1 : citrixInfoByGSLBvserverList) {
                mwModelGSLBCitrixListData = new MwModelCitrixInfoParam();
                String name = "";
                if (!Strings.isNullOrEmpty(param1.getName())) {
                    name = param1.getName();
                }
                mwModelGSLBCitrixListData.setVirtualServerName(name);
                for (MwModelCitrixRelationParam relationParam : gslbvserverServiceList) {
                    if (name.equals(relationParam.getName())) {
                        mwModelGSLBCitrixListData = new MwModelCitrixInfoParam();
                        mwModelGSLBCitrixListData.setVirtualServerName(name);
                        mwModelGSLBCitrixListData.setServicename(relationParam.getServicename());
                        MwModelCitrixInfoParam modelInfoMap = new MwModelCitrixInfoParam();
                        if (modelInfoByName != null && modelInfoByName.get(relationParam.getServicename()) != null && modelInfoByName.get(relationParam.getServicename()).size() > 0) {
                            modelInfoMap = modelInfoByName.get(relationParam.getServicename()).get(0);
                            mwModelGSLBCitrixListData.setServicetype(modelInfoMap.getServicetype());
                            mwModelGSLBCitrixListData.setServicePort(modelInfoMap.getPort().toString());
                            mwModelGSLBCitrixListData.setServiceIp(modelInfoMap.getIpaddress());
                        }
                    } else {
                        continue;
                    }
                    mwModelGSLBDatas.add(mwModelGSLBCitrixListData);
                }
            }
        }
        for (MwModelCitrixInfoParam data : mwModelGSLBDatas) {
            //GSLBService不为空，则匹配关联的domain数据
            if (!Strings.isNullOrEmpty(data.getServicename())) {
                for (MwModelCitrixRelationParam relationParam : gslbDomainServiceList) {
                    //GSLBService名称和关联关系数据中名称相同，获取domain数据
                    if (data.getServicename().equals(relationParam.getServicename())) {
                        data.setDomainname(relationParam.getName());
                    }
                }
            }
        }
        return mwModelGSLBDatas;
    }

    /**
     * 获取lbvserver、server、service关系数据
     *
     * @param mParam
     * @return
     * @throws Exception
     */
    public List<MwModelCitrixInfoParam> getRelationDataByLBList(MwQueryCitrixParam mParam) throws Exception {
        //获取登录信息
        MwModelCitrixLoginParam param = getLoginInfo(mParam);
        //通过APi获取LB Virtual Servers和service关联关系
        param.setType(LBVSERVER_SERVICE_BINDING);
        param.setIsRelationQuery(true);
        param.setInstanceId(mParam.getModelInstanceId());
        String lbvserverServiceInfo = getCitrixDataInfo(param);
        log.info("citrix 模型lbvserver_service_binding：" + lbvserverServiceInfo);
        JSONObject lbvserverServiceJson = JSONObject.parseObject(lbvserverServiceInfo != null ? lbvserverServiceInfo : "");
        List<MwModelCitrixRelationParam> lbvserverServiceList = new ArrayList<>();
        if (lbvserverServiceJson != null && lbvserverServiceJson.get(param.getType()) != null) {
            lbvserverServiceList = JSONArray.parseArray(lbvserverServiceJson.get(param.getType()).toString(), MwModelCitrixRelationParam.class);
        }
        //通过APi获取LB Server和service关联关系
        param.setType(SERVER_SERVICE_BINDING);
        param.setIsRelationQuery(true);
        String lbServerServiceInfo = getCitrixDataInfo(param);
        log.info("citrix 模型server_service_binding:" + lbServerServiceInfo);
        JSONObject lbServerServiceJson = JSONObject.parseObject(lbServerServiceInfo != null ? lbServerServiceInfo : "");
        List<MwModelCitrixRelationParam> lbServerServiceList = new ArrayList<>();
        if (lbServerServiceJson != null && lbServerServiceJson.get(param.getType()) != null) {
            lbServerServiceList = JSONArray.parseArray(lbServerServiceJson.get(param.getType()).toString(), MwModelCitrixRelationParam.class);
        }


        MwModelCitrixInfoParam mwModelCitrixListData;
        List<MwModelCitrixInfoParam> mwModelCitrixListDatas = new ArrayList<>();
        List<MwModelInfoDTO> modelInfo = mwModelCitrixDao.getModelIndexByName();
        Map<String, String> m = new HashMap();
        for (MwModelInfoDTO mwModelInfoDTO : modelInfo) {
            m.put(mwModelInfoDTO.getModelName(), mwModelInfoDTO.getModelIndex());
        }
        //通过ES获取LB Virtual Servers模型数据、LB service模型数据

        QueryInstanceModelParam instanceParam = new QueryInstanceModelParam();
        List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
        instanceParam.setModelIndexs(Arrays.asList(m.get(ModelCitrixType.LB_VIRTUAL_SERVERS.getType())));
        instanceParam.setPropertiesList(propertiesList);
        instanceParam.setSkipDataPermission(mParam.getIsTimeTask());
        AddModelInstancePropertiesParam instancePropertiesParam = new AddModelInstancePropertiesParam();
        instancePropertiesParam.setPropertiesIndexId("relationInstanceId");
        instancePropertiesParam.setPropertiesValue(mParam.getRelationInstanceId() + "");
        instancePropertiesParam.setPropertiesType(ModelPropertiesType.INTEGER.getCode());
        propertiesList.add(instancePropertiesParam);
        instanceParam.setPropertiesList(propertiesList);
        instanceParam.setPageSize(pageSize);
        //获取LB Virtual Servers实例数据信息
        List<Map<String, Object>> LBVirtualServersInfo = mwModelInstanceServiceImpl.getInstanceInfoByPropertiesValue(instanceParam);

        instanceParam.setModelIndexs(Arrays.asList(m.get(ModelCitrixType.LB_SERVICES.getType())));
        //获取LB service实例数据信息
        List<Map<String, Object>> LBServiceInfo = mwModelInstanceServiceImpl.getInstanceInfoByPropertiesValue(instanceParam);
        //将实例名称当成key值，转为map数据
        Map<String, List<Map<String, Object>>> modelInfoByName = LBServiceInfo.stream().collect(Collectors.groupingBy((Map h) -> h.get(MwModelViewCommonService.INSTANCE_NAME_KEY).toString()));
        List<MwModelCitrixInfoParam> citrixInfoList = JSONArray.parseArray(JSONObject.toJSONString(LBVirtualServersInfo), MwModelCitrixInfoParam.class);

        if (CollectionUtils.isNotEmpty(lbvserverServiceList)) {
            for (MwModelCitrixInfoParam param1 : citrixInfoList) {
                mwModelCitrixListData = new MwModelCitrixInfoParam();
                String name = "";
                if (!Strings.isNullOrEmpty(param1.getName())) {
                    name = param1.getName();
                }
                //获取名称 VS_A_运营商X_端口1，将第二个内容A 作为应用名称使用。
                if (name.split("_").length > 3) {
                    mwModelCitrixListData.setName(name.split("_")[1]);
                } else {
                    mwModelCitrixListData.setName(name);
                }
                mwModelCitrixListData.setVirtualServerName(name);
                mwModelCitrixListData.setVirtualServerIpPort(param1.getIpv46() + ":" + param1.getPort());

                for (MwModelCitrixRelationParam relationParam : lbvserverServiceList) {
                    if (name.equals(relationParam.getName())) {
                        mwModelCitrixListData = new MwModelCitrixInfoParam();
                        if (name.split("_").length > 3) {
                            mwModelCitrixListData.setName(name.split("_")[1]);
                        } else {
                            mwModelCitrixListData.setName(name);
                        }
                        mwModelCitrixListData.setVirtualServerName(name);
                        mwModelCitrixListData.setVirtualServerIpPort(param1.getIpv46() + ":" + param1.getPort());
                        mwModelCitrixListData.setVirtualServiceName(relationParam.getServicename());

                        Map<String, Object> modelInfoMap = new HashMap<>();
                        if (modelInfoByName != null && modelInfoByName.get(relationParam.getServicename()) != null && modelInfoByName.get(relationParam.getServicename()).size() > 0) {
                            modelInfoMap = modelInfoByName.get(relationParam.getServicename()).get(0);
                            mwModelCitrixListData.setServiceIpPort(modelInfoMap.get("ipaddress") + ":" + modelInfoMap.get("port"));
                        }
                    } else {
                        continue;
                    }
                    mwModelCitrixListDatas.add(mwModelCitrixListData);
                }
            }
        }
        return mwModelCitrixListDatas;
    }

    public Map<String, Map<String, List<MwModelCitrixRelationParam>>> getLBRelationList(MwQueryCitrixParam mParam) {
        Map<String, Map<String, List<MwModelCitrixRelationParam>>> m = new HashMap();
        List<MwModelInfoDTO> modelInfo = mwModelCitrixDao.getModelIndexByName();
        List<MwModelInstanceParam> instanceInfo = mwModelCitrixDao.getModelInstanceInfoByName(citrixModelId, mParam.getModelInstanceId());
        Map<String, MwModelInstanceParam> mInstanceMap = instanceInfo.stream().collect(Collectors.toMap(s -> s.getModelIndex() + "_" + s.getInstanceName(), s -> s));
        Map<String, String> mInfo = new HashMap();
        for (MwModelInfoDTO mwModelInfoDTO : modelInfo) {
            mInfo.put(mwModelInfoDTO.getModelName(), mwModelInfoDTO.getModelIndex());
        }
        //获取登录信息
        MwModelCitrixLoginParam param = getLoginInfo(mParam);
        //通过APi获取LB Virtual Servers和service关联关系
        param.setType(LBVSERVER_SERVICE_BINDING);
        param.setIsRelationQuery(true);
        param.setInstanceId(mParam.getModelInstanceId());
        String lbvserverServiceInfo = getCitrixDataInfo(param);
        log.info("citrix 模型lbvserver_service_binding：" + lbvserverServiceInfo);
        JSONObject lbvserverServiceJson = JSONObject.parseObject(lbvserverServiceInfo != null ? lbvserverServiceInfo : "");
        List<MwModelCitrixRelationParam> lbvserverServiceList = new ArrayList<>();
        if (lbvserverServiceJson != null && lbvserverServiceJson.get(param.getType()) != null) {
            lbvserverServiceList = JSONArray.parseArray(lbvserverServiceJson.get(param.getType()).toString(), MwModelCitrixRelationParam.class);
        }
        String servicesModelIndex = mInfo.get(LB_SERVICES.getType());
        String lbVServerModelIndex = mInfo.get(LB_VIRTUAL_SERVERS.getType());
        List<MwModelCitrixRelationParam> lbvserverServiceListDis = new ArrayList<>();
        //给name和Servicename赋上实例Id
        for (MwModelCitrixRelationParam param1 : lbvserverServiceList) {
            MwModelInstanceParam lbVServerInstanceParam = mInstanceMap.get(lbVServerModelIndex + "_" + param1.getName());
            MwModelInstanceParam serviceInstnaceParam = mInstanceMap.get(servicesModelIndex + "_" + param1.getServicename());
            if (lbVServerInstanceParam != null && serviceInstnaceParam != null) {
                param1.setNameInstanceId(lbVServerInstanceParam.getInstanceId());
                param1.setNameModeleId(Integer.valueOf(lbVServerInstanceParam.getModelId()));
                param1.setServiceNameInstanceId(serviceInstnaceParam.getInstanceId());
                param1.setServiceNameModelId(Integer.valueOf(serviceInstnaceParam.getModelId()));
                lbvserverServiceListDis.add(param1);
            }

        }
        //Servicename为key，获取对应的vserver连接数据
        Map<String, List<MwModelCitrixRelationParam>> maps = lbvserverServiceListDis.stream().collect(Collectors.groupingBy(MwModelCitrixRelationParam::getName));
        m.put(lbVServerModelIndex, maps);

        //通过APi获取Servers和service关联关系
        param.setType(SERVER_SERVICE_BINDING);
        param.setIsRelationQuery(true);
        String lbServerServiceInfo = getCitrixDataInfo(param);
        log.info("citrix 模型server_service_binding:" + lbServerServiceInfo);
        JSONObject lbServerServiceJson = JSONObject.parseObject(lbServerServiceInfo != null ? lbServerServiceInfo : "");
        List<MwModelCitrixRelationParam> lbServerServiceList = new ArrayList<>();
        if (lbServerServiceJson != null && lbServerServiceJson.get(param.getType()) != null) {
            lbServerServiceList = JSONArray.parseArray(lbServerServiceJson.get(param.getType()).toString(), MwModelCitrixRelationParam.class);
        }
        String serverModelIndex = mInfo.get(LB_SERVER.getType());
        List<MwModelCitrixRelationParam> lbServerServiceListDis = new ArrayList<>();
        //给name和Servicename赋上实例Id
        for (MwModelCitrixRelationParam param1 : lbServerServiceList) {
            MwModelInstanceParam serverInstnaceParam = mInstanceMap.get(serverModelIndex + "_" + param1.getName());
            MwModelInstanceParam servicesInstnaceParam = mInstanceMap.get(servicesModelIndex + "_" + param1.getServicename());
            if (serverInstnaceParam != null && servicesInstnaceParam != null) {
                param1.setNameInstanceId(serverInstnaceParam.getInstanceId());
                param1.setNameModeleId(Integer.valueOf(serverInstnaceParam.getModelId()));
                param1.setServiceNameInstanceId(servicesInstnaceParam.getInstanceId());
                param1.setServiceNameModelId(Integer.valueOf(servicesInstnaceParam.getModelId()));
                lbServerServiceListDis.add(param1);
            }
        }
        //Serverame为key，获取对应的service连接数据
        Map<String, List<MwModelCitrixRelationParam>> maps2 = lbServerServiceListDis.stream().collect(Collectors.groupingBy(MwModelCitrixRelationParam::getName));
        m.put(serverModelIndex, maps2);
        return m;
    }


    public List<MwModelCitrixInfoParam> syncSaveLBRelationList(MwQueryCitrixParam mParam, Map<String, List<MwModelCitrixInfoParam>> mapAll) throws Exception {
        //获取登录信息
        MwModelCitrixLoginParam param = getLoginInfo(mParam);
        //通过APi获取LB Virtual Servers和service关联关系
        param.setType(LBVSERVER_SERVICE_BINDING);
        param.setIsRelationQuery(true);
        param.setInstanceId(mParam.getModelInstanceId());
        String lbvserverServiceInfo = getCitrixDataInfo(param);
        log.info("citrix 模型lbvserver_service_binding：" + lbvserverServiceInfo);
        JSONObject lbvserverServiceJson = JSONObject.parseObject(lbvserverServiceInfo != null ? lbvserverServiceInfo : "");
        List<MwModelCitrixRelationParam> lbvserverServiceList = new ArrayList<>();
        if (lbvserverServiceJson != null && lbvserverServiceJson.get(param.getType()) != null) {
            lbvserverServiceList = JSONArray.parseArray(lbvserverServiceJson.get(param.getType()).toString(), MwModelCitrixRelationParam.class);
        }
        MwModelCitrixInfoParam mwModelCitrixListData;
        List<MwModelCitrixInfoParam> mwModelCitrixListDatas = new ArrayList<>();
        List<MwModelInfoDTO> modelInfo = mwModelCitrixDao.getModelIndexByName();
        Map<String, String> m = new HashMap();
        for (MwModelInfoDTO mwModelInfoDTO : modelInfo) {
            m.put(mwModelInfoDTO.getModelName(), mwModelInfoDTO.getModelIndex());
        }
        List<MwModelCitrixInfoParam> citrixInfoList = mapAll.get(m.get(ModelCitrixType.LB_VIRTUAL_SERVERS.getType()));
        List<MwModelCitrixInfoParam> citrixInfoByLBServiceList = mapAll.get(m.get(ModelCitrixType.LB_SERVICES.getType()));
        //将实例名称当成key值，转为map数据
        Map<String, List<MwModelCitrixInfoParam>> modelInfoByName = citrixInfoByLBServiceList.stream().collect(Collectors.groupingBy((MwModelCitrixInfoParam h) -> h.getInstanceName()));
        if (CollectionUtils.isNotEmpty(lbvserverServiceList)) {
            for (MwModelCitrixInfoParam param1 : citrixInfoList) {
                mwModelCitrixListData = new MwModelCitrixInfoParam();
                String name = "";
                if (!Strings.isNullOrEmpty(param1.getName())) {
                    name = param1.getName();
                }
                //获取名称 VS_A_运营商X_端口1，将第二个内容A 作为应用名称使用。
                if (name.split("_").length > 3) {
                    mwModelCitrixListData.setName(name.split("_")[1]);
                } else {
                    mwModelCitrixListData.setName(name);
                }
                mwModelCitrixListData.setVirtualServerName(name);
                mwModelCitrixListData.setVirtualServerIpPort(param1.getIpv46() + ":" + param1.getPort());

                for (MwModelCitrixRelationParam relationParam : lbvserverServiceList) {
                    if (name.equals(relationParam.getName())) {
                        mwModelCitrixListData = new MwModelCitrixInfoParam();
                        if (name.split("_").length > 3) {
                            mwModelCitrixListData.setName(name.split("_")[1]);
                        } else {
                            mwModelCitrixListData.setName(name);
                        }
                        mwModelCitrixListData.setVirtualServerName(name);
                        mwModelCitrixListData.setVirtualServerIpPort(param1.getIpv46() + ":" + param1.getPort());
                        mwModelCitrixListData.setVirtualServiceName(relationParam.getServicename());

                        MwModelCitrixInfoParam modelInfoMap = new MwModelCitrixInfoParam();
                        if (modelInfoByName != null && modelInfoByName.get(relationParam.getServicename()) != null && modelInfoByName.get(relationParam.getServicename()).size() > 0) {
                            modelInfoMap = modelInfoByName.get(relationParam.getServicename()).get(0);
                            mwModelCitrixListData.setServiceIpPort(modelInfoMap.getIpaddress() + ":" + modelInfoMap.getPort());
                        }
                    } else {
                        continue;
                    }
                    mwModelCitrixListDatas.add(mwModelCitrixListData);
                }
            }
        }
        return mwModelCitrixListDatas;
    }


    @Override
    public Reply exportCitrixRelationList(MwQueryCitrixParam param, HttpServletRequest request, HttpServletResponse response) {
        try {
            List<Map> mapList = new ArrayList<>();
            //mergeIndex：需要合并的列的序号数,不指定默认所有列都进行合并
            //生成和表头字段长度相对应列的序列号数[0,1,2,3,4,5,...]
            List mergeIndex = IntStream.rangeClosed(0, param.getHeader().size() - 1).boxed().collect(Collectors.toList());
            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
            qParam.setModelIndex(param.getModelIndex());
            List<MwModelGSLBCitrixListData> mwModelGSLBLists = new ArrayList<>();
            List<MwModelLBCitrixListData> mwModelCitrixLists = new ArrayList<>();
            List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
            AddModelInstancePropertiesParam instancePropertiesParam = new AddModelInstancePropertiesParam();
            instancePropertiesParam.setPropertiesIndexId("relationInstanceId");
            instancePropertiesParam.setPropertiesValue(param.getRelationInstanceId() + "");
            instancePropertiesParam.setPropertiesType(ModelPropertiesType.INTEGER.getCode());
            propertiesList.add(instancePropertiesParam);
            qParam.setPropertiesList(propertiesList);
            List<Map<String, Object>> citrixRelationInfo = mwModelInstanceService.getInstanceInfoByExport(qParam);
            if (param.getIsGSLBQuery() != null && param.getIsGSLBQuery()) {
                List<MwModelGSLBCitrixListData> citrixGSLBInfoList = JSONArray.parseArray(JSONObject.toJSONString(citrixRelationInfo), MwModelGSLBCitrixListData.class);
                mwModelGSLBLists = GSLBDataHandle(param, citrixGSLBInfoList);
                mapList = objectsToMaps(mwModelGSLBLists);
            } else {
                List<MwModelLBCitrixListData> citrixLBInfoList = JSONArray.parseArray(JSONObject.toJSONString(citrixRelationInfo), MwModelLBCitrixListData.class);
                mwModelCitrixLists = LBDataHandle(param, citrixLBInfoList);
                mapList = objectsToMaps(mwModelCitrixLists);
            }
            List<String> lable = param.getHeader();
            List<String> lableName = param.getHeaderName();
            ExcelMergeUtils.exportToExcelForXlsx("列表导出", "列表导出", lableName, lable, mapList, mergeIndex, response);
        } catch (Exception e) {
            log.error("exportCitrixRelationList{}", e);
        }
        return Reply.ok("导出成功");
    }

    private MwModelCitrixLoginParam getLoginInfo(MwQueryCitrixParam mParam) {
        QueryModelInstanceParam queryModelInstanceParam = new QueryModelInstanceParam();
        TransferUtils.transferBean(mParam, queryModelInstanceParam);
        //根据VCenter实例获取es数据信息
        List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(queryModelInstanceParam);
        //获取虚拟化VCenter的连接信息，URL、用户名、密码
        String userName = "";
        String url = "";
        String password = "";
        String port = "";
        String modelId = "0";
        List<MwModelMacrosParam> macrosParams = JSON.parseArray(JSONObject.toJSONString(listInfo), MwModelMacrosParam.class);
        for (MwModelMacrosParam m : macrosParams) {
            userName = m.getUSERNAME();
            url = m.getHOST();
            if (m.getPASSWORD().length() == 172) {
                password = RSAUtils.decryptData(m.getPASSWORD(), RSAUtils.RSA_PRIVATE_KEY);
            } else {
                password = m.getPASSWORD();
            }
            port = m.getPORT();
            modelId = m.getModelId() != null ? m.getModelId().toString() : "0";
        }
        log.info("citrix 登录信息：HOST:" + url + ";USERNAME:" + userName);
        MwModelCitrixLoginParam param = new MwModelCitrixLoginParam();
        param.setUserName(userName);
        param.setUrl(url);
        param.setPassword(password);
        param.setPort(port);
        param.setModelId(Integer.valueOf(modelId));
        return param;
    }

    private void addInstanceToModel(MwModelCitrixLoginParam param, Integer relationInstanceId, String queryModelName,
                                    List<AddAndUpdateModelInstanceParam> instanceInfoList, List<String> modelIndexs,
                                    Map<String, List<MwModelCitrixInfoParam>> mapAll) {
        //通过api获取对应的Citrix数据
        param.setInstanceId(relationInstanceId);
        String strInfo = getCitrixDataInfo(param);
        log.info("获取的返回数据::" + strInfo);
        JSONObject strInfoJson = JSONObject.parseObject(strInfo != null ? strInfo : "");
        log.info("citrix 模型11111111111：" + strInfoJson);
        List<MwModelCitrixInfoParam> infoList = new ArrayList<>();
        if (strInfoJson != null && strInfoJson.get(param.getType()) != null) {
            infoList = JSONArray.parseArray(strInfoJson.get(param.getType()).toString(), MwModelCitrixInfoParam.class);
        }
        //获取模型属性
        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();

        List<ModelInfo> modelInfos = mwModelManageDao.selectModelInfoWithParent(queryModelName);
        List<PropertyInfo> propertyInfoLists = new ArrayList<>();
        String modelIndex = "";
        Integer modelId = 0;
        String modelName = "";
        for (ModelInfo modelInfo : modelInfos) {
            propertyInfoLists.addAll(modelInfo.getPropertyInfos());
            if (queryModelName.equals(modelInfo.getModelName())) {
                modelIndex = modelInfo.getModelIndex();
                modelId = modelInfo.getModelId();
                modelName = modelInfo.getModelName();
            }
        }
        for (MwModelCitrixInfoParam citrixInfoParam : infoList) {
            citrixInfoParam.setModelId(modelId + "");
            citrixInfoParam.setModelIndex(modelIndex);
            citrixInfoParam.setInstanceName(Strings.isNullOrEmpty(citrixInfoParam.getName()) ? citrixInfoParam.getServicename() : citrixInfoParam.getName());
            ;
        }
        //将数据放入MapAll中，方便后面调用
        Map<String, List<MwModelCitrixInfoParam>> map = new HashMap();
        map.put(modelIndex, infoList);
        mapAll.putAll(map);
        for (PropertyInfo propertyInfo : propertyInfoLists) {
            AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
            addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
            addModelInstancePropertiesParam.setModelId(modelId);
            addModelInstancePropertiesParam.setModelIndex(modelIndex);
            addModelInstancePropertiesParam.setModelName(modelName);
            propertiesParamList.add(addModelInstancePropertiesParam);
        }
        //虚拟化设备的type和模型的name相同，则该设备加入到模型中
        if (propertiesParamList != null && propertiesParamList.size() > 0) {
            modelIndex = propertiesParamList.get(0).getModelIndex();
            modelIndexs.add(modelIndex);
        }
        for (MwModelCitrixInfoParam info : infoList) {
            Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(info));
            //通过匹配模型名称，获取对应的模型数据
            List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
            if (propertiesParamList != null && propertiesParamList.size() > 0) {
                for (AddModelInstancePropertiesParam p : propertiesParamList) {
                    AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                    TransferUtils.transferBean(p, instanceParam);
                    //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                    instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                    if (instanceParam.getPropertiesIndexId().equals(MwModelViewCommonService.INSTANCE_NAME_KEY)) {
                        instanceParam.setPropertiesValue(Strings.isNullOrEmpty(info.getName()) ? info.getServicename() : info.getName());
                    }
                    if (instanceParam.getPropertiesIndexId().equals(MwModelViewCommonService.RELATION_INSTANCE_ID)) {
                        instanceParam.setPropertiesValue(relationInstanceId != null ? relationInstanceId.toString() : "0");
                    }
                    modelIndex = instanceParam.getModelIndex();
                    modelId = instanceParam.getModelId();
                    modelName = instanceParam.getModelName();
                    propertiesParamLists.add(instanceParam);
                }
            }
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            instanceParam.setModelIndex(modelIndex);
            instanceParam.setModelId(modelId);
            instanceParam.setModelName(modelName);
            instanceParam.setInstanceName(Strings.isNullOrEmpty(info.getName()) ? info.getServicename() : info.getName());
            instanceParam.setRelationInstanceId(relationInstanceId);
            instanceParam.setPropertiesList(propertiesParamLists);
            if ((!Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                instanceInfoList.add(instanceParam);
            }
        }
    }

    public void saveAllInfo(List<AddAndUpdateModelInstanceParam> instanceInfoList, List<String> modelIndexs, Integer relationInstanceId) throws Exception {
        //清空之前的数据
        DeleteModelInstanceParam deleteParam = new DeleteModelInstanceParam();
        deleteParam.setModelIndexs(modelIndexs);
        deleteParam.setRelationInstanceId(relationInstanceId);
        mwModelInstanceService.batchDeleteInstanceInfo(deleteParam);
        if (instanceInfoList != null && instanceInfoList.size() > 0) {
            mwModelInstanceService.saveData(instanceInfoList, true, true);
        }
    }

    /**
     * @param infoList
     * @param relationInstanceId 关联实例Id
     * @param queryModelName
     */
    private void addInstanceToModel(List<MwModelCitrixInfoParam> infoList, Integer relationInstanceId, String queryModelName,
                                    List<AddAndUpdateModelInstanceParam> instanceInfoList, List<String> modelIndexs) {
        //获取模型属性
        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();

        List<ModelInfo> modelInfos = mwModelManageDao.selectModelInfoWithParent(queryModelName);
        List<PropertyInfo> propertyInfoLists = new ArrayList<>();
        String modelIndex = "";
        Integer modelId = 0;
        String modelName = "";
        for (ModelInfo modelInfo : modelInfos) {
            propertyInfoLists.addAll(modelInfo.getPropertyInfos());
            modelIndex = modelInfo.getModelIndex();
            modelId = modelInfo.getModelId();
            modelName = modelInfo.getModelName();
        }
        for (PropertyInfo propertyInfo : propertyInfoLists) {
            AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
            addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
            addModelInstancePropertiesParam.setModelId(modelId);
            addModelInstancePropertiesParam.setModelIndex(modelIndex);
            addModelInstancePropertiesParam.setModelName(modelName);
            propertiesParamList.add(addModelInstancePropertiesParam);
        }
        //循环获取到的设备
        //设备的type和模型的name相同，则该设备加入到模型中
        if (propertiesParamList != null && propertiesParamList.size() > 0) {
            modelIndex = propertiesParamList.get(0).getModelIndex();
            modelIndexs.add(modelIndex);
        }
        for (MwModelCitrixInfoParam info : infoList) {
            Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(info));
            //通过匹配模型名称，获取对应的模型数据
            List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();

            if (propertiesParamList != null && propertiesParamList.size() > 0) {
                for (AddModelInstancePropertiesParam p : propertiesParamList) {
                    AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                    TransferUtils.transferBean(p, instanceParam);
                    //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                    instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                    if (instanceParam.getPropertiesIndexId().equals(MwModelViewCommonService.INSTANCE_NAME_KEY)) {
                        instanceParam.setPropertiesValue(Strings.isNullOrEmpty(info.getName()) ? info.getServicename() : info.getName());
                    }
                    if (instanceParam.getPropertiesIndexId().equals(MwModelViewCommonService.RELATION_INSTANCE_ID)) {
                        instanceParam.setPropertiesValue(relationInstanceId != null ? relationInstanceId.toString() : "0");
                    }
                    modelIndex = instanceParam.getModelIndex();
                    modelId = instanceParam.getModelId();
                    modelName = instanceParam.getModelName();
                    propertiesParamLists.add(instanceParam);
                }
            }
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            instanceParam.setModelIndex(modelIndex);
            instanceParam.setModelId(modelId);
            instanceParam.setModelName(modelName);
            instanceParam.setInstanceName(Strings.isNullOrEmpty(info.getName()) ? info.getServicename() : info.getName());
            instanceParam.setRelationInstanceId(relationInstanceId);
            instanceParam.setPropertiesList(propertiesParamLists);
            if ((!Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                instanceInfoList.add(instanceParam);
            }
        }
    }

    private String getCitrixDataInfo(MwModelCitrixLoginParam param) {
        String respContent = "";
        String url = param.getUrl();
        String userName = param.getUserName();
        String password = param.getPassword();
        String port = param.getPort();
        String type = param.getType();
        try {
            //查询代理服务器
            List<ProxyInfo> proxyInfos = new ArrayList<>();
            respContent = proxySearch.doProxySearch(String.class, proxyInfos, param.getInstanceId()
                    , "mwCitrixService", "getCitrixDataInfo", param, null);
            if (proxyInfos.size() > 0) {
                return respContent;
            }
            HttpGet httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/config/" + type);
            if (param.getIsRelationQuery() != null && param.getIsRelationQuery()) {
                httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/config/" + type + "?bulkbindings=yes");
            }
            log.info("http地址001：" + httpGet);
            httpGet.addHeader("X-NITRO-USER", userName);
            httpGet.addHeader("X-NITRO-PASS", password);
            CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000)
                    .setConnectionRequestTimeout(2000).setSocketTimeout(5000).build();
            httpGet.setConfig(requestConfig);
            HttpResponse resp = client.execute(httpGet);//执行时机
            if (resp.getStatusLine().getStatusCode() == 200) {
                HttpEntity he = resp.getEntity();
                respContent = EntityUtils.toString(he, "UTF-8");
                client.close();
            }
            //再次判断请求，修改api路径
            if (respContent.length() <= 60) {
                httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/stat/" + type);
                if (param.getIsRelationQuery() != null && param.getIsRelationQuery()) {
                    httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/stat/" + type + "?bulkbindings=yes");
                }
                log.info("http地址002：" + httpGet);
                httpGet.addHeader("X-NITRO-USER", userName);
                httpGet.addHeader("X-NITRO-PASS", password);
                httpGet.setConfig(requestConfig);
                CloseableHttpClient client2 = HttpClients.createDefault();
                resp = client2.execute(httpGet);//执行时机
                if (resp.getStatusLine().getStatusCode() == 200) {
                    HttpEntity he = resp.getEntity();
                    respContent = EntityUtils.toString(he, "UTF-8");
                }
                client2.close();
            }
        } catch (Throwable e) {
            log.error("fail to getCitrixDataInfo1 case:{}", e);
            try {
                //再次判断请求，修改api路径
                HttpGet httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/stat/" + type);
                log.info("try again to getCitrixDataInfo1 :{}", httpGet);
                if (param.getIsRelationQuery() != null && param.getIsRelationQuery()) {
                    httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/stat/" + type + "?bulkbindings=yes");
                }
                log.info("http地址002：" + httpGet);
                httpGet.addHeader("X-NITRO-USER", userName);
                httpGet.addHeader("X-NITRO-PASS", password);
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000)
                        .setConnectionRequestTimeout(2000).setSocketTimeout(5000).build();
                httpGet.setConfig(requestConfig);
                CloseableHttpClient client = HttpClients.createDefault();
                HttpResponse resp = null;//执行时机
                resp = client.execute(httpGet);
                if (resp.getStatusLine().getStatusCode() == 200) {
                    HttpEntity he = resp.getEntity();
                    respContent = EntityUtils.toString(he, "UTF-8");
                }
                client.close();
            } catch (IOException ex) {
                log.error("fail to getCitrixDataInfo2 case:{}", e);
            }
        }
        return respContent;
    }

    /**
     * 定时任务同步Citrix数据
     *
     * @return
     */
    @Override
    public TimeTaskRresult getCitrixInfoByTaskTime() {
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try {
            //基础数据
            List<Integer> relationIds1 = new ArrayList<>();
            List<AddAndUpdateModelInstanceParam> instanceInfoList1 = new ArrayList<>();
            List<String> modelIndexList1 = new ArrayList<>();
            DeleteModelInstanceParam deleteParam1 = new DeleteModelInstanceParam();

            //关联数据
            List<Integer> relationIds2 = new ArrayList<>();
            DeleteModelInstanceParam deleteParam2 = new DeleteModelInstanceParam();
            List<String> modelIndexList2 = new ArrayList<>();
            List<AddAndUpdateModelInstanceParam> instanceInfoList2 = new ArrayList<>();
            List<MwModelInfoDTO> citrixList = mwModelCitrixDao.getAllCitrixModelInfo();
            for (MwModelInfoDTO dto : citrixList) {
                List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
                MwQueryCitrixParam mParam = new MwQueryCitrixParam();
                Map<String, List<MwModelCitrixInfoParam>> mapAll = new HashMap();
                mParam.setModelIndex(dto.getModelIndex());
                mParam.setRelationInstanceId(dto.getInstanceId());
                mParam.setModelInstanceId(dto.getInstanceId());
                mParam.setIsTimeTask(true);

                List<String> modelIndexs = new ArrayList<>();
                //获取登录信息
                MwModelCitrixLoginParam param = getLoginInfo(mParam);
                if (Strings.isNullOrEmpty(param.getUrl()) || Strings.isNullOrEmpty(param.getUserName()) || Strings.isNullOrEmpty(param.getPassword())) {
                    continue;
                }
                //通过APi获取LB Virtual Servers
                param.setType(ModelCitrixType.LB_VIRTUAL_SERVERS.getDesc());
                addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.LB_VIRTUAL_SERVERS.getType(), instanceInfoList, modelIndexs, mapAll);
                //获取LB Services
                param.setType(ModelCitrixType.LB_SERVICES.getDesc());
                addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.LB_SERVICES.getType(), instanceInfoList, modelIndexs, mapAll);

                //获取LB Server
                param.setType(LB_SERVER.getDesc());
                addInstanceToModel(param, mParam.getRelationInstanceId(), LB_SERVER.getType(), instanceInfoList, modelIndexs, mapAll);

                //获取GSLB Virtual Servers
                param.setType(ModelCitrixType.GSLB_VIRTUAL_SERVERS.getDesc());
                addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.GSLB_VIRTUAL_SERVERS.getType(), instanceInfoList, modelIndexs, mapAll);

                //获取GSLB Services
                param.setType(ModelCitrixType.GSLB_SERVICES.getDesc());
                addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.GSLB_SERVICES.getType(), instanceInfoList, modelIndexs, mapAll);

                //获取GSLB Domain
                param.setType(ModelCitrixType.GSLB_DOMAIN.getDesc());
                addInstanceToModel(param, mParam.getRelationInstanceId(), ModelCitrixType.GSLB_DOMAIN.getType(), instanceInfoList, modelIndexs, mapAll);

                //LB关联列表数据
                List<MwModelCitrixInfoParam> mwModelCitrixListDatas = syncSaveLBRelationList(mParam, mapAll);
                addInstanceToModel(mwModelCitrixListDatas, mParam.getRelationInstanceId(), LOAD_BALANCING.getType(), instanceInfoList, modelIndexs);

                //GSLB关联列表数据
                List<MwModelCitrixInfoParam> mwModelGSLBDatas = syncSaveGSLBRelationList(mParam, mapAll);
                addInstanceToModel(mwModelGSLBDatas, mParam.getRelationInstanceId(), ModelCitrixType.GSLB.getType(), instanceInfoList, modelIndexs);

                modelIndexList1.addAll(modelIndexs);
                relationIds1.add(mParam.getRelationInstanceId());
                instanceInfoList1.addAll(instanceInfoList);

            }
            deleteParam1.setModelIndexs(modelIndexList1);
            deleteParam1.setRelationInstanceIds(relationIds1);
            mwModelInstanceService.batchDeleteInstanceInfo(deleteParam1);
            if (instanceInfoList1 != null && instanceInfoList1.size() > 0) {
                mwModelInstanceService.saveData(instanceInfoList1, true, true);
            }
            taskRresult.setSuccess(true);
        } catch (Throwable e) {
            log.error("fail to getCitrixInfoByTaskTime case:{}", e);
            taskRresult.setSuccess(false);
            taskRresult.setFailReason("定时任务同步Citrix数据执行失败");
        }
        return taskRresult;
    }


    /**
     * 获取所有的citrix设备
     */
    @Override
    public Reply getAllModelCitrixAssets() {
        List<ModelInstanceDto> citrixAssets = mwModelCitrixDao.getAllCitrixInstanceInfo();
        return Reply.ok(citrixAssets);
    }

    /**
     * 查询指定citrix设备的端口和Ip
     */
    @Override
    public Reply advancedQueryCitrixInfo(MwAdvancedQueryCitrixParam param) {
        try {
            //只对Load Balancing数据中的公网端口和IP进行查询
            param.getRelationInstanceId();//关联Id
            String modelName = LOAD_BALANCING.getType();
            MwModelInfoDTO mwModelInfoDTO = mwModelCitrixDao.getModelIndexInfo(modelName);

            QueryEsParam esParam = new QueryEsParam();
            esParam.setModelIndexs(Arrays.asList(mwModelInfoDTO.getModelIndex()));
            List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
            QueryModelInstanceByPropertyIndexParam propertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
            propertyIndexParam.setPropertiesIndexId(RELATION_INSTANCE_ID);
            propertyIndexParam.setPropertiesValueList(Arrays.asList(param.getRelationInstanceId()));
            paramLists.add(propertyIndexParam);
            esParam.setParamLists(paramLists);
            if (param.getRelationInstanceId() == null) {
                esParam.setParamLists(null);
            }
            //通过es查询数据
            List<Map<String, Object>> map = mwModelViewService.getAllInstanceInfoByModelIndexs(esParam);
            List<MwModelLBCitrixListData> citrixLBInfoList = JSONArray.parseArray(JSONObject.toJSONString(map), MwModelLBCitrixListData.class);
            List<MwCitrixIPAndPortParam> mwCitrixIPAndPortParamList = new ArrayList<>();

            BigInteger startIpV6 = BigInteger.valueOf(0);
            BigInteger endIpV6 = BigInteger.valueOf(0);
            if (!Strings.isNullOrEmpty(param.getIpStart())) {
                //判断是否是IPV6
                boolean isIpV6 = IPAddressUtil.isIPv6LiteralAddress(param.getIpStart());
                if (isIpV6) {
                    startIpV6 = ModelIPUtil.Ipv6IptoBigInteger(param.getIpStart());
                }
            }
            if (!Strings.isNullOrEmpty(param.getIpEnd())) {
                //判断是否是IPV6
                boolean isIpV6 = IPAddressUtil.isIPv6LiteralAddress(param.getIpEnd());
                if (isIpV6) {
                    endIpV6 = ModelIPUtil.Ipv6IptoBigInteger(param.getIpEnd());
                }
            }

            for (MwModelLBCitrixListData citrixListData : citrixLBInfoList) {
                String ipPort = citrixListData.getVirtualServerIpPort();
                String ipStr = ipPort.substring(0, ipPort.lastIndexOf(":"));
                String postStr = ipPort.substring(ipPort.lastIndexOf(":") + 1);
                MwCitrixIPAndPortParam citrixIPAndPortParam = new MwCitrixIPAndPortParam();
                citrixIPAndPortParam.setIp(ipStr);
                citrixIPAndPortParam.setPort(postStr);
                //范围筛选
                boolean isIpV6 = IPAddressUtil.isIPv6LiteralAddress(ipStr);
                //IPV6数据过滤
                if (isIpV6) {
                    BigInteger ipIntVal = ModelIPUtil.Ipv6IptoBigInteger(ipStr);
                    //ip范围查询
                    if (startIpV6.intValue() != 0 && endIpV6.intValue() != 0) {
                        if (ipIntVal.compareTo(startIpV6) == 1 && ipIntVal.compareTo(endIpV6) == -1) {
                            mwCitrixIPAndPortParamList.add(citrixIPAndPortParam);
                        }

                    } else if (!Strings.isNullOrEmpty(param.getIpRange()) && IPAddressUtil.isIPv6LiteralAddress(param.getIpRange())) {//ip地址段查询
                        //是否在指定地址段内
                        boolean isRange = ModelIPUtil.IpContain(param.getIpRange(), ipStr);
                        if (isRange) {
                            mwCitrixIPAndPortParamList.add(citrixIPAndPortParam);
                        }
                    } //没设置查询范围，则查询所有
                    if (Strings.isNullOrEmpty(param.getIpStart()) &&
                            Strings.isNullOrEmpty(param.getIpEnd()) && Strings.isNullOrEmpty(param.getIpRange())) {
                        mwCitrixIPAndPortParamList.add(citrixIPAndPortParam);
                    }

                }
                boolean isIpV4 = IPAddressUtil.isIPv4LiteralAddress(ipStr);
                //IPV4数据过滤
                if (isIpV4) {
                    //范围查询
                    if (!Strings.isNullOrEmpty(param.getIpStart()) && !Strings.isNullOrEmpty(param.getIpEnd()) && !Strings.isNullOrEmpty(ipStr)) {
                        boolean isRange = ModelIPUtil.ipIsValid(param.getIpStart(), param.getIpEnd(), ipStr);
                        if (isRange) {
                            mwCitrixIPAndPortParamList.add(citrixIPAndPortParam);
                        }
                    } else if (!Strings.isNullOrEmpty(param.getIpRange())) {//地址段查询
                        boolean isRange = IpV4Util.isInRange(ipStr, param.getIpRange());
                        if (isRange) {
                            mwCitrixIPAndPortParamList.add(citrixIPAndPortParam);
                        }
                    } //没设置查询范围，则查询所有
                    if (Strings.isNullOrEmpty(param.getIpStart()) &&
                            Strings.isNullOrEmpty(param.getIpEnd()) && Strings.isNullOrEmpty(param.getIpRange())) {
                        mwCitrixIPAndPortParamList.add(citrixIPAndPortParam);
                    }
                }
            }
            List<MwCitrixIPAndPortParam> list = new ArrayList<>();
            list = mwCitrixIPAndPortParamList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getIp() + "_" + s.getPort()))), ArrayList::new));
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();

            //默认排序 IP、port
            list = list.stream().sorted(Comparator.comparing(MwCitrixIPAndPortParam::getIp).
                    thenComparing(s -> s.getPort() != null ? Integer.valueOf(s.getPort()) : 0).reversed()).collect(Collectors.toList());
            //条件查询
            if (!Strings.isNullOrEmpty(param.getQueryType())) {
                if (IP.equals(param.getQueryType())) {
                    //先查询条件过滤
                    if (!Strings.isNullOrEmpty(param.getQueryValue())) {
                        list = list.stream().filter(s -> s.getIp().equals(param.getQueryValue().trim())).collect(Collectors.toList());
                    }
                    //在排序
                    list = list.stream().sorted(Comparator.comparing(MwCitrixIPAndPortParam::getIp).
                            thenComparing(s -> s.getPort() != null ? Integer.valueOf(s.getPort()) : 0).reversed()).collect(Collectors.toList());
                }
                if (PORT.equals(param.getQueryType())) {
                    //先查询条件过滤
                    if (!Strings.isNullOrEmpty(param.getQueryValue())) {
                        list = list.stream().filter(s -> s.getPort().equals(param.getQueryValue().trim())).collect(Collectors.toList());
                    }
                    //在排序
                    list = list.stream().sorted(Comparator.comparing(MwCitrixIPAndPortParam::getPort).
                            thenComparing(MwCitrixIPAndPortParam::getIp).reversed()).collect(Collectors.toList());
                }
            }
            List<MwCitrixIPAndPortParam> lists = new ArrayList<>();
            pageInfo.setTotal(list.size());
            lists = pageList.getList(list, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(lists);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("高级查询失败" + e);
            return Reply.fail(500, "高级查询失败");
        }
    }

}
