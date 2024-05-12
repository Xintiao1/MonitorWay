package cn.mw.monitor.server.service.impl;

import cn.joinhealth.monitor.zbx.utils.Utils;
import cn.mw.monitor.TPServer.service.MwTPServerService;
import cn.mw.monitor.assets.dao.MwAssetsInterfaceDao;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.server.constant.ServerConstant;
import cn.mw.monitor.server.dao.ItemNameDao;
import cn.mw.monitor.server.dao.MwMyMonitorDao;
import cn.mw.monitor.server.dao.ServerAssetsDao;
import cn.mw.monitor.server.dao.TangibleOutbandDao;
import cn.mw.monitor.server.param.AddOrUpdateComLayoutParam;
import cn.mw.monitor.server.serverdto.ApplicationDTO;
import cn.mw.monitor.server.serverdto.*;
import cn.mw.monitor.server.service.MwMyMonitorService;
import cn.mw.monitor.server.service.ResultResolver;
import cn.mw.monitor.server.util.ListDeepCopy;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.DiscDto;
import cn.mw.monitor.service.alert.dto.ItemData;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.AssetsInterfaceDTO;
import cn.mw.monitor.service.assets.model.ModelInterfaceDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.model.RedisItemHistoryDto;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.link.dto.MwLinkInterfaceDto;
import cn.mw.monitor.service.link.service.MWNetWorkLinkCommonService;
import cn.mw.monitor.service.model.param.MwModelFilterInterfaceParam;
import cn.mw.monitor.service.model.param.QueryEsParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.model.util.ExportTxtUtil;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.MyMonitorCommons;
import cn.mw.monitor.service.server.api.dto.*;
import cn.mw.monitor.service.server.param.*;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.webmonitor.model.MwHistoryDTO;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.util.ListSortUtil;
import cn.mw.monitor.util.MwVisualizedDateUtil;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.dto.ItemHistoryDto;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.server.serverdto.InterfaceInfoEnum.INTERFACE_DESCR;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;
import static cn.mw.monitor.service.virtual.dto.VirtualizationType.VCENTER;
import static cn.mwpaas.common.enums.DateUnitEnum.SECOND;


/**
 * @author xhy
 * @date 2020/4/26 15:26
 */

@Service
@Slf4j
public class MwServerServiceImpl implements MwServerService {
    private static final Logger logger = LoggerFactory.getLogger(MwServerServiceImpl.class);

    @Value("${server.historyByItemIds.timeInterval}")
    private int historyByItemIdsTimeInterval;

    @Value("${server.interface.filter}")
    private String isFilter;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;
    @Autowired
    private MwServerManager mwServerManager;
    @Autowired
    private MyMonitorCommons myMonitorCommons;
    @Autowired
    private MwMyMonitorService mwMymonitorService;
    private static final int insBatchFetchNum = 900;

    private static final int itemIdGroupSize = 11000;
    @Resource
    private MwModelViewCommonService mwModelViewCommonService;
    @Resource
    private ItemNameDao itemNameDao;
    @Resource
    private MwMyMonitorDao myMonitorDao;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MWWebZabbixManger zabbixManger;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;


    @Value("${interface.assets.size}")
    private int assetsSize;

    @Value("${server.group.count}")
    private int groupCount;

    @Resource
    private ServerAssetsDao assetsDao;
    @Resource
    private TangibleOutbandDao tangibleOutbandDao;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MwAssetsInterfaceDao mwAssetsInterfaceDao;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MwZabbixHistoryDataHandle zabbixHistoryDataHandle;

    @Autowired
    private MwTPServerService tpServerService;

    @Autowired
    private ResultResolver resultResolver;

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MwTangibleAssetsService tangibleAssetsService;

    private final String ABNORMAL = "ABNORMAL";//异常
    private final String NORMAL = "NORMAL";//正常
    private final String UNKNOWN = "UNKNOWN";//未知
    private final String SHUTDOWN = "SHUTDOWN";//宕机
    private final String VMXNET3 = "vmxnet3";//

    @Autowired
    private MWNetWorkLinkCommonService linkCommonService;

//    @Override
//    public Reply getHistoryData(ServerHistoryDto serverHistoryDto) {
//        //通过hostid获得不同的itemid 然后查询itemid的history
//        try {
//            List<HistoryListDto> lists;
//            if (!"CPU_UTILIZATION".equals(serverHistoryDto.getName().get(0))) {
//                List<String> names = serverHistoryDto.getName();
//                Map<String, String> itemChName = mwServerManager.getItemChName(names);
//                List<ItemApplication> itemList = new ArrayList<>();
//                for (String name : names) {
//                    MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyType(serverHistoryDto.getMonitorServerId(), name, serverHistoryDto.getAssetsId(), false);
//                    JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
//                    if (node.size() > 0) {
//                        ItemApplication itemApplication = new ItemApplication();
//                        itemApplication.setItemid(node.get(0).get("itemid").asText());
//                        itemApplication.setValue_type(node.get(0).get("value_type").asText());
//                        itemApplication.setName(name);
//                        itemApplication.setUnits(node.get(0).get("units").asText());
//                        itemApplication.setDelay(node.get(0).get("delay").asText());
//                        itemApplication.setChName(itemChName.get(name));
//                        itemList.add(itemApplication);
//                    }
//                }
//                serverHistoryDto.setItemApplicationList(itemList);
//                lists = getLineChartHistory(serverHistoryDto);
//            } else {
//                lists = getHistory(serverHistoryDto);
//            }
//            return Reply.ok(lists);
//        } catch (Exception e) {
//            log.error("fail to getHistoryData with serverHistoryDto={}, cause:{}", serverHistoryDto, e);
//            return Reply.fail(ErrorConstant.SERVER_HISTORY_CODE_302002, ErrorConstant.SERVER_HISTORY_MSG_302002);
//        }
//
//    }

