package cn.mw.monitor.server.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.server.dao.ItemNameDao;
import cn.mw.monitor.server.dao.MwMyMonitorDao;
import cn.mw.monitor.server.model.MwBaseComponent;
import cn.mw.monitor.server.param.*;
import cn.mw.monitor.server.serverdto.ComponentLayoutDTO;
import cn.mw.monitor.server.serverdto.ItemNameRank;
import cn.mw.monitor.server.serverdto.ItemRank;
import cn.mw.monitor.server.serverdto.MwBaseComponentDTO;
import cn.mw.monitor.server.service.MwMyMonitorService;
import cn.mw.monitor.server.service.ResultResolver;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.MyMonitorCommons;
import cn.mw.monitor.service.server.api.dto.*;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.NewUnits;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

/**
 * @author syt
 * @Date 2021/2/3 11:14
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional
public class MwMymonitorServiceImpl implements MwMyMonitorService {
    private static final Logger logger = LoggerFactory.getLogger("MwMyMonitorService");

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MyMonitorCommons myMonitorCommons;

    @Autowired
    private MwServerManager mwServerManager;

    @Autowired
    private MwServerService mwServerService;

    @Resource
    private MwMyMonitorDao myMonitorDao;

    @Resource
    private ItemNameDao itemNameDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwZabbixHistoryDataHandle zabbixHistoryDataHandle;

    @Autowired
    private ResultResolver resultResolver;

    private final String NORMAL = "NORMAL";//正常


    @Override
    public Reply getItemInfoByFilter(QueryArgumentsParam qParam) {
        List<ItemApplication> list = new ArrayList<>();
        AssetsBaseDTO baseDTO = qParam.getAssetsBaseDTO();
        boolean emptyDiscoverId = false;
        boolean emptyUnits = false;
        String assetsId = "";
        Integer serverId = 0;
        if(qParam.getAssetsBaseDTO()!=null){
            assetsId = qParam.getAssetsBaseDTO().getAssetsId();
            serverId = qParam.getAssetsBaseDTO().getMonitorServerId();
        }
        List<String> templateIdList = new ArrayList<>();
        //根据主机Id获取所有父模板数据
        if(!Strings.isNullOrEmpty(assetsId) && intValueConvert(serverId)!=0){
            MWZabbixAPIResult templatesResult = mwtpServerAPI.getTemplatesByHostId(serverId,assetsId);
            JsonNode datas = (JsonNode) templatesResult.getData();
            if (datas != null && datas.size() > 0) {
                JsonNode data = datas.get(0);
                if (data.get("parentTemplates").size() > 0) {
                    JsonNode parentTemplates = data.get("parentTemplates");
                    //根据接口Api获取templateId
                    for(JsonNode tempNode : parentTemplates){
                        String templateId = tempNode.get("templateid").asText();
                        if(!Strings.isNullOrEmpty(templateId)){
                            templateIdList.add(templateId);
                        }
                    }
                }
            }
        }



        try {
            //有下拉框的前提条件是监控项名称中带有中括号
            String itemName = qParam.isDropdownFlag() ? "[" : null;
            //当百分比标识为true时，只查监控项中单位为%的监控项
            String units = qParam.isPercentFlag() ? "%" : null;
            if (null != baseDTO) {
                ItemApplication item = qParam.getItemApplication();
                HashMap filter = new HashMap<>();
                if (qParam.isFigureFlag()) {
                    filter.put("value_type", Arrays.asList("0", "3"));
                    filter.put("valuemapid", "0");
                }
                if (null != item) {//添加查询监控项的筛选条件
                    //如果不为空，说明是用户选择完一个以后的再选
                    if (!qParam.isMultipleFlag()) {//当多选是false时，说明是单选，后端直接返回空数组，控制前端为单选
                        return Reply.ok(list);
                    }
                    if (item.getDelay() != null && StringUtils.isNotEmpty(item.getDelay()) && qParam.isFigureFlag()) {
                        filter.put("delay", item.getDelay());
                    }
//                 && StringUtils.isNotEmpty(item.getUnits())
                    if (item.getUnits() != null) {
                        if (StringUtils.isEmpty(item.getUnits())) {//当单位是空字符串时，通过zabbixAPI过滤不出来单位为空字符串的监控项
                            //所以需要定义变量，再进行处理
                            emptyUnits = true;
                        } else {
                            filter.put("units", item.getUnits());
                        }
                        filter.put("units", item.getUnits());
                    }
                    log.info("进度001::");
                    if (item.getDiscoverId() != null) {
                        if (StringUtils.isNotEmpty(item.getDiscoverId())) {//当存在自动发现规则id时，直接返回对应规则id的监控项
                            list.addAll(getRuleItemPrototypes(baseDTO, filter, Arrays.asList(item.getDiscoverId())));
                            return Reply.ok(list);
                        } else {
                            emptyDiscoverId = true;
                        }
                    }
                } else {
                    if (units != null && StringUtils.isNotEmpty(units)) {
                        filter.put("units", units);
                    }
                }

                if (qParam.isWithoutUnitsFlag()) {
                    emptyUnits = false;
                    filter.remove("units");
                }
                if(CollectionUtils.isEmpty(templateIdList)){
                    templateIdList.add(baseDTO.getTemplateId());
                }
                MWZabbixAPIResult itemsByTemplateIds = mwtpServerAPI.getItemsByTemplateIdsFilter(baseDTO.getMonitorServerId(),templateIdList , filter);
                if (itemsByTemplateIds == null) {
                    log.info("根据模板id获取监控项接口返回null");
                }
                log.info("根据模板id获取监控项接口返回Code:" + itemsByTemplateIds.getCode() + ";message:" + itemsByTemplateIds.getMessage());
                list = JSONArray.parseArray(String.valueOf(itemsByTemplateIds.getData()), ItemApplication.class);
                log.info("进度002::list尺寸" + list.size());
                list.forEach(itemApplication -> {//获取监控项对应的中文名称
//                //当带有中括号的时候，需要把中括号的内容去掉，只保留原始监控项名称
//                itemApplication.setName(qParam.isDropdownFlag() ? itemApplication.getName().substring(itemApplication.getName().indexOf("]") + 1) : itemApplication.getName());
                    itemApplication.setChName(mwServerManager.getChName(itemApplication.getName()));
                });
                if (!emptyDiscoverId) {
                    list.addAll(getRuleItemPrototypes(baseDTO, filter, null));
                }
                if (emptyUnits) {
                    //需要将list中单位不为空的去掉
                    list = list.stream()
                            .filter(x -> StringUtils.isEmpty(x.getUnits()))
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("获取监控项失败::", e);
        }

//        Set<ItemApplication> itemApplicationSet = new TreeSet<>(Comparator.comparing(ItemApplication::getName));
//        itemApplicationSet.addAll(list);
//        list = new ArrayList<>(itemApplicationSet);
        return Reply.ok(list);
    }

    @Override
    public Reply getComponentList() {
        List<MwBaseComponent> baseComponents = myMonitorDao.selectBaseComponents();
        List<MwBaseComponentDTO> list = new ArrayList<>();
        baseComponents.forEach(baseComponent -> {
            MwBaseComponentDTO dto = CopyUtils.copy(MwBaseComponentDTO.class, baseComponent);
            JSONObject jsonObject = JSONObject.parseObject(baseComponent.getComponentSelfParam());
            JSONObject jsonParam = JSONObject.parseObject(baseComponent.getComponentParam());
            dto.setSelfParam(jsonObject);
            dto.setParam(jsonParam);
            list.add(dto);

        });
        return Reply.ok(list);
    }

    @Override
    public Reply getLineChartsData(LineChartDTO lineChartDTO) {
        //通过hostid获得不同的itemid 然后查询itemid的history
        try {
            Map<String, Object> resultData = new HashMap<>();
            String assetsStatus = lineChartDTO.getAssetsStatus();
            if (StringUtils.isNotBlank(assetsStatus) && !NORMAL.equals(assetsStatus)) {
                //异常资产不返回数据
                return Reply.ok(resultData);
            }
            //判断是否需要取趋势数据
            List trendData = getBaseLineChartTrendData(lineChartDTO);
            if (lineChartDTO.getIsTrend() != null && lineChartDTO.getIsTrend() && lineChartDTO.getDateType() != 1) {
                resultData.put("list", trendData);
                return Reply.ok(resultData);
            }
            boolean bracketFlag = false;
            Object lists = null;
            AssetsBaseDTO assetsBaseDTO = lineChartDTO.getAssetsBaseDTO();
            log.info("获取监控项查询参数" + assetsBaseDTO);
            if (assetsBaseDTO != null) {
                List<ItemApplication> items = lineChartDTO.getItemApplicationList();
                if (items != null && items.size() > 0) {
                    if (items.get(0).getDiscoverId() != null && StringUtils.isNotEmpty(items.get(0).getDiscoverId())) {
                        if (lineChartDTO.isSingleLine()) {//当存在一个折线图的时候，需要对选中的监控项进行取平均值
                            ServerHistoryDto serverHistoryDto = ServerHistoryDto.builder()
                                    .dateType(lineChartDTO.getDateType())
                                    .dateEnd(lineChartDTO.getDateEnd() == null ? null : lineChartDTO.getDateEnd())
                                    .dateStart(lineChartDTO.getDateStart() == null ? null : lineChartDTO.getDateStart())
                                    .build();
                            serverHistoryDto.setAssetsId(assetsBaseDTO.getAssetsId());
                            serverHistoryDto.setMonitorServerId(assetsBaseDTO.getMonitorServerId());
                            serverHistoryDto.setId(assetsBaseDTO.getId());
                            serverHistoryDto.setTemplateId(assetsBaseDTO.getTemplateId());
                            serverHistoryDto.setName(Arrays.asList(items.get(0).getName()));
                            Reply data = mwServerService.getHistoryData(serverHistoryDto);
                            if (data.getRes() == PaasConstant.RES_SUCCESS) {
                                lists = data.getData();
                            }
                            resultData.put("list", lists);
                            List list = handleLineChartsData(resultData);
                            resultData.put("list", list);
                            return Reply.ok(resultData);
                        }


                        bracketFlag = true;
                        if (Strings.isNullOrEmpty(lineChartDTO.getTypeItemName()) && (lineChartDTO.getTypeItemNames() == null || lineChartDTO.getTypeItemNames().size() <= 0)) {//当该字段为空时，说明是初始化的状态，要查出所有TypeItemNames
                            //首先需要确定数据库中是否含有当前TypeItemNames
                            List typeItemNames = new ArrayList();
                            boolean hasDescription = false;
                            TypeFilterDTO typeFilter = itemNameDao.getTypeFilter(assetsBaseDTO.getId(), items.get(0).getName());
                            if (typeFilter != null && typeFilter.getShowData() != null) {
                                typeItemNames = JSONObject.parseArray(typeFilter.getShowData(), Object.class);
//                                if (typeItemNames == null || typeItemNames.size() <= 0) {
//                                    if (items.get(0).getName().indexOf("INTERFACE") != -1) {
//                                        MWZabbixAPIResult data = mwtpServerAPI.getItemDataByAppName(assetsBaseDTO.getMonitorServerId(), assetsBaseDTO.getAssetsId(), null, ZabbixItemConstant.INTERFACE_DESCR);
//                                        JsonNode result = (JsonNode) data.getData();
//                                        if (data.getCode() == 0 && result.size() > 0) {
//                                            hasDescription = true;
//                                        }
//                                    }
//                                }
                            } else {
                                typeItemNames = mwServerManager.getNames(assetsBaseDTO.getMonitorServerId(), assetsBaseDTO.getAssetsId(), null, items.get(0).getName(), true);
                            }
                            if (typeItemNames != null && typeItemNames.size() > 0) {
                                //初始化第一个TypeItemName(其中对象可能是字符串也有可能是DropDownNamesDesc对象)
//                                lineChartDTO.setTypeItemName(typeItemNames.get(0) instanceof String ? typeItemNames.get(0).toString() :
//                                        (JSONObject.parseObject(typeItemNames.get(0).toString(), DropDownNamesDesc.class)).getName());

                                resultData.put("typeItemNames", typeItemNames);
                            } else {
                                resultData.put("typeItemNames", typeItemNames);
//                                if (hasDescription) {
//                                    resultData.put("typeItemNames", typeItemNames);
//                                }
                            }
                            if (items.size() == 1) {
                                resultData.put("typeItemNamesMultiple", true);
                            }
                        }
                    }
                    log.info("获取监控项查询参数2" + assetsBaseDTO);
                    List<String> itemNames = new ArrayList<>();
                    if (items.size() == 1 && lineChartDTO.getTypeItemNames() != null && lineChartDTO.getTypeItemNames().size() > 0) {
                        for (String type : lineChartDTO.getTypeItemNames()) {
                            String name = "[" + type + "]" + items.get(0).getName();
                            itemNames.add(bracketFlag ? name : items.get(0).getName());
                        }
                    } else {
                        for (ItemApplication item : items) {
                            String name = "[" + lineChartDTO.getTypeItemName() + "]" + item.getName();
                            itemNames.add(bracketFlag ? name : item.getName());
                        }
                    }
                    lineChartDTO.setItemNames(itemNames);
                }
                if (!lineChartDTO.isNoSelectChart()) {
                    log.info("获取监控项查询参数3" + lineChartDTO);
                    Map<String, String> itemChName = mwServerManager.getItemChName(lineChartDTO.getItemNames());
                    MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(assetsBaseDTO.getMonitorServerId(), lineChartDTO.getItemNames(), assetsBaseDTO.getAssetsId());
                    //根据主机查询对应的监控项信息
                    if(result!=null && result.getCode() == 0){
                        items = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                    }
                    items.forEach(itemApplication -> {
                        String s = itemChName.get(itemApplication.getName());

                        itemApplication.setChName((s == null || StringUtils.isEmpty(s)) ? itemApplication.getName() : s);
                    });
                    lineChartDTO.setItemApplicationList(items);

                    ServerHistoryDto serverHistoryDto = ServerHistoryDto.builder()
                            .dateType(lineChartDTO.getDateType())
                            .itemApplicationList(items)
                            .dateEnd(lineChartDTO.getDateEnd() == null ? null : lineChartDTO.getDateEnd())
                            .dateStart(lineChartDTO.getDateStart() == null ? null : lineChartDTO.getDateStart())
                            .build();
                    serverHistoryDto.setAssetsId(assetsBaseDTO.getAssetsId());
                    serverHistoryDto.setMonitorServerId(assetsBaseDTO.getMonitorServerId());
                    serverHistoryDto.setId(assetsBaseDTO.getId());
                    serverHistoryDto.setTemplateId(assetsBaseDTO.getTemplateId());
                    serverHistoryDto.setIsTrend(lineChartDTO.getIsTrend());
                    log.info("获取监控项查询参数4" + serverHistoryDto);
                    Reply data = mwServerService.getHistoryData(serverHistoryDto);
                    if (data.getRes() == PaasConstant.RES_SUCCESS) {
                        lists = data.getData();
                    }
                    resultData.put("list", lists);
                } else {
                    resultData.put("list", null);
                }
//                lists = myMonitorCommons.getLineChartHistory(lineChartDTO);
            } else {
                throw new RuntimeException("资产信息不存在！");
            }
            List list = handleLineChartsData(resultData);
            resultData.put("list", list);
            return Reply.ok(resultData);
        } catch (Exception e) {
            logger.error("fail to getLineChartsData with lineChartDTO={}, cause:{}", lineChartDTO, e);
            return Reply.fail(ErrorConstant.SERVER_HISTORY_CODE_302002, ErrorConstant.SERVER_HISTORY_MSG_302002);
        }
    }


    /**
     * 高级表格查询历史数据获取趋势数据
     */
    private List getBaseLineChartTrendData(LineChartDTO lineChartDTO) {
        Integer dateType = lineChartDTO.getDateType();
        String dateStart = lineChartDTO.getDateStart();
        String dateEnd = lineChartDTO.getDateEnd();
        AssetsBaseDTO assetsBaseDTO = lineChartDTO.getAssetsBaseDTO();
        String assetsId = assetsBaseDTO.getAssetsId();
        int monitorServerId = assetsBaseDTO.getMonitorServerId();
        ServerHistoryDto serverHistoryDto = ServerHistoryDto.builder().dateType(dateType).dateStart(dateStart).dateEnd(dateEnd).build();
        serverHistoryDto.setAssetsId(assetsId);
        serverHistoryDto.setMonitorServerId(monitorServerId);
        List<ItemApplication> itemApplicationList = lineChartDTO.getItemApplicationList();
        List<String> typeItemNames = lineChartDTO.getTypeItemNames();
        List<String> itemNames = new ArrayList<>();
        for (ItemApplication itemApplication : itemApplicationList) {
            String name = itemApplication.getName();
            if(CollectionUtils.isEmpty(typeItemNames)){
                itemNames.add(name);
                continue;
            }
            for (String typeItemName : typeItemNames) {
                itemNames.add("["+typeItemName+"]"+name);
            }
        }
        log.info("MwMymonitorServiceImpl{} getBaseLineChartTrendData() itemNames2::"+itemNames);
        serverHistoryDto.setName(itemNames);
        serverHistoryDto.setIsTrend(lineChartDTO.getIsTrend());
        List list = zabbixHistoryDataHandle.handleHistory(serverHistoryDto);
        return list;
    }

    /**
     * 处理自定义布局返回数据结构
     *
     * @param resultData
     * @return
     */
    private List handleLineChartsData(Map<String, Object> resultData) {
        List list = (List) resultData.get("list");
        List newList = new ArrayList();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Object o : list) {
                List l = (List) o;
                newList.addAll(l);
            }
        }
        List realNewList = new ArrayList();
        realNewList.add(newList);
        return realNewList;
    }

    @Override
    public Reply saveComponentLayout(ComponentLayoutDTO aParam) {
        if (aParam.getRoleId() == 0) {//当用户为系统管理员时，有两种可能（保存默认布局/保存自定义布局）

        } else {//当用户为其他用户时，只有一种可能，保存自定义布局
            aParam.setDefaultFlag(false);
        }
        AddOrUpdateComLayoutParam param = CopyUtils.copy(AddOrUpdateComLayoutParam.class, aParam);
        String jsonString = JSONObject.toJSONString(aParam.getComponentLayout());
        param.setComponentLayout(jsonString);
        param.setModifier(iLoginCacheInfo.getLoginName());
        List<AddOrUpdateComLayoutParam> layoutParams = myMonitorDao
                .selectByFilter(aParam.getDefaultFlag() ? null : aParam.getUserId(), aParam.getMonitorServerId(), aParam.getTemplateId(), aParam.getDefaultFlag(), aParam.getNavigationBarId(), aParam.getAssetsId());


        if (layoutParams != null && layoutParams.size() > 0) {//说明数据库中已有相应保存信息，只需要对信息进行更新即可
            //查询布局版本控制的数据量
            Integer versionCount = myMonitorDao.selectComLayoutVersionCount(param.getComLayoutId());
            //修改之前先查询原先的数据
            AddOrUpdateComLayoutParam lastParam = myMonitorDao.selectComLayoutDataById(param.getComLayoutId());
            myMonitorDao.update(param);
            //获取版本控制
            Map versionMap = myMonitorDao.selectComLayoutVersion(param.getComLayoutId());
            Integer minVersion = 0;
            Integer maxVersion = 0;
            if (versionMap != null && versionMap.get("maxVersion") != null && versionMap.get("minVersion") != null) {
                minVersion = Integer.valueOf(versionMap.get("minVersion").toString());
                maxVersion = Integer.valueOf(versionMap.get("maxVersion").toString());
            }
            AddOrUpdateComLayoutVersionParam versionParam = CopyUtils.copy(AddOrUpdateComLayoutVersionParam.class, lastParam);
            versionParam.setComLayoutId(param.getComLayoutId());
            if (versionCount >= 3) {
                //删除版本数最小的那条数据，只保留最新的三条数据
                myMonitorDao.deleteComLayoutByMinVersion(param.getComLayoutId(), minVersion);
            }
            maxVersion = maxVersion + 1;
            versionParam.setVersion(maxVersion);
            myMonitorDao.insertComLayoutVersion(versionParam);
            return Reply.ok();
        }
        param.setCreator(iLoginCacheInfo.getLoginName());
        myMonitorDao.insert(param);
        return Reply.ok();
    }

    @Override
    public Reply selectComponentLayout(QueryComLayoutParam qParam) {
        List<AddOrUpdateComLayoutParam> layoutParams = new ArrayList<>();
        layoutParams = myMonitorDao.selectByFilter(qParam.getUserId(), qParam.getMonitorServerId(), qParam.getTemplateId(), false, qParam.getNavigationBarId(), qParam.getAssetsId());
        if (layoutParams.size() <= 0) {
            layoutParams = myMonitorDao.selectByFilter(null, qParam.getMonitorServerId(), qParam.getTemplateId(), true, qParam.getNavigationBarId(), qParam.getAssetsId());
        }
        if (layoutParams.size() > 0) {
            ComponentLayoutDTO dto = CopyUtils.copy(ComponentLayoutDTO.class, layoutParams.get(0));
            JSONObject jsonObject = JSONObject.parseObject(layoutParams.get(0).getComponentLayout());
            dto.setComponentLayout(jsonObject);
            return Reply.ok(dto);
        }
        return Reply.ok(new ComponentLayoutDTO());
    }

    @Override
    public Reply selectComLayoutByVersion(QueryComLayoutVersionParam param) {
        //获取版本控制
        Map versionMap = myMonitorDao.selectComLayoutVersion(param.getComLayoutId());
        //回撤步数
        Integer retreatNum = param.getRetreatNum();
        if (retreatNum != null) {
            Integer maxVersion = 0;
            Integer version = 0;
            if (versionMap != null && versionMap.get("maxVersion") != null) {
                maxVersion = Integer.valueOf(versionMap.get("maxVersion").toString());
            } else {
                return Reply.fail(500, "暂无历史版本数据！");
            }
            if (retreatNum == 1) {
                version = maxVersion;
            } else if (retreatNum == 2) {
                version = maxVersion - 1;
            } else if (retreatNum == 3) {
                version = maxVersion - 2;
            }
            if (version <= 0) {
                version = maxVersion;
            }
            AddOrUpdateComLayoutParam cparam = myMonitorDao.selectComLayoutByVersion(param.getComLayoutId(), version);
            if (cparam != null) {
                ComponentLayoutDTO dto = CopyUtils.copy(ComponentLayoutDTO.class, cparam);
                JSONObject jsonObject = JSONObject.parseObject(cparam.getComponentLayout());
                dto.setComponentLayout(jsonObject);
                return Reply.ok(dto);
            }
        }
        return Reply.ok(new ComponentLayoutDTO());
    }

    @Override
    public Reply getItemRank(ItemBaseDTO param) {
        try {
            ItemRank memory = mwServerManager.getItemRank(param);
            List<ItemNameRank> list = memory.getItemNameRankList();
            Collections.sort(list, new ItemNameRank());//倒序排序
            if (list.size() > 5) {
                list = list.subList(0, 5);
            }
            memory.setItemNameRankList(list);
            return Reply.ok(memory);
        } catch (Exception e) {
            logger.error("fail to getMemory with serverDto={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.SERVER_RANK_CODE_302001, ErrorConstant.SERVER_RANK_MSG_302001);
        }
    }

    @Override
    public Reply getItemsTableInfo(ItemBaseDTO param) {
        AssetsBaseDTO assets = param.getAssetsBaseDTO();
        List<ItemApplication> itemApplicationList = param.getItemApplicationList();
        if (itemApplicationList != null && itemApplicationList.size() > 0) {
            boolean flag = false;//表示是否是模糊查询
            String discoverId = itemApplicationList.get(0).getDiscoverId();
            if (discoverId != null && StringUtils.isNotEmpty(discoverId)) {
                flag = true;
            }
            //获取list对象中属性为Name 的重新组成一个数组，并进行去重
            List<String> itemNames = itemApplicationList.stream().map(item -> item.getName()).distinct().collect(Collectors.toList());
            if (assets != null) {
                logger.info("getItemsTableInfo 查询基础表格信息,运行成功结束");
                return myMonitorCommons.getItemsIsFilter(assets.getMonitorServerId(), assets.getAssetsId(), itemNames, flag);
            }
        }

        return Reply.fail("查询基础表格信息失败！");
    }


    @Override
    public Reply getBarGraphInfo(LineChartDTO param) {
        try {
            Reply data = getLineChartsData(param);
            if (data.getRes() == PaasConstant.RES_SUCCESS) {
                Map<String, Object> resultData = (Map<String, Object>) data.getData();
                //获取所有历史信息
                List<List<Map<String, Object>>> lists = (List<List<Map<String, Object>>>) resultData.get("list");
                if (lists != null && lists.size() > 0) {
                    for (Map<String, Object> list : lists.get(0)) {
                        //每五条取一条
                        List<MWItemHistoryDto> newMaxList = new ArrayList<>();
                        List<MWItemHistoryDto> newAvgList = new ArrayList<>();
                        List<MWItemHistoryDto> newRealList = new ArrayList<>();
                        List<MWItemHistoryDto> newMinList = new ArrayList<>();

                        List<MWItemHistoryDto> maxList = (List<MWItemHistoryDto>) list.get("maxData");
                        List<MWItemHistoryDto> avgList = (List<MWItemHistoryDto>) list.get("avgData");
                        List<MWItemHistoryDto> realList = (List<MWItemHistoryDto>) list.get("realData");
                        List<MWItemHistoryDto> minList = (List<MWItemHistoryDto>) list.get("minData");
                        if (realList != null && realList.size() > 0) {
                            for (int i = 0, len = realList.size(); i < len; i = i + 5) {
                                newRealList.add(realList.get(i));
                            }
                        } else {
                            if (avgList != null && avgList.size() > 0) {
                                for (int i = 0, len = avgList.size(); i < len; i = i + 5) {
                                    newAvgList.add(avgList.get(i));
                                    newMaxList.add(maxList.get(i));
                                    newMinList.add(minList.get(i));
                                }
                            }
                        }
                        list.put("maxData", newMaxList);
                        list.put("minData", newMinList);
                        list.put("avgData", newAvgList);
                        list.put("realData", newRealList);
                    }
                }
                resultData.put("list", lists);
                return Reply.ok(resultData);
            } else {
                return Reply.fail("查询柱状图数据失败！");
            }
        } catch (Exception e) {
            logger.info("fail to getBarGraphInfo   " + e);
            return Reply.fail("查询柱状图数据失败！");
        }
    }

    @Override
    public Reply getPieChartInfo(LineChartDTO param) {
        try {
            Map<String, Object> resultData = new HashMap<>();
            AssetsBaseDTO assetsBaseDTO = param.getAssetsBaseDTO();
            if (param.getTypeItemName() == null || StringUtils.isEmpty(param.getTypeItemName())) {//当该字段为空时，说明是初始化的状态，要查出所有TypeItemNames
                //首先需要确定数据库中是否含有当前TypeItemNames
                List typeItemNames = new ArrayList();
                TypeFilterDTO typeFilter = itemNameDao.getTypeFilter(assetsBaseDTO.getId(), param.getItemNames().get(0));
                if (typeFilter != null && typeFilter.getShowData() != null) {
                    typeItemNames = JSONObject.parseArray(typeFilter.getShowData(), Object.class);
                } else {
                    typeItemNames = mwServerManager.getNames(assetsBaseDTO.getMonitorServerId(), assetsBaseDTO.getAssetsId(), null, param.getItemNames().get(0), true);
                }
                if (typeItemNames != null && typeItemNames.size() > 0) {
                    //初始化第一个TypeItemName(其中对象可能是字符串也有可能是DropDownNamesDesc对象)
                    param.setTypeItemName(typeItemNames.get(0) instanceof String ? typeItemNames.get(0).toString() : ((DropDownNamesDesc) typeItemNames.get(0)).getName());
                }
                resultData.put("typeItemNames", typeItemNames);
            }
            DiskListDto diskListDto = myMonitorCommons.getDiskInfoByDiskName(assetsBaseDTO.getMonitorServerId(), param.getTypeItemName(), assetsBaseDTO.getAssetsId());
            resultData.put("diskListDto", diskListDto);
            return Reply.ok(resultData);
        } catch (Exception e) {
            logger.info("fail to getPieChartInfo  " + e);
            return Reply.fail("查询磁盘信息饼状图失败！");
        }
    }

    @Override
    public Reply getPieChartData(ItemBaseDTO param) {
        AssetsBaseDTO assets = param.getAssetsBaseDTO();
        List<ItemApplication> itemApplicationList = param.getItemApplicationList();
        if (itemApplicationList != null && itemApplicationList.size() > 0) {
            boolean flag = false;//表示是否是模糊查询
            String discoverId = itemApplicationList.get(0).getDiscoverId();
            if (discoverId != null && StringUtils.isNotEmpty(discoverId)) {
                flag = true;
            }
            //获取list对象中属性为Name 的重新组成一个数组，并进行去重
            List<String> itemNames = itemApplicationList.stream().map(item -> item.getName()).distinct().collect(Collectors.toList());
            if (assets != null) {
                logger.info("getItemsTableInfo 查询饼状图分布,运行成功结束");
                return myMonitorCommons.getItemsIsFilter(assets.getMonitorServerId(), assets.getAssetsId(), itemNames, flag);

            }
        }

        return Reply.fail("查询饼状图分布信息失败！");
    }

    /**
     * 查询高级表格信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getAdvanceTableInfo(AdvanceTableDTO param) {
        Map AllMap = new HashMap();
        try {
            String assetsStatus = param.getAssetsStatus();
            if (StringUtils.isNotBlank(assetsStatus) && !NORMAL.equals(assetsStatus)) {
                //异常资产不返回数据
                return Reply.ok(AllMap);
            }
            long time1 = System.currentTimeMillis();
            List<ApplicationDTO> applicationDTOList = param.getApplicationList();
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            //监控接口
            List<String> devNames = new ArrayList<>();
            //监控项
            List<String> itemNames = new ArrayList<>();
            List<String> devNamesList = new ArrayList<>();
            String applicationNames = "";
            for (ApplicationDTO dto : applicationDTOList) {
                //监控接口
                devNames = new ArrayList<>();
                //监控项
                itemNames = new ArrayList<>();
                //监控项
                String interfaceName = "监控接口";
                List<Map> titleName = new ArrayList<>();
                if (param.getApplicationList().get(0) != null && param.getApplicationList().size() > 0 && param.getApplicationList().get(0).getInterfaceName() != null) {
                    interfaceName = param.getApplicationList().get(0).getInterfaceName();
                }
//            itemNames = param.getApplicationList().get(0).getItemNames();
                List<String> ownItemName = param.getApplicationList().get(0).getItemNames();
                List<String> ownDevName = param.getApplicationList().get(0).getDevNames();
                List<String> itemNameList = new ArrayList<>();
                applicationNames = dto.getApplicationName();
//                itemNames = dto.getItemNames();
                QueryAdvanceTableParam p = new QueryAdvanceTableParam();
                p.setMonitorServerId(param.getMonitorServerId());
                p.setHostid(param.getHostid());
                p.setApplicationName(applicationNames);
                Map<String, Object> mapItem = getItemListMapByApplication(p);
                if (mapItem != null) {
                    if (mapItem.get("itemNameMap") != null && ((List) mapItem.get("itemNameMap")).size() > 0) {
                        List<Map<String, Object>> list = (List) mapItem.get("itemNameMap");
                        for (String items : ownItemName) {
                            for (Map<String, Object> m : list) {
                                if (items.equals(m.get("EnStrName").toString())) {
                                    itemNames.add(m.get("EnStrName").toString());
                                }
                            }
                        }
                    }
                    if (mapItem.get("devNameMap") != null && ((List) mapItem.get("devNameMap")).size() > 0) {
                        devNamesList = (List) mapItem.get("devNameMap");
                        for (String ownDevNameStr : ownDevName) {
                            if (devNamesList.contains(ownDevNameStr)) {
                                devNames.add(ownDevNameStr);
                            }
                        }
                        if (CollectionUtils.isEmpty(devNames)) {
                            devNames.addAll(devNamesList);
                        }
                    }
                }
                List<Map<String, Object>> mapList = new ArrayList<>();
                if (devNames.size() != 0) {
                    for (String deviceName : devNames) {
                        for (String itemName : itemNames) {
                            String itemId = deviceName + itemName;//监控项名称为监控主体+监控类型名称 例： [2C C5 D3 3B 6D 00 ,BLUEMOON-WIFI]<10.3.48.154>MW_CLIENTS_CHANNELS
                            itemNameList.add(itemId);
                        }
                    }
                    Map titleMap = new HashMap();
                    titleMap.put("field", "devName");
                    titleMap.put("title", interfaceName);
                    titleName.add(titleMap);
                    getDataInfoBydeviceName(param, applicationNames, itemNameList, itemNames, devNames, mapList);
                } else {
                    titleName = new ArrayList<>();
                    for (String itemName : itemNames) {
                        String itemId = itemName;//
                        itemNameList.add(itemId);
                    }
                    getDataInfoByitemName(param, applicationNames, itemNameList, mapList);
                }
                for (String itemName : itemNames) {
                    //监控项名称
                    String chName = mwServerManager.getChName(itemName);
                    Map titleMap = new HashMap();
                    titleMap.put("field", itemName);
                    titleMap.put("title", chName);
                    titleName.add(titleMap);
                }
                List<Map<String, Object>> lists = new ArrayList<>();
                List<Map> listInfo = new ArrayList<>();
                //数据过滤
                if (!Strings.isNullOrEmpty(param.getQueryName()) && !Strings.isNullOrEmpty(param.getQueryValue()) && devNames.size() != 0) {
                    if ("fuzzyQuery".equals(param.getQueryName())) {
                        //全字段查询
                        lists = mapList.stream().filter(map -> map.values().stream().anyMatch(s -> s instanceof Map ? ((Map) s).get("lastValue").toString().indexOf(param.getQueryValue()) != -1 : false)).collect(Collectors.toList());
                    } else {
                        for (Map<String, Object> map : mapList) {
                            if ("devName".equals(param.getQueryName())) {
                                boolean isFlag = map.get(param.getQueryName()).toString().indexOf(param.getQueryValue()) != -1;
                                if (isFlag) {
                                    lists.add(map);
                                }
                            } else {
                                boolean isFlag = ((Map) map.get(param.getQueryName())).get("lastValue").toString().indexOf(param.getQueryValue()) != -1;
                                if (isFlag) {
                                    lists.add(map);
                                }
                            }
                        }
                    }
                } else {
                    lists = mapList;
                }
                Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                //排序
                if (!Strings.isNullOrEmpty(param.getSortField()) && devNames.size() != 0) {
                    Collections.sort(lists, new Comparator<Map<String, Object>>() {
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            //对监控接口排序
                            if ("devName".equals(param.getSortField())) {
                                String valStr1 = o1.get(param.getSortField()).toString();
                                String valStr2 = o2.get(param.getSortField()).toString();
                                boolean isNumber = pattern.matcher(o1.get(param.getSortField()).toString()).matches();
                                if (isNumber) {
                                    Double val1 = Double.valueOf(valStr1.equals("") ? "0" : valStr1);
                                    Double val2 = Double.valueOf(valStr2.equals("") ? "0" : valStr2);
                                    //升序
                                    if (param.getSortMode() != null && param.getSortMode() == 0) {
                                        return val1.compareTo(val2);
                                    } else {//降序
                                        return val2.compareTo(val1);
                                    }
                                } else {
                                    //返回结果为字符串类型
                                    Collator instace = Collator.getInstance(Locale.CHINA);
                                    //升序
                                    if (param.getSortMode() != null && param.getSortMode() == 0) {
                                        return instace.compare(valStr1, valStr2);
                                    } else {//降序
                                        return instace.compare(valStr2, valStr1);
                                    }
                                }
                            } else { //对自定义监控项排序
                                if (o1.get(param.getSortField()) != null) {
                                    //返回结果为数值类型
                                    if (((Map) o1.get(param.getSortField())).get("valueType").toString().equals("3") || ((Map) o1.get(param.getSortField())).get("valueType").toString().equals("0")) {
                                        Double val1 = Double.valueOf(((Map) o1.get(param.getSortField())).get("oldValue").toString());
                                        Double val2 = Double.valueOf(((Map) o2.get(param.getSortField())).get("oldValue").toString());
                                        //升序
                                        if (param.getSortMode() != null && param.getSortMode() == 0) {
                                            return val1.compareTo(val2);
                                        } else {//降序
                                            return val2.compareTo(val1);
                                        }
                                    } else {

                                        //如果类型为字符串，但内容为数字
                                        if (((Map) o1.get(param.getSortField())).get("lastValue") != null && !"".equals(((Map) o1.get(param.getSortField())).get("lastValue")) &&
                                                pattern.matcher(((Map) o1.get(param.getSortField())).get("lastValue").toString()).matches()) {
                                            Double val1 = Double.valueOf(((Map) o1.get(param.getSortField())).get("lastValue").toString().equals("") ? "0" : ((Map) o1.get(param.getSortField())).get("lastValue").toString());
                                            Double val2 = Double.valueOf(((Map) o2.get(param.getSortField())).get("lastValue").toString().equals("") ? "0" : ((Map) o2.get(param.getSortField())).get("lastValue").toString());
                                            //升序
                                            if (param.getSortMode() != null && param.getSortMode() == 0) {
                                                return val1.compareTo(val2);
                                            } else {//降序
                                                return val2.compareTo(val1);
                                            }
                                        } else {
                                            //返回结果为字符串类型
                                            Collator instace = Collator.getInstance(Locale.CHINA);
                                            String val1 = ((Map) o1.get(param.getSortField())).get("lastValue") != null ? ((Map) o1.get(param.getSortField())).get("lastValue").toString() : "";
                                            String val2 = ((Map) o1.get(param.getSortField())).get("lastValue") != null ? ((Map) o2.get(param.getSortField())).get("lastValue").toString() : "";
                                            //升序
                                            if (param.getSortMode() != null && param.getSortMode() == 0) {
                                                return instace.compare(val1, val2);
                                            } else {//降序
                                                return instace.compare(val2, val1);
                                            }
                                        }
                                    }
                                } else {
                                    return 0;
                                }
                            }
                        }
                    });
                }
                for (Map maps : lists) {
                    Map map = new HashMap();
                    //监控接口为空
                    if (devNames.size() == 0) {
                        listInfo.add(maps);
                    } else {
                        maps.forEach((k, v) -> {
                            if ("devName".equals(k)) {
                                map.put(k, v.toString());
                            } else {
                                map.put(k, ((Map) v).get("lastValue").toString());
                            }
                        });
                        listInfo.add(map);
                    }
                }
                pageInfo.setTotal(listInfo.size());
                List listByPage = pageList.getList(listInfo, param.getPageNumber(), param.getPageSize());
                pageInfo.setList(listByPage);
                AllMap.put("titleName", titleName);
                AllMap.put("dataList", pageInfo);
                //对高级表格的页面布局数据进行修改，保证每次进入不同模板下的设备时，监控项和监控接口都是对应的。
                //对数据进行处理
                String[] strSubs = param.getComponentLayout().split("(?<=applicationList)");
                StringBuilder sbr = new StringBuilder();
                for (String strSub : strSubs) {
                    String componentLayout = strSub;
                    String str = "\"applicationName\":\""+applicationNames+"\"";
                    String type = "\"activeMenu\": \"table\"";
                    if (strSub.contains(str) && strSub.contains(type)) {
                        int index1 = strSub.indexOf("\"devOp\":");
                        int index2 = strSub.indexOf("\"itemOp\":");
                        if(index1>0 && index2 > 1){
                            String oldDevOpStr = strSub.substring(index1 + 8, index2 - 1);
                            String devOpStr = JSONArray.toJSONString(devNamesList).toString();
                            componentLayout = strSub.replace(oldDevOpStr, devOpStr);
                        }
                    }
                    sbr.append(componentLayout);
                }
                String allComponentLayout = sbr.toString();
                AddOrUpdateComLayoutParam layoutParamayout = new AddOrUpdateComLayoutParam();
                layoutParamayout.setComponentLayout(allComponentLayout);
                layoutParamayout.setModifier(iLoginCacheInfo.getLoginName());
                layoutParamayout.setComLayoutId(param.getComLayoutId());
                layoutParamayout.setNavigationBarId(0);
                myMonitorDao.update(layoutParamayout);
            }


//        int index1 = param.getComponentLayout().indexOf("\"devOp\":");
//        int index2 = param.getComponentLayout().indexOf("\"itemOp\":");
//        String oldDevOpStr = param.getComponentLayout().substring(index1 + 8, index2 - 1);
//        String devOpStr = JSONArray.toJSONString(devNamesList).toString();
//        String componentLayout = param.getComponentLayout().replace(oldDevOpStr, devOpStr);
//        AddOrUpdateComLayoutParam layoutParamayout = new AddOrUpdateComLayoutParam();
//        layoutParamayout.setComponentLayout(allComponentLayout);
//        layoutParamayout.setModifier(iLoginCacheInfo.getLoginName());
//        layoutParamayout.setComLayoutId(param.getComLayoutId());
//        layoutParamayout.setNavigationBarId(0);
//        myMonitorDao.update(layoutParamayout);
            long time2 = System.currentTimeMillis();
        } catch (Throwable e) {
            log.error("获取高级表格数据失败", e);
            return Reply.fail(500, "获取高级表格数据失败");
        }
        return Reply.ok(AllMap);
    }


    @Override
    public LineChartDTO getLineChartDTO(LineChartDTO param) {
        boolean bracketFlag = false;
        AssetsBaseDTO assetsBaseDTO = param.getAssetsBaseDTO();
        if (assetsBaseDTO != null) {
            List<ItemApplication> items = param.getItemApplicationList();
            if (items != null && items.size() > 0) {
                if (items.get(0).getDiscoverId() != null && StringUtils.isNotEmpty(items.get(0).getDiscoverId())) {
                    bracketFlag = true;
                    if (param.getTypeItemName() == null || StringUtils.isEmpty(param.getTypeItemName())) {//当该字段为空时，说明是初始化的状态，要查出所有TypeItemNames
                        //首先需要确定数据库中是否含有当前TypeItemNames
                        List typeItemNames = new ArrayList();
                        TypeFilterDTO typeFilter = itemNameDao.getTypeFilter(assetsBaseDTO.getId(), items.get(0).getName().indexOf("INTERFACE") != -1 ? "INTERFACE" : items.get(0).getName());
                        if (typeFilter != null && typeFilter.getShowData() != null) {
                            typeItemNames = JSONObject.parseArray(typeFilter.getShowData(), Object.class);

                        } else {
                            typeItemNames = mwServerManager.getNames(assetsBaseDTO.getMonitorServerId(), assetsBaseDTO.getAssetsId(), null, items.get(0).getName(), false);
                        }
                        if (typeItemNames != null && typeItemNames.size() > 0) {
                            //初始化第一个TypeItemName(其中对象可能是字符串也有可能是DropDownNamesDesc对象)
                            param.setTypeItemName(typeItemNames.get(0) instanceof String ? typeItemNames.get(0).toString() :
                                    (JSONObject.parseObject(typeItemNames.get(0).toString(), DropDownNamesDesc.class)).getName());
                        }
                    }
                }
                List<String> itemNames = new ArrayList<>();
                for (ItemApplication item : items) {
                    String name = "[" + param.getTypeItemName() + "]" + item.getName();
                    itemNames.add(bracketFlag ? name : item.getName());
                }
                param.setItemNames(itemNames);
            }
            Map<String, String> itemChName = mwServerManager.getItemChName(param.getItemNames());
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(assetsBaseDTO.getMonitorServerId(), param.getItemNames(), assetsBaseDTO.getAssetsId());
            //根据主机查询对应的监控项信息
            if(result!=null && result.getCode() == 0) {
                items = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
            }
            items.forEach(itemApplication -> {
                String s = itemChName.get(itemApplication.getName());

                itemApplication.setChName((s == null || StringUtils.isEmpty(s)) ? itemApplication.getName() : s);
            });
            param.setItemApplicationList(items);
            if (param.getDateType() != null) {
                Calendar calendar = Calendar.getInstance();
                param.setDateEnd(DateUtils.formatDateTime(calendar.getTime()));
                switch (param.getDateType()) {//1：hour 2:day 3:week 4:month
                    case 1:
                        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                        param.setDateStart(DateUtils.formatDateTime(calendar.getTime()));
                        break;
                    case 2:
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                        param.setDateStart(DateUtils.formatDateTime(calendar.getTime()));
                        break;
                    case 3:
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 7);
                        param.setDateStart(DateUtils.formatDateTime(calendar.getTime()));
                        break;
                    case 4:
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
                        param.setDateStart(DateUtils.formatDateTime(calendar.getTime()));
                        break;
                    default:
                        break;
                }
            }
        }
        return param;
    }

    /**
     * 高级表格 监控接口、监控项查询
     *
     * @param param
     * @param applicationName
     * @param itemNameList
     * @param itemNames
     * @param mapList
     */
    private void getDataInfoBydeviceName(AdvanceTableDTO param, String
            applicationName, List<String> itemNameList, List<String> itemNames, List<String> devNames, List<Map<String, Object>> mapList) {
        long time3 = System.currentTimeMillis();
        logger.info("高级表格itemNames数据量：" + itemNames.size());
        int coreSizePool = 15;
        coreSizePool = (coreSizePool > itemNames.size()) ? itemNames.size() : coreSizePool;
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, 18, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Future<MWZabbixAPIResult>> futureList = new ArrayList<>();
        List<MWZabbixAPIResult> listInfo = new ArrayList<>();
        try {
            for (String itemName : itemNames) {
                Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                    @Override
                    public MWZabbixAPIResult call() throws Exception {
                        MWZabbixAPIResult results = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), param.getHostid(), applicationName, itemName);
                        return results;
                    }
                };
                Future<MWZabbixAPIResult> submit = executorService.submit(callable);
                futureList.add(submit);
            }
            if (futureList.size() > 0) {
                futureList.forEach(f -> {
                    try {
                        MWZabbixAPIResult result = f.get(20, TimeUnit.SECONDS);
                        listInfo.add(result);
                    } catch (Exception e) {
                        log.error("fail to getDataInfoBydeviceName:多线程等待数据返回失败 param:{},cause:{}", param, e);
                    }
                });
            }
            executorService.shutdown();
            logger.info("关闭线程池");
            long time4 = System.currentTimeMillis();
            List<RequestZabbixParam> lists = new ArrayList<>();
            //数据合并
            for (MWZabbixAPIResult results : listInfo) {
                if (results.getCode() == 0) {
                    JsonNode data = (JsonNode) results.getData();
                    List<RequestZabbixParam> lists01 = JSONObject.parseArray(String.valueOf(data), RequestZabbixParam.class);
                    lists.addAll(lists01);
                }
            }
            //获取valuemapIds，需要映射的值进行对应转换
            List<String> valuemapIds = new ArrayList<>();
            for (RequestZabbixParam requestZabbixParam : lists) {
                //监控接口
                String valuemapid = requestZabbixParam.getValuemapid();
                valuemapIds.add(valuemapid);
            }
            List<String> valuemapIdList = valuemapIds.stream().distinct().collect(Collectors.toList());
            //映射值进行转换
            Map<String, Map> valueMapByIdMap = mwServerManager.getValueMapByIdList(param.getMonitorServerId(), valuemapIdList);

            for (String devNameInfo : devNames) {
                Map valMap = new HashMap();
                Map<String, Map> map = new HashMap();
                log.error("获取监控项集合：" + lists);
                for (RequestZabbixParam requestZabbixParam : lists) {
                    String name = requestZabbixParam.getName();
                    //数据过滤
                    if (name.indexOf(devNameInfo) != -1) {
                        String strName;//监控项名称
                        String devName;//监控接口名称
                        String lastValue = requestZabbixParam.getLastvalue();
                        String valueType = requestZabbixParam.getValueType();
                        String valuemapid = requestZabbixParam.getValuemapid();
                        Map mapMory = new HashMap();
                        mapMory.put("oldValue", lastValue);
                        mapMory.put("valueType", valueType);
                        if (NumberUtils.isNumber(lastValue)) {
                            BigDecimal bValue = new BigDecimal(lastValue);
                            if (lastValue.indexOf(".") != -1) {
                                lastValue = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                            }
                        }
                        String units = requestZabbixParam.getUnits();
                        log.info("高级表格数值单位类型：" + lastValue + ";" + units + ";" + valueType);
                        if ("b".equals(units)) {
                            units = "B";
                        }
                        if (!Strings.isNullOrEmpty(units)) {//如果有单位，进行单位转换
                            lastValue = UnitsUtil.getValueWithUnits(lastValue, units);
                        }
                        if (name.indexOf("<") != -1 && name.indexOf("[") != -1) {
                            devName = name.substring(0, name.indexOf(">"));
                            strName = name.substring(name.indexOf(">") + 1);
                        } else if (name.indexOf("<") == -1 && name.indexOf("[") != -1) {
                            devName = name.substring(1, name.lastIndexOf("]"));
                            strName = name.substring(name.lastIndexOf("]") + 1);
                        } else {
                            strName = name;
                            devName = "";
                        }//时间单位 数值处理
                        if ("uptime".equals(units)) {
                            Long longValue = getNumByStr(lastValue);
                            lastValue = SeverityUtils.getLastTime(longValue);
                        }
                        if ("unixtime".equals(units)) {
                            Long longValue = getNumByStr(lastValue);
                            if (longValue < 9999999999l) {
                                lastValue = DateUtils.formatDateTime(new Date(longValue * 1000L));
                            } else {
                                lastValue = DateUtils.formatDateTime(new Date(longValue));
                            }

                        }
                        if (valueMapByIdMap != null && valueMapByIdMap.size() > 0 &&
                                valueMapByIdMap.get(valuemapid) != null && valueMapByIdMap.get(valuemapid).get(lastValue) != null) {
                            String newvalue = valueMapByIdMap.get(valuemapid).get(lastValue).toString();
                            if (!Strings.isNullOrEmpty(newvalue)) {
                                lastValue = newvalue;
                            }
                        }
                        mapMory.put("lastValue", lastValue);
                        if (map.containsKey(devName)) {
                            Map maps = map.get(devName);
                            maps.put(strName, mapMory);
                            map.put(devName, maps);
                        } else {
                            valMap = new HashMap();
                            valMap.put("devName", devName);
                            valMap.put(strName, mapMory);
                            map.put(devName, valMap);
                        }
                    }
                }
                map.forEach((k, v) -> {
                    mapList.add(v);
                });
            }
            long time5 = System.currentTimeMillis();
        } catch (Exception e) {
            log.error("高级表格监控接口:监控项查询失败", e);
        }

        ////System.out.println("高级表格多线程请求zabbix数据耗时：" + (time4 - time3) + "ms；数据处理时间：" + (time5 - time4) + "ms；数据数量：" + mapList.size());
    }

    /**
     * 高级表格数据获取 仅监控项查询
     *
     * @param param
     * @param applicationName
     * @param itemNameList
     * @param mapList
     */
    private void getDataInfoByitemName(AdvanceTableDTO param, String
            applicationName, List<String> itemNameList, List<Map<String, Object>> mapList) {
        MWZabbixAPIResult results = mwtpServerAPI.itemGetbySearchNames(param.getMonitorServerId(), applicationName, itemNameList, param.getHostid());
        if (results !=null && results.getCode() == 0) {
            JsonNode data = (JsonNode) results.getData();
            boolean isFlag = true;
            //监控接口
            Map valMap = new HashMap();
            List<String> valuemapIds = new ArrayList<>();
            for (JsonNode itemName : data) {
                String valuemapid = itemName.get("valuemapid").asText();
                valuemapIds.add(valuemapid);
            }
            List<String> valuemapIdList = valuemapIds.stream().distinct().collect(Collectors.toList());
            Map<String, Map> valueMapByIdMap = mwServerManager.getValueMapByIdList(param.getMonitorServerId(), valuemapIdList);
            log.error("获取监控项集合：" + data);
            Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
            for (JsonNode itemName : data) {
                String name = itemName.get("name").asText();
                String strName;//监控项名称
                String lastValue = itemName.get("lastvalue").asText();
                String valuemapid = itemName.get("valuemapid").asText();
                String units = itemName.get("units").asText();
                log.info("高级表格数值单位类型：" + lastValue + ";" + units);
                boolean isNum = pattern.matcher(lastValue).matches();
                if (lastValue.indexOf(".") != -1 && isNum) {
                    BigDecimal bValue = new BigDecimal(lastValue);
                    lastValue = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                }
                if ("b".equals(units)) {
                    units = "B";
                }
                NewUnits infoByUnits = NewUnits.getInfoByUnits(units);
                if (!Strings.isNullOrEmpty(units) && infoByUnits != null) {//如果有单位，进行单位转换
                    lastValue = UnitsUtil.getValueWithUnits(lastValue, units);
                }
                strName = name;
                String devName = "";
                //持续运行时间 数值处理
                if ("uptime".equals(units)) {
                    Long longValue = getNumByStr(lastValue);
                    lastValue = SeverityUtils.getLastTime(longValue);
                }
                if ("unixtime".equals(units)) {
                    Long longValue = getNumByStr(lastValue);
                    if (longValue < 9999999999l) {
                        lastValue = DateUtils.formatDateTime(new Date(longValue * 1000L));
                    } else {
                        lastValue = DateUtils.formatDateTime(new Date(longValue));
                    }
                }
                if (valueMapByIdMap != null && valueMapByIdMap.size() > 0 &&
                        valueMapByIdMap.get(valuemapid) != null && valueMapByIdMap.get(valuemapid).get(lastValue) != null) {
                    String newvalue = valueMapByIdMap.get(valuemapid).get(lastValue).toString();
                    if (!Strings.isNullOrEmpty(newvalue)) {
                        lastValue = newvalue;
                    }
                }
                valMap.put(strName, lastValue);
            }
            mapList.add(valMap);
        }
    }

    /**
     * 高级表格查询所有应用集list
     *
     * @param param
     * @return
     */
    @Override
    public Reply getApplicationList(QueryAdvanceTableParam param) {
        //获取应用集list
        List<Map<String, String>> list = applicationNameList(param);
        return Reply.ok(list);
    }

    private List<Map<String, String>> applicationNameList(QueryAdvanceTableParam param) {
        //获取应用集list
        MWZabbixAPIResult result = mwtpServerAPI.getApplication(param.getMonitorServerId(), param.getHostid());
        List<cn.mw.monitor.server.serverdto.ApplicationDTO> appList = resultResolver
                .analysisResult(mwtpServerAPI.getServerType(param.getMonitorServerId()), String.valueOf(result.getData()));
        List<Map<String, String>> list = new ArrayList<>();
        for (cn.mw.monitor.server.serverdto.ApplicationDTO app : appList) {
            Map<String, String> map = new HashMap();
            map.put("EnName", app.getName());
            map.put("ChName", app.getChName());
            map.put("id", app.getItemid());
            list.add(map);
        }
        return list;
    }

    /**
     * 高级表格根据应用集查询监控项和监控设备list
     *
     * @param param
     * @return
     */
    @Override
    public Reply getItemListByApplication(QueryAdvanceTableParam param) {
        Map<String, Object> map = getItemListMapByApplication(param);
        return Reply.ok(map);
    }

    private Map<String, Object> getItemListMapByApplication(QueryAdvanceTableParam param) {
        //获取监控项List 和监控设备list
        MWZabbixAPIResult results = mwtpServerAPI.getItemNameByAppName(param.getMonitorServerId(), param.getHostid(), param.getApplicationName(), "");
        Set<String> strNameSet = new HashSet();
        Set<String> devNameSet = new HashSet();
        Map<String, Object> map = new HashMap();
        List<Map<String, String>> mapNameList = new ArrayList<>();
        List<String> strNameList = new ArrayList<>();
        List<Map<String, String>> oldNameList = new ArrayList<>();
        List<String> oldNameAllList = new ArrayList<>();
        List<String> oldDevList = new ArrayList<>();
        List<String> devNameList = new ArrayList<>();
        if (results != null && results.getCode() == 0) {
            JsonNode data = (JsonNode) results.getData();
            data.forEach(itemName -> {
                String name = itemName.get("name").asText();
                //例: [2C C5 D3 3B 6D 00 ,BLUEMOON-WIFI]<10.3.48.154>MW_CLIENTS_CHANNELS
                //[2C C5 D3 3B 6D 00 ,BLUEMOON-WIFI]<10.3.48.154>为监控设备名称   MW_CLIENTS_CHANNELS监控项名称
                String strName = name; //监控项名称
                Map mapOldName = new HashMap<>();
                String chStrNameByOld = mwServerManager.getChName(strName);
                mapOldName.put("ChStrName", chStrNameByOld);
                mapOldName.put("EnStrName", strName);
                oldNameList.add(mapOldName);
                String devName = ""; //监控设备名称
                if (name.indexOf("<") != -1 && name.indexOf("[") != -1) {
                    devName = name.substring(0, name.indexOf(">") + 1);
                    strName = name.substring(name.indexOf(">") + 1);
                } else if (name.indexOf("<") == -1 && name.indexOf("[") != -1) {
                    devName = name.substring(0, name.lastIndexOf("]") + 1);
                    strName = name.substring(name.lastIndexOf("]") + 1);
                }
                if (!Strings.isNullOrEmpty(devName)) {
                    oldNameAllList.add(devName);
                }
                if (!strNameSet.contains(strName)) {
                    Map mapName = new HashMap<>();
                    String chStrName = mwServerManager.getChName(strName);
                    strNameList.add(strName);
                    mapName.put("ChStrName", chStrName);
                    mapName.put("EnStrName", strName);
                    mapNameList.add(mapName);
                }
                if (!devNameSet.contains(devName) && !Strings.isNullOrEmpty(devName)) {
                    devNameList.add(devName);
                }
                strNameSet.add(strName);
                if (!Strings.isNullOrEmpty(devName)) {
                    devNameSet.add(devName);
                }
            });
        }

        if (devNameList != null && devNameList.size() != 0) {
            Collections.sort(devNameList, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        }
        if (oldNameAllList.size() != ((ArrayNode) results.getData()).size()) {
            map.put("itemNameMap", oldNameList);
            map.put("devNameMap", oldDevList);
        } else {
            map.put("itemNameMap", mapNameList);
            map.put("devNameMap", devNameList);
        }
        return map;
    }


    /**
     * 获取模板所有自动发现规则中的监控项
     *
     * @param baseDTO
     * @return
     */
    public List<ItemApplication> getRuleItemPrototypes(AssetsBaseDTO baseDTO, Map<String, Object> filter, List<String> ruleIds) {
        List<ItemApplication> list = new ArrayList<>();
        if (ruleIds == null || ruleIds.size() <= 0) { //判断是不是已经有筛选条件 自动发现规则id，如果没有就查询模板所有的自动发现规则id
            MWZabbixAPIResult ruleByHostId = mwtpServerAPI.getDRuleByHostId(baseDTO.getMonitorServerId(), baseDTO.getAssetsId());
            if (!ruleByHostId.isFail()) {
                ruleIds = ruleIds == null ? new ArrayList<>() : ruleIds;
                JsonNode ruleData = (JsonNode) ruleByHostId.getData();
                if (ruleData.size() > 0) {
                    for (JsonNode rule : ruleData) {
                        String ruleId = rule.get("itemid").asText();
                        ruleIds.add(ruleId);
                    }
                }
            }
        }
        for (String ruleId : ruleIds) {
            MWZabbixAPIResult itemPrototype = mwtpServerAPI.getItemPrototypeFilter(baseDTO.getMonitorServerId(), ruleId, filter);
            List<ItemApplication> items = JSONArray.parseArray(String.valueOf(itemPrototype.getData()), ItemApplication.class);
            if (items != null && items.size() > 0) {
                items.forEach(item -> {
                    if (item.getName().indexOf("[") != -1) {
                        item.setName(item.getName().substring(item.getName().indexOf("]") + 1));
                    }
                    item.setDiscoverId(ruleId);
                    item.setChName(mwServerManager.getChName(item.getName()));
                });
                list.addAll(items);
            }
        }
        return list;
    }

    private Long getNumByStr(String str) {
        Long intNum = 0l;
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String strNum = m.replaceAll("").trim();
        if (!Strings.isNullOrEmpty(strNum)) {
            intNum = Long.valueOf(strNum);
        }
        return intNum;
    }
}
