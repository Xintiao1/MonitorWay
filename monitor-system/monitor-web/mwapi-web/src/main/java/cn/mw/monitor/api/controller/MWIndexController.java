package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.dto.*;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.model.IndexBulk;
import cn.mw.monitor.screen.model.IndexModelBase;
import cn.mw.monitor.screen.param.EditorIndexParam;
import cn.mw.monitor.screen.service.MWLagerScreenService;
import cn.mw.monitor.screen.service.MWModelManage;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author xhy
 * @date 2020/4/16 16:17
 */
@RequestMapping("/mwapi")
@Controller
@Api(value = "主页", tags = "主页")
public class MWIndexController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("MWIndexController");
    private static final Logger dbLogger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MWLagerScreenService mwLagerScreenService;

    @Autowired
    private MWModelManage mwModelManage;
    @Autowired
    private MwAssetsManager mwAssetsManager;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    //查询分组主机设备的数据
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/groupnameList/browse")
    @ResponseBody
    @ApiOperation(value = "查询分组主机设备的数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name="userId",value = "用户id",paramType = "query")
    })
    public ResponseBase getHostUserRatioData(@PathParam("userId") Integer userId) {
        try {
            String key = genRedisKey("saveAlertCount", "host_count", userId);
            String redislist = redisTemplate.opsForValue().get(key);
            List<AlertPriorityType> list = new ArrayList<>();
            if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                list = JSONArray.parseArray(redislist, AlertPriorityType.class);
            } else {
                list = mwModelManage.hostgroupListGetByName(userId);
            }
            return setResultSuccess(list);
        } catch (Exception e) {
            logger.error("getHostUserRatioData{}", e);
            return setResultFail("MWIndexController{} getHostUserRatioData() error", "");
        }
    }

    @MwPermit(moduleName = "home_manage")
    @GetMapping("/indexmessage/browse")
    @ResponseBody
    @ApiOperation(value = "消息统计")
    public ResponseBase messageBrowse(@PathParam("userId") Integer userId, @PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            String key = genRedisKey("messageStatistics", "message", userId);
            String redisValue = redisTemplate.opsForValue().get(key);
            MessageDto messageDto = new MessageDto();
            if (null != redisValue && StringUtils.isNotEmpty(redisValue)) {
                messageDto = JSONObject.parseObject(redisValue, MessageDto.class);
            } else {
//                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(66).userId(userId).type(DataType.INDEX.getName()).build();
                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
                messageDto = mwModelManage.messageStatistics(filterAssetsParam);
            }
            return setResultSuccess(messageDto);
        } catch (Exception e) {
            logger.error("messageBrowse{}", e);
            return setResultFail("MWIndexController{} messageBrowse() error", "");
        }
    }

    @MwPermit(moduleName = "home_manage")
    @GetMapping("/hostUserRatioList/browse")
    @ResponseBody
    @ApiOperation(value = "查询利用率")
    public ResponseBase getGroupListData(@PathParam("userId") Integer userId, @PathParam("name") String
            name, @PathParam("mwRankCount") Integer mwRankCount,@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            String key = genRedisKey("getAlertEvent_getHostRank", name, userId);
            String redislist = redisTemplate.opsForValue().get(key);
            ItemRank itemRank = new ItemRank();
            if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                itemRank = JSONObject.parseObject(redislist, ItemRank.class);
            } else {
//                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(MWUtils.RANK.get(name)).userId(userId).type(DataType.INDEX.getName()).build();
                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
                itemRank = mwModelManage.getHostRank(name, filterAssetsParam);
            }
