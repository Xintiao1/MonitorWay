package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.api.param.org.QueryOrgForDropDown;
import cn.mw.monitor.assets.dao.MWTreeCustomClassifyDao;
import cn.mw.monitor.assets.dao.MwAssetsTypeDao;
import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.assets.param.MWTreeCustomClassifyParam;
import cn.mw.monitor.assets.service.MWTreeCustomClassifyService;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MWTreeCustomClassifyServiceImpl
 * @Author gengjb
 * @Date 2021/9/9 14:17
 * @Version 1.0
 **/
@Service
@Transactional
public class MWTreeCustomClassifyServiceImpl implements MWTreeCustomClassifyService {

    private static final Logger logger = LoggerFactory.getLogger("MWTreeCustomClassifyServiceImpl");

    @Resource
    private MWTreeCustomClassifyDao treeCustomClassifyDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Resource
    private MwAssetsTypeDao mwAssetsTypeDao;

    @Autowired
    private MWOrgService mwOrgService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    /**
     * 创建数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    @Override
    public Reply createCustomClassify(MWTreeCustomClassifyParam classifyParam) {
        try {
            if(StringUtils.isBlank(classifyParam.getCustomName())){
                return Reply.fail("自定义名称不可为空");
            }
            String msg = customNameOnlyCheck(classifyParam.getCustomName(), null);
            if(StringUtils.isNotBlank(msg)){
                return Reply.fail(msg);
            }
            String loginName = iLoginCacheInfo.getLoginName();
            classifyParam.setCreator(loginName);
            classifyParam.setModifier(loginName);
            treeCustomClassifyDao.createTreeCustomClassify(classifyParam);
            return Reply.ok("数据添加成功");
        }catch (Exception e){
            logger.error("fail to createCustomClassify with id={}, cause:{}", classifyParam, e.getMessage());
            return Reply.fail("添加自定义树状结构失败");
        }
    }

    private String customNameOnlyCheck(String customName,Integer customId){
        if(StringUtils.isBlank(customName)){
            return "";
        }
        Integer customNameCount = treeCustomClassifyDao.getCustomNameCount(customName, customId);
        if(customNameCount > 0){
            return "该自定义名称已存在，请重新输入";
        }
        return "";
    }

    /**
     * 删除自定义分类数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    @Override
    public Reply deleteCustomClassify(MWTreeCustomClassifyParam classifyParam) {
        try {
            List<Integer> customIds = classifyParam.getCustomIds();
            if(!CollectionUtils.isEmpty(customIds)){
                treeCustomClassifyDao.deleteTreeCustomClassify(customIds);
            }
            return Reply.ok("数据删除成功");
        }catch (Exception e){
            logger.error("fail to deleteCustomClassify with id={}, cause:{}", classifyParam, e.getMessage());
            return Reply.fail("删除自定义树状结构失败");
        }
    }

    /**
     * 修改自定义分类数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    @Override
    public Reply updateCustomClassify(MWTreeCustomClassifyParam classifyParam) {
        try {
            if(StringUtils.isBlank(classifyParam.getCustomName())){
                return Reply.fail("自定义名称不可为空");
            }
            String msg = customNameOnlyCheck(classifyParam.getCustomName(), classifyParam.getId());
            if(StringUtils.isNotBlank(msg)){
                return Reply.fail(msg);
            }
            String loginName = iLoginCacheInfo.getLoginName();
            classifyParam.setModifier(loginName);
            treeCustomClassifyDao.updateTreeCustomClassify(classifyParam);
            return Reply.ok("数据修改成功");
        }catch (Exception e){
            logger.error("fail to updateCustomClassify with id={}, cause:{}", classifyParam, e.getMessage());
            return Reply.fail("修改自定义树状结构失败");
        }
    }

    /**
     * 查询自定义分类数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    @Override
    public Reply selectCustomClassify(MWTreeCustomClassifyParam classifyParam) {
        try {
            PageHelper.startPage(classifyParam.getPageNumber(), classifyParam.getPageSize());
            List<MWTreeCustomClassifyParam> classifyParams = treeCustomClassifyDao.selectTreeCustomClassify(classifyParam);
            return Reply.ok(classifyParams);
        }catch (Exception e){
            logger.error("fail to selectCustomClassify with id={}, cause:{}", classifyParam, e.getMessage());
            return Reply.fail("查询自定义树状结构失败");
        }
    }

    /**
     * 根据选中的自定义分类查询资产数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    @Override
    public Reply selectCustomClassifyAssets(MWTreeCustomClassifyParam classifyParam) {
        try {
            //一级分类ID
            Integer oneLevelClassifyId = classifyParam.getOneLevelClassifyId();
            //二级分类ID
            Integer twoLevelClassifyId = classifyParam.getTwoLevelClassifyId();
            //三级分类ID
            Integer threeLevelClassifyId = classifyParam.getThreeLevelClassifyId();
            Integer tableType = classifyParam.getTableType();
            List<AssetsTreeDTO> assetsData = null;
            Map<String, String> allAssetsStatus = new HashMap<>();//资产状态
            List<String> ids = new ArrayList<>();//资产ID集合
            if(oneLevelClassifyId != null){
                assetsData = getAssetsData(tableType, oneLevelClassifyId,null,allAssetsStatus,ids);//获取一级分类数据
                if(twoLevelClassifyId != null){
                    getLowerLevelClassifyData(assetsData,twoLevelClassifyId,tableType);//根据一级分类获取二级分类
                    if(threeLevelClassifyId != null){
                        for (AssetsTreeDTO assetsTreeDTO : assetsData) {//根据二级分类数据获取三级分类
                            List<AssetsTreeDTO> children = assetsTreeDTO.getChildren();
                            getLowerLevelClassifyData(children,threeLevelClassifyId,tableType);
                        }
                        //清空三级以下数据，并且删除为空的分类
                        for (AssetsTreeDTO assetsDatum : assetsData) {
                            List<AssetsTreeDTO> children = assetsDatum.getChildren();
                            if(!CollectionUtils.isEmpty(children)){
                                Iterator<AssetsTreeDTO> iterator = children.iterator();
                                while(iterator.hasNext()){
                                    AssetsTreeDTO child = iterator.next();
                                    List<AssetsTreeDTO> children1 = child.getChildren();
                                    if(CollectionUtils.isEmpty(child.getAssetsList())){
                                        iterator.remove();
                                    }else{
                                        child.setCount(child.getAssetsList().size());
                                    }
                                    if(!CollectionUtils.isEmpty(children1)){
                                        Iterator<AssetsTreeDTO> iterator1 = children1.iterator();
                                        while(iterator1.hasNext()){
                                            AssetsTreeDTO child1 = iterator1.next();
                                            child1.setChildren(new ArrayList<>());
                                            if(CollectionUtils.isEmpty(child1.getAssetsList())){
                                                iterator1.remove();
                                            }else{
                                                child1.setCount(child1.getAssetsList().size());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            treeAssetsStstusClassIfy(assetsData,allAssetsStatus,ids);
            return Reply.ok(assetsData);
        }catch (Exception e){
            logger.error("fail to selectCustomClassify with id={}, cause:{}", classifyParam, e.getMessage());
            return Reply.fail("查询资产数据失败");
        }
    }

    private  List<AssetsTreeDTO> getAssetsData(Integer tableType,Integer assetsType,List<String> assets,Map<String, String> status, List<String> asIds){
        String moduleType = "";
        String tableName = "";
        String assetsSubTypeId = "";
        int settingEnable = 0;
        switch (tableType) {
            case 1:
                moduleType = DataType.ASSETS.getName();
                tableName = "mw_tangibleassets_table";
                assetsSubTypeId = "assets_type_sub_id";
                break;
            case 2:
                moduleType = DataType.INASSETS.getName();
                tableName = "mw_intangibleassets_table";
                assetsSubTypeId = "sub_assets_type_id";
                if (assetsType == 1) {
                    assetsType = 2;
                }
                break;
            case 3:
                moduleType = DataType.OUTBANDASSETS.getName();
                tableName = "mw_outbandassets_table";
                assetsSubTypeId = "assets_type_sub_id";
                break;
            default:
                break;
        }
        AssetsTreeDTO unknown = new AssetsTreeDTO();
        List<AssetsTreeDTO> treeDTOS = new ArrayList<>();
        switch (assetsType){
            case 1://品牌（加品牌图标）
                treeDTOS = treeCustomClassifyDao.selectAssetsVendorList(tableName, settingEnable,assets);
                break;
            case 2://资产类型（资产类型图标待加）
                treeDTOS = treeCustomClassifyDao.selectAssetsTypeList(moduleType, assetsSubTypeId, tableName, settingEnable,assets);
                break;
            case 3://标签
                List<AssetsDTO> ids = new ArrayList<>();
                List<AssetsTreeDTO> dtos = treeCustomClassifyDao.selectAssetsLabelList(moduleType, tableName, settingEnable,assets);
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
                ids = ids.stream().distinct().collect(Collectors.toList());
                List<AssetsDTO> assetsDTOS = treeCustomClassifyDao.selectAllAssets(moduleType, tableName,settingEnable,assets);
                if (ids.size() > 0 && assetsDTOS != null && assetsDTOS.size() > 0) {
                    assetsDTOS.removeAll(ids);
                }
                if (CollectionUtils.isNotEmpty(assetsDTOS)) {
                    unknown.setTypeName("未知");
                    unknown.setTypeId(0);
                    unknown.setAssetsList(assetsDTOS);
                }
                break;
            case 6://机构
                List<AssetsDTO> orgIds = new ArrayList<>();
                Reply reply = mwOrgService.selectDorpdownList(new QueryOrgForDropDown());
                if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                    List<MWOrgDTO> orgList = (List<MWOrgDTO>) reply.getData();
                    if (orgList != null && orgList.size() > 0) {
                        for (MWOrgDTO org : orgList) {
                            AssetsTreeDTO assetsTreeDTO = new AssetsTreeDTO();
                            assetsTreeDTO.setTypeName(org.getOrgName());
                            assetsTreeDTO.setTypeId(org.getOrgId());
                            List<AssetsDTO> assetsDTOList = treeCustomClassifyDao.selectAssetsOrgList(moduleType, org.getOrgId(), tableName, settingEnable,assets);
                            if (CollectionUtils.isNotEmpty(assetsDTOList)) {
                                Iterator iterator = assetsDTOList.iterator();
                                while (iterator.hasNext()) {
                                    AssetsDTO assetsDTO = (AssetsDTO) iterator.next();
                                    if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsId())) {
                                        orgIds.add(assetsDTO);
                                    } else {
                                        iterator.remove();
                                    }
                                }
                            }
                            if (CollectionUtils.isNotEmpty(assetsDTOList)){
                                if (org.getChilds() != null && org.getChilds().size() > 0) {
                                    List<MWOrgDTO> childs = org.getChilds();
                                    for (MWOrgDTO child : childs) {
                                        AssetsTreeDTO assetsTreeChild = new AssetsTreeDTO();
                                        assetsTreeChild.setTypeName(child.getOrgName());
                                        assetsTreeChild.setTypeId(child.getOrgId());
                                        List<AssetsDTO> childAssetsDTOList = treeCustomClassifyDao.selectAssetsOrgList(moduleType, org.getOrgId(), tableName, settingEnable,assets);
                                        if (CollectionUtils.isNotEmpty(childAssetsDTOList)) {
                                            Iterator iterator = childAssetsDTOList.iterator();
                                            while (iterator.hasNext()) {
                                                AssetsDTO assetsDTO = (AssetsDTO) iterator.next();
                                                if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsId())) {
                                                    orgIds.add(assetsDTO);
                                                } else {
                                                    iterator.remove();
                                                }
                                            }
                                        }
                                        if (CollectionUtils.isNotEmpty(childAssetsDTOList)){
                                            assetsTreeChild.setAssetsList(childAssetsDTOList);
                                            assetsTreeDTO.addChild(assetsTreeChild);
                                        }
                                    }
                                }
                                assetsTreeDTO.setAssetsList(assetsDTOList);
                                treeDTOS.add(assetsTreeDTO);
                            }
                        }
                    }
                }
                orgIds = orgIds.stream().distinct().collect(Collectors.toList());
//                    AssetsTreeDTO orgUnknown = new AssetsTreeDTO();
                List<AssetsDTO> orgAssetsDTOS = treeCustomClassifyDao.selectAllAssets(moduleType, tableName,settingEnable,assets);
                if (orgIds.size() > 0 && orgAssetsDTOS != null && orgAssetsDTOS.size() > 0) {
                    orgAssetsDTOS.removeAll(orgIds);
                }
                if (CollectionUtils.isNotEmpty(orgAssetsDTOS)) {
                    unknown.setTypeName("未知");
                    unknown.setTypeId(0);
                    unknown.setAssetsList(orgAssetsDTOS);
                }
                break;
            default:
                break;
        }
        Map<Integer,Set<String>> hostIdMap = new HashMap<>();
        List<String> assetsIds = new ArrayList<>();
        getAssetsHostId(treeDTOS,hostIdMap,assetsIds);
        //查询资产监控状态
        List<String> ids = mwAssetsTypeDao.selectAssetsMonitorStatus(assetsIds);
        asIds.addAll(ids);
        //获取资产所有状态
        Map<String, String> allAssetsStatus = getAllAssetsStatus(hostIdMap);
        status.putAll(allAssetsStatus);
        treeDTOS = treeList(treeDTOS, allAssetsStatus,ids);
        treeDTOS = getChildrenList(treeDTOS);
        if("未知".equals(unknown.getTypeName())) {
            treeDTOS.add(unknown);
        }
        //将未知放在最前面
        AssetsTreeDTO handleUnknown = null;
        if(!CollectionUtils.isEmpty(treeDTOS)){
            Iterator<AssetsTreeDTO> iterator = treeDTOS.iterator();
            while(iterator.hasNext()){
                AssetsTreeDTO assetsTreeDTO = iterator.next();
                if("未知".equals(assetsTreeDTO.getTypeName())){
                    handleUnknown = assetsTreeDTO;
                    iterator.remove();
                }
            }
            if(handleUnknown != null){
                treeDTOS.add(0,handleUnknown);
            }
        }
        return treeDTOS;
    }


    private void getAssetsHostId( List<AssetsTreeDTO> treeDTOS,Map<Integer,Set<String>> hostIdMap,List<String> assetsIds){
        if(!CollectionUtils.isEmpty(treeDTOS)){
            for (AssetsTreeDTO treeDTO : treeDTOS) {
                List<AssetsDTO> assetsList = treeDTO.getAssetsList();
                if(CollectionUtils.isEmpty(assetsList)){
                    continue;
                }
                for (AssetsDTO assetsDTO : assetsList) {
                    assetsIds.add(assetsDTO.getId());
                    String assetsId = assetsDTO.getAssetsId();
                    Integer monitorServerId = assetsDTO.getMonitorServerId();
                    if(StringUtils.isBlank(assetsId) || monitorServerId == null){
                        continue;
                    }
                    if(hostIdMap.get(monitorServerId) == null){
                        Set<String> hostIds = new HashSet<>();
                        hostIds.add(assetsId);
                        hostIdMap.put(monitorServerId,hostIds);
                        continue;
                    }
                    if(hostIdMap.get(monitorServerId) != null){
                        Set<String> hostIds = hostIdMap.get(monitorServerId);
                        hostIds.add(assetsId);
                        hostIdMap.put(monitorServerId,hostIds);
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
    public Map<String, String> getAllAssetsStatus(Map<Integer,Set<String>> hostIdMap) {
        Map<String, String> statusMap = new HashMap<>();
        if(hostIdMap.isEmpty()){
            return statusMap;
        }
        Set<String> hostSets = new HashSet<>();
        for (Map.Entry<Integer, Set<String>> entry : hostIdMap.entrySet()) {
            Integer key = entry.getKey();
            Set<String> value = entry.getValue();
            //有改动-zabbi
            MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(key, ZabbixItemConstant.NEW_ASSETS_STATUS, value);
            if (!statusData.isFail()) {
                JsonNode jsonNode = (JsonNode) statusData.getData();
                if (jsonNode.size() > 0) {
                    for (JsonNode node : jsonNode) {
                        Integer lastvalue = node.get("lastvalue").asInt();
                        String hostId = node.get("hostid").asText();
                        String name = node.get("name").asText();
                        if((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)){
                            String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                            statusMap.put(key + ":" + hostId, status);
                            hostSets.add(hostId);
                        }
                        if(hostSets.contains(hostId)){
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
     * 递归实现状态赋值
     *
     * @param list
     * @return
     */
    public List<AssetsTreeDTO> treeList(List<AssetsTreeDTO> list, Map<String, String> statusMap,List<String> ids) {
        if (list != null && list.size() > 0) {
            list.forEach(tree -> {
                if("unknown".equals(tree.getTypeName())){
                    tree.setTypeName("未知");
                }
                tree.setStatusUrl(getOverAllStatus(tree.getAssetsList(), statusMap,ids));
                List<AssetsTreeDTO> children = tree.getChildren();
                tree.setChildren(treeList(children, statusMap,ids));
            });
        }
        return list;
    }