    //    修改版  qzg
    @Override
    public Reply getHistoryData(ServerHistoryDto serverHistoryDto) {
        String assetsStatus = serverHistoryDto.getAssetsStatus();
        if (StringUtils.isNotBlank(assetsStatus) && !NORMAL.equals(assetsStatus)) {
            //异常资产不返回数据
            return Reply.ok(new ArrayList<>());
        }
        List list = zabbixHistoryDataHandle.handleHistory(serverHistoryDto);
        if (serverHistoryDto.getIsTrend() != null && serverHistoryDto.getIsTrend() && serverHistoryDto.getDateType() != 1)
            return Reply.ok(list);
        logger.info("进入历史折线图数据接口！");
        try {
            List<List<Map>> mapList = new ArrayList();
            List<String> names = new ArrayList<>();
            List<String> items = new ArrayList<>();
            long startTime1 = System.currentTimeMillis();

            //该接口目前只支持同一种数据类型的请求。即valType[] 中的值是相同的，所以取第一条数据作为value_type
            List<ItemApplication> itemApplicationList = serverHistoryDto.getItemApplicationList();
            if ((itemApplicationList != null && itemApplicationList.size() != 0)) {//根据ItemName获取
                for (ItemApplication itemInfo : itemApplicationList) {
                    items.add(itemInfo.getItemid());
                    names.add(itemInfo.getName());
                }
            } else {
                names = serverHistoryDto.getName();
                items = serverHistoryDto.getItemId();
            }
            //判断是否是itemId 查询
            boolean isFlag = false;
            MWZabbixAPIResult mwZabbixAPIResult;
            //获取网站的下载速度和响应时间 1:下载速度,2:响应时间
            if (!Strings.isNullOrEmpty(serverHistoryDto.getWebName()) && !Strings.isNullOrEmpty(serverHistoryDto.getTitleType())) {
                String key = null;
                if (serverHistoryDto.getTitleType().equals("1")) {
                    key = "web.test.in[" + serverHistoryDto.getWebName() + "," + serverHistoryDto.getWebName() + "," + "bps]";
                } else if (serverHistoryDto.getTitleType().equals("2")) {
                    key = "web.test.time[" + serverHistoryDto.getWebName() + "," + serverHistoryDto.getWebName() + "," + "resp]";
                }
                Assert.notNull(key, ErrorConstant.WEBMONITOR_MSG_301008);
                Assert.notNull(serverHistoryDto.getAssetsId(), ErrorConstant.WEBMONITOR_MSG_301008);
                Assert.notNull(serverHistoryDto.getMonitorServerId(), ErrorConstant.WEBMONITOR_MSG_301008);
                String hostId = serverHistoryDto.getAssetsId();
                Integer monitorServerId = serverHistoryDto.getMonitorServerId();
                List<String> hostids = new ArrayList<>();
                hostids.add(hostId);
                mwZabbixAPIResult = mwtpServerAPI.getWebItemId(monitorServerId, hostids, key);
            } else {
                //根据监控项获取最新数据信息。
                if (items != null && items.size() > 0) {//根据ItemId获取
                    mwZabbixAPIResult = mwtpServerAPI.itemGetbyItemidList(serverHistoryDto.getMonitorServerId(), items, serverHistoryDto.getAssetsId(), false);
                    isFlag = true;
                } else if ((items == null || items.size() == 0) && (names != null && names.size() != 0)) {//根据ItemName获取
                    mwZabbixAPIResult = mwtpServerAPI.itemGetbySearch(serverHistoryDto.getMonitorServerId(), names, serverHistoryDto.getAssetsId());
                    isFlag = false;
                } else {
                    String strMessage = "监控项名称和监控项Id不可同时为空！";
                    return Reply.fail(strMessage);
                }
            }
            Date date = new Date();
            long endTime = date.getTime() / 1000;
            //开始时间，默认是一小时前
            long startTime = DateUtils.addHours(date, -1).getTime() / 1000;

            logger.info("则线图：DateType()：" + serverHistoryDto.getDateType() + "；则线图：DateStart()" + serverHistoryDto.getDateStart() + "；则线图：DateEnd()" + serverHistoryDto.getDateEnd());
            Date startDate = DateUtils.addHours(date, -1);
            Date endDate = date;

            if (!Strings.isNullOrEmpty(serverHistoryDto.getStartTime())) {
                startDate = DateUtils.parse(serverHistoryDto.getStartTime());
            }
            if (!Strings.isNullOrEmpty(serverHistoryDto.getEndTime())) {
                endDate = DateUtils.parse(serverHistoryDto.getEndTime());
            }

            //为了兼容更多接口的参数，DateStart、DateEnd也作为开始时间结束时间传入。
            if (serverHistoryDto.getDateType() == 5 && serverHistoryDto.getDateStart() != null) {
                if (serverHistoryDto.getDateStart().length() == 24) {
                    startDate = parseByTime(serverHistoryDto.getDateStart());
                } else {
                    startDate = DateUtils.parse(serverHistoryDto.getDateStart());
                }
                logger.info("则线图3333333：" + startDate);
            }
            if (serverHistoryDto.getDateType() == 5 && serverHistoryDto.getDateEnd() != null) {
                if (serverHistoryDto.getDateEnd().length() == 24) {
                    endDate = parseByTime(serverHistoryDto.getDateEnd());
                } else {
                    endDate = DateUtils.parse(serverHistoryDto.getDateEnd());
                }
                logger.info("则线图44444444：" + endDate);
            }
            if (serverHistoryDto.getDateType() != null) {
                switch (serverHistoryDto.getDateType()) {//1：hour 2:day 3:week 4:month
                    case 1:
                        startTime = DateUtils.addHours(date, -1).getTime() / 1000;
                        logger.info("开始执行最近一小时数据查询" + new Date());
                        break;
                    case 2:
                        startTime = DateUtils.addDays(date, -1).getTime() / 1000;
                        break;
                    case 3:
                        startTime = DateUtils.addWeeks(date, -1).getTime() / 1000;
                        break;
                    case 4:
                        startTime = DateUtils.addMonths(date, -1).getTime() / 1000;
                        break;
                    case 5:
                        startTime = (startDate.getTime()) / 1000;
                        endTime = (endDate.getTime()) / 1000;
                        break;
                    default:
                        break;
                }
            }

            logger.info("折线图获取时间qqqqqqqqqDateType()：" + serverHistoryDto.getDateType() + "；startTime：" + startTime + "；endTime：" + endTime);
            logger.info("折线图获取最新数据mwZabbixAPIResult：" + mwZabbixAPIResult);

            JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
            //数据类型集合
            List<Integer> valType4 = new ArrayList<>();
            //数据类型集合
            List<Integer> valType3 = new ArrayList<>();
            //数据类型集合
            List<Integer> valType0 = new ArrayList<>();
            //itemid集合
            List<String> itemlist4 = new ArrayList<>();
            //itemid集合
            List<String> itemlist3 = new ArrayList<>();
            //itemid集合
            List<String> itemlist0 = new ArrayList<>();
            //itemName集合
            List<String> nameList4 = new ArrayList<>();
            //itemName集合
            List<String> nameList3 = new ArrayList<>();
            //itemName集合
            List<String> nameList0 = new ArrayList<>();
            //数据类型为0的CPU、内存集合
            List<String> itemlist0Cpu = new ArrayList<>();
            List<String> nameList0Cpu = new ArrayList<>();
            Map<String, HistoryListDto> mapInfo0Cpu = new HashMap();


            //数据类型为3的CPU、内存集合
            List<String> itemlist3Cpu = new ArrayList<>();
            List<String> nameList3Cpu = new ArrayList<>();
            Map<String, HistoryListDto> mapInfo3Cpu = new HashMap();

            Map<String, HistoryListDto> mapInfo0 = new HashMap();


            Map<String, HistoryListDto> mapInfo3 = new HashMap();
            Map<String, HistoryListDto> mapInfo4 = new HashMap();
            final boolean isFlags = isFlag;
            if (node.size() > 0) {
                node.forEach(item -> {
                    HistoryListDto historyListDto = new HistoryListDto();
                    historyListDto.setUnit(item.get("units").asText());
                    String name = (item.get("name").asText());
                    //获取监控项的母模板itemId
                    String masterItemId = "";
                    if (item.get("master_itemid") != null) {
                        masterItemId = item.get("master_itemid").asText();
                    }
                    //数据获取间隔
                    historyListDto.setDelay(item.get("delay").asText());
                    if (!Strings.isNullOrEmpty(item.get("delay").asText()) && !"0".equals(item.get("delay").asText())) {
                        historyListDto.setDelay(item.get("delay").asText());
                    } else if (!Strings.isNullOrEmpty(masterItemId)) {
                        // master_itemid
                        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyItemidList(serverHistoryDto.getMonitorServerId(), Arrays.asList(masterItemId), serverHistoryDto.getAssetsId(), false);
                        JsonNode node1 = (JsonNode) result.getData();
                        if (node1 != null) {
                            historyListDto.setDelay(node1.get(0).get("delay").asText());
                        }
                    } else {
                        //从数据库中找出itemName对应的母模板的名称，再次从zabbix接口中获取。
                        String parentItemName = itemNameDao.getParentItemName(name);
                        MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverHistoryDto.getMonitorServerId(), parentItemName, serverHistoryDto.getAssetsId());
                        JsonNode node1 = (JsonNode) result.getData();
                        if (node1.size() > 0) {
                            historyListDto.setDelay(node1.get(0).get("delay").asText());
                        }
                    }
                    if (name.indexOf("Download speed for step") != -1 && serverHistoryDto.getTitleType().equals("1")) {
                        historyListDto.setTitleName("网站下载速度");
                    } else if (name.indexOf("Response time for step") != -1 && serverHistoryDto.getTitleType().equals("2")) {
                        historyListDto.setTitleName("网站响应时间");
                    } else {
                        historyListDto.setTitleName(mwServerManager.getChName(name));
                    }
                    historyListDto.setLastUpdateTime(SeverityUtils.getDate(new Date()));
                    if (item.get("value_type").asInt() == 4 || item.get("value_type").asInt() == 1) {
                        itemlist4.add(item.get("itemid").asText());
                        valType4.add(item.get("value_type").asInt());
                        nameList4.add(name);
                        mapInfo4.put(item.get("itemid").asText(), historyListDto);
                    } else if (item.get("value_type").asInt() == 3) {
                        valType3.add(item.get("value_type").asInt());
                        //如果为itemId 查询，则为精确查询，查询监控为独一的，不需要取各自的平均值
                        //cpu、MEMORY多个监控项取值时，去除[CPU]CPU_UTILIZATION、[MEMORY]MEMORY_UTILIZATION项。
                        boolean cpuFlag = (name).indexOf("CPU_UTILIZATION") != -1 && (name).indexOf("[CPU]CPU_UTILIZATION") == -1;
                        boolean memoryFlag = (name).indexOf("MEMORY_UTILIZATION") != -1 && (name).indexOf("[MEMORY]MEMORY_UTILIZATION") == -1;
                        if (!isFlags && (cpuFlag || memoryFlag)) {
                            //CPU监控项 memory监控
                            itemlist3Cpu.add(item.get("itemid").asText());
                            nameList3Cpu.add(name);
                            mapInfo3Cpu.put(item.get("itemid").asText(), historyListDto);
                        } else {
                            itemlist3.add(item.get("itemid").asText());
                            nameList3.add(name);
                            mapInfo3.put(item.get("itemid").asText(), historyListDto);
                        }
                    } else if (item.get("value_type").asInt() == 0) {
                        valType0.add(item.get("value_type").asInt());
                        //如果为itemId 查询，则为精确查询，查询监控为独一的，不需要取各自的平均值
                        //cpu、MEMORY多个监控项取值时，去除[CPU]CPU_UTILIZATION、[MEMORY]MEMORY_UTILIZATION项。
                        boolean cpuFlag = (name).indexOf("CPU_UTILIZATION") != -1 && (name).indexOf("[CPU]CPU_UTILIZATION") == -1;
                        boolean memoryFlag = (name).indexOf("MEMORY_UTILIZATION") != -1 && (name).indexOf("[MEMORY]MEMORY_UTILIZATION") == -1;
                        if (!isFlags && (cpuFlag || memoryFlag)) {
                            //CPU监控项 memory监控
                            itemlist0Cpu.add(item.get("itemid").asText());
                            nameList0Cpu.add(name);
                            mapInfo0Cpu.put(item.get("itemid").asText(), historyListDto);
                        } else {
                            itemlist0.add(item.get("itemid").asText());
                            nameList0.add(name);
                            mapInfo0.put(item.get("itemid").asText(), historyListDto);
                        }
                    }

                });
            }
            List<MWItemHistoryDto> mwItemHistoryDtos = new ArrayList<>();
            if (valType0.size() > 0 && valType0.get(0) == 0) {
                logger.info("开始执行查询历史记录111" + new Date());
                //lastValue的类型0：浮点型数字
                if (itemlist0Cpu.size() > 0) {
                    mwItemHistoryDtos = zabbixManger.HistoryGetByTimeAndHistory(serverHistoryDto.getMonitorServerId(), itemlist0Cpu, startTime, endTime, 0, serverHistoryDto.getDateType());
                    mapList.add(getDataByMapByCPU(serverHistoryDto, mwItemHistoryDtos, mapInfo0Cpu));
                }
                if ((itemlist0.size() > 0)) {
                    mwItemHistoryDtos = zabbixManger.HistoryGetByTimeAndHistory(serverHistoryDto.getMonitorServerId(), itemlist0, startTime, endTime, 0, serverHistoryDto.getDateType());
                    List<Map> dataByMap = getDataByMap(serverHistoryDto, mwItemHistoryDtos, mapInfo0);
                    mapList.add(dataByMap);
                }
            }
            if (valType3.size() > 0 && valType3.get(0) == 3) {
                logger.info("开始执行查询历史记录222" + new Date());
                if (itemlist3Cpu.size() > 0) {
                    mwItemHistoryDtos = zabbixManger.HistoryGetByTimeAndHistory(serverHistoryDto.getMonitorServerId(), itemlist3Cpu, startTime, endTime, 3, serverHistoryDto.getDateType());
                    mapList.add(getDataByMapByCPU(serverHistoryDto, mwItemHistoryDtos, mapInfo3Cpu));
                }
                //lastValue的类型为3数字
                if (itemlist3.size() > 0) {
                    logger.info("开始执行查询历史记录333" + new Date());
                    mwItemHistoryDtos = zabbixManger.HistoryGetByTimeAndHistory(serverHistoryDto.getMonitorServerId(), itemlist3, startTime, endTime, 3, serverHistoryDto.getDateType());
                    mapList.add(getDataByMap(serverHistoryDto, mwItemHistoryDtos, mapInfo3));
                }
            }
            if (valType4.size() > 0 && (valType4.get(0) == 4 || valType4.get(0) == 1)) {
                logger.info("开始执行查询历史记录444" + new Date());
                //lastValue的类型为文本类型
                if (itemlist4.size() > 0) {
                    mwItemHistoryDtos = zabbixManger.HistoryGetByTimeAndHistoryCN(serverHistoryDto.getMonitorServerId(), itemlist4, startTime, endTime, valType4.get(0));
                    mapList.add(getDataByMapCN(serverHistoryDto, mwItemHistoryDtos, mapInfo4));
                }
            }
            logger.info("执行查询历史记录555" + mwItemHistoryDtos.size() + new Date());
            logger.info("执行查询历史记录666" + mapList.size() + new Date());
            long endTime8 = System.currentTimeMillis();
            logger.info("总耗时：" + (endTime8 - startTime1) + "ms");
            getItemNameHealthValue(serverHistoryDto.getAssetsId(), serverHistoryDto.getName(), mapList);
            mapList.forEach(s->s.sort(Comparator.comparing(map -> Objects.toString(map.get("titleName"), ""))));
            return Reply.ok(mapList);
        } catch (Throwable e) {
            log.error("执行查询历史记录失败", e);
            log.error("fail to getHistoryData with serverHistoryDto={}, cause:{}", serverHistoryDto, e);
//            return Reply.fail(ErrorConstant.SERVER_HISTORY_CODE_302002, ErrorConstant.SERVER_HISTORY_MSG_302002);
            return Reply.ok();

        }

    }


    /**
     * 查询对应监控项的健康值
     *
     * @param assetsId 资产主机ID
     * @param name     监控项名称集合
     */
    private void getItemNameHealthValue(String assetsId, List<String> name, List mapList) {
        log.info("获取健康值" + assetsId + ":::" + name + "::" + mapList);
        List<Map<String, Object>> healthValue = itemNameDao.getHealthValue(name, assetsId);
        if (CollectionUtils.isNotEmpty(mapList)) {
            for (Object o : mapList) {
                List list = (List) o;
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Object o2 : list) {
                        Map map2 = (Map) o2;
                        map2.put("healthValue", "");
                        map2.put("healthUnit", "");
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(healthValue)) return;
        for (Map<String, Object> map : healthValue) {
            String itemName = (String) map.get("itemName");
            String value = (String) map.get("value");
            if (!itemName.contains("MW_INTERFACE_OUT_TRAFFIC") && !itemName.contains("MW_INTERFACE_IN_TRAFFIC")) {
                List list = (List) mapList.get(0);
                if (CollectionUtils.isNotEmpty(list)) {
                    Map map2 = (Map) list.get(0);
                    map2.put("healthValue", value.replace("%", ""));
                    map2.put("healthUnit", "%");
                }
            }
            if (itemName.contains("MW_INTERFACE_OUT_TRAFFIC")) {
                List list = (List) mapList.get(0);
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Object o : list) {
                        Map map2 = (Map) o;
                        String titleName = (String) map2.get("titleName");
                        if (StringUtils.isNotBlank(titleName) && titleName.contains("每秒发送流量")) {
                            map2.put("healthValue", value.replace("Kbps", ""));
                            map2.put("healthUnit", "Kbps");
                        }
                    }
                }
            }
            if (itemName.contains("MW_INTERFACE_IN_TRAFFIC")) {
                List list = (List) mapList.get(0);
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Object o : list) {
                        Map map2 = (Map) o;
                        String titleName = (String) map2.get("titleName");
                        if (StringUtils.isNotBlank(titleName) && titleName.contains("每秒接收流量")) {
                            map2.put("healthValue", value.replace("Kbps", ""));
                            map2.put("healthUnit", "Kbps");
                        }
                    }
                }
            }
        }
    }

    public Date parseByTime(String str) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        logger.info("进入新的时间转换函数：pattern" + pattern + "；str：" + str);
        if (null == str) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        try {
            return formatter.parse(str);
        } catch (ParseException e) {
            log.error("fail to parseByTime param{}, case by {}", str, e);
        }
        return null;
    }


    //对zabbix获取的中文数据 进行处理
    private List<Map> getDataByMapCN(ServerHistoryDto serverHistoryDto, List<MWItemHistoryDto> mwItemHistoryDtos, Map<String, HistoryListDto> mapInfo) {
        long endTime2 = 0l;
        long endTime3 = 0l;
        long endTime4 = 0l;
        long endTime5 = 0l;
        List<Map> mapList = new ArrayList<>();
        Map<String, Object> map = new HashMap();
        endTime2 = System.currentTimeMillis();
        List<MWItemHistoryDto> lists = new ArrayList<>();
        Set<String> set = mapInfo.keySet();
        if (set != null && set.size() > 0) {
            for (String key : set) {
                lists = new ArrayList<>();
                if (mapInfo.size() == 1) {
                    lists = mwItemHistoryDtos;
                } else {
                    for (MWItemHistoryDto dto : mwItemHistoryDtos) {
                        if (key.equals(dto.getItemid())) {
                            lists.add(dto);
                        }
                    }
                }
                List<MWItemHistoryDto> newLists = lists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(MWItemHistoryDto::getValue))), ArrayList::new));
                HistoryListDto historyListDto = mapInfo.get(key);
                map = new HashMap<>();
                map.put("realData", newLists);
                map.put("unitByReal", historyListDto.getUnit());
                map.put("titleName", historyListDto.getTitleName());
                map.put("lastUpdateTime", historyListDto.getLastUpdateTime());
                mapList.add(map);
            }
        } else {
            map = new HashMap<>();
            List<MWItemHistoryDto> newLists = new ArrayList<>();
            map.put("realData", newLists);
            map.put("unitByReal", "");
            map.put("titleName", "");
            map.put("lastUpdateTime", "");
            mapList.add(map);
        }

        endTime5 = System.currentTimeMillis();

        logger.info("数据分组处理耗时：" + (endTime3 - endTime2) + "ms" +
                "数据极值获取耗时：" + (endTime4 - endTime3) + "ms" +
                "数据处理总耗时：" + (endTime5 - endTime2) + "ms");

        return mapList;
    }


    //对zabbix获取的数据进行处理
    private List<Map> getDataByMap(ServerHistoryDto serverHistoryDto, List<MWItemHistoryDto> mwItemHistoryDtos, Map<String, HistoryListDto> mapInfo) {
        long endTime2 = 0l;
        long endTime3 = 0l;
        long endTime4 = 0l;
        long endTime5 = 0l;
        long endTime6 = 0l;
        long endTime7 = 0l;
        List<List<MWItemHistoryDto>> subList;
        List<MWItemHistoryDto> maxList = new ArrayList<>();
        List<MWItemHistoryDto> minList = new ArrayList<>();
        List<MWItemHistoryDto> avgList = new ArrayList<>();

        //将所有数据放入一个list中，获取lsit的最大值，选取合适的单位
        List<MWItemHistoryDto> realListAll = new ArrayList<>();
        List<MWItemHistoryDto> maxListAll = new ArrayList<>();
        List<MWItemHistoryDto> minListAll = new ArrayList<>();
        List<MWItemHistoryDto> avgListAll = new ArrayList<>();

        List<List<MWItemHistoryDto>> realLists = new ArrayList<>();
        List<List<MWItemHistoryDto>> maxLists = new ArrayList<>();
        List<List<MWItemHistoryDto>> minLists = new ArrayList<>();
        List<List<MWItemHistoryDto>> avgLists = new ArrayList<>();

        List<Map> mapList = new ArrayList<>();
        //真实数据。
        List<MWItemHistoryDto> realList = new ArrayList<>();
        Map<String, Object> map = new HashMap();
        endTime2 = System.currentTimeMillis();
        List<MWItemHistoryDto> lists = new ArrayList<>();
        Set<String> set = mapInfo.keySet();
        HistoryListDto historyListAllDto = new HistoryListDto();
        String unitByMaxAll = "";
        String unitByMinAll = "";
        String unitByAvgAll = "";
        String unitByRealAll = "";
        int x = 0;
        log.info("自定义资产详情查询开始" + new Date());
        if (set != null && set.size() > 0) {
            realListAll = new ArrayList<>();
            maxListAll = new ArrayList<>();
            minListAll = new ArrayList<>();
            avgListAll = new ArrayList<>();
            for (String key : set) {
                endTime3 = System.currentTimeMillis();
                x++;
                realList = new ArrayList<>();
                lists = new ArrayList<>();
                maxList = new ArrayList<>();
                minList = new ArrayList<>();
                avgList = new ArrayList<>();
                for (MWItemHistoryDto dto : mwItemHistoryDtos) {
                    if (key.equals(dto.getItemid())) {
                        lists.add(dto);
                    }
                }
                endTime4 = System.currentTimeMillis();
                HistoryListDto historyListDto = mapInfo.get(key);
                historyListAllDto = historyListDto;
                ////System.out.println("第" + x + "遍" + historyListDto.getTitleName() + "，数据过滤耗时：" + (endTime4 - endTime3) + "ms");
                int delay;
                String delayStr = "";
                if (!Strings.isNullOrEmpty(historyListDto.getDelay())) {
                    delayStr = historyListDto.getDelay();
                }
                String units = "";
                log.info("自定义资产详情查询开始2" + new Date());
                //一小时，一天，自定义时间，取真实数据
                if (serverHistoryDto.getDateType() != null) {
                    units = delayStr.replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
                    switch (serverHistoryDto.getDateType()) {//1：hour 2:day 3:week 4:month
                        case 1:
                            //一小时
                            realList = lists;
                            break;
                        case 2:
                            //一天
                            realList = lists;
                            break;
                        case 3:
                            log.info("自定义资产详情查询单位" + units);
                            //一周 默认为15分钟为一个数据节点。
                            delay = 15;
                            if ("".equals(units) || "md".equals(units) || "wd".equals(units) || "h".equals(units) || "%RH".equals(units) || "IOTINTERVAL".equals(units)) {//取值间隔为月每次，周每次，小时每次，则不需要对最大值最小值平均值处理。
                                maxList = lists;
                                minList = lists;
                                avgList = lists;
                            } else {
                                log.info("自定义资产详情查询周数据5" + delayStr);
                                if (delayStr != null && getInteger(delayStr) != null) {
                                    delay = 15 * 60 / getInteger(delayStr);
                                    //当时间间隔返回值大于15分钟时，分组数据为每条一个节点
                                    if (getInteger(delayStr) > 900) {
                                        delay = 1;
                                    }
                                }
                                log.info("自定义资产详情查询周数据" + new Date());
                                subList = splitList(lists, delay);
                                log.info("自定义资产详情查询周数据2" + new Date());
                                for (List<MWItemHistoryDto> list : subList) {
                                    MWItemHistoryDto maxDto = list.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                                    MWItemHistoryDto mixDto = list.stream().min(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                                    MWItemHistoryDto avgDto = new MWItemHistoryDto();
                                    Double avgVal = list.stream().mapToDouble(mwItemHistoryDto -> Double.valueOf(mwItemHistoryDto.getDoubleValue())).average().getAsDouble();
                                    Double values = avgVal;
                                    avgDto.setValue(String.valueOf(values));
                                    avgDto.setLastValue(values.longValue());
                                    avgDto.setDoubleValue(values);
                                    //取数值最后一条数据的时间。
                                    avgDto.setDateTime(list.get((list.size() - 1)).getDateTime());
                                    maxList.add(maxDto);
                                    minList.add(mixDto);
                                    avgList.add(avgDto);
                                }
                                log.info("自定义资产详情查询周数据3" + new Date());
                            }
                            break;
                        case 4:
                            log.info("自定义资产详情查询2单位" + units);
                            //一个月
                            //获取时间间隔数值
                            delay = 60;
                            if ("".equals(units) || "md".equals(units) || "wd".equals(units) || "h".equals(units) || "%RH".equals(units) || "IOTINTERVAL".equals(units)) {//取值间隔为月每次，周每次，小时每次，则不需要对最大值最小值平均值处理。
                                maxList = lists;
                                minList = lists;
                                avgList = lists;
                            } else {
                                if (getInteger(delayStr) != null) {
                                    delay = 60 * 60 / getInteger(delayStr);
                                    if (getInteger(delayStr) > 3600) {
                                        delay = 1;
                                    }
                                }
                                subList = splitList(lists, delay);
                                for (List<MWItemHistoryDto> list : subList) {
                                    MWItemHistoryDto maxDto = list.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                                    MWItemHistoryDto mixDto = list.stream().min(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                                    MWItemHistoryDto avgDto = new MWItemHistoryDto();
                                    Double avgVal = list.stream().mapToDouble(mwItemHistoryDto -> Double.valueOf(mwItemHistoryDto.getDoubleValue())).average().getAsDouble();
                                    Double values = avgVal;
                                    avgDto.setValue(String.valueOf(values));
                                    avgDto.setDoubleValue(values);
                                    avgDto.setLastValue(values.longValue());
                                    //取数值最后一条数据的时间。
                                    avgDto.setDateTime(list.get((list.size() - 1)).getDateTime());
                                    maxList.add(maxDto);
                                    minList.add(mixDto);
                                    avgList.add(avgDto);
                                }
                            }
                            break;
                        case 5:
                            //自定义
                            realList = lists;
                            break;
                        default:
                            break;
                    }
                    log.info("自定义资产详情查询3" + new Date());
                    endTime5 = System.currentTimeMillis();
                    //添加数据源，为下面for数据准备；多少条数据就分为多少个list
                    realLists.add(realList);
                    maxLists.add(maxList);
                    minLists.add(minList);
                    avgLists.add(avgList);
                    //添加数据流，为下面获取最大值准备；将所有数据放入一个list
                    realListAll.addAll(realList);
                    maxListAll.addAll(maxList);
                    minListAll.addAll(minList);
                    avgListAll.addAll(avgList);
                }
            }
            log.info("自定义资产详情查询开始4" + new Date());
            //多条数据时，根据所有数据的最大值，获取统一的单位。
            boolean isJZ = maxListAll.size() != 0 && minListAll.size() != 0 && avgListAll.size() != 0;
            if (isJZ) {
                //查询最大值，最小值，平均值中的极大值，用于取值单位。
                MWItemHistoryDto maxValDtoByMax = maxListAll.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                MWItemHistoryDto maxValDtoByMin = minListAll.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                MWItemHistoryDto maxValDtoByAvg = avgListAll.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                String maxValByMax = maxValDtoByMax.getValue();
                String maxValByMin = maxValDtoByMin.getValue();
                String maxValByAvg = maxValDtoByAvg.getValue();
                unitByMaxAll = UnitsUtil.getConvertedValue(new BigDecimal(maxValByMax), historyListAllDto.getUnit()).get("units");
                unitByMinAll = UnitsUtil.getConvertedValue(new BigDecimal(maxValByMin), historyListAllDto.getUnit()).get("units");
                unitByAvgAll = UnitsUtil.getConvertedValue(new BigDecimal(maxValByAvg), historyListAllDto.getUnit()).get("units");
            }
            log.info("自定义资产详情查询开始5" + new Date());
            if (realListAll.size() != 0) {
                //取真实值数组的最大值，据此选择合适的单位。
                MWItemHistoryDto maxValDtoByReal = realListAll.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                String maxValByReal = maxValDtoByReal.getValue();
                unitByRealAll = UnitsUtil.getConvertedValue(new BigDecimal(maxValByReal), historyListAllDto.getUnit()).get("units");
            }
            log.info("自定义资产详情查询开始6" + new Date());
            //对数据进行处理，转换为统一的单位
            int y = 0;
            for (String key : set) {
                HistoryListDto historyListDto = mapInfo.get(key);
                boolean isJZ1 = maxLists.size() != 0 && minLists.size() != 0 && avgLists.size() != 0;
                String delayStr = "";
                if (!Strings.isNullOrEmpty(historyListAllDto.getDelay())) {
                    delayStr = historyListAllDto.getDelay();
                }
                if (isJZ1) {
                    for (MWItemHistoryDto dto : maxLists.get(y)) {
                        String v = UnitsUtil.getValueMap(dto.getDoubleValue().toString(), unitByMaxAll, historyListDto.getUnit()).get("value");
                        Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setValue(String.valueOf(values));
                    }
                    for (MWItemHistoryDto dto : minLists.get(y)) {
                        String v = UnitsUtil.getValueMap(dto.getDoubleValue().toString(), unitByMinAll, historyListDto.getUnit()).get("value");
                        Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setValue(String.valueOf(values));
                    }
                    for (MWItemHistoryDto dto : avgLists.get(y)) {
                        String v = UnitsUtil.getValueMap(dto.getDoubleValue().toString(), unitByAvgAll, historyListDto.getUnit()).get("value");
                        Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setValue(String.valueOf(values));
                    }
                }
                if (realLists.size() != 0) {
                    for (MWItemHistoryDto dto : realLists.get(y)) {
                        String v = UnitsUtil.getValueMap(dto.getDoubleValue().toString(), unitByRealAll, historyListDto.getUnit()).get("value");
                        Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        if ("次/s".equals(historyListDto.getUnit())) {
                            dto.setValue(String.valueOf(values.longValue()));
                        } else {
                            dto.setValue(String.valueOf(values));
                        }
                    }
                }
                //使用率  取list值得最后一位最新数据为使用率
                String maxUsring = "";
                String minUsring = "";
                String avgUsring = "";
                if (isJZ) {
                    if (maxLists.size() >= y && maxLists.get(y).size() > 0) {
                        maxUsring = maxLists.get(y).get(maxLists.size() - 1).getValue();
                    }
                    if (minLists.size() >= y && minLists.get(y).size() > 0) {
                        minUsring = minLists.get(y).get(minLists.size() - 1).getValue();
                    }
                    if ((avgLists.size() >= y) && avgLists.get(y).size() > 0 && (avgLists.get(y).size() >= avgLists.size() - 1)) {
                        avgUsring = avgLists.get(y).get(avgLists.size() - 1).getValue();
                    }
                }
                String realUsring = "";
                log.info("组装线路查询数据" + new Date());
                if ((realLists.size() >= y) && realLists.get(y).size() > 0 && (realLists.get(y).size() >= realList.size() - 1)) {
                    realUsring = realLists.get(y).get(realLists.get(y).size() - 1).getValue();
                }
                endTime6 = System.currentTimeMillis();
                ////System.out.println("第" + y + "遍，数据处理耗时：" + (endTime6 - endTime5) + "ms");
                map = new HashMap<>();
                if(CollectionUtils.isNotEmpty(realLists.get(y)) && CollectionUtils.isEmpty(avgLists.get(y))){
                    map.put("avgData", realLists.get(y));
                }else{
                    map.put("avgData", avgLists.get(y));
                }
                map.put("maxData", maxLists.get(y));
                map.put("minData", minLists.get(y));
                map.put("maxUsring", maxUsring);
                map.put("minUsring", minUsring);
                map.put("avgUsring", avgUsring);
                map.put("unitByMax", unitByMaxAll);
                map.put("unitByMin", unitByMinAll);
                map.put("unitByAvg", unitByAvgAll);
                map.put("realData", realLists.get(y));
                map.put("realUsring", realUsring);
                map.put("unitByReal", unitByRealAll);
                map.put("titleName", historyListDto.getTitleName());
                map.put("lastUpdateTime", historyListDto.getLastUpdateTime());
                map.put("delay", delayStr);
                mapList.add(map);
                y++;
            }
        } else {
            map = new HashMap<>();
            if(CollectionUtils.isNotEmpty(realList) && CollectionUtils.isEmpty(avgList)){
                map.put("avgData", realList);
            }else{
                map.put("avgData", avgList);
            }
            map.put("maxData", maxList);
            map.put("minData", minList);
            map.put("maxUsring", "");
            map.put("minUsring", "");
            map.put("avgUsring", "");
            map.put("unitByMax", "");
            map.put("unitByMin", "");
            map.put("unitByAvg", "");
            map.put("realData", realList);
            map.put("realUsring", "");
            map.put("unitByReal", "");
            map.put("titleName", "");
            map.put("lastUpdateTime", "");
            map.put("delay", "");
            mapList.add(map);
        }


        logger.info("getDataByMap数据分组处理耗时：" + (endTime3 - endTime2) + "ms" +
                "getDataByMap数据极值获取耗时：" + (endTime4 - endTime3) + "ms" +
                "getDataByMap数据处理总耗时：" + (endTime5 - endTime2) + "ms");
        return mapList;
    }


//    private List<Map> getDataByMap(ServerHistoryDto serverHistoryDto, List<MWItemHistoryDto> mwItemHistoryDtos, Map<String, HistoryListDto> mapInfo) {
//        long endTime2 = 0l;
//        long endTime3 = 0l;
//        long endTime4 = 0l;
//        long endTime5 = 0l;
//        long endTime6 = 0l;
//        long endTime7 = 0l;
//        List<List<MWItemHistoryDto>> subList;
//        List<MWItemHistoryDto> maxList = new ArrayList<>();
//        List<MWItemHistoryDto> minList = new ArrayList<>();
//        List<MWItemHistoryDto> avgList = new ArrayList<>();
//        List<Map> mapList = new ArrayList<>();
//        //真实数据。
//        List<MWItemHistoryDto> realList = new ArrayList<>();
//        Map<String, Object> map = new HashMap();
//        endTime2 = System.currentTimeMillis();
//        List<MWItemHistoryDto> lists = new ArrayList<>();
//        Set<String> set = mapInfo.keySet();
//        int x = 0;
//        if (set != null && set.size() > 0) {
//            for (String key : set) {
//                endTime3 = System.currentTimeMillis();
//                x++;
//                realList = new ArrayList<>();
//                lists = new ArrayList<>();
//                maxList = new ArrayList<>();
//                minList = new ArrayList<>();
//                avgList = new ArrayList<>();
//                for (MWItemHistoryDto dto : mwItemHistoryDtos) {
//                    if (key.equals(dto.getItemid())) {
//                        lists.add(dto);
//                    }
//                }
//                endTime4 = System.currentTimeMillis();
//                HistoryListDto historyListDto = mapInfo.get(key);
//                ////System.out.println("第" + x + "遍" + historyListDto.getTitleName() + "，数据过滤耗时：" + (endTime4 - endTime3) + "ms");
//                int delay;
//                String delayStr = "";
//                if (!Strings.isNullOrEmpty(historyListDto.getDelay())) {
//                    delayStr = historyListDto.getDelay();
//                }
//                String units = "";
//                //一小时，一天，自定义时间，取真实数据
//                if (serverHistoryDto.getDateType() != null) {
//                    units = delayStr.replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
//                    switch (serverHistoryDto.getDateType()) {//1：hour 2:day 3:week 4:month
//                        case 1:
//                            //一小时
//                            realList = lists;
//                            break;
//                        case 2:
//                            //一天
//                            realList = lists;
//                            break;
//                        case 3:
//                            //一周 默认为15分钟为一个数据节点。
//                            delay = 15;
//                            if ("".equals(units) || "md".equals(units) || "wd".equals(units) || "h".equals(units)) {//取值间隔为月每次，周每次，小时每次，则不需要对最大值最小值平均值处理。
//                                maxList = lists;
//                                minList = lists;
//                                avgList = lists;
//                            } else {
//                                if (delayStr != null && getInteger(delayStr) != null) {
//                                    delay = 15 * 60 / getInteger(delayStr);
//                                    //当时间间隔返回值大于15分钟时，分组数据为每条一个节点
//                                    if(getInteger(delayStr) > 900){
//                                        delay = 1;
//                                    }
//                                }
//                                subList = splitList(lists, delay);
//                                for (List<MWItemHistoryDto> list : subList) {
//                                    MWItemHistoryDto maxDto = list.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                                    MWItemHistoryDto mixDto = list.stream().min(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                                    MWItemHistoryDto avgDto = new MWItemHistoryDto();
//                                    Double avgVal = list.stream().mapToDouble(mwItemHistoryDto -> Double.valueOf(mwItemHistoryDto.getDoubleValue())).average().getAsDouble();
//                                    Double values = new BigDecimal(avgVal).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                                    avgDto.setValue(String.valueOf(values));
//                                    avgDto.setLastValue(values.longValue());
//                                    avgDto.setDoubleValue(values);
//                                    //取数值最后一条数据的时间。
//                                    avgDto.setDateTime(list.get((list.size() - 1)).getDateTime());
//                                    maxList.add(maxDto);
//                                    minList.add(mixDto);
//                                    avgList.add(avgDto);
//                                }
//                            }
//                            break;
//                        case 4:
//                            //一个月
//                            //获取时间间隔数值
//                            delay = 60;
//                            if ("".equals(units) || "md".equals(units) || "wd".equals(units) || "h".equals(units)) {//取值间隔为月每次，周每次，小时每次，则不需要对最大值最小值平均值处理。
//                                maxList = lists;
//                                minList = lists;
//                                avgList = lists;
//                            } else {
//                                if (getInteger(delayStr) != null) {
//                                    delay = 60 * 60 / getInteger(delayStr);
//                                    if(getInteger(delayStr) > 3600){
//                                        delay = 1;
//                                    }
//                                }
//                                subList = splitList(lists, delay);
//                                for (List<MWItemHistoryDto> list : subList) {
//                                    MWItemHistoryDto maxDto = list.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                                    MWItemHistoryDto mixDto = list.stream().min(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                                    MWItemHistoryDto avgDto = new MWItemHistoryDto();
//                                    Double avgVal = list.stream().mapToDouble(mwItemHistoryDto -> Double.valueOf(mwItemHistoryDto.getDoubleValue())).average().getAsDouble();
//                                    Double values = new BigDecimal(avgVal).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                                    avgDto.setValue(String.valueOf(values));
//                                    avgDto.setDoubleValue(values);
//                                    avgDto.setLastValue(values.longValue());
//                                    //取数值最后一条数据的时间。
//                                    avgDto.setDateTime(list.get((list.size() - 1)).getDateTime());
//                                    maxList.add(maxDto);
//                                    minList.add(mixDto);
//                                    avgList.add(avgDto);
//                                }
//                            }
//                            break;
//                        case 5:
//                            //自定义
//                            realList = lists;
//                            break;
//                        default:
//                            break;
//                    }
//                    endTime5 = System.currentTimeMillis();
//                }
//                ////System.out.println("第" + x + "遍，极值分组处理耗时：" + (endTime5 - endTime4) + "ms");
//                String unitByMax = "";
//                String unitByMin = "";
//                String unitByAvg = "";
//                String unitByReal = "";
//                boolean isJZ = maxList.size() != 0 && minList.size() != 0 && avgList.size() != 0;
//                NewUnits infoByUnits = NewUnits.getInfoByUnits(historyListDto.getUnit());
//                if (infoByUnits != null && infoByUnits.getMapKey() >= 0) {//单位需要转换
//                    if (isJZ) {
//                        //查询最大值，最小值，平均值中的极大值，用于取值单位。
//                        MWItemHistoryDto maxValDtoByMax = maxList.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                        MWItemHistoryDto maxValDtoByMin = minList.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                        MWItemHistoryDto maxValDtoByAvg = avgList.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                        String maxValByMax = maxValDtoByMax.getValue();
//                        String maxValByMin = maxValDtoByMin.getValue();
//                        String maxValByAvg = maxValDtoByAvg.getValue();
//                        unitByMax = UnitsUtil.getConvertedValue(new BigDecimal(maxValByMax), historyListDto.getUnit()).get("units");
//                        unitByMin = UnitsUtil.getConvertedValue(new BigDecimal(maxValByMin), historyListDto.getUnit()).get("units");
//                        unitByAvg = UnitsUtil.getConvertedValue(new BigDecimal(maxValByAvg), historyListDto.getUnit()).get("units");
//                        for (MWItemHistoryDto dto : maxList) {
//                            String v = UnitsUtil.getValueMap(dto.getLastValue().toString(), unitByMax, historyListDto.getUnit()).get("value");
//                            Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                            dto.setValue(String.valueOf(values));
//                        }
//                        for (MWItemHistoryDto dto : minList) {
//                            String v = UnitsUtil.getValueMap(dto.getLastValue().toString(), unitByMin, historyListDto.getUnit()).get("value");
//                            Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                            dto.setValue(String.valueOf(values));
//                        }
//                        for (MWItemHistoryDto dto : avgList) {
//                            String v = UnitsUtil.getValueMap(dto.getLastValue().toString(), unitByAvg, historyListDto.getUnit()).get("value");
//                            Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                            dto.setValue(String.valueOf(values));
//                        }
//                    }
//                    if (realList.size() != 0) {
//                        //取真实值数组的最大值，据此选择合适的单位。
//                        MWItemHistoryDto maxValDtoByReal = realList.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
//                        String maxValByReal = maxValDtoByReal.getValue();
//                        unitByReal = UnitsUtil.getConvertedValue(new BigDecimal(maxValByReal), historyListDto.getUnit()).get("units");
//                        for (MWItemHistoryDto dto : realList) {
//                            String v = UnitsUtil.getValueMap(dto.getLastValue().toString(), unitByReal, historyListDto.getUnit()).get("value");
//                            Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                            dto.setValue(String.valueOf(values));
//                        }
//                    }
//
//
//                } else {
//                    unitByMax = historyListDto.getUnit();
//                    unitByMin = historyListDto.getUnit();
//                    unitByAvg = historyListDto.getUnit();
//                    unitByReal = historyListDto.getUnit();
//                }
//                //使用率
//                String maxUsring = "";
//                String minUsring = "";
//                String avgUsring = "";
//                if (isJZ) {
//                    maxUsring = maxList.get(maxList.size() - 1).getValue();
//                    minUsring = minList.get(minList.size() - 1).getValue();
//                    avgUsring = avgList.get(avgList.size() - 1).getValue();
//                }
//                String realUsring = "";
//                if (realList.size() > 0) {
//                    realUsring = realList.get(realList.size() - 1).getValue();
//                }
//                endTime6 = System.currentTimeMillis();
//                ////System.out.println("第" + x + "遍，数据处理耗时：" + (endTime6 - endTime5) + "ms");
//                map = new HashMap<>();
//                map.put("maxData", maxList);
//                map.put("minData", minList);
//                map.put("avgData", avgList);
//                map.put("maxUsring", maxUsring);
//                map.put("minUsring", minUsring);
//                map.put("avgUsring", avgUsring);
//                map.put("unitByMax", unitByMax);
//                map.put("unitByMin", unitByMin);
//                map.put("unitByAvg", unitByAvg);
//                map.put("realData", realList);
//                map.put("realUsring", realUsring);
//                map.put("unitByReal", unitByReal);
//                map.put("titleName", historyListDto.getTitleName());
//                map.put("lastUpdateTime", historyListDto.getLastUpdateTime());
//                map.put("delay", delayStr);
//                mapList.add(map);
//            }
//        } else {
//            map = new HashMap<>();
//            map.put("maxData", maxList);
//            map.put("minData", minList);
//            map.put("avgData", avgList);
//            map.put("maxUsring", "");
//            map.put("minUsring", "");
//            map.put("avgUsring", "");
//            map.put("unitByMax", "");
//            map.put("unitByMin", "");
//            map.put("unitByAvg", "");
//            map.put("realData", realList);
//            map.put("realUsring", "");
//            map.put("unitByReal", "");
//            map.put("titleName", "");
//            map.put("lastUpdateTime", "");
//            map.put("delay", "");
//            mapList.add(map);
//        }
//        endTime7 = System.currentTimeMillis();
//
//        logger.info("数据处理总耗时：" + (endTime7 - endTime2) + "ms；数据量：" + maxList.size() + "数据量：" + realList.size());
//
//
//        return mapList;
//    }

    //对zabbix获取的多个监控数据进行处理
    private List<Map> getDataByMapByCPU(ServerHistoryDto serverHistoryDto, List<MWItemHistoryDto> mwItemHistoryDtos, Map<String, HistoryListDto> mapInfo) {

        long endTime2 = 0l;
        long endTime3 = 0l;
        long endTime4 = 0l;
        long endTime5 = 0l;
        List<List<MWItemHistoryDto>> subList;
        List<MWItemHistoryDto> maxList = new ArrayList<>();
        List<MWItemHistoryDto> minList = new ArrayList<>();
        List<MWItemHistoryDto> avgList = new ArrayList<>();
        List<Map> mapList = new ArrayList<>();
        //真实数据。
        List<MWItemHistoryDto> realList = new ArrayList<>();
        Map<String, Object> map = new HashMap();
        endTime2 = System.currentTimeMillis();
        List<MWItemHistoryDto> lists = new ArrayList<>();
        List<List<MWItemHistoryDto>> listAll = new ArrayList<>();
        HistoryListDto historyListDto = new HistoryListDto();
        Set<String> set = mapInfo.keySet();
        //CPU 、内存 等数据 有多个监控子项时，需要取监控子项的平均值。
        int sizeList = 0;
        for (String key : set) {
            realList = new ArrayList<>();
            lists = new ArrayList<>();
            maxList = new ArrayList<>();
            minList = new ArrayList<>();
            avgList = new ArrayList<>();
            for (MWItemHistoryDto dto : mwItemHistoryDtos) {
                if (key.equals(dto.getItemid())) {
                    lists.add(dto);
                }
            }
            //循环所有子项数据，获取最大的数据长度
            if (sizeList < lists.size()) {
                sizeList = lists.size();
            }
            if (lists.size() != 0) {
                //获取所有数据
                listAll.add(lists);
                historyListDto = mapInfo.get(key);
            }
        }
        int len = listAll.size();
        int sum = 0;
        List<MWItemHistoryDto> listInfo = new ArrayList<>();
        for (int x = 0; x < sizeList; x++) {
            Double lastValue = 0.0;
            Double valueDouble;
            Date dateTime = null;
            sum = 0;
            for (int y = 0; y < len; y++) {
                //循环数组，没有数据的则跳过
                if (listAll.get(y) != null && ((ArrayList) listAll.get(y)).size() >= (x + 1) && listAll.get(y).get(x) != null && listAll.get(y).get(x).getValue() != null) {
                    sum++;
                    lastValue = lastValue + Double.valueOf(listAll.get(y).get(x).getValue());
                }
                if (listAll.get(y) != null && ((ArrayList) listAll.get(y)).size() >= (x + 1) && listAll.get(y).get(x) != null && listAll.get(y).get(x).getDateTime() != null) {
                    dateTime = listAll.get(y).get(x).getDateTime();
                }
            }
            //防止下面算式除数为0；
            if (sum == 0) {
                sum = 1;
            }
            valueDouble = Double.valueOf(lastValue / sum);
            //只保留两位小数
            Double values = new BigDecimal(valueDouble).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            MWItemHistoryDto mwItemHistoryDto = new MWItemHistoryDto();
            mwItemHistoryDto.setLastValue(values.longValue());
            mwItemHistoryDto.setDateTime(dateTime);
            mwItemHistoryDto.setValue(String.valueOf(values));
            mwItemHistoryDto.setDoubleValue(values);
            listInfo.add(mwItemHistoryDto);
        }
        int delay;
        String units = "";
        String delayStr = "";
        if (!Strings.isNullOrEmpty(historyListDto.getDelay())) {
            delayStr = historyListDto.getDelay();
        }
        //一小时，一天，自定义时间，取真实数据
        if (serverHistoryDto.getDateType() != null) {
            if (!Strings.isNullOrEmpty(delayStr)) {
                units = delayStr.replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
            }
            switch (serverHistoryDto.getDateType()) {//1：hour 2:day 3:week 4:month
                case 1:
                    //一小时
                    realList = listInfo;
                    break;
                case 2:
                    //一天
                    realList = listInfo;
                    break;
                case 3:
                    //一周
                    delay = 15;
                    if ("".equals(units) || "md".equals(units) || "wd".equals(units) || "h".equals(units)) {//取值间隔为月每次，周每次，小时每次，则不需要对最大值最小值平均值处理。
                        maxList = listInfo;
                        minList = listInfo;
                        avgList = listInfo;
                    } else {
                        if (getInteger(delayStr) != null) {
                            delay = 15 * 60 / getInteger(delayStr);
                            if (getInteger(delayStr) > 900) {
                                delay = 1;
                            }
                        }
                        subList = splitList(listInfo, delay);
                        endTime3 = System.currentTimeMillis();
                        for (List<MWItemHistoryDto> list : subList) {
                            MWItemHistoryDto maxDto = list.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                            MWItemHistoryDto mixDto = list.stream().min(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                            MWItemHistoryDto avgDto = new MWItemHistoryDto();
                            Double avgVal = list.stream().mapToDouble(mwItemHistoryDto -> Double.valueOf(mwItemHistoryDto.getDoubleValue())).average().getAsDouble();
                            Double values = new BigDecimal(avgVal).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            avgDto.setValue(String.valueOf(values));
                            avgDto.setDoubleValue(values);
                            //取数值最后一条数据的时间。
                            avgDto.setDateTime(list.get((list.size() - 1)).getDateTime());
                            maxList.add(maxDto);
                            minList.add(mixDto);
                            avgList.add(avgDto);
                        }
                    }
                    break;
                case 4:
                    //一个月
                    //获取时间间隔数值
                    delay = 60;
                    if ("".equals(units) || "md".equals(units) || "wd".equals(units) || "h".equals(units)) {//取值间隔为月每次，周每次，小时每次，则不需要对最大值最小值平均值处理。
                        maxList = listInfo;
                        minList = listInfo;
                        avgList = listInfo;
                    } else {
                        if (getInteger(delayStr) != null) {
                            delay = 60 * 60 / getInteger(delayStr);
                            if (getInteger(delayStr) > 3600) {
                                delay = 1;
                            }
                        }
                        subList = splitList(listInfo, delay);
                        endTime3 = System.currentTimeMillis();
                        for (List<MWItemHistoryDto> list : subList) {
                            MWItemHistoryDto maxDto = list.stream().max(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                            MWItemHistoryDto mixDto = list.stream().min(Comparator.comparing(MWItemHistoryDto::getDoubleValue)).get();
                            MWItemHistoryDto avgDto = new MWItemHistoryDto();
                            Double avgVal = list.stream().mapToDouble(mwItemHistoryDto -> Double.valueOf(mwItemHistoryDto.getDoubleValue())).average().getAsDouble();
                            Double values = new BigDecimal(avgVal).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            avgDto.setValue(String.valueOf(values));
                            avgDto.setDoubleValue(values);
                            //取数值最后一条数据的时间。
                            avgDto.setDateTime(list.get((list.size() - 1)).getDateTime());
                            maxList.add(maxDto);
                            minList.add(mixDto);
                            avgList.add(avgDto);
                        }
                    }
                    break;
                case 5:
                    //自定义
                    realList = listInfo;
                    break;
                default:
                    break;
            }
            endTime4 = System.currentTimeMillis();
        }
        String unitByMax = "";
        String unitByMin = "";
        String unitByAvg = "";
        String unitByReal = "";
        boolean isJZ = maxList.size() != 0 && minList.size() != 0 && avgList.size() != 0;
        map = new HashMap<>();
        unitByMax = historyListDto.getUnit();
        unitByMin = historyListDto.getUnit();
        unitByAvg = historyListDto.getUnit();
        unitByReal = historyListDto.getUnit();

        //使用率
        String maxUsring = "";
        String minUsring = "";
        String avgUsring = "";
        String realUsring = "";
        if (isJZ) {
            maxUsring = maxList.get(maxList.size() - 1).getValue();
            minUsring = minList.get(minList.size() - 1).getValue();
            avgUsring = avgList.get(avgList.size() - 1).getValue();
        }

        if (realList.size() > 0) {
            realUsring = realList.get(realList.size() - 1).getValue();
        }
        if(CollectionUtils.isNotEmpty(realList) && CollectionUtils.isEmpty(avgList)){
            map.put("avgData", realList);
        }else{
            map.put("avgData", avgList);
        }
        map.put("maxData", maxList);
        map.put("minData", minList);
        map.put("maxUsring", maxUsring);
        map.put("minUsring", minUsring);
        map.put("avgUsring", avgUsring);
        map.put("unitByMax", unitByMax);
        map.put("unitByMin", unitByMin);
        map.put("unitByAvg", unitByAvg);
        map.put("realData", realList);
        map.put("realUsring", realUsring);
        map.put("unitByReal", unitByReal);
        map.put("titleName", historyListDto.getTitleName());
        map.put("lastUpdateTime", historyListDto.getLastUpdateTime());
        map.put("delay", delayStr);
        mapList.add(map);

        endTime5 = System.currentTimeMillis();

        logger.info("数据分组处理耗时：" + (endTime3 - endTime2) + "ms" +
                "数据极值获取耗时：" + (endTime4 - endTime3) + "ms" +
                "数据处理总耗时：" + (endTime5 - endTime2) + "ms");

        return mapList;
    }

    public static Integer getInteger(String str) {
        int i = 0;
        String reg = "[^0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(str);
        String trim = m.replaceAll("").trim();
        String units = str.replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
        if ("h".equals(units)) {
            i = Integer.valueOf(trim) * 60 * 60;
        }
        if ("m".equals(units)) {
            i = Integer.valueOf(trim) * 60;
        }
        if ("s".equals(units)) {
            i = Integer.valueOf(trim);
        }
        return i;
    }

    public static Integer getIntegerNew(String str) {
        int i = 0;
        String reg = "[^0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(str);
        String trim = m.replaceAll("").trim();
        String units = str.replaceAll("\\s*", "").replaceAll("[^(A-Za-z)]", "");
        if ("h".equals(units)) {
            i = (Integer.valueOf(trim) * 60 * 60) + 1;
        }
        if ("m".equals(units)) {
            i = (Integer.valueOf(trim) * 60) + 1;
        }
        if ("s".equals(units)) {
            i = Integer.valueOf(trim);
        }
        return i;
    }

    private List<List<MWItemHistoryDto>> splitList(List<MWItemHistoryDto> messagesList, int groupSize) {
        int length = messagesList.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize; // TODO
        List<List<MWItemHistoryDto>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            newList.add(messagesList.subList(fromIndex, toIndex));
        }
        return newList;
    }

    /**
     * 取了前五的监控项排行榜，从大到小
     *
     * @param param
     * @return
     */
    @Override
    public Reply getItemRank(RankServerDTO param) {
        try {
            ItemRank memory = mwServerManager.getItemRank(param);
            List<ItemNameRank> list = memory.getItemNameRankList();
            if (param.isSortByNameFlag()) {
                Collections.sort(list, new Comparator<ItemNameRank>() {
                    @Override
                    public int compare(ItemNameRank o1, ItemNameRank o2) {
                        if (null == o1.getType()) {
                            return 1;
                        }
                        if (null == o2.getType()) {
                            return -1;
                        }
                        return Collator.getInstance(Locale.ENGLISH).compare(o1.getType(), o2.getType());
                    }
                });
            } else {
                Collections.sort(list, new ItemNameRank());//倒序排序
            }
            if (list.size() > 5) {
                list = list.subList(0, 5);
            }
            memory.setItemNameRankList(list);
            return Reply.ok(memory);
        } catch (Exception e) {
            log.error("fail to getMemory with serverDto={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.SERVER_RANK_CODE_302001, ErrorConstant.SERVER_RANK_MSG_302001);
        }
    }

    @Override
    public Reply getSoftwareDataList(AssetsIdsPageInfoParam param) {
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            List listDtos = new ArrayList<>();
            MWZabbixAPIResult softwarelist = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), param.getAssetsId(), ZbApplicationNameEnum.SOFTWARE.getName(), "_SOFTWARELIST");

            JsonNode node = (JsonNode) softwarelist.getData();
            if (node.size() > 0) {
                for (int i = 0; i < node.size(); i++) {
                    String name = node.get(i).get("name").asText();
                    String lastvalue = node.get(i).get("lastvalue").asText();
                    if (ZabbixItemConstant.MW_WINDOWS_SOFTWARELIST.equals(name) || ZabbixItemConstant.MW_WIN_SOFTWARELIST.equals(name)) {
                        if (lastvalue != null && !"".equals(lastvalue)) {
                            List<SoftwareListDto> softwareListDtoList = new ArrayList<>();
                            if (ZabbixItemConstant.MW_WIN_SOFTWARELIST.equals(name)) {
                                StringBuilder values = strFormat(lastvalue);
                                softwareListDtoList = JSONObject.parseArray(String.valueOf(values), SoftwareListDto.class);
                            } else {
                                softwareListDtoList = JSONObject.parseArray(lastvalue, SoftwareListDto.class);
                            }
                            softwareListDtoList.removeAll(Collections.singleton(new SoftwareListDto()));
                            listDtos.addAll(softwareListDtoList);
                            Set tempSet = new HashSet<>(listDtos);
                            listDtos.clear();
                            listDtos.addAll(tempSet);
                        }
                    } else if (ZabbixItemConstant.MW_MACOS_SOFTWARELIST.equals(name)) {
                        Map<String, String> map = new HashMap<>();
                        if (lastvalue != null && !"".equals(lastvalue)) {

                            logger.info("mac softinfo lastvalue:{}", lastvalue);

                            lastvalue = lastvalue.replaceAll(" ", "");
                            String[] split = lastvalue.split("\n\n");
                            for (int j = 1; j < split.length; j = j + 2) {
                                String key = split[j].replaceAll(":", "");
                                String value = "";
                                int index = split[j + 1].indexOf("Version:");
                                int indexEnd = split[j + 1].indexOf("\n");
                                if (index != -1) {
                                    value = split[j + 1].substring(index + 8, indexEnd);
                                }
                                map.put(key, value);
                            }
                        }
                        listDtos = map.entrySet().stream().map(e -> new SoftwareListDto(e.getKey(), e.getValue(), true)).collect(Collectors.toList());
                    }
                }
            }
            pageInfo.setTotal(listDtos.size());
            listDtos = pageList.getList(listDtos, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(listDtos);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getSoftwareDataList with hostid={}, cause:{}", param.getAssetsId(), e);
            return Reply.fail(ErrorConstant.SERVER_DISK_CODE_302003, ErrorConstant.SERVER_DISK_MSG_302003);
        }
    }

    public StringBuilder strFormat(String lastvalue) {
        lastvalue = lastvalue.replaceAll("\"", "")
                .replaceFirst("\r\n\r\n", "[{\"")
                .replaceAll("\r\n\r\n", "\"},{\"")
                .replaceAll(" : ", "\":\"")
                .replaceAll(" ", "")
                .replaceAll("\r\n", "\",\"") + "\"}]";
        String[] parts = lastvalue.split("},\\{");
        StringBuilder values = new StringBuilder();
        String displayName = "";
        String displayVersion = "";
        String publisher = "";
        String displayNameStr = "";
        String displayVersionStr = "";
        String publisherStr = "";
        values.append("[{");
        for (int n = 0; n < parts.length; n++) {
            ////System.out.println(n);
            if (parts[n].contains("\"DisplayName")) {
                displayNameStr = parts[n].substring(parts[n].indexOf("\"DisplayName"));
                displayName = displayNameStr.substring(0, displayNameStr.indexOf(",\"")).replace("DisplayName", "Caption");
            }
            if (parts[n].contains("\"DisplayVersion")) {
                displayVersionStr = parts[n].substring(parts[n].indexOf("\"DisplayVersion"));
                displayVersion = displayVersionStr.substring(0, displayVersionStr.indexOf(",\"")).replace("DisplayVersion", "Version");
            }
            if (parts[n].contains("\"Publisher")) {
                publisherStr = parts[n].substring(parts[n].indexOf("\"Publisher"));
                publisher = publisherStr.substring(0, publisherStr.indexOf(",\"")).replace("Publisher", "Vendor");
            }
            if (n == parts.length - 1) {
                values.append(displayName).append(",").append(displayVersion).append(",").append(publisher).append("}]");
            } else {
                values.append(displayName).append(",").append(displayVersion).append(",").append(publisher).append("},{");
            }
        }
        return values;
    }

    @Override
    public Reply getDiskDataList(AssetsBaseDTO assetsBaseDTO) {
        try {
            log.info("getDiskDataList 获取磁盘列表，开始运行");
            List<DiskListDto> listDtos = new ArrayList<>();
            List<String> nameList = mwServerManager.getNames(assetsBaseDTO.getMonitorServerId(), assetsBaseDTO.getAssetsId(), "DISK_INFO", "DISK_NAME", false);
            if (nameList.size() > 0) {
                int threadSize = nameList.size() > 1 ? (nameList.size() / 2) : nameList.size();
//                ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
                ThreadPoolExecutor executorService = new ThreadPoolExecutor(threadSize, nameList.size(), 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                List<Future<DiskListDto>> futureList = new ArrayList<>();
                nameList.forEach(name -> {
                    GetDiskListThread getDiskListThread = new GetDiskListThread() {
                        @Override
                        public DiskListDto call() throws Exception {
                            return getDiskInfoByDiskName(assetsBaseDTO.getMonitorServerId(), name, assetsBaseDTO.getAssetsId());
                        }
                    };
                    Future<DiskListDto> f = executorService.submit(getDiskListThread);
                    futureList.add(f);
                });
                futureList.forEach(f -> {
                    try {
                        DiskListDto netListDto = f.get(20, TimeUnit.SECONDS);
                        listDtos.add(netListDto);
                    } catch (Exception e) {
                        f.cancel(true);
                    }
                });
                executorService.shutdown();
                logger.info("关闭线程池");
            }
            return Reply.ok(listDtos);
        } catch (Exception e) {
            log.error("fail to getDiskDataList with assetsBaseDTO={}, cause:{}", assetsBaseDTO, e);
            return Reply.fail(ErrorConstant.SERVER_DISK_CODE_302003, ErrorConstant.SERVER_DISK_MSG_302003);
        }
    }


    @Override
    public Reply getDiskDetail(DiskTypeDto diskTypeDto) {
        try {
            log.info("getDiskDetail 获取磁盘详情，开始运行");
            List<DiscDto> list = new ArrayList<>();
            DiscDto discDto = new DiscDto();
            if (null != diskTypeDto) {
                String hostName = assetsDao.getTypeName(diskTypeDto.getAssetsId(), diskTypeDto.getMonitorServerId());
//                hostName = "Windows Server";
                if (null != hostName && StringUtils.isNotEmpty(hostName)) {
                    if ("/".equals(diskTypeDto.getType())) {//防止模糊查询匹配多个数据。
                        diskTypeDto.setType("/]");
                    }
                    if (hostName.equals("Windows Server")) {//应用集有两个 磁盘和磁盘IO
                        MWZabbixAPIResult result0 = mwtpServerAPI.getItemDataByAppName(diskTypeDto.getMonitorServerId(), diskTypeDto.getAssetsId(), "DISK", diskTypeDto.getType());
                        MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppName(diskTypeDto.getMonitorServerId(), diskTypeDto.getAssetsId(), "DISK_IO", diskTypeDto.getType());
                        discDto.setInfoData(getDataResult(diskTypeDto.getMonitorServerId(), result0, "0"));
                        discDto.setInfoName("磁盘");
                        DiscDto discDto2 = new DiscDto();
                        discDto2.setInfoData(getDataResult(diskTypeDto.getMonitorServerId(), result1, "0"));
                        discDto2.setInfoName("磁盘IO");
                        list.add(discDto2);
                    } else {//应用集有一个 磁盘 if (hostName.equals("Linux Server"))
                        MWZabbixAPIResult result3 = mwtpServerAPI.getItemDataByAppName(diskTypeDto.getMonitorServerId(), diskTypeDto.getAssetsId(), "DISK", diskTypeDto.getType());
                        discDto.setInfoData(getDataResult(diskTypeDto.getMonitorServerId(), result3, "0"));
                        discDto.setInfoName("磁盘");
                    }
                }
                list.add(discDto);
            }
            logger.info("SERVER_LOG[]getDiskDetail[]根据一个hostid和分区获取获取磁盘的详细数据[]{}[]", diskTypeDto);
            log.info("getDiskDetail 获取磁盘详情，成功结束运行");
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("getDiskDetail 获取磁盘详情，失败结束运行");
            log.error("fail to getDiskDetail with diskTypeDto={}, cause:{}", diskTypeDto, e.getMessage());
            return Reply.fail(ErrorConstant.SERVER_DISK_CODE_302003, ErrorConstant.SERVER_DISK_MSG_302003);
        }
    }

    @Override
    public Reply getNetDetail(DiskTypeDto diskTypeDto) {
        try {
            log.info("getNetDetail 获取接口详情，开始运行");
            List<DiscDto> list = new ArrayList<>();
            if (null != diskTypeDto) {

                InterfaceInfoEnum[] str = InterfaceInfoEnum.values();
                List<String> itemNameLists = new ArrayList<>();
                for (InterfaceInfoEnum enumss : str) {
                    if (!INTERFACE_DESCR.getName().equals(enumss.getName())) {//过滤描述信息，描述信息从数据库中获取
                        itemNameLists.add("[" + diskTypeDto.getType() + "]" + enumss.getName());
                    }
                }
                long time1 = System.currentTimeMillis();
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(diskTypeDto.getMonitorServerId(), itemNameLists, diskTypeDto.getAssetsId());
//                MWZabbixAPIResult result = mwtpServerAPI.getItemDataByAppName(diskTypeDto.getMonitorServerId(), diskTypeDto.getAssetsId(), "INTERFACES", "[" + diskTypeDto.getType() + "]");
                List<ItemData> dataResult = getDataResult(diskTypeDto.getMonitorServerId(), result, "0");
                //校验，若若出入的流量为0，则流量百分比也为0
                String inTrafficValue = "";
                String outTrafficValue = "";
                for (ItemData data : dataResult) {
                    if (data.getName().indexOf("MW_INTERFACE_IN_TRAFFIC") != -1) {
                        inTrafficValue = data.getValue().replaceAll("[a-zA-Z]", "");
                    }
                    if (data.getName().indexOf("MW_INTERFACE_OUT_TRAFFIC") != -1) {
                        outTrafficValue = data.getValue().replaceAll("[a-zA-Z]", "");
                    }
                    //去除小数点，只保留整数
                    if (data.getName().indexOf("INTERFACE_IN_UTILIZATION") != -1) {
                        String outVal = data.getValue().replaceAll("[a-zA-Z%]", "");
                        BigDecimal outValue = new BigDecimal(outVal);
                        if (outVal.indexOf(".") != -1) {
                            outVal = outValue.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
                            data.setValue(outVal);
                        }
                    }
                    if (data.getName().indexOf("INTERFACE_OUT_UTILIZATION") != -1) {
                        String outVal = data.getValue().replaceAll("[a-zA-Z%]", "");
                        BigDecimal outValue = new BigDecimal(outVal);
                        if (outVal.indexOf(".") != -1) {
                            outVal = outValue.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
                            data.setValue(outVal);
                        }
                    }
                }
                for (ItemData data : dataResult) {
                    if (data.getName().indexOf("INTERFACE_IN_UTILIZATION") != -1 && "0".equals(inTrafficValue)) {
                        data.setValue("0");
                    }
                    if (data.getName().indexOf("INTERFACE_OUT_UTILIZATION") != -1 && "0".equals(outTrafficValue)) {
                        data.setValue("0");
                    }
                    if (data.getName().equals("[" + diskTypeDto.getType() + "]" + ZabbixItemConstant.INTERFACE_INDEX)) {
                        if (data.getValue().indexOf(".") != -1) {
                            data.setValue(data.getValue().substring(0, data.getValue().indexOf(".")));
                        }
                        MWZabbixAPIResult resultData = mwtpServerAPI
                                .getItemDataByAppName(diskTypeDto.getMonitorServerId(), diskTypeDto.getAssetsId(), "IPADDRESS", "[" + data.getValue() + "]");
                        JsonNode dataList = (JsonNode) resultData.getData();
                        if (dataList.size() > 0) {
                            List<ItemData> itemDatas = getDataResult(diskTypeDto.getMonitorServerId(), resultData, "0");
                            itemDatas = itemDatas.stream().filter(x -> !(x.getName().indexOf("INTERFACE_IPBCASTADDR") != -1 || x.getName().indexOf("INTERFACE_IPV4INDEX") != -1)).collect(Collectors.toList());
                            dataResult.addAll(itemDatas);
                        }
                        break;
                    }
                }
                //固定属性，从数据库中查询获取。
                List<QueryAssetsInterfaceParam> interfaceList = mwAssetsInterfaceDao.getAllInterface(diskTypeDto.getId(), diskTypeDto.getType(), null);
                if (interfaceList != null && interfaceList.size() > 0) {
                    for (QueryAssetsInterfaceParam listParam : interfaceList) {
                        List<ItemData> itemDatas = new ArrayList<>();
                        ItemData itemData1 = new ItemData();
                        itemData1.setChName("接口类型");
                        itemData1.setValue(listParam.getType() + "");
                        itemDatas.add(itemData1);
                        ItemData itemData2 = new ItemData();
                        itemData2.setChName("IP地址");
                        itemData2.setValue(listParam.getIp() == null ? "" : listParam.getIp());
                        itemDatas.add(itemData2);
                        ItemData itemData3 = new ItemData();
                        itemData3.setChName("子网掩码");
                        itemData3.setValue(listParam.getSubnetMask() == null ? "" : listParam.getSubnetMask());
                        itemDatas.add(itemData3);
                        dataResult.addAll(itemDatas);
                    }
                }
                Collections.sort(dataResult, new Comparator<ItemData>() {
                    @Override
                    public int compare(ItemData o1, ItemData o2) {
                        if (null == o1.getChName()) {
                            return 1;
                        }
                        if (null == o2.getChName()) {
                            return -1;
                        }
                        return Collator.getInstance(Locale.CHINESE).compare(o1.getChName(), o2.getChName());
                    }
                });
                DiscDto discDto = new DiscDto();
                discDto.setInfoData(dataResult);
                discDto.setInfoName("接口");
                list.add(discDto);
            }
            log.info("getNetDetail 获取接口详情，成功运行！");
            logger.info("SERVER_LOG[]getNetDetail[]根据一个hostid和接口名称获取获取接口的详细数据[]{}[]", diskTypeDto);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("getNetDetail 获取接口详情，失败运行");
            log.error("fail to getNetDetail with diskTypeDto={}, cause:{}", diskTypeDto, e);
            return Reply.fail(ErrorConstant.SERVER_NET_CODE_302004, ErrorConstant.SERVER_NET_MSG_302004);
        }
    }


    @Override
    public Reply getApplication(AssetsBaseDTO param) {
        try {
            log.info("getApplication 获取应用集，开始运行");
            List<ApplicationDTO> list = new ArrayList<>();
            ApplicationDTO LLDDto = new ApplicationDTO();
            int monitorServerId =  param.getMonitorServerId();
            List<String> hostIdList = new ArrayList<>();
            hostIdList.add(param.getAssetsId());
            //判断是否是虚拟化设备
            if(VCENTER.getModelId().equals(param.getModelId()) && !org.elasticsearch.common.Strings.isNullOrEmpty(param.getIp()) ){
                MWZabbixAPIResult hostInfoResult = mwtpServerAPI.getHostInfoByName(monitorServerId,"<"+param.getIp()+">");
                String hostId = "";
                if (hostInfoResult.getCode() == 0) {
                    JsonNode jsonNode = (JsonNode) hostInfoResult.getData();
                    for (JsonNode hostInfo : jsonNode) {
                        hostId = hostInfo.get("hostid").asText();
                        hostIdList.add(hostId);
                    }
                }
            }

            if(CollectionUtils.isNotEmpty(hostIdList)){
                for(String hostid : hostIdList){
                    if (null != hostid && StringUtils.isNotEmpty(hostid)) {
                        MWZabbixAPIResult result = mwtpServerAPI.getApplication(param.getMonitorServerId(), hostid);
                        list = resultResolver.analysisResult(mwtpServerAPI.getServerType(monitorServerId), String.valueOf(result.getData()));
                        MWZabbixAPIResult LLDResult = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostid);
                        LLDDto.setName(ZabbixItemConstant.APPLICATION_LLD);
                        LLDDto.setChName("LLD");
                        if (LLDResult.getCode() == 0) {
                            String data = String.valueOf(LLDResult.getData());
                            List<ApplicationDTO> all = JSONArray.parseArray(data, ApplicationDTO.class);
                            LLDDto.setCount(all.size());
                            LLDDto.setItemIds(all.stream().map(item -> item.getItemid()).collect(Collectors.toList()));
                        }
                    }
                    ApplicationDTO appDTO = new ApplicationDTO();
                    appDTO.setName(ZabbixItemConstant.APPLICATION_NAME);
                    appDTO.setChName("全部");
                    MWZabbixAPIResult result = mwtpServerAPI.itemGetbyHostId(monitorServerId, hostid);
                    if (result.getCode() == 0) {
                        String data = String.valueOf(result.getData());
                        List<ApplicationDTO> all = JSONArray.parseArray(data, ApplicationDTO.class);
                        appDTO.setCount(all.size());
                        appDTO.setItemIds(all.stream().map(item -> item.getItemid()).collect(Collectors.toList()));
                    }
                    list.add(appDTO);
                    list.add(LLDDto);
                }
            }
            logger.info("SERVER_LOG[]getDiskDetail[]根据一个hostid和接口名称获取获取接口的详细数据[]{}[]");
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getApplication with cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.SERVER_APPLICATION_CODE_302004, ErrorConstant.SERVER_APPLICATION_MSG_302004);
        }

    }

    private List<List<String>> splitStrList(List<String> messagesList, int groupSize) {
        int length = messagesList.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize; // TODO
        List<List<String>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            newList.add(messagesList.subList(fromIndex, toIndex));
        }
        return newList;
    }

    @Override
    public Reply getItemApplication(ApplicationParam param) {
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            List<ItemApplication> itemList = new ArrayList<>();
            long time1 = System.currentTimeMillis();
            if (null != param && StringUtils.isNotEmpty(param.getAssetsId()) && StringUtils.isNotEmpty(param.getApplicationName())) {
                MWZabbixAPIResult result = null;
                List<ItemApplication> newList = new ArrayList<>();

                List<String> itemIds = param.getItems();

                //优化测试
                if (itemIds.size() > itemIdGroupSize) {
                    List<List<String>> itemIdList = splitStrList(itemIds, itemIdGroupSize);
                    for (List<String> itemIdSubList : itemIdList) {
                        result = mwtpServerAPI.getItemName(param.getMonitorServerId(), itemIdSubList);
                        if (result != null && !result.isFail()) {
                            String data = String.valueOf(result.getData());
                            newList.addAll(JSONArray.parseArray(data, ItemApplication.class));
                        }
                    }
                } else {
                    if ("LLD".equals(param.getApplicationName())) {
                        result = mwtpServerAPI.getDRuleByHostId(param.getMonitorServerId(), param.getAssetsId());
                    } else {
                        result = mwtpServerAPI.getItemName(param.getMonitorServerId(), itemIds);
                    }
                    if (result != null && !result.isFail()) {
                        String data = String.valueOf(result.getData());
                        newList = JSONArray.parseArray(data, ItemApplication.class);
                    }
                }
                long time2 = System.currentTimeMillis();
                //获取valuemapIds，需要映射的值进行对应转换
                List<String> valuemapIds = new ArrayList<>();
                for (ItemApplication item : newList) {
                    //监控接口
                    String valuemapid = item.getValuemapid();
                    if (!"0".equals(valuemapid) && valuemapid != null) {
                        valuemapIds.add(valuemapid);
                    }
                }
                Map<String, Map> valueMapByIdMap = new HashMap<>();
                if (valuemapIds.size() > 0) {
                    List<String> valuemapIdList = valuemapIds.stream().distinct().collect(Collectors.toList());
                    //映射值进行转换
                    valueMapByIdMap = mwServerManager.getValueMapByIdList(param.getMonitorServerId(), valuemapIdList);
                }
                //通过itemName 获取中文名称，一次性全部从数据库中取出，避免多次循环连接数据库。
                Map<String, String> itemChNameMap = mwServerManager.getitemChNameMap();
                long time3 = System.currentTimeMillis();

                List<ItemApplication> newList1 = new ArrayList<>();

                for (ItemApplication item : newList) {
                    String lastValue = "";
                    String units = item.getUnits();
                    String valuemapId = item.getValuemapid();
                    if (item.getValue_type() != null) {
                        item.setHistory(Integer.parseInt(item.getValue_type()));
                    }
                    if (getInteger(item.getDelay()) != null) {
                        item.setSortDelay(Long.valueOf(getIntegerNew(item.getDelay())));
                    }
                    //监控接口
                    if ("uptime".equals(units)) {
                        //持续时间 不做则线图处理
                        lastValue = SeverityUtils.getLastTime(Long.valueOf(Double.valueOf(item.getLastvalue()).intValue()));
                    } else if ("0".equals(item.getValue_type()) || "3".equals(item.getValue_type())) {
                        lastValue = UnitsUtil.getValueWithUnits(item.getLastvalue(), item.getUnits());
                    } else {
                        lastValue = item.getLastvalue();
                    }
                    if (valueMapByIdMap != null && valueMapByIdMap.size() > 0 &&
                            valueMapByIdMap.get(valuemapId) != null && valueMapByIdMap.get(valuemapId).get(lastValue) != null) {
                        String newvalue = valueMapByIdMap.get(valuemapId).get(lastValue).toString();
                        if (!Strings.isNullOrEmpty(newvalue)) {
                            lastValue = newvalue;
                        }
                    }
                    item.setValue_type(("0".equals(item.getValue_type()) || "3".equals(item.getValue_type())) ? "NUMERAL" : "NOTNUMERAL");
                    if ("uptime".equals(units)) {
                        item.setValue_type("NOTNUMERAL");
                    }
                    item.setLastvalue(lastValue);
                    String name = item.getName();
                    String chName = mwServerManager.getChNameByMap(name, itemChNameMap);
                    item.setChName(chName);
                    item.setState("0".equals(item.getState()) ? "NORMAL" : "UNSUPPORTED");
                    if (!"0".equals(item.getLastclock()) && item.getLastclock() != null) {
                        item.setSortLastclock(Long.parseLong(item.getLastclock()));
                        item.setLastclock(SeverityUtils.getDate(item.getSortLastclock()));
                    } else {
                        item.setLastclock("");
                        item.setSortLastclock(0L);
                    }
                }
                if (Strings.isNullOrEmpty(param.getStatus())) {
                    param.setStatus("NORMAL");
                }
                //状态过滤
                List<ItemApplication> collect = newList.stream().filter(e -> e.getState().equals(param.getStatus())).collect(Collectors.toList());
                List<ItemApplication> collects = new ArrayList<>();
                List<ItemApplication> collect1 = new ArrayList<>();
                if (!Strings.isNullOrEmpty(param.getQueryVal())) {
                    //全字段过滤
                    List<ItemApplication> collectByChName = collect.stream().filter(e -> e.getChName().indexOf(param.getQueryVal()) != -1).collect(Collectors.toList());

                    //全字段过滤
                    List<ItemApplication> collectByDelay = collect.stream().filter(e -> e.getDelay().indexOf(param.getQueryVal()) != -1).collect(Collectors.toList());

                    //全字段过滤
                    List<ItemApplication> collectByState = collect.stream().filter(e -> e.getState().indexOf(param.getQueryVal()) != -1).collect(Collectors.toList());
                    if (!ZabbixItemConstant.APPLICATION_LLD.equals(param.getApplicationName())) {
                        //全字段过滤
                        List<ItemApplication> collectByLastvalue = collect.stream().filter(e -> e.getLastvalue().indexOf(param.getQueryVal()) != -1).collect(Collectors.toList());

                        //全字段过滤
                        List<ItemApplication> collectByLastclock = collect.stream().filter(e -> e.getLastclock().indexOf(param.getQueryVal()) != -1).collect(Collectors.toList());
                        collect1.addAll(collectByLastvalue);
                        collect1.addAll(collectByLastclock);
                    }
                    collect1.addAll(collectByChName);
                    collect1.addAll(collectByDelay);
                    collect1.addAll(collectByState);
                    collects = collect1.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ItemApplication::getItemid))), ArrayList::new));
                } else {
                    collects.addAll(collect);
                }

                long time4 = System.currentTimeMillis();
                //排序
                if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField())) {
                    ListSortUtil<ItemApplication> listSortUtil = new ListSortUtil<>();
                    String sort = "sort" + param.getSortField().substring(0, 1).toUpperCase() + param.getSortField().substring(1);
                    if ("lastclock".equals(param.getSortField()) || "delay".equals(param.getSortField())) {
                        param.setSortField(sort);
                    }
                    //查看当前属性名称是否在对象中
                    try {
                        Field field = ItemApplication.class.getDeclaredField(param.getSortField());
                        listSortUtil.sort(collects, param.getSortField(), param.getSortMode());
                    } catch (NoSuchFieldException e) {
                        logger.info("has no field", e);
                        listSortUtil.sort(collects, sort, param.getSortMode());
                    }
                }
                pageInfo.setTotal(collects.size());
                itemList = pageList.getList(collects, param.getPageNumber(), param.getPageSize());
                long time5 = System.currentTimeMillis();
                pageInfo.setList(itemList);
                ////System.out.println("指标详情查询zabbix时间："+(time2-time1)+"ms；映射值进行转换时间："+(time3-time2)+"ms；数据整理时间："+(time4-time3)+"ms；排序时间："+(time5-time4)+"ms");
            }

            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getItemApplication with param={}, cause:{}", param, e.getMessage());
            return Reply.fail(ErrorConstant.MYMONITOR_SELECT_ITEMS_INFO_CODE_302027, ErrorConstant.MYMONITOR_SELECT_ITEMS_INFO_MSG_302027);
        }
    }