//            //CPU多核取平均值
//            Map<String,List<ItemNameRank>> itemMap = new HashMap<>();
//            if("CPU_UTILIZATION".equals(name)){
//                List<ItemNameRank> itemNameRankList = itemRank.getItemNameRankList();
//                if(!CollectionUtils.isEmpty(itemNameRankList)){
//                    for (ItemNameRank itemNameRank : itemNameRankList) {
//                        String assetsId = itemNameRank.getAssetsId();
//                        String id = itemNameRank.getId();
//                        if(itemMap.containsKey(assetsId+id)){
//                            List<ItemNameRank> itemNameRanks = itemMap.get(assetsId + id);
//                            itemNameRanks.add(itemNameRank);
//                            itemMap.put(assetsId+id,itemNameRanks);
//                        }else{
//                            List<ItemNameRank> itemNameRanks = new ArrayList<>();
//                            itemNameRanks.add(itemNameRank);
//                            itemMap.put(assetsId+id,itemNameRanks);
//                        }
//                    }
//                }
//            }
//            List<ItemNameRank> cpuRealDatas = new ArrayList<>();
//            if(!itemMap.isEmpty()){
//                for (String itemKey : itemMap.keySet()) {
//                    List<ItemNameRank> itemNameRanks = itemMap.get(itemKey);
//                    double value = 0;
//                    for (ItemNameRank itemNameRank : itemNameRanks) {
//                        value += itemNameRank.getLastValue();
//                    }
//                    ItemNameRank rank = new ItemNameRank();
//                    rank = itemNameRanks.get(0);
//                    rank.setLastValue(new BigDecimal(value/itemNameRanks.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue());
//                    cpuRealDatas.add(rank);
//                }
//            }
//            itemRank.setItemNameRankList(cpuRealDatas);
            Set<String> assetsIdAndIps = new HashSet<>();
            if (null != itemRank.getItemNameRankList() && itemRank.getItemNameRankList().size() > 0) {
                //进行数据去重
                List<ItemNameRank> itemNameRankList = itemRank.getItemNameRankList();
                Iterator<ItemNameRank> iterator = itemNameRankList.iterator();
                while(iterator.hasNext()){
                    ItemNameRank next = iterator.next();
                    if(assetsIdAndIps.contains(next.getAssetsId()+next.getIp()+next.getId())){
                        iterator.remove();
                    }else{
                        assetsIdAndIps.add(next.getAssetsId()+next.getIp()+next.getId());
                    }
                }
                int rankcount = (itemRank.getItemNameRankList().size() > mwRankCount ? mwRankCount : itemRank.getItemNameRankList().size());
                itemRank.setItemNameRankList(itemRank.getItemNameRankList().subList(0, rankcount));

            }