    /**
     * 查询资产的整体状态，当资产中有异常的展示黄色感叹号
     *
     * @param assetsList
     * @param statusMap
     * @return
     */
    public String getOverAllStatus(List<AssetsDTO> assetsList, Map<String, String> statusMap,List<String> ids) {
        int unknownCount = 0;
        int abnormalCount = 0;
        int normalCount = 0;
        for (AssetsDTO asset : assetsList) {
            if(ids.contains(asset.getId())){
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

    //获取子节点集合
    private List<AssetsTreeDTO> getChildrenList(List<AssetsTreeDTO> child) {
        if (child != null && child.size() > 0) {
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            return child.stream().peek((c) -> c.setChildren(getChildrenList(c.getChildren())))
                    .sorted((o1,o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getTypeName()), pinyin4jUtil.getStringPinYin(o2.getTypeName())))
                    .collect(Collectors.toList());
        }
        return child;
    }

    /**
     * 获取下级分类数据
     * @param assetsData
     */
    private void getLowerLevelClassifyData( List<AssetsTreeDTO> assetsData,Integer ClassifyId,Integer tableType){
        if(CollectionUtils.isEmpty(assetsData)){
            return;
        }
        List<AssetsTreeDTO> dtoss = new ArrayList<>();
        Set<String> assetsIds = new HashSet<>();
        for (AssetsTreeDTO assetsTreeDTO : assetsData) {
            List<AssetsDTO> assetsList = assetsTreeDTO.getAssetsList();
            if(!CollectionUtils.isEmpty(assetsList)){
                for (AssetsDTO assetsDTO : assetsList) {
                    assetsIds.add(assetsDTO.getId());
                }
            }
//            if(CollectionUtils.isEmpty(assetsIds)){
//                return;
//            }
//            List<String> ids = new ArrayList<>();
//            ids.addAll(assetsIds);
//            List<AssetsTreeDTO> assetsTreeDTOS = getAssetsData(tableType, ClassifyId, ids);
//            List<AssetsTreeDTO> children = assetsTreeDTO.getChildren();
//            if(children == null){
//                assetsTreeDTO.setChildren(assetsTreeDTOS);
//            }else{
//                children.addAll(assetsTreeDTOS);
//            }
        }
        List<String> ids = new ArrayList<>();
        ids.addAll(assetsIds);
        List<AssetsTreeDTO> assetsTreeDTOS = getAssetsData(tableType, ClassifyId, ids,new HashMap<>(),new ArrayList<>());
        for (AssetsTreeDTO assetsTreeDTO : assetsData){
            if(!CollectionUtils.isEmpty(assetsTreeDTOS)){
                for (AssetsTreeDTO treeDTO : assetsTreeDTOS) {
                    treeDTO.setChildren(null);
                }
            }
            List<AssetsTreeDTO> dtos = new ArrayList<>();
            dtos.addAll(assetsTreeDTOS);
            List<AssetsDTO> assetsList = assetsTreeDTO.getAssetsList();
            List<String> asIds = new ArrayList<>();
            for (AssetsDTO assetsDTO : assetsList) {
                asIds.add(assetsDTO.getId());
            }
            Set<String> s = new HashSet<>();
            for (AssetsTreeDTO treeDTO : assetsTreeDTOS) {
                List<AssetsDTO> assetsList1 = treeDTO.getAssetsList();
                for (AssetsDTO assetsDTO : assetsList1) {
                    if(!asIds.contains(assetsDTO.getId())){
                        s.add(assetsDTO.getId());
                    }
                }
            }
            List<AssetsTreeDTO> children = assetsTreeDTO.getChildren();
            List<AssetsTreeDTO> assetsTreeDTOList = new ArrayList<>();
            delAssetsId(dtos, s, assetsTreeDTOList);
            if(children == null){
                assetsTreeDTO.setChildren(assetsTreeDTOList);
            }else{
                children.addAll(assetsTreeDTOList);
            }
        }
    }



    private void delAssetsId(List<AssetsTreeDTO> assetsTreeDTOS,Set<String> assetIds, List<AssetsTreeDTO> assetsTreeDTOList){
        if(!CollectionUtils.isEmpty(assetsTreeDTOS) && !CollectionUtils.isEmpty(assetIds)){
            for (AssetsTreeDTO assetsTreeDTO : assetsTreeDTOS) {
                AssetsTreeDTO dto = new AssetsTreeDTO();
                BeanUtils.copyProperties(assetsTreeDTO,dto);
                List<AssetsDTO> assetsList = assetsTreeDTO.getAssetsList();
                List<AssetsDTO> assets = new ArrayList<>();
                for (AssetsDTO next : assetsList) {
                    if(!assetIds.contains(next.getId())){
                        AssetsDTO assetsDTO = new AssetsDTO();
                        BeanUtils.copyProperties(next,assetsDTO);
                        assets.add(assetsDTO);
                    }
                }
                if(!CollectionUtils.isEmpty(assets)){
                    dto.setAssetsList(assets);
                    assetsTreeDTOList.add(dto);
                }
//                assetsTreeDTO.setCount(assetsTreeDTO.getCount()-count);
//                delAssetsId(assetsTreeDTO.getChildren(),assetIds,assetsTreeDTOList);
            }
        }
    }

    private void treeAssetsStstusClassIfy(List<AssetsTreeDTO> treeDTOS, Map<String, String> allAssetsStatus,List<String> ids){
        if(!CollectionUtils.isEmpty(treeDTOS)){
            for (AssetsTreeDTO treeDTO : treeDTOS) {
                List<AssetsTreeDTO> children = treeDTO.getChildren();
                Map<String,List<AssetsDTO>> map = new HashMap<>();
                if(CollectionUtils.isEmpty(children)){
                    List<AssetsDTO> assetsList = treeDTO.getAssetsList();
                    if(!CollectionUtils.isEmpty(assetsList)){
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
                            if(!CollectionUtils.isEmpty(map.get(status))){
                                List<AssetsDTO> assetsDTOS = map.get(status);
                                assetsDTOS.add(assetsDTO);
                                map.put(status,assetsDTOS);
                                continue;
                            }
                            if(CollectionUtils.isEmpty(map.get(status))){
                                List<AssetsDTO> assetsDTOS = new ArrayList<>();
                                assetsDTOS.add(assetsDTO);
                                map.put(status,assetsDTOS);
                                continue;
                            }
                        }
                    }
                    List<AssetsTreeDTO> treeDTOList = new ArrayList<>();
                    if(!map.isEmpty()){
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
                treeAssetsStstusClassIfy(children,allAssetsStatus,ids);
            }
        }
    }
}