//    @Override
//    public Reply getItemApplicationNew(ApplicationParam param) {
//        try {
//            PageInfo pageInfo = new PageInfo<List>();
//            PageList pageList = new PageList();
//            List<ItemApplication> itemList = new ArrayList<>();
//            if (null != param && StringUtils.isNotEmpty(param.getAssetsId()) && StringUtils.isNotEmpty(param.getApplicationName())) {
//                MWZabbixAPIResult result = null;
////                Boolean nameFlag;
//                //当应用集名称为all时，不传入应用集名称获取所有应用集监控项
////                nameFlag = (param.getApplicationName().equals(ZabbixItemConstant.APPLICATION_NAME)) ? false : true;
//                //当应用集下的监控项数量超过11000后，直接用应用集名称查不到监控项数据
//                List<ItemApplication> newList = new ArrayList<>();
//                pageInfo.setTotal(param.getCount());
//                List<String> itemIds = param.getItems();
//
//
//                if (param.getSortField() == null || StringUtils.isEmpty(param.getSortField())) {//如果不排序的话正常查询分页的内容
//                    itemIds = pageList.getList(param.getItems(), param.getPageNumber(), param.getPageSize());
//                    result = mwtpServerAPI.getItemName(param.getMonitorServerId(), itemIds);
//                    if (!result.isFail()) {
//                        String data = String.valueOf(result.getData());
//                        newList = JSONArray.parseArray(data, ItemApplication.class);
//                    }
//                } else {
//                    for (int i = 0; i < itemIds.size(); i = i + 11000) {
//                        int index = (i + 11000) > itemIds.size() ? itemIds.size() : (i + 11000);
//                        result = mwtpServerAPI.getItemName(param.getMonitorServerId(), itemIds.subList(i, index));
//
//                        if (!result.isFail()) {
//                            String data = String.valueOf(result.getData());
//                            newList.addAll(JSONArray.parseArray(data, ItemApplication.class));
//                        }
//                    }
//
//                }
//
//
//                if (newList.size() > 0) {
//                    int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
//                    coreSizePool = (coreSizePool < newList.size()) ? coreSizePool : newList.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
////                    int threadSize = newList.size() > 1 ? (newList.size() / 2) : newList.size();
////                        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
//                    ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, newList.size(), 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
//                    List<Future<ItemApplication>> futureList = new ArrayList<>();
//                    for (ItemApplication li : newList) {
//                        GetItemThread getItemThread = new GetItemThread() {
//                            @Override
//                            public ItemApplication call() throws Exception {
//                                li.setHistory(Integer.parseInt(li.getValue_type()));
//                                if ("0".equals(li.getLastclock())) {
//                                    //可能需要查询历史最新一条记录
//                                    List<MWItemHistoryDto> list = zabbixManger.HistoryGetByTimeAndHistory(
//                                            param.getMonitorServerId(), li.getItemid(), li.getHistory(), 1);
//                                    if (list != null && list.size() > 0) {
//                                        li.setLastvalue(list.get(0).getValue());
//                                        li.setLastclock(list.get(0).getClock());
//                                    }
//                                }
//                                if (!"0".equals(li.getLastclock())) {
//                                    if (!"0".equals(li.getValuemapid())) {
//                                        String newValue = mwServerManager.getValueMapById(param.getMonitorServerId(), li.getValuemapid(), li.getLastvalue());
//                                        if (null != newValue && StringUtils.isNotEmpty(newValue)) {
//                                            li.setLastvalue(newValue);
//                                        }
//                                    } else {
//                                        String dataUnits = "";
//                                        if ("uptime".equals(li.getUnits())) {
//                                            double v = Double.parseDouble(li.getLastvalue());
//                                            long l = new Double(v).longValue();
//                                            dataUnits = SeverityUtils.getLastTime(l);
//                                        } else if ("0".equals(li.getValue_type()) || "3".equals(li.getValue_type())) {
//                                            dataUnits = UnitsUtil.getValueWithUnits(li.getLastvalue(), li.getUnits());
//                                        } else {
//                                            dataUnits = li.getLastvalue();
//                                        }
//                                        li.setLastvalue(dataUnits);
//                                    }
//                                    li.setSortLastclock(Long.parseLong(li.getLastclock()));
//                                    li.setLastclock(SeverityUtils.getDate(li.getSortLastclock()));
//
//                                } else {
//                                    li.setLastclock("");
//                                    li.setSortLastclock(0L);
//                                }
//                                String name = li.getName();
//                                String chName = mwServerManager.getChName(name);
//                                li.setChName(chName);
//                                li.setState("0".equals(li.getState()) ? "NORMAL" : "UNSUPPORTED");
//                                li.setValue_type(("0".equals(li.getValue_type()) || "3".equals(li.getValue_type())) ? "NUMERAL" : "NOTNUMERAL");
//                                return li;
//                            }
//                        };
//                        Future<ItemApplication> f = executorService.submit(getItemThread);
//                        futureList.add(f);
//                    }
//                    for (Future<ItemApplication> f : futureList) {
//                        try {
//                            ItemApplication itemApplication = f.get(10, TimeUnit.SECONDS);
//                            itemList.add(itemApplication);
//                        } catch (Exception e) {
//                            f.cancel(true);
//                        }
//                    }
//                    executorService.shutdown();
//                    logger.info("关闭线程池");
//                }
//
//                if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField())) {
//                    ListSortUtil<ItemApplication> listSortUtil = new ListSortUtil<>();
//                    String sort = "sort" + param.getSortField().substring(0, 1).toUpperCase() + param.getSortField().substring(1);
//
////                    "delay".equals(param.getSortField()) ||
//                    if ("lastclock".equals(param.getSortField())) {
//                        param.setSortField(sort);
//                    }
//                    //查看当前属性名称是否在对象中
//                    try {
//                        Field field = ItemApplication.class.getDeclaredField(param.getSortField());
//                        listSortUtil.sort(itemList, param.getSortField(), param.getSortMode());
//                    } catch (NoSuchFieldException e) {
//                        logger.info("has no field", e);
//                        listSortUtil.sort(itemList, sort, param.getSortMode());
//                    }
//
//                    itemList = pageList.getList(itemList, param.getPageNumber(), param.getPageSize());
//                }
//                pageInfo.setPages(pageList.getPages());
//                pageInfo.setPageNum(param.getPageNumber());
//                pageInfo.setEndRow(pageList.getEndRow());
//                pageInfo.setStartRow(pageList.getStartRow());
//                pageInfo.setList(itemList);
//
//            }
//            return Reply.ok(pageInfo);
//        } catch (Exception e) {
//            log.error("fail to getItemApplication with param={}, cause:{}", param, e.getMessage());
//            return Reply.fail(ErrorConstant.SERVER_APPLICATION_CODE_302004, ErrorConstant.SERVER_APPLICATION_MSG_302004);
//        }
//    }

    @Override
    public Reply getAlarmByHostId(AssetsIdsPageInfoParam alarmParam) {
        try {
            List<AlarmDTO> alarmList = new ArrayList<>();
            if (null != alarmParam && StringUtils.isNotEmpty(alarmParam.getAssetsId())) {
                List<String> list = new ArrayList<>();
                list.add(alarmParam.getAssetsId());
                log.info("资产详情为回复告警hostid:" + list.toString());
                MWZabbixAPIResult problemget = mwtpServerAPI.problemget(alarmParam.getMonitorServerId(), list);
                alarmList = getDataAlarm(problemget, alarmParam.getWebMonitorName());
            }
            return Reply.ok(alarmList);
        } catch (Exception e) {
            log.error("fail to getAlarmByhostId with param={}, cause:{}", alarmParam, e);
            return Reply.fail(ErrorConstant.SERVER_ALARM_CODE_302005, ErrorConstant.SERVER_ALARM_MSG_302005);
        }
    }

    @Override
    public Reply getRunServiceObjectByIpNoPage(RunServiceObjectParam param) {
        try {
            List<AssetsDTO> list = new ArrayList<>();
            if (null != param.getIp() && StringUtils.isNotEmpty(param.getIp())) {
                list.addAll(assetsDao.selectTangibleAssetsByIp(param.getIp(), param.getId()));
                list.addAll(assetsDao.selectOutbandAssetsByIp(param.getIp(), param.getId()));
            }
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getRunServiceObjectByIp with ip={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.SERVER_RUNSERVEROBJECT_CODE_302011, ErrorConstant.SERVER_RUNSERVEROBJECT_MSG_302011);
        }
    }


    /**
     * 获取资产的可用性
     * 改版 qzg
     *
     * @param param
     * @return
     */
    @Override
    public Reply getAvailableByHostIdTest(QueryAssetsAvailableParam param) {
        try {
            long time1 = 0l;
            long time2 = 0l;
            long time3 = 0l;
            long time4 = 0l;
            //先获取可用性监控项id信息
            QueryAssetsAvailableParam newParam = mwServerManager.getItemIdByAvailableItem(param);
            if (newParam.getItemId() != null && StringUtils.isNotEmpty(newParam.getItemId())) {
                //获取数据信息
                List<MwHistoryDTO> historyDTOList = new ArrayList<>();
                AvailableInfoDTO infoDTO = new AvailableInfoDTO();
                List<AssetsAvailableDTO> colorData = new ArrayList<>();
                AssetsAvailableDTO availableDTO;
                Long startTime = 0L;
                Long endTime = 0L;
                Integer MonitorServerId = 0;
                String ItemId = "";
                Integer valueType = 0;
                Long lastValue = -2L;
                int count = 0;
                Double sum = 0.0;
                int startIndex = 0;
                Calendar calendar = Calendar.getInstance();
                List<MWItemHistoryDtoBySer> lastHisDTOs = new ArrayList<>();
                time1 = System.currentTimeMillis();
                switch (param.getDateType()) {
                    case 1:
                        //按一天
                        endTime = calendar.getTimeInMillis() / 1000L;
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
                        startTime = calendar.getTimeInMillis() / 1000L;
                        //单天的数据
                        List<MWItemHistoryDtoBySer> historyDtos = zabbixManger.HistoryGetByTimeAndHistorySer(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());
                        lastHisDTOs.addAll(historyDtos);
                        break;
                    case 2:
                        //按30天
                        long time11 = System.currentTimeMillis();
                        String startDateStr2 = DateUtils.addDays(DateUtils.formatDate(new Date()), -30);
                        String endDateStr2 = DateUtils.formatDate(new Date());
                        param.setStartDateStr(startDateStr2);
                        param.setEndDateStr(endDateStr2);
                        //30天的数据（改版查询）+当天的数据
                        //前29天数据 从mw_assetsusability_report表中判断获取
//                        lastHisDTOs = getHistoryInfoBydays(newParam, param);
                        long time12 = System.currentTimeMillis();
                        //单天的数据
                        endTime = DateUtils.parse(endDateStr2).getTime() / 1000L;
                        startTime = DateUtils.parse(startDateStr2).getTime() / 1000L;
                        List<MWItemHistoryDtoBySer> historyDtoToDay = new ArrayList<>();
                        historyDtoToDay = zabbixHistoryDataHandle.getAssetsUsability(startTime, endTime, newParam.getMonitorServerId(), newParam.getItemId());//判断是否取趋势数据
                        if (CollectionUtils.isEmpty(historyDtoToDay)) {
                            historyDtoToDay = zabbixManger.HistoryGetByTimeAndHistorySer(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());
                        }
                        long time13 = System.currentTimeMillis();
                        lastHisDTOs.addAll(historyDtoToDay);
                        log.info("获取资产可用性查询耗时：" + (time12 - time11) + "ms；" + (time13 - time12) + "ms；");
                        break;
                    case 3:
                        //           自定义
                        endTime = param.getDateEnd().getTime() / 1000L;
                        startTime = param.getDateStart().getTime() / 1000L;
                        //自定义时间，包含当天时间，则+当天的数据
                        //将时间装换为yyyy-MM-dd 字符串类型
                        String startDateStr3 = DateUtils.formatDate(param.getDateStart());
                        String endDateStr3 = DateUtils.formatDate(param.getDateEnd());
                        param.setStartDateStr(startDateStr3);
                        param.setEndDateStr(endDateStr3);
                        //获得当天0点时间
                        long nowDate3 = DateUtils.getTimesMorning().getTime() / 1000L;
                        List<MWItemHistoryDtoBySer> historyDtoToDays = new ArrayList<>();
                        historyDtoToDays = zabbixHistoryDataHandle.getAssetsUsability(startTime, endTime, newParam.getMonitorServerId(), newParam.getItemId());//判断是否取趋势数据
                        if (CollectionUtils.isEmpty(historyDtoToDays)) {
                            historyDtoToDays = zabbixManger.HistoryGetByTimeAndHistorySer(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());
                        }
                        lastHisDTOs.addAll(historyDtoToDays);
                        break;
                    default:
                        break;
                }
                time2 = System.currentTimeMillis();
                int floor = 0;
                List<MWItemHistoryDtoBySer> lastHisDTO = lastHisDTOs.stream().sorted(Comparator.comparing(s -> Long.valueOf(s.getClock()))).collect(Collectors.toList());
                for (int i = 0; i < lastHisDTO.size(); i++) {
                    historyDTOList.add(MwHistoryDTO.builder()
                            .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(lastHisDTO.get(i).getLastValue()))
                            .value("8")
                            .dateTime(new Date(Long.valueOf(lastHisDTO.get(i).getClock()) * 1000L)).build());
                    if (lastHisDTO.get(i).getLastValue() == 1L || lastHisDTO.get(i).getLastValue() == 2L) {
                        sum++;//为了计算可用率
                    }
                    if (lastValue != lastHisDTO.get(i).getLastValue()) {
                        if ((count - 1) == 0) {
                            availableDTO = new AssetsAvailableDTO();
                            availableDTO.setGt(startIndex);
                            availableDTO.setLte(i);
                            availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                            colorData.add(availableDTO);
                            count--;
                        }
                        lastValue = lastHisDTO.get(i).getLastValue();
                        startIndex = i;
                        count++;
                    }
                    if (i == lastHisDTO.size() - 1) {//最后收尾的值
                        availableDTO = new AssetsAvailableDTO();
                        availableDTO.setGt(startIndex);
                        availableDTO.setLte(i);
                        availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                        colorData.add(availableDTO);
                    }
                    if (i == 0) {
                        i = floor;
                        for (int j = 0; j < floor; j++) {
                            startTime = startTime + 60;
                            historyDTOList.add(MwHistoryDTO.builder()
                                    .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(-1L))
                                    .value("8")
                                    .dateTime(new Date(startTime * 1000L)).build());
                        }
                    }
                }
                int size = lastHisDTO.size();
                time3 = System.currentTimeMillis();
                String per = new BigDecimal(sum * 100 / size).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%";
                infoDTO.setAvailablePer(per);
                infoDTO.setColorData(colorData);
                infoDTO.setHistoryDTOList(historyDTOList);
                log.info("获取资产可用性总耗时：" + (time2 - time1) + "ms；" + (time3 - time2) + "ms；");
                return Reply.ok(infoDTO);
            } else {
                return Reply.fail("此资产暂无可用性监控指标！", 2021);
            }
        } catch (Exception e) {
            logger.error("fail to getAvailableByHostIdTest param{}, case by {}", param, e);
            return Reply.fail(500, "获取资产的可用性失败");
        }
    }

    @Override
    public Reply getAvailableByHostId(QueryAssetsAvailableParam param) {
        try {
            Calendar calendar = Calendar.getInstance();
            Long startTime = 0L;
            Long endTime = 0L;
            switch (param.getDateType()) {
                case 1:
                    //            按一天
                    endTime = calendar.getTimeInMillis() / 1000L;
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
                    startTime = calendar.getTimeInMillis() / 1000L;
                    break;
                case 2:
                    //            按30天
                    endTime = calendar.getTimeInMillis() / 1000L;
                    calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 30);
                    startTime = calendar.getTimeInMillis() / 1000L;
                    break;
                case 3:
                    //           自定义
                    endTime = param.getDateEnd().getTime() / 1000L;
                    startTime = param.getDateStart().getTime() / 1000L;
                    break;
                default:
                    break;
            }
            //先获取可用性监控项id信息
            QueryAssetsAvailableParam newParam = mwServerManager.getItemIdByAvailableItem(param);
            if (newParam.getItemId() != null && StringUtils.isNotEmpty(newParam.getItemId())) {
                AvailableInfoDTO infoDTO = new AvailableInfoDTO();
                //获取数据信息
                List<MwHistoryDTO> historyDTOList = new ArrayList<>();
                List<AssetsAvailableDTO> colorData = new ArrayList<>();
                AssetsAvailableDTO availableDTO;
                Long lastValue = -2L;
                int count = 0;
                Double sum = 0.0;
                int startIndex = 0;

                List<MWItemHistoryDto> lastHisDTO = new ArrayList<>();
                List<MWItemHistoryDto> historyDtos = zabbixManger.HistoryGetByTimeAndHistory(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());


                if (historyDtos != null && historyDtos.size() > 0) {
                    int floor = 0;
                    Long time_start = Long.valueOf(historyDtos.get(0).getClock());
                    // 查看当前时间是否是最开始的时间，如果不是，这段时间为未管理的阶段
                    if ((startTime + 60 * 60) < time_start) {
                        floor = (int) Math.floor((time_start - startTime) / 60);

                        MWItemHistoryDto itemHistoryDto = new MWItemHistoryDto();
                        itemHistoryDto.setLastValue(-1L);
                        itemHistoryDto.setClock(startTime.toString());

                        lastHisDTO.add(itemHistoryDto);

                        lastHisDTO.addAll(Arrays.asList(new MWItemHistoryDto[floor]));
                        lastHisDTO.addAll(historyDtos);
                    } else {
                        lastHisDTO.addAll(historyDtos);
                    }

                    for (int i = 0; i < lastHisDTO.size(); i++) {
                        historyDTOList.add(MwHistoryDTO.builder()
                                .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(lastHisDTO.get(i).getLastValue()))
                                .value("8")
                                .dateTime(new Date(Long.valueOf(lastHisDTO.get(i).getClock()) * 1000L)).build());
                        if (lastHisDTO.get(i).getLastValue() == 1L || lastHisDTO.get(i).getLastValue() == 2L) {
                            sum++;//为了计算可用率
                        }
                        if (lastValue != lastHisDTO.get(i).getLastValue()) {
                            if ((count - 1) == 0) {
                                availableDTO = new AssetsAvailableDTO();
                                availableDTO.setGt(startIndex);
                                availableDTO.setLte(i);
                                availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                                colorData.add(availableDTO);
                                count--;
                            }
                            lastValue = lastHisDTO.get(i).getLastValue();
                            startIndex = i;
                            count++;
                        }
                        if (i == lastHisDTO.size() - 1) {//最后收尾的值
                            availableDTO = new AssetsAvailableDTO();
                            availableDTO.setGt(startIndex);
                            availableDTO.setLte(i);
                            availableDTO.setColor(ZabbixItemConstant.COLORMAP.get(lastValue));
                            colorData.add(availableDTO);
                        }
                        if (i == 0) {
                            i = floor;
                            for (int j = 0; j < floor; j++) {
                                startTime = startTime + 60;
                                historyDTOList.add(MwHistoryDTO.builder()
                                        .lastUpdateValue(ZabbixItemConstant.COLORVALUEMAP.get(-1L))
                                        .value("8")
                                        .dateTime(new Date(startTime * 1000L)).build());
                            }
                        }
                    }
                    int size = historyDtos.size();

                    String per = new BigDecimal(sum * 100 / size).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%";
                    infoDTO.setAvailablePer(per);
                    infoDTO.setColorData(colorData);
                    infoDTO.setHistoryDTOList(historyDTOList);
                } else if (param.getDateType() != 3) {
                    return Reply.fail("此资产暂无可用性监控指标！", 2021);
                }
                logger.info("success to getAvailableByHostId result:{}", infoDTO);
                return Reply.ok(infoDTO);
            } else {
                return Reply.fail("此资产暂无可用性监控指标！", 2021);
            }
        } catch (Exception e) {
            logger.error("fail to getAvailableByHostId errorInfo:{}", e);
            return Reply.fail("fail to getAvailableByHostId errorInfo:{}", e);
        }
    }


    private List<MWItemHistoryDtoBySer> getHistoryInfoBydays(QueryAssetsAvailableParam newParam, QueryAssetsAvailableParam param) {
        long time3 = 0l;
        long time4 = 0l;
        long time5 = 0l;
        long time6 = 0l;
        long time1 = System.currentTimeMillis();
        List<MWItemHistoryDtoBySer> lastHisDTOs = new ArrayList<>();

        Integer MonitorServerId = newParam.getMonitorServerId();
        String ItemId = newParam.getItemId();
        Integer valueType = newParam.getValue_type();

        //查询可用性不为100%且不为0%的时间日期
        List<String> timeErrorStrs = assetsDao.selectErrorAvailableById(param);

        //查询可用性为100%的时间日期
        List<String> timeAllStrs = assetsDao.selectAllAvailableById(param);

        //查询可用性为0%的时间日期
        List<String> timeNoneStrs = assetsDao.selectNoneAvailableById(param);
        long time2 = System.currentTimeMillis();
        String startStr = "";
        String endStr = "";
        Date startDate = null;
        Date endDate = null;
        Long startTime = 0L;
        Long endTime = 0L;
        //获取数据信息
        List<MwHistoryDTO> historyDTOList = new ArrayList<>();
        String yesterday = "";
        Map<String, List<MWItemHistoryDtoBySer>> map = new HashMap();
        List list = new ArrayList();
        List<MWItemHistoryDtoBySer> lists = new ArrayList<>();
        //可用性不为100%且不为0%的数据
        String time = "";
        try {
            if (timeErrorStrs != null && timeErrorStrs.size() > 0) {
                for (String timeStr : timeErrorStrs) {
                    startStr = timeStr + " 00:00:00";
                    endStr = timeStr + " 23:59:59";
                    startDate = DateUtils.parse(startStr);
                    endDate = DateUtils.parse(endStr);
                    startTime = startDate.getTime() / 1000L;
                    endTime = endDate.getTime() / 1000L;
                    List<MWItemHistoryDtoBySer> historyDtos = zabbixManger.HistoryGetByTimeAndHistorySer(MonitorServerId, ItemId, startTime, endTime, valueType);
                    lastHisDTOs.addAll(historyDtos);
                    //将数据出入map,作为100%和0%
                    map.put(timeErrorStrs.get(0), historyDtos);
                    time = timeErrorStrs.get(0);
                }
                if (map.get(timeErrorStrs.get(0)) != null && map.get(timeErrorStrs.get(0)).size() > 0) {
                    lists = map.get(timeErrorStrs.get(0));
                }
                time3 = System.currentTimeMillis();
            } else {
                //获取昨天的时间
                Date yesterdayTime = DateUtils.addDays(new Date(), -1);
                yesterday = DateUtils.formatDate(yesterdayTime);
                startStr = yesterday + " 00:00:00";
                endStr = yesterday + " 23:59:59";
                startDate = DateUtils.parse(startStr);
                endDate = DateUtils.parse(endStr);
                startTime = startDate.getTime() / 1000L;
                endTime = endDate.getTime() / 1000L;
                List<MWItemHistoryDtoBySer> historyDtos = zabbixManger.HistoryGetByTimeAndHistorySer(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());
                map.put(yesterday, historyDtos);
                time = yesterday;
                if (map.get(yesterday) != null && map.get(yesterday).size() > 0) {
                    lists = map.get(yesterday);
                }
                time3 = System.currentTimeMillis();
            }
            if (lists.size() > 0) {
                //可用性为100%的数据
                if (timeAllStrs != null && timeAllStrs.size() > 0) {
                    for (String timeStr : timeAllStrs) {
                        //相差的时间数
                        long betweenNum = DateUtils.between(timeStr, time, SECOND);
                        List<MWItemHistoryDtoBySer> listDTO = ListDeepCopy.deepCopy(lists);
                        for (MWItemHistoryDtoBySer dto : listDTO) {
                            dto.setLastValue(1l);
                            dto.setClock(String.valueOf((Long.valueOf(dto.getClock()) - (betweenNum))));
                        }
                        lastHisDTOs.addAll(listDTO);
                    }
                }
                time5 = System.currentTimeMillis();
                //可用性为0%的数据
                if (timeNoneStrs != null && timeNoneStrs.size() > 0) {
                    for (String timeStr : timeNoneStrs) {
                        //相差的时间数
                        long betweenNum = DateUtils.between(timeStr, time, SECOND);
                        List<MWItemHistoryDtoBySer> listDTO = ListDeepCopy.deepCopy(lists);
                        for (MWItemHistoryDtoBySer dto : listDTO) {
                            dto.setLastValue(0l);
                            dto.setValue("0");
                            dto.setClock(String.valueOf((Long.valueOf(dto.getClock()) - (betweenNum))));
                        }
                        lastHisDTOs.addAll(listDTO);
                    }
                }
                time6 = System.currentTimeMillis();
            }
            log.info("资产可用性耗时：" + (time2 - time1) + "ms；" + (time3 - time2) + "ms；" + (time5 - time3) + "ms；" + (time6 - time5) + "ms；");
        } catch (Exception e) {
            logger.error("fail to getHistoryInfoBydays param{}, case by {}", param, e);
        }
        return lastHisDTOs;
    }


    @Override
    public Reply getChannelInfoList(AssetsIdsPageInfoParam param) {
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            List<ChannelDto> lists = new ArrayList<>();
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(param.getMonitorServerId(), ZabbixItemConstant.MW_DH_CHANNEL, param.getAssetsId(), false);
            JsonNode node = (JsonNode) result.getData();
            logger.info("getChannelInfoList result:{}", result);
            String str = "";
            if (node.size() > 0) {
                str = node.get(0).get("lastvalue").asText();
            }
            logger.info("getChannelInfoList str:{}", str);

            String channelStr = str.replaceAll("\\[", "").replaceAll("]", "")
                    .replaceAll("\"", "").replaceAll("\\\\r", "")
                    .replaceAll("\n", "").replaceAll("\r", "")
                    .replaceAll("\t", "").replaceAll(" ", "");
            String[] split = channelStr.split(",");
            if (split.length > 0) {
                for (String s : split) {
                    lists.add(new ChannelDto(s));
                }
                lists.removeAll(Collections.singleton(new ChannelDto()));
                lists.removeAll(Collections.singleton(new ChannelDto("")));
            }
            pageInfo.setTotal(lists.size());
            lists = pageList.getList(lists, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(lists);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getChannelInfoList with hostid={}, cause:{}", param.getAssetsId(), e);
            return Reply.fail(ErrorConstant.SERVER_DISK_CODE_302003, ErrorConstant.SERVER_DISK_MSG_302003);
        }
    }

    @Override
    public Reply itemCheckNow(int monitorServerId, List<String> itemIds) {
        //将所有的监控项进行是否立即执行 "6"是立即执行；"1"是诊断信息
        MWZabbixAPIResult result = mwtpServerAPI.taskItems(monitorServerId, "6", itemIds);
        if (result != null && !result.isFail()) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                return Reply.ok("刷新成功");
            }
        }
        return Reply.fail("刷新失败");
    }


    /**
     * 根据typeId 获取对应相关联的机构，用户，用户组
     *
     * @param id
     * @param type type的取值在 DataType 枚举里
     * @return
     */
    @Override
    public Reply getRecordByAssetsId(String id, String type) {
        try {
            SysDto sysDto = new SysDto();
            if (null != id && StringUtils.isNotEmpty(id)) {
                List<String> orgName = assetsDao.getOrgNameByAssetsId(id, type);

                List<String> groupName = assetsDao.getGroupNameByAssetsId(id, type);
                List<String> userName = assetsDao.getUserNameByAssetsId(id, type);
                sysDto.setOrgName(orgName);
                sysDto.setGroupNameList(groupName);
                sysDto.setUserNameList(userName);
            }
            return Reply.ok(sysDto);
        } catch (Exception e) {
            log.error("fail to getRecordByAssetsId" +
                    " with param={}, cause:{}", id, e);
            return Reply.fail(ErrorConstant.SERVER_RECORD_CODE_302007, ErrorConstant.SERVER_RECORD_MSG_302007);
        }
    }

    /**
     * 根据应用集名称以及监控项名称获取所有分区名称
     *
     * @param param
     * @return
     */
    @Override
    public Reply getNameListByNameType(TypeFilterDTO param) {
        List nameList = new ArrayList<>();
        List typeItemNames = new ArrayList();
        boolean updateFlag = false;
        try {
            //首先需要确定数据库中是否含有当前TypeItemNames
            TypeFilterDTO typeFilter = itemNameDao.getTypeFilter(param.getTangibleAssetsId(), param.getNameType());
            if (typeFilter != null) {
                updateFlag = true;
                typeItemNames = param.isHasDescription() ? JSONObject.parseArray(typeFilter.getShowData(), DropDownNamesDesc.class)
                        : JSONObject.parseArray(typeFilter.getShowData(), String.class);
            }
            //zabbix中监控数据
            if (param.getNameType() != null && param.getNameType().indexOf("INTERFACE") != -1) {
                nameList = mwServerManager.getNames(param.getMonitorServerId(), param.getAssetsId(), "INTERFACES_INFO", "INTERFACE_NAME", param.isHasDescription());
            } else if ("DISK".equals(param.getNameType())) {
                nameList = mwServerManager.getNames(param.getMonitorServerId(), param.getAssetsId(), "DISK_INFO", "DISK_NAME", param.isHasDescription());
            } else {
                nameList = mwServerManager.getNames(param.getMonitorServerId(), param.getAssetsId(), param.getApplicationName(), param.getNameType(), param.isHasDescription());
            }
            if (!Strings.isNullOrEmpty(param.getOperation())) {
                OperationEnum o = OperationEnum.valueOf(param.getOperation());
                switch (o) {
                    case create:
                        //这时候是查询添加按钮的可供选择的名称(需要排除)
                        nameList.removeAll(typeItemNames);
                        break;
                    case select:
                        //如果数据库中有数据，直接展示数据库中的数据
                        if (updateFlag) {
                            nameList = typeItemNames;
                        } else {
                            //将查到的数据存到数据库保存
//                        param.setShowData(JSONObject.toJSONString(nameList));
//                        itemNameDao.insert(param);
                        }
                        break;
                    case update:
                        if (updateFlag) {
                            if (param.getShowList().size() > 0) {
                                typeItemNames.addAll(param.getShowList());
                            }
                            typeFilter.setShowData(JSONObject.toJSONString(typeItemNames));
                            itemNameDao.update(typeFilter);
                        } else {
                            itemNameDao.insert(param);
                        }
                        break;
                    case delete:

                        if (updateFlag) {
                            typeItemNames.removeAll(param.getShowList());
                            typeFilter.setShowData(JSONObject.toJSONString(typeItemNames));
                            itemNameDao.update(typeFilter);
                        } else {
                            nameList.removeAll(param.getShowList());
                            param.setShowData(JSONObject.toJSONString(nameList));
                            itemNameDao.insert(param);
                        }
                        break;
                    default:
                        break;
                }
            }

        } catch (Exception e) {
            if ("INTERFACE".equals(param.getNameType())) {
                nameList = mwServerManager.getNames(param.getMonitorServerId(), param.getAssetsId(), "INTERFACES_INFO", "INTERFACE_NAME", param.isHasDescription());
            } else if ("DISK".equals(param.getNameType())) {
                nameList = mwServerManager.getNames(param.getMonitorServerId(), param.getAssetsId(), "DISK_INFO", "DISK_NAME", param.isHasDescription());
            } else {
                nameList = mwServerManager.getNames(param.getMonitorServerId(), param.getAssetsId(), param.getApplicationName(), param.getNameType(), param.isHasDescription());
            }
        }
        if ("INTERFACE".equals(param.getNameType())) {
            Collections.sort(nameList, new Comparator<DropDownNamesDesc>() {
                @Override
                public int compare(DropDownNamesDesc o1, DropDownNamesDesc o2) {
                    if (o1.getName().contains(VMXNET3)) {
                        return -1;
                    }
                    if (o2.getName().contains(VMXNET3)) {
                        return 1;
                    }
                    return Collator.getInstance(Locale.CHINESE).compare(o1.getName(), o2.getName());
                }
            });
        }
        return Reply.ok(nameList);
    }

    /**
     * @param param
     * @return
     */
    @Override
    public Reply getNetDataList(AssetsIdsPageInfoParam param) {
        try {
            PageInfo pageInfo = new PageInfo<List>();
            PageList pageList = new PageList();
            List<NetListDto> listDtos = getNetDataListAll(param);
            pageInfo.setTotal(listDtos.size());
            if (param.getSortInfo() != null) {
                listDtos = sortNetList(listDtos, param.getSortInfo());
            }
            listDtos = pageList.getList(listDtos, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(listDtos);
            logger.info("SERVER_LOG[]getNetDataList[]根据一个hostid获取网络名字的数据[]{}[]", param.getAssetsId());
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getNetDataList with hostid={}, cause:{}", param.getAssetsId(), e);
            return Reply.fail(ErrorConstant.SERVER_NET_CODE_302004, ErrorConstant.SERVER_NET_MSG_302004);
        }
    }

    /**
     * @param param
     * @return
     */
    @Override
    public List<NetListDto> getNetDataAllList(AssetsIdsPageInfoParam param) {
        List<NetListDto> listDtos = new ArrayList<>();
        try {
            listDtos = getNetDataListAll(param);
        } catch (Exception e) {
            log.error("fail to getNetDataList with hostid={}, cause:{}", param.getAssetsId(), e);
        }
        return listDtos;
    }

    /**
     * @param param
     * @return
     */
    @Override
    public Reply getNetNumCount(AssetsIdsPageInfoParam param) {
        try {
            NetListInfoDto dto = new NetListInfoDto();
            List<NetListDto> listDtos = getNetDataListAll(param);
            Integer allNum = listDtos.size();
            long upNum = listDtos.stream().filter(s -> "up".equals(s.getState())).count();
            long dowmNum = listDtos.stream().filter(s -> "down".equals(s.getState())).count();
            dto.setAllNum((long) allNum);
            dto.setUpNum(upNum);
            dto.setDownNum(dowmNum);
            return Reply.ok(dto);
        } catch (Exception e) {
            log.error("fail to getNetNumCount", param, e);
            return Reply.fail(ErrorConstant.SERVER_NET_CODE_302004, ErrorConstant.SERVER_NET_MSG_302004);
        }
    }

    /**
     * 所有资产从zabbix上更新
     *
     * @return
     */
    @Override
    public Reply getAllNetDataListByZabbix() {
        try {
            long time1 = System.currentTimeMillis();
            List<AssetsIdsPageInfoParam> paramList = new ArrayList<>();
            //资产类型查询资产设备,服务器，网络设备，安全设备
            List<Integer> typeIds = Arrays.asList(1, 2, 3);
            if (modelAssetEnable) {
                QueryEsParam queryEsParam = new QueryEsParam();
                QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
                qParam.setPropertiesIndexId(ASSETTYPE_ID_KEY);
                qParam.setPropertiesValueList(typeIds);
                queryEsParam.setParamLists(Arrays.asList(qParam));
                List<Map<String, Object>> hostList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
                log.info("新资产数据::"+hostList);
                for (Map<String, Object> m : hostList) {
                    AssetsIdsPageInfoParam param = new AssetsIdsPageInfoParam();
                    param.setId(strValueConvert(m.get(INSTANCE_ID_KEY)));
                    param.setAssetsId(strValueConvert(m.get(ASSETS_ID)));
                    param.setAssetsIp(strValueConvert(m.get(IN_BAND_IP)));
                    param.setMonitorServerId(intValueConvert(m.get(MONITOR_SERVER_ID)));
                    paramList.add(param);
                }
            } else {
                List<MwTangibleassetsTable> list = mwTangibleAssetsService.selectAssetsListByTypeIds(typeIds);
                log.info("老资产数据::"+list);
                for (MwTangibleassetsTable table : list) {
                    AssetsIdsPageInfoParam param = new AssetsIdsPageInfoParam();
                    param.setId(table.getId());
                    param.setAssetsId(table.getAssetsId());
                    param.setAssetsIp(table.getInBandIp());
                    param.setMonitorServerId(table.getMonitorServerId());
                    paramList.add(param);
                }
            }
            log.info("资产数据参数getAllNetDataListByZabbix::"+paramList);
            getNetDataListByZabbix(paramList, new ArrayList<>(), new ArrayList<>());
            long time2 = System.currentTimeMillis();
            log.info("getAllNetDataListByZabbix::时间1：" + (time2 - time1) + "ms");
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to getAllNetDataListByZabbix with cause:{}", e);
            return Reply.fail(500, "同步zabbix接口失败");
        }
    }

    @Override
    public void getNetDataListByZabbix(List<AssetsIdsPageInfoParam> paramList, List<NetListDto> listDtos, List<String> netNameList) {
        List<String> itemNames = Arrays.asList(InterfaceInfoEnum.MW_INTERFACE_OUT_TRAFFIC.getName(),
                InterfaceInfoEnum.MW_INTERFACE_IN_TRAFFIC.getName(),
                InterfaceInfoEnum.MW_INTERFACE_OUT_DROPPED.getName(),
                InterfaceInfoEnum.MW_INTERFACE_IN_DROPPED.getName(),
                InterfaceInfoEnum.MW_INTERFACE_SPEED.getName(),
                InterfaceInfoEnum.INTERFACE_IN_UTILIZATION.getName(),
                InterfaceInfoEnum.INTERFACE_OUT_UTILIZATION.getName(),
                InterfaceInfoEnum.INTERFACE_MTU.getName(),
                InterfaceInfoEnum.INTERFACE_INDEX.getName(),
                INTERFACE_DESCR.getName(),
                InterfaceInfoEnum.MW_INTERFACE_STATUS.getName());
        Map<Integer, List<String>> groupMap = new HashedMap();
        Map<String, Map<String, Object>> collect1 = new HashedMap();
        groupMap = paramList.stream().filter(s -> s.getMonitorServerId() != 0).collect(Collectors.groupingBy(s -> intValueConvert(s.getMonitorServerId()), Collectors.mapping(s -> s.getAssetsId(), Collectors.toList())));
        for (AssetsIdsPageInfoParam param : paramList) {
            String key = intValueConvert(param.getAssetsId()) + "_" + intValueConvert(param.getMonitorServerId());
            Map<String, Object> map = new HashedMap();
            map.put(INSTANCE_ID_KEY, param.getId());
            map.put(IN_BAND_IP, param.getAssetsIp());
            collect1.put(key, map);
        }
        long time1 = System.currentTimeMillis();

        Map<String, Map<String, Object>> finalCollect = collect1;
        groupMap.forEach((k, v) -> {
            List<String> hostIds = v;
            List<List<String>> hostIdsGroups = null;
            if (null != hostIds) {
                hostIdsGroups = Lists.partition(hostIds, assetsSize);
            }
            //多线程处理zabbix请求
            int coreSizePool = 15;
            coreSizePool = (coreSizePool > hostIdsGroups.size()) ? hostIdsGroups.size() : coreSizePool;
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, 18, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            List<Future<MWZabbixAPIResult>> futureList = new ArrayList<>();
            List<MWZabbixAPIResult> listInfo = new ArrayList<>();
            for (List<String> hostIdList : hostIdsGroups) {
                Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                    @Override
                    public MWZabbixAPIResult call() throws Exception {
                        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyHostIdsSearch(k, itemNames, hostIdList);
                        return result;
                    }
                };
                Future<MWZabbixAPIResult> submit = executorService.submit(callable);
                futureList.add(submit);
            }
            if (futureList.size() > 0) {
                futureList.forEach(f -> {
                    try {
                        MWZabbixAPIResult result = f.get(5, TimeUnit.MINUTES);
                        listInfo.add(result);
                    } catch (Exception e) {
                        log.error("fail to itemGetbyHostIdsSearch:多线程等待数据返回失败 param:{},cause:{}", e);
                    }
                });
            }
            executorService.shutdown();
            log.info("关闭线程池");
            log.info("zabbix请求返回数据::" + listInfo);
            for (MWZabbixAPIResult result : listInfo) {
                AssetsIdsPageInfoParam param = new AssetsIdsPageInfoParam();
                param.setMonitorServerId(k);
                dataConvert(result, param, listDtos, netNameList, finalCollect);
            }
        });
        long time2 = System.currentTimeMillis();
        log.info("get zabbix netDevice::" + listDtos);
        if(CollectionUtils.isNotEmpty(listDtos)){
            insertDataByZabbixInfo(listDtos);
        }
        long time3 = System.currentTimeMillis();
        log.info("getNetDataListByOne时间1：" + (time2 - time1) + "ms；时间2：" + (time3 - time2) + "ms;");
    }

    private void dataConvert(MWZabbixAPIResult result, AssetsIdsPageInfoParam param, List<NetListDto> listDtos, List<String> netNameList, Map<String, Map<String, Object>> collect1) {
        if (result != null && !result.isFail()) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode != null && jsonNode.size() > 0) {
                List<ItemGetDTO> itemGetDTOS = JSONObject.parseArray(result.getData().toString(), ItemGetDTO.class);
                log.info("获取的zabbix接口数据::" + itemGetDTOS);
                List<String> valuemapIds = new ArrayList<>();
                for (ItemGetDTO item : itemGetDTOS) {
                    String valuemapid = item.getValuemapid();
                    valuemapIds.add(valuemapid);
                }
                List<String> valuemapIdList = valuemapIds.stream().distinct().collect(Collectors.toList());
                Map<String, Map> valueMapByIdMap = mwServerManager.getValueMapByIdList(param.getMonitorServerId(), valuemapIdList);

                Map<String, List<ItemGetDTO>> collect = itemGetDTOS.stream().collect(Collectors.groupingBy(ItemGetDTO::getOriginalType));
                for (Map.Entry<String, List<ItemGetDTO>> value : collect.entrySet()) {
                    List<ItemGetDTO> dtos = value.getValue();
                    NetListDto netListDto = new NetListDto();
                    netListDto.setInterfaceName(value.getKey().substring(1, value.getKey().length() - 1));
                    Double inTrafficValue = null;
                    Double outTrafficValue = null;
                    for (ItemGetDTO dto : dtos) {
                        String name = dto.getName();
                        if ("MW_INTERFACE_IN_TRAFFIC".equals(name)) {
                            inTrafficValue = dto.getSortLastValue();
                        }
                        if ("MW_INTERFACE_OUT_TRAFFIC".equals(name)) {
                            outTrafficValue = dto.getSortLastValue();
                        }
                    }
                    Double finalInTrafficValue = inTrafficValue;
                    Double finalOutTrafficValue = outTrafficValue;

                    dtos.forEach(itemGetDTO -> {

                        netListDto.setHostId(itemGetDTO.getHostid());
                        if (collect1 != null && collect1.containsKey(itemGetDTO.getHostid() + "_" + param.getMonitorServerId())) {
                            Map<String, Object> mapInfo = collect1.get(itemGetDTO.getHostid() + "_" + param.getMonitorServerId());
                            netListDto.setAssetsId(strValueConvert(mapInfo.get(INSTANCE_ID_KEY)));
                            netListDto.setHostIp(strValueConvert(mapInfo.get(IN_BAND_IP)));
                        }
                        String name = itemGetDTO.getName();
                        String lastvalue = itemGetDTO.getLastvalue();
                        Double sortLastValue = itemGetDTO.getSortLastValue();

                        InterfaceInfoEnum itemName = InterfaceInfoEnum.valueOf(name.substring(name.indexOf("]") + 1));
                        switch (itemName) {
                            case MW_INTERFACE_OUT_TRAFFIC:
                                netListDto.setOutBps(lastvalue);
                                netListDto.setSortOutBps(sortLastValue);
                                break;
                            case MW_INTERFACE_IN_TRAFFIC:
                                netListDto.setInBps(lastvalue);
                                netListDto.setSortInBps(sortLastValue);
                                break;
                            case MW_INTERFACE_OUT_DROPPED:
                                netListDto.setSendLoss(lastvalue);
                                netListDto.setSortSendLoss(sortLastValue);
                                break;
                            case MW_INTERFACE_IN_DROPPED:
                                netListDto.setAcceptLoss(lastvalue);
                                netListDto.setSortAcceptLoss(sortLastValue);
                                break;
                            case MW_INTERFACE_SPEED:
                                netListDto.setRate(lastvalue);
                                netListDto.setSortRate(sortLastValue);
                                break;
                            case INTERFACE_IN_UTILIZATION:
                                String val = lastvalue.replaceAll("[a-zA-Z%]", "");
                                BigDecimal bValue = new BigDecimal(val);
                                if (val.indexOf(".") != -1) {
                                    val = bValue.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
                                }
                                if (finalInTrafficValue.intValue() == 0) {
                                    netListDto.setInBpsRatio("0");
                                    netListDto.setSortInBpsRatio(0.0);
                                } else {
                                    netListDto.setInBpsRatio(val);
                                    netListDto.setSortInBpsRatio(sortLastValue);
                                }
                                break;
                            case INTERFACE_OUT_UTILIZATION:
                                String outVal = lastvalue.replaceAll("[a-zA-Z%]", "");
                                BigDecimal outValue = new BigDecimal(outVal);
                                if (outVal.indexOf(".") != -1) {
                                    outVal = outValue.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
                                }
                                if (finalOutTrafficValue.intValue() == 0) {
                                    netListDto.setOutBpsRatio("0");
                                    netListDto.setSortOutBpsRatio(0.0);
                                } else {
                                    netListDto.setOutBpsRatio(outVal);
                                    netListDto.setSortOutBpsRatio(sortLastValue);
                                }
                                break;
                            case MW_INTERFACE_STATUS:
                                String valuemapid = itemGetDTO.getValuemapid();
                                if (valueMapByIdMap != null && valueMapByIdMap.size() > 0 &&
                                        valueMapByIdMap.get(valuemapid) != null && valueMapByIdMap.get(valuemapid).get(lastvalue) != null) {
                                    String newvalue = valueMapByIdMap.get(valuemapid).get(lastvalue).toString();
                                    if (!Strings.isNullOrEmpty(newvalue)) {
                                        netListDto.setState(newvalue);
                                    }
                                }
                                break;
                            case INTERFACE_DESCR:
                                netListDto.setInterfaceDescr(lastvalue);
                                break;
                            case INTERFACE_MTU:
                                netListDto.setMTU(lastvalue != null ? Integer.valueOf(lastvalue) : 0);
                                break;
                            case INTERFACE_INDEX:
                                netListDto.setInterfaceIndex(lastvalue != null ? Integer.valueOf(lastvalue) : 0);
                                break;
                            case INTERFACE_MACADDR:
                                netListDto.setMacaddr(lastvalue);
                                break;
                            default:
                                break;
                        }
                    });
                    listDtos.add(netListDto);
                    netNameList.add(netListDto.getInterfaceName());
                }
            }
        }
    }


    private void insertDataByZabbixInfo(List<NetListDto> listDtos) {
        Set<String> collect = listDtos.stream().filter(s -> !Strings.isNullOrEmpty(s.getHostId())).map(s -> s.getHostId()).collect(Collectors.toSet());
        log.info("get zabbix netDevice::" + listDtos);
        //第一次进入从数据库中获取所有设备的所有接口数据
        Reply replyBeform = mwModelCommonService.getAllInterfaceNameAndHostId(new ArrayList<>(collect));
        List<ModelInterfaceDTO> assetsInterfaceBeformList = (List<ModelInterfaceDTO>) replyBeform.getData();
        log.info("第一次进入从数据库中获取设备数据::" + assetsInterfaceBeformList);
        Set<String> scanNames = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(assetsInterfaceBeformList)) {
            scanNames = assetsInterfaceBeformList.stream().filter(s->!Strings.isNullOrEmpty(s.getName())).map(s -> s.getName().toLowerCase() + "_" + s.getHostId()).collect(Collectors.toSet());
        }
        //问题：现在的snmp扫描有可能扫描不到接口数据，但是zabbix上的接口信息存在，
        // 所以将zabbix上获取的接口数据同步到数据库中，可以进行后续操作（修改接口描述和设置接口告警）
        //zabbix同步的数据进行过滤，过滤掉数据库中已存在的数据，只保留zabbix有而数据库中没有的，插入数据库
        List<NetListDto> newListDtos = new ArrayList<>(listDtos);
        Iterator<NetListDto> its = newListDtos.iterator();
        while (its.hasNext()) {
            NetListDto m = its.next();
            if(m.getInterfaceName()!=null){
                String lowerName = m.getInterfaceName().toLowerCase() + "_" + m.getHostId();
                if (scanNames.contains(lowerName)) {
                    its.remove();
                }
            }
        }
        log.info("过滤后的数据zabbix的数据" + newListDtos);
        List<AssetsInterfaceDTO> assetsDtoList = new ArrayList<>();
        String user = iLoginCacheInfo.getLoginName();
        Date curDate = new Date();
        //过滤后的数据，保存到数据库中
        for (NetListDto netListDto : newListDtos) {
            if(Strings.isNullOrEmpty(netListDto.getInterfaceName())){
                continue;
            }
            AssetsInterfaceDTO assetsDto = new AssetsInterfaceDTO();
            //主机监控Id
            assetsDto.setHostId(netListDto.getHostId());
            //资产实例Id
            assetsDto.setAssetsId(netListDto.getAssetsId());
            //主机监控IP
            assetsDto.setHostIp(netListDto.getHostIp());
            assetsDto.setName(netListDto.getInterfaceName());
            assetsDto.setDescription(netListDto.getInterfaceDescr());
            assetsDto.setState(netListDto.getState());
            assetsDto.setIfIndex(netListDto.getInterfaceIndex() != null ? netListDto.getInterfaceIndex() : 0);
            assetsDto.setMtu(netListDto.getMTU());
            assetsDto.setMac(netListDto.getMacaddr());
            assetsDto.setAlertTag(false);
            assetsDto.setShowFlag(true);
            assetsDto.setCreator(user);
            assetsDto.setCreateDate(curDate);
            assetsDto.setModifier(user);
            assetsDto.setModificationDate(curDate);
            assetsDtoList.add(assetsDto);
        }
        long time3 = System.currentTimeMillis();
        log.info("批量插入的数量:" + assetsDtoList.size());
        //批量插入，每900条执行一次
        if (CollectionUtils.isNotEmpty(assetsDtoList)) {
            List<List<AssetsInterfaceDTO>> subGroups = new ArrayList<>();
            subGroups = Lists.partition(assetsDtoList, insBatchFetchNum);
            if (null != subGroups) {
                for (List<AssetsInterfaceDTO> subList : subGroups) {
                    mwModelCommonService.insertInterface(subList);
                }
            }
        }
    }


    /**
     * @param param
     * @return
     */
    public List<NetListDto> getNetDataListAll(AssetsIdsPageInfoParam param) {
        List<NetListDto> listDtos = new ArrayList<>();
        List<NetListDto> listDisDtos = new ArrayList<>();
        List<String> netNameList = new ArrayList<>();
        try {
            getNetDataListByZabbix(Arrays.asList(param), listDtos, netNameList);
            Map<String, NetListDto> maps = listDtos.stream().collect(Collectors.toMap(s -> s.getInterfaceName(), s -> s, (
                    value1, value2) -> {
                return value2;
            }));
            //再次获取设备的所有接口信息,带查询参数
            Reply reply = mwModelCommonService.getAllAssetsInterface(param);
            List<AssetsInterfaceDTO> assetsInterfaceList = (List<AssetsInterfaceDTO>) reply.getData();
            if (CollectionUtils.isEmpty(assetsInterfaceList) && !param.isQueryFlag()) {
                listDisDtos = listDtos;
            } else {
                List<AssetsInterfaceDTO> disAssetsInterfaceList = assetsInterfaceList.stream().filter(s -> s.getShowFlag() != null && s.getShowFlag()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(disAssetsInterfaceList)) {
                    listDisDtos = new ArrayList<>();
                    for (AssetsInterfaceDTO dto : disAssetsInterfaceList) {
                        NetListDto netListDto = new NetListDto();
                        if (maps != null && maps.containsKey(dto.getName())) {
                            NetListDto netListDtoZabbix = maps.get(dto.getName());
                            netListDtoZabbix.setId(dto.getId());
                            netListDtoZabbix.setAlertTag(dto.getAlertTag());
                            netListDtoZabbix.setInterfaceDescr(dto.getDescription());
                            listDisDtos.add(netListDtoZabbix);
                        }
                        if (!netNameList.contains(dto.getName())) {
                            netListDto.setAlertTag(dto.getAlertTag());
                            netListDto.setId(dto.getId());
                            netListDto.setInterfaceDescr(dto.getDescription());
                            netListDto.setInterfaceName(dto.getName());
                            netListDto.setState(dto.getState());
                            netListDto.setOutBpsRatio("0");
                            netListDto.setSortOutBpsRatio(0.0);
                            netListDto.setInBpsRatio("0");
                            netListDto.setSortInBpsRatio(0.0);
                            netListDto.setOutBps("0");
                            netListDto.setSortOutBps(0.0);
                            netListDto.setInBps("0");
                            netListDto.setSortInBps(0.0);
                            netListDto.setSendLoss("0");
                            netListDto.setSortSendLoss(0.0);
                            netListDto.setAcceptLoss("0");
                            netListDto.setSortAcceptLoss(0.0);
                            netListDto.setRate("0");
                            netListDto.setSortRate(0.0);
                            listDisDtos.add(netListDto);
                        }
                    }
                }
            }
            long time3 = System.currentTimeMillis();
            Collections.sort(listDisDtos, new AlphanumericComparator());

            if (isFilter != null && Boolean.valueOf(isFilter)) {
                //接口过滤
                List<String> disNameList = new ArrayList<>();
                List<String> noStartWithList = new ArrayList<>();
                MwModelFilterInterfaceParam filterInfo = mwAssetsInterfaceDao.getFilterInfo();
                if (filterInfo != null) {
                    if (!Strings.isNullOrEmpty(filterInfo.getFilterField())) {
                        disNameList = Arrays.asList(filterInfo.getFilterField().split(","));
                    }
                    if (!Strings.isNullOrEmpty(filterInfo.getNoStartWith())) {
                        noStartWithList = Arrays.asList(filterInfo.getNoStartWith().split(","));
                    }
                }
                if (CollectionUtils.isNotEmpty(listDisDtos)) {
                    //过滤接口名称不含有指定的字段
                    for (String name : disNameList) {
                        Iterator<NetListDto> it = listDisDtos.iterator();
                        while (it.hasNext()) {
                            NetListDto m = it.next();
                            if(m.getInterfaceName()!=null && name!=null){
                                int x = (m.getInterfaceName().toLowerCase()).indexOf(name.toLowerCase());
                                if (x != -1) {
                                    it.remove();
                                }
                            }
                        }
                    }
                    //过滤接口不以指定字段开头的
                    Iterator<NetListDto> it = listDisDtos.iterator();
                    while (it.hasNext()) {
                        NetListDto m = it.next();
                        if(m.getInterfaceName()!=null) {
                            String lowerName = m.getInterfaceName().toLowerCase();
                            if (myStartWith(lowerName, noStartWithList)) {
                                it.remove();
                            }
                        }

                    }
                }
            }

            //全字段查询
            if (!Strings.isNullOrEmpty(param.getFuzzyQuery())) {
                listDisDtos = listDisDtos.stream().filter(s ->
                        (s.getInterfaceName() != null && s.getInterfaceName().indexOf(param.getFuzzyQuery()) != -1) ||
                                (s.getInterfaceDescr() != null && s.getInterfaceDescr().indexOf(param.getFuzzyQuery()) != -1) ||
                                (s.getState() != null && s.getState().indexOf(param.getFuzzyQuery()) != -1) ||
                                ((s.getAlertTag() != null && s.getAlertTag().toString().equals(param.getFuzzyQuery())) || ("false".equals(param.getFuzzyQuery()) && s.getAlertTag() == null))
                ).collect(Collectors.toList());
            }

            //接口名称查询
            if (!Strings.isNullOrEmpty(param.getInterfaceName())) {
                listDisDtos = listDisDtos.stream().filter(s -> (s.getInterfaceName() != null && s.getInterfaceName().indexOf(param.getFuzzyQuery()) != -1)).collect(Collectors.toList());
            }

            //接口描述查询
            if (!Strings.isNullOrEmpty(param.getInterfaceDescr())) {
                listDisDtos = listDisDtos.stream().filter(s -> (s.getInterfaceDescr() != null && s.getInterfaceDescr().indexOf(param.getInterfaceDescr()) != -1)).collect(Collectors.toList());
            }

            //状态查询
            if (!Strings.isNullOrEmpty(param.getState())) {
                listDisDtos = listDisDtos.stream().filter(s -> (s.getState() != null && s.getState().indexOf(param.getState()) != -1)).collect(Collectors.toList());
            }

            //告警标记查询
            if (param.getAlertTag() != null) {
                listDisDtos = listDisDtos.stream().filter(s -> (s.getAlertTag() != null && s.getAlertTag().equals(param.getAlertTag())) || ("false".equals(param.getAlertTag().toString()) && s.getAlertTag() == null)).collect(Collectors.toList());
            }
            long time4 = System.currentTimeMillis();
            log.info("SERVER_LOG[]getNetDataList[]根据一个hostid获取网络名字的数据[]{}[]", param.getAssetsId());
            log.info("时间3：" + (time4 - time3) + "ms", param.getAssetsId());

        } catch (Exception e) {
            log.error("fail to getNetDataList with hostid={}, cause:{}", param.getAssetsId(), e);
        }
        return listDisDtos;
    }

    private Boolean myStartWith(String nameStr, List<String> strList) {
        for (String str : strList) {
            if (str!=null && nameStr.startsWith(str.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

//    @Override
//    public Reply getNetDataList(AssetsIdsPageInfoParam param) {
//        try {
//            PageInfo pageInfo = new PageInfo<List>();
//            PageList pageList = new PageList();
//            List<NetListDto> listDtos = new ArrayList<>();
//            //获得hostId的接口名称应用集
//            List<String> interfaceNames = mwServerManager.getNames(param.getMonitorServerId(), param.getAssetsId(), "INTERFACES_INFO", "INTERFACE_NAME", false);
//            pageInfo.setTotal(interfaceNames.size());
//            Integer maximumPoolSize = interfaceNames.size();
//            if (param.getSortInfo() == null) {//如果不排序的话正常查询分页的内容
//                interfaceNames = pageList.getList(interfaceNames, param.getPageNumber(), param.getPageSize());
//            }
//            if (interfaceNames.size() > 0) {
//
//                int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
//                coreSizePool = (coreSizePool < interfaceNames.size()) ? coreSizePool : interfaceNames.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
//                ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, maximumPoolSize, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
//                List<Future<NetListDto>> futureList = new ArrayList<>();
//
//                interfaceNames.forEach(name -> {
//                    GetNetListThread getNetListThread = new GetNetListThread() {
//                        @Override
//                        public NetListDto call() throws Exception {
//                            return getNetListDto(param, name);
//                        }
//                    };
//                    Future<NetListDto> f = executorService.submit(getNetListThread);
//                    futureList.add(f);
//                });
//                for (Future<NetListDto> f : futureList) {
//                    try {
//                        NetListDto netListDto = f.get(10, TimeUnit.SECONDS);
//                        listDtos.add(netListDto);
//                    } catch (Exception e) {
//                        f.cancel(true);
//                    }
//                }
//
//                executorService.shutdown();
//                logger.info("关闭线程池");
//            }
//            if (param.getSortInfo() != null) {
//                listDtos = sortNetList(listDtos, param.getSortInfo());
//                listDtos = pageList.getList(listDtos, param.getPageNumber(), param.getPageSize());
//            }
//            pageInfo.setList(listDtos);
//            logger.info("SERVER_LOG[]getNetDataList[]根据一个hostid获取网络名字的数据[]{}[]", param.getAssetsId());
//            return Reply.ok(pageInfo);
//        } catch (Exception e) {
//            log.error("fail to getNetDataList with hostid={}, cause:{}", param.getAssetsId(), e);
//            return Reply.fail(ErrorConstant.SERVER_NET_CODE_302004, ErrorConstant.SERVER_NET_MSG_302004);
//        }
//    }

    @Override
    public Reply getNavigationBarByApplication(QueryNavigationBarParam param) {
        List<NavigationBarDTO> titleList = new ArrayList<>();
        OperationEnum o = null;
        try {
            titleList.add(new NavigationBarDTO(0, "概览"));
            if (null != param.getAssetsId() && StringUtils.isNotEmpty(param.getAssetsId())) {
                //当资产有关联带外资产时，优先选择带外资产中所监控的硬件数据
                Boolean isHave = mwServerManager.getIsHaveMonitorInfo(param.getOutBandIp());

                if (isHave) {
                    titleList.add(new NavigationBarDTO(-1, ZbApplicationNameEnum.valueOf("HARDWARE").getChName()));
                }
                MWZabbixAPIResult result = mwtpServerAPI.getApplication(param.getMonitorServerId(), param.getAssetsId());
                List<String> strList = Arrays.asList("FAN_SENSORS", "POWER_SENSORS", "TEMPERATURE_SENSORS", "BATTERY_SENSOR", "HARDWARE");
                Set set = new HashSet();
                if (result != null && !result.isFail()) {
                    String data = String.valueOf(result.getData());
                    List<ApplicationDTO> lists = resultResolver.analysisResult(mwtpServerAPI.getServerType(param.getMonitorServerId()), data);
                    if (lists.size() > 0) {
                        lists.forEach(list -> {
                            if ("DISK".equals(list.getName())) {
                                titleList.add(new NavigationBarDTO(-2, ZbApplicationNameEnum.valueOf(list.getName()).getChName()));
                            }
                            if ("INTERFACES".equals(list.getName())) {
                                titleList.add(new NavigationBarDTO(-3, ZbApplicationNameEnum.valueOf(list.getName()).getChName()));
                            }
                            if ("SOFTWARE".equals(list.getName())) {
                                titleList.add(new NavigationBarDTO(-4, ZbApplicationNameEnum.valueOf(list.getName()).getChName()));
                            }
                            if (strList.contains(list.getName().trim().toUpperCase())) {
                                if (!isHave && (!set.contains(ZbApplicationNameEnum.valueOf("HARDWARE").getChName()))) {
                                    set.add(ZbApplicationNameEnum.valueOf("HARDWARE").getChName());
                                    titleList.add(new NavigationBarDTO(-1, ZbApplicationNameEnum.valueOf("HARDWARE").getChName()));
                                }
                            }
                            if ("CHANNEL_INFO".equals(list.getName())) {
                                titleList.add(new NavigationBarDTO(-6, ZbApplicationNameEnum.valueOf(list.getName()).getChName()));
                            }
                            if ("Always_On".equals(list.getName())) {
                                titleList.add(new NavigationBarDTO(-7, ZbApplicationNameEnum.valueOf(list.getName()).getChName()));
                            }
                            if (list.getName().contains(ZbApplicationNameEnum.PROCESS_TOP.getName())) {
                                titleList.add(new NavigationBarDTO(-9, ZbApplicationNameEnum.PROCESS_TOP.getChName()));
                            }

                        });
                    }
                }
            }
            if (modelAssetEnable) {
                titleList.add(new NavigationBarDTO(-8, ZbApplicationNameEnum.ALERT_TRIGGER_INFO.getChName()));
            }
            o = OperationEnum.valueOf(param.getOperation());
        } catch (Exception e) {
            log.error("fail to getNavigationBarByApplication with param={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.SERVER_NAVIGATION_BAR_CODE_302006, ErrorConstant.SERVER_NAVIGATION_BAR_MSG_302006);
        }

        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        if (param.getDefaultFlag() != null && param.getDefaultFlag()) {
            //默认全局布局，同类型的共同使用
            switch (o) {
                case create:
                    //先操作数据库
                    myMonitorDao.insertNavigationBar(param);
                    break;
                case select:
                    break;
                case update:
                    //自定义布局修改设为全局布局
                    if (param.getFlag() != null && param.getDefaultFlag() != null && param.getFlag() != param.getDefaultFlag()) {
                        //判断全局布局可有该数据，有，则删除自定义布局中的删除标志数据，没有新增全局数据
                        Integer num = myMonitorDao.checkByNavigation(param);
                        if (num > 0) {
                            //删除自定义布局中的删除标识数据
                            param.setType(1);//删除标识
                            myMonitorDao.deleteCustomNavigationBar(param);
                            param.setType(0);//新增标识
                            myMonitorDao.deleteCustomNavigationBar(param);
                            myMonitorDao.updateBycustomNavigationBarId(param);

                            //查询修改之前的全局布局是否有数据
                            List<AddOrUpdateComLayoutParam> layoutAll = myMonitorDao.selectByFilter(userId, param.getMonitorServerId(), param.getTemplateId(), false, param.getCustomNavigationBarId(), null);
                            if (layoutAll.size() <= 0) {
                                layoutAll = myMonitorDao.selectByFilter(null, param.getMonitorServerId(), param.getTemplateId(), true, param.getCustomNavigationBarId(), null);
                            }
                            //查询当前的自定义的布局数据
                            List<AddOrUpdateComLayoutParam> layoutParams = myMonitorDao.selectByFilter(userId, param.getMonitorServerId(), param.getTemplateId(), false, param.getNavigationBarId(), Integer.valueOf(param.getAssetsId()));
                            if (layoutParams.size() <= 0) {
                                layoutParams = myMonitorDao.selectByFilter(null, param.getMonitorServerId(), param.getTemplateId(), true, param.getNavigationBarId(), Integer.valueOf(param.getAssetsId()));
                            }
                            //修改前全局布局数据，没有则新增，有数据则修改
                            if (layoutAll.size() < 0) {
                                if (layoutParams.size() > 0) {
                                    AddOrUpdateComLayoutParam dto = CopyUtils.copy(AddOrUpdateComLayoutParam.class, layoutParams.get(0));
                                    String jsonObject = layoutParams.get(0).getComponentLayout();
                                    dto.setComponentLayout(jsonObject);
                                    dto.setAssetsId(null);
                                    dto.setNavigationBarId(param.getCustomNavigationBarId());
                                    //布局详情表新增自定义布局数据
                                    myMonitorDao.insert(dto);
                                }
                            } else {
                                AddOrUpdateComLayoutParam dto = CopyUtils.copy(AddOrUpdateComLayoutParam.class, layoutAll.get(0));
                                if (layoutParams.size() > 0) {
                                    String jsonObject = layoutParams.get(0).getComponentLayout();
                                    dto.setComponentLayout(jsonObject);
                                }
                                myMonitorDao.update(dto);
                            }
                        } else {
                            Integer navigationBarId = param.getNavigationBarId();
                            param.setType(0);//删除新增标识
                            myMonitorDao.deleteCustomNavigationBarByAdd(param);
                            myMonitorDao.insertNavigationBar(param);
                            //查询当前的自定义的布局数据
                            List<AddOrUpdateComLayoutParam> layoutParams = myMonitorDao.selectByFilter(userId, param.getMonitorServerId(), param.getTemplateId(), false, navigationBarId, Integer.valueOf(param.getAssetsId()));
                            if (layoutParams.size() <= 0) {
                                layoutParams = myMonitorDao.selectByFilter(null, param.getMonitorServerId(), param.getTemplateId(), true, navigationBarId, Integer.valueOf(param.getAssetsId()));
                            }
                            if (layoutParams.size() > 0) {
                                AddOrUpdateComLayoutParam dto = CopyUtils.copy(AddOrUpdateComLayoutParam.class, layoutParams.get(0));
                                String jsonObject = layoutParams.get(0).getComponentLayout();
                                dto.setComponentLayout(jsonObject);
                                dto.setAssetsId(null);
                                dto.setNavigationBarId(param.getNavigationBarId());
                                //布局详情表新增自定义布局数据
                                myMonitorDao.insert(dto);
                            }
                        }
                    } else {
                        //直接修改数据
                        myMonitorDao.updateNavigationBar(param);
                    }
                    break;
                case delete:
                    if (param.getFlagList() != null && param.getFlagList().size() > 0) {
                        for (int x = 0; x < param.getFlagList().size(); x++) {
                            //自定义布局删除时设为全局布局
                            if (param.getFlagList().get(x) != null && param.getDefaultFlag() != null && param.getFlagList().get(x) != param.getDefaultFlag()) {
                                //判断自定义布局改为全局布局
                                //删除原自定义布局中的新增标识数据
                                param.setType(0);//新增标识
                                param.setCustomNavigationBarId(param.getCustomNavigationBarIdList().get(x));
                                //判断删除的tab标签是否是固定标签(id<0)
                                if (param.getNavigationBarIds().get(x) <= 0) {
                                    AddNavigationBarByDeleteFlag deleteFlag = new AddNavigationBarByDeleteFlag();
                                    deleteFlag.setAssetsId(param.getAssetsId());
                                    deleteFlag.setTemplateId(param.getTemplateId());
                                    deleteFlag.setBarId(param.getNavigationBarIds().get(x));
                                    deleteFlag.setDefaultFlag(param.getDefaultFlag());
                                    if (param.getDefaultFlag()) {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteTemplate(deleteFlag);
                                    } else {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteAssets(deleteFlag);
                                    }
                                } else {
                                    myMonitorDao.deleteCustomNavigationBar(param);
                                    myMonitorDao.deleteComponentLayoutByCustom(param.getNavigationBarId(), param.getAssetsId());
                                }
                            } else {
                                //判断删除的tab标签是否是固定标签(id<0)
                                if (param.getNavigationBarIds().get(x) <= 0) {
                                    //删除标识表新增一条数据
                                    AddNavigationBarByDeleteFlag deleteFlag = new AddNavigationBarByDeleteFlag();
                                    deleteFlag.setAssetsId(param.getAssetsId());
                                    deleteFlag.setTemplateId(param.getTemplateId());
                                    deleteFlag.setBarId(param.getNavigationBarIds().get(x));
                                    deleteFlag.setDefaultFlag(param.getDefaultFlag());
                                    if (param.getDefaultFlag()) {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteTemplate(deleteFlag);
                                    } else {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteAssets(deleteFlag);
                                    }
                                } else {
                                    //直接删除数据
                                    myMonitorDao.deleteNavigationBar(param);
                                    myMonitorDao.delete(param.getNavigationBarIds());
                                }

                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        } else {
            //当前资产自定义table页
            switch (o) {
                case create:
                    //插入自定义导航栏表
                    param.setType(0);
                    myMonitorDao.insertCustomNavigationBar(param);
                    break;
                case select:
                    break;
                case update:
                    //全局布局修改时设为自定义布局
                    if (param.getFlag() != null && param.getDefaultFlag() != null && param.getFlag() != param.getDefaultFlag()) {
                        //插入删除标识数据(删除标识是为了去除全局布局中的数据)
                        param.setType(1);
                        int count = myMonitorDao.checkByCustomNavigation(param);
                        if (count == 0) {
                            myMonitorDao.insertCustomNavigationBar(param);
                        }
                        //插入新增标识数据(新增标识是为了添加自定义布局中的数据)
                        param.setType(0);
                        int count1 = myMonitorDao.checkByCustomNavigation(param);
                        boolean isInsert = false;
                        //判断自定义布局数据是否新增，如果新增，将新增主键id存入ComLayout表中作为NavigationBarId使用
                        if (count1 == 0) {
                            myMonitorDao.insertCustomNavigationBar(param);
                            isInsert = true;
                        }
                        //先查询默认布局的布局数据，将布局数据插入到自定义布局中
                        List<AddOrUpdateComLayoutParam> layoutParams = myMonitorDao.selectByFilter(userId, param.getMonitorServerId(), param.getTemplateId(), false, param.getNavigationBarId(), null);
                        if (layoutParams.size() <= 0) {
                            layoutParams = myMonitorDao.selectByFilter(null, param.getMonitorServerId(), param.getTemplateId(), true, param.getNavigationBarId(), null);
                        }
                        if (layoutParams.size() > 0) {
                            AddOrUpdateComLayoutParam dto = CopyUtils.copy(AddOrUpdateComLayoutParam.class, layoutParams.get(0));
                            String jsonObject = layoutParams.get(0).getComponentLayout();
                            dto.setComponentLayout(jsonObject);
                            dto.setAssetsId(Integer.valueOf(param.getAssetsId()));
                            if (isInsert) {
                                dto.setNavigationBarId(Integer.valueOf(param.getId()));
                            } else {
                                dto.setNavigationBarId(param.getNavigationBarId());
                            }
                            //布局详情表新增自定义布局数据
                            myMonitorDao.insert(dto);
                        }
                    } else {
                        //直接修改数据
                        myMonitorDao.updateCustomNavigationBar(param);
                    }
                    break;
                case delete:
                    if (param.getFlagList() != null && param.getFlagList().size() > 0) {
                        for (int x = 0; x < param.getFlagList().size(); x++) {
                            //全局布局删除时设为自定义布局
                            if (param.getFlagList().get(x) != null && param.getDefaultFlag() != null && param.getFlagList().get(x) != param.getDefaultFlag()) {
                                //插入删除标识数据
                                param.setType(1);
                                param.setNavigationBarId(param.getNavigationBarIds().get(x));
                                //判断删除的tab标签是否是固定标签(id<0)
                                if (param.getNavigationBarIds().get(x) <= 0) {
                                    AddNavigationBarByDeleteFlag deleteFlag = new AddNavigationBarByDeleteFlag();
                                    deleteFlag.setAssetsId(param.getAssetsId());
                                    deleteFlag.setTemplateId(param.getTemplateId());
                                    deleteFlag.setBarId(param.getNavigationBarIds().get(x));
                                    deleteFlag.setDefaultFlag(param.getDefaultFlag());
                                    deleteFlag.setDefaultFlag(param.getDefaultFlag());
                                    if (param.getDefaultFlag()) {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteTemplate(deleteFlag);
                                    } else {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteAssets(deleteFlag);
                                    }
                                } else {
                                    myMonitorDao.insertCustomNavigationBar(param);
                                }
                            } else {
                                //直接删除本地新增数据
                                param.setType(0);
                                param.setCustomNavigationBarId(param.getCustomNavigationBarIdList().get(x));
                                //判断删除的tab标签是否是固定标签(id<0)
                                if (param.getNavigationBarIds().get(x) <= 0) {
                                    //删除标识表新增一条数据
                                    AddNavigationBarByDeleteFlag deleteFlag = new AddNavigationBarByDeleteFlag();
                                    deleteFlag.setAssetsId(param.getAssetsId());
                                    deleteFlag.setTemplateId(param.getTemplateId());
                                    deleteFlag.setBarId(param.getNavigationBarIds().get(x));
                                    deleteFlag.setDefaultFlag(param.getDefaultFlag());
                                    if (param.getDefaultFlag()) {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteTemplate(deleteFlag);
                                    } else {
                                        //删除标识表新增一条数据
                                        myMonitorDao.insertCustomNavigationByDeleteAssets(deleteFlag);
                                    }
                                } else {
                                    myMonitorDao.deleteCustomNavigationBar(param);
                                    myMonitorDao.deleteComponentLayoutByCustom(param.getNavigationBarIds().get(x), param.getAssetsId());
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        //再从数据库中查找是否存在自定义导航栏
        List<NavigationBarDTO> navigationBarDTOS = myMonitorDao.selectNavigationBar(param.getTemplateId(), param.getAssetsId());
        titleList.addAll(navigationBarDTOS);
        titleList.add(new NavigationBarDTO(-5, "指标详情"));//指标详情放到最后
        //查询除有删除标识的NavigationBarId
        List<AddNavigationBarByDeleteFlag> deleteFlagList = myMonitorDao.getAllNavigationBarByDeleteFlag(param);
        //去除删除标识的数据
        List<NavigationBarDTO> titleLists = titleList.stream().parallel().filter(a -> deleteFlagList.stream().noneMatch(b -> Objects.equals(a.getNavigationBarId(), b.getBarId()))).collect(Collectors.toList());
        return Reply.ok(titleLists);
    }

    /**
     * 获取详情页table标签列表接口
     * 数据库结构已修改，有需要的从这个接口获取数据
     *
     * @param templateId
     * @param assetsId
     * @return
     */
    @Override
    public Reply selectNavigationBar(String templateId, String assetsId) {
        try {
            List<NavigationBarDTO> navigationBarDTOS = myMonitorDao.selectNavigationBar(templateId, assetsId);
            return Reply.ok(navigationBarDTOS);
        } catch (Exception e) {
            log.error("fail to selectNavigationBar cause:{}", templateId, assetsId, e);
            return Reply.fail(500, "获取详情页table标签列表失败");
        }
    }

    /**
     * 根据monitorServerId,hostId 查询持续运行时间和资产状态（正常或异常）
     *
     * @param monitorServerId 第三方监控服务器id
     * @param hostId          zabbix中对应资产的hostId
     * @return
     */
    @Override
    public Reply getDurationAndStatusByHostId(int monitorServerId, String hostId) {
        try {
            HostDetailDto hostDetailDto = new HostDetailDto();
            long lastvalue = 0;
            String longTime = "";
            for (Map.Entry<Integer, String> entry : ZabbixItemConstant.ASSETS_UPTIME_ITEM_NAMES.entrySet()) {
                MWZabbixAPIResult result = mwtpServerAPI.ItemGetBykey(monitorServerId, hostId, entry.getValue());
                if (result != null && !result.isFail()) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode.size() > 0) {
                        lastvalue = jsonNode.get(0).get("lastvalue").asLong();
                    }
                }
                if (lastvalue != 0) {
                    longTime = SeverityUtils.getLastTime(lastvalue);
                    break;
                }
            }

            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyNameList(monitorServerId, Arrays.asList(ZabbixItemConstant.CPU_CORE, ZabbixItemConstant.MEMORY_TOTAL), hostId, true);
            if (result != null && !result.isFail()) {
                JsonNode jsonNode = (JsonNode) result.getData();
                if (jsonNode.size() > 0) {
                    jsonNode.forEach(node -> {
                        String name = node.get("name").asText();
                        if (ZabbixItemConstant.CPU_CORE.equals(name)) {
                            hostDetailDto.setCpuCores(node.get("lastvalue").asInt());
                        }
                        if (ZabbixItemConstant.MEMORY_TOTAL.equals(name)) {
                            hostDetailDto.setTotalMemory(UnitsUtil.getValueWithUnits(node.get("lastvalue").asText(), node.get("units").asText()));
                        }
                    });

                }
            }

            hostDetailDto.setDuration(longTime);
            return Reply.ok(hostDetailDto);
        } catch (Exception e) {
            log.error("fail to getDurationByHostId with monitorServerId={} hostid={}, cause:{}", monitorServerId, hostId, e);
            return Reply.fail(ErrorConstant.SERVER_DURATION_AND_STATUS_CODE_302012, ErrorConstant.SERVER_DURATION_AND_STATUS_MSG_302012);
        }
    }

    @Override
    public Reply getHistoryByItemId(ItemLineParam iParam) {
        //通过itemid 然后查询itemid的最新的60条的history
        try {
            HistoryListDto dto = new HistoryListDto();
            List<MwHistoryDTO> historyDTOList = new ArrayList<>();
            if (null != iParam.getItemId() && StringUtils.isNotEmpty(iParam.getItemId())) {
                List<MWItemHistoryDto> list = zabbixManger.HistoryGetByTimeAndHistory(
                        iParam.getMonitorServerId(), iParam.getItemId(), iParam.getHistory(), 60);
                if (list.size() > 0) {
                    list.forEach(
                            respDto -> {
                                historyDTOList.add(MwHistoryDTO.builder()
                                        .value(("uptime".equals(iParam.getUnits()) ? SeverityUtils.getLastTime(Long.valueOf(Double.valueOf(respDto.getValue()).intValue())) :
                                                (MwServerService.NUMERAL.equals(iParam.getValue_type()))
                                                        ? UnitsUtil.getValueAndUnits(respDto.getValue(), iParam.getUnits()).get("value")
                                                        : respDto.getValue()))
                                        .dateTime(new Date(Long.valueOf(respDto.getClock()) * 1000L)).build());
                            }
                    );
                    Map<String, String> historyyUnits = UnitsUtil.getValueAndUnits(list.get(0).getValue(), iParam.getUnits());
                    dto.setUnit(historyyUnits.get("units"));
                    if ("uptime".equals(iParam.getUnits())) {
                        dto.setUnit("");
                    }
                    dto.setTitleName(iParam.getItemName());
                    dto.setDataList(historyDTOList);
                }
            }
            return Reply.ok(dto);
        } catch (Exception e) {
            log.error("fail to getHistoryByItemId with ItemLineParam={}, cause:{}", iParam, e);
            return Reply.fail(ErrorConstant.SERVER_HISTORY_CODE_302002, ErrorConstant.SERVER_HISTORY_MSG_302002);
        }
    }

    @Override
    public Reply getHistoryByItemIds(List<ItemLineParam> itemLineParams, int monitorServerId) {

        List<HistoryListDto> ret = new ArrayList<>();
        Map<String, String> unitMap = new HashedMap();
        Map<String, String> valueTypeMap = new HashedMap();
        Map<Integer, Map<String, HistoryListDto>> historyMap = new HashedMap();
        Map<Integer, List<String>> historyItemIdMap = new HashedMap();

        for (ItemLineParam itemLineParam : itemLineParams) {
            String itemId = itemLineParam.getItemId();

            unitMap.put(itemId, itemLineParam.getUnits());
            valueTypeMap.put(itemId, itemLineParam.getValue_type());
            Map<String, HistoryListDto> dtoMap = historyMap.get(itemLineParam.getHistory());
            if (null == dtoMap) {
                dtoMap = new HashedMap();
                historyMap.put(itemLineParam.getHistory(), dtoMap);
            }

            List<String> itemids = historyItemIdMap.get(itemLineParam.getHistory());
            if (null == itemids) {
                itemids = new ArrayList<>();
                historyItemIdMap.put(itemLineParam.getHistory(), itemids);
            }
            itemids.add(itemId);

            if (null == dtoMap.get(itemLineParam.getItemId())) {
                HistoryListDto dto = new HistoryListDto();
                dto.setTitleName(itemLineParam.getItemName());
                List<MwHistoryDTO> historyDTOList = new ArrayList<>();
                dto.setDataList(historyDTOList);
                dto.setUnit(itemLineParam.getUnits());
                dtoMap.put(itemLineParam.getItemId(), dto);
            }
        }

        Date till = new Date();
        Date from = DateUtils.addHours(till, historyByItemIdsTimeInterval);
        long fromLong = from.getTime() / 1000;
        long tillLong = till.getTime() / 1000;

        for (Integer history : historyMap.keySet()) {
            List<String> itemids = historyItemIdMap.get(history);
            Map<String, HistoryListDto> dtoMap = historyMap.get(history);
            List<MWItemHistoryDto> list = zabbixManger.HistoryGetByTimeAndItemIds(
                    monitorServerId, itemids, fromLong, tillLong, history);

            if (list.size() > 0) {
                list.forEach(
                        respDto -> {
                            String unit = unitMap.get(respDto.getItemid());
                            String valueType = valueTypeMap.get(respDto.getItemid());
                            HistoryListDto dto = dtoMap.get(respDto.getItemid());
                            List<MwHistoryDTO> historyDTOList = dto.getDataList();

                            historyDTOList.add(MwHistoryDTO.builder()
                                    .value(("uptime".equals(unit) ? SeverityUtils.getLastTime(Long.valueOf(Double.valueOf(respDto.getValue()).intValue())) :
                                            (MwServerService.NUMERAL.equals(valueType))
                                                    ? UnitsUtil.getValueAndUnits(respDto.getValue(), unit).get("value")
                                                    : respDto.getValue()))
                                    .dateTime(new Date(Long.valueOf(respDto.getClock()) * 1000L)).build());
                        }
                );
            }

            ret.addAll(dtoMap.values());
        }

        return Reply.ok(ret);
    }

    @Override
    public Reply getHardwareByHostId(QueryApplicationTableParam param) {
        try {
            if (null != param.getOutBandIp() && StringUtils.isNotEmpty(param.getOutBandIp())) {
                AssetsBaseDTO outAssetsInfo = tangibleOutbandDao.getOutHostId(param.getOutBandIp());
                if (null != outAssetsInfo && StringUtils.isNotEmpty(outAssetsInfo.getAssetsId())) {
                    param.setAssetsId(outAssetsInfo.getAssetsId());
                    param.setMonitorServerId(outAssetsInfo.getMonitorServerId());
                }
            }
            param.setHardwareFlag(true);
            ApplicationTableInfos hardwareInfos = myMonitorCommons.getApplicationTableInfos(param);

            if (param.isLimitFlag()) {
                Map<String, List<Map<String, Object>>> allData = hardwareInfos.getAllData();
                Map<String, PageInfo> pageInfoData = new HashMap<>();
                for (Map.Entry<String, List<Map<String, Object>>> entry : allData.entrySet()) {
                    PageInfo pageInfo = new PageInfo<List>();
                    PageList pageList = new PageList();
                    pageInfo.setTotal(entry.getValue().size());
                    List list = pageList.getList(entry.getValue(), param.getPageNumber(), param.getPageSize());
                    pageInfo.setList(list);
                    pageInfoData.put(entry.getKey(), pageInfo);
                }
                hardwareInfos.setPageInfo(pageInfoData);
            }
            return Reply.ok(hardwareInfos);
        } catch (Exception e) {
            log.error("fail to getHardwareByHostId with param={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.SERVER_HARDWARE_INFO_CODE_302018, ErrorConstant.SERVER_HARDWARE_INFO_MSG_302018);
        }
    }

    @Override
    public Reply getDiskInfo(int monitorServerId, String name, String hostId) {
        DiskListDto diskInfo = getDiskInfoByDiskName(monitorServerId, name, hostId);
        diskInfo.setUpdateTime(DateUtils.now());
        return Reply.ok(diskInfo);
    }

    @Override
    public Reply getMonitoringItems(int monitorServerId, String hostId, String itemName) {
        try {
            List<ItemApplication> items = itemsGet(monitorServerId, hostId, itemName);
            return Reply.ok(items);
        } catch (Exception e) {
            log.error("fail to getMonitoringItems with hostid={}, cause:{}", hostId, e);
            return Reply.fail(ErrorConstant.HOST_MONITORINGITEMHISTORY_CODE_302010, ErrorConstant.HOST_MONITORINGITEMHISTORY_MSG_302010);
        }
    }

    @Override
    public Reply getHistoryDataInfo(ServerHistoryDto serverHistoryDto) {
        try {
            List<HistoryListDto> list = getLineChartHistory(serverHistoryDto);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getHistoryDataInfo with ServerHistoryDto={}, cause:{}", serverHistoryDto, e);
            return Reply.fail(ErrorConstant.HOST_MONITORINGITEM_CODE_302009, ErrorConstant.HOST_MONITORINGITEM_MSG_302009);
        }
    }

    /**
     * 根据资产Id 和ip 获取所有拥有相同IP的有形和带外资产数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getRunServiceObjectByIp(RunServiceObjectParam param) {
        try {
            PageInfo pageInfo = new PageInfo<List>();
            List<AssetsDTO> list = new ArrayList<>();
            if (null != param.getIp() && StringUtils.isNotEmpty(param.getIp()) && param.getType() != 2) {
                list.addAll(assetsDao.selectTangibleAssetsByIp(param.getIp(), param.getId()));
//                list.addAll(assetsDao.selectOutbandAssetsByIp(param.getIp(), param.getId()));
            }
            if (null != param.getIp() && StringUtils.isNotEmpty(param.getIp()) && param.getType() == 2) {
                list.addAll(assetsDao.selectOutbandAssetsByIp(param.getIp(), param.getId()));
//                list.addAll(assetsDao.selectOutbandAssetsByIp(param.getIp(), param.getId()));
            }
            PageList pageList = new PageList();
            List newList = pageList.getList(list, param.getPageNumber(), param.getPageSize());
            if (list.size() > 0) {
                pageInfo.setPages(pageList.getPages());
                pageInfo.setPageNum(param.getPageNumber());
                pageInfo.setEndRow(pageList.getEndRow());
                pageInfo.setStartRow(pageList.getStartRow());
            }
            pageInfo.setList(newList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getRunServiceObjectByIp with ip={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.SERVER_RUNSERVEROBJECT_CODE_302011, ErrorConstant.SERVER_RUNSERVEROBJECT_MSG_302011);
        }
    }

    /**
     * 根据zabbix返回信息处理成数组
     *
     * @param result
     * @return
     */
    private List<AlarmDTO> getDataAlarm(MWZabbixAPIResult result, String webMonitorName) {
        List<AlarmDTO> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode resultData = (JsonNode) result.getData();
            if (resultData.size() > 0) {
                for (JsonNode problem : resultData) {
                    AlarmDTO alarmDto = new AlarmDTO();
                    alarmDto.setEventid(problem.get("eventid").asText());
                    alarmDto.setName(problem.get("name").asText());
                    alarmDto.setSeverity(Utils.SEVERITY.get(problem.get("severity").asText()));
                    alarmDto.setClock(SeverityUtils.getDate(problem.get("clock").asLong()));
                    alarmDto.setLongTime(SeverityUtils.CalculateTime(SeverityUtils.getDate(problem.get("clock").asLong())));
                    alarmDto.setObjectid(problem.get("objectid").asText());
                    list.add(alarmDto);
                }
                if (webMonitorName != null && StringUtils.isNotEmpty(webMonitorName)) {
                    list = list.stream().filter(x -> x.getName() != null && x.getName().indexOf(webMonitorName) != -1).collect(Collectors.toList());
                }
            }
        }

        return list;
    }

    private List<ItemData> getDataResult(int monitorServerId, MWZabbixAPIResult result, String flag) {
        JsonNode resultData = (JsonNode) result.getData();
        List<ItemData> list = new ArrayList<>();
        if (result.getCode() == 0 && resultData.size() > 0) {
            resultData.forEach(item -> {
                ItemData itemData = new ItemData();
                String name = item.get("name").asText();
                itemData.setName(name);
                if (!"0".equals(item.get("valuemapid").asText())) {
                    String newValue = mwServerManager.getValueMapById(monitorServerId, item.get("valuemapid").asText(), item.get("lastvalue").asText());
                    if (null != newValue && StringUtils.isNotEmpty(newValue)) {
                        itemData.setValue(newValue);
                    }
                } else {
                    if ("0".equals(item.get("value_type").asText()) || "3".equals(item.get("value_type").asText())) {
                        String dataUnits = UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText());
                        itemData.setValue(dataUnits);
                    } else {
                        itemData.setValue(item.get("lastvalue").asText());
                    }
                }
                itemData.setValuemapId(item.get("valuemapid").asText());
                String chName = "";
//                0表示中文转换后不带[]
                if ("0".equals(flag)) {
                    chName = mwServerManager.getChNameWithout(name);
                    if (null != chName && StringUtils.isNotEmpty(chName)) {
                        itemData.setChName(chName);
                    }

                    list.add(itemData);
                }
//                1表示带[]的中文转换
                if ("1".equals(flag)) {
                    chName = mwServerManager.getChName(name);
                    itemData.setChName(chName);
                    list.add(itemData);
                }

            });
        }
        return list;
    }


    public List<HistoryListDto> getHistory(ServerHistoryDto serverHistoryDto) {
        log.info("getHistoryData 获取所有历史数据，生成CPU利用率折线图,开始运行");
        //通过hostid获得不同的itemid 然后查询itemid的history
        try {
            Boolean flag = false;
            String keyPrefix = "";
            List<String> names = serverHistoryDto.getName();
            List<ItemHistoryDto> itemList = new ArrayList<>();
            List<HistoryListDto> lists = new ArrayList<>();
            List<String> itemIds = new ArrayList<>();
            String units = "";
            String delay = "";
            Integer type = 0;
            for (String name : names) {
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyType(serverHistoryDto.getMonitorServerId(), name, serverHistoryDto.getAssetsId(), false);
                JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                String itemId = "";
                Integer history = 0;
                if (node.size() > 0) {
                    for (JsonNode n : node) {
                        itemId = n.get("itemid").asText();
                        history = n.get("value_type").asInt();
                        ItemHistoryDto itemHistoryDto = new ItemHistoryDto();
                        itemHistoryDto.setHistory(history);
                        itemHistoryDto.setItemId(itemId);
                        itemHistoryDto.setName(n.get("name").asText());
                        itemHistoryDto.setUnits(n.get("units").asText());
                        units = n.get("units").asText();
                        delay = n.get("delay").asText();
//                        itemHistoryDto.setChName(itemChName.get(name));
                        itemList.add(itemHistoryDto);
                        itemIds.add(itemId);

                    }
                    type = history;
                }
            }
            Long startTime = 0L;
            Calendar calendar = Calendar.getInstance();
            Long endTime = calendar.getTimeInMillis() / 1000L;
            if (serverHistoryDto.getDateType() == 1) {
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                startTime = calendar.getTimeInMillis() / 1000L;
            } else if (serverHistoryDto.getDateType() == 2) {
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                startTime = calendar.getTimeInMillis() / 1000L;
            } else if (serverHistoryDto.getDateType() == 3) {
                flag = true;
                keyPrefix = serverHistoryDto.getId() + ":15mins:";
            } else if (serverHistoryDto.getDateType() == 4) {
                flag = true;
                keyPrefix = serverHistoryDto.getId() + ":60mins:";
            } else if (serverHistoryDto.getDateType() == 5) {
                //           自定义
                startTime = DateUtils.parse(serverHistoryDto.getDateStart()).getTime() / 1000L;
                endTime = DateUtils.parse(serverHistoryDto.getDateEnd()).getTime() / 1000L;
            }
            HistoryListDto dto = new HistoryListDto();
            List<MwHistoryDTO> historyDTOList = new ArrayList<>();
            dto.setLastUpdateTime(SeverityUtils.getDate(new Date()));
            List<List<MWItemHistoryDto>> zabbixList = new ArrayList<>();
            List<List<RedisItemHistoryDto>> redisList = new ArrayList<>();
            for (ItemHistoryDto item : itemList) {
                if (!flag) {
                    List<MWItemHistoryDto> list = zabbixManger.HistoryGetByTimeAndHistory(serverHistoryDto.getMonitorServerId(), item.getItemId(), startTime, endTime, item.getHistory());
                    zabbixList.add(list);
                } else {
                    String key = keyPrefix + item.getItemId() + item.getName();
                    Set<String> range = redisTemplate.opsForZSet().range(key, 0, -1);
                    List<RedisItemHistoryDto> list = new ArrayList<>();
                    for (String str : range) {
                        if (null != str && StringUtils.isNotEmpty(str)) {
                            RedisItemHistoryDto redisItemHistoryDto = JSONObject.parseObject(str, RedisItemHistoryDto.class);
                            list.add(redisItemHistoryDto);
                        }
                    }
                    redisList.add(list);
                }
            }


            if (!flag) {
                if (zabbixList.size() > 0) {
//                    接收有值的数组下标
                    Integer index = 0;
                    for (int k = 0; k < zabbixList.size(); k++) {
//                        防止list.size()为0
                        if (zabbixList.get(k).size() > 0) {
                            index = k;
                            break;
                        }
                    }
                    for (int i = 0; i < zabbixList.get(index).size(); i++) {
                        Double sum = 0.0;
                        int invalidNum = 0;
                        for (int j = 0; j < zabbixList.size(); j++) {
                            if (!(zabbixList.get(j).size() > 0)) {
                                invalidNum++;
                                continue;
                            }
                            sum += Double.valueOf(zabbixList.get(j).get(i).getValue());
                        }
                        sum = sum / (zabbixList.size() - invalidNum);
                        zabbixList.get(index).get(i).setValue(sum.toString());
                    }
                    String finalUnits = units;
                    zabbixList.get(index).forEach(
                            respDto -> {
                                historyDTOList.add(MwHistoryDTO.builder()
                                        .value(new BigDecimal(respDto.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP).toString())
                                        .dateTime(new Date(Long.valueOf(respDto.getClock()) * 1000L)).build());
                            }
                    );
                    if (historyDTOList.size() > 0) {
                        //最后一条数据的值
                        BigDecimal lastValue = new BigDecimal(historyDTOList.get(historyDTOList.size() - 1).getValue());
                        dto.setLastUpdateValue(lastValue.toString());
                    }
                    dto.setUnit(units);
                    dto.setDelay(delay);
                    dto.setTitleName(ZabbixItemConstant.CPU_UTILIZATION);
                    dto.setDataList(historyDTOList);
                    lists.add(dto);
                }
            } else {
                if (redisList.size() > 0) {
//                    接收有值的数组下标
                    Integer index = 0;
                    for (int k = 0; k < redisList.size(); k++) {
//                        防止list.size()为0
                        if (redisList.get(k).size() > 0) {
                            index = k;
                            break;
                        }
                    }
                    for (int i = 0; i < redisList.get(index).size(); i++) {
                        Double avg = 0.0;
                        Double max = 0.0;
                        Double min = 0.0;
                        int invalidNum = 0;
                        for (int j = 0; j < redisList.size(); j++) {
                            if (!(redisList.get(j).size() > 0)) {
                                invalidNum++;
                                continue;
                            }
                            avg += Double.valueOf(redisList.get(j).get(i).getAvgValue());
                            max += Double.valueOf(redisList.get(j).get(i).getMaxValue());
                            min += Double.valueOf(redisList.get(j).get(i).getMinValue());
                        }
                        avg = avg / (redisList.size() - invalidNum);
                        max = max / (redisList.size() - invalidNum);
                        min = min / (redisList.size() - invalidNum);
                        redisList.get(index).get(i).setAvgValue(avg.toString());
                        redisList.get(index).get(i).setMaxValue(max.toString());
                        redisList.get(index).get(i).setMinValue(min.toString());
                    }
                    for (RedisItemHistoryDto respDto : redisList.get(index)) {
                        String value = "";
                        if ("AVG".equals(serverHistoryDto.getValueType())) {
                            value = respDto.getAvgValue();
                        } else if ("MAX".equals(serverHistoryDto.getValueType())) {
                            value = respDto.getMaxValue();
                        } else if ("MIN".equals(serverHistoryDto.getValueType())) {
                            value = respDto.getMinValue();
                        }
                        respDto.setValue(value);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            historyDTOList.add(MwHistoryDTO.builder()
                                    .value(new BigDecimal(respDto.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP).toString())
                                    .dateTime(simpleDateFormat.parse(respDto.getUpdateTime())).build());
                        } catch (ParseException e) {
                            log.error("ParseException", e);
                        }
                    }
                    if (historyDTOList.size() > 0) {
                        //最后一条数据的值
                        BigDecimal lastValue = new BigDecimal(historyDTOList.get(historyDTOList.size() - 1).getValue());
                        dto.setLastUpdateValue(lastValue.toString());
                    }
                    dto.setUnit(units);
                    dto.setDelay(delay);
                    dto.setTitleName(ZabbixItemConstant.CPU_UTILIZATION);
                    dto.setDataList(historyDTOList);
                    lists.add(dto);
                }
            }
            log.info("SERVER_LOG[]getHistoryData[]根据一个hostid获取不同的itemid[]查询所有的itemid在某个时间段的所有数据[]{}[]", serverHistoryDto);
            log.info("getHistoryData 获取所有历史数据，生成折线图,运行成功结束");
            return lists;
        } catch (Exception e) {
            log.error("fail to getHistoryData with serverHistoryDto={}, cause:{}", serverHistoryDto, e.getMessage());
            log.info("getHistoryData 获取所有历史数据，生成折线图,运行失败结束");
            log.error("error stack", e);
            return null;
        }

    }

    //根据lastValue找到list中的最大值的数组下标
    public Integer getMaxListByMWItemHistoryDto(List<MWItemHistoryDto> list) {
        Long max = 0L;
        Integer index = 0;
        for (int i = 0; i < list.size(); i++) {
            Long lastValue = list.get(i).getLastValue();
            if (max < lastValue) {
                max = lastValue;
                index = i;
            }
        }
        return index;
    }

    /**
     * 根据条件获取不同时间段处理过的历史数据
     *
     * @param serverHistoryDto
     * @return
     */
    public List<HistoryListDto> getLineChartHistory(ServerHistoryDto serverHistoryDto) {
        List<HistoryListDto> lists = new ArrayList<>();
        List<ItemApplication> list = serverHistoryDto.getItemApplicationList();
        if (list.size() > 0) {
//            当list不为空时有意义
            DateTypeDTO dateTypeParams = getDateTypeParams(serverHistoryDto.getDateType(), serverHistoryDto.getId(), DateUtils.parse(serverHistoryDto.getDateStart()), DateUtils.parse(serverHistoryDto.getDateEnd()));
//            获取峰值单位以及未处理的历史数据
            Map<String, Object> map = getPeakValue(serverHistoryDto.getMonitorServerId(), dateTypeParams, list, serverHistoryDto.getValueType());
//            对数据进行处理
            for (ItemApplication item : list) {
                HistoryListDto dto = new HistoryListDto();
                List<MwHistoryDTO> historyDTOList = new ArrayList<>();

                if (!dateTypeParams.getFlag()) {
                    List<MWItemHistoryDto> MWItemHistoryDtos = (List<MWItemHistoryDto>) map.get(item.getItemid());
                    if (MWItemHistoryDtos.size() > 0) {
                        MWItemHistoryDtos.forEach(
                                respDto -> {
                                    historyDTOList.add(MwHistoryDTO.builder()
                                            .value(UnitsUtil.getValueMap(respDto.getValue(), (String) map.get("units"), item.getUnits()).get("value"))
                                            .dateTime(new Date(Long.valueOf(respDto.getClock()) * 1000L)).build());
                                }
                        );
                    }
                } else {
                    List<RedisItemHistoryDto> redisItemHistoryDtos = (List<RedisItemHistoryDto>) map.get(item.getItemid());
                    if (redisItemHistoryDtos.size() > 0) {
                        for (RedisItemHistoryDto respDto : redisItemHistoryDtos) {
                            String value = "";
                            if ("AVG".equals(serverHistoryDto.getValueType())) {
                                value = respDto.getAvgValue();
                            } else if ("MAX".equals(serverHistoryDto.getValueType())) {
                                value = respDto.getMaxValue();
                            } else if ("MIN".equals(serverHistoryDto.getValueType())) {
                                value = respDto.getMinValue();
                            }
                            respDto.setValue(value);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                historyDTOList.add(MwHistoryDTO.builder()
                                        .value(UnitsUtil.getValueMap(respDto.getValue(), (String) map.get("units"), item.getUnits()).get("value"))
                                        .dateTime(simpleDateFormat.parse(respDto.getUpdateTime())).build());
                            } catch (ParseException e) {
                                log.error("ParseException", e);
                            }
                        }
                    }
                }
                if (historyDTOList.size() > 0) {
                    //最后一条数据的值
                    BigDecimal lastValue = new BigDecimal(historyDTOList.get(historyDTOList.size() - 1).getValue());
                    dto.setLastUpdateValue(lastValue.toString());
                }
                dto.setUnit((String) map.get("units"));
                dto.setTitleName(item.getChName());
                dto.setDataList(historyDTOList);
                dto.setDelay(item.getDelay());
                dto.setLastUpdateTime(SeverityUtils.getDate(new Date()));
                lists.add(dto);
            }
        }
        return lists;
    }

    /**
     * 通过hostId和itemName的模糊查询,获取主机所有监控项
     *
     * @param hostId
     * @return
     */
    public List<ItemApplication> itemsGet(int monitorServerId, String hostId, String itemName) {
        List<ItemApplication> list = new ArrayList<>();
        if (null != hostId && StringUtils.isNotEmpty(hostId)) {
            MWZabbixAPIResult itemsbyHostId = mwtpServerAPI.itemsGet(monitorServerId, hostId, itemName);
            if (itemsbyHostId != null && itemsbyHostId.getData() != null) {
                list = JSONArray.parseArray(String.valueOf(itemsbyHostId.getData()), ItemApplication.class);
                if (CollectionUtils.isNotEmpty(list)) {
                    list.forEach(itemApplication -> {
                        itemApplication.setChName(mwServerManager.getChName(itemApplication.getName()));
                    });
                }
            }
        }
        return list;
    }

    public List<ItemApplication> itemsGetByNames(int monitorServerId, List<String> hostIds, List<String> itemNames) {
        List<ItemApplication> list = new ArrayList<>();
        if (null != hostIds) {
            MWZabbixAPIResult itemsbyHostId = mwtpServerAPI.itemGetbyTypeNames(monitorServerId, itemNames, hostIds);
            if (null != itemsbyHostId) {
                list = JSONArray.parseArray(String.valueOf(itemsbyHostId.getData()), ItemApplication.class);
                list.forEach(itemApplication -> {
                    itemApplication.setChName(mwServerManager.getChName(itemApplication.getName()));
                });
            }
        }
        return list;
    }

    /**
     * 根据时间段类型，获取需要的参数
     *
     * @param dateType 时间段类型 1==>按一个小时  2==>按一天   3==>按一周   4==>按一个月
     * @return
     */
    public DateTypeDTO getDateTypeParams(Integer dateType, String assetsId, Date dateStart, Date dateEnd) {
        DateTypeDTO dateTypeDto = new DateTypeDTO();
        Calendar calendar = Calendar.getInstance();
        dateTypeDto.setFlag(false);
        switch (dateType) {
            case 1:
                //            按一个小时
                dateTypeDto.setEndTime(calendar.getTimeInMillis() / 1000L);
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                dateTypeDto.setStartTime(calendar.getTimeInMillis() / 1000L);
                return dateTypeDto;
            case 2:
                //            按一天
                dateTypeDto.setEndTime(calendar.getTimeInMillis() / 1000L);
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                dateTypeDto.setStartTime(calendar.getTimeInMillis() / 1000L);
                return dateTypeDto;
            case 3:
                //            按一周
                dateTypeDto.setFlag(true);
                dateTypeDto.setKeyPrefix(assetsId + ":15mins:");
                return dateTypeDto;
            case 4:
                //            按一个月
                dateTypeDto.setFlag(true);
                dateTypeDto.setKeyPrefix(assetsId + ":60mins:");
                return dateTypeDto;
            case 5:
                //           自定义
                dateTypeDto.setStartTime(dateStart.getTime() / 1000L);
                dateTypeDto.setEndTime(dateEnd.getTime() / 1000L);
                return dateTypeDto;
            default:
                break;

        }
        return dateTypeDto;
    }

    /**
     * 根据参数获取峰值的单位和值以及历史数据
     *
     * @param dateTypeDto
     * @param itemsList
     * @param valueType
     * @return
     */
    public Map<String, Object> getPeakValue(int monitorServerId, DateTypeDTO dateTypeDto, List<ItemApplication> itemsList, String valueType) {
        Map<String, Object> map = new HashMap<>();
        if (itemsList.size() > 0) {
//        找数据最大值
            Double maxValue = 0.0;
            for (ItemApplication item : itemsList) {
                if (!dateTypeDto.getFlag()) {
//              一个小时和一天的情况下的数据获取
                    List<MWItemHistoryDto> list = zabbixManger.HistoryGetByTimeAndHistory(monitorServerId, item.getItemid(), dateTypeDto.getStartTime(), dateTypeDto.getEndTime(), Integer.valueOf(item.getValue_type()));
                    map.put(item.getItemid(), list);
//                    判断最大值
                    if (maxValue < getMaxValue(list).doubleValue()) {
                        maxValue = getMaxValue(list).doubleValue();
                    }
                } else {
//              一周和一月的情况下的数据获取
                    String key = dateTypeDto.getKeyPrefix() + item.getItemid() + item.getName();
                    List<RedisItemHistoryDto> list = getRedisItemHistory(key);
                    map.put(item.getItemid(), list);
//              以数组最后一个元素作为判定条件
                    if (list.size() > 0) {
//                            通过前端传来的valueType参数，判断获取的是哪一个的最大值
                        if ("AVG".equals(valueType) && list.get(list.size() - 1).getAvgMax() > maxValue) {
                            maxValue = list.get(0).getAvgMax();
                        } else if ("MAX".equals(valueType) && list.get(list.size() - 1).getMaxMax() > maxValue) {
                            maxValue = list.get(0).getMaxMax();
                        } else if ("MIN".equals(valueType) && list.get(list.size() - 1).getMinMax() > maxValue) {
                            maxValue = list.get(0).getMinMax();
                        }
                    }
                }
            }
            Map<String, String> valueAndUnits = UnitsUtil.getValueAndUnits(maxValue.toString(), itemsList.get(0).getUnits());
            map.putAll(valueAndUnits);
        }
        return map;
    }

    /**
     * 根据key值获取redis存储的Zsets数据，并转换成list<RedisItemHistoryDto>
     *
     * @param key
     * @return
     */
    public List<RedisItemHistoryDto> getRedisItemHistory(String key) {
        Set<String> range = redisTemplate.opsForZSet().range(key, 0, -1);
        List<RedisItemHistoryDto> list = new ArrayList<>();
        if (!range.isEmpty()) {
            for (String str : range) {
                if (null != str && StringUtils.isNotEmpty(str) && !"null".equals(str)) {
                    RedisItemHistoryDto redisItemHistoryDto = JSONObject.parseObject(str, RedisItemHistoryDto.class);
                    list.add(redisItemHistoryDto);
                }
            }
        }
        return list;
    }

    //根据lastValue找到list中的最大值
    public Long getMaxValue(List<MWItemHistoryDto> list) {
        Long max = 0L;
        for (int i = 0; i < list.size(); i++) {
            Long lastValue = list.get(i).getLastValue();
            if (max < lastValue) {
                max = lastValue;
            }
        }
        return max;
    }

    /**
     * 根据磁盘名称和磁盘所在的hostId查询有关该磁盘的有关信息
     *
     * @param name
     * @param hostid
     * @return
     */
    public DiskListDto getDiskInfoByDiskName(int monitorServerId, String name, String hostid) {
        DiskListDto diskListDto = new DiskListDto();
        diskListDto.setType(name);
        String newName = "[" + name + "]" + "MW_";
        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(monitorServerId, newName, hostid, false);
        Map<String, String> m = new HashMap();
        if (result1.getCode() == 0) {
            JsonNode node = (JsonNode) result1.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    String dataName = data.get("name").asText();
                    String lastValue = data.get("lastvalue").asText();
                    m.put(dataName, lastValue);
                });
            }

            if (node.size() > 0) {
                node.forEach(data -> {
                    String dataName = data.get("name").asText();
                    String lastValue = data.get("lastvalue").asText();
                    String units = data.get("units").asText();
                    lastValue = UnitsUtil.getValueWithUnits(lastValue, units);
                    if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(0))) {
                        diskListDto.setDiskUserRate(lastValue);
                    } else if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(1))) {
                        diskListDto.setDiskUser(lastValue);
                    } else if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(2))) {
                        diskListDto.setDiskFree(lastValue);
                    } else if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(3))) {
                        diskListDto.setDiskTotal(lastValue);
                    }
                });
            }
        }
        if (diskListDto.getDiskTotal() == null) {
            String diskUser = "";
            String disFree = "";
            String diskTotal = "";
            String lastValue = "";
            //总容量为空时,利用使用量+空余量 = 总容量替换
            if (m.get(newName + ZabbixItemConstant.diskItemName.get(2)) != null && m.get(newName + ZabbixItemConstant.diskItemName.get(1)) != null) {
                diskUser = m.get(newName + ZabbixItemConstant.diskItemName.get(2));
                disFree = m.get(newName + ZabbixItemConstant.diskItemName.get(1));
                diskTotal = (Double.valueOf(diskUser) + Double.valueOf(disFree)) + "";
                lastValue = UnitsUtil.getValueWithUnits(diskTotal, "B");
            }
            diskListDto.setDiskTotal(lastValue);
        }
        return diskListDto;
    }

    public List<NetListDto> sortNetList(List<NetListDto> list, Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            InterfaceInfoEnum value = InterfaceInfoEnum.valueOf(entry.getKey());
            switch (value) {
                case MW_INTERFACE_OUT_TRAFFIC:
                    list.sort((entry.getValue() == 0 ? Comparator.comparing(NetListDto::getSortOutBps) : Comparator.comparing(NetListDto::getSortOutBps).reversed()));
                    break;
                case MW_INTERFACE_IN_TRAFFIC:
                    list.sort((entry.getValue() == 0 ? Comparator.comparing(NetListDto::getSortInBps) : Comparator.comparing(NetListDto::getSortInBps).reversed()));
                    break;
                case MW_INTERFACE_OUT_DROPPED:
                    list.sort((entry.getValue() == 0 ? Comparator.comparing(NetListDto::getSortSendLoss) : Comparator.comparing(NetListDto::getSortSendLoss).reversed()));
                    break;
                case MW_INTERFACE_IN_DROPPED:
                    list.sort((entry.getValue() == 0 ? Comparator.comparing(NetListDto::getSortAcceptLoss) : Comparator.comparing(NetListDto::getSortAcceptLoss).reversed()));
                    break;
                case MW_INTERFACE_SPEED:
                    list.sort((entry.getValue() == 0 ? Comparator.comparing(NetListDto::getSortRate) : Comparator.comparing(NetListDto::getSortRate).reversed()));
                    break;
                case INTERFACE_IN_UTILIZATION:
                    list.sort((entry.getValue() == 0 ? Comparator.comparing(NetListDto::getSortInBpsRatio) : Comparator.comparing(NetListDto::getSortInBpsRatio).reversed()));
                    break;
                case INTERFACE_OUT_UTILIZATION:
                    list.sort((entry.getValue() == 0 ? Comparator.comparing(NetListDto::getSortOutBpsRatio) : Comparator.comparing(NetListDto::getSortOutBpsRatio).reversed()));
                    break;
                default:
                    break;
            }
        }
        return list;
    }

    public NetListDto getNetListDto(AssetsIdsPageInfoParam param, String name) {
        NetListDto netListDto = new NetListDto();
        netListDto.setInterfaceName(name);
        String newName = "[" + name + "]" + "MW_";
        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(param.getMonitorServerId(), newName, param.getAssetsId(), false);
        if (result1.getCode() == 0) {
            JsonNode node = (JsonNode) result1.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    String dataName = data.get("name").asText();
                    String lastValue = data.get("lastvalue").asText();
                    Double sortLastValue = data.get("lastvalue").asDouble();
                    String units = data.get("units").asText();
                    lastValue = UnitsUtil.getValueWithUnits(lastValue, units);
                    InterfaceInfoEnum value = InterfaceInfoEnum.valueOf(dataName.substring(dataName.indexOf("]") + 1));
                    switch (value) {
                        case MW_INTERFACE_OUT_TRAFFIC:
                            netListDto.setOutBps(lastValue);
                            netListDto.setSortOutBps(sortLastValue);
                            break;
                        case MW_INTERFACE_IN_TRAFFIC:
                            netListDto.setInBps(lastValue);
                            netListDto.setSortInBps(sortLastValue);
                            break;
                        case MW_INTERFACE_OUT_DROPPED:
                            netListDto.setSendLoss(lastValue);
                            netListDto.setSortSendLoss(sortLastValue);
                            break;
                        case MW_INTERFACE_IN_DROPPED:
                            netListDto.setAcceptLoss(lastValue);
                            netListDto.setSortAcceptLoss(sortLastValue);
                            break;
                        case MW_INTERFACE_SPEED:
                            netListDto.setRate(lastValue);
                            netListDto.setSortRate(sortLastValue);
                            break;
                        case MW_INTERFACE_STATUS:
                            if (!"0".equals(data.get("valuemapid").asText())) {
                                String newValue = mwServerManager.getValueMapById(param.getMonitorServerId(), data.get("valuemapid").asText(), data.get("lastvalue").asText());
                                if (null != newValue && StringUtils.isNotEmpty(newValue)) {
                                    netListDto.setState(newValue);
                                }
                            } else {
                                netListDto.setState(lastValue);
                            }
                            break;
                        default:
                            break;
                    }
                });
            }
        }
        return netListDto;
    }


    /**
     * 查询资产状态
     *
     * @param param
     * @return
     */
    @Override
    public Reply getAssetsStatusInfo(QueryAssetsStatusParam param) {
        try {
            if (param.getServerId() == null || StringUtils.isBlank(param.getAssetsId())) {
                return Reply.ok(null);
            }
            String status = "";
            MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(param.getServerId(), ZabbixItemConstant.NEW_ASSETS_STATUS, param.getAssetsId());
            Set<String> hostSets = new HashSet<>();
            if (statusData != null && !statusData.isFail()) {
                JsonNode jsonNode = (JsonNode) statusData.getData();
                if (jsonNode.size() > 0) {
                    for (JsonNode node : jsonNode) {
                        Integer lastvalue = node.get("lastvalue").asInt();
                        String hostId = node.get("hostid").asText();
                        String name = node.get("name").asText();
                        if ((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)) {
                            status = (lastvalue == 0) ? ABNORMAL : NORMAL;
                            hostSets.add(hostId);
                        }
                        if (hostSets.contains(hostId)) {
                            continue;
                        }
                        status = (lastvalue == 0) ? ABNORMAL : NORMAL;
                    }
                }
            }
            if (null == param.getMonitorFlag()) {
                status = UNKNOWN;
            } else {
                if (param.getMonitorFlag()) {
                    if (StringUtils.isEmpty(status)) {
                        status = UNKNOWN;
                    }
                } else {
                    status = SHUTDOWN;
                }
            }
            return Reply.ok(status);
        } catch (Throwable e) {
            log.error("MwServerServiceImpl :: getAssetsStatusInfo{}", e);
            return Reply.fail("getAssetsStatusInfo{}", e);
        }
    }


    class AlphanumericComparator implements Comparator<NetListDto> {
        public int compare(NetListDto s1, NetListDto s2) {
            String[] parts1 = s1.getInterfaceName().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            String[] parts2 = s2.getInterfaceName().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            int length = Math.min(parts1.length, parts2.length);
            for (int i = 0; i < length; i++) {
                int cmp = compareParts(parts1[i], parts2[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.compare(parts1.length, parts2.length);
        }

        private int compareParts(String part1, String part2) {
            if (isNumber(part1) && isNumber(part2)) {
                return Long.compare(Long.parseLong(part1), Long.parseLong(part2));
            } else {
                return part1.compareTo(part2);
            }
        }

        private boolean isNumber(String str) {
            return str.matches("\\d+");
        }
    }

    @Autowired
    private MWAlertService alertService;

    /**
     * 查询进程信息
     *
     * @param monitorServerId 服务器ID
     * @param hostId          主机ID
     * @param itemName        监控项名称
     * @return
     */
    @Override
    public Reply getAssetsDetailsProcess(int monitorServerId, String hostId, String itemName) {
        try {
            log.info("MwServerServiceImpl{} getAssetsDetailsProcess() monitorServerId:"+monitorServerId+">>hostId:"+hostId+">>itemName:"+itemName);
            AssetsProcessTopDto assetsProcessTopDto = new AssetsProcessTopDto();
            //查询告警信息
            List<ZbxAlertDto> allAlerts = getAssetsAlertInfo(monitorServerId, hostId, false);
            log.info("MwServerServiceImpl{} getAssetsDetailsProcess() allAlerts"+allAlerts);
            if (CollectionUtils.isEmpty(allAlerts)) {
                return Reply.ok(assetsProcessTopDto);
            }
            //取最近一次时间的告警
            allAlerts.forEach(item -> {
                item.setAlertDate(DateUtils.parse(item.getClock(), DateConstant.NORM_DATETIME));
            });
            ZbxAlertDto zbxAlertDto = allAlerts.stream().max(Comparator.comparing(ZbxAlertDto::getAlertDate)).get();
            log.info("MwServerServiceImpl{} getAssetsDetailsProcess() zbxAlertDto"+zbxAlertDto);
            //查询最近快照信息
            MWZabbixAPIResult result = mwtpServerAPI.itemsGet(monitorServerId, hostId, itemName);
            log.info("MwServerServiceImpl{} getAssetsDetailsProcess() zbxAlertDto"+result);
            if (result == null || result.isFail()) {
                return Reply.ok(assetsProcessTopDto);
            }
            ItemApplication itemApplication = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class).get(0);
            log.info("MwServerServiceImpl{} getAssetsDetailsProcess() itemApplication"+itemApplication);
            if(StringUtils.isNotBlank(itemApplication.getLastvalue())){
                assetsProcessTopDto.extractFrom(zbxAlertDto, itemApplication);
                return Reply.ok(assetsProcessTopDto);
            }
            //如果没有最新数据，需要查询最近的一条历史数据
            MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetByItemid(monitorServerId, itemApplication.getItemid(), Integer.parseInt(itemApplication.getValue_type()), 1);
            log.info("MwServerServiceImpl{} getAssetsDetailsProcess() historyResult"+historyResult);
            if (historyResult == null || historyResult.isFail()) {
                return Reply.ok(assetsProcessTopDto);
            }
            List<MwProcessHistoryDto> processHistoryDtos = JSONArray.parseArray(String.valueOf(historyResult.getData()), MwProcessHistoryDto.class);
            log.info("MwServerServiceImpl{} getAssetsDetailsProcess() processHistoryDtos"+processHistoryDtos);
            if(CollectionUtils.isEmpty(processHistoryDtos)){
                return Reply.ok(assetsProcessTopDto);
            }
            itemApplication.setLastvalue(processHistoryDtos.get(0).getValue());
            assetsProcessTopDto.extractFrom(zbxAlertDto, itemApplication);
            return Reply.ok(assetsProcessTopDto);
        } catch (Throwable e) {
            log.error("查询资产详情进程信息失败", e);
            return Reply.fail("getAssetsDetailsProcess{}", e);
        }
    }


    /**
     * 资产详情页签进程top10下载txt
     *
     * @param assetsBaseDTO 查询参数
     * @param response
     */
    @Override
    public void downloadAssetsDetailsProcess(AssetsBaseDTO assetsBaseDTO, HttpServletResponse response) {
        try {
            //根据参数获取最近7天的进程快照数据
            if (StringUtils.isBlank(assetsBaseDTO.getAssetsId()) || StringUtils.isBlank(assetsBaseDTO.getItemName()) || assetsBaseDTO.getMonitorServerId() == 0) {
                log.info("下载资产详情进程失败参数为空" + assetsBaseDTO);
                return;
            }
            //查询数据的itemID
            MWZabbixAPIResult result = mwtpServerAPI.itemsGet(assetsBaseDTO.getMonitorServerId(), assetsBaseDTO.getAssetsId(), assetsBaseDTO.getItemName());
            if (result == null || result.isFail()) {
                return;
            }
            ItemApplication itemApplication = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class).get(0);
            //获取最近7天的时间
            Date currDate = new Date();
            Date startDate = DateUtils.addDays(currDate, -7);
            MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetByTimeAndType(assetsBaseDTO.getMonitorServerId(), itemApplication.getItemid(), startDate.getTime() / 1000, currDate.getTime() / 1000, Integer.parseInt(itemApplication.getValue_type()));
            if (historyResult == null || historyResult.isFail()) {
                return;
            }
            List<MwProcessHistoryDto> processHistoryDtos = JSONArray.parseArray(String.valueOf(historyResult.getData()), MwProcessHistoryDto.class);
            //按照时间排序
            valueSort(processHistoryDtos);
            //获取告警
            List<ZbxAlertDto> allAlerts = getAssetsAlertInfo(assetsBaseDTO.getMonitorServerId(), assetsBaseDTO.getAssetsId(), true);
            StringBuffer buffer = new StringBuffer();
            //时间转换
            for (int i = 0; i < allAlerts.size(); i++) {
                ZbxAlertDto allAlert = allAlerts.get(i);
                long time = DateUtils.parse(allAlert.getClock(), DateConstant.NORM_DATETIME).getTime();
                if (i != 0) {
                    buffer.append("\r\n\r\n");
                }
                buffer.append("告警时间:").append(allAlert.getClock()).append("       ").append("告警标题:").append(allAlert.getName()).append("\r\n");
                for (MwProcessHistoryDto processHistoryDto : processHistoryDtos) {
                    long processTime = Long.parseLong(processHistoryDto.getClock());
                    if ((processTime - (time / 1000)) <= 120 && (processTime - (time / 1000)) >= 0) {
                        String dateStr = DateUtils.formatDateTime(new Date(Long.parseLong(processHistoryDto.getClock()) * 1000));
                        buffer.append("快照产生时间: ").append(dateStr).append("\r\n").append(processHistoryDto.getValue()).append("\r\n");
                    }
                }
            }
            //导出TXT
            ExportTxtUtil.exportTxt(response, buffer.toString(), assetsBaseDTO.getItemName());
        } catch (Throwable e) {
            log.error("下载资产详情进程失败", e);
        }
    }



    /**
     * 获取资产告警信息
     */
    private List<ZbxAlertDto> getAssetsAlertInfo(int monitorServerId, String hostId, Boolean isHistoryAlert) {
        //查询告警信息
        AlertParam mwAlertDto = new AlertParam();
        mwAlertDto.setPageNumber(1);
        mwAlertDto.setPageSize(Integer.MAX_VALUE);
        mwAlertDto.setQueryMonitorServerId(monitorServerId);
        mwAlertDto.setQueryHostIds(Arrays.asList(hostId));
        if (isHistoryAlert) {
            //设置查询最近七天数据
            Date currDate = new Date();
            Date startDate = DateUtils.addDays(currDate, -7);
            mwAlertDto.setStartTime(DateUtils.format(startDate, DateConstant.NORM_DATETIME));
            mwAlertDto.setEndTime(DateUtils.format(currDate, DateConstant.NORM_DATETIME));
        }
        Reply histAlertPage = alertService.getHistAlertPage(mwAlertDto);
        List<ZbxAlertDto> allAlerts = new ArrayList<>();
        if (histAlertPage != null && histAlertPage.getRes() == PaasConstant.RES_SUCCESS) {
            PageInfo pageInfo = (PageInfo) histAlertPage.getData();
            allAlerts.addAll(pageInfo.getList());
        }
        Reply currAlertPage = alertService.getCurrAlertPage(mwAlertDto);
        if (currAlertPage != null && currAlertPage.getRes() == PaasConstant.RES_SUCCESS) {
            PageInfo pageInfo = (PageInfo) currAlertPage.getData();
            allAlerts.addAll(pageInfo.getList());
        }
        return allAlerts;
    }


    private void valueSort(List<MwProcessHistoryDto> processHistoryDtos) {
        Collections.sort(processHistoryDtos, new Comparator<MwProcessHistoryDto>() {
            @Override
            public int compare(MwProcessHistoryDto o1, MwProcessHistoryDto o2) {
                if (Long.parseLong(o1.getClock()) < Long.parseLong(o2.getClock())) {
                    return -1;
                }
                if (Long.parseLong(o1.getClock()) > Long.parseLong(o2.getClock())) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void dateSort(List<ZbxAlertDto> allAlerts) {
        Collections.sort(allAlerts, new Comparator<ZbxAlertDto>() {
            @Override
            public int compare(ZbxAlertDto o1, ZbxAlertDto o2) {
                if (o1.getAlertDate().compareTo(o2.getAlertDate()) > 0) {
                    return 1;
                }
                if (o1.getAlertDate().compareTo(o2.getAlertDate()) < 0) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 获取数据趋势数据
     * @param param
     * @return
     */
    @Override
    public Reply getHistoryTrend(QueryItemTrendParam param) {
        try {
            Map<Integer, List<String>> hostMap = dataTypeHandler(param);
            //获取redis数据
            String dataStr = getRedisData(param,true);
            if(StringUtils.isNotBlank(dataStr)){
                List<ItemTrendGroupDto> groupDtos = JSONArray.parseArray(dataStr, ItemTrendGroupDto.class);
                return Reply.ok(groupDtos);
            }
            List<ItemTrendDto> trendDtos = new ArrayList<>();
            for (Map.Entry<Integer, List<String>> entry : hostMap.entrySet()) {
                Integer serverId = entry.getKey();
                List<String> hostIds = entry.getValue();
                List<ItemApplication> itemApplications = new ArrayList<>();
                //分组查询
                List<List<String>> partition = Lists.partition(hostIds, groupCount);
                for (List<String> hosts : partition) {
                    //查询数据的itemid
                    MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, param.getItemNames(), hosts);
                    //数据转换
                    if(result == null || result.isFail()){continue;}
                    itemApplications.addAll(JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class));
                }
                if(CollectionUtils.isEmpty(itemApplications)){continue;}
                Map<String, ItemApplication> applicationMap = itemApplications.stream().collect(Collectors.toMap(ItemApplication::getItemid, obj -> obj));
                List<String> itemIds = itemApplications.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
                List<Date> times = new ArrayList<>();
                List<List<String>> items = Lists.partition(itemIds, groupCount);
                List<ItemTrendApplication> trendApplications = new ArrayList<>();
                for (List<String> item : items) {
                    //查询历史趋势
                    MWZabbixAPIResult trendResult = mwtpServerAPI.trendBatchGet(serverId, item, DateUtils.parse(param.getStartTime()).getTime() / 1000, DateUtils.parse(param.getEndTime()).getTime() / 1000);
                    if(trendResult == null || trendResult.isFail()){continue;}
                    trendApplications.addAll(JSONArray.parseArray(String.valueOf(trendResult.getData()), ItemTrendApplication.class));
                }
                for (ItemTrendApplication trendApplication : trendApplications) {
                    ItemApplication itemApplication = applicationMap.get(trendApplication.getItemid());
                    if(StringUtils.isEmpty(itemApplication.getName()) || itemApplication.getName().contains(ServerConstant.VMEMORY_UTILIZATION)){
                        continue;
                    }
                    ItemTrendDto trendDto = new ItemTrendDto();
                    trendDto.extractFrom(itemApplication,param,trendApplication,serverId);
                    trendDtos.add(trendDto);
                }
            }
            //数据分组
            List<ItemTrendGroupDto> groupDtos = trendDataGroupHandler(trendDtos,param.getDataType());
            //数据存储到redis
            String data = JSONObject.toJSONString(groupDtos);
            setRedisData(param,true,data);
            return Reply.ok(groupDtos);
        }catch (Throwable e){
            log.error("MwServerServiceImpl{} getHistoryTrend() error",e);
            return Reply.fail("MwServerServiceImpl{} getHistoryTrend() error",param);
        }
    }

    private List<ItemTrendGroupDto> trendDataGroupHandler(List<ItemTrendDto> trendDtos,Integer dataType){
        if(dataType != 1){
            for (ItemTrendDto trendDto : trendDtos) {
                String name = trendDto.getName();
                if(StringUtils.isBlank(name) || !name.contains("]")){continue;}
                trendDto.setName(name.split("]")[1]);
            }
        }
        List<ItemTrendGroupDto> groupDtos = new ArrayList<>();
        Map<String, List<ItemTrendDto>> idMap = trendDtos.stream().filter(item->item.getId() != null).collect(Collectors.groupingBy(item -> item.getId()));
        for (Map.Entry<String, List<ItemTrendDto>> entry : idMap.entrySet()) {
            String id = entry.getKey();
            List<ItemTrendDto> value = entry.getValue();
            Map<String, List<ItemTrendDto>> listMap = value.stream().collect(Collectors.groupingBy(item -> item.getName()));
            ItemTrendGroupDto groupDto = new ItemTrendGroupDto();
            groupDto.setId(id);
            groupDto.setTrendMap(listMap);
            groupDtos.add(groupDto);
        }
        return groupDtos;
    }

    @Override
    public Reply getAssetsTempAndHumidity(QueryItemTrendParam param) {
        try {
            if(StringUtils.isBlank(param.getAssetsTypeName()) || CollectionUtils.isEmpty(param.getItemNames())){return Reply.fail("param is null",param);}
            //获取redis数据
            String dataStr = getRedisData(param,false);
            if(StringUtils.isNotBlank(dataStr)){
                List<ItemTempAndHumidityDto> tempAndHumidityDtos = JSONArray.parseArray(dataStr, ItemTempAndHumidityDto.class);
                return Reply.ok(tempAndHumidityDtos);
            }
            QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
            assetsParam.setPageSize(Integer.MAX_VALUE);
            assetsParam.setAssetsTypeName(param.getAssetsTypeName());
            assetsParam.setUserId(userService.getAdmin());
            List<MwTangibleassetsTable> assetsTable = mwAssetsManager.getAssetsTable(assetsParam);
            if(CollectionUtils.isEmpty(assetsTable)){return Reply.fail("not assets",param);}
            log.info("MwServerServiceImpl{} getAssetsTempAndHumidity() assetsTable:"+assetsTable);
            Map<Integer, List<String>> groupMap = assetsTable.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            List<ItemApplication> itemApplications = new ArrayList<>();
            for (Integer serverId : groupMap.keySet()) {
                List<String> hostIds = groupMap.get(serverId);
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, param.getItemNames(), hostIds);
                //数据转换
                if(result == null || result.isFail()){continue;}
                itemApplications.addAll(JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class));
            }
            log.info("MwServerServiceImpl{} getAssetsTempAndHumidity() itemApplications:"+itemApplications);
            if(CollectionUtils.isEmpty(itemApplications)){return Reply.ok();}
            List<ItemTempAndHumidityDto> tempAndHumidityDtos = assetsTypeQueryHandler(itemApplications,param.getRoomNames());
            //数据存储到redis
            String data = JSONObject.toJSONString(tempAndHumidityDtos);
            setRedisData(param,false,data);
            return Reply.ok(tempAndHumidityDtos);
        }catch (Throwable e){
            log.error("MwServerServiceImpl{} getAssetsTypeData() error",e);
            return Reply.fail("MwServerServiceImpl{} getAssetsTypeData() error",param);
        }
    }

    @Override
    public Reply getAssetsDetatils(QueryItemTrendParam param) {
        List<String> ids = param.getIds();
        if(CollectionUtils.isEmpty(ids)){
            return Reply.fail("id is not empty",param);
        }
        //查询资产数据
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setAssetsTypeName(param.getAssetsTypeName());
        assetsParam.setUserId(userService.getAdmin());
        assetsParam.setAssetsIds(ids);
        List<MwTangibleassetsTable> assetsTable = new ArrayList<>();
        Reply reply = tangibleAssetsService.selectList(assetsParam);
        if(reply != null){
            PageInfo pageInfo = (PageInfo) reply.getData();
            assetsTable = pageInfo.getList();
        }
        if(CollectionUtils.isEmpty(assetsTable)){
            return Reply.fail("not query assets",param);
        }
        Map<Integer, List<String>> groupMap = assetsTable.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        Map<String,ItemApplication> valueMap = new HashMap<>();
        for (Integer serverId : groupMap.keySet()) {
            List<String> hostIds = groupMap.get(serverId);
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, ServerConstant.SYSTEM_UPTIME, hostIds);
            //数据转换
            if(result == null || result.isFail()){continue;}
            List<ItemApplication> applications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
            if(CollectionUtils.isEmpty(applications)){continue;}
            for (ItemApplication application : applications) {
                valueMap.put(serverId+application.getHostid(),application);
            }
        }
        List<AssetsDetailDto> assetsDetailDtos = new ArrayList<>();
        for (MwTangibleassetsTable mwTangibleassetsTable : assetsTable) {
            AssetsDetailDto detailDto = new AssetsDetailDto();
            detailDto.extractFrom(mwTangibleassetsTable,valueMap.get(mwTangibleassetsTable.getMonitorServerId()+mwTangibleassetsTable.getAssetsId()));
            if(detailDto.getAssetsStatus() != null && detailDto.getAssetsStatus().equals(NORMAL)){
                detailDto.setAssetsUsability("100%");
            }else{
                detailDto.setAssetsUsability("0%");
            }
            assetsDetailDtos.add(detailDto);
        }
        return Reply.ok(assetsDetailDtos);
    }

    private List<ItemTempAndHumidityDto> assetsTypeQueryHandler(List<ItemApplication> itemApplications,List<String> roomNames){
        List<ItemTempAndHumidityDto> tempAndHumidityDtos = new ArrayList<>();
        for (ItemApplication itemApplication : itemApplications) {
            String itemName = itemApplication.getName();
            if(StringUtils.isBlank(itemName) || !itemName.contains("]")){continue;}
            itemApplication.setChName(itemName.substring(itemName.indexOf("[")+1,itemName.indexOf("]")));
            itemApplication.setName(itemName.split("]")[1]);
        }
        //按照机房名称分组
        Map<String, List<ItemApplication>> nameMap = itemApplications.stream().filter(item->StringUtils.isNotBlank(item.getChName())).collect(Collectors.groupingBy(item -> item.getChName()));
        if(nameMap.isEmpty()){return tempAndHumidityDtos;}
        for (Map.Entry<String, List<ItemApplication>> entry : nameMap.entrySet()) {
            List<ItemApplication> applications = entry.getValue();
            ItemTempAndHumidityDto tempAndHumidityDto = new ItemTempAndHumidityDto();
            String roomName = entry.getKey();
            if(CollectionUtils.isNotEmpty(roomNames) && !roomNames.contains(roomName)){continue;}
            tempAndHumidityDto.setName(roomName);
            for (ItemApplication application : applications) {
                if(application.getName().equals(ServerConstant.Temperature)){
                    tempAndHumidityDto.setTemperature(application.getLastvalue()+application.getUnits());
                } if(application.getName().equals(ServerConstant.Humidity)){
                    tempAndHumidityDto.setHumidity(application.getLastvalue()+application.getUnits());
                }
            }
            if(StringUtils.isEmpty(tempAndHumidityDto.getTemperature()) && StringUtils.isEmpty(tempAndHumidityDto.getHumidity())){
                continue;
            }
            tempAndHumidityDtos.add(tempAndHumidityDto);
        }
        return tempAndHumidityDtos;
    }

    private Map<Integer,List<String>> dataTypeHandler(QueryItemTrendParam param){
        Map<Integer,List<String>> assetsHostMap = new HashMap<>();
        if(param.getDataType() == 1){
            //根据线路ID查询线路
            List<MwLinkInterfaceDto> linkInterfaceInfo = linkCommonService.getLinkInterfaceInfo(param.getIds());
            if(CollectionUtils.isEmpty(linkInterfaceInfo)){return assetsHostMap;}
            List<String> itemNames = new ArrayList<>();
            for (MwLinkInterfaceDto interfaceDto : linkInterfaceInfo) {
                itemNames.add("["+interfaceDto.getInterfaceName()+"]"+ ServerConstant.MW_INTERFACE_IN_TRAFFIC);
                itemNames.add("["+interfaceDto.getInterfaceName()+"]"+ ServerConstant.MW_INTERFACE_OUT_TRAFFIC);
            }
            param.setItemNames(itemNames);
            assetsHostMap = linkInterfaceInfo.stream().filter(item->item.getServerId() != null &&  item.getServerId() != 0)
                    .collect(Collectors.groupingBy(MwLinkInterfaceDto::getServerId, Collectors.mapping(MwLinkInterfaceDto::getHostId, Collectors.toList())));
            param.setIdMap(linkInterfaceInfo.stream().collect(Collectors.toMap( item -> item.getServerId()+item.getHostId()+item.getInterfaceName(), MwLinkInterfaceDto::getLinkId)));
        }else{
            //查询资产
            QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
            assetsParam.setPageSize(Integer.MAX_VALUE);
            assetsParam.setAssetsTypeName(param.getAssetsTypeName());
            assetsParam.setAssetsIds(param.getIds());
            assetsParam.setUserId(userService.getAdmin());
            List<MwTangibleassetsTable> assetsTable = mwAssetsManager.getAssetsTable(assetsParam);
            if(CollectionUtils.isEmpty(assetsTable)){return assetsHostMap;}
            assetsHostMap = assetsTable.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            param.setIdMap(assetsTable.stream().collect(Collectors.toMap( item -> item.getMonitorServerId()+item.getAssetsId(), MwTangibleassetsTable::getId)));
        }
        return assetsHostMap;
    }

    private String getRedisData(QueryItemTrendParam param,boolean flag){
        String redisKey;
        if(param.getItemNames() == null){
            param.setItemNames(new ArrayList<>());
        }
        if(flag){
            redisKey = String.join("", param.getIds())+param.getDataType()+param.getStartTime()+param.getEndTime()+String.join("", param.getItemNames());
        }else{
            redisKey = param.getAssetsTypeName()+String.join("", param.getItemNames());
        }
        String dataStr = redisTemplate.opsForValue().get(redisKey);
        return dataStr;
    }

    private void setRedisData(QueryItemTrendParam param,boolean flag,String data){
        String redisKey;
        if(flag){
            redisKey = String.join("", param.getIds())+param.getDataType()+param.getStartTime()+param.getEndTime()+String.join("", param.getItemNames());
        }else{
            redisKey = param.getAssetsTypeName()+String.join("", param.getItemNames());
        }
        redisTemplate.opsForValue().set(redisKey, data, 20, TimeUnit.MINUTES);
    }
}
