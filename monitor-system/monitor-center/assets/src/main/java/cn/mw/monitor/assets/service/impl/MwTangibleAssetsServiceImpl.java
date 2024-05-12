package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.api.param.org.QueryOrgForDropDown;
import cn.mw.monitor.assets.api.exception.AssetsException;
import cn.mw.monitor.assets.api.param.assets.QueryLabelParam;
import cn.mw.monitor.assets.api.param.assets.UpdateMonStateParam;
import cn.mw.monitor.assets.api.param.assets.UpdateSetStateParam;
import cn.mw.monitor.assets.dao.MwAssetsInterfaceDao;
import cn.mw.monitor.assets.dao.MwAssetsTypeDao;
import cn.mw.monitor.assets.dao.MwMacrosDao;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.assets.dto.GroupHosts;
import cn.mw.monitor.assets.model.*;
import cn.mw.monitor.assets.param.MwTangibleAssetsSyncPush;
import cn.mw.monitor.assets.service.CheckTangibleAssetsListener;
import cn.mw.monitor.assets.service.MwAssetsNewFieldService;
import cn.mw.monitor.assets.utils.AssetsSyncKafkaProducerUtil;
import cn.mw.monitor.assets.utils.ZabbixUtils;
import cn.mw.monitor.assetsTemplate.dao.MwAseetstemplateTableDao;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.engineManage.dao.MwEngineManageTableDao;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.link.dao.MWNetWorkLinkDao;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.alert.dto.MWAlertAssetsParam;
import cn.mw.monitor.service.assets.api.IMWAssetsListener;
import cn.mw.monitor.service.assets.api.IMWBatchAssetsProcFinListener;
import cn.mw.monitor.service.assets.api.MwOutbandAssetsService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.BatchDeleteAssetsEvent;
import cn.mw.monitor.service.assets.event.UpdateTangibleassetsEvent;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.*;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.*;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.dto.UpdateDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.service.webmonitor.model.HttpParam;
import cn.mw.monitor.service.zbx.exception.HostCreatFailException;
import cn.mw.monitor.service.zbx.model.HostCreatResult;
import cn.mw.monitor.service.zbx.model.HostCreateParam;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dao.MWOrgDao;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.webMonitor.api.param.webMonitor.DeleteWebMonitorParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.UpdateWebMonitorStateParam;
import cn.mw.monitor.webMonitor.dao.MwWebmonitorTableDao;
import cn.mw.monitor.webMonitor.service.MwWebMonitorService;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.assets.utils.RuleType.getInfoByName;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

/**
 * Created by baochengbin on 2020/3/12.
 */
@Service
@Slf4j
@Transactional
public class MwTangibleAssetsServiceImpl extends ListenerService implements MwTangibleAssetsService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/assets");
    private static final Integer OTHER_DEVICE_TYPE = 21;

    private static final int successCode = 0;

    private Set<Integer> monitorServerSet = new CopyOnWriteArraySet<>();

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MwOutbandAssetsService mwOutbandAssetsService;

    @Autowired
    private MWOrgService mwOrgService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Resource
    private MwAseetstemplateTableDao mwAseetstemplateTableDao;
    @Resource
    private AssetsSyncKafkaProducerUtil assetsSyncKafkaProducerUtil;

    @Resource
    private MwMacrosDao mwMacrosDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MWUserCommonService userCommonService;

    @Resource
    private MwEngineManageTableDao mwEngineManageTableDao;
    @Resource
    private MwAssetsTypeDao mwAssetsTypeDao;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Resource
    private MWOrgDao mworgDao;

    @Autowired
    private LicenseManagementService licenseManagement;

    @Autowired
    private MwAssetsNewFieldService assetsNewFieldService;

    @Resource
    private MwAssetsInterfaceDao mwAssetsInterfaceDao;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private ZabbixUtils zabbixUtils;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;
    @Value("${assets.push.enable}")
    private boolean assetsPush;
    @Value("${mg.area.label}")
    private String mgAreaLabel;

    @Autowired
    public void addchecks(List<CheckTangibleAssetsListener> checklisteners) {
        for (CheckTangibleAssetsListener checkTangibleAssetsListener : checklisteners) {
            log.info("MwTangibleAssetsServiceImpl addchecks:" + checkTangibleAssetsListener.getClass().getCanonicalName());
        }
        addCheckLists(checklisteners);
    }

    @Autowired
    public void addpost(List<IMWAssetsListener> postlisteners) {
        for (IMWAssetsListener imwAssetsListener : postlisteners) {
            log.info("MwTangibleAssetsServiceImpl add post:" + imwAssetsListener.getClass().getCanonicalName());
        }
        addPostProcessorList(postlisteners);
    }

    @Autowired
    public void addFinish(List<IMWBatchAssetsProcFinListener> finishlisteners) {
        for (IMWBatchAssetsProcFinListener imwAssetsListener : finishlisteners) {
            log.info("MwTangibleAssetsServiceImpl add finish:{}", imwAssetsListener.getClass().getCanonicalName());
        }
        addFinishProcessorList(finishlisteners);
    }

    @Override
    public Reply getAssetsTypesTree(QueryAssetsTypeParam param) {
        //请求参数
        Map<String, Object> queryParam = new HashMap<>();
        //资产树状结构数据
        List<AssetsTreeDTO> treeDTOS = new ArrayList<>();
        String moduleType = "";
        String tableName = "";
        String assetsSubTypeId = "";
        int settingEnable = 0;
        boolean isDept = false;
        //获取当前用户的查询限制条件（数据权限，组织ID，用户组ID，用户ID）
        getUserPerm(queryParam);
        switch (param.getTableType()) {
            case 1:
                moduleType = DataType.ASSETS.getName();
                tableName = "mw_tangibleassets_table";
                assetsSubTypeId = "assets_type_sub_id";
                if (param.getSettingEnable() != null) {
                    settingEnable = param.getSettingEnable();
                }
                break;
            case 2:
                moduleType = DataType.INASSETS.getName();
                tableName = "mw_intangibleassets_table";
                assetsSubTypeId = "sub_assets_type_id";
                if (param.getAssetsTypeId() == 1) {
                    param.setAssetsTypeId(2);
                }
                break;
            case 3:
                moduleType = DataType.OUTBANDASSETS.getName();
                tableName = "mw_outbandassets_table";
                assetsSubTypeId = "assets_type_sub_id";
                break;
            default:
                return Reply.fail("查询资产分类信息失败！");
        }
        queryParam.put("moduleType", moduleType);
        queryParam.put("tableName", tableName);
        queryParam.put("assetsSubTypeId", assetsSubTypeId);
        queryParam.put("settingEnable", settingEnable);
        AssetsTreeDTO unknown = new AssetsTreeDTO();
        try {
            switch (param.getAssetsTypeId()) {
                case 0:
                    break;
                case 1://品牌（加品牌图标）
                    treeDTOS = mwAssetsTypeDao.selectAssetsVendorList(queryParam);
                    break;
                case 2://资产类型（资产类型图标待加）
                    treeDTOS = mwAssetsTypeDao.selectAssetsTypeList(queryParam);
                    break;
                case 3://标签
                    List<AssetsDTO> ids = new ArrayList<>();
                    List<AssetsTreeDTO> dtos = mwAssetsTypeDao.selectAssetsLabelList(queryParam);
                    //将标签中文本框含有相同标签名称的归为一类
                    if (settingEnable == 1) {
                        treeDTOS.addAll(formatAssets(dtos, ids));
                    } else {
                        for (AssetsTreeDTO treeDTO : dtos) {
                            if (treeDTO.getAssetsList() != null && treeDTO.getAssetsList().size() > 0) {
                                ids.addAll(treeDTO.getAssetsList());
                            }
                            HashMap<String, AssetsTreeDTO> typeNameMap = new HashMap<>();
                            List<AssetsTreeDTO> children = treeDTO.getChildren();
                            for (AssetsTreeDTO child : children) {
                                String labelName = child.getTypeName();
                                if (typeNameMap.containsKey(labelName)) {
                                    AssetsTreeDTO dto = new AssetsTreeDTO();
                                    //合并相同的key的value
                                    List<AssetsDTO> assetsList = typeNameMap.get(labelName).getAssetsList();
                                    assetsList.addAll(child.getAssetsList());
                                    dto.setTypeName(labelName);
                                    dto.setTypeId(typeNameMap.get(labelName).getTypeId());
                                    dto.setAssetsList(assetsList);
                                    dto.setUuid(child.getUuid());
                                } else {
                                    typeNameMap.put(labelName, child);
                                }
                                if (child.getAssetsList() != null && child.getAssetsList().size() > 0) {
                                    ids.addAll(treeDTO.getAssetsList());
                                }
                            }
                            List<AssetsTreeDTO> newList = new ArrayList<>();
                            for (String labelName : typeNameMap.keySet()) {
                                newList.add(typeNameMap.get(labelName));
                            }
                            //将新的子级list存入
                            treeDTO.setChildren(newList);
                        }
                        treeDTOS.addAll(dtos);
                    }

                    ids = ids.stream().distinct().collect(Collectors.toList());
//                    AssetsTreeDTO unknown = new AssetsTreeDTO();
                    List<AssetsDTO> assetsDTOS = mwAssetsTypeDao.selectAllAssets(queryParam);
                    if (ids.size() > 0 && assetsDTOS != null && assetsDTOS.size() > 0) {
                        assetsDTOS.removeAll(ids);
                    }
                    if (CollectionUtils.isNotEmpty(assetsDTOS)) {
                        unknown.setTypeName("未知");
                        unknown.setTypeId(0);
                        unknown.setAssetsList(assetsDTOS);
                    }
                    break;
                case 4://用户
                    break;
                case 5://用户组
                    break;
                case 6://机构
                    isDept = true;
                    List<AssetsDTO> orgIds = new ArrayList<>();
                    HashSet<String> assetsIdSet = new HashSet<>();
                    HashSet<Integer> userOrgSet = new HashSet<>();
                    Reply userOrgReply = mwOrgService.selectDorpdownList(new QueryOrgForDropDown());
                    if (userOrgReply.getRes() == PaasConstant.RES_SUCCESS) {
                        List<MWOrgDTO> orgList = (List<MWOrgDTO>) userOrgReply.getData();
                        getUserOrgSet(orgList, userOrgSet);
                    }
                    List<MWOrgDTO> orgList = mwOrgService.getAllOrgList();
                    //判断当前用户是否时LDAP用户
                    assetsTreeCheckLdapUser(queryParam, orgList);
                    if (CollectionUtils.isNotEmpty(orgList)) {
                        for (MWOrgDTO org : orgList) {
                            AssetsTreeDTO assetsTreeDTO = new AssetsTreeDTO();
                            assetsTreeDTO.setTypeName(org.getOrgName());
                            assetsTreeDTO.setTypeId(org.getOrgId());
                            List<AssetsDTO> assetsDTOList = new ArrayList<>();
                            if (userOrgSet.contains(org.getOrgId())) {
                                queryParam.put("orgId", org.getOrgId());
                                assetsDTOList = mwAssetsTypeDao.selectAssetsOrgList(queryParam);
                                if (CollectionUtils.isNotEmpty(assetsDTOList)) {
                                    Iterator iterator = assetsDTOList.iterator();
                                    while (iterator.hasNext()) {
                                        AssetsDTO assetsDTO = (AssetsDTO) iterator.next();
                                        if (moduleType.equals(DataType.INASSETS.getName())) {
                                            if (assetsDTO != null) {
                                                orgIds.add(assetsDTO);
                                                assetsIdSet.add(assetsDTO.getId());
                                            } else {
                                                iterator.remove();
                                            }
                                        } else {
                                            if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsId())) {
                                                orgIds.add(assetsDTO);
                                                assetsIdSet.add(assetsDTO.getId());
                                            } else {
                                                iterator.remove();
                                            }
                                        }
                                    }
                                }
                            }
                            if (org.getChilds() != null && org.getChilds().size() > 0) {
                                assetsDTOList.addAll(getChildOrgAssetsList(queryParam, assetsTreeDTO, org.getChilds(),
                                        orgIds, assetsIdSet, userOrgSet));
                            }
                            if (CollectionUtils.isNotEmpty(assetsDTOList)) {
                                assetsDTOList = assetsDTOList.stream().distinct().collect(Collectors.toList());
                                assetsTreeDTO.setAssetsList(assetsDTOList);
                                treeDTOS.add(assetsTreeDTO);
                            } else {
                                assetsTreeDTO.setAssetsList(assetsDTOList);
                                treeDTOS.add(assetsTreeDTO);
                            }
                        }
                    }
                    //根据用户是否存在用户组数据，判断是否需要增加对应的资产数据
                    if (queryParam.containsKey("groupIds") && !(boolean) queryParam.get("isAdmin")) {
                        //获取用户组下的所有资产数据
                        List<AssetsDTO> assetsList = mwAssetsTypeDao.selectAssetsByGroupIds(queryParam);
                        updateAssetsList(queryParam, treeDTOS, assetsIdSet, assetsList);
                    }
                    //若查询存在用户ID数据，则需要根据用户ID获取资产数据
                    if (queryParam.containsKey("userId") && !(boolean) queryParam.get("isAdmin")) {
                        //获取用户下的所有资产数据
                        List<AssetsDTO> assetsList = mwAssetsTypeDao.selectAssetsByUserId(queryParam);
                        updateAssetsList(queryParam, treeDTOS, assetsIdSet, assetsList);
                    }
                    //删除空白的组织资产数据
                    sortAssetsTreeList(treeDTOS);
                    //先将机构分组
                    treeDTOS = getChildrenList(treeDTOS);
                    //根据数据进行资产类型的分组
                    treeListAssetsTypeGroup(treeDTOS);
//                    orgIds = orgIds.stream().distinct().collect(Collectors.toList());
//                    AssetsTreeDTO orgUnknown = new AssetsTreeDTO();
//                    List<AssetsDTO> orgAssetsDTOS = mwAssetsTypeDao.selectAllAssets(queryParam);
//                    if (orgIds.size() > 0 && orgAssetsDTOS != null && orgAssetsDTOS.size() > 0) {
//                        orgAssetsDTOS.removeAll(orgIds);
//                    }
//                    if (CollectionUtils.isNotEmpty(orgAssetsDTOS)) {
//                        unknown.setTypeName("未知");
//                        unknown.setTypeId(0);
//                        unknown.setAssetsList(orgAssetsDTOS);
//                    }
                    break;
                case 8://用户组
                    treeDTOS = mwAssetsTypeDao.selectAssetsUserGroupList(queryParam);
                    if (CollectionUtils.isNotEmpty(treeDTOS)) {
                        for (AssetsTreeDTO treeDTO : treeDTOS) {
                            treeDTO.setChildren(null);
                        }
                    }
                default:
                    break;
            }
            Map<Integer, Set<String>> hostIdMap = new HashMap<>();
            List<String> assetsIds = new ArrayList<>();
            if ("未知".equals(unknown.getTypeName())) {
                treeDTOS.add(unknown);
            }
            getAssetsHostId(treeDTOS, hostIdMap, assetsIds);
            //查询资产监控状态
            List<String> ids = mwAssetsTypeDao.selectAssetsMonitorStatus(assetsIds);
            //获取资产所有状态
            Map<String, String> allAssetsStatus = getAllAssetsStatus(hostIdMap);
            //递归整理，赋值整体状态
//            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
//            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            long time1 = System.currentTimeMillis();
            count = 0;
            treeDTOS = treeList(treeDTOS, allAssetsStatus, ids);
//            int count = 0;
//            for (AssetsTreeDTO treeDTO : treeDTOS) {
//                List<AssetsTreeDTO> children = treeDTO.getChildren();
//                count = count+1;
//                treeDTO.setUuid(count+"");
//                if(CollectionUtils.isNotEmpty(children)){
//                    for (AssetsTreeDTO child : children) {
//                        count = count+1;
//                        child.setUuid(count+"");
//                        List<AssetsTreeDTO> children1 = child.getChildren();
//                        if(CollectionUtils.isNotEmpty(children1)){
//                            for (AssetsTreeDTO assetsTreeDTO : children1) {
//                                count = count+1;
//                                assetsTreeDTO.setUuid(count+"");
//                            }
//                        }
//                    }
//                }
//            }
            long time2 = System.currentTimeMillis();
            if (!isDept) {
                treeDTOS = getChildrenList(treeDTOS);
            }
            long time3 = System.currentTimeMillis();
//            if ("未知".equals(unknown.getTypeName())) {
//                treeDTOS.add(unknown);
//            }
            //将未知放在最前面
            AssetsTreeDTO handleUnknown = null;
            if (!CollectionUtils.isEmpty(treeDTOS)) {
                Iterator<AssetsTreeDTO> iterator = treeDTOS.iterator();
                while (iterator.hasNext()) {
                    AssetsTreeDTO assetsTreeDTO = iterator.next();
                    if ("未知".equals(assetsTreeDTO.getTypeName())) {
                        handleUnknown = assetsTreeDTO;
                        iterator.remove();
                    }
                }
                if (handleUnknown != null) {
                    treeDTOS.add(0, handleUnknown);
                }
            }