//            //数据排序
//            if(itemRank != null){
//                List<ItemNameRank> itemNameRankList = itemRank.getItemNameRankList();
//                if(CollectionUtils.isNotEmpty(itemNameRankList)){
//                    Collections.sort(itemNameRankList, new Comparator<ItemNameRank>() {
//                        @Override
//                        public int compare(ItemNameRank o1, ItemNameRank o2) {
//                            return o2.getLastValue().compareTo(o1.getLastValue());
//                        }
//                    });
//                }
//            }
            return setResultSuccess(itemRank);
        } catch (Exception e) {
            logger.error("getGroupListData{}", e.getMessage());
            return setResultFail("MWIndexController{} getGroupListData() error", "");
        }
    }

    //查询流量排行
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/hostUserTrafficList/browse")
    @ResponseBody
    @ApiOperation(value = "查询流量排行")
    public ResponseBase getTrafficListData(@PathParam("userId") Integer userId, @PathParam("name") String
            name, @PathParam("mwRankCount") Integer mwRankCount,@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
//        ,@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId
        try {
            String key = genRedisKey("getAlertEvent_getHostRank", name, userId);
            String redislist = redisTemplate.opsForValue().get(key);
            ItemRank itemRank = new ItemRank();
            if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                itemRank = JSONObject.parseObject(redislist, ItemRank.class);
            } else {
//                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(MWUtils.RANK.get(name)).userId(userId).type(DataType.INDEX.getName()).build();
                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
                itemRank = mwModelManage.getHostRank(name, filterAssetsParam);
            }
            //换算单位进行排序
            List<ItemNameRank> itemNameRankList = itemRank.getItemNameRankList();
            logger.info("首页流量接收长度为："+itemNameRankList.size());
            if(CollectionUtils.isEmpty(itemNameRankList))return setResultSuccess(itemRank);
            for (ItemNameRank rank : itemNameRankList) {
                Double lastValue = rank.getLastValue();
                String units = rank.getUnits();
                Map<String, String> valueMap = UnitsUtil.getValueMap(lastValue + "", "Kbps", units);
                String value = valueMap.get("value");
                rank.setLinkSortValue(Double.parseDouble(value));
            }
            //数据排序
            if(CollectionUtils.isNotEmpty(itemNameRankList)){
                Collections.sort(itemNameRankList, new Comparator<ItemNameRank>() {
                    @Override
                    public int compare(ItemNameRank o1, ItemNameRank o2) {
                        return o2.getLinkSortValue().compareTo(o1.getLinkSortValue());
                    }
                });
            }
            if (null != itemRank.getItemNameRankList() && itemRank.getItemNameRankList().size() > 0) {
                int rankcount = (itemRank.getItemNameRankList().size() > mwRankCount ? mwRankCount : itemRank.getItemNameRankList().size());
                List<ItemNameRank> itemNameRanks = itemRank.getItemNameRankList().subList(0, rankcount);
                for (ItemNameRank itemNameRank : itemNameRanks) {
                    String dataUnits = UnitsUtil.getValueWithUnits(itemNameRank.getLastValue().toString(), itemNameRank.getUnits());
                    itemNameRank.setValue(dataUnits);
                }
                itemRank.setItemNameRankList(itemRank.getItemNameRankList().subList(0, rankcount));
            }
            return setResultSuccess(itemRank);
        } catch (Exception e) {
            logger.error("getTrafficListData{}", e);
            return setResultFail("MWIndexController{} getTrafficListData() error", "");
        }
    }

    //获得确认和未确认的告警数量
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getAlertCountAcknowledged/browse")
    @ResponseBody
    @ApiOperation(value = "获得确认和未确认的告警数量")
    public ResponseBase getAlertCountAcknowledged(@PathParam("userId") Integer userId,
                                                  @PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            String key = genRedisKey("getAlertCount", "host_acknowledged", userId);
            String count = redisTemplate.opsForValue().get(key);
            JSONObject object = new JSONObject();
            if (null != count && StringUtils.isNotEmpty(count)) {
                object = JSONObject.parseObject(count);
            } else {
//                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(6).userId(userId).type(DataType.INDEX.getName()).build();
                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
                object = mwModelManage.getAlertCount(filterAssetsParam);
            }
            return setResultSuccess(object);
        } catch (Exception e) {
            logger.error("getAlertCountAcknowledged{}", e);
            return setResultFail("MWIndexController{} getAlertCountAcknowledged() error", "");
        }
    }

    //获得当前告警
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getAlertEvent/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前告警")
    public ResponseBase getAlertEvent(@PathParam("userId") Integer userId,
                                      @PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            String key = genRedisKey("getAlertEvent", "alertEvent", userId);
            String redisList = redisTemplate.opsForValue().get(key);
            List<HistEventDto> nowEventDtos = new ArrayList<>();
            if (null != redisList && StringUtils.isNotEmpty(redisList)) {
                nowEventDtos = JSONArray.parseArray(redisList, HistEventDto.class);
            } else {
//                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(11).userId(userId).type(DataType.INDEX.getName()).build();
                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
                nowEventDtos = mwModelManage.getAlertEvent(filterAssetsParam);
            }
            return setResultSuccess(nowEventDtos);
        } catch (Exception e) {
            logger.error("getAlertEvent{}", e);
            return setResultFail("MWIndexController{} getAlertEvent() error", "");
        }
    }

    //获得历史事件
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getHistEvent/browse")
    @ResponseBody
    @ApiOperation(value = "获得历史事件")
    public ResponseBase getHistEvent(@PathParam("userId") Integer userId, @PathParam("count") Integer
            count,@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            String key = genRedisKey("getAlertEvent", "histEvent", userId);
            String redisList = redisTemplate.opsForValue().get(key);
            List<HistEventDto> histEventDtos = new ArrayList<>();
            if (null != redisList && StringUtils.isNotEmpty(redisList)) {
                histEventDtos = JSONArray.parseArray(redisList, HistEventDto.class);
            } else {
//                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(12).userId(userId).type(DataType.INDEX.getName()).build();
                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
                histEventDtos = mwModelManage.getHistEvent(100, filterAssetsParam);
            }
            if (histEventDtos.size() > count) {
                histEventDtos = histEventDtos.subList(0, count);
            }
            return setResultSuccess(histEventDtos);
        } catch (Exception e) {
            logger.error("getHistEvent{}", e);
            return setResultFail("MWIndexController{} getHistEvent() error", "");
        }
    }

    /**
     * 获得当日时间数据分布
     */
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getTodayDataList/browse")
    @ResponseBody
    @ApiOperation(value = "获得当日时间数据分布")
    public ResponseBase getTodayDataList(@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
//            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(27).userId(userId).type(DataType.INDEX.getName()).build();
            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
            TodayDataListDto dto = mwModelManage.getTodayDataList(filterAssetsParam);
            return setResultSuccess(dto);
        } catch (Exception e) {
            logger.error("getTodayDataList{}", e);
            return setResultFail("MWIndexController{} getTodayDataList() error", "");
        }
    }

    /**
     * 获得当日数据和历史数据值
     */
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getLogCount/browse")
    @ResponseBody
    @ApiOperation(value = "获得当日数据和历史数据值")
    public ResponseBase getLogCount(@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
//            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(28).userId(userId).type(DataType.INDEX.getName()).build();
            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
            Map map = (Map) mwModelManage.getLogCount(filterAssetsParam);
            return setResultSuccess(map);
        } catch (Exception e) {
            logger.error("getLogCount{}", e);
            return setResultFail("MWIndexController{} getLogCount() error", "");
        }
    }

    /**
     * 获得zabbix监控项的值
     */
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getItemValue/browse")
    @ResponseBody
    @ApiOperation(value = "获得zabbix监控项的值")
    public ResponseBase getItemValue(@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
//            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(6).userId(userId).type(DataType.INDEX.getName()).build();
            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
            List<Map<String, Object>> itemValue = mwModelManage.getItemValue(filterAssetsParam);
            return setResultSuccess(itemValue);
        } catch (Exception e) {
            logger.error("getItemValue{}", e);
            return setResultFail("MWIndexController{} getItemValue() error", "");
        }
    }

    /**
     * 获得资产数量统计
     */
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getHostCountByLog/browse")
    @ResponseBody
    @ApiOperation(value = "获得资产数量统计")
    public ResponseBase getHostCountByLog(@PathParam("modelId") Integer modelId,@PathParam("modelDataId") String modelDataId) {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
//            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(26).userId(userId).type(DataType.INDEX.getName()).build();
            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
            ItemRank itemRank = mwModelManage.getHostCountByLog(filterAssetsParam);
            return setResultSuccess(itemRank);
        } catch (Exception e) {
            logger.error("getHostCountByLog{}", e);
            return setResultFail("MWIndexController{} getHostCountByLog() error", "");
        }
    }

    /**
     * 查询日志源
     */
    @MwPermit(moduleName = "home_manage")
    @GetMapping("/getLogHostList/browse")
    @ResponseBody
    @ApiOperation(value = "查询日志源")
    public ResponseBase getLogHostList() {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            MwCommonAssetsDto mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).monitorMode(11).build();
            List<String> list = mwAssetsManager.getLogHostList(mwCommonAssetsDto);
            return setResultSuccess(list);
        } catch (Exception e) {
            logger.error("getLogHostList{}", e);
            return setResultFail("MWIndexController{} getLogHostList() error", "");
        }

    }

    @PostMapping("/getIndexModelBase/browse")
    @ResponseBody
    @ApiOperation(value = "查询首页模块")
    public ResponseBase getIndexBase() {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            List<IndexBulk> list = mwModelManage.getIndexModelBase(userId);
            return setResultSuccess(list);
        } catch (Exception e) {
            logger.error("getIndexBase{}", e);
            return setResultFail("MWIndexController{} getIndexBase() error", "");
        }
    }

    @PostMapping("/getPageSelectBase/browse")
    @ResponseBody
    @ApiOperation(value = "查询首页模块下拉")
    public ResponseBase getPageSelectBase() {
        List<IndexModelBase> list = null;
        try {
            list = mwModelManage.getPageSelectBase();
        } catch (Exception e) {
            return setResultFail("MWIndexController{} getPageSelectBase() error", "");
        }
        return setResultSuccess(list);
    }

    @PostMapping("/indexLayout/editor")
    @ResponseBody
    @ApiOperation(value = "首页模块编辑")
    public ResponseBase updateIndexLayout(@RequestBody EditorIndexParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        try {
            Reply reply = null;
            reply = mwLagerScreenService.editIndexLayout(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("updateIndexLayout{}", e);
            return setResultFail("MWIndexController{} updateIndexLayout() error", "");
        }
    }

    private String genRedisKey(String methodName, String objectName, Integer uid) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append(":").append(objectName)
                .append("_").append(uid);
        return sb.toString();
    }


}
