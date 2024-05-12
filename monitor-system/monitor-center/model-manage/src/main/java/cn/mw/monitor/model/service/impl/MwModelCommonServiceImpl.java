package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.param.org.QueryOrgForDropDown;
import cn.mw.monitor.model.dao.*;
import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.param.rancher.RancherInstanceParam;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelSysLogService;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.AssetTypeIconDTO;
import cn.mw.monitor.service.assets.model.AssetsInterfaceDTO;
import cn.mw.monitor.service.assets.model.ModelInterfaceDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.MwPropertiesValueDTO;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.dto.rancher.*;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.NetListDto;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.dto.MwUserDTO;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.ConnectCheckModelEnum.RANCHER;
import static cn.mw.monitor.model.util.ModelUtils.checkStrIsNumber;
import static cn.mw.monitor.service.model.service.ModelCabinetField.RELATIONSITECABINET;
import static cn.mw.monitor.service.model.service.ModelPropertiesType.MONITORSERVER_RELATION;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

/**
 * @author qzg
 * @date 2021/12/06
 */
@Service
@Slf4j
public class MwModelCommonServiceImpl implements MwModelCommonService {
    @Resource
    private MWModelCommonDao mwModelCommonDao;
    private int pageSize = 10000;
    @Autowired
    private MwModelViewServiceImpl mwModelViewServiceImpl;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;
    @Autowired
    private MWUserService userService;
    @Autowired
    private MwServerService service;
    @Autowired
    private ModelServer modelSever;
    @Autowired
    private MwModelRancherServiceImpl mwModelRancherServiceImpl;
    @Resource
    private MwModelViewDao mwModelViewDao;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private ILoginCacheInfo loginCacheInfo;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Resource
    private MWModelZabbixMonitorDao mwModelZabbixMonitorDao;
    @Resource
    private MWOrgService mwOrgService;
    @Autowired
    private MwModelSysLogService mwModelSysLogService;
    @Autowired
    private MWUserService mwuserService;
    @Resource
    private MwModelSysLogDao mwModelSysLogDao;
    @Autowired
    private RancherClientUtils rancherClientUtils;
    @Value("${model.instance.batchFetchNum}")
    private int insBatchFetchNum;
    @Value("${fromUser.modelId}")
    private String fromUserModelId;
    @Value("${modelSystem.ModelId}")
    private Integer modelSystemModelId;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;


