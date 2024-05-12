package cn.mw.monitor.screen.service.impl;

import cn.mw.monitor.api.param.org.QueryOrgForDropDown;
import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.screen.dao.MWLagerScreenDao;
import cn.mw.monitor.screen.dao.MWLagerScreenMapDao;
import cn.mw.monitor.screen.dto.CoordinateAddress;
import cn.mw.monitor.screen.dto.LargeScreenAssetsInterfaceDto;
import cn.mw.monitor.screen.dto.LargeScreenMapDto;
import cn.mw.monitor.screen.service.MWLagerScreenService;
import cn.mw.monitor.screen.service.MWLargeScreenMapService;
import cn.mw.monitor.service.alert.dto.DiscDto;
import cn.mw.monitor.service.alert.dto.ItemData;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.DiskTypeDto;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MWLagerScreenMapServiceImpl
 * @Author gengjb
 * @Date 2022/9/2 9:15
 * @Version 1.0
 **/
@Service
@Slf4j
public class MWLargeScreenMapServiceImpl implements MWLargeScreenMapService {

    @Autowired
    private MWLagerScreenMapDao screenMapDao;

    @Autowired
    private ILoginCacheInfo loginCacheInfo;

    @Autowired
    private MwServerService serverService;

    @Autowired
    private MWLagerScreenService lagerScreenService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    /**
     * 获取大屏地图选择数据
     * @return
     */
    @Override
    public Reply getScreenMapChoiceInformation() {
        try {
            Map<String,Object> queryParam = new HashMap<>();
            queryParam.put("moduleType",DataType.ASSETS.getName());
            queryParam.put("tableName","mw_tangibleassets_table");
            queryParam.put("assetsSubTypeId","assets_type_sub_id");
            List<LargeScreenAssetsInterfaceDto> largeScreenAssetsInterfaceDtos = screenMapDao.selectAseetsAndInterface();
            Map<String, LargeScreenAssetsInterfaceDto> assetsMap = getAssets(largeScreenAssetsInterfaceDtos);
            getUserPerm(queryParam);
            List<AssetsTreeDTO> vendorList = screenMapDao.selectAssetsVendorList(queryParam);//品牌
            List<LargeScreenAssetsInterfaceDto> pinpaiList = handleTreeData(vendorList, assetsMap);
            List<AssetsTreeDTO> typeList = screenMapDao.selectAssetsTypeList(queryParam);//资产类型
            List<LargeScreenAssetsInterfaceDto> leixingList = handleTreeData(typeList, assetsMap);
            List<AssetsTreeDTO> labelList = getLabelAssetsData(queryParam);//标签
            List<LargeScreenAssetsInterfaceDto> biaoqianList = handleTreeData(labelList, assetsMap);
            List<AssetsTreeDTO> orgAssetsData = getOrgAssetsData(queryParam);//机构数据
            List<LargeScreenAssetsInterfaceDto> jigouList = handleTreeData(orgAssetsData, assetsMap);
            LargeScreenAssetsInterfaceDto treeDTO1 = new LargeScreenAssetsInterfaceDto();
            treeDTO1.setName("品牌");
            treeDTO1.setChildren(pinpaiList);
            treeDTO1.setDisabled(true);
            Map<String,Object> map1 = new HashMap<>();
            map1.put("label",treeDTO1.getName());
            treeDTO1.setMap(map1);
            LargeScreenAssetsInterfaceDto treeDTO2 = new LargeScreenAssetsInterfaceDto();
            treeDTO2.setName("资产类型");
            treeDTO2.setChildren(leixingList);
            treeDTO2.setDisabled(true);
            Map<String,Object> map2 = new HashMap<>();
            map2.put("label",treeDTO2.getName());
            treeDTO2.setMap(map2);
            LargeScreenAssetsInterfaceDto treeDTO3 = new LargeScreenAssetsInterfaceDto();
            treeDTO3.setName("标签");
            treeDTO3.setChildren(biaoqianList);
            treeDTO3.setDisabled(true);
            Map<String,Object> map3 = new HashMap<>();
            map3.put("label",treeDTO3.getName());
            treeDTO3.setMap(map3);
            LargeScreenAssetsInterfaceDto treeDTO4 = new LargeScreenAssetsInterfaceDto();
            treeDTO4.setName("机构");
            treeDTO4.setChildren(jigouList);
            treeDTO4.setDisabled(true);
            Map<String,Object> map4 = new HashMap<>();
            map4.put("label",treeDTO4.getName());
            treeDTO4.setMap(map4);
            //处理数据
            List<LargeScreenAssetsInterfaceDto> realData = new ArrayList<>();
            realData.add(treeDTO1);
            realData.add(treeDTO2);
            realData.add(treeDTO3);
            realData.add(treeDTO4);
            return Reply.ok(realData);
        }catch (Throwable e){
            log.error("查询地图选择信息失败"+e.getMessage());
            return Reply.fail("查询地图选择信息失败"+e.getMessage());
        }
    }