//            //System.out.println("测试递归时间 time1t:" + (time2-time1) + "time2" + (time3-time2));
            //判断是否需要最低级的状态数据
            if (param.getIsFlagStatus() == null || !param.getIsFlagStatus()) {
                treeAssetsStstusClassIfy(treeDTOS, allAssetsStatus, ids);
            }
            return Reply.ok(treeDTOS);
        } catch (Exception e) {
            log.error("fail to getAssetsTypesTree with param={}, cause:{}", param, e);
            return Reply.fail("查询资产分类信息失败！");
        }
    }

    /**
     * 更新资产树结构
     *
     * @param queryParam  查询SQL参数
     * @param treeDTOS    资产树结构列表
     * @param assetsIdSet 已添加的资产ID数据
     * @param assetsList  资产数据列表
     */
    private void updateAssetsList(Map<String, Object> queryParam, List<AssetsTreeDTO> treeDTOS,
                                  HashSet<String> assetsIdSet, List<AssetsDTO> assetsList) {
        for (AssetsDTO assets : assetsList) {
            if (assetsIdSet.contains(assets.getId())) {
                continue;
            } else {
                //将该资产加入资产树状结构中
                queryParam.put("assetsId", assets.getId());
                List<OrgDTO> list = mworgDao.selectOrgByParams(queryParam);
                for (OrgDTO org : list) {
                    //将该资产插入到对应的节点上
                    updateAssetsToTree(treeDTOS, org, assets);
                }
                //获取该资产的所有机构数据
                assetsIdSet.add(assets.getId());
            }
        }
    }

    /**
     * 整理资产树列表 空白资产列表直接删除
     *
     * @param treeDTOS 资产树
     */
    private List<AssetsDTO> sortAssetsTreeList(List<AssetsTreeDTO> treeDTOS) {
        Iterator iterator = treeDTOS.iterator();
        List<AssetsDTO> allList = new ArrayList<>();
        while (iterator.hasNext()) {
            AssetsTreeDTO treeNode = (AssetsTreeDTO) iterator.next();
            List<AssetsDTO> lowerList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(treeNode.getChildren())) {
                lowerList = sortAssetsTreeList(treeNode.getChildren());
            } else {
                if (CollectionUtils.isEmpty(treeNode.getAssetsList())) {
                    //删除该节点
                    iterator.remove();
                    continue;
                }
            }
            if (CollectionUtils.isNotEmpty(treeNode.getAssetsList())) {
                List<AssetsDTO> list = treeNode.getAssetsList();
                list.addAll(lowerList);
                list = distinctList(list);
                treeNode.setAssetsList(list);
                allList.addAll(list);
            } else {
                if (CollectionUtils.isNotEmpty(lowerList)) {
                    treeNode.setAssetsList(distinctList(lowerList));
                    allList.addAll(lowerList);
                } else {
                    iterator.remove();
                }
            }
        }
        return distinctList(allList);
    }

    /**
     * 去重资产数据
     *
     * @param list 资产数据
     * @return 去重后的资产数据
     */
    private List<AssetsDTO> distinctList(List<AssetsDTO> list) {
        List<AssetsDTO> newList = new ArrayList<>();
        Set<String> assetsSet = new HashSet<>();
        for (AssetsDTO assets : list) {
            if (!assetsSet.contains(assets.getId())) {
                assetsSet.add(assets.getId());
                newList.add(assets);
            }
        }
        return newList;
    }

    /**
     * 更新资产到资产树里
     *
     * @param treeDTOS 资产树
     * @param org      组织/机构
     * @param assets   资产
     */
    private void updateAssetsToTree(List<AssetsTreeDTO> treeDTOS, OrgDTO org, AssetsDTO assets) {
        for (AssetsTreeDTO treeNode : treeDTOS) {
            if (treeNode.getTypeId() == org.getOrgId()) {
                if (CollectionUtils.isNotEmpty(treeNode.getAssetsList())) {
                    treeNode.getAssetsList().add(assets);
                } else {
                    List<AssetsDTO> list = new ArrayList<>();
                    list.add(assets);
                    treeNode.setAssetsList(list);
                }
            } else {
                if (CollectionUtils.isNotEmpty(treeNode.getChildren())) {
                    updateAssetsToTree(treeNode.getChildren(), org, assets);
                }
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
     * 获取子级机构的资产数据
     *
     * @param queryParam    查询参数
     * @param assetsTreeDTO 上级资产数据
     * @param childs        子机构列表数据
     * @param orgIds        已添加的资产数据
     * @param assetsIdSet   已添加的资产ID
     * @param userOrgSet    用户的机构ID集合
     */
    private List<AssetsDTO> getChildOrgAssetsList(Map<String, Object> queryParam, AssetsTreeDTO assetsTreeDTO,
                                                  List<MWOrgDTO> childs, List<AssetsDTO> orgIds,
                                                  HashSet<String> assetsIdSet, HashSet<Integer> userOrgSet) {
        //当前机构及子机构的所有资产数据
        List<AssetsDTO> allAssetsList = new ArrayList<>();
        for (MWOrgDTO child : childs) {
            //下级的资产数据
            List<AssetsDTO> lowerAssetsList = new ArrayList<>();
            //下级资产树状数据
            AssetsTreeDTO assetsTreeChild = new AssetsTreeDTO();
            assetsTreeChild.setTypeName(child.getOrgName());
            assetsTreeChild.setTypeId(child.getOrgId());
            List<AssetsDTO> childAssetsDTOList = new ArrayList<>();
            if (userOrgSet.contains(child.getOrgId())) {
                queryParam.put("orgId", child.getOrgId());
                //获取当前机构的资产数据
                childAssetsDTOList = mwAssetsTypeDao.selectAssetsOrgList(queryParam);
                if (CollectionUtils.isNotEmpty(childAssetsDTOList)) {
                    Iterator iterator = childAssetsDTOList.iterator();
                    while (iterator.hasNext()) {
                        AssetsDTO assetsDTO = (AssetsDTO) iterator.next();
                        if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsId())) {
                            orgIds.add(assetsDTO);
                            assetsIdSet.add(assetsDTO.getId());
                        } else {
                            iterator.remove();
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(child.getChilds())) {
                lowerAssetsList.addAll(getChildOrgAssetsList(queryParam, assetsTreeChild, child.getChilds(), orgIds, assetsIdSet, userOrgSet));
            }
            if (CollectionUtils.isNotEmpty(childAssetsDTOList) || CollectionUtils.isNotEmpty(lowerAssetsList)) {
                childAssetsDTOList.addAll(lowerAssetsList);
                childAssetsDTOList.stream().distinct().collect(Collectors.toList());
                assetsTreeChild.setAssetsList(childAssetsDTOList);
                assetsTreeDTO.addChild(assetsTreeChild);
                allAssetsList.addAll(childAssetsDTOList);
            } else {
                childAssetsDTOList.addAll(lowerAssetsList);
                childAssetsDTOList.stream().distinct().collect(Collectors.toList());
                assetsTreeChild.setAssetsList(childAssetsDTOList);
                assetsTreeDTO.addChild(assetsTreeChild);
            }
        }
        allAssetsList = allAssetsList.stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(assetsTreeDTO.getAssetsList())) {
            assetsTreeDTO.getAssetsList().addAll(allAssetsList);
        } else {
            assetsTreeDTO.setAssetsList(allAssetsList);
        }
        return allAssetsList;
    }

    /**
     * 获取用户查询资产的限制条件
     *
     * @param queryParam 查询参数
     */
    private void getUserPerm(Map<String, Object> queryParam) {
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        //数据权限：private public
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);
        //用户角色是否为系统管理员
        Boolean isAdmin = false;
        //用户所在的用户组id
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            queryParam.put("groupIds", groupIds);
        }
        switch (dataPermission) {
            case PRIVATE:
                queryParam.put("userId", userId);
                break;
            case PUBLIC:
                String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                List<Integer> orgIds = new ArrayList<>();
                if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                    isAdmin = true;
                }
                if (!isAdmin) {
                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                }
                if (null != orgIds && orgIds.size() > 0) {
                    queryParam.put("orgIds", orgIds);
                }
                break;
        }
        queryParam.put("isAdmin", isAdmin);
        queryParam.put("perm", dataPermission.getName());
    }

    /**
     * 更新通过标签获取资产数据
     *
     * @param dtos
     * @param ids  已经选中的标签数据
     * @return
     */
    private List<AssetsTreeDTO> formatAssets(List<AssetsTreeDTO> dtos, List<AssetsDTO> ids) {
        List<AssetsTreeDTO> newList = new ArrayList<>();
        for (AssetsTreeDTO treeDTO : dtos) {
            HashMap<String, AssetsTreeDTO> typeNameMap = new HashMap<>();
            List<AssetsTreeDTO> children = treeDTO.getChildren();
            for (AssetsTreeDTO child : children) {
                String labelName = child.getTypeName();
                if (typeNameMap.containsKey(labelName)) {
                    //合并相同的key的value
                    List<AssetsDTO> assetsList = typeNameMap.get(labelName).getAssetsList();
                    List<AssetsDTO> list = child.getAssetsList();
                    for (AssetsDTO assetsDTO : list) {
                        if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsName())) {
                            assetsList.add(assetsDTO);
                            ids.add(assetsDTO);
                        }
                    }
                } else {
                    List<AssetsDTO> assetsList = child.getAssetsList();
                    List<AssetsDTO> newAssetsList = new ArrayList<>();
                    for (AssetsDTO assetsDTO : assetsList) {
                        if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsName())) {
                            newAssetsList.add(assetsDTO);
                            ids.add(assetsDTO);
                        }
                    }
                    child.setAssetsList(newAssetsList);
                    typeNameMap.put(labelName, child);
                }
            }
            List<AssetsTreeDTO> childList = new ArrayList<>();
            for (String labelName : typeNameMap.keySet()) {
                childList.add(typeNameMap.get(labelName));
            }
            //将新的子级list存入
            if (CollectionUtils.isNotEmpty(childList)) {
                treeDTO.setChildren(childList);
                List<AssetsDTO> newAssetsList = new ArrayList<>();
                List<AssetsDTO> assetsList = treeDTO.getAssetsList();
                for (AssetsDTO assetsDTO : assetsList) {
                    if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsName())) {
                        newAssetsList.add(assetsDTO);
                        ids.add(assetsDTO);
                    }
                }
                treeDTO.setAssetsList(newAssetsList);
                newList.add(treeDTO);
            }
        }
        return newList;
    }

    //获取子节点集合
    private List<AssetsTreeDTO> getChildrenList(List<AssetsTreeDTO> child) {
        if (child != null && child.size() > 0) {
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            return child.stream().peek((c) -> c.setChildren(getChildrenList(c.getChildren())))
                    .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getTypeName()), pinyin4jUtil.getStringPinYin(o2.getTypeName())))
                    .collect(Collectors.toList());
        }
        return child;

    }

    private Integer count = 0;

    /**
     * 递归实现状态赋值
     *
     * @param list
     * @return
     */
    public List<AssetsTreeDTO> treeList(List<AssetsTreeDTO> list, Map<String, String> statusMap, List<String> ids) {
        if (list != null && list.size() > 0) {
            list.forEach(tree -> {
                if ("unknown".equals(tree.getTypeName())) {
                    tree.setTypeName("未知");
                }
                count = count + 1;
                tree.setUuid(count + "");
                tree.setStatusUrl(getOverAllStatus(tree.getAssetsList(), statusMap, ids));
                List<AssetsTreeDTO> children = tree.getChildren();
                tree.setChildren(treeList(children, statusMap, ids));
            });
        }
        return list;
    }

    /**
     * 根据资产ID取机构信息
     *
     * @param assetsId 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(String assetsId) {
        try {
            MwTangibleassetsDTO mwTangAssets = new MwTangibleassetsDTO();
            if (modelAssetEnable) {//是否走资源中心资产
                mwTangAssets = mwModelViewCommonService.findModelAssetsByInstanceId(Integer.parseInt(assetsId));
                mwTangAssets.setId(String.valueOf(mwTangAssets.getModelInstanceId()));
                mwTangAssets.setAssetsName(mwTangAssets.getHostName());
            } else {
                mwTangAssets = mwTangibleAssetsDao.selectById(assetsId);
            }
            MwTangibleassetsByIdDTO mdto = mwModelViewCommonService.doSelectById(mwTangAssets);
            logger.info("ACCESS_LOG[]TangibleAssets[]有形资产管理[]根据自增序列ID取资产信息[]{}", assetsId);
            return Reply.ok(mdto);
        } catch (Exception e) {
            log.error("fail to selectById with d={}", assetsId, e);
        }
        return Reply.fail(ErrorConstant.TANGASSETSCODE_210100, ErrorConstant.TANGASSETS_MSG_210100);
    }


    /**
     * 查询所有资产信息及关联信息
     *
     * @param
     * @return
     */
    public Reply selectListWithExtend() {
        //snmpv1, v2, v3的monitorMode是相同的
        Map map = new HashMap();
        map.put("monitorMode", RuleType.SNMPv1v2.getMonitorMode());
        List mwTangAssetses = mwTangibleAssetsDao.selectListWithExtend(map);
        return Reply.ok(mwTangAssetses);
    }

    public Reply selectListWithExtend(Map map) {
        List mwTangAssetses = mwTangibleAssetsDao.selectListWithExtend(map);
        //需要获取机构、用户组、负责人信息
        List assetsDtos = getAssetsUsreNews(mwTangAssetses);
        return Reply.ok(assetsDtos);
    }


    /**
     * 仅查询所有资产信息,不返回资产表关联的其他表的信息
     *
     * @param
     * @return
     */
    public Reply selectTopoAssetsList() {
        List mwTangAssetses = mwTangibleAssetsDao.selectTopoAssetsList();
        return Reply.ok(mwTangAssetses);
    }

    /**
     * 仅查询所有vxlan资产信息,不返回资产表关联的其他表的信息
     *
     * @param
     * @return
     */
    public Reply selectVXLanAssetsList() {
        List mwTangAssetses = mwTangibleAssetsDao.selectVXLanAssetsList();
        return Reply.ok(mwTangAssetses);
    }

    public Reply selectTopoAssetsList(Map map) {
        List<MwTangibleassetsTable> mwTangAssetses = mwTangibleAssetsDao.selectTopoAssetsList(map);
        if (map.get("keyName").equals(MwTangibleassetsDTO.class.getName())) {
            List<MwTangibleassetsDTO> list = new ArrayList<>();
            for (MwTangibleassetsTable b : mwTangAssetses) {
                MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
                BeanUtils.copyProperties(b, mwTangibleassetsDTO);
                list.add(mwTangibleassetsDTO);
            }
            return Reply.ok(list);
        } else {
            return Reply.ok(mwTangAssetses);
        }

    }

    /**
     * 查询资产信息list
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply selectList(QueryTangAssetsParam qParam) {
        try {
            List<MwTangibleassetsTable> mwTangAssetses = new ArrayList();
            List<String> ids = qParam.getAssetsIds();
            if (ids != null && ids.size() == 0) {
                PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                pageInfo.setList(mwTangAssetses);

                logger.info("ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);

                return Reply.ok(pageInfo);
            }

            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限：private public
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);//用户所在的用户组id
            if (null != groupIds && groupIds.size() > 0) {
                qParam.setGroupIds(groupIds);
            }
            if (null != qParam.getLogicalQueryLabelParamList() && qParam.getLogicalQueryLabelParamList().size() > 0) {
                List<String> assetsIds = mwLabelCommonServcie.getTypeIdsByLabel(qParam.getLogicalQueryLabelParamList());
                if (null != assetsIds && assetsIds.size() > 0) {
                    if (ids != null && ids.size() > 0) {
                        assetsIds.retainAll(ids);
                    }
                    if (null != assetsIds && assetsIds.size() > 0) {
                        qParam.setAssetsIds(assetsIds);
                    } else {
                        PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                        pageInfo.setList(mwTangAssetses);

                        logger.info("ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);

                        return Reply.ok(pageInfo);
                    }
                } else {
                    PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                    pageInfo.setList(mwTangAssetses);

                    logger.info("ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);

                    return Reply.ok(pageInfo);
                }
            }

            mwTangAssetses = doSelectAssets(qParam, dataPermission, loginName, userId);

            //将资产数据进行排序处理
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            List<MwTangibleassetsTable> sortMwTangAssetses = mwTangAssetses.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getAssetsName()), pinyin4jUtil.getStringPinYin(o2.getAssetsName()))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sortMwTangAssetses)) {
                //根据分页信息分割数据
                Integer pageNumber = qParam.getPageNumber();
                Integer pageSize = qParam.getPageSize();
                int fromIndex = pageSize * (pageNumber - 1);
                int toIndex = pageSize * pageNumber;
                if (toIndex > sortMwTangAssetses.size()) {
                    toIndex = sortMwTangAssetses.size();
                }
                mwTangAssetses.clear();
                mwTangAssetses = sortMwTangAssetses.subList(fromIndex, toIndex);
            }
            log.info("selectList modelAssetEnable::" + modelAssetEnable);
            if (modelAssetEnable) {
                log.info("selectList aasetsStatus::" + sortMwTangAssetses.stream().filter(item -> item.getItemAssetsStatus() != null).collect(Collectors.toList()));
                PageInfo pageInfo = new PageInfo<>(sortMwTangAssetses);
                pageInfo.setList(mwTangAssetses);
                return Reply.ok(pageInfo);
            }
            //加资产健康状态
            if (mwTangAssetses != null && mwTangAssetses.size() > 0) {
                Map<Integer, List<String>> groupMap = mwTangAssetses.stream().filter(item -> item.getMonitorServerId() != null && item.getMonitorServerId() != 0)
                        .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
                Map<String, String> statusMap = new HashMap<>();
                Set<String> hostSets = new HashSet<>();
                for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
                    if (value.getKey() != null && value.getKey() > 0) {
                        MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.NEW_ASSETS_STATUS, value.getValue());
                        log.info("assetsStatus{}" + statusData);
                        if (statusData != null && !statusData.isFail()) {
                            JsonNode jsonNode = (JsonNode) statusData.getData();
                            if (jsonNode.size() > 0) {
                                for (JsonNode node : jsonNode) {
                                    Integer lastvalue = node.get("lastvalue").asInt();
                                    String hostId = node.get("hostid").asText();
                                    String name = node.get("name").asText();
                                    if ((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)) {
                                        String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                        statusMap.put(value.getKey() + ":" + hostId, status);
                                        hostSets.add(hostId);
                                    }
                                    if (hostSets.contains(hostId)) {
                                        continue;
                                    }
                                    String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                    statusMap.put(value.getKey() + ":" + hostId, status);
                                }
                            }
                        }
//                        statusMap.put(value.getKey() + ":" + values, "ABNORMAL");
                    }
                }
                log.info("assetsStatus{}two" + statusMap);
                String status = "";
                for (MwTangibleassetsTable asset : mwTangAssetses) {
                    if (null == asset.getMonitorFlag()) {
                        status = "UNKNOWN";
                    } else {
                        if (asset.getMonitorFlag()) {
                            String s = statusMap.get(asset.getMonitorServerId() + ":" + asset.getAssetsId());
                            if (s != null && StringUtils.isNotEmpty(s)) {
                                status = s;
                            } else {
                                status = "UNKNOWN";
                            }
                        } else {
                            status = "SHUTDOWN";
                        }
                    }
                    asset.setItemAssetsStatus(status);
                }
                logger.info("ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);
            }
            //获取资产自定义字段的值
            logger.info("有形资产查询自定义字段start" + new Date());
            assetsNewFieldService.getAssetsCustomFieldValue(mwTangAssetses);
            logger.info("有形资产查询自定义字段end" + new Date());
            PageInfo pageInfo = new PageInfo<>(sortMwTangAssetses);
            pageInfo.setList(mwTangAssetses);

            if (qParam.getNetFlowInterface() == 1) {
                for (MwTangibleassetsTable assets : mwTangAssetses) {
                    List<QueryAssetsInterfaceParam> interfaceList = mwAssetsInterfaceDao.getAllInterface(assets.getId(), null, false);
                    assets.setInterfaceList(interfaceList);
                }
            }

            logger.info("ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);

            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to selectList with mtaDTO={}, cause:{}", qParam, e);
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210102, ErrorConstant.TANGASSETS_MSG_210102);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    @Autowired
    private MWUserService mwUserService;

    @Override
    public List<MwTangibleassetsTable> doSelectAssets(Object objectParam, DataPermission dataPermission
            , String loginName, Integer userId) {
        List<MwTangibleassetsTable> mwTangAssetses = null;
        QueryTangAssetsParam qParam = new QueryTangAssetsParam();
        try {
            PropertyUtils.copyProperties(qParam, objectParam);
            log.info("modelAssetEnable:{}", modelAssetEnable);
            if (modelAssetEnable) {
                QueryModelAssetsParam queryModelAssetsParam = new QueryModelAssetsParam();
                PropertyUtils.copyProperties(queryModelAssetsParam, qParam);
                queryModelAssetsParam.setInstanceName(qParam.getAssetsName());
                queryModelAssetsParam.setAssetsName(qParam.getHostName());
                if (queryModelAssetsParam.getIsQueryAssetsState() == null) {
                    queryModelAssetsParam.setIsQueryAssetsState(true);
                }
                setModelIds(queryModelAssetsParam, qParam);
                queryModelAssetsParam.setUserId(userId);
                mwTangAssetses = mwModelViewCommonService.findModelAssets(MwTangibleassetsTable.class, queryModelAssetsParam);
            } else {
                switch (dataPermission) {
                    case PRIVATE:
                        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
                        List<String> idList = mwUserService.getAllTypeIdList(userInfo, DataType.ASSETS);
                        //                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                        Map priCriteria = PropertyUtils.describe(qParam);
                        priCriteria.put("isSystem", userInfo.isSystemUser());
                        priCriteria.put("listSet", Joiner.on(",").join(idList));
                        mwTangAssetses = mwTangibleAssetsDao.selectPriList(priCriteria);
                        break;
                    case PUBLIC:
                        String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                        List<Integer> orgIds = new ArrayList<>();
                        Boolean isAdmin = false;
                        if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                            isAdmin = true;
                        }
                        if (!isAdmin) {
                            // List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                            //orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);
                            orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                        }
                        qParam.setIsAdmin(isAdmin);
                        if (null != orgIds && orgIds.size() > 0) {
                            qParam.setOrgIds(orgIds);
                        }
                        //                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                        Map pubCriteria = PropertyUtils.describe(qParam);
                        mwTangAssetses = mwTangibleAssetsDao.selectPubList(pubCriteria);
                        break;
                }
            }
        } catch (Throwable e) {
            log.error("权限资产查询失败", e);
        }
        return mwTangAssetses;
    }

    private void setModelIds(QueryModelAssetsParam queryModelAssetsParam, QueryTangAssetsParam qParam) {
        if (CollectionUtils.isNotEmpty(qParam.getAssetsIds())) {
            List<String> assetsIds = qParam.getAssetsIds();
            List<Integer> intIds = assetsIds.stream().map(Integer::valueOf).collect(Collectors.toList());
            queryModelAssetsParam.setInstanceIds(intIds);
        }
    }

    @Override
    public Reply selectAllLabel(QueryTangAssetsParam qParam) {
        try {
            QueryLabelParam labelParam = new QueryLabelParam();
            labelParam.setAssetsTypeId(qParam.getAssetsTypeId());
            labelParam.setLabelName(qParam.getLabelName());
            // 获取本次查询的标签所有的标签值
            List<MwAllLabelDTO> allLabel = mwTangibleAssetsDao.selectAllLabel(labelParam);

            return Reply.ok(allLabel);

        } catch (Exception e) {
            log.error("fail to selectAllLabel with assetsTypeId={}, cause:{}", qParam, e);
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210102, ErrorConstant.TANGASSETS_MSG_210102);
        }
    }

    /**
     * 更新资产信息
     *
     * @param uParam
     * @return
     */
    @Override
    public Reply updateAssets(UpdateTangAssetsParam uParam) throws Throwable {
        Reply reply = null;
        uParam.setModifier(iLoginCacheInfo.getLoginName());
        uParam.setModificationDate(new Date());
        List<String> ids = uParam.getIds();
        if (ids != null && ids.size() > 0) {
            reply = updateAsstsbatch(uParam);
        } else {
            reply = updateAssetsOne(uParam);
        }
        if (assetsPush) {
            List<String> idList = new ArrayList<>();
            //资产数据变更，kafka推送消息
            if (CollectionUtils.isEmpty(uParam.getIds())) {
                idList.add(uParam.getId());
            } else {
                idList = uParam.getIds();
            }
            tangibleAssetsPushConvert(idList);
        }
        return reply;
    }

    public Reply updateAsstsbatch(UpdateTangAssetsParam uParam) {
        try {
            //配置管理增加许可管理
            if (uParam.isSettingFlagcheckbox() && uParam.getSettingFlag()) {
                //判断原配置管理数量
                int allSettingCount = mwTangibleAssetsDao.countSettingAssets(null);
                //本次配置的数量
                int nowSettingCount = mwTangibleAssetsDao.countSettingAssets(uParam.getIds());
                ResponseBase responseBase = licenseManagement.getLicenseManagemengt("prop_manage", allSettingCount, uParam.getIds().size() - nowSettingCount);
                if (responseBase.getRtnCode() != 200) {
                    return Reply.fail("更新失败,配置数量超过许可数量");
                }
            }
            if (uParam.isAssetsLabelcheckbox()) {//如果批量勾选上标签就修改
                //处理标签多选问题
                assetsLabelCheckBoxHandle(uParam.getAssetsLabel());
                //标签重复数据校验
                String labelRepeatData = getLabelRepeatData(uParam.getAssetsLabel(), uParam.getId(), DataType.ASSETS.getName());
                if (StringUtils.isNotBlank(labelRepeatData)) {
                    return Reply.fail(500, labelRepeatData);
                }
                //将资产原来的标签数据与现在的数据进行合并添加
                Map<String, List<MwAssetsLabelDTO>> assetsLabelMap = batchEditAssetsLabelHandle(uParam);
                //删除原标签参数
                mwLabelCommonServcie.deleteLabelBoards(uParam.getIds(), DataType.ASSETS.getName());
                //插入标签参数
                if (!assetsLabelMap.isEmpty()) {
                    for (Map.Entry<String, List<MwAssetsLabelDTO>> entry : assetsLabelMap.entrySet()) {
                        String typeId = entry.getKey();
                        List<MwAssetsLabelDTO> labelDTOS = entry.getValue();
                        mwLabelCommonServcie.insertLabelboardMapper(labelDTOS, typeId, DataType.ASSETS.getName());
                    }
                }
//                if (null != uParam.getAssetsLabel() && uParam.getAssetsLabel().size() > 0) {
//                    uParam.getIds().forEach(typeId -> {//给每一个资产添加标签
//                        mwLabelCommonServcie.insertLabelboardMapper(uParam.getAssetsLabel(), typeId, DataType.ASSETS.getName());
//                    });
//                }
            }
            //轮询引擎设置
            if (uParam.isPollingEnginecheckbox()) {
                if (uParam.getPollingEngineList() == null || uParam.getPollingEngineList().size() == 0) {
                    for (Map.Entry<Integer, List<String>> entry : uParam.getProxyIdList().entrySet()) {
                        Map<String, Object> updateParam = new HashMap<>();
                        updateParam.put("proxy_hostid", null);
                        MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(entry.getKey(), entry.getValue(), updateParam);
                        if (result.isFail()) {
                            log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                            throw new AssetsException("删除主机代理失败:" + result.getData());
                        }
                        mwTangibleAssetsDao.updateBatchPolling(null, entry.getKey(), entry.getValue());
                    }
                }
                for (Map.Entry<Integer, String> entry : uParam.getPollingEngineList().entrySet()) {
                    if (entry.getKey() != null && entry.getKey() != 0) {
                        //获取代理ip
                        String pollId = entry.getValue();
                        if (pollId != null && !"".equals(pollId)) {
                            MwEngineManageDTO proxyEntity = mwEngineManageTableDao.selectById(pollId);
                            Map<String, Object> updateParam = new HashMap<>();
                            updateParam.put("proxy_hostid", proxyEntity.getProxyId());
                            MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(entry.getKey(), uParam.getProxyIdList().get(entry.getKey()), updateParam);
                            if (result.isFail()) {
                                log.error("[]ERROR_LOG[][]修改主机代理失败[][]msg:[]{}", result.getData());
                                throw new AssetsException("修改主机代理失败:" + result.getData());
                            }
                            mwTangibleAssetsDao.updateBatchPolling(pollId, entry.getKey(), uParam.getProxyIdList().get(entry.getKey()));
                        } else {
                            Map<String, Object> updateParam = new HashMap<>();
                            updateParam.put("proxy_hostid", null);
                            MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(entry.getKey(), uParam.getProxyIdList().get(entry.getKey()), updateParam);
                            if (result.isFail()) {
                                log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                                throw new AssetsException("删除主机代理失败:" + result.getData());
                            }
                            mwTangibleAssetsDao.updateBatchPolling(null, entry.getKey(), uParam.getProxyIdList().get(entry.getKey()));
                        }
                    }

                }
            }


            //用户，用户组，机构设置
            updateMapperAndPerms(uParam);
            if (uParam.isMonitorFlagcheckbox()) {//如果批量勾选上监控状态就修改
                TangibleAssetMonitorState tas = TangibleAssetMonitorState.valueOf(uParam.getMonitorFlag().toString().toUpperCase());
                //修改zabbix中的主机状态
                for (Map.Entry<Integer, List<String>> entry : uParam.getProxyIdList().entrySet()) {
                    if (entry.getKey() != null && entry.getKey() != 0) {
                        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.hostUpdate(entry.getKey(), entry.getValue(), tas.getZabbixStatus());
                        if (mwZabbixAPIResult.isFail()) {
                            throw new AssetsException(ErrorConstant.TANGASSETS_MSG_210118);
                        }
                    }
                }
//                uParam.set(tas.isActive());
            }
            //编辑数据库
            mwTangibleAssetsDao.updateBatch(uParam);
        } catch (Exception e) {
            return Reply.fail("更新失败");
        }
        return Reply.ok("更新成功");
    }

    public Reply updateAssetsOne(AddUpdateTangAssetsParam uParam) throws Throwable {
        try {

            //配置管理增加许可管理
            if (uParam.getSettingFlag()) {
                //判断原配置管理数量
                int allSettingCount = mwTangibleAssetsDao.countSettingAssets(null);
                ResponseBase responseBase = licenseManagement.getLicenseManagemengt("prop_manage", allSettingCount, 1);
                if (responseBase.getRtnCode() != 200) {
                    return Reply.fail("更新失败,配置数量超过许可数量");
                }
            }

            //处理标签多选问题
            assetsLabelCheckBoxHandle(uParam.getAssetsLabel());
            if (uParam.getFlag() == 1) {
                List<Reply> faillist;
                faillist = publishCheckEvent(UpdateTangibleassetsEvent.builder().updateTangAssetsParam(uParam).build());
                if (faillist.size() > 0) {
                    throw new ServiceException(faillist);
                }
                publishPostEvent(UpdateTangibleassetsEvent.builder().updateTangAssetsParam(uParam).build());

            }
            uParam.setModifier(iLoginCacheInfo.getLoginName());

            MwTangibleassetsDTO beforeAssets = mwTangibleAssetsDao.selectById(uParam.getId());
            String beforePollId = beforeAssets.getPollingEngine();
            String beforeIp = beforeAssets.getInBandIp();

            //是否更新了资产的ip
            if (!beforeIp.equals(uParam.getInBandIp())) {
                MWZabbixAPIResult result = mwtpServerAPI.hostInterfaceGet(uParam.getMonitorServerId(), uParam.getAssetsId());
                if (!result.isFail()) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode.size() > 0) {
                        String interfaceid = jsonNode.get(0).get("interfaceid").asText();
                        Map<String, Object> map = new HashMap<>();
                        map.put("ip", uParam.getInBandIp());
                        MWZabbixAPIResult updateResult = mwtpServerAPI.hostInterfaceUpdate(uParam.getMonitorServerId(), interfaceid, map);
                        if (updateResult.isFail()) {
                            log.error("[]ERROR_LOG[][]修改主机interface信息失败[][]msg:[]{}", updateResult.getData());
                            throw new AssetsException("修改主机interface信息失败:" + updateResult.getData());
                        }
                    }
                }
            }

            //获取代理ip
            String pollId = uParam.getPollingEngine();
            if (pollId != null && !"".equals(pollId)) {
                MwEngineManageDTO proxyEntity = mwEngineManageTableDao.selectById(pollId);
                MWZabbixAPIResult result = mwtpServerAPI.hostProxyUpdate(uParam.getMonitorServerId(), uParam.getAssetsId(), proxyEntity.getProxyId());
                if (result.isFail()) {
                    log.error("[]ERROR_LOG[][]修改主机代理失败[][]msg:[]{}", result.getData());
                    throw new AssetsException("修改主机代理失败:" + result.getData());
                }
//                else {
//                    if (beforePollId == null || beforePollId.equals("")) {
//                        mwEngineCommonsService.updateMonitorNums(true, pollId, uParam.getAssetsId());
//                    } else if (beforePollId.equals(pollId)) {
//                    } else if (!beforePollId.equals(pollId)) {
//                        mwEngineCommonsService.updateMonitorNums(false, beforePollId, uParam.getAssetsId());
//                        mwEngineCommonsService.updateMonitorNums(true, pollId, uParam.getAssetsId());
//                    }
//                }
            } else {
                if (!Strings.isNullOrEmpty(beforePollId) && uParam.getMonitorServerId() != null && uParam.getMonitorServerId() != 0) {
                    MWZabbixAPIResult result = mwtpServerAPI.hostProxyUpdate(uParam.getMonitorServerId(), uParam.getAssetsId(), null);
                    if (result.isFail()) {
                        log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                        throw new AssetsException("删除主机代理失败:" + result.getData());
                    }
//                    else {
//                        if (beforePollId != null && !beforePollId.equals("")) {
//                            mwEngineCommonsService.updateMonitorNums(false, beforePollId, uParam.getAssetsId());
//                        }
//                    }
                }
            }

            //监控状态就修改