    @Override
    public List<MwModeRommCommonParam> getAllRoomAndCabinetInfo(QueryModelInstanceByPropertyIndexParamList params) {
        List<MwModeRommCommonParam> mwRoomList = new ArrayList<>();
        try {
            String queryFiled = "";
            String queryVal = "";
            if (CollectionUtils.isNotEmpty(params.getParamLists())) {
                QueryModelInstanceByPropertyIndexParam propertyIndexParam = params.getParamLists().get(0);
                queryFiled = propertyIndexParam.getPropertiesIndexId();
                queryVal = propertyIndexParam.getPropertiesValue();
            }
            List<MwModelInstanceCommonParam> modelInstanceInfoList = mwModelCommonDao.selectModelInfoByRoomAndCabinet();
            //按照模型视图分组，modelView为1表示机房模型，为2表示机柜模型
            Map<String, List<MwModelInstanceCommonParam>> map = modelInstanceInfoList.stream().collect(Collectors.groupingBy(MwModelInstanceCommonParam::getModelView));
            if (map != null) {
                //获取机房模型实例数据
                List<MwModelInstanceCommonParam> roomInstanceInfos = map.get("1");
                Set<String> roomIndex = roomInstanceInfos.stream().map(s -> s.getModelIndex()).collect(Collectors.toSet());
                Set<Integer> roomInstanceIds = roomInstanceInfos.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
                //从es中获取所有数据
                QueryInstanceModelParam param = new QueryInstanceModelParam();
                param.setPageSize(pageSize);
                param.setModelIndexs(new ArrayList<>(roomIndex));
                param.setInstanceIds(new ArrayList<>(roomInstanceIds));

                List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
                AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
                propertiesParam.setPropertiesIndexId(queryFiled);
                propertiesParam.setPropertiesValue(queryVal);
                propertiesParam.setPropertiesType(1);
                propertiesList.add(propertiesParam);
                param.setPropertiesList(propertiesList);

                Map<String, Object> roomMap = mwModelViewServiceImpl.getModelListInfoByBase(param);
                List<Map<String, Object>> roomListMap = new ArrayList<>();
                if (roomMap != null && roomMap.get("data") != null) {
                    roomListMap = (List<Map<String, Object>>) roomMap.get("data");
                }
                mwRoomList = MwModelUtils.convertEsData(MwModeRommCommonParam.class, roomListMap);
                //机房布局数据设置
                for (MwModeRommCommonParam roomParam : mwRoomList) {
                    List<List<QueryLayoutDataParam>> layoutData = new ArrayList<>();
                    List<QueryLayoutDataParam> listLayoutDataParam = new ArrayList<>();
                    List<List<QueryLayoutDataParam>> lists = roomParam.getLayoutData();
                    for (List listArr : lists) {
                        listLayoutDataParam = JSONArray.parseArray(JSONObject.toJSONString(listArr), QueryLayoutDataParam.class);
                        layoutData.add(listLayoutDataParam);
                    }
                    roomParam.setLayoutData(layoutData);
                }

                //获取机柜模型实例数据
                List<MwModelInstanceCommonParam> cabinetInstanceInfos = map.get("2");
                Set<String> cabinetIndex = cabinetInstanceInfos.stream().map(s -> s.getModelIndex()).collect(Collectors.toSet());
                Set<Integer> cabinetInstanceIds = cabinetInstanceInfos.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
                param.setModelIndexs(new ArrayList<>(cabinetIndex));
                param.setInstanceIds(new ArrayList<>(cabinetInstanceIds));
                Map<String, Object> cabinetMap = mwModelViewServiceImpl.getModelListInfoByBase(param);
                List<Map<String, Object>> cabinetListMap = new ArrayList<>();
                if (cabinetMap != null && cabinetMap.get("data") != null) {
                    cabinetListMap = (List<Map<String, Object>>) cabinetMap.get("data");
                }

                List<MwModeCabinetCommonParam> mwCabinetList = MwModelUtils.convertEsData(MwModeCabinetCommonParam.class, cabinetListMap);
                List<CabinetLayoutDataParam> cabinetLayoutInfo = new ArrayList<>();
                //机柜布局数据设置
                for (MwModeCabinetCommonParam cabinetCommonParam : mwCabinetList) {
                    cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(cabinetCommonParam.getLayoutData()), CabinetLayoutDataParam.class);
                    cabinetCommonParam.setLayoutData(cabinetLayoutInfo);
                }
                //根据机柜的所属机房id分组
                Map<Integer, List<MwModeCabinetCommonParam>> cabinetMapByRelationRoom = mwCabinetList.stream().collect(Collectors.groupingBy(MwModeCabinetCommonParam::getRelationSiteRoom));

                for (MwModeRommCommonParam roomCommonParam : mwRoomList) {
                    Integer instanceId = roomCommonParam.getModelInstanceId();
                    if (cabinetMapByRelationRoom != null && cabinetMapByRelationRoom.containsKey(instanceId)) {
                        List<MwModeCabinetCommonParam> relationCabinetrList = cabinetMapByRelationRoom.get(instanceId);
                        roomCommonParam.setRelationCabinetrList(relationCabinetrList);
                    }
                }
            }
            return mwRoomList;
        } catch (Exception e) {
            log.error("fail to getAllRoomAndCabinetInfo cause:{}", e);
        }
        return null;
    }

    @Override
    public List<MwModelFromUserParam> getInstanceInfoByModelIndex() {
        try {
            List<MwModelInstanceCommonParam> modelInstanceInfoList = mwModelCommonDao.selectModelInstanceInfo(fromUserModelId);
            Set<String> modelIndexs = modelInstanceInfoList.stream().map(s -> s.getModelIndex()).collect(Collectors.toSet());
            Set<Integer> instanceIds = modelInstanceInfoList.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
            //从es中获取所有数据
            QueryInstanceModelParam param = new QueryInstanceModelParam();
            param.setPageSize(pageSize);
            param.setModelIndexs(new ArrayList<>(modelIndexs));
            param.setInstanceIds(new ArrayList<>(instanceIds));
            Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(param);
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
            }
            //外部关联字段值转换（id转为name）
            relationFieldConvert(listMap);
            List<MwModelFromUserParam> mwModelFromUserList = MwModelUtils.convertEsData(MwModelFromUserParam.class, listMap);
            return mwModelFromUserList;
        } catch (Exception e) {
            log.error("fail to getInstanceInfoByModelIndex cause:{}", e);
        }
        return null;
    }

    /**
     * 根据实例数据获取Rancher关联信息
     *
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public List<MwModelRancherCommonDTO> findRancherInfoByModelAssets(QueryModelInstanceByPropertyIndexParamList params) {
        List<MwModelRancherCommonDTO> rancherAllCommonList = new ArrayList<>();
        try {
            //获取所有模型分组
            List<AssetTypeIconDTO> assetTypeIconDTOS = mwModelViewDao.selectAllAssetsTypeIcon();
            Map<String, AssetTypeIconDTO> assetTypeMap = new HashMap<>();
            for (AssetTypeIconDTO assetTypeIconDTO : assetTypeIconDTOS) {
                assetTypeMap.put(assetTypeIconDTO.getId().toString(), assetTypeIconDTO);
            }
            //获取所有模型的公共属性
            List<ModelInfo> modelInfoList = mwModelManageDao.selectAllModelInfo();
            Map<String, PropertyInfo> propertyMap = new HashMap<>();
            if (null != modelInfoList) {
                for (ModelInfo modelInfo : modelInfoList) {
                    if (null != modelInfo.getPropertyInfos()) {
                        for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                            propertyMap.put(propertyInfo.getIndexId(), propertyInfo);
                        }
                    }
                }
            }
            List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
            for (QueryModelInstanceByPropertyIndexParam queryParam : params.getParamLists()) {
                PropertyInfo propertyInfo = propertyMap.get(queryParam.getPropertiesIndexId());
                AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
                addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
                addModelInstancePropertiesParam.setPropertiesValue(queryParam.getPropertiesValue());
                propertiesParamList.add(addModelInstancePropertiesParam);
            }
            List<Map<String, Object>> listMap = new ArrayList<>();

            QueryInstanceModelParam param = new QueryInstanceModelParam();
            String modelIndex = mwModelInstanceDao.getModelIndexByModelId(RANCHER.getModelId());
            param.setModelIndexs(Arrays.asList(modelIndex));
            mwModelViewServiceImpl.getInstanceListData(param);
            param.setPageSize(pageSize);
            param.setPropertiesList(propertiesParamList);
            //获取所有rancher实例数据
            Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(param);
            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
            }
            //获取Rancher的连接信息，URL、tokens、密码
            List<MwModelMacrosParam> macrosParams = JSON.parseArray(JSONObject.toJSONString(listMap), MwModelMacrosParam.class);
            String url = "";
            String tokens = "";
            Integer rancherModelId = 0;
            Integer rancherInstanceId = 0;
            for (MwModelMacrosParam m : macrosParams) {
                url = m.getHOST();
                rancherModelId = m.getModelId();
                tokens = RSAUtils.decryptData(m.getTOKENS() != null ? m.getTOKENS() : "", RSAUtils.RSA_PRIVATE_KEY);
                rancherInstanceId = m.getModelInstanceId();

                MwModelRancherCommonDTO rancherCommonDTO = new MwModelRancherCommonDTO();
                RancherInstanceParam rancherParam = new RancherInstanceParam();
                rancherParam.setModelId(rancherModelId);
                rancherParam.setModelInstanceId(rancherInstanceId);
                rancherParam.setTokens(tokens);
                rancherParam.setIPAdress(url);
                rancherCommonDTO.setRancherName(m.getInstanceName());

                if (!Strings.isNullOrEmpty(tokens) && !Strings.isNullOrEmpty(url)) {
                    ModelMapper modelMapper = new ModelMapper();
                    long time1 = System.currentTimeMillis();
                    //集群数据
                    List<MwModelRancherDataInfoDTO> clusterList = rancherClientUtils.getClusters(rancherParam);
                    if (CollectionUtils.isNotEmpty(clusterList)) {
                        long time2 = System.currentTimeMillis();
                        //项目数据
                        List<MwModelRancherDataInfoDTO> projectsList = rancherClientUtils.getProjects(rancherParam);
                        long time3 = System.currentTimeMillis();
                        //根据clusteId获取所有的nameSpace数据
                        List<MwModelRancherDataInfoDTO> allNameSpaceList = new ArrayList<>();
                        for (MwModelRancherDataInfoDTO clusterDTO : clusterList) {
                            List<MwModelRancherDataInfoDTO> nameSpaceListByclusterId = rancherClientUtils.getNameSpacesByCluster(clusterDTO.getId(), rancherParam);
                            allNameSpaceList.addAll(nameSpaceListByclusterId);
                        }
                        long time4 = System.currentTimeMillis();
                        //node数据
                        List<MwModelRancherDataInfoDTO> nodesList = rancherClientUtils.getNodes(rancherParam);
                        long time5 = System.currentTimeMillis();

                        long time6 = System.currentTimeMillis();
                        //单位转换，获取利用率
                        mwModelRancherServiceImpl.rancherDataListHanding(clusterList);
                        //类型转换
                        List<MwModelRancherClusterCommonDTO> clusterCommonList = clusterList.stream().map(s -> modelMapper.map(s, MwModelRancherClusterCommonDTO.class)).collect(Collectors.toList());

                        //类型转换
                        List<MwModelRancherProjectCommonDTO> projectsCommonList = projectsList.stream().map(s -> modelMapper.map(s, MwModelRancherProjectCommonDTO.class)).collect(Collectors.toList());

                        //单位转换，获取利用率
                        mwModelRancherServiceImpl.rancherDataListHanding(nodesList);

                        //类型转换
                        List<MwModelRancherNodesDTO> nodesCommonList = nodesList.stream().map(s -> modelMapper.map(s, MwModelRancherNodesDTO.class)).collect(Collectors.toList());
                        //根据PId进行group分组
                        Map<String, List<MwModelRancherNodesDTO>> nodesListMapByPid = nodesCommonList.stream().collect(Collectors.groupingBy(s -> s.getPId() != null ? s.getPId() : ""));

                        //类型转换
                        List<MwModelRancherNameSpaceDTO> nameSpaceCommonList = allNameSpaceList.stream().map(s -> modelMapper.map(s, MwModelRancherNameSpaceDTO.class)).collect(Collectors.toList());
                        //根据PId进行group分组
                        Map<String, List<MwModelRancherNameSpaceDTO>> nameSpaceByPid = nameSpaceCommonList.stream().collect(Collectors.groupingBy(s -> s.getPId() != null ? s.getPId() : ""));

                        //循环projects数据，依据project的id和nameSpace的pid对应关系，获取下一级nameSpace的数据
                        for (MwModelRancherProjectCommonDTO projectCommonDTO : projectsCommonList) {
                            if (nameSpaceByPid != null && nameSpaceByPid.containsKey(projectCommonDTO.getId())) {
                                projectCommonDTO.setNameSpaceList(nameSpaceByPid.get(projectCommonDTO.getId()));
                            }
                        }

                        //projects数据，根据PId进行group分组
                        Map<String, List<MwModelRancherProjectCommonDTO>> projectsCommonMapByPid = projectsCommonList.stream().collect(Collectors.groupingBy(s -> s.getPId() != null ? s.getPId() : ""));

                        //循环cluster集群数据，依据id和pid对应关系，获取下一级ProjectList和NodeList的数据
                        for (MwModelRancherClusterCommonDTO clusterCommonDTO1 : clusterCommonList) {
                            String id = clusterCommonDTO1.getId();
                            if (projectsCommonMapByPid != null && projectsCommonMapByPid.containsKey(id)) {
                                clusterCommonDTO1.setProjectList(projectsCommonMapByPid.get(id));
                            }
                            if (nodesListMapByPid != null && nodesListMapByPid.containsKey(id)) {
                                clusterCommonDTO1.setNodeList(nodesListMapByPid.get(id));
                            }
                        }
                        long time7 = System.currentTimeMillis();
                        rancherCommonDTO.setClusterList(clusterCommonList);
                        rancherAllCommonList.add(rancherCommonDTO);
                        log.info(rancherCommonDTO.getRancherName() + "::Rancher关联数据转换耗时:" + (time7 - time6) + "ms");
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取所有Rancher关联数据失败", e);
        }
        return rancherAllCommonList;
    }


    /**
     * 监控服务关联字段值转换
     */
    public void monitorServerRelationConvert(List<Map<String, Object>> listMap) {
        //获取所有模型属性
        List<PropertyInfo> allPropertyList = mwModelViewServiceImpl.getAllPropertyInfo();
        //过滤，获取监控服务关联属性
        List<PropertyInfo> disList = allPropertyList.stream().filter(s -> (s.getIsShow() != null && s.getIsShow()) && s.getPropertiesTypeId() == MONITORSERVER_RELATION.getCode()).collect(Collectors.toList());

        Set<String> monitorServerRelation = disList.stream().map(s -> s.getIndexId()).collect(Collectors.toSet());
        Map<String, Object> itemMapCut = new HashMap();
        for (Map<String, Object> ms : listMap) {
            ms.forEach((key, val) -> {
                //没纳管的数据，监控服务关联类型的设空值
                if (monitorServerRelation != null && monitorServerRelation.size() > 0 && monitorServerRelation.contains(key)) {
                    itemMapCut.put(key, val);
                    ms.put(key, "");
                }
            });
        }
        if (itemMapCut.size() == 0) {//没有监控服务关联属性，直接结束
            return;
        }


        List<Map<String, Object>> disMap = listMap.stream().filter(s -> s.get(ASSETS_ID) != null && !s.get(ASSETS_ID).toString().equals("") && s.get(MONITOR_SERVER_ID) != null && !"0".equals(s.get(MONITOR_SERVER_ID).toString())).collect(Collectors.toList());
        if (disMap != null && disMap.size() > 0) {
            Map<Integer, List<Map<String, Object>>> groupMap = disMap.stream()
                    .collect(Collectors.groupingBy(s -> Integer.valueOf(s.get(MONITOR_SERVER_ID).toString())));

            for (Map.Entry<Integer, List<Map<String, Object>>> e : groupMap.entrySet()) {
                Integer serverId = e.getKey();
                List<Map<String, Object>> listMap2 = e.getValue();
                //获取hostId
                Set<String> assetsIds = listMap2.stream().map(s -> s.get(ASSETS_ID).toString()).collect(Collectors.toSet());
                //获取监控项List
                List<String> itemList = itemMapCut.values().stream().map(Object::toString).distinct().collect(Collectors.toList());

                //查询监控项数据
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemList, new ArrayList<>(assetsIds));
                if (result == null || result.isFail()) {
                    continue;
                }
                List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                Map<String, List<ItemApplication>> mapByHostId = itemApplications.stream().collect(Collectors.groupingBy(s -> s.getHostid()));

                for (Map<String, Object> ms : listMap2) {
                    if (ms.get(ASSETS_ID) != null) {
                        String assetsId = ms.get(ASSETS_ID).toString();
                        if (mapByHostId != null && mapByHostId.containsKey(assetsId)) {
                            List<ItemApplication> items = mapByHostId.get(assetsId);
                            Map mas = new HashMap();
                            if (CollectionUtils.isNotEmpty(items)) {
                                String itemName = items.get(0).getName();
                                String units = items.get(0).getUnits();
                                String valType = items.get(0).getValue_type();
                                if (itemName.indexOf("]") != -1) {
                                    itemName = itemName.split("]")[1];
                                }
                                String valueUnits = "";
                                if (valType.equals("0") || valType.equals("3")) {
                                    //数据处理
                                    handlerUnitsChange(items);
                                    //多监控项的取平均值
                                    double value = items.stream().filter(s -> !Strings.isNullOrEmpty(s.getLastvalue()) && checkStrIsNumber(s.getLastvalue())).map(ItemApplication::getLastvalue).collect(Collectors.toList()).stream().mapToDouble(item -> Double.parseDouble(item)).reduce((a, b) -> a + b).getAsDouble();
                                    //单位转换
                                    Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), units);
                                    String ValueStr = convertedValue.get("value");
                                    units = convertedValue.get("units");
                                    String values = new BigDecimal(ValueStr).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    valueUnits = values + units;
                                }
                                mas.put(itemName, valueUnits);
                                ms.forEach((key, val) -> {
                                    Object obj = itemMapCut.get(key);
                                    //对监控服务关联数据进行赋值转换
                                    if (itemMapCut != null && mas != null && mas.size() > 0 && obj != null && mas.containsKey(obj.toString())) {
                                        ms.put(key, mas.get(obj.toString()));
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    //单位转换处理
    public void handlerUnitsChange(List<ItemApplication> itemApplications) {
        for (ItemApplication itemDto : itemApplications) {
            if (itemDto != null && !Strings.isNullOrEmpty(itemDto.getLastvalue())) {
                String value = itemDto.getLastvalue();
                if (checkStrIsNumber(value) && StringUtils.isNotBlank(value) && (value.contains("+") || value.contains("E"))) {
                    value = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                }
                itemDto.setLastvalue(value);
            }
        }

    }

    /**
     * 负责人、机构、用户组字段转换
     */
    public void powerFieldConvert(Boolean skipDataPermission, List<Map<String, Object>> listMap) {
        List<Integer> allUserIds = new ArrayList<>();
        List<Integer> allOrgIds = new ArrayList<>();
        List<Integer> allGroupIds = new ArrayList<>();
        for (Map<String, Object> m : listMap) {
            if (m.get(USER_IDS) != null && m.get(USER_IDS) instanceof List) {
                List<Integer> userIdList = (List) JSONArray.parse(JSONObject.toJSONString(m.get(USER_IDS)));
                allUserIds.addAll(userIdList);
            }
            if (m.get(ORG_IDS) != null && m.get(ORG_IDS) instanceof List) {
                List<List<Integer>> orgIdList = (List) JSONArray.parse(JSONObject.toJSONString(m.get(ORG_IDS)));
                List<Integer> orgIdLists = orgIdList.stream().flatMap(List::stream).collect(Collectors.toList());
                allOrgIds.addAll(orgIdLists);
            }
            List<Integer> groupIdList = new ArrayList<>();
            if (m.get(GROUP_IDS) != null && m.get(GROUP_IDS).toString() != "" && m.get(GROUP_IDS) instanceof List) {
                groupIdList = (List) JSONArray.parse(JSONObject.toJSONString(m.get(GROUP_IDS)));
                allGroupIds.addAll(groupIdList);
            }
        }

        List<Integer> allOrgDisIds = allOrgIds.stream().distinct().collect(Collectors.toList());
        List<Integer> allUserDisIds = allUserIds.stream().distinct().collect(Collectors.toList());
        List<Integer> allGroupDisIds = allGroupIds.stream().distinct().collect(Collectors.toList());
        Map<Integer, String> userMaps = new HashMap<>();
        Map<Integer, String> groupMaps = new HashMap<>();
        Map<Integer, String> orgMaps = new HashMap<>();

        String loginName = null;
        if (skipDataPermission != null && skipDataPermission) {
            loginName = null;
        } else {
            GlobalUserInfo globalUser = userService.getGlobalUser();
            HashSet<Integer> userOrgSet = new HashSet<>();
            //根据当前用户获取机构Id
            Reply orgReply = mwOrgService.selectDorpdownList(new QueryOrgForDropDown());
            if (orgReply.getRes() == PaasConstant.RES_SUCCESS) {
                List<MWOrgDTO> orgList = (List<MWOrgDTO>) orgReply.getData();
                getUserOrgSet(orgList, userOrgSet);
                allOrgDisIds = new ArrayList<>(userOrgSet);
            }
            //根据当前用户获取用户Id
            Reply userReply = mwuserService.getDropDownUser();
            if (userReply.getRes() == PaasConstant.RES_SUCCESS) {
                List<MwUserDTO> userList = (List<MwUserDTO>) userReply.getData();
                Set<Integer> userIds = userList.stream().map(s -> s.getUserId()).collect(Collectors.toSet());
                allUserDisIds = new ArrayList<>(userIds);
            }
            if (globalUser.isSystemUser()) {//系统管理员
                loginName = null;
            } else {//普通用户
                loginName = globalUser.getLoginName();
            }
        }
        if (CollectionUtils.isNotEmpty(allUserDisIds)) {
            List<MwModelUserDTO> modelUserDTOS = new ArrayList<>();
            List<List<Integer>> allUserIdGroups = Lists.partition(allUserDisIds, insBatchFetchNum);
            if (null != allUserIdGroups) {
                for (List<Integer> allUserIdList : allUserIdGroups) {
                    modelUserDTOS.addAll(mwModelViewDao.getModelUserInfo(allUserIdList));
                }
            }
            userMaps = modelUserDTOS.stream().collect(Collectors.toMap(s -> s.getUserId(), s -> s.getUserName(), (
                    value1, value2) -> {
                return value2;
            }));
        }
        if (CollectionUtils.isNotEmpty(allGroupDisIds)) {
            //根据登录用户，来获取用户组信息
            List<MwModelGroupDTO> modelGroupDTOS = mwModelViewDao.getModelGroupInfo(loginName);
            groupMaps = modelGroupDTOS.stream().collect(Collectors.toMap(s -> s.getGroupId(), s -> s.getGroupName(), (
                    value1, value2) -> {
                return value2;
            }));
        }
        if (CollectionUtils.isNotEmpty(allOrgDisIds)) {
            List<MwModelOrgDTO> modelOrgDTOs = new ArrayList<>();
            List<List<Integer>> allOrgIdGroups = Lists.partition(allOrgDisIds, insBatchFetchNum);
            if (null != allOrgIdGroups) {
                for (List<Integer> allOrgIdList : allOrgIdGroups) {
                    modelOrgDTOs.addAll(mwModelViewDao.getModelOrgInfo(allOrgIdList));
                }
            }
            orgMaps = modelOrgDTOs.stream().collect(Collectors.toMap(s -> s.getOrgId(), s -> s.getOrgName(), (
                    value1, value2) -> {
                return value2;
            }));
        }

        for (Map<String, Object> m : listMap) {
            if (m.get(USER_IDS) != null && m.get(USER_IDS) instanceof List) {
                List<Integer> userIdList = (List) JSONArray.parse(JSONObject.toJSONString(m.get(USER_IDS)));
                List<String> userNameList = new ArrayList<>();
                for (Integer userId : userIdList) {
                    String userName = userMaps.get(userId);
                    userNameList.add(userName);
                }
                m.put(USER_NAMES, userNameList);
            }
            if (m.get(ORG_IDS) != null && m.get(ORG_IDS) instanceof List) {
                List<List<Integer>> orgIdList = (List) JSONArray.parse(JSONObject.toJSONString(m.get(ORG_IDS)));
                List<String> orgNameList = new ArrayList<>();
                for (List<Integer> orgIds : orgIdList) {
                    String orgName = "";
                    for (Integer orgId : orgIds) {
                        if (orgMaps.containsKey(orgId)) {
                            orgName += orgMaps.get(orgId) + "/";
                        }
                    }
                    if (orgName.length() > 1) {
                        orgName = orgName.substring(0, orgName.length() - 1);
                        orgNameList.add(orgName);
                    }
                }
                m.put(ORG_NAMES, orgNameList);
            }
            List<Integer> groupIdList = new ArrayList<>();
            if (m.get(GROUP_IDS) != null && m.get(GROUP_IDS).toString() != "" && m.get(GROUP_IDS) instanceof List) {
                groupIdList = (List) JSONArray.parse(JSONObject.toJSONString(m.get(GROUP_IDS)));
                List<String> groupNameList = new ArrayList<>();
                for (Integer groupId : groupIdList) {
                    String groupName = groupMaps.get(groupId);
                    groupNameList.add(groupName);
                }
                m.put(GROUP_NAMES, groupNameList);
            }
        }
    }

    private void getUserOrgSet(List<MWOrgDTO> orgList, HashSet<Integer> userOrgSet) {
        if (CollectionUtils.isNotEmpty(orgList)) {
            for (MWOrgDTO org : orgList) {
                userOrgSet.add(org.getOrgId());
                if (CollectionUtils.isNotEmpty(org.getChilds())) {
                    getUserOrgSet(org.getChilds(), userOrgSet);
                }
            }
        }
    }

    /**
     * 外部关联字段id转为name
     */
    public void relationFieldConvert(List<Map<String, Object>> listMap) {

        List<PropertyInfo> propertyInfoList = mwModelViewServiceImpl.getAllPropertyInfo();
        Set<String> disPropertyList = propertyInfoList.stream().filter(s -> (s.getIsShow() != null && s.getIsShow()) && (s.getPropertiesTypeId().intValue() == 5 || s.getPropertiesTypeId().intValue() == 4)).map(PropertyInfo::getIndexId).collect(Collectors.toSet());
        List<Integer> relationIds = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");
        for (Map<String, Object> m : listMap) {
            m.forEach((k, v) -> {
                //判断是否是外部关联属性
                if (CollectionUtils.isNotEmpty(disPropertyList) && disPropertyList.contains(k) && v != null && !Strings.isNullOrEmpty(v.toString())) {
                    boolean isRelationSiteRoomNum = pattern.matcher(v.toString()).matches();
                    if (isRelationSiteRoomNum) {
                        relationIds.add(Integer.valueOf(v.toString()));
                    }
                }
            });
        }

        List<List<Integer>> instanceIdGroups = null;
        List<MwModelInstanceCommonParam> modelInstanceList = new ArrayList<>();
        if (null != relationIds) {
            instanceIdGroups = Lists.partition(relationIds, insBatchFetchNum);
            for (List<Integer> instanceIdList : instanceIdGroups) {
                if (CollectionUtils.isNotEmpty(instanceIdList)) {
                    //外部关联实例id查询对应实例名称
                    modelInstanceList.addAll(mwModelInstanceDao.getInstanNameAndRelationNameById(instanceIdList));
                }
            }
        }
        Map<String, MwModelInstanceCommonParam> instanceMap = modelInstanceList.stream().collect(Collectors.toMap(s -> s.getModelInstanceId() + "", s -> s, (
                value1, value2) -> {
            return value2;
        }));

        for (Map<String, Object> m : listMap) {
            m.forEach((k, v) -> {
                //判断是否是外部关联属性
                if (instanceMap != null && instanceMap.size() > 0 && v != null && instanceMap.containsKey(v.toString()) && disPropertyList.contains(k)) {
                    m.put(k, instanceMap.get(v.toString()).getModelInstanceName());
                }
            });
        }
    }

    /**
     * 获取本体模型和所有父模型的属性
     *
     * @return
     */
    @Override
    public Map<Integer, List<PropertyInfo>> getAllModelPropertyInfo() {
        List<ModelInfo> modelInfoLists = mwModelManageDao.selectAllModelListWithParent();
        //将模型id作为key值，转为map
        Map<Integer, List<ModelInfo>> modelInfoListMap = modelInfoLists.stream().collect(Collectors.groupingBy(ModelInfo::getModelId));
        //将模型id作为key值，本体模型和所有父模型属性值做为value，转为map
        Map<Integer, List<PropertyInfo>> modelAndParentPropertyMap = new HashMap<>();
        for (ModelInfo modelInfo : modelInfoLists) {
            List<PropertyInfo> propertyInfoList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(modelInfo.getPropertyInfos())){
                propertyInfoList.addAll(modelInfo.getPropertyInfos());
            }
            if (!Strings.isNullOrEmpty(modelInfo.getPids()) && CollectionUtils.isNotEmpty(modelInfo.getPropertyInfos())) {
                String pids = modelInfo.getPids().substring(0, modelInfo.getPids().length() - 1);
                List<String> pidList = Arrays.asList(pids.split(","));
                for (String pid : pidList) {
                    if (!com.google.common.base.Strings.isNullOrEmpty(pid) && modelInfoListMap != null && modelInfoListMap.get(Integer.valueOf(pid)) != null) {
                        List<ModelInfo> modelInfoList = modelInfoListMap.get(Integer.valueOf(pid));
                        if (CollectionUtils.isNotEmpty(modelInfoList)) {
                            propertyInfoList.addAll(modelInfoList.get(0).getPropertyInfos());
                        }
                    }
                }
            }
            // //将模型id作为key值，本体模型和所有父模型属性list做为value
            modelAndParentPropertyMap.put(modelInfo.getModelId(), propertyInfoList);
        }
        return modelAndParentPropertyMap;
    }


    /**
     * 获取模型和父模型属性
     *
     * @param modelId
     */
    public List<ModelInfo> getModelInfoAndParent(Integer modelId) {
        List<ModelInfo> modelAndParentInfoList = new ArrayList<>();
        //获取关联模型的父模型属性
        List<ModelInfo> pIdModelInfo = mwModelManageDao.selectModelListWithParent(modelId);
        //将模型id作为key值，转为map
        Map<Integer, List<ModelInfo>> modelInfoListMap = pIdModelInfo.stream().collect(Collectors.groupingBy(ModelInfo::getModelId));
        //将模型id作为key值，本体模型和所有父模型做为value，转为map
        for (ModelInfo modelInfo : pIdModelInfo) {
            if (modelInfo.getModelTypeId().intValue() == 1) {
                if (!com.google.common.base.Strings.isNullOrEmpty(modelInfo.getPids())) {
                    String pids = modelInfo.getPids().substring(0, modelInfo.getPids().length() - 1);
                    List<String> pidList = Arrays.asList(pids.split(","));
                    for (String pid : pidList) {
                        if (!com.google.common.base.Strings.isNullOrEmpty(pid) && modelInfoListMap != null && modelInfoListMap.get(Integer.valueOf(pid)) != null) {
                            //获取每个模型的父模型集合
                            modelAndParentInfoList.addAll(modelInfoListMap.get(Integer.valueOf(pid)));
                        }
                    }
                }
                //将本体模型也加入
                modelAndParentInfoList.add(modelInfo);
            }
        }
        return modelAndParentInfoList;
    }

    @Override
    public Reply getAllAssetsInterfaceByCriteria(AssetsIdsPageInfoParam param) {
        List<AssetsInterfaceDTO> list = new ArrayList<>();
        //页面列表显示的数据
        List<NetListDto> lists = service.getNetDataAllList(param);
        List<String> interFaceNameList = lists.stream().map(s -> s.getInterfaceName()).collect(Collectors.toList());
        try {
            Map map = new HashMap();
            //获取资产的接口信息
            map.put("assetIds", Arrays.asList(param.getId()));
            if (!Strings.isNullOrEmpty(param.getInterfaceName())) {
                map.put("interfaceName", param.getInterfaceName());
            }
            if (!Strings.isNullOrEmpty(param.getInterfaceDescr())) {
                map.put("interfaceDescr", param.getInterfaceDescr());
            }
            if (!Strings.isNullOrEmpty(param.getState())) {
                map.put("state", param.getState());
            }
            if (param.getAlertTag() != null) {
                map.put("alertTag", param.getAlertTag());
            }
            list = mwModelZabbixMonitorDao.getAllInterfaceByCriteria(map);
            //将页面显示的数据ShowFlag设为true;
            for (AssetsInterfaceDTO dto : list) {
                if (interFaceNameList.contains(dto.getName())) {
                    dto.setShowFlag(true);
                } else {
                    dto.setShowFlag(false);
                }
            }
            list = list.stream().sorted(Comparator.comparing(AssetsInterfaceDTO::getShowFlag).reversed().
                    thenComparing(AssetsInterfaceDTO::getName)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getAllAssetsInterfaceByCriteria::获取资产的接口信息失败", e);
            return Reply.fail(500, "获取资产的接口信息失败");
        }
        return Reply.ok(list);
    }

    @Override
    public Reply getAllAssetsInterface(AssetsIdsPageInfoParam param) {
        List<AssetsInterfaceDTO> list = new ArrayList<>();
        try {
            Map map = new HashMap();
            //获取资产的接口信息
            if (!Strings.isNullOrEmpty(param.getId())) {
                map.put("assetIds", Arrays.asList(param.getId()));
            }
            if (!Strings.isNullOrEmpty(param.getInterfaceName())) {
                map.put("interfaceName", param.getInterfaceName());
            }
            if (!Strings.isNullOrEmpty(param.getInterfaceDescr())) {
                map.put("interfaceDescr", param.getInterfaceDescr());
            }
            if (!Strings.isNullOrEmpty(param.getState())) {
                map.put("state", param.getState());
            }
            if (param.getAlertTag() != null) {
                map.put("alertTag", param.getAlertTag());
            }
            list = mwModelZabbixMonitorDao.getAllInterfaceByCriteria(map);
        } catch (Exception e) {
            log.error("getAllAssetsInterfaceByCriteria::获取资产的接口信息失败", e);
            return Reply.fail(500, "获取资产的接口信息失败");
        }
        return Reply.ok(list);
    }


    @Override
    public Reply getAllInterfaceNameAndHostId(List<String> hostIds) {
        List<ModelInterfaceDTO> list = new ArrayList<>();
        try {
            Map map = new HashMap();
            list = mwModelZabbixMonitorDao.getAllInterfaceNameAndHostId(hostIds);
        } catch (Exception e) {
            log.error("getAllInterfaceNameAndHostId::获取资产的接口信息失败", e);
            return Reply.fail(500, "获取资产的接口信息失败");
        }
        return Reply.ok(list);
    }


    @Override
    public List<MwModelInterfaceCommonParam> queryInterfaceInfoAlertTag(MwModelInterfaceCommonParam param) {
        List<MwModelInterfaceCommonParam> list = new ArrayList<>();
        try {
            list = mwModelZabbixMonitorDao.queryInterfaceInfoAlertTag(param);
        } catch (Exception e) {
            log.error("queryInterfaceInfoAlertTag::获取资产告警接口信息失败", e);
        }
        return list;

    }

    @Override
    public List<MwPropertiesValueDTO> getAllAlertModelProperties() {
        List<MwPropertiesValueDTO> valueDTOList = new ArrayList<>();
        try {
            //获取基础设施下的模型及其属性
            List<ModelInfo> modelInfoLists = mwModelManageDao.selectAllModelListWithParent();
            List<PropertyInfo> propertyInfos = new ArrayList<>();
            for (ModelInfo modelInfo : modelInfoLists) {
                if (CollectionUtils.isNotEmpty(modelInfo.getPropertyInfos())) {
                    propertyInfos.addAll(modelInfo.getPropertyInfos());
                }
            }
            List<PropertyInfo> propertyInfoList = propertyInfos.stream().filter(s -> s.getAlertField() != null && s.getAlertField()).collect(Collectors.toList());
            for (PropertyInfo propertyInfo : propertyInfoList) {
                MwPropertiesValueDTO dto = new MwPropertiesValueDTO();
                dto.setModelPropertiesName(propertyInfo.getPropertiesName());
                dto.setModelPropertiesIndexId(propertyInfo.getIndexId());
                valueDTOList.add(dto);
            }
        } catch (Exception e) {
            log.error("queryInterfaceInfoAlertTag::获取资产告警接口信息失败", e);
        }
        return valueDTOList;
    }


    @Override
    public void getModelAssetsChangeMessage(HashMap<String, String> map) {
        if (map.get(AlertEnum.ALERTTITLE.toString()) != null) {
            try {
                String alertTitle = map.get(AlertEnum.ALERTTITLE.toString());
                //资产CPU和内存大小变更告警
                if (alertTitle.contains(CPUCHANGEALTERTAG) || alertTitle.contains(MEMORYCHANGEALTERTAG)) {
                    //获取告警资产的主机Id和Ip信息
                    String hostId = map.get(AlertEnum.HOSTID.toString());
                    String hostIP = map.get(AlertEnum.HOSTIP.toString());
                    //问题详情
                    String problemdetails = map.get(AlertEnum.PROBLEMDETAILS.toString());
                    String operateDes = problemdetails;
                    if (!Strings.isNullOrEmpty(problemdetails)) {
                        if (problemdetails.split(":").length > 1) {
                            operateDes = problemdetails.split(":")[1];
                        }
                    }
                    String alertInfo = "变更信息:";
                    if (alertTitle.contains(CPUCHANGEALTERTAG)) {
                        alertInfo = CPUCHANGEALTERTAG;
                    }
                    if (alertTitle.contains(MEMORYCHANGEALTERTAG)) {
                        alertInfo = MEMORYCHANGEALTERTAG;
                    }
                    //获取资产信息
                    List<Map<String, Object>> assetsDTO = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                            .inBandIp(hostIP).assetsId(hostId).filterQuery(true).build());
                    if (CollectionUtils.isNotEmpty(assetsDTO)) {
                        Map<String, Object> mapDto = assetsDTO.get(0);
                        String instanceName = mapDto.get(INSTANCE_NAME_KEY).toString();
                        String instanceId = mapDto.get(INSTANCE_ID_KEY).toString();
                        String typeName = mapDto.get(ASSETTYPE_NAME).toString();
                        String typeSubName = mapDto.get(ASSETSUBTYPE_NAME).toString();

                        //模型实例变更记录
                        Integer version = mwModelSysLogDao.getChangeHistoryVersion("instance_" + instanceId);
                        if (version != null) {
                            version = version + 1;
                        } else {
                            version = 1;
                        }
                        SystemLogDTO builder = SystemLogDTO.builder().userName("admin").modelName(OperationTypeEnum.CHANGE_INSTANCE.getName())
                                .objName(typeName + "/" + typeSubName + "/" + instanceName)
                                .operateDes(OperationTypeEnum.CHANGE_INSTANCE.getName() + ":" + alertInfo + operateDes).operateDesBefore("-").version(version).build();
                        //添加到模型管理日志
                        mwModelSysLogService.saveInstaceChangeHistory(builder);
                    }
                }
            } catch (Exception e) {
                log.error("获取资产变更信息失败;告警信息:" + map, e);
            }

        }
    }


    @Override
    public void testConnectServer() {
        String remoteHost = "10.180.5.134";
        String username = "root";
        String password = "Mwa2021#";
        String remoteFilePath = "/etc/text/odbc.ini";

        try {
            JSch jsch = new JSch();

            // 创建SSH会话
            Session session = jsch.getSession(username, remoteHost, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            // 打开命令执行通道
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            // 执行读取文件命令
            String readCommand = "cat " + remoteFilePath;
            channel.setCommand(readCommand);
            channel.connect();

            // 读取命令输出
            InputStream inputStream = channel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder fileContentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContentBuilder.append(line).append("\n");
            }

            // 关闭通道和会话
            reader.close();
            channel.disconnect();
            session.disconnect();

            // 处理文件内容
            String fileContent = fileContentBuilder.toString();
            // 在这里进行文件内容的修改操作
            // ...

            // 打印修改后的文件内容
            System.out.println(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertInterface(List<AssetsInterfaceDTO> assetsInterfaceDTOS) {
        try {
            mwModelZabbixMonitorDao.batchInsert(assetsInterfaceDTOS);
        } catch (Exception e) {
            log.error("接口同步zabbix数据保存数据库失败", e);
        }
    }

    @Override
    public Reply getInstanceNameByModelId() {
        try {
            List<String> instanceName = mwModelInstanceDao.getInstanceNameByModelId(modelSystemModelId);
            return Reply.ok(instanceName);
        } catch (Exception e) {
            log.error("getInstanceNameByModelId fail to", e);
            return Reply.fail(500, "获取资产名称失败");
        }
    }

    /**
     * 获取机柜关联的资产数据
     *
     * @return
     */
    @Override
    public Reply getAllInstanceInfoByCabinet() {
        try {
            List<MwModelInstanceCommonParam> allCabinetInfo = mwModelInstanceDao.getAllCabinetInfo();
            Set<Integer> instanceIds = allCabinetInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
            QueryEsParam queryEsParam = new QueryEsParam();
            QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
            qParam.setPropertiesValueList(new ArrayList(instanceIds));
            queryEsParam.setParamLists(Arrays.asList(qParam));
            //查询机柜设备信息
            List<Map<String, Object>> cabinetListMap = getAllInstanceInfoByQueryParam(queryEsParam);
            Map<Integer, String> map = new HashedMap();
            if (CollectionUtils.isNotEmpty(cabinetListMap)) {
                //获取所有机柜中的所属机组Id
                Set<Integer> relationGroupIds = cabinetListMap.stream().filter(s -> intValueConvert(s.get(RELATION_GROUP)) != 0).map(s -> intValueConvert(s.get(RELATION_GROUP))).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(relationGroupIds)) {
                    //根据机组Id去实例表中查询实例名称信息
                    List<QueryInstanceParam> relationGroupInfoList = mwModelInstanceDao.getInstanceNameByIds(new ArrayList<>(relationGroupIds));
                    //将数据组成实例id为key，name为value的map
                    Map<Integer, String> groupNameByIdMap = relationGroupInfoList.stream().collect(Collectors.toMap(s -> s.getModelInstanceId(), s -> s.getInstanceName(), (
                            value1, value2) -> {
                        return value2;
                    }));
                    //将机柜数据转为实例Id为key，机柜实例名称+机组名称为value的map
                    map = cabinetListMap.stream().collect(Collectors.toMap(s -> intValueConvert(s.get(INSTANCE_ID_KEY)),
                            s -> intValueConvert(s.get(RELATION_GROUP)) != 0 && groupNameByIdMap != null && groupNameByIdMap.containsKey(intValueConvert(s.get(RELATION_GROUP))) ?
                                    s.get(INSTANCE_NAME_KEY).toString() + "_" + groupNameByIdMap.get(intValueConvert(s.get(RELATION_GROUP))) :
                                    s.get(INSTANCE_NAME_KEY).toString(), (
                                    value1, value2) -> {
                                return value2;
                            }));
                }
            }

            //查询机柜下属设备
            qParam.setPropertiesIndexId(RELATIONSITECABINET.getField());
            qParam.setPropertiesValueList(new ArrayList(instanceIds));
            queryEsParam.setParamLists(Arrays.asList(qParam));
            List<Map<String, Object>> listMap = getAllInstanceInfoByQueryParam(queryEsParam);

            List<Map<String, Object>> newList = mwModelViewServiceImpl.getAssetsStateByZabbix(listMap);
            Map<Integer, List<Map<String, Object>>> mapGroup = newList.stream().collect(Collectors.groupingBy(s -> intValueConvert(s.get(RELATIONSITECABINET.getField()))));
            Map<String, List<MwTangibleassetsDTO>> dataMap = new HashedMap();
            if (map != null && map.size() > 0) {
                map.forEach((k, v) -> {
                    if (mapGroup != null && mapGroup.containsKey(k)) {
                        List<Map<String, Object>> assetsList = mapGroup.get(k);
                        if (CollectionUtils.isNotEmpty(assetsList)) {
                            List<MwTangibleassetsDTO> mwTangibleassetsTables = new ArrayList<>();
                            try {
                                mwTangibleassetsTables = MwModelUtils.convertEsData(MwTangibleassetsDTO.class, assetsList);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dataMap.put(k + "_" + v, mwTangibleassetsTables);
                        }
                    }
                });
            }
            return Reply.ok(dataMap);
        } catch (Exception e) {
            log.error("getAllInstanceInfoByCabinet fail to", e);
            return Reply.fail(500, "获取机柜关联资产失败");
        }
    }


    /**
     * 根据查询条件进行全局查询
     *
     * @param param
     * @return
     */
    @Override
    public List<Map<String, Object>> getAllInstanceInfoByQueryParam(QueryEsParam param) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            if (CollectionUtils.isNotEmpty(param.getParamLists())) {
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                for (QueryModelInstanceByPropertyIndexParam propertyIndexParam : param.getParamLists()) {
                    if (!Strings.isNullOrEmpty(propertyIndexParam.getPropertiesIndexId())) {
                        QueryBuilder queryBuilder1 = null;
                        if (StringUtils.isNotBlank(propertyIndexParam.getPropertiesValue())) {
                            //字符串类型查询
                            if(ModelPropertiesType.STRING.getCode() == propertyIndexParam.getPropertiesType()){
                                queryBuilder1 = QueryBuilders.termQuery(propertyIndexParam.getPropertiesIndexId()+KEYWORD, propertyIndexParam.getPropertiesValue());
                            }else{
                                queryBuilder1 = QueryBuilders.termQuery(propertyIndexParam.getPropertiesIndexId(), propertyIndexParam.getPropertiesValue());
                            }
                        }
                        if (CollectionUtils.isNotEmpty(propertyIndexParam.getPropertiesValueList())) {
                            //字符串类型查询
                            if(ModelPropertiesType.STRING.getCode() == propertyIndexParam.getPropertiesType()){
                                queryBuilder1 = QueryBuilders.termsQuery(propertyIndexParam.getPropertiesIndexId()+KEYWORD, propertyIndexParam.getPropertiesValueList());
                            }else{
                                queryBuilder1 = QueryBuilders.termsQuery(propertyIndexParam.getPropertiesIndexId(), propertyIndexParam.getPropertiesValueList());
                            }
                        }
                        queryBuilder.must(queryBuilder1);
                    }
                }
                if (CollectionUtils.isNotEmpty(param.getExistsList())) {
                    for (String existsField : param.getExistsList()) {
                        queryBuilder.must(QueryBuilders.existsQuery(existsField));
                    }
                }
                sourceBuilder.query(queryBuilder);//条件查询
            }

            sourceBuilder.from(0);
            sourceBuilder.size(pageSize);
            searchRequest.source(sourceBuilder);
            if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
                searchRequest.indices(String.join(",", param.getModelIndexs()));
            } else {
                searchRequest.indices("mw_*");
            }
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                if (sourceAsMap != null && sourceAsMap.size() > 0)
                    sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
        } catch (Exception e) {
            log.error("查询索引数据失败", e);
        }
        return listMap;

    }

    /**
     * 获取所有机柜下属设备
     *
     * @return
     */
    @Override
    public Reply getCabinetRelationDevice() throws Exception {
        //获取所有的基础数据实例
        List<Map<String, Object>> allInstanceInfoByBase = mwModelInstanceService.getAllInstanceInfoByBase();
        List<Integer> cabinetIds = new ArrayList<>();
        for (Map<String, Object> m : allInstanceInfoByBase) {
            int cabinetId = intValueConvert(m.get(RELATIONSITECABINET.getField()));
            if (cabinetId != 0) {
                cabinetIds.add(cabinetId);
            }
        }

        QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
        params.setModelIndexs(new ArrayList<>());
        params.setInstanceIds(cabinetIds);
        //根据机柜Ids数据，查询所有机柜信息
        List<Map<String, Object>> allCabinetMaps = mwModelViewServiceImpl.selectInstanceInfoByIdsAndModelIndexs(params);

        Map<Integer, Map<String, Object>> cabinetCollect = allCabinetMaps.stream().collect(Collectors.toMap(s -> intValueConvert(s.get(INSTANCE_ID_KEY)), s -> s, (
                value1, value2) -> {
            return value2;
        }));

        List<MwModelInstanceCommonParam> mwModelInstanceCommonParams = new ArrayList<>();
        try {

            for (Map<String, Object> ms : allInstanceInfoByBase) {
                MwModelInstanceCommonParam commonParam = new MwModelInstanceCommonParam();
                String deviceName = strValueConvert(ms.get(INSTANCE_NAME_KEY));
                Integer deviceInstanceId = intValueConvert(ms.get(INSTANCE_ID_KEY));
                Integer deviceModelId = intValueConvert(ms.get(MODEL_ID_KEY));
                String deviceModelIndex = strValueConvert(ms.get(MODEL_INDEX));
                //关联的机柜Id
                Integer relationCabinetId = intValueConvert(ms.get(RELATIONSITECABINET.getField()));

                if (cabinetCollect != null && cabinetCollect.containsKey(relationCabinetId)) {
                    Map<String, Object> cabinetInfoMap = cabinetCollect.get(relationCabinetId);
                    Integer cabinetInstanceId = intValueConvert(cabinetInfoMap.get(INSTANCE_ID_KEY));
                    String cabinetName = strValueConvert(cabinetInfoMap.get(INSTANCE_NAME_KEY));
                    String cabinetCode = strValueConvert(cabinetInfoMap.get(INSTANCE_CODE));
                    commonParam.setRelationInstanceId(cabinetInstanceId);
                    commonParam.setRelationInstanceName(cabinetName);
                    commonParam.setCabinetPosition(cabinetCode);
                }
                commonParam.setModelId(deviceModelId);
                commonParam.setModelIndex(deviceModelIndex);
                commonParam.setModelInstanceId(deviceInstanceId);
                commonParam.setModelInstanceName(deviceName);
                mwModelInstanceCommonParams.add(commonParam);
            }
        } catch (Exception e) {
            log.error("获取所有机柜下属设备失败", e);
        }
        return Reply.ok(mwModelInstanceCommonParams);
    }

    /**
     * 根据实例Id获取接口信息，包括状态
     * 从mw_cmdbmd_assets_interface表获取
     *
     * @return
     */
    @Override
    public Reply getInterfaceInfosByAssetsIds(MwModelAssetsInterfaceParam param) {
        List<ModelInterfaceDTO> interfaceInfoByAssetsId = new ArrayList<>();
        try {
            AssetsIdsPageInfoParam assetsIdsPageInfoParam = new AssetsIdsPageInfoParam();
            Map<String, Object> map = mwModelInstanceService.selectInfoByInstanceId(intValueConvert(param.getDeviceId()));
            if (map != null) {
                assetsIdsPageInfoParam.setId(param.getDeviceId());
                assetsIdsPageInfoParam.setMonitorServerId(intValueConvert(map.get(MONITOR_SERVER_ID)));
                assetsIdsPageInfoParam.setAssetsId(strValueConvert(map.get(ASSETS_ID)));
                assetsIdsPageInfoParam.setAssetsIp(strValueConvert(map.get(IN_BAND_IP)));
            }
            List<NetListDto> lists = service.getNetDataAllList(assetsIdsPageInfoParam);
            for (NetListDto netListDto : lists) {
                ModelInterfaceDTO modelInterfaceDTO = JSONObject.parseObject(JSONObject.toJSONString(netListDto), ModelInterfaceDTO.class);
                modelInterfaceDTO.setName(netListDto.getInterfaceName());
                interfaceInfoByAssetsId.add(modelInterfaceDTO);
            }
        } catch (Exception e) {
            log.error("根据实例Id获取接口信息失败", e);
        }
        return Reply.ok(interfaceInfoByAssetsId);
    }

    @Override
    public List<MwModelPropertyInfo> getModelPropertyInfoByModelId(Integer modelId) {
        List<MwModelPropertyInfo> mwModelPropertyInfoList = new ArrayList<>();
        List<ModelInfo> modelInfoAndParent = getModelInfoAndParent(modelId);
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(modelInfoAndParent)) {
            for (ModelInfo modelInfo : modelInfoAndParent) {
                propertyInfoList.addAll(modelInfo.getPropertyInfos());
            }
        }
        if (CollectionUtils.isNotEmpty(propertyInfoList)) {
            for (PropertyInfo propertyInfo : propertyInfoList) {
                MwModelPropertyInfo modelPropertyInfo = new MwModelPropertyInfo();
                modelPropertyInfo.setPropertyIndexId(propertyInfo.getIndexId());
                modelPropertyInfo.setPropertyName(propertyInfo.getPropertiesName());
                mwModelPropertyInfoList.add(modelPropertyInfo);
            }
        }
        return mwModelPropertyInfoList;
    }


}