    private Map<String,LargeScreenAssetsInterfaceDto> getAssets(List<LargeScreenAssetsInterfaceDto> largeScreenAssetsInterfaceDtos){
        Map<String,LargeScreenAssetsInterfaceDto> assetsInterfaceDtoMap = new HashMap<>();
        for (LargeScreenAssetsInterfaceDto largeScreenAssetsInterfaceDto : largeScreenAssetsInterfaceDtos) {
            assetsInterfaceDtoMap.put(largeScreenAssetsInterfaceDto.getId(),largeScreenAssetsInterfaceDto);
        }
        return assetsInterfaceDtoMap;
    }

    private List<LargeScreenAssetsInterfaceDto> handleTreeData(List<AssetsTreeDTO> treeDTOS,Map<String, LargeScreenAssetsInterfaceDto> assetsMap){
        List<LargeScreenAssetsInterfaceDto> largeScreenAssetsInterfaceDtos = new ArrayList<>();
        for (AssetsTreeDTO treeDTO : treeDTOS) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            LargeScreenAssetsInterfaceDto screenAssetsInterfaceDto = new LargeScreenAssetsInterfaceDto();
            screenAssetsInterfaceDto.setName(treeDTO.getTypeName());
            screenAssetsInterfaceDto.setDisabled(true);
            List<AssetsDTO> assetsList = treeDTO.getAssetsList();
            List<LargeScreenAssetsInterfaceDto> children = new ArrayList<>();
            for (AssetsDTO assetsDTO : assetsList) {
                String id = assetsDTO.getId();
                LargeScreenAssetsInterfaceDto dto = assetsMap.get(id);
                screenAssetsInterfaceDto.setDisabled(true);
                Map<String,Object> m = new HashMap<>();
                m.put("tag",dto.getAssetsId());
                m.put("name",dto.getName());
                m.put("assetsId",dto.getAssetsId());
                m.put("assetsIp",dto.getAssetsIp());
                m.put("assetsServerId",dto.getAssetsServerId());
                m.put("id",dto.getId());
                m.put("type",1);
                dto.setMap(m);
                dto.setDisabled(false);
                List<String> interFaceNames = dto.getInterFaceNames();
                if(CollectionUtils.isEmpty(interFaceNames))continue;
                List<LargeScreenAssetsInterfaceDto> children2 = new ArrayList<>();
                for (String interFaceName : interFaceNames) {
                    LargeScreenAssetsInterfaceDto interfaceDto = new LargeScreenAssetsInterfaceDto();
                    interfaceDto.setName(interFaceName);
                    Map<String,Object> m2 = new HashMap<>();
                    m2.put("tag",dto.getAssetsId());
                    m2.put("name",interFaceName);
                    m2.put("type",2);
                    m2.put("assetsId",dto.getAssetsId());
                    m2.put("assetsIp",dto.getAssetsIp());
                    m2.put("assetsServerId",dto.getAssetsServerId());
                    m2.put("id",dto.getId());
                    interfaceDto.setMap(m2);
                    interfaceDto.setDisabled(false);
                    children2.add(interfaceDto);
                }
                dto.setChildren(children2);
                children.add(dto);
            }
            screenAssetsInterfaceDto.setChildren(children);
            Map<String,Object> typeMap = new HashMap<>();
            typeMap.put("label",treeDTO.getTypeName());
            screenAssetsInterfaceDto.setMap(typeMap);
            largeScreenAssetsInterfaceDtos.add(screenAssetsInterfaceDto);
        }
        return largeScreenAssetsInterfaceDtos;
    }

    /**
     * 创建地图展示信息
     * @param screenMapDto
     * @return
     */
    @Override
    public Reply createScreenMapShowInformation(LargeScreenMapDto screenMapDto) {
       try {
           //获取当前登录用户ID
           Integer userId = loginCacheInfo.getCacheInfo(loginCacheInfo.getLoginName()).getUserId();
           List<Object> datas = screenMapDto.getDatas();
           if(CollectionUtils.isEmpty(datas)) return Reply.fail("数据不可为空");
           Map<String,LargeScreenMapDto> dtoMap = new HashMap<>();
           //接下数据
           for (Object data : datas) {
               List<Object> list = (List<Object>) data;
               if(CollectionUtils.isEmpty(list))continue;
               for (Object o : list) {
                   if(o == null)continue;
                   Map<String,Object> map = (Map<String, Object>) o;
                   Object tag = map.get("tag");
                   Object type = map.get("type");
                   Object name = map.get("name");
                   Object assetsServerId = map.get("assetsServerId");
                   if(tag != null && dtoMap.containsKey(tag.toString())){
                       LargeScreenMapDto largeScreenMapDto = dtoMap.get(tag.toString());
                       if(type != null && Integer.parseInt(type.toString()) == 2 && name != null){
                           String interfaceName = largeScreenMapDto.getInterfaceName();
                           if(StringUtils.isNotBlank(interfaceName)){
                               largeScreenMapDto.setInterfaceName(interfaceName+","+name.toString());
                           }else{
                               largeScreenMapDto.setInterfaceName(name.toString());
                           }
                       }
                       if(type != null && Integer.parseInt(type.toString()) == 1 && name != null){
                           largeScreenMapDto.setAssetsName(map.get("name").toString());
                       }
                       largeScreenMapDto.setMonitorServerId(Integer.parseInt(assetsServerId.toString()));
                       largeScreenMapDto.setDatas(screenMapDto.getDatas());
                       dtoMap.put(tag.toString(),largeScreenMapDto);
                       continue;
                   }
                   if(tag != null && !dtoMap.containsKey(tag.toString())){
                       LargeScreenMapDto largeScreenMapDto = new LargeScreenMapDto();
                       if(type != null && Integer.parseInt(type.toString()) == 2 && name != null){
                           String interfaceName = largeScreenMapDto.getInterfaceName();
                           if(StringUtils.isNotBlank(interfaceName)){
                               largeScreenMapDto.setInterfaceName(interfaceName+","+name.toString());
                           }else{
                               largeScreenMapDto.setInterfaceName(name.toString());
                           }
                       }
                       if(type != null && Integer.parseInt(type.toString()) == 1 && name != null){
                           largeScreenMapDto.setAssetsName(map.get("name").toString());
                       }
                       largeScreenMapDto.setAssetsId(map.get("assetsId").toString());
                       largeScreenMapDto.setAssetsIp(map.get("assetsIp").toString());
                       largeScreenMapDto.setShowInformation(screenMapDto.getShowInformation());
                       largeScreenMapDto.setMonitorServerId(Integer.parseInt(assetsServerId.toString()));
                       largeScreenMapDto.setUserId(userId);
                       largeScreenMapDto.setOrgId(screenMapDto.getOrgId());
                       largeScreenMapDto.setDatas(screenMapDto.getDatas());
                       dtoMap.put(tag.toString(),largeScreenMapDto);
                   }
               }
           }
           if(dtoMap == null || dtoMap.isEmpty())return Reply.fail("参数不正确");
           for (Map.Entry<String, LargeScreenMapDto> entry : dtoMap.entrySet()) {
               LargeScreenMapDto mapDto = entry.getValue();
               //数据转为json字符串存储
               mapDto.setShowData(JSONObject.toJSONString(mapDto.getDatas()));
               screenMapDao.insertScreenMapShowInformation(mapDto);
           }
           return Reply.ok("新增成功");
       }catch (Throwable e){
           log.error("创建地图展示信息失败"+e.getMessage());
           return Reply.fail("创建地图展示信息失败"+e.getMessage());
       }
    }

    @Override
    public Reply selectScreenMapShowInformation(LargeScreenMapDto screenMapDto) {
        try {
            List<LargeScreenMapDto> largeScreenMapDtos = new ArrayList<>();
            //获取当前登录用户ID
            Integer userId = loginCacheInfo.getCacheInfo(loginCacheInfo.getLoginName()).getUserId();
            if(userId == null)return Reply.ok(largeScreenMapDtos);
            //查询数据库数据信息
            Integer id = screenMapDto.getOrgId();
            largeScreenMapDtos = screenMapDao.selectScreenMapShowInformation(userId,id);
            if(CollectionUtils.isEmpty(largeScreenMapDtos))Reply.ok(largeScreenMapDtos);
            //获取告警数据
            Map<Integer, Integer> alertcount = getAlertcount();
            //根据数据库接口名称和展示信息获取数据
            for (LargeScreenMapDto largeScreenMapDto : largeScreenMapDtos) {
                Map<String,Object> dataMap = new LinkedHashMap<>();
                String assetsId = largeScreenMapDto.getAssetsId();
                Integer monitorServerId = largeScreenMapDto.getMonitorServerId();
                String interfaceName = largeScreenMapDto.getInterfaceName();
                Integer orgId = largeScreenMapDto.getOrgId();
                String showInformation = largeScreenMapDto.getShowInformation();
                dataMap.put("资产名称",largeScreenMapDto.getAssetsName());
                dataMap.put("IP地址",largeScreenMapDto.getAssetsIp());
                if(StringUtils.isNotBlank(interfaceName)){
                    List<String> names = Arrays.asList(interfaceName.split(","));
                    if(StringUtils.isNotBlank(showInformation) && showInformation.contains("INTERFACE")){
                        dataMap.putAll(getInterfaceNews(names, assetsId, monitorServerId,showInformation));
                    }
                }
                //查询zabbix中数据
                getZabbixItemValue(assetsId,monitorServerId,showInformation,dataMap);
                //判断是否需要告警
                if(StringUtils.isNotBlank(showInformation) && showInformation.contains("告警次数")){
                    Integer count = alertcount.get(orgId);
                    if(count != null){
                        dataMap.put("告警次数",count);
                    }else{
                        dataMap.put("告警次数",0);
                    }
                }
                largeScreenMapDto.setDataMap(dataMap);
                //数据转换
                String showData = largeScreenMapDto.getShowData();
                List list = JSONObject.parseObject(showData, List.class);
                largeScreenMapDto.setDatas(list);
            }
            Map<String,List<LargeScreenMapDto>> realDataMap = new HashMap<>();
            if(CollectionUtils.isNotEmpty(largeScreenMapDtos)){
                for (LargeScreenMapDto largeScreenMapDto : largeScreenMapDtos) {
                    if(realDataMap  != null && realDataMap.containsKey(largeScreenMapDto.getOrgId().toString())){
                        List<LargeScreenMapDto> screenMapDtos = realDataMap.get(largeScreenMapDto.getOrgId().toString());
                        screenMapDtos.add(largeScreenMapDto);
                        realDataMap.put(largeScreenMapDto.getOrgId().toString(),screenMapDtos);
                        continue;
                    }
                    if(realDataMap  == null || !realDataMap.containsKey(largeScreenMapDto.getOrgId().toString())){
                        List<LargeScreenMapDto> screenMapDtos = new ArrayList<>();
                        screenMapDtos.add(largeScreenMapDto);
                        realDataMap.put(largeScreenMapDto.getOrgId().toString(),screenMapDtos);
                    }
                }
            }
            return Reply.ok(realDataMap);
        }catch (Throwable e){
            log.error("查询地图展示信息失败"+e.getMessage());
            return Reply.fail("查询地图展示信息失败"+e.getMessage());
        }
    }



    private void getZabbixItemValue(String assetsId,Integer monitorServerId,String showInformation,Map<String,Object> dataMap){
        List<String> list = Arrays.asList(showInformation.split(","));
        List<String> showNames = new ArrayList<>();
        Iterator<String> iterator = list.iterator();
        while(iterator.hasNext()){
            String showName = iterator.next();
            if(showName.equals("告警次数") || showName.contains("INTERFACE"))continue;
            showNames.add(showName);
        }
        if(CollectionUtils.isEmpty(showNames))return;
        //查询所有监控项
        List<Map<String, String>> itemNameByName = screenMapDao.getItemNameByName(showNames);
        Map<String,String> itemMap = new HashMap<>();
        for (Map<String, String> stringStringMap : itemNameByName) {
            String itemName = stringStringMap.get("itemName");
            String descr = stringStringMap.get("descr");
            itemMap.put(itemName,descr);
        }
        //查询监控下信息
        MWZabbixAPIResult zabbixAPIResult = mwtpServerAPI.itemGetbySearch(monitorServerId, showNames, assetsId);
        Map<String,List<Double>> zabbixValueMap = new HashMap<>();
        if (!zabbixAPIResult.isFail()){
            JsonNode jsonNode = (JsonNode) zabbixAPIResult.getData();
            if (jsonNode != null && jsonNode.size() > 0){
                for (JsonNode node : jsonNode){
                    Double lastvalue = node.get("lastvalue").asDouble();
                    String units = node.get("units").asText();
                    String name = node.get("name").asText();
                    if(name.contains("(AVG 1M)CPU_UTILIZATION"))continue;
                    for (String key : itemMap.keySet()) {
                        if(zabbixValueMap.containsKey(key+","+units) && name.contains(key)){
                            List<Double> doubles = zabbixValueMap.get(key+","+units);
                            doubles.add(lastvalue);
                            zabbixValueMap.put(key+","+units,doubles);
                            continue;
                        }
                        if(!zabbixValueMap.containsKey(key+","+units) && name.contains(key)){
                            List<Double> doubles = new ArrayList<>();
                            doubles.add(lastvalue);
                            zabbixValueMap.put(key+","+units,doubles);
                        }
                    }
                }
            }
        }
        //处理数据
        if(zabbixValueMap == null || zabbixValueMap.isEmpty())return;
        for (String showNameUnits : zabbixValueMap.keySet()) {
            String[] nameAndUnits = showNameUnits.split(",");
            List<Double> values = zabbixValueMap.get(showNameUnits);
            double sum = 0;
            for (Double value : values) {
                sum += value;
            }
            for (String key : itemMap.keySet()){
                String showName = itemMap.get(key);
                if(nameAndUnits[0].contains(key)){
                    if(nameAndUnits.length > 1){
                        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(sum / values.size()), nameAndUnits[1]);
                        if(convertedValue != null && !convertedValue.isEmpty()){
                            dataMap.put(showName,convertedValue.get("value")+convertedValue.get("units"));
                        }else{
                            dataMap.put(showName,new BigDecimal(sum/values.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+nameAndUnits[1]);
                        }
                    }else{
                        dataMap.put(showName,new BigDecimal(sum/values.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue());
                    }
                }
            }
        }
    }

    /**
     * 修改地图展示信息
     * @param screenMapDto
     * @return
     */
    @Override
    public Reply updateScreenMapShowInformation(LargeScreenMapDto screenMapDto) {
        try {
            //获取当前登录用户ID
            Integer userId = loginCacheInfo.getCacheInfo(loginCacheInfo.getLoginName()).getUserId();
            screenMapDao.deleteScreenMapShowInformation(screenMapDto.getOrgId(),userId);
            screenMapDto.setUserId(userId);
            createScreenMapShowInformation(screenMapDto);
            return Reply.ok("修改成功");
        }catch (Throwable e){
            log.error("创建地图展示信息失败"+e.getMessage());
            return Reply.fail("创建地图展示信息失败"+e.getMessage());
        }
    }

    /**
     * 删除地图展示信息
     * @param screenMapDto
     * @return
     */
    @Override
    public Reply deleteScreenMapShowInformation(LargeScreenMapDto screenMapDto) {
        try {
            //获取当前登录用户ID,
            Integer userId = loginCacheInfo.getCacheInfo(loginCacheInfo.getLoginName()).getUserId();
            screenMapDao.deleteScreenMapShowInformation(screenMapDto.getOrgId(),userId);
            return Reply.ok("删除成功");
        }catch (Throwable e){
            log.error("删除地图展示信息失败"+e.getMessage());
            return Reply.fail("删除地图展示信息失败"+e.getMessage());
        }
    }


    /**
     * 获取接口信息
     */
    private Map<String,Object> getInterfaceNews(List<String> names,String assetsId,Integer monitorServerId,String showInformation){
        Map<String,Object> interfaceMap = new LinkedHashMap<>();
        List<String> showNames = Arrays.asList(showInformation.split(","));
        if(CollectionUtils.isEmpty(names))return interfaceMap;
        for (String name : names) {
            //查询接口信息
            DiskTypeDto diskTypeDto = new DiskTypeDto();
            diskTypeDto.setType(name);
            diskTypeDto.setAssetsId(assetsId);
            diskTypeDto.setMonitorServerId(monitorServerId);
            Reply netDetail = serverService.getNetDetail(diskTypeDto);
            if (null == netDetail || netDetail.getRes() != PaasConstant.RES_SUCCESS)continue;
            List<DiscDto> discDtos = (List<DiscDto>) netDetail.getData();
            //获取接口利用率和接口状态
            if(CollectionUtils.isEmpty(discDtos))return interfaceMap;
            for (DiscDto discDto : discDtos) {
                List<ItemData> infoData = discDto.getInfoData();
                if(CollectionUtils.isEmpty(infoData))continue;
                for (ItemData infoDatum : infoData) {
                    String itemName = infoDatum.getName();//监控项名称
                    if(StringUtils.isNotBlank(itemName) && itemName.contains("MW_INTERFACE_STATUS") && showNames.contains("MW_INTERFACE_STATUS")){//接口状态
                        interfaceMap.put("接口状态["+name+"]",infoDatum.getValue());
                        continue;
                    }
                    if(StringUtils.isNotBlank(itemName) && itemName.contains("INTERFACE_OUT_UTILIZATION") && showNames.contains("INTERFACE_OUT_UTILIZATION")){//接口发送利用率
                        interfaceMap.put("接口利用率(出)["+name+"]",infoDatum.getValue());
                        continue;
                    }
                    if(StringUtils.isNotBlank(itemName) && itemName.contains("INTERFACE_IN_UTILIZATION") && showNames.contains("INTERFACE_IN_UTILIZATION")){//接口接收利用率
                        interfaceMap.put("接口利用率(入)["+name+"]",infoDatum.getValue());
                        continue;
                    }
                    if(StringUtils.isNotBlank(infoDatum.getChName()) && infoDatum.getChName().equals("MTU") && showNames.contains("INTERFACE_MTU")){//接口接收利用率
                        interfaceMap.put("MTU["+name+"]",infoDatum.getValue());
                        continue;
                    }
                    if(StringUtils.isNotBlank(itemName)){
                        if(itemName.contains("]")){
                            String[] split = itemName.split("]");
                            if(StringUtils.isNotBlank(split[1]) && showNames.contains(split[1])){
                                interfaceMap.put(infoDatum.getChName()+"["+name+"]",infoDatum.getValue());
                            }
                        }
                    }
                }
            }
        }
        return interfaceMap;
    }

    /**
     * 获取告警次数
     */
    private  Map<Integer,Integer> getAlertcount(){
        Map<Integer,Integer> alertMap = new HashMap<>();
        //查询机构告警次数数据
        Reply reply = lagerScreenService.getCoordinate();
        if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS)return alertMap;
        List<CoordinateAddress> coordinateAddress = (List<CoordinateAddress>) reply.getData();
        if(CollectionUtils.isEmpty(coordinateAddress))return alertMap;
        for (CoordinateAddress address : coordinateAddress) {
            Integer orgId = address.getOrgId();
            Map<String, Integer> orgAsset = address.getOrgAsset();
            if(orgAsset != null){
                Integer alertCount = orgAsset.get("告警次数");
                if(alertCount != null){
                    alertMap.put(orgId,alertCount);
                }
            }
        }
        return alertMap;
    }



    /**
     * 获取用户查询资产的限制条件
     *
     * @param queryParam 查询参数
     */
    private void getUserPerm(Map<String, Object> queryParam) {
        String loginName = loginCacheInfo.getLoginName();
        Integer userId = loginCacheInfo.getCacheInfo(loginName).getUserId();
        //数据权限：private public
        String perm = loginCacheInfo.getRoleInfo().getDataPerm();
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
     * 获取标签数据
     */
    private  List<AssetsTreeDTO> getLabelAssetsData(Map<String,Object> queryParam){
        AssetsTreeDTO unknown = new AssetsTreeDTO();
        List<AssetsTreeDTO> treeDTOS = new ArrayList<>();
        List<AssetsDTO> ids = new ArrayList<>();
        List<AssetsTreeDTO> dtos = screenMapDao.selectAssetsLabelList(queryParam);
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
//                    AssetsTreeDTO unknown = new AssetsTreeDTO();
        List<AssetsDTO> assetsDTOS = screenMapDao.selectAllAssets(queryParam);
        if (ids.size() > 0 && assetsDTOS != null && assetsDTOS.size() > 0) {
            assetsDTOS.removeAll(ids);
        }
        if (CollectionUtils.isNotEmpty(assetsDTOS)) {
            unknown.setTypeName("未知");
            unknown.setTypeId(0);
            unknown.setAssetsList(assetsDTOS);
        }
        treeDTOS.add(unknown);
        return treeDTOS;
    }

    @Autowired
    MWOrgService mwOrgService;

    /**
     * 获取机构分类资产数据
     * @param queryParam
     */
    private  List<AssetsTreeDTO> getOrgAssetsData(Map<String,Object> queryParam){
        List<AssetsTreeDTO> treeDTOS = new ArrayList<>();
        List<AssetsDTO> orgIds = new ArrayList<>();
        HashSet<String> assetsIdSet = new HashSet<>();
        HashSet<Integer> userOrgSet = new HashSet<>();
        Reply userOrgReply = mwOrgService.selectDorpdownList(new QueryOrgForDropDown());
        if (userOrgReply.getRes() == PaasConstant.RES_SUCCESS ){
            List<MWOrgDTO> orgList = (List<MWOrgDTO>) userOrgReply.getData();
            getUserOrgSet(orgList,userOrgSet);
        }
        List<MWOrgDTO> orgList = mwOrgService.getAllOrgList();
        if (CollectionUtils.isNotEmpty(orgList)){
            for (MWOrgDTO org : orgList) {
                AssetsTreeDTO assetsTreeDTO = new AssetsTreeDTO();
                assetsTreeDTO.setTypeName(org.getOrgName());
                assetsTreeDTO.setTypeId(org.getOrgId());
                List<AssetsDTO> assetsDTOList = new ArrayList<>();
                if (userOrgSet.contains(org.getOrgId())){
                    queryParam.put("orgId",org.getOrgId());
                    assetsDTOList    = screenMapDao.selectAssetsOrgList(queryParam);
                    if (CollectionUtils.isNotEmpty(assetsDTOList)) {
                        Iterator iterator = assetsDTOList.iterator();
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
                if (org.getChilds() != null && org.getChilds().size() > 0) {
                    assetsDTOList.addAll(getChildOrgAssetsList(queryParam, assetsTreeDTO, org.getChilds(),
                            orgIds, assetsIdSet, userOrgSet));
                }
                if (CollectionUtils.isNotEmpty(assetsDTOList)) {
                    assetsDTOList = assetsDTOList.stream().distinct().collect(Collectors.toList());
                    assetsTreeDTO.setAssetsList(assetsDTOList);
                    treeDTOS.add(assetsTreeDTO);
                }else{
                    assetsTreeDTO.setAssetsList(assetsDTOList);
                    treeDTOS.add(assetsTreeDTO);
                }
            }
        }
        //删除空白的组织资产数据
        sortAssetsTreeList(treeDTOS);
        //先将机构分组
        return getChildrenList(treeDTOS);
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

    private void getUserOrgSet(List<MWOrgDTO> orgList, HashSet<Integer> userOrgSet) {
        if (CollectionUtils.isNotEmpty(orgList)){
            for (MWOrgDTO org : orgList){
                userOrgSet.add(org.getOrgId());
                if (CollectionUtils.isNotEmpty(org.getChilds())){
                    getUserOrgSet(org.getChilds(),userOrgSet);
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
                childAssetsDTOList = screenMapDao.selectAssetsOrgList(queryParam);
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
     * 获取资产监控项
     * @return
     */
    @Override
    public Reply getScreenItemName() {
        List<Map<String, String>> maps = screenMapDao.selectScreenAllMonitorItem();
        if(CollectionUtils.isEmpty(maps))return Reply.ok(maps);
        //设置默认监控项
        for (Map<String, String> map : maps) {
            String itemName = map.get("itemName");
            map.put("isDefault","0");
            if(StringUtils.isBlank(itemName))continue;
            if("CPU_UTILIZATION".equals(itemName) || "MEMORY_UTILIZATION".equals(itemName) || "INTERFACE_IN_UTILIZATION".equals(itemName)
              || "INTERFACE_OUT_UTILIZATION".equals(itemName) || "MW_INTERFACE_STATUS".equals(itemName)){
                map.put("isDefault","1");
            }
        }
        Map<String,String> alertMap = new HashMap<>();
        alertMap.put("itemName","告警次数");
        alertMap.put("name","告警次数");
        alertMap.put("isDefault","1");
        alertMap.put("id",Integer.MAX_VALUE+"");
        maps.add(alertMap);
        return Reply.ok(maps);
    }
}