//            TangibleAssetMonitorState tas = TangibleAssetMonitorState.valueOf(uParam.getMonitorFlag().toString().toUpperCase());
//            //修改zabbix中的主机状态
//            if (uParam.getMonitorServerId() != null && uParam.getMonitorServerId() != 0) {
//                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.hostUpdate(uParam.getMonitorServerId(), Arrays.asList(uParam.getAssetsId()), tas.getZabbixStatus());
//                if (mwZabbixAPIResult.isFail()) {
//                    throw new AssetsException(ErrorConstant.TANGASSETS_MSG_210118);
//                }
//            }

            //修改zabbix中主机可见名称
            boolean isUpdateNmae = updateZabbixSoName(uParam.getMonitorServerId(), uParam.getAssetsId(), uParam.getAssetsName());
            if (!isUpdateNmae) {
                log.info("修改资产可见名称失败,主机ID：" + uParam.getAssetsId() + ",修改名称:" + uParam.getAssetsName());
            }
            //更新资产类型 暂时不给更新资产类型
//            Set<String> gids = new HashSet<>();
//            Map criteria = new HashMap();
//            criteria.put("assetsTypeId", uParam.getAssetsTypeId());
//            criteria.put("subAssetsTypeId", uParam.getAssetsTypeSubId());
//            criteria.put("serverId", uParam.getMonitorServerId());
//            List<MwAssetsTemplateDTO> assetsTemplateList = mwAseetstemplateTableDao.selectList(criteria);
//            for (MwAssetsTemplateDTO mwAssetsTemplateDTO : assetsTemplateList) {
//                gids.add(mwAssetsTemplateDTO.getGroupId());
//            }
//            ArrayList<String> groupIdList = new ArrayList<>(gids);
//            MWZabbixAPIResult resu = mwtpServerAPI.hostUpdateGroup(uParam.getMonitorServerId(),uParam.getAssetsId(),groupIdList);
//            if (resu.isFail()) {
//                log.error("[]ERROR_LOG[][]修改主机所属组失败[][]msg:[]{}", resu.getData());
//                throw new AssetsException("修改主机所属组:" + resu.getData());
//            }
            //删除标签参数
            mwLabelCommonServcie.deleteLabelBoard(uParam.getId(), DataType.ASSETS.getName());
            //插入标签参数
            if (null != uParam.getAssetsLabel() && uParam.getAssetsLabel().size() > 0) {
                mwLabelCommonServcie.insertLabelboardMapper(uParam.getAssetsLabel(), uParam.getId(), DataType.ASSETS.getName());
            }

            //用户，用户组，机构设置
            deleteMapperAndPerm(uParam);
            addMapperAndPerm(uParam);


            mwTangibleAssetsDao.update(uParam);
            //配置管理增加许可管理
            if (!uParam.getSettingFlag()) {
                //判断原配置管理数量
                try {
                    int allSettingCount = mwTangibleAssetsDao.countSettingAssets(null);
                    licenseManagement.getLicenseManagemengt("prop_manage", allSettingCount, 0);
                } catch (Exception e) {
                    log.error("更新许可数量失败", e);
                }
            }

            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateAssets with mtaDTO={}, cause:{}", uParam, e);
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210103, ErrorConstant.TANGASSETS_MSG_210103);
        }
    }

    /**
     * 删除负责人，用户组，机构 权限关系
     *
     * @param auParam
     */
    private void deleteMapperAndPerm(AddUpdateTangAssetsParam auParam) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(auParam.getId()))
                .type(DataType.ASSETS.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 添加負責人
     *
     * @param uParam
     */
    private void addMapperAndPerm(AddUpdateTangAssetsParam uParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(uParam.getGroupIds())  //用户组
                .userIds(uParam.getPrincipal()) //责任人
                .orgIds(uParam.getOrgIds()) //机构
                .typeId(uParam.getId())    //资产数据主键
                .type(DataType.ASSETS.getName())  //ASSETS
                .desc(DataType.ASSETS.getDesc()).build(); //资产
        mwCommonService.addMapperAndPerm(insertDto);
    }


    /**
     * 修改权限
     *
     * @param uParam
     */
    private void updateMapperAndPerms(UpdateTangAssetsParam uParam) {
        UpdateDTO updateDTO = UpdateDTO.builder()
                .isGroup(uParam.isGroupIdscheckbox())
                .isUser(uParam.isPrincipalcheckbox())
                .isOrg(uParam.isOrgIdscheckbox())
                .groupIds(uParam.getGroupIds())  //用户组
                .userIds(uParam.getPrincipal()) //责任人
                .orgIds(uParam.getOrgIds()) //机构
                .typeIds(uParam.getIds())    //批量资产数据主键
                .type(DataType.ASSETS.getName())  //ASSETS
                .desc(DataType.ASSETS.getDesc()).build(); //资产
        mwCommonService.editorMapperAndPerms(updateDTO);
    }

//    /**
//     * 新增资产信息
//     *
//     * @param aParam
//     * @return
//     */
//    @Override
//    public Reply insertAssets(AddUpdateTangAssetsParam aParam) throws Throwable {
//        return doInsertAssets(aParam, false);//参数1：增加有形资产的参数  参数2：该资产为非批量增加
//    }

    /**
     * 新增资产信息
     *
     * @param aParam
     * @return
     */
    @Override
    public Reply insertAssets(AddUpdateTangAssetsParam aParam, boolean isbatch) throws Throwable {

        if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
            //设置第三方监控服务器中主机名称
            aParam.setTPServerHostName(UuidUtil.getUid());
        }
        AddUpdateOutbandAssetsParam outParam = null;
        if (aParam.getAssetsTypeId() == 69) {//说明是带外资产，添加到带外资产表
            //访问zabbix获取主机id, 生成资产id
            if (aParam.getAssetsId() == null || StringUtils.isEmpty(aParam.getAssetsId())) {
                aParam.setAssetsId(createAndGetZabbixHostId(aParam));
            }
            outParam = CopyUtils.copy(AddUpdateOutbandAssetsParam.class, aParam);
            outParam.setIpAddress(aParam.getInBandIp());
        }
        try {
            return aParam.getAssetsTypeId() == 69 ? mwOutbandAssetsService.insertAssets(outParam) : doInsertAssets(aParam, isbatch);
        } catch (Throwable t) {
            log.error("insertAssets", t);
            //报错删除已添加的zabbixHost
            if (aParam.getMonitorServerId() != null && aParam.getMonitorServerId() != 0) {
                ArrayList<String> hostNames = new ArrayList<>();
                hostNames.add(aParam.getTPServerHostName());
                MWZabbixAPIResult result = mwtpServerAPI.hostListGetByHostName(aParam.getMonitorServerId(), hostNames);
                if (!result.isFail()) {
                    List<String> hostIds = new ArrayList<>();
                    JsonNode data = (JsonNode) result.getData();
                    data.forEach(hostId -> {
                        hostIds.add(hostId.get("hostid").asText());
                    });
                    mwtpServerAPI.hostDelete(aParam.getMonitorServerId(), hostIds);
                }
            }
            return Reply.fail(t.getMessage());
        }
    }

    @Override
    public Reply getTemplateList(AddUpdateTangAssetsParam aParam) throws Throwable {
        List<MwAssetsTemplateDTO> data = mwAseetstemplateTableDao.getTemplateByServerIdAndAssetsType(aParam);
        return Reply.ok(data);
    }

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Override
    public Reply doInsertAssets(AddUpdateTangAssetsParam aParam, boolean isbatch) throws Throwable {

        //说明创建不关联zabbix的资产,启用状态设置成未启用
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0) {
            aParam.setEnable(TangibleAssetState.DISACTIVE.name());
        } else {//说明是关联zabbix资产,启用状态设置成启用
            aParam.setEnable(TangibleAssetState.ACTIVE.name());
        }
        log.info("doInsertAssets params:" + aParam.toString() + ";isbatch" + isbatch);
        List<Reply> faillist;

        //为什么当资产为服务器的时候设置的配置状态要改成false??
//        Integer monitorModetor = aParam.getMonitorMode();
//        if (monitorModetor.equals(1)) {
//            aParam.setSettingFlag(false);
//        }
        if (aParam.getMonitorMode() != null) {
            RuleType ruleType = RuleType.getInfoByMonitorMode(aParam.getMonitorMode());
            //当通过网络设备方式添加资产时,此时并不会匹配模版,则把模版id置空
            if (ruleType == RuleType.NetWorkDevice) {
                aParam.setTemplateId("");
            }
        }

        //设置创建时间和创建人
        if (StringUtils.isBlank(aParam.getCreator())) {
            aParam.setCreator(iLoginCacheInfo.getLoginName());
        }
        if (StringUtils.isBlank(aParam.getModifier())) {
            aParam.setModifier(iLoginCacheInfo.getLoginName());
        }
        //通过工具类创建一个随机不重复的主键
        aParam.setId(UuidUtil.getUid());

        //判断是否为批量增加资产
        if (!isbatch) {
            //数据重复校验 即将添加的数据的 资产id(assets_id) 和带内ip(in_band_ip) 是否重复，二者有一即重复。
            faillist = publishCheckEvent(AddTangibleassetsEvent.builder().addTangAssetsParam(aParam).build());
            if (faillist.size() > 0) {
                throw new Throwable(faillist.get(0).getMsg());
            }

            //访问zabbix获取主机id, 生成资产id
            String zabbixHostId = createAndGetZabbixHostId(aParam);
            log.info("zabbixHostId:{}", zabbixHostId);
            //设置资产id
            aParam.setAssetsId(zabbixHostId);
        }
        //判断是否成功创建资产
        if (aParam.getMonitorServerId() != null && aParam.getMonitorServerId() > 0 && Strings.isNullOrEmpty(aParam.getAssetsId())) {
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210104, ErrorConstant.TANGASSETS_MSG_210104 + "fail to create zabbixHostid");
        }

        //设置伪删除和创建修改时间
        aParam.setDeleteFlag(false);
        Date date = new Date();
        aParam.setCreateDate(date);
        aParam.setModificationDate(date);

        //插入标签参数
        if (null != aParam.getAssetsLabel() && aParam.getAssetsLabel().size() > 0) {
            mwLabelCommonServcie.insertLabelboardMapper(aParam.getAssetsLabel(), aParam.getId(), DataType.ASSETS.getName());
        }

        String checkNowMsg = "";
        //是否启动立即执行
        if (aParam.isCheckNowFlag() && !Strings.isNullOrEmpty(aParam.getAssetsId())) {
            Boolean aBoolean = mwAssetsManager.checkNowItems(aParam.getMonitorServerId(), aParam.getAssetsId());
            if (!aBoolean) {
                checkNowMsg = "立即执行的操作未成功";
            }
        }

        //保存数据
        mwTangibleAssetsDao.insert(aParam);
        mwTangibleAssetsDao.insertDeviceInfo(aParam);
        monitorServerSet.add(aParam.getMonitorServerId());
        //？ PostAssetsProcesser.processAddPostTangibleAssets 设置了一些关联权限
        publishPostEvent(AddTangibleassetsEvent.builder().addTangAssetsParam(aParam).build());
        List<String> ids = new ArrayList<>(Arrays.asList(aParam.getId()));
        if (assetsPush && CollectionUtils.isNotEmpty(ids)) {
            //资产数据变更，kafka推送消息
            tangibleAssetsPushConvert(ids);
        }
        return Reply.ok("新增成功" + checkNowMsg);
    }

    /**
     * 删除资产信息
     *
     * @param ids
     * @return
     */
    @Transactional
    @Override
    public Reply deleteAssets(List<DeleteTangAssetsID> ids) {

        if (null == ids || ids.size() == 0) {
            return Reply.ok("无删除数据");
        }
        //校验删除知产是否被线路引用，如果被线路引用，则必须先删除线路数据
        List<String> linkNames = deleteAssetsCheckLinkRelation(ids);
        if (CollectionUtils.isNotEmpty(linkNames)) {
            StringBuilder builder = new StringBuilder();
            for (String linkName : linkNames) {
                builder.append(linkName + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            return Reply.fail("该资产与线路[" + builder.toString() + "]有关联，请先删除线路数据再进行资产删除");
        }
        deleteAssetsMonitorMapper(ids);

        List<String> tids = new ArrayList<String>();
        List<String> zabbixIds = new ArrayList<String>();
        List<String> VMzabbixIds = new ArrayList<String>();
        Collections.sort(ids);

        //根据服务器id分组
        int monitorServerId = -1;
        List<MonitorServer> monitorServers = new ArrayList<MonitorServer>();
        MonitorServer ms = null;
        for (DeleteTangAssetsID deleteTangAssetsID : ids) {
            //当监控方式为虚拟化时，创建的虚拟化资产自动发现的那些虚拟机，主机太多删除不掉时需要做的处理
            if (deleteTangAssetsID.getMonitorMode() != null && deleteTangAssetsID.getMonitorMode() == 7) {
                //删除虚拟化对应缓存
                redisTemplate.delete(redisTemplate.keys("virtualization::" + "*"));
                //主机对应的自动发现的规则名
                MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(deleteTangAssetsID.getMonitorServerId(), deleteTangAssetsID.getAssetsId());
                JsonNode resultData = (JsonNode) dRuleByHostId.getData();
                if (resultData.size() > 0) {
                    for (JsonNode resultDatum : resultData) {
                        String name = resultDatum.get("name").asText();
//                                    根据规则名获取主机组信息
                        MWZabbixAPIResult groupHostByName = mwtpServerAPI.getGroupHostByName(deleteTangAssetsID.getMonitorServerId(), name);
                        JsonNode groupHost = (JsonNode) groupHostByName.getData();
                        if (groupHost.size() > 0) {
                            groupHost.forEach(group -> {
                                List<GroupHosts> groupHostDTOS = JSONArray.parseArray(group.get("hosts").toString(), GroupHosts.class);
                                if (groupHostDTOS != null && groupHostDTOS.size() > 0) {
                                    List<String> list = groupHostDTOS.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                                    zabbixIds.addAll(list);
                                    VMzabbixIds.addAll(list);
                                }
                            });
                        }
                    }
                }
            }
            tids.add(deleteTangAssetsID.getId());
            zabbixIds.add(deleteTangAssetsID.getAssetsId());
            if (monitorServerId != deleteTangAssetsID.getMonitorServerId()) {
                ms = new MonitorServer();
                ms.setMonitorServerId(deleteTangAssetsID.getMonitorServerId());
                monitorServerId = deleteTangAssetsID.getMonitorServerId();
                monitorServers.add(ms);
            }
            ms.getZabbixIds().add(deleteTangAssetsID.getAssetsId());
            if (VMzabbixIds.size() > 0) {
                ms.getZabbixIds().addAll(VMzabbixIds);
            }
        }

        mwTangibleAssetsDao.delete(tids);
        mwTangibleAssetsDao.deleteDeviceInfo(tids);
        updateMonitorServerSet();
        if (assetsPush && CollectionUtils.isNotEmpty(tids)) {
            //资产数据变更，kafka推送消息
            tangibleAssetsPushConvert(tids);
        }

        //删除标签参数

        //删除关联信息
        tids.forEach(
                id -> {
                    mwTangibleAssetsDao.deleteAssetsAgentByAssetsId(id);
                    mwTangibleAssetsDao.deleteAssetsPortByAssetsId(id);
                    //mwTangibleAssetsDao.deleteAssetsLabelByAssetsId(id);
                    mwTangibleAssetsDao.deleteAssetsSnmpv12ByAssetsId(id);
                    mwTangibleAssetsDao.deleteAssetsSnmpv3ByAssetsId(id);
                    mwLabelCommonServcie.deleteLabelBoard(id, DataType.ASSETS.getName());

                    //删除负责人
                    DeleteDto deleteDto = DeleteDto.builder().typeId(id).type(DataType.ASSETS.getName()).build();
                    mwCommonService.deleteMapperAndPerm(deleteDto);
                });
        //删除告警映射关系
        mwTangibleAssetsDao.deleteAssetsActionMapper(zabbixIds);

        //删除redis缓存
        deleteRedisActive(tids);

//        //更新引擎监控数量
//        for (DeleteTangAssetsID id : ids) {
//            if (id.getAssetsId().length() != "1603259561318843dd528c36e42d0b2c".length()) {
//                if (id.getPollingEngine() != null && !id.getPollingEngine().equals("")) {
//                    mwEngineCommonsService.updateMonitorNums(false, id.getPollingEngine(), id.getAssetsId());
//                }
//            }
//        }

        //删除zabbix 配置
        boolean isFail = false;
        List<Integer> failServers = new ArrayList<Integer>();
        for (MonitorServer monitorServer : monitorServers) {
            List<String> delZabbixIds = monitorServer.getZabbixIds();
            //去除不存在的id
            List<String> lists = new ArrayList<>();
            delZabbixIds.forEach(id -> {
                if (id.length() != "1603259561318843dd528c36e42d0b2c".length()) {
                    lists.add(id);
                }
            });
            if (monitorServer.getMonitorServerId() != null && monitorServer.getMonitorServerId() != 0) {
                if (lists.size() > 10) {//当zabbix一次删除过多的时候，删除不掉资产
                    for (int i = 0; i < lists.size(); i = i + 10) {
                        List<String> list = lists.subList(i, (i + 10) > lists.size() ? lists.size() : (i + 10));
                        MWZabbixAPIResult result = mwtpServerAPI.hostDelete(monitorServer.getMonitorServerId(), list);
                        if (result.isFail()) {
//                            isFail = true;
                            failServers.add(monitorServer.getMonitorServerId());
                            log.error("[]ERROR_LOG[][]删除主机失败[][]msg:[]{}", result.getData());
                        }
                    }
                } else {
                    MWZabbixAPIResult result = mwtpServerAPI.hostDelete(monitorServer.getMonitorServerId(), lists);
                    if (result.isFail()) {
//                        isFail = true;
                        failServers.add(monitorServer.getMonitorServerId());
                        log.error("[]ERROR_LOG[][]删除主机失败[][]msg:[]{}", result.getData());
                    }
                }
            }
        }

        if (isFail) {
            return Reply.fail("主机删除失败:[" + failServers + "]");
        }

        //删除有型资产关联数据
        BatchDeleteAssetsEvent batchDeleteAssetsEvent = new BatchDeleteAssetsEvent();
        batchDeleteAssetsEvent.setDeleteTangAssetsIDList(ids);
        try {
            publishFinishEvent(batchDeleteAssetsEvent);
        } catch (Throwable throwable) {
            log.error("deleteAssets publishFinishEvent", throwable);
        }

        //配置管理增加许可管理
        try {
            //判断原配置管理数量
            int allSettingCount = mwTangibleAssetsDao.countSettingAssets(null);
            licenseManagement.getLicenseManagemengt("prop_manage", allSettingCount, 0);
        } catch (Exception e) {
            log.error("更新许可数量失败", e);
        }

        return Reply.ok("删除成功");
    }

    public void deleteRedisActive(List<String> ids) {
        Set<String> deleteIds = new HashSet<>();
        ids.forEach(id -> deleteIds.add(id));
        if (deleteIds.size() > 0) {
            redisTemplate.delete(deleteIds);
        }
    }

    @Override
    public Reply updateState(UpdateAssetsStateParam uParam) {
        TangibleAssetStateType tast = TangibleAssetStateType.valueOf(uParam.getStateType());
        switch (tast) {
            case AssetState:
                TangibleAssetState tas = TangibleAssetState.valueOf(uParam.getEnable());
                mwtpServerAPI.hostUpdate(uParam.getMonitorServerId(), uParam.getHostIds(), tas.getTangibleAssetMonitorState().getZabbixStatus());
                mwTangibleAssetsDao.updateAssetsState(uParam);
                break;
            case MonitorState:
                TangibleAssetMonitorState tams = TangibleAssetMonitorState.valueOf(uParam.getEnable().toUpperCase());
                mwtpServerAPI.hostUpdate(uParam.getMonitorServerId(), uParam.getHostIds(), tams.getZabbixStatus());
                UpdateMonStateParam umsp = new UpdateMonStateParam();
                umsp.setIdList(uParam.getIdList());
                umsp.setHostIds(uParam.getHostIds());
                umsp.setMonitorFlag(tams.isActive());
                mwTangibleAssetsDao.updateAssetsMonState(umsp);
                /**
                 * 修改监控状态的时候要判断是否时zabbixaggent的监控方式
                 * 如果是，还需判断其是否有web监测，如果有，同时停用或者启用web监测
                 */
                if (uParam.getMonitorMode() == 1) {
                    List<HttpParam> httpParams = mwWebmonitorTableDao.selectHttpIds(uParam.getIdList());
                    UpdateWebMonitorStateParam updateWeb = new UpdateWebMonitorStateParam();
                    if (httpParams.size() > 0) {
                        httpParams.forEach(dto -> {
                            updateWeb.setId(dto.getId());
                            updateWeb.setHttpTestId(dto.getHttpId());
                            updateWeb.setMonitorServerId(dto.getMonitorServerId());
                            updateWeb.setEnable(tams.isActive() == true ? "ACTIVE" : "DISACTIVE");
                            mwWebMonitorService.updateState(updateWeb);
                        });
                    }
                }
                break;
            case SettingState:
                TangibleAssetSetState tass = TangibleAssetSetState.valueOf(uParam.getEnable().toUpperCase());
                UpdateSetStateParam ussp = new UpdateSetStateParam();
                ussp.setIdList(uParam.getIdList());
                ussp.setHostIds(uParam.getHostIds());
                ussp.setSettingFlag(tass.isEnable());
                mwTangibleAssetsDao.updateAssetsSetState(ussp);
                break;
            default:
        }
        return Reply.ok("更新成功");
    }

    @Override
    public String createAndGetOutAssetsZabbixHostId(AddUpdateTangAssetsParam aParam) {
        if (aParam.getAssetsId() != null && StringUtils.isNotEmpty(aParam.getAssetsId())) {
            return aParam.getAssetsId();
        }
        //说明创建不关联zabbix的资产
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0) {
            return null;
        }
        //获取分组Id
        ArrayList<String> groupIdList = new ArrayList<>();
        groupIdList.add((aParam.getGroupId() != null && StringUtils.isNotEmpty(aParam.getGroupId())) ? aParam
                .getGroupId() : aParam.getHostGroupId());
        ArrayList<Map<String, Object>> interList = new ArrayList<>();
        Map interMap = new HashMap();
        interMap.put("ip", aParam.getInBandIp());
        interMap.put("main", 1);
        String port = "623";
        int interfaceType = 1;

        Map detailsMap = new HashMap();
        List<Map> macroDTOS = new ArrayList<>();
        Map<String, Object> otherParam = new HashMap<>();
        if (aParam.getMonitorMode() == RuleType.IPMI.getMonitorMode()) {
            if (null != aParam.getMwIPMIAssetsDTO() && !"".equals(aParam.getMwIPMIAssetsDTO().getPort())) {
                port = aParam.getMwIPMIAssetsDTO().getPort().toString();
            }
            interfaceType = RuleType.IPMI.geInterfaceType();
            //用户名称
            otherParam.put("ipmi_username", aParam.getMwIPMIAssetsDTO().getAccount());
            //密码
            otherParam.put("ipmi_password", aParam.getMwIPMIAssetsDTO().getPassword());
        } else {
            logger.warn("no match MonitorMode" + aParam.getMonitorMode());
        }

        interMap.put("dns", "");
        interMap.put("port", port);
        interMap.put("type", interfaceType);//type 接口类型  1 - agent 2 - SNMP;3 - IPMI;4 - JMX.
        interMap.put("useip", 1);//使用的链接方式 0 DNS名称连接 1 IP地址进行连接
        if (detailsMap.size() > 0) {
            interMap.put("details", detailsMap);
        }
        interList.add(interMap);

        ArrayList<String> templList = new ArrayList<>();
        templList.add(aParam.getTemplateId());

        Integer status = aParam.getMonitorFlag()
                ? TangibleAssetMonitorState.TRUE.getZabbixStatus() : TangibleAssetMonitorState.FALSE.getZabbixStatus();
        //设置状态
        otherParam.put("status", status);
        //关联引擎
        if (!Strings.isNullOrEmpty(aParam.getPollingEngine()) && !"localhost".equals(aParam.getPollingEngine())) {
            //获取代理ip
            MwEngineManageDTO proxyEntity = mwEngineManageTableDao.selectById(aParam.getPollingEngine());
            otherParam.put("proxy_hostid", proxyEntity.getProxyId());
        }
        Pattern p = Pattern.compile("[0-9a-zA-Z_\\. \\-]+");
        String name = aParam.getAssetsName();
        if (Strings.isNullOrEmpty(name) || !p.matcher(name).matches()) {
            name = aParam.getTPServerHostName();
        }
        String visibleName = aParam.getAssetsName();
        MWZabbixAPIResult result = mwtpServerAPI.hostCreate(aParam.getMonitorServerId(), name, visibleName
                , groupIdList, interList, templList, macroDTOS, otherParam);
        if (result.isFail()) {
            logger.error("[]ERROR_LOG[][]添加主机失败[][]msg:[]{}", result.getData());
            throw new AssetsException("添加主机失败:" + result.getData());
        }
        String hostids = String.valueOf(result.getData());
        JSONObject js = JSONObject.parseObject(hostids);
        JSONArray strs = (JSONArray) js.get("hostids");
        String hostid = strs.getString(0);
        aParam.setTPServerHostName(name);
        aParam.setHostName(visibleName);
        //修改zabbix中主机可见名称
//        boolean isUpdateNmae = updateZabbixSoName(aParam.getMonitorServerId(), hostid, aParam.getAssetsName());
//        if (!isUpdateNmae) {
//            log.info("修改资产可见名称失败,主机ID：" + hostid + ",修改名称:" + aParam.getAssetsName());
//        }

        return hostid;
    }


    public String createAndGetZabbixHostId(AddUpdateTangAssetsParam aParam) throws Exception {
        if (aParam.getAssetsId() != null && StringUtils.isNotEmpty(aParam.getAssetsId())) {
            return aParam.getAssetsId();
        }
        //说明创建不关联zabbix的资产
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0) {
            return "";
        }
        if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
            //设置第三方监控服务器中主机名称
            aParam.setTPServerHostName(UuidUtil.getUid());
        }

        RuleType ruleType = RuleType.getInfoByMonitorMode(aParam.getMonitorMode());
        switch (ruleType) {
            case Logger:
                return UuidUtil.getUid();
            default:
                log.warn("no match MonitorMode" + aParam.getMonitorMode());
                break;
        }
        HostCreateParam hostCreateParam = zabbixUtils.transform(aParam, null);
        log.info("hostCreateParam::"+hostCreateParam);
        Boolean updateHostName = true;
        MWZabbixAPIResult result = createHostByZabbix(hostCreateParam);
        String hostid = "";

        if (result.isFail()) {
            log.error("ERROR_LOG添加主机失败msg:{}  params:{}", result.getData(),
                    hostCreateParam.toString());
            if (result.getData() != null && result.getData().toString().indexOf("Incorrect characters used for host name") != -1) {
                updateHostName = false;
                //主机名存在非法字符时，自动替换成UUID
                hostCreateParam.setHost(aParam.getTPServerHostName());
                result = createHostByZabbix(hostCreateParam);
                if (result.isFail()) {
                    throw new HostCreatFailException(result.getMessage() + ":" + result.getData());
                }
            }

            log.error("新增资产失败11:" + result.getData());
            //主机名和可见名重复时，全部追加uuid
            if (strValueConvert(result.getData()).indexOf("Host with the same") != -1 && strValueConvert(result.getData()).indexOf("already exists") != -1) {
                hostCreateParam.setHost(hostCreateParam.getHost() + UuidUtil.get16Uid());
                hostCreateParam.setVisibleName(hostCreateParam.getHost());
                result = createHostByZabbix(hostCreateParam);
                log.error("新增资产失败22:" + result.getData());
                if (result.isFail()) {
                    throw new HostCreatFailException("新增资产失败:" + result.getMessage() + ":" + result.getData());
                }
            }
        }
        if (result != null && result.getCode() == successCode) {
            String hostids = String.valueOf(result.getData());
            JSONObject js = JSONObject.parseObject(hostids);
            JSONArray strs = (JSONArray) js.get("hostids");
            aParam.setTPServerHostName(hostCreateParam.getHost());
            aParam.setHostName(hostCreateParam.getVisibleName());
            hostid = strs.getString(0);
        }
        return hostid;
    }

    private MWZabbixAPIResult createHostByZabbix(HostCreateParam hostCreateParam) {
        MWZabbixAPIResult result = null;
        if (StringUtils.isNotEmpty(hostCreateParam.getProxyID())) {
            result = mwtpServerAPI.hostCreate(hostCreateParam.getServerId(), hostCreateParam.getHost(), hostCreateParam.getVisibleName()
                    , hostCreateParam.getGroups()
                    , hostCreateParam.getInterfaces()
                    , hostCreateParam.getTemplates()
                    , hostCreateParam.getMacros()
                    , hostCreateParam.getStatus()
                    , hostCreateParam.getProxyID());
        } else {
            result = mwtpServerAPI.hostCreate(hostCreateParam.getServerId(), hostCreateParam.getHost(), hostCreateParam.getVisibleName()
                    , hostCreateParam.getGroups()
                    , hostCreateParam.getInterfaces()
                    , hostCreateParam.getTemplates()
                    , hostCreateParam.getMacros()
                    , hostCreateParam.getStatus());
            log.info("result:" + result);
        }
        return result;
    }

    @Override
    public void batchCreateAndGetZabbixHostId(List<AddUpdateTangAssetsParam> aParams) throws Exception {

        List<AddUpdateTangAssetsParam> zabbixParams = new ArrayList<>();

        Map<Integer, AddUpdateTangAssetsParam> indexMap = new HashMap<>();
        for (int i = 0; i < aParams.size(); i++) {
            AddUpdateTangAssetsParam param = aParams.get(i);
            indexMap.put(i, param);
            param.setBatchIndex(i);
            RuleType ruleType = RuleType.getInfoByMonitorMode(param.getMonitorMode());
            switch (ruleType) {
                case Logger:
                    param.setAssetsId(UuidUtil.getUid());
                    continue;
                default:
                    zabbixParams.add(param);
            }
        }
        log.info("zabbixParams param:" + zabbixParams);
        List<HostCreateParam> hostCreateParams = zabbixUtils.transform(zabbixParams);
        log.info("hostCreateParams param:" + hostCreateParams);
        Map<Integer, List<HostCreateParam>> groupMap = new HashMap<>();
        for (HostCreateParam hostCreateParam : hostCreateParams) {
            List<HostCreateParam> list = groupMap.get(hostCreateParam.getServerId());
            if (null == list) {
                list = new ArrayList<>();
                groupMap.put(hostCreateParam.getServerId(), list);
            }

            list.add(hostCreateParam);
        }

        for (List<HostCreateParam> batch : groupMap.values()) {
            HostCreateParam first = batch.get(0);
            MWZabbixAPIResult result = mwtpServerAPI.hostBatchCreate(first.getServerId(), batch);
            if (result != null && result.isFail()) {
                log.error("批量新增资产纳管result:" + result);
                throw new HostCreatFailException("批量新增资产失败:" + result.getMessage() + ":" + result.getData());
            }
            log.info("manage resultGetCode::" + strValueConvert(result.getCode()));
            if (result.code == successCode) {
                log.info("manage result::" + strValueConvert(result.getData()));
                String hostids = strValueConvert(result.getData());
                if (strValueConvert(result.getData()).endsWith("]}")) {
                    HostCreatResult hostCreatResult = JSONObject.parseObject(hostids, HostCreatResult.class);
                    List<String> values = hostCreatResult.getHostids();
                    if (null != values) {
                        for (int i = 0; i < values.size(); i++) {
                            String value = values.get(i);
                            HostCreateParam hostCreateParam = batch.get(i);
                            AddUpdateTangAssetsParam zabbixParam = indexMap.get(hostCreateParam.getBatchIndex());
                            zabbixParam.setAssetsId(value);
                            zabbixParam.setTPServerHostName(hostCreateParam.getHost());
                            zabbixParam.setHostName(hostCreateParam.getVisibleName());
                        }
                    }
                }
            }
        }
    }


    @Override
    public MwTangibleassetsDTO selectByIp(String ip) {
        List<MwTangibleassetsDTO> ret = mwTangibleAssetsDao.checkIpAddress(ip);
        if (null != ret && ret.size() > 0) {
            return ret.get(0);
        }
        return null;
    }

    @Override
    public Reply getTemplateMacrosByTemplateId(int monitorServerId, String templateId) {
        List<MacrosDTO> macros = new ArrayList<>();
        if (monitorServerId >= 0) {
            MWZabbixAPIResult result = mwtpServerAPI.getMacrosByTemplateId(monitorServerId, templateId);
            if (!result.isFail()) {
                List<MacrosDTO> macrosList = mwMacrosDao.selectMacros();
                Map<String, List<MacrosDTO>> collect = macrosList.stream().collect(Collectors.groupingBy(Macros::getMacro));
                JsonNode node = (JsonNode) result.getData();
                if (node.size() > 0) {
                    node.forEach(macro -> {
//                        String chMacro = mwMacrosDao.selectChMacro(macro.get("macro").asText());
                        List<MacrosDTO> macroDTO = collect.get(macro.get("macro").asText());
                        if (macroDTO != null && macroDTO.size() > 0) {
                            MacrosDTO macrosDTO = macroDTO.get(0);
                            macrosDTO.setValue(macro.get("value").asText());
                            macros.add(macrosDTO);
                        }
//                    macrosDTO.setChMacro((chMacro != null && StringUtils.isNotEmpty(chMacro)) ? chMacro : macro.get("macro").asText());
                    });
                }
            }
        }
        return Reply.ok(macros);
    }

    @Override
    public Reply getTemplateListByMode(AddUpdateTangAssetsParam aParam) {
        if (null != aParam.getMonitorModeName() && StringUtils.isNotEmpty(aParam.getMonitorModeName())) {
            RuleType ruleType = getInfoByName(aParam.getMonitorModeName());
            aParam.setMonitorMode(ruleType.getMonitorMode());
        }
        //根据添加模式判断是否需要查询所有模板
        List<MwAssetsTemplateDTO> data = new ArrayList<>();
        if (aParam.getAddPattern() == null || aParam.getAddPattern() != 1) {
            data = mwAseetstemplateTableDao.getTemplateByServerIdAndMonitorMode(aParam);
        }
        if (aParam.getAddPattern() != null && aParam.getAddPattern() == 1) {
            data = mwAseetstemplateTableDao.getByServerIdAllTemplate(aParam);
        }
        return Reply.ok(data);
    }

    @Override
    public Reply updateAssetsTemplateIds() {
        List<MwAssetsIdsDTO> allAssetsIdInfo = mwTangibleAssetsDao.getAllAssetsIds();
        List<MwAssetsIdsDTO> updataTemplateList = new ArrayList<>();
        Map<Integer, List<String>> mapHost = new HashMap();
        if (allAssetsIdInfo != null && allAssetsIdInfo.size() > 0) {
            //将模型名称当成key值，转为map数据
            for (MwAssetsIdsDTO assets : allAssetsIdInfo) {
                Integer serverId = assets.getMonitorServerId();
                String hostId = assets.getHostId();
                if (mapHost.containsKey(serverId)) {
                    List<String> hostIdList1 = mapHost.get(serverId);
                    hostIdList1.add(hostId);
                    mapHost.put(serverId, hostIdList1);
                } else {
                    List<String> hostIdList = new ArrayList<>();
                    hostIdList.add(hostId);
                    mapHost.put(assets.getMonitorServerId(), hostIdList);
                }
            }
            mapHost.forEach((k, v) -> {
                if (v != null && v.size() > 0) {
                    MWZabbixAPIResult result = mwtpServerAPI.getHostInfosById(k, v);
                    if (!result.isFail()) {
                        JsonNode node = (JsonNode) result.getData();
                        if (node.size() > 0) {
                            node.forEach(data -> {
                                MwAssetsIdsDTO mwDto = new MwAssetsIdsDTO();
                                String templateId = "";
                                if (data.get("parentTemplates").size() > 0) {
                                    //根据接口Api获取templateId
                                    templateId = data.get("parentTemplates").get(0).get("templateid").asText();
                                }
                                String hostId = data.get("hostid").asText();
                                if (!Strings.isNullOrEmpty(templateId) && !Strings.isNullOrEmpty(hostId)) {
                                    mwDto.setTemplateId(templateId);
                                    mwDto.setHostId(hostId);
                                    updataTemplateList.add(mwDto);
                                }
                            });
                        }
                    }
                }

            });
            if (updataTemplateList.size() > 0) {
                mwTangibleAssetsDao.updateTemplateIdBatch(updataTemplateList);
            }
        }
        mwOutbandAssetsService.updateAssetsTemplateIds();
        return Reply.ok("更新成功");
    }

    @Override
    public List<MwTangibleassetsTable> selectBySrecah(String search, Boolean disableWildcard) {
        String value = null == disableWildcard ? "" : Boolean.toString(disableWildcard).toLowerCase();
        List<MwTangibleassetsTable> tangibleassetsTables = mwTangibleAssetsDao.selectBySrecah(search, value);
        return tangibleassetsTables;
    }

    @Override
    public MwTangibleassetsDTO selectByAssetsIdAndServerId(String assetsId, int monitorServerId) {
        MwTangibleassetsDTO mwTangibleassetsDTO = mwTangibleAssetsDao.selectByAssetsIdAndServerId(assetsId, monitorServerId);
        return mwTangibleassetsDTO;
    }

    @Autowired
    MwWebMonitorService mwWebMonitorService;

    @Resource
    private MwWebmonitorTableDao mwWebmonitorTableDao;

    @Resource
    MWNetWorkLinkDao mwNetWorkLinkDao;

    @Autowired
    MWNetWorkLinkService mwNetWorkLinkService;

    /**
     * 删除资产时删除对应的web监测模块的数据
     *
     * @param idList
     */
    @Transactional
    public void deleteAssetsMonitorMapper(List<DeleteTangAssetsID> idList) {
        List<String> ids = new ArrayList<>();
        idList.forEach(dto -> {
            ids.add(dto.getId());
        });
        /**
         * 根据资产主键查询web监测需要删除的数据，然后调用删除方法
         */
        DeleteWebMonitorParam deleteWebMonitorParam = new DeleteWebMonitorParam();
        List<Integer> webids = mwWebmonitorTableDao.selectIds(ids);

        List<HttpParam> httpParams = mwWebmonitorTableDao.selectHttpIds(ids);
        if (webids.size() > 0 && httpParams.size() > 0) {
            deleteWebMonitorParam.setHttpTestIds(httpParams);
            deleteWebMonitorParam.setIdList(webids);
            Reply reply = mwWebMonitorService.deleteWebMonitor(deleteWebMonitorParam);
            if (reply.getRes() != 0) {
                throw new RuntimeException("删除web监测数据失败");
            }
        }
        /**
         *删除资产时删除对应的线路数据
         */
        List<String> linkids = mwNetWorkLinkDao.selectAssetsLink(idList);
        if (linkids.size() > 0) {
            mwNetWorkLinkService.deleteMappers(linkids);
        }
    }

    /**
     * 查询资产的整体状态，当资产中有异常的展示黄色感叹号
     *
     * @param assetsList
     * @param statusMap
     * @return
     */
    public String getOverAllStatus(List<AssetsDTO> assetsList, Map<String, String> statusMap, List<String> ids) {
        int unknownCount = 0;
        int abnormalCount = 0;
        int normalCount = 0;
        for (AssetsDTO asset : assetsList) {
            if (ids.contains(asset.getId())) {
                normalCount++;
                continue;
            }
            String s = statusMap.get(asset.getMonitorServerId() + ":" + asset.getAssetsId());
            if (s == null || StringUtils.isEmpty(s)) {
                unknownCount++;
            } else if ("ABNORMAL".equals(s)) {
                abnormalCount++;
            } else {
                normalCount++;
            }
        }
        int size = assetsList.size();

        if (size == unknownCount) {
            return "UNKNOWN";
        }
        if (size == abnormalCount) {
            return "ABNORMAL";
        }
        if (size == normalCount) {
            return "NORMAL";
        }
        if (unknownCount < size || abnormalCount < size) {
            return "WARNING";
        }
        return "";
    }


    private void getAssetsHostId(List<AssetsTreeDTO> treeDTOS, Map<Integer, Set<String>> hostIdMap, List<String> assetsIds) {
        if (!CollectionUtils.isEmpty(treeDTOS)) {
            for (AssetsTreeDTO treeDTO : treeDTOS) {
                List<AssetsDTO> assetsList = treeDTO.getAssetsList();
                if (CollectionUtils.isEmpty(assetsList)) {
                    continue;
                }
                for (AssetsDTO assetsDTO : assetsList) {
                    assetsIds.add(assetsDTO.getId());
                    String assetsId = assetsDTO.getAssetsId();
                    Integer monitorServerId = assetsDTO.getMonitorServerId();
                    if (StringUtils.isBlank(assetsId) || monitorServerId == null) {
                        continue;
                    }
                    if (hostIdMap.get(monitorServerId) == null) {
                        Set<String> hostIds = new HashSet<>();
                        hostIds.add(assetsId);
                        hostIdMap.put(monitorServerId, hostIds);
                        continue;
                    }
                    if (hostIdMap.get(monitorServerId) != null) {
                        Set<String> hostIds = hostIdMap.get(monitorServerId);
                        hostIds.add(assetsId);
                        hostIdMap.put(monitorServerId, hostIds);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * 获取zabbix 中所有资产的资产状态
     *
     * @return
     */
    public Map<String, String> getAllAssetsStatus(Map<Integer, Set<String>> hostIdMap) {
        Map<String, String> statusMap = new HashMap<>();
        if (hostIdMap.isEmpty()) {
            return statusMap;
        }
        Set<String> hostSets = new HashSet<>();
        log.info("查询资产状态" + hostIdMap);
        for (Map.Entry<Integer, Set<String>> entry : hostIdMap.entrySet()) {
            Integer key = entry.getKey();
            Set<String> value = entry.getValue();
            log.info("查询资产树状结构数据资产状态开始," + value.size() + new Date());
            MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(key, ZabbixItemConstant.NEW_ASSETS_STATUS, value);
            log.info("查询资产树状结构数据资产状态结束," + value.size() + new Date());
            if (statusData != null && !statusData.isFail()) {
                JsonNode jsonNode = (JsonNode) statusData.getData();
                if (jsonNode.size() > 0) {
                    for (JsonNode node : jsonNode) {
                        Integer lastvalue = node.get("lastvalue").asInt();
                        String hostId = node.get("hostid").asText();
                        String name = node.get("name").asText();
                        if ((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)) {
                            String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                            statusMap.put(key + ":" + hostId, status);
                            hostSets.add(hostId);
                        }
                        if (hostSets.contains(hostId)) {
                            continue;
                        }
                        String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                        statusMap.put(key + ":" + hostId, status);
                    }
                }
            }
            /*statusMap.put(key + ":" + value, "ABNORMAL");*/
        }
        return statusMap;
    }

    /**
     * 查询资产查询条件为文本输入的所有数据，实现可以模糊下拉功能
     * 该接口以整合到所有字段模糊查询接口中，暂时该接口不使用
     *
     * @return
     */
    @Override
    public Reply selectAssetsSearchTermData() {
        return Reply.ok("");
    }

    /**
     * 有形资产模糊搜索所有字段联想
     *
     * @param value
     * @return
     */
    @Override
    public Reply fuzzSeachAllFiledData(String value, boolean assetsIOTFlag) {
        //根据值模糊查询数据
        List<Map<String, String>> fuzzSeachAllFileds = mwTangibleAssetsDao.fuzzSeachAllFiled(value, assetsIOTFlag);
        Set<String> fuzzSeachData = new HashSet<>();
        if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
            for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                String assetsName = fuzzSeachAllFiled.get("assetsName") != null ? fuzzSeachAllFiled.get("assetsName") : "";
                String hostName = fuzzSeachAllFiled.get("hostName") != null ? fuzzSeachAllFiled.get("hostName") : "";
                String inBandIp = fuzzSeachAllFiled.get("inBandIp") != null ? fuzzSeachAllFiled.get("inBandIp") : "";
                String manufacturer = fuzzSeachAllFiled.get("manufacturer") != null ? fuzzSeachAllFiled.get("manufacturer") : "";
                String specifications = fuzzSeachAllFiled.get("specifications") != null ? fuzzSeachAllFiled.get("specifications") : "";
                String typeName = fuzzSeachAllFiled.get("typeName") != null ? fuzzSeachAllFiled.get("typeName") : "";
                String subTapeName = fuzzSeachAllFiled.get("subTapeName") != null ? fuzzSeachAllFiled.get("subTapeName") : "";
                value = value != null ? value : "";
                if (!Strings.isNullOrEmpty(assetsName) && assetsName.contains(value)) {
                    fuzzSeachData.add(assetsName.trim());
                }
                if (!Strings.isNullOrEmpty(hostName) && hostName.contains(value)) {
                    fuzzSeachData.add(hostName.trim());
                }
                if (!Strings.isNullOrEmpty(inBandIp) && inBandIp.contains(value)) {
                    fuzzSeachData.add(inBandIp.trim());
                }
                if (!Strings.isNullOrEmpty(manufacturer) && manufacturer.contains(value)) {
                    fuzzSeachData.add(manufacturer.trim());
                }
                if (!Strings.isNullOrEmpty(specifications) && specifications.contains(value)) {
                    fuzzSeachData.add(specifications.trim());
                }
                if (!Strings.isNullOrEmpty(typeName) && typeName.contains(value)) {
                    fuzzSeachData.add(typeName.trim());
                }
                if (!Strings.isNullOrEmpty(subTapeName) && subTapeName.contains(value)) {
                    fuzzSeachData.add(subTapeName.trim());
                }
            }
        }
        List<String> fuzzyQuerys = new ArrayList<>();
        fuzzyQuerys.addAll(fuzzSeachData);
        Collections.sort(fuzzyQuerys);
        AssetsSearchTermFuzzyParam assetsSearchTermFuzzyParam = new AssetsSearchTermFuzzyParam();
        getAssetsFiledFuzzyQuery(assetsSearchTermFuzzyParam);
        assetsSearchTermFuzzyParam.setFuzzyQuery(fuzzyQuerys);
        return Reply.ok(assetsSearchTermFuzzyParam);
    }

    private void getAssetsFiledFuzzyQuery(AssetsSearchTermFuzzyParam assetsSearchTermFuzzyParam) {
        //查询资产查询条件中的资产名称，主机名称，IP地址，规格数据
        List<Map<String, String>> fuzzyQuerys = mwTangibleAssetsDao.selectAssetsTermFuzzyQuery();
        //资产id集合
        Set<String> assetsIds = new HashSet<String>();
        //资产名称集合
        Set<String> assetsNames = new HashSet<String>();
        //主机名称集合
        Set<String> hostNames = new HashSet<String>();
        //IP地址集合
        Set<String> inBandIps = new HashSet<String>();
        //资产规格集合
        Set<String> specifications = new HashSet<String>();
        if (!CollectionUtils.isEmpty(fuzzyQuerys)) {
            fuzzyQuerys.forEach(assetsNews -> {
                //资产ID
                if (StringUtils.isNotBlank(assetsNews.get("assetsId"))) {
                    String assetsId = assetsNews.get("assetsId");
                    assetsIds.add(assetsId.trim());
                }
                //资产名称
                if (StringUtils.isNotBlank(assetsNews.get("assetsName"))) {
                    String assetsName = assetsNews.get("assetsName");
                    assetsNames.add(assetsName.trim());
                }
                //主机名称
                if (StringUtils.isNotBlank(assetsNews.get("hostName"))) {
                    String hostName = assetsNews.get("hostName");
                    hostNames.add(hostName.trim());
                }
                //IP地址
                if (StringUtils.isNotBlank(assetsNews.get("inBandIp"))) {
                    String inBandIp = assetsNews.get("inBandIp");
                    inBandIps.add(inBandIp.trim());
                }
                //规格
                if (StringUtils.isNotBlank(assetsNews.get("specifications"))) {
                    String specification = assetsNews.get("specifications");
                    specifications.add(specification.trim());
                }
            });
        }
        //资产id集合
        List<String> assetsIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(assetsIds)) {
            assetsIdList.addAll(assetsIds);
            Collections.sort(assetsIdList);
        }
        //资产名称集合
        List<String> assetsNameList = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(assetsNames)) {
            assetsNameList.addAll(assetsNames);
            Collections.sort(assetsNameList);
        }
        //主机名称集合
        List<String> hostNameList = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(hostNames)) {
            hostNameList.addAll(hostNames);
            Collections.sort(hostNameList);
        }
        //IP地址集合
        List<String> inBandIpList = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(inBandIps)) {
            inBandIpList.addAll(inBandIps);
            Collections.sort(inBandIpList);
        }
        //资产规格集合
        List<String> specificationList = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(specifications)) {
            specificationList.addAll(specifications);
            Collections.sort(specificationList);
        }
        assetsSearchTermFuzzyParam.setAssetsIds(assetsIdList);
        assetsSearchTermFuzzyParam.setAssetsName(assetsNameList);
        assetsSearchTermFuzzyParam.setHostName(hostNameList);
        assetsSearchTermFuzzyParam.setInBandIp(inBandIpList);
        assetsSearchTermFuzzyParam.setSpecifications(specificationList);
        List<DropdownDTO> list = new ArrayList<>();
        List<DropdownDTO> dtos = mwTangibleAssetsDao.selectAssetsTypeList(0, 1);
        assetsSearchTermFuzzyParam.setAssetsTypes(dtos);
        if (dtos != null && dtos.size() > 0) {
            for (DropdownDTO pid : dtos) {
                list.addAll(mwTangibleAssetsDao.selectAssetsTypeList(pid.getDropKey(), 1));
            }
        }
        assetsSearchTermFuzzyParam.setAssetsSubTypes(list);

    }


    /**
     * 批量编辑资产时查询资产标签的交集
     *
     * @param updateTangAssetsParam 资产数据
     * @return
     */
    @Override
    public Reply batchEditAssetsGetLabel(UpdateTangAssetsParam updateTangAssetsParam) {
        List<MwAssetsLabelDTO> labels = new ArrayList<>();
        //获取选中的资产ID
        List<String> ids = updateTangAssetsParam.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return Reply.ok(labels);
        }
        //根据资产ID查询出所有的标签数据
        List<List<MwAssetsLabelDTO>> assetsLabels = new ArrayList<>();
        for (String id : ids) {
            List<MwAssetsLabelDTO> labelDtos = mwLabelCommonServcie.getLabelBoard(id, updateTangAssetsParam.getAssetsType());
            if (!CollectionUtils.isEmpty(labelDtos)) {
                for (MwAssetsLabelDTO labelDto : labelDtos) {
                    labelDto.setLabelId(labelDto.getId());
                }
            }
            assetsLabels.add(labelDtos);
        }
        if (assetsLabels.size() > 1) {
            Integer count = 1;
            labels = getManyAssetsLabelIntersection(assetsLabels, count);
        }
        return Reply.ok(labels);
    }

    /**
     * 有形资产批量编辑时删除标签数据
     *
     * @param labelDTOs 标签数据
     * @return
     */
    @Override
    public Reply batchEditAssetsDeleteLabel(List<MwAssetsLabelDTO> labelDTOs) {
        if (CollectionUtils.isEmpty(labelDTOs)) {
            return Reply.ok("删除成功");
        }
        for (MwAssetsLabelDTO labelDTO : labelDTOs) {
            //资产ID集合
            List<String> assetsIds = labelDTO.getAssetsIds();
            String inputFormat = labelDTO.getInputFormat();
            if (CollectionUtils.isEmpty(assetsIds) || StringUtils.isBlank(inputFormat)) {
                return Reply.ok("删除成功");
            }
            switch (inputFormat) {
                case "1":
                    Map<String, Object> textLabelMap = new HashMap<>();
                    //文本类型标签删除
                    String tagboard = labelDTO.getTagboard();
                    //添加删除需要的参数
                    textLabelMap.put("tableName", "mw_label_mapper");
                    textLabelMap.put("module", "ASSETS");
                    textLabelMap.put("tagboard", tagboard);
                    textLabelMap.put("typeIds", assetsIds);
                    mwTangibleAssetsDao.deleteAssetsLabel(textLabelMap);
                    break;
                case "2":
                    Map<String, Object> dateLabelMap = new HashMap<>();
                    //日期类型标签删除
                    Date dateTagboard = labelDTO.getDateTagboard();
                    //添加删除需要的参数
                    dateLabelMap.put("tableName", "mw_label_date_mapper");
                    dateLabelMap.put("module", "ASSETS");
                    dateLabelMap.put("tagboard", dateTagboard);
                    dateLabelMap.put("typeIds", assetsIds);
                    mwTangibleAssetsDao.deleteAssetsLabel(dateLabelMap);
                    break;
                case "3":
                    Map<String, Object> dropLabelMap = new HashMap<>();
                    //下拉类型标签删除
                    Integer dropTagboard = labelDTO.getDropTagboard();
                    //添加删除需要的参数
                    dropLabelMap.put("tableName", "mw_label_drop_mapper");
                    dropLabelMap.put("module", "ASSETS");
                    dropLabelMap.put("tagboard", dropTagboard);
                    dropLabelMap.put("typeIds", assetsIds);
                    mwTangibleAssetsDao.deleteAssetsLabel(dropLabelMap);
                    break;
            }
        }
        return Reply.ok("删除成功");
    }


    private List<MwAssetsLabelDTO> getManyAssetsLabelIntersection(List<List<MwAssetsLabelDTO>> assetsLabels, Integer count) {
        List<MwAssetsLabelDTO> intersectionLabelDtos = new ArrayList<>();
        if (CollectionUtils.isEmpty(intersectionLabelDtos)) {
            intersectionLabelDtos.addAll(assetsLabels.get(0));
        }
        List<MwAssetsLabelDTO> list = new ArrayList<>();
        for (MwAssetsLabelDTO mwAssetsLabelDTO : intersectionLabelDtos) {
            //A集合标签类型
            String inputFormatA = mwAssetsLabelDTO.getInputFormat();
            //A集合标签ID
            Integer labelIdA = mwAssetsLabelDTO.getId();
            switch (inputFormatA) {
                case "1":
                    //文本类型
                    //A集合文本值
                    String textTagboardA = mwAssetsLabelDTO.getTagboard();
                    for (MwAssetsLabelDTO assetsLabelDTO : assetsLabels.get(count)) {
                        //B集合标签类型
                        String inputFormatB = assetsLabelDTO.getInputFormat();
                        //B集合标签ID
                        Integer labelIdB = assetsLabelDTO.getId();
                        //B集合文本值
                        String textTagboardB = assetsLabelDTO.getTagboard();
                        if (inputFormatA.equals(inputFormatB) && labelIdA.equals(labelIdB) && textTagboardA.equals(textTagboardB)) {
                            list.add(assetsLabelDTO);
                        }
                    }
                    break;
                case "2":
                    //日期类型
                    //A集合日期值
                    Date dateTagboardA = mwAssetsLabelDTO.getDateTagboard();
                    for (MwAssetsLabelDTO assetsLabelDTO : assetsLabels.get(count)) {
                        //B集合标签类型
                        String inputFormatB = assetsLabelDTO.getInputFormat();
                        //B集合标签ID
                        Integer labelIdB = assetsLabelDTO.getId();
                        //B集合日期值
                        Date dateTagboardB = assetsLabelDTO.getDateTagboard();
                        if (inputFormatA.equals(inputFormatB) && labelIdA.equals(labelIdB) && dateTagboardA.compareTo(dateTagboardB) == 0) {
                            list.add(assetsLabelDTO);
                        }
                    }
                    break;
                case "3":
                    //日期类型
                    //A集合日期值
                    Integer dropTagboardA = mwAssetsLabelDTO.getDropTagboard();
                    for (MwAssetsLabelDTO assetsLabelDTO : assetsLabels.get(count)) {
                        //B集合标签类型
                        String inputFormatB = assetsLabelDTO.getInputFormat();
                        //B集合标签ID
                        Integer labelIdB = assetsLabelDTO.getId();
                        //B集合日期值
                        Integer dropTagboardB = assetsLabelDTO.getDropTagboard();
                        if (inputFormatA.equals(inputFormatB) && labelIdA.equals(labelIdB) && dropTagboardA.equals(dropTagboardB)) {
                            list.add(assetsLabelDTO);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        count++;
        intersectionLabelDtos.clear();
        intersectionLabelDtos.addAll(list);
        if (!CollectionUtils.isEmpty(intersectionLabelDtos) && count < assetsLabels.size()) {
            getManyAssetsLabelIntersection(assetsLabels, count);
        }
        return intersectionLabelDtos;
    }


    /**
     * 资产修改时将新增的标签数据与资产原有的数据进行合并
     *
     * @param uParam 资产修改的数据
     */
    private Map<String, List<MwAssetsLabelDTO>> batchEditAssetsLabelHandle(UpdateTangAssetsParam uParam) {
        List<String> ids = uParam.getIds();
        Map<String, List<MwAssetsLabelDTO>> newAssetsLabelDto = new HashMap<>();
        //将原来的标签与现在新增的标签进行合并
        for (String id : ids) {
            List<MwAssetsLabelDTO> assetsLabel = uParam.getAssetsLabel();
            List<MwAssetsLabelDTO> newAssetsLabels = new ArrayList<>();
            newAssetsLabels.addAll(assetsLabel);
            List<MwAssetsLabelDTO> oldLabelDtos = mwLabelCommonServcie.getLabelBoard(id, "ASSETS");
            if (!CollectionUtils.isEmpty(oldLabelDtos)) {
                for (MwAssetsLabelDTO oldLabelDto : oldLabelDtos) {
                    String inputFormat = oldLabelDto.getInputFormat();
                    switch (inputFormat) {
                        case "1":
                            //说明是文本类型标签
                            //文本标签值
                            String tagboard = oldLabelDto.getTagboard();
                            //文本标签ID
                            Integer textLabelId = oldLabelDto.getId();
                            boolean flag = true;
                            for (MwAssetsLabelDTO mwAssetsLabelDTO : newAssetsLabels) {
                                //如果原来这个标签已存在，现在添加同样的标签，只取其中一条
                                if ("1".equals(mwAssetsLabelDTO.getInputFormat()) && tagboard.equals(mwAssetsLabelDTO.getTagboard())
                                        && textLabelId == mwAssetsLabelDTO.getLabelId()) {
                                    flag = false;
                                }
                            }
                            //将数据合并到同一个集合中
                            if (flag) {
                                oldLabelDto.setTypeId(id);
                                oldLabelDto.setModuleType("ASSETS");
                                oldLabelDto.setLabelId(textLabelId);
                                newAssetsLabels.add(oldLabelDto);
                            }
                            break;
                        case "2":
                            //时间类型标签
                            //时间标签的值
                            Date dateTagboard = oldLabelDto.getDateTagboard();
                            //标签ID
                            Integer dateLabelId = oldLabelDto.getId();
                            boolean dateflag = true;
                            for (MwAssetsLabelDTO mwAssetsLabelDTO : newAssetsLabels) {
                                //如果原来这个标签已存在，现在添加同样的标签，只取其中一条
                                if ("2".equals(mwAssetsLabelDTO.getInputFormat()) && dateTagboard.compareTo(mwAssetsLabelDTO.getDateTagboard()) == 0
                                        && dateLabelId == mwAssetsLabelDTO.getLabelId()) {
                                    dateflag = false;
                                }
                            }
                            //将数据合并到同一个集合中
                            if (dateflag) {
                                oldLabelDto.setTypeId(id);
                                oldLabelDto.setLabelId(dateLabelId);
                                oldLabelDto.setModuleType("ASSETS");
                                newAssetsLabels.add(oldLabelDto);
                            }
                            break;
                        case "3":
                            //下拉类型的标签
                            //下拉数据的ID
                            Integer dropTagboard = oldLabelDto.getDropTagboard();
                            //标签ID
                            Integer dropLabelId = oldLabelDto.getId();
                            //如果下拉ID为空，说明是新增的值，直接添加
                            boolean dropflag = true;
                            for (MwAssetsLabelDTO mwAssetsLabelDTO : newAssetsLabels) {
                                //如果原来这个标签已存在，现在添加同样的标签，只取其中一条
                                if (mwAssetsLabelDTO.getDropTagboard() != null && "3".equals(mwAssetsLabelDTO.getInputFormat()) &&
                                        dropTagboard.compareTo(mwAssetsLabelDTO.getDropTagboard()) == 0 && dropLabelId == mwAssetsLabelDTO.getLabelId()) {
                                    dropflag = false;
                                }
                            }
                            //将数据合并到同一个集合中
                            if (dropflag) {
                                oldLabelDto.setTypeId(id);
                                oldLabelDto.setLabelId(dropLabelId);
                                oldLabelDto.setModuleType("ASSETS");
                                newAssetsLabels.add(oldLabelDto);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            newAssetsLabelDto.put(id, newAssetsLabels);
        }
        return newAssetsLabelDto;
    }

    private String getLabelRepeatData(List<MwAssetsLabelDTO> list, String typeId, String moduleType) {
        //不同类型的标签存入不同的mapper表中
        List<MwAssetsLabelDTO> dropTagboardList = new ArrayList<>();
        list.forEach(
                mwAssetsLabelDTO -> {
                    Integer labelId = mwAssetsLabelDTO.getLabelId();
                    if (null != labelId && labelId != 0) {
                        String inputFormat = mwAssetsLabelDTO.getInputFormat();
                        mwAssetsLabelDTO.setTypeId(typeId);//资产id对应的typeId
                        mwAssetsLabelDTO.setModuleType(moduleType);
                        switch (inputFormat) {
                            case "3"://3.下拉
                                dropTagboardList.add(mwAssetsLabelDTO);
                                break;
                            default://4.其他
                                break;
                        }
                    }
                });
        if (dropTagboardList.size() > 0) {
            //判断标签是否存在
            String msg = checkDropLabelValueOnly(dropTagboardList);
            return msg;
        }
        return "";
    }

    /**
     * 判断下拉的数据是否有重复值
     *
     * @param dropTagboardList 下拉标签数据
     */
    private String checkDropLabelValueOnly(List<MwAssetsLabelDTO> dropTagboardList) {
        StringBuilder msg = new StringBuilder();
        Set<String> repeatData = new HashSet<>();
        Map<String, Object> map = new HashMap<>();
        for (MwAssetsLabelDTO mwAssetsLabelDTO : dropTagboardList) {
            Integer labelId = mwAssetsLabelDTO.getLabelId();
            String dropValue = mwAssetsLabelDTO.getDropValue();
            if (map.get(labelId + dropValue) != null) {
                repeatData.add(dropValue);
            } else {
                map.put(labelId + dropValue, dropValue);
            }
        }
        for (MwAssetsLabelDTO mwAssetsLabelDTO : dropTagboardList) {
            //标签ID
            Integer labelId = mwAssetsLabelDTO.getLabelId();
            Integer dropId = mwAssetsLabelDTO.getDropTagboard();
            //根据标签ID查询标签Code
            String labelCode = mwTangibleAssetsDao.getLabelCode(labelId);
            //根据标签Code和dropID和标签值查询是否有重复数据
            Integer count = mwTangibleAssetsDao.getDropLabelRepeatData(labelCode, dropId, mwAssetsLabelDTO.getDropValue());
            if (count > 0) {
                repeatData.add(mwAssetsLabelDTO.getDropValue());
            }
        }
        if (repeatData != null && repeatData.size() > 0) {
            for (String value : repeatData) {
                msg.append(value + "、");
            }
        }
        if (msg != null && msg.length() > 0) {
            msg.deleteCharAt(msg.length() - 1);
            msg.insert(0, "以下标签值：");
            msg.append("已存在！");
            return msg.toString();
        }
        return "";
    }

    /**
     * @param treeDTOS 最终组成的数据
     */
    private void treeListAssetsTypeGroup(List<AssetsTreeDTO> treeDTOS) {
        if (CollectionUtils.isEmpty(treeDTOS)) {
            return;
        }
        for (AssetsTreeDTO treeDTO : treeDTOS) {
            List<AssetsDTO> assetsList = treeDTO.getAssetsList();
            if (CollectionUtils.isEmpty(assetsList)) {
                continue;
            }
            List<String> assetsIds = new ArrayList<>();
            for (AssetsDTO assetsDTO : assetsList) {
                String id = assetsDTO.getId();
                assetsIds.add(id);
            }
            //根据ID查询资产类型
            List<AssetsTreeDTO> dtos = mwAssetsTypeDao.selectAssetsType(assetsIds);
            //根据资产类型进行分组
            List<AssetsTreeDTO> children = treeDTO.getChildren();
            if (children == null) {
                treeDTO.setChildren(dtos);
            } else {
                children.addAll(dtos);
            }
//            Map<String,List<AssetsDTO>> assetsTypeMap = new HashMap<>();
//            for (AssetsDTO assetsDTO : assetsDTOS) {
//                String typeName = assetsDTO.getTypeName();
//                if(assetsTypeMap.get(typeName) == null){
//                    List<AssetsDTO> dtos = new ArrayList<>();
//                    dtos.add(assetsDTO);
//                    assetsTypeMap.put(typeName,dtos);
//                    continue;
//                }
//                if(assetsTypeMap.get(typeName) != null){
//                    List<AssetsDTO> dtos = assetsTypeMap.get(typeName);
//                    dtos.add(assetsDTO);
//                    assetsTypeMap.put(typeName,dtos);
//                    continue;
//                }
//            }
//            for (Map.Entry<String, List<AssetsDTO>> entry : assetsTypeMap.entrySet()) {
//                String typeName = entry.getKey();
//                List<AssetsDTO> dtoList = entry.getValue();
//                AssetsTreeDTO dto = new AssetsTreeDTO();
//                dto.setTypeName(typeName);
//                dto.setTypeId();
//            }
//        }
        }
    }

    /**
     * 拆分标签多选的值
     *
     * @param assetsLabel
     */
    private void assetsLabelCheckBoxHandle(List<MwAssetsLabelDTO> assetsLabel) {
        List<MwAssetsLabelDTO> labelDTOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(assetsLabel)) {
            return;
        }
        for (MwAssetsLabelDTO mwAssetsLabelDTO : assetsLabel) {
            if ("3".equals(mwAssetsLabelDTO.getInputFormat())) {
                List<DropdownDTO> dropDownS = mwAssetsLabelDTO.getDropDowns();
                if (CollectionUtils.isEmpty(dropDownS)) {
                    labelDTOS.add(mwAssetsLabelDTO);
                    continue;
                }
                for (DropdownDTO dropDown : dropDownS) {
                    MwAssetsLabelDTO labelDto = new MwAssetsLabelDTO();
                    BeanUtils.copyProperties(mwAssetsLabelDTO, labelDto);
                    labelDto.setDropTagboard(dropDown.getDropId());
                    labelDto.setDropKey(dropDown.getDropKey());
                    labelDto.setDropValue(dropDown.getDropValue());
                    labelDTOS.add(labelDto);
                }
            } else {
                labelDTOS.add(mwAssetsLabelDTO);
            }
        }
        assetsLabel.clear();
        assetsLabel.addAll(labelDTOS);
    }

    /**
     * 设置资产树状结构资产状态分类
     *
     * @param treeDTOS        资产树状结构数据
     * @param allAssetsStatus 资产状态集合
     * @param ids
     */
    private void treeAssetsStstusClassIfy(List<AssetsTreeDTO> treeDTOS, Map<String, String> allAssetsStatus, List<String> ids) {
        if (!CollectionUtils.isEmpty(treeDTOS)) {
            for (AssetsTreeDTO treeDTO : treeDTOS) {
                List<AssetsTreeDTO> children = treeDTO.getChildren();
                Map<String, List<AssetsDTO>> map = new HashMap<>();
                //如果夏季数据为空，说明时最末级数据，在末级数据下再增加状态分类
                if (CollectionUtils.isEmpty(children)) {
                    //获取资产数据
                    List<AssetsDTO> assetsList = treeDTO.getAssetsList();
                    if (!CollectionUtils.isEmpty(assetsList)) {
                        for (AssetsDTO assetsDTO : assetsList) {
                            String status = "";
                            if (!ids.contains(assetsDTO.getId())) {
                                String s = allAssetsStatus.get(assetsDTO.getMonitorServerId() + ":" + assetsDTO.getAssetsId());
                                if (s != null && StringUtils.isNotEmpty(s)) {
                                    status = s;
                                } else {
                                    status = "UNKNOWN";
                                }
                            } else {
                                status = "NORMAL";
                            }
                            if (!CollectionUtils.isEmpty(map.get(status))) {
                                List<AssetsDTO> assetsDTOS = map.get(status);
                                assetsDTOS.add(assetsDTO);
                                map.put(status, assetsDTOS);
                                continue;
                            }
                            if (CollectionUtils.isEmpty(map.get(status))) {
                                List<AssetsDTO> assetsDTOS = new ArrayList<>();
                                assetsDTOS.add(assetsDTO);
                                map.put(status, assetsDTOS);
                                continue;
                            }
                        }
                    }
                    List<AssetsTreeDTO> treeDTOList = new ArrayList<>();
                    if (!map.isEmpty()) {
                        for (Map.Entry<String, List<AssetsDTO>> entry : map.entrySet()) {
                            String status = entry.getKey();
                            AssetsTreeDTO assetsTreeDTO = new AssetsTreeDTO();
                            List<AssetsDTO> assetsDTOS = entry.getValue();
                            switch (status) {
                                case "NORMAL":
                                    assetsTreeDTO.setAssetsList(assetsDTOS);
                                    assetsTreeDTO.setChildren(new ArrayList<>());
                                    assetsTreeDTO.setCount(assetsDTOS.size());
                                    assetsTreeDTO.setTypeName("正常");
                                    assetsTreeDTO.setStatusUrl("NORMAL");
                                    treeDTOList.add(assetsTreeDTO);
                                    break;
                                case "ABNORMAL":
                                    assetsTreeDTO.setAssetsList(assetsDTOS);
                                    assetsTreeDTO.setChildren(new ArrayList<>());
                                    assetsTreeDTO.setCount(assetsDTOS.size());
                                    assetsTreeDTO.setTypeName("异常");
                                    assetsTreeDTO.setStatusUrl("ABNORMAL");
                                    treeDTOList.add(assetsTreeDTO);
                                    break;
                                case "UNKNOWN":
                                    assetsTreeDTO.setAssetsList(assetsDTOS);
                                    assetsTreeDTO.setChildren(new ArrayList<>());
                                    assetsTreeDTO.setCount(assetsDTOS.size());
                                    assetsTreeDTO.setTypeName("未监控");
                                    assetsTreeDTO.setStatusUrl("UNKNOWN");
                                    treeDTOList.add(assetsTreeDTO);
                                    break;
                                default://4.其他
                                    break;
                            }
                        }
                    }
                    treeDTO.setChildren(treeDTOList);
                }
                treeAssetsStstusClassIfy(children, allAssetsStatus, ids);
            }
        }
    }

    /**
     * 删除资产时判断线路关联
     *
     * @param ids
     */
    @Override
    @Transactional
    public List<String> deleteAssetsCheckLinkRelation(List<DeleteTangAssetsID> ids) {

        List<DeleteTangAssetsID> delIds = ids.stream()
                .filter(item -> StringUtils.isNotEmpty(item.getAssetsId())).collect(Collectors.toList());

        //查询所有线路所关联资产
        if (CollectionUtils.isEmpty(delIds)) return null;
        //拿出所有删除的主机ID
        Set<String> hostIds = new HashSet<>();
        for (DeleteTangAssetsID assets : ids) {
            hostIds.add(assets.getAssetsId());
        }
        //根据主机ID查询关联线路
        List<String> hostIdList = Arrays.asList(hostIds.toArray(new String[0]));
        List<String> linkNames = mwTangibleAssetsDao.selectAssetsRelationLink(hostIdList);
        return linkNames;
    }

    /**
     * 查询所有监控项信息
     *
     * @return
     */
    @Override
    public Reply selectAllMonitorItem() {
        return Reply.ok(mwTangibleAssetsDao.selectAllMonitorItem());
    }

    @Override
    public Reply findAllMonitorServerId() {
        return Reply.ok(monitorServerSet);
    }

    /**
     * 修改zabbix可见名称
     *
     * @param hostId     主机ID
     * @param assetsName 资产名称
     */
    private boolean updateZabbixSoName(Integer serverId, String hostId, String assetsName) {
        if (StringUtils.isNotBlank(hostId) && StringUtils.isNotBlank(assetsName)) {
            //调用zabbix接口根据主机ID修改可见名称
            MWZabbixAPIResult result = mwtpServerAPI.hostUpdateSoName(serverId, hostId, assetsName);
            if (result != null && !result.isFail()) {
                return true;
            }
            //如果名称已重复，则在实例名称后添加4个随机的数字字母。
            if (result.getData().toString().endsWith('"' + assetsName + '"' + " already exists.")) {
                assetsName = assetsName + "_" + UuidUtil.get16Uid();
                MWZabbixAPIResult result1 = mwtpServerAPI.hostUpdateSoName(serverId, hostId, assetsName);
                if (result1 != null && !result1.isFail()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<IpAssetsNameDTO> getAssetsNameByIps(List<String> list) {
        return mwTangibleAssetsDao.getAssetsNameByIps(list);
    }

    private void updateMonitorServerSet() {
        List<MwAssetsIdsDTO> mwAssetsIdsDTOS = mwTangibleAssetsDao.getAllMonitorServerIds();
        Set newSet = new CopyOnWriteArraySet<>();
        for (MwAssetsIdsDTO mwAssetsIdsDTO : mwAssetsIdsDTOS) {
            newSet.add(mwAssetsIdsDTO.getMonitorServerId());
        }
        monitorServerSet = newSet;
    }

    @Override
    public List<String> getAssetsNameByIp(String ip) {
        return mwTangibleAssetsDao.getAssetsNameByIp(ip);
    }

    @Override
    public Map<Integer, AssetTypeIconDTO> selectAllAssetsTypeIcon() {
        List<AssetTypeIconDTO> list = mwAssetsTypeDao.selectAllAssetsTypeIcon();
        Map<Integer, AssetTypeIconDTO> map = list.stream().collect(Collectors.toMap(AssetTypeIconDTO::getId, Function.identity()));
        return map;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            if (!modelAssetEnable && MWAlertAssetsParam.tangibleassetsDTOMap.size() == 0) {
                synchronized (MWAlertAssetsParam.tangibleassetsDTOMap) {
                    if (MWAlertAssetsParam.tangibleassetsDTOMap.size() == 0) {
                        List<MwTangibleassetsDTO> tangibleassetsDTOS = mwTangibleAssetsDao.selectByHostIdandIPs();
                        for (MwTangibleassetsDTO dto : tangibleassetsDTOS) {
                            String key = dto.getAssetsId() + "-" + dto.getInBandIp();
                            MWAlertAssetsParam.tangibleassetsDTOMap.put(key, dto);
                            monitorServerSet.add(dto.getMonitorServerId());
                        }
                    }
                }
                ;
            }
        } catch (Exception e) {
            logger.error("查询资产表错误:{}", e);
        }

    }

    /**
     * 判断当前登录用户是否时LDAP用户，并进行权限过滤
     */
    private void assetsTreeCheckLdapUser(Map<String, Object> queryParam, List<MWOrgDTO> orgList) {
        Object isAdmin = queryParam.get("isAdmin");
        if (isAdmin == null || Boolean.parseBoolean(isAdmin.toString()) || CollectionUtils.isEmpty(orgList)) return;
        String loginName = iLoginCacheInfo.getLoginName();
        List<Integer> orgIds = mwOrgCommonService.getAllOrgIdsByName(loginName);
        if (CollectionUtils.isEmpty(orgIds)) return;
        Reply reply = userCommonService.selectByUserId(iLoginCacheInfo.getCacheInfo(loginName).getUserId());
        if (reply == null || reply.getRes() != PaasConstant.RES_SUCCESS) return;
        MWUser user = (MWUser) reply.getData();
        if (!"AD".equals(user.getUserType())) return;
        Iterator<MWOrgDTO> iterator = orgList.iterator();
        while (iterator.hasNext()) {
            MWOrgDTO next = iterator.next();
            if (!orgIds.contains(next.getOrgId())) {
                iterator.remove();
            }
        }
    }

    /**
     * 获取资产信息的用户信息
     */
    private List getAssetsUsreNews(List mwTangAssetses) {
        if (CollectionUtils.isEmpty(mwTangAssetses)) return mwTangAssetses;
        Map<String, MwTangibleassetsDTO> assetsMap = new HashMap();
        for (Object mwTangAssets : mwTangAssetses) {
            MwTangibleassetsDTO mwTangibleassetsDTO = (MwTangibleassetsDTO) mwTangAssets;
            assetsMap.put(mwTangibleassetsDTO.getId(), mwTangibleassetsDTO);
        }
        //获取资产数据的用户机构信息
        List<cn.mw.monitor.bean.DataPermission> dataAuthByIds = mwCommonService.getDataAuthByIds(DataType.ASSETS, new ArrayList<>(assetsMap.keySet()));
        if (CollectionUtils.isEmpty(dataAuthByIds)) return mwTangAssetses;
        List realData = new ArrayList();
        try {
            for (cn.mw.monitor.bean.DataPermission dataAuthById : dataAuthByIds) {
                if (!assetsMap.containsKey(dataAuthById.getId())) continue;
                MwTangibleassetsDTO mwTangibleassetsDTO = assetsMap.get(dataAuthById.getId());
                //机构转换
                List<cn.mw.monitor.service.user.dto.OrgDTO> department = dataAuthById.getDepartment();
                List<OrgDTO> assetsOrgDTOs = CopyUtils.copyList(OrgDTO.class, department);
                mwTangibleassetsDTO.setDepartment(assetsOrgDTOs);
                mwTangibleassetsDTO.setGroup(dataAuthById.getGroups());
                mwTangibleassetsDTO.setPrincipal(dataAuthById.getPrincipal());
                realData.add(mwTangibleassetsDTO);
            }
        } catch (Throwable e) {
            log.error("获取资产机构用户组失败{}", e);
        }
        return realData;
    }

    @Override
    public void insertDeviceInfo(AddUpdateTangAssetsParam record) {
        mwTangibleAssetsDao.insertDeviceInfo(record);
    }

    @Override
    public void deleteDeviceInfo(List<String> list) {
        mwTangibleAssetsDao.deleteDeviceInfo(list);
    }

    @Override
    public void batchDeleteAssetsSnmpv12ByAssetsId(List<String> idList) {
        mwTangibleAssetsDao.batchDeleteAssetsSnmpv12ByAssetsId(idList);
    }

    @Override
    public void batchDeleteAssetsSnmpv3ByAssetsId(List<String> idList) {
        mwTangibleAssetsDao.batchDeleteAssetsSnmpv3ByAssetsId(idList);
    }

    @Override
    public void batchDeleteAssetsAgentByAssetsId(List<String> idList) {
        mwTangibleAssetsDao.batchDeleteAssetsAgentByAssetsId(idList);
    }

    @Override
    public void batchDeleteAssetsPortByAssetsId(List<String> idList) {
        mwTangibleAssetsDao.batchDeleteAssetsPortByAssetsId(idList);
    }

    @Override
    public void deleteAssetsActionMapper(List<String> assetsIds) {
        mwTangibleAssetsDao.deleteAssetsActionMapper(assetsIds);
    }

    @Override
    public List<MwTangibleassetsTable> selectAssetsListByTypeIds(List<Integer> assetsTypeIds) {
        List<MwTangibleassetsTable> list = new ArrayList<>();
        try {
            list = mwTangibleAssetsDao.selectAssetsListByTypeIds(assetsTypeIds);
        } catch (Exception e) {
            log.error("selectAssetsListByTypeIds to fail::", e);
        }
        return list;
    }

    /**
     * 修改轮询引擎
     *
     * @param pollingEngine
     */
    @Override
    public void updateAssetsPollingEngineInfo(String pollingEngine, Integer monitorServerId, String hostid) {
        if (!modelAssetEnable) {
            //修改老资产
            mwTangibleAssetsDao.updateAssetsPollingEngine(pollingEngine, monitorServerId, hostid);
        }
    }

    @Override
    public void tangibleAssetsPushConvert(List<String> ids) {
        List<MwTangibleAssetsSyncPush> list = new ArrayList<>();
        try {
            //ids为空时，表示数据同步，将同步所有的数据
            List<MwTangibleassetsTable> assetsList = mwTangibleAssetsDao.selectTangibleAssetsByIds(ids);
            //查询资产对应的负责人
            List<UserMapperDTO> userMapperDTO = mwTangibleAssetsDao.selectAssetsUserInfo();
            //查询资产对应的部门
            List<OrgMapperDTO> orgMapperDTOS = mwTangibleAssetsDao.selectAssetsOrgInfo();
            //获取墨攻部门信息映射数据
            List<IdAndNameInfoDTO> mgDeptInfo = mwTangibleAssetsDao.getMgDeptInfo();
            //获取墨攻区域信息映射数据
            List<IdAndNameInfoDTO> mgAreaInfo = mwTangibleAssetsDao.getMgAreaInfo();
            //获取墨攻设备类型映射数据
            List<IdAndNameInfoDTO> mgDeviceTypeInfo = mwTangibleAssetsDao.getMgDeviceTypeInfo();

            //墨攻部门信息转换，name为Key，id为value
            Map<String, String> mgDeptCollect = mgDeptInfo.stream().collect(Collectors.toMap(s -> s.getName(), s -> s.getId(), (
                    value1, value2) -> {
                return value2;
            }));
            //墨攻区域信息转换，name为Key，id为value
            Map<String, String> mgAreaCollect = mgAreaInfo.stream().collect(Collectors.toMap(s -> s.getName(), s -> s.getId(), (
                    value1, value2) -> {
                return value2;
            }));
            //墨攻设备类型转换，name为Key，id为value
            Map<String, String> mgDeviceTypeCollect = mgDeviceTypeInfo.stream().collect(Collectors.toMap(s -> s.getName(), s -> s.getId(), (
                    value1, value2) -> {
                return value2;
            }));

            Map<String, List<UserMapperDTO>> userMap = userMapperDTO.stream().collect(Collectors.groupingBy(s -> s.getTypeId()));
            Map<String, List<String>> orgMap = orgMapperDTOS.stream().collect(Collectors.groupingBy(s -> s.getTypeId(), Collectors.mapping(s -> s.getOrgName(), Collectors.toList())));
            //对资产数据进行转换，
            for (MwTangibleassetsTable dto : assetsList) {
                MwTangibleAssetsSyncPush syncPush = new MwTangibleAssetsSyncPush();
                syncPush.setTenantId(100);
                syncPush.setAssetName(dto.getAssetsName());
                syncPush.setAssetPrivateIp(dto.getInBandIp());
                //资产状态（监控状态，启用为在线，未启用为下线）
                syncPush.setAssetStatus(dto.getMonitorFlag().equals(true) ? 0 : 1);
                if (orgMap != null && orgMap.containsKey(dto.getId())) {
                    List<String> orgNames = orgMap.get(dto.getId());
                    StringBuffer orgSBuffIds = new StringBuffer();
                    if (CollectionUtils.isNotEmpty(orgNames)) {
                        for (String orgName : orgNames) {
                            if (mgDeptCollect != null && mgDeptCollect.containsKey(orgName)) {
                                String mgDeptId = mgDeptCollect.get(orgName);
                                orgSBuffIds.append(mgDeptId);
                                orgSBuffIds.append(",");
                            }
                        }
                        String mgDeptId = orgSBuffIds.toString();
                        if (!Strings.isNullOrEmpty(mgDeptId)) {
                            mgDeptId = mgDeptId.substring(0, mgDeptId.length() - 1);
                        }
                        syncPush.setDeptOneId(mgDeptId);
                        syncPush.setDeptId(mgDeptId);
                    }
                }
                syncPush.setNetType(2);
                //设备类型转换
                syncPush.setDeviceType(OTHER_DEVICE_TYPE);
                if (mgDeviceTypeCollect != null && mgDeviceTypeCollect.containsKey(dto.getAssetsTypeSubName())) {
                    String deviceTypeIdStr = mgDeviceTypeCollect.get(dto.getAssetsTypeSubName());
                    int deviceTypeId = intValueConvert(deviceTypeIdStr);
                    if (deviceTypeId == 0) {
                        syncPush.setDeviceType(OTHER_DEVICE_TYPE);
                    } else {
                        syncPush.setDeviceType(deviceTypeId);
                    }

                }

                if (userMap != null && userMap.containsKey(dto.getId())) {
                    List<UserMapperDTO> userMapperDTOS = userMap.get(dto.getId());
                    if (CollectionUtils.isNotEmpty(userMapperDTOS)) {
                        UserMapperDTO userDto = userMapperDTOS.get(0);
                        syncPush.setPrincipal(userDto.getUserName());
                        syncPush.setPrincipalTel(userDto.getPhoneNumber());
                    }
                }
                //
                Map<String, List<MwAssetsLabelDTO>> labelMaps = mwLabelCommonServcie.getLabelBoards(ids, DataType.ASSETS.getName());
                if (labelMaps != null && labelMaps.containsKey(dto.getId())) {
                    List<MwAssetsLabelDTO> labelDTOS = labelMaps.get(dto.getId());
                    if (CollectionUtils.isNotEmpty(labelDTOS)) {
                        for (MwAssetsLabelDTO labelDTO : labelDTOS) {
                            labelDTO.getLabelName().equals(mgAreaLabel);
                            String areaName = labelDTO.getDropValue();
                            syncPush.setAssetLocation(areaName);
                            if (mgAreaCollect != null && mgAreaCollect.containsKey(areaName)) {
                                String areaId = mgAreaCollect.get(areaName);
                                syncPush.setAssetArea(areaId);
                            }
                        }
                    }
                }
                syncPush.setDelFlag(dto.getDeleteFlag() ? 1 : 0);
                syncPush.setFirmAssetId(dto.getId());
                list.add(syncPush);
            }
            String jsonList = JSON.toJSONString(list);
            log.info("墨攻kafka消息推送数据::" + jsonList);
            //kafka推送数据
            assetsSyncKafkaProducerUtil.sendKafkaMessage(jsonList);
        } catch (Exception e) {
            log.error("墨攻kafka消息推送失败::", e);
        }
    }

}

