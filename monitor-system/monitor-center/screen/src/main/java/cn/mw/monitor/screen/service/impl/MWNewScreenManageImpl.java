package cn.mw.monitor.screen.service.impl;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.assets.model.MwOutbandAssetsTable;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.labelManage.dao.MwLabelManageTableDao;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.constant.ScreenConstant;
import cn.mw.monitor.screen.dao.MWNewScreenManageDao;
import cn.mw.monitor.screen.dto.*;
import cn.mw.monitor.screen.manage.AlertHandler;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.param.MWAlertCountParam;
import cn.mw.monitor.screen.param.MWNewScreenAssetsCensusParam;
import cn.mw.monitor.screen.service.GetDataByCallable;
import cn.mw.monitor.screen.service.MWModelManage;
import cn.mw.monitor.screen.service.MWNewScreenManage;
import cn.mw.monitor.screen.util.MWNewScreenDateUtil;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.AssetsDto;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.api.MwInspectModeService;
import cn.mw.monitor.service.assets.api.MwOutbandAssetsService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.enums.AssetsStatusEnum;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.QueryOutbandAssetsParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.assets.service.MwAssetsMainTainService;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.label.model.QueryLabelParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.timetask.api.MwTimeTaskCommonsService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @ClassName MWNewScreenManageImpl
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/29 10:33
 * @Version 1.0
 **/
@Service
@Slf4j
public class MWNewScreenManageImpl implements MWNewScreenManage {

    @Autowired
    private MWNewScreenManageDao newScreenManageDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MWModelManage mwModelManage;

    @Autowired
    private MWAlertService mwalertService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWUserService userService;


    public static final List<String> diskNames = Arrays.asList("MW_DISK_USED", "MW_DISK_TOTAL", "MW_DISK_FREE");

    @Autowired
    private MwOutbandAssetsService assetsService;

    @Autowired
    private MwTangibleAssetsService tangibleAssetsService;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MWCommonService commonService;

    @Value("${scheduling.enabled}")
    private boolean schedulingEnabled;

    @Autowired
    private MwTimeTaskCommonsService timeTaskCommonsService;

    @Autowired
    private MWOrgService orgService;

    @Autowired
    private AlertHandler alertHandler;

    @Autowired
    private MwInspectModeService mwInspectModeService;

    @Autowired
    private MwAssetsMainTainService assetsMainTainService;

    /**
     * 资产信息数据查询
     * @return
     */
    @Override
    public Reply getNewScreenAssets() {
        try {
            List<MWMainTainHostView> underMaintenanceHost = assetsMainTainService.getUnderMaintenanceHost();
            MWNewScreenAssetsDto dto = new MWNewScreenAssetsDto();
            List<MwScreenAssetsDto> screenAssetsDtos = new ArrayList<>();
            String key = genRedisKey("homePage", ScreenConstant.ASSETS_GROUP);
            String redislist = redisTemplate.opsForValue().get(key);
            if(StringUtils.isNotBlank(redislist)){
                screenAssetsDtos = JSON.parseArray(redislist, MwScreenAssetsDto.class);
            }else{
                QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
                assetsParam.setPageNumber(1);
                assetsParam.setPageSize(Integer.MAX_VALUE);
                assetsParam.setIsQueryAssetsState(true);
                //根据类型查询资产数据
                Reply reply = tangibleAssetsService.selectList(assetsParam);
                List<MwTangibleassetsTable> mwTangibleassetsTables = new ArrayList<>();
                Object data = reply.getData();
                if (null != data) {
                    PageInfo pageInfo = (PageInfo) data;
                    mwTangibleassetsTables = pageInfo.getList();
                }
                log.info("首页查询资产信息"+mwTangibleassetsTables);
                Map<Integer ,AssetTypeIconDTO> typeIconDTOMap = mwModelViewCommonService.selectAllAssetsTypeIcon();
                if(CollectionUtils.isNotEmpty(mwTangibleassetsTables)){
                    for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables){
                        MwScreenAssetsDto screenAssetsDto = new MwScreenAssetsDto();
                        screenAssetsDto.extractFrom(mwTangibleassetsTable,typeIconDTOMap,commonService.getSystemAssetsType());
                        screenAssetsDtos.add(screenAssetsDto);
                    }
                }
            }
            log.info("MWNewScreenManageImpl{} getNewScreenAssets() screenAssetsDtos::"+screenAssetsDtos);
            dto.setIsModel(commonService.getSystemAssetsType());
            if(CollectionUtils.isNotEmpty(screenAssetsDtos)){
                //根据当前登录用户权限过滤资产
                List<String> currLoginUserAssets = getCurrLoginUserAssets();//当前用户可以查看的资产
                //删除用户没有权限的资产数据
                Iterator<MwScreenAssetsDto> iterator = screenAssetsDtos.iterator();
                while(iterator.hasNext()){
                    MwScreenAssetsDto next = iterator.next();
                    String id = next.getId();
                    if(CollectionUtils.isNotEmpty(currLoginUserAssets) && id != null && !currLoginUserAssets.contains(id)){
                        iterator.remove();
                    }
                }
            }
            if(CollectionUtils.isEmpty(screenAssetsDtos)){
               return Reply.ok(dto);
            }
            //进行数据分组处理，获取资产信息数据
            getAseetsStatus(screenAssetsDtos,dto);
            boolean systemAssetsType = commonService.getSystemAssetsType();//获取是否模型资产
            log.info("MWNewScreenManageImpl{} getNewScreenAssets() systemAssetsType::"+systemAssetsType);
            if(systemAssetsType){ return  Reply.ok(dto);}
            handleOutBandAssets(dto);
            return  Reply.ok(dto);
        }catch (Exception e){
            log.error("查询新大屏资产数据失败",e);
            return Reply.fail("查询新大屏资产数据失败");
        }
    }



    /**
     * 首页资产处理带外资产数据
     * @param dto
     */
    private void handleOutBandAssets(MWNewScreenAssetsDto dto){
        //合并带外资产信息
        QueryOutbandAssetsParam queryOutbandAssetsParam = QueryOutbandAssetsParam.builder().isHomePageType(1).build();
        Reply reply = assetsService.selectList(queryOutbandAssetsParam);
        if(reply == null || reply.getRes() != PaasConstant.RES_SUCCESS || reply.getData() == null)return;
        dto.setTangibleAssetsAmount(dto.getTotalAseetsAmount());
        PageInfo pageInfo = (PageInfo) reply.getData();
        List<MwOutbandAssetsTable> mwOutbandAssetses = pageInfo.getList();
        if(CollectionUtils.isEmpty(mwOutbandAssetses))return;
        dto.setTotalAseetsAmount(dto.getTangibleAssetsAmount()+mwOutbandAssetses.size());
        dto.setOutAeestsAmount(mwOutbandAssetses.size());
        Map<String,Integer> map = new HashMap<>();
        List<MWNewScreenAssetsClassifyDto> normalAssetsMap = dto.getNormalAssetsMap();
        List<MWNewScreenAssetsClassifyDto> unusualAssetsMap = dto.getUnusualAssetsMap();
        List<MWNewScreenAssetsClassifyDto> downTimeAssetsMap = dto.getDownTimeAssetsMap();
        for (MwOutbandAssetsTable mwOutbandAssets : mwOutbandAssetses) {
            String itemAssetsStatus = mwOutbandAssets.getItemAssetsStatus();
            if(map != null && map.containsKey(itemAssetsStatus)){
                Integer count = map.get(itemAssetsStatus);
                map.put(itemAssetsStatus,count+1);
            }else{
                map.put(itemAssetsStatus,1);
            }
            String assetsTypeName = mwOutbandAssets.getAssetsTypeName();
            if("NORMAL".equals(itemAssetsStatus)){
                boolean flag = true;
                MWNewScreenAssetsClassifyDto classifyDto = new MWNewScreenAssetsClassifyDto();
                if(CollectionUtils.isNotEmpty(normalAssetsMap)){
                    for (MWNewScreenAssetsClassifyDto mwNewScreenAssetsClassifyDto : normalAssetsMap) {
                        if(assetsTypeName.equals(mwNewScreenAssetsClassifyDto.getTypeName())){
                            mwNewScreenAssetsClassifyDto.setCount(mwNewScreenAssetsClassifyDto.getCount()+1);
                            flag = false;
                        }
                    }
                }
                if(flag){
                    if(normalAssetsMap == null){
                        normalAssetsMap = new ArrayList<>();
                    }
                    classifyDto.setTypeName(assetsTypeName);
                    classifyDto.setCount(1);
                    classifyDto.setUrl("a5a6dec443b0447b9a16d7ccb1a8278b.gif");
                    normalAssetsMap.add(classifyDto);
                }
            }
            if("ABNORMAL".equals(itemAssetsStatus)){
                boolean flag = true;
                MWNewScreenAssetsClassifyDto classifyDto = new MWNewScreenAssetsClassifyDto();
                if(CollectionUtils.isNotEmpty(unusualAssetsMap)){
                    for (MWNewScreenAssetsClassifyDto mwNewScreenAssetsClassifyDto : unusualAssetsMap) {
                        if(assetsTypeName.equals(mwNewScreenAssetsClassifyDto.getTypeName())){
                            mwNewScreenAssetsClassifyDto.setCount(mwNewScreenAssetsClassifyDto.getCount()+1);
                            flag = false;
                        }
                    }
                }
                if(flag){
                    if(unusualAssetsMap == null){
                        unusualAssetsMap = new ArrayList<>();
                    }
                    classifyDto.setTypeName(assetsTypeName);
                    classifyDto.setCount(1);
                    unusualAssetsMap.add(classifyDto);
                }
            }
            if(!"ABNORMAL".equals(itemAssetsStatus) && !"NORMAL".equals(itemAssetsStatus)){
                boolean flag = true;
                MWNewScreenAssetsClassifyDto classifyDto = new MWNewScreenAssetsClassifyDto();
                if(CollectionUtils.isNotEmpty(downTimeAssetsMap)){
                    for (MWNewScreenAssetsClassifyDto mwNewScreenAssetsClassifyDto : downTimeAssetsMap) {
                        if(assetsTypeName.equals(mwNewScreenAssetsClassifyDto.getTypeName())){
                            mwNewScreenAssetsClassifyDto.setCount(mwNewScreenAssetsClassifyDto.getCount()+1);
                            flag = false;
                        }
                    }
                }
                if(flag){
                    if(downTimeAssetsMap == null){
                        downTimeAssetsMap = new ArrayList<>();
                    }
                    classifyDto.setTypeName(assetsTypeName);
                    classifyDto.setCount(1);
                    downTimeAssetsMap.add(classifyDto);
                }
            }
        }
        dto.setNormalAssetsMap(normalAssetsMap);
        dto.setUnusualAssetsMap(unusualAssetsMap);
        dto.setDownTimeAssetsMap(downTimeAssetsMap);
        if(map != null){
            for (String key : map.keySet()) {
                Integer count = map.get(key);
                if("NORMAL".equals(key) && count != null){
                    dto.setNormalOutBandAseetsAmount(count);
                    dto.setNormalAseetsAmount(dto.getNormalAseetsAmount()+count);
                }
                if("ABNORMAL".equals(key) && count != null){
                    dto.setUnusualOutBandAseetsAmount(count);
                    dto.setUnusualAseetsAmount(dto.getUnusualAseetsAmount()+count);
                }
                if(!"ABNORMAL".equals(key) && !"NORMAL".equals(key) && count != null){
                    dto.setDownTimeOutBandAseetsAmount(count);
                    dto.setDownTimeAseetsAmount(dto.getDownTimeAseetsAmount()+count);
                }
            }
        }
    }

    /**
     * 资产统计数据查询
     * @param param
     * @return
     */
    @Override
    public Reply getNewScreenAssetsCensusData(MWNewScreenAssetsCensusParam param) {
        try {
            //获取日期
            int dateType = param.getDateType();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String startTime = null;
            String endTime = null;
            switch (dateType){
                case 1://本周
                    List<Date> week = MWNewScreenDateUtil.getWeek();
                    startTime = format.format(week.get(0));
                    endTime = format.format(week.get(1));
                    break;
                case 2://本月
                    List<Date> month = MWNewScreenDateUtil.getMonth();
                    startTime = format.format(month.get(0));
                    endTime = format.format(month.get(1));
                    break;
                case 3://上月
                    List<Date> lastMonth = MWNewScreenDateUtil.getLastMonth();
                    startTime = format.format(lastMonth.get(0));
                    endTime = format.format(lastMonth.get(1));
                    break;
                case 0:
                    startTime = param.getStartTime().substring(0,10);
                    endTime = param.getEndTime().substring(0,10);
                    break;
                default:
                    break;
            }
            MWNewScreenAssetsCensusDto screenAssetsCensusDto = new MWNewScreenAssetsCensusDto();
            List<Integer> count = new ArrayList<>();
            List<String> date = new ArrayList<>();
            //根据日期区间查询资产
            List<Map<String, Object>> assetsDateRegionDatas = newScreenManageDao.selectAssetsDateRegionData(startTime, endTime);
            if(CollectionUtils.isNotEmpty(assetsDateRegionDatas)){
                for (Map<String, Object> assetsDateRegionData : assetsDateRegionDatas) {
                    String censusDate = (String) assetsDateRegionData.get("censusDate");
                    int assetsAmount = Integer.parseInt(assetsDateRegionData.get("assetsAmount").toString()) ;
                    count.add(assetsAmount);
                    date.add(censusDate);
                }
            }
            QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
            assetsParam.setPageNumber(1);
            assetsParam.setPageSize(Integer.MAX_VALUE);
            assetsParam.setIsQueryAssetsState(false);
            //根据类型查询资产数据
            Reply reply = tangibleAssetsService.selectList(assetsParam);
            List<MwTangibleassetsTable> mwTangibleassetsTables = new ArrayList<>();
            Object data = reply.getData();
            PageInfo newPageInfo = new PageInfo<>();
            if (null != data) {
                PageInfo pageInfo = (PageInfo) data;
                mwTangibleassetsTables = pageInfo.getList();
            }
            int currCount = mwTangibleassetsTables.size();
            //获取昨天的资产数量
            List<Date> yesterday = MWNewScreenDateUtil.getYesterday();
            Integer yesterdayCount = newScreenManageDao.selectOneDayAssetsCount(format.format(yesterday.get(0)));
            //查询今日数据
            if(dateType == 1 || dateType == 2 || format.parse(endTime).compareTo(new Date()) == 0 || format.parse(endTime).compareTo(new Date()) == 1){
                //查询昨日资产总数
                count.add(currCount);
                date.add(format.format(new Date()));
            }
            if(yesterdayCount != null){
                screenAssetsCensusDto.setCompareYesterday(currCount-yesterdayCount);
            }
            screenAssetsCensusDto.setTotal(currCount);
            screenAssetsCensusDto.setCount(count);
            screenAssetsCensusDto.setDate(date);
            return Reply.ok(screenAssetsCensusDto);
        }catch (Exception e){
            log.error("查询新大屏资产统计数据失败",e);
            return Reply.fail("查询新大屏资产统计数据失败");
        }
    }

    /**
     * 查询告警运维事件
     * @return
     */
    @Override
    public Reply getNewScreenAlertDevOpsEvent(List<MWNewScreenAlertDevOpsEventDto> param) {
        try {
            MWNewScreenAlertDevOpsEventDto dto = new MWNewScreenAlertDevOpsEventDto();
            MWNewScreenAssetsCensusParam censusParam = new MWNewScreenAssetsCensusParam();
            if(!CollectionUtils.isEmpty(param)){
                for (MWNewScreenAlertDevOpsEventDto mwNewScreenAlertDevOpsEventDto : param) {
                    Integer userId = mwNewScreenAlertDevOpsEventDto.getUserId();
                    Integer modelId = mwNewScreenAlertDevOpsEventDto.getModelId();
                    String modelDataId = mwNewScreenAlertDevOpsEventDto.getModelDataId();
                    if(modelId != null && modelId == 7){//查询消息信息
                        getAlertMessage(userId,modelId,modelDataId,dto);
                    }
                    if(modelId != null && modelId == 1){//查询告警消息
                        getCurrAlert(mwNewScreenAlertDevOpsEventDto,dto);
                        censusParam.setDateType(mwNewScreenAlertDevOpsEventDto.getDateType() == null?0:mwNewScreenAlertDevOpsEventDto.getDateType());
                        censusParam.setStartTime(mwNewScreenAlertDevOpsEventDto.getStartTime());
                        censusParam.setEndTime(mwNewScreenAlertDevOpsEventDto.getEndTime());
                    }
                }
            }

            Reply newScreenActivityAlertCount = getNewScreenActivityAlertCount(censusParam);
            if(newScreenActivityAlertCount != null && newScreenActivityAlertCount.getRes() == PaasConstant.RES_SUCCESS){
                Map<String,List<ActivityAlertClassifyDto>> map = (Map<String, List<ActivityAlertClassifyDto>>) newScreenActivityAlertCount.getData();
                dto.setAlertClassiftMap(map);
            }
            return Reply.ok(dto);
        }catch (Exception e){
            log.error("查询新大屏告警运维事件数据失败",e);
            return Reply.fail("查询新大屏告警运维事件数据失败");
        }
    }



    /**
     * 获取活动告警信息
     * @param param
     * @return
     */
    @Override
    public Reply getActivityAlertData(MWNewScreenAssetsCensusParam param) {
        try {
            AlertParam alertParam = new AlertParam();
            alertParam.setPageSize(param.getPageSize());
            alertParam.setPageNumber(param.getPageNumber());
            alertParam.setFuzzyQuery(param.getAlertName());
            alertParam.setSeverity(param.getAlertLevel());
            List<String> hostIds = alertHandler.handler();
            if(CollectionUtils.isNotEmpty(hostIds)){
                alertParam.setQueryHostIds(hostIds);
            }
            Integer dateType = param.getDateType();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(dateType != null && dateType == 1){//今天时间
                List<Date> today = MWNewScreenDateUtil.getToday();
                alertParam.setStartTime(format.format(today.get(0)));
                alertParam.setEndTime(format.format(today.get(1)));
            }
            if(dateType != null && dateType == 0){//自定义时间
                alertParam.setStartTime(param.getStartTime());
                alertParam.setEndTime(param.getEndTime());
            }
            Reply reply = mwalertService.getCurrAlertPage(alertParam);
            return reply;
        }catch (Exception e){
            log.error("查询新大屏活动告警数据失败",e);
            return Reply.fail("查询新大屏活动告警数据失败");
        }
    }

    /**
     * 查询CPU利用率
     * @param param
     * @return
     */
    @Override
    public Reply getNewScreenAssetsTopN(MWNewScreenAlertDevOpsEventDto param) {
        try {
            long startTime = System.currentTimeMillis();
            MWNewScreenTopNDto dto = new MWNewScreenTopNDto();
            String name = param.getName();
            Integer userId = param.getUserId();
            Integer modelId = param.getModelId();
            String modelDataId = param.getModelDataId();
            Integer mwRankCount = param.getMwRankCount();
            String key = genRedisKey("homePage", name);
            String redislist = redisTemplate.opsForValue().get(key);
            ItemRank itemRank = new ItemRank();
            if(StringUtils.isNotBlank(redislist) && (param.getIsCache() == null || !param.getIsCache())){
                MWNewScreenTopNDto screenTopNDto = JSONObject.parseObject(redislist, MWNewScreenTopNDto.class);
                if(screenTopNDto != null){
                    flowErrorDataFilter(screenTopNDto.getItemNameRankList(),MWNewScreenAssetsFilterDto.builder().modelId(modelId == null?0:modelId).modelDataId(modelDataId).userId(userId).build());
                    if (null != screenTopNDto.getItemNameRankList() && screenTopNDto.getItemNameRankList().size() > 0) {
                        //是否检查模式筛选数据
                        List<ItemNameRank> itemNameRankList = screenTopNDto.getItemNameRankList();
                        itemRankFilter(itemNameRankList);
                        int rankcount = (screenTopNDto.getItemNameRankList().size() > mwRankCount ? mwRankCount : screenTopNDto.getItemNameRankList().size());
                        screenTopNDto.setItemNameRankList(screenTopNDto.getItemNameRankList().subList(0, rankcount));
                    }
                    return Reply.ok(screenTopNDto);
                }
            }
            log.info("MWNewScreenManageImpl{} getNewScreenAssetsTopN() schedulingEnabled:"+schedulingEnabled);
            if(!schedulingEnabled){//直接返回，并手动执行任务
                log.info("MWNewScreenManageImpl{} getNewScreenAssetsTopN()"+ScreenConstant.TOPN_ACTION);
                timeTaskCommonsService.executeScreenTimeTask(ScreenConstant.TOPN_ACTION);
                return Reply.ok(dto);
            }
            MWNewScreenAssetsFilterDto filterAssetsParam = MWNewScreenAssetsFilterDto.builder().modelId(modelId == null?0:modelId).modelDataId(modelDataId).userId(userId).build();
            itemRank = getHostRank(name, filterAssetsParam);
            log.info("MWNewScreenManageImpl{} getNewScreenAssetsTopN():22"+name);
            if (null != itemRank.getItemNameRankList() && itemRank.getItemNameRankList().size() > 0 && mwRankCount != null) {
                int rankcount = (itemRank.getItemNameRankList().size() > mwRankCount ? mwRankCount : itemRank.getItemNameRankList().size());
                itemRank.setItemNameRankList(itemRank.getItemNameRankList().subList(0, rankcount));
            }
            log.info("MWNewScreenManageImpl{} getNewScreenAssetsTopN():33"+name);
            if(itemRank != null){
                Map<String,Object> map = new HashMap<>();
                dto.setItemNameRankList(itemRank.getItemNameRankList());
                dto.setTitleNode(itemRank.getTitleNode());
//                dto.setTitleRanks(itemRank.getTitleRanks());
            }
            //如果是丢包率，需要查询出ping值
            if("ICMP_LOSS".equals(name)){
                getLossPingValue(dto);
            }
            dataChange(dto,name);
            //查询资产状态
            getAssetsStatus(dto.getItemNameRankList());
            if("DISK_UTILIZATION".equals(name)){
               getDiskNews(dto.getItemNameRankList());
            }
            log.info("MWNewScreenManageImpl{} getNewScreenAssetsTopN() realData::"+name+"::"+dto);
            return Reply.ok(dto);
        }catch (Exception e){
            log.error("查询新大屏TopNCPU数据失败",e);
            return Reply.fail("查询新大屏TopNCPU数据失败");
        }
    }

    /**
     * 查询磁盘信息
     * @param itemNameRankList
     */
    private List<MWNewScreenDiskDto> getDiskNews(List<ItemNameRank> itemNameRankList){
        Map<Integer,List<String>> hostMaps = new HashMap<>();
        List<MWNewScreenDiskDto> realData = new ArrayList<>();
        if(CollectionUtils.isEmpty(itemNameRankList)){return realData;}
        itemNameRankList.forEach(itemNameRank -> {
            String assetsId = itemNameRank.getAssetsId();
            Integer monitorServerId = itemNameRank.getMonitorServerId();
            if(hostMaps.containsKey(monitorServerId)){
                List<String> strings = hostMaps.get(monitorServerId);
                strings.add(assetsId);
                hostMaps.put(monitorServerId,strings);
            }else{
                List<String> hostIds = new ArrayList<>();
                hostIds.add(assetsId);
                hostMaps.put(monitorServerId,hostIds);
            }
        });
        if(hostMaps.isEmpty()){return realData;}
        for (Integer key : hostMaps.keySet()) {
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbySearch(key, diskNames, hostMaps.get(key));
            if (!mwZabbixAPIResult.isFail()){
                JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
                if (jsonNode.size() > 0){
                    for (JsonNode node : jsonNode){
                        String hostId = node.get("hostid").asText();
                        String name = node.get("name").asText();
                        String units = node.get("units").asText();
                        String value = node.get("lastvalue").asText();
                        for (ItemNameRank realDatum : itemNameRankList) {
                            String assetsId = realDatum.getAssetsId();
                            String type = realDatum.getType();
                            //数据转换
                            Map<String, String> mbps = UnitsUtil.getValueMap(value, "GB", units);
                            if(hostId.equals(assetsId) && ("["+type+"]MW_DISK_USED").equals(name)){//已使用容量
                                realDatum.setDiskUsed(new BigDecimal(mbps.get("value")).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"GB");
                            }
                            if(hostId.equals(assetsId) && ("["+type+"]MW_DISK_TOTAL").equals(name)){//总容量
                                realDatum.setDiskTotal(new BigDecimal(mbps.get("value")).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"GB");
                            }
                            if(hostId.equals(assetsId) && ("["+type+"]MW_DISK_FREE").equals(name)){//剩余容量
                                realDatum.setDiskNotUsed(new BigDecimal(mbps.get("value")).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"GB");
                            }
                        }
                    }
                }
            }
        }
        //磁盘名称处理
        if(CollectionUtils.isNotEmpty(itemNameRankList)){
            for (ItemNameRank realDatum : itemNameRankList) {
                String name = realDatum.getName();
                if(name.contains("[")){
                    int i = name.indexOf("[");
                    realDatum.setName(name.substring(0,i));
                }
                String diskTotal = realDatum.getDiskTotal();
                if(StringUtils.isBlank(diskTotal) && StringUtils.isNotBlank(realDatum.getDiskUsed()) && StringUtils.isNotBlank(realDatum.getDiskNotUsed())){//总容量没有需要计算
                    BigDecimal total = new BigDecimal(realDatum.getDiskUsed().replace("GB", "")).add(new BigDecimal(realDatum.getDiskNotUsed().replace("GB", "")));
                    realDatum.setDiskTotal(total.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()+"GB");
                }
            }
        }
        return realData;
    }

    /**
     * 获取ping值
     * @param dto
     */
    private void getLossPingValue(MWNewScreenTopNDto dto){
        List<ItemNameRank> itemNameRankList = dto.getItemNameRankList();
        if(CollectionUtils.isEmpty(itemNameRankList)){
            return;
        }
        Map<String,ItemNameRank> rankMap = new HashMap<>();
        //进行数据分组
        Map<Integer,List<String>> serverIdMap = new HashMap<>();
        for (ItemNameRank itemNameRank : itemNameRankList) {
            Integer monitorServerId = itemNameRank.getMonitorServerId();
            String assetsId = itemNameRank.getAssetsId();
            if(monitorServerId != null && StringUtils.isNotBlank(assetsId) && CollectionUtils.isEmpty(serverIdMap.get(monitorServerId))){
                rankMap.put(assetsId,itemNameRank);
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId);
                serverIdMap.put(monitorServerId,assetsIds);
                continue;
            }
            if(monitorServerId != null && StringUtils.isNotBlank(assetsId) && !CollectionUtils.isEmpty(serverIdMap.get(monitorServerId))){
                rankMap.put(assetsId,itemNameRank);
                List<String> assetsIds = serverIdMap.get(monitorServerId);
                assetsIds.add(assetsId);
                serverIdMap.put(monitorServerId,assetsIds);
            }
        }
        if(serverIdMap.isEmpty()){
            return;
        }
        for (Integer serverId : serverIdMap.keySet()) {
            List<String> assetsIds = serverIdMap.get(serverId);
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(serverId, "ICMP_PING", assetsIds);
            if (result.code == 0) {
                JsonNode itemData = (JsonNode) result.getData();
                if (itemData.size() > 0){
                    for (JsonNode item : itemData){
                        String hostid = item.get("hostid").asText();
                        String lastvalue = item.get("lastvalue").asText();
                        String units = item.get("units").asText();
                        if(StringUtils.isBlank(hostid)){
                            continue;
                        }
                        ItemNameRank itemNameRank = rankMap.get(hostid);
                        itemNameRank.setLossPingUnit("ms");
                        if(StringUtils.isNotBlank(lastvalue)){
                            itemNameRank.setLossPingValue(Integer.parseInt(lastvalue));
                        }else{
                            itemNameRank.setLossPingValue(null);
                        }
                    }
                }
            }
        }
    }


    /**
     * 流量占比信息
     * @param param
     * @return
     */
    @Override
    public Reply getNewScreenLinkTopN(MWNewScreenAlertDevOpsEventDto param) {
        try {
            long startTime = System.currentTimeMillis();
            List<String> names = new ArrayList<>();
            names.add("INTERFACE_IN_UTILIZATION");
            names.add("INTERFACE_OUT_UTILIZATION");
            Integer userId = param.getUserId();
            Integer modelId = param.getModelId();
            String modelDataId = param.getModelDataId();
            String key = genRedisKey("homePage", ScreenConstant.INTERFACE_FLOW_KEY);
            String redislist = redisTemplate.opsForValue().get(key);
            MWNewScreenAssetsFilterDto filterAssetsParam = MWNewScreenAssetsFilterDto.builder().modelId(modelId == null?0:modelId).modelDataId(modelDataId).userId(userId).build();
            MWNewScreenTopNDto dto = new MWNewScreenTopNDto();
            log.info("首页topN数据调用"+names+":::"+(System.currentTimeMillis() - startTime));
            if (StringUtils.isNotBlank(redislist)  && (param.getIsCache() == null || !param.getIsCache())) {
                dto = JSONObject.parseObject(redislist, MWNewScreenTopNDto.class);
                if(dto != null){
                    List<ItemNameRank> itemNameRankList = dto.getItemNameRankList();
                    log.info("首页topN数据调用2"+names+":::"+(System.currentTimeMillis() - startTime));
                    flowErrorDataFilter(itemNameRankList,filterAssetsParam);
                    log.info("首页topN数据调用3"+names+":::"+(System.currentTimeMillis() - startTime));
                    if(CollectionUtils.isNotEmpty(itemNameRankList) && itemNameRankList.size() > param.getMwRankCount()){
                        dto.setItemNameRankList(itemNameRankList.subList(0,param.getMwRankCount()));
                    }
                    return Reply.ok(dto);
                }
            }
            if(!schedulingEnabled){//直接返回，并手动执行任务
                log.info("MWNewScreenManageImpl{} getNewScreenLinkTopN()"+ScreenConstant.TOPNLINK_ACTION);
                timeTaskCommonsService.executeScreenTimeTask(ScreenConstant.TOPNLINK_ACTION);
                return Reply.ok(dto);
            }
            Map<Integer, List<String>> map = getAssetsFilterData(filterAssetsParam);
            ItemRank itemRank = mwModelManage.getScreenFlowNews(names, null, map,param.getMwRankCount());
            dto.setItemNameRankList(itemRank.getItemNameRankList());
            dto.setTitleNode(itemRank.getTitleNode());
            dto.setTitleRanks(itemRank.getTitleRanks());
            dataChange(dto,"INTERFACE_OUT_UTILIZATION");
            log.info("首页topN数据调用4"+names+":::"+(System.currentTimeMillis() - startTime));
            return Reply.ok(dto);
        }catch (Exception e){
            log.error("查询新大屏TopN线路数据失败",e);
            return Reply.fail("查询新大屏TopN线路数据失败");
        }
    }


    /**
     * 获取首页流量错误包数据排行
     * @param param
     * @return
     */
    @Override
    public Reply getHomePageFlowErrorCountTopN(MWNewScreenAlertDevOpsEventDto param) {
        try {
            long startTime = System.currentTimeMillis();
            List<String> itemNames = new ArrayList<>();
            itemNames.add("INTERFACE_OUT_ERRORS");
            itemNames.add("INTERFACE_IN_ERRORS");
            MWNewScreenTopNDto newScreenTopNDto = new MWNewScreenTopNDto();
            Integer userId = param.getUserId();
            Integer modelId = param.getModelId();
            String modelDataId = param.getModelDataId();
            Integer mwRankCount = param.getMwRankCount();
            Map<String,MWNewScreenLinkTopNDto> map = new HashMap<>();
            MWNewScreenAssetsFilterDto filterAssetsParam = MWNewScreenAssetsFilterDto.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).build();
            log.info("首页topN数据调用2"+itemNames+":::"+(System.currentTimeMillis() - startTime));
            if(StringUtils.isBlank(redisTemplate.opsForValue().get("homePage"+itemNames.get(0))) && !schedulingEnabled){
                log.info("MWNewScreenManageImpl{} getNewScreenLinkTopN()"+ScreenConstant.TOPFLOWERROR_ACTION);
                timeTaskCommonsService.executeScreenTimeTask(ScreenConstant.TOPFLOWERROR_ACTION);
                return Reply.ok(newScreenTopNDto);
            }
            for (String itemName : itemNames) {
                String redislist = redisTemplate.opsForValue().get("homePage"+itemName);
                ItemRank itemRank = new ItemRank();
                if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                    itemRank = JSONObject.parseObject(redislist, ItemRank.class);
                    log.info("首页topN数据调用3"+itemNames+":::"+(System.currentTimeMillis() - startTime));
                    flowErrorDataFilter(itemRank.getItemNameRankList(),filterAssetsParam);
                    log.info("首页topN数据调用4"+itemNames+":::"+(System.currentTimeMillis() - startTime));
                }
                List<ItemNameRank> itemNameRankList = itemRank.getItemNameRankList();
                if(CollectionUtils.isEmpty(itemNameRankList)){continue;}
                //数据组装，发送和接收
                for (ItemNameRank itemNameRank : itemNameRankList){
                    if(map.get(itemNameRank.getAssetsId()+itemNameRank.getType()) == null){
                        MWNewScreenLinkTopNDto dto = new MWNewScreenLinkTopNDto();
                        dto.setId(itemNameRank.getId());
                        dto.setName(itemNameRank.getName());
                        dto.setIp(itemNameRank.getIp());
                        dto.setAssetsId(itemNameRank.getAssetsId());
                        dto.setIsWebMonitor(itemNameRank.getIsWebMonitor());
                        dto.setMonitorServerId(itemNameRank.getMonitorServerId());
                        dto.setType(itemNameRank.getType());
                        dto.setUrl(itemNameRank.getUrl());
                        dto.setParam(itemNameRank.getParam());
                        if("INTERFACE_IN_ERRORS".equals(itemName)){
                            dto.setAcceptLastValue(itemNameRank.getLastValue()==null?0:itemNameRank.getLastValue());
                            dto.setAcceptUnits(itemNameRank.getUnits());
                        }
                        if("INTERFACE_OUT_ERRORS".equals(itemName)){
                            dto.setSendLastValue(itemNameRank.getLastValue()==null?0:itemNameRank.getLastValue());
                            dto.setSendUnits(itemNameRank.getUnits());
                        }
                        map.put(itemNameRank.getAssetsId()+itemNameRank.getType(),dto);
                    }else{
                        MWNewScreenLinkTopNDto dto = map.get(itemNameRank.getAssetsId() + itemNameRank.getType());
                        if("INTERFACE_IN_ERRORS".equals(itemName)){
                            dto.setAcceptLastValue(itemNameRank.getLastValue()==null?0:itemNameRank.getLastValue());
                            dto.setAcceptUnits(itemNameRank.getUnits());
                        }
                        if("INTERFACE_OUT_ERRORS".equals(itemName)){
                            dto.setSendLastValue(itemNameRank.getLastValue()==null?0:itemNameRank.getLastValue());
                            dto.setSendUnits(itemNameRank.getUnits());
                        }
                        map.put(itemNameRank.getAssetsId()+itemNameRank.getType(),dto);
                    }
                }
            }
            List<MWNewScreenLinkTopNDto> screenLinkTopNDtos = new ArrayList<>();
            if(!map.isEmpty()){
                for (Map.Entry<String, MWNewScreenLinkTopNDto> stringMWNewScreenLinkTopNDtoEntry : map.entrySet()) {
                    screenLinkTopNDtos.add(stringMWNewScreenLinkTopNDtoEntry.getValue());
                }
            }
            if(CollectionUtils.isEmpty(screenLinkTopNDtos)){ return Reply.ok(newScreenTopNDto);}
            List<MWNewScreenLinkTopNDto> dtos = new ArrayList<>();
            dtos = screenLinkTopNDtos.subList(0, screenLinkTopNDtos.size());
            List<MWNewScreenLinkTopNDto> realData = new ArrayList<>();
            dataChange(newScreenTopNDto,"INTERFACE_ERRORS");
            realData = dtos;
            //资产状态查询
            List<ItemNameRank> itemNameRankList = new ArrayList<>();
            if(!CollectionUtils.isEmpty(realData)){
                for (MWNewScreenLinkTopNDto realDatum : realData) {
                    String assetsId = realDatum.getAssetsId();
                    Integer monitorServerId = realDatum.getMonitorServerId();
                    ItemNameRank rank = new ItemNameRank();
                    rank.setAssetsId(assetsId);
                    rank.setMonitorServerId(monitorServerId);
                    itemNameRankList.add(rank);
                }
            }
            log.info("首页topN数据调用5"+itemNames+":::"+(System.currentTimeMillis() - startTime));
            getAssetsStatus(itemNameRankList);
            if(CollectionUtils.isNotEmpty(realData) && CollectionUtils.isNotEmpty(itemNameRankList)){
                for (ItemNameRank rank : itemNameRankList) {
                    String assetsId = rank.getAssetsId();
                    String assetsStatus = rank.getAssetsStatus();
                    for (MWNewScreenLinkTopNDto realDatum : realData) {
                        String assetsId2 = realDatum.getAssetsId();
                        if(assetsId.equals(assetsId2)){
                            realDatum.setAssetsStatus(assetsStatus);
                        }
                    }
                }
            }
            List<ItemNameRank> ranks = new ArrayList<>();
            for (MWNewScreenLinkTopNDto realDatum : realData) {
                ItemNameRank rank = new ItemNameRank();
                rank.setMonitorServerId(realDatum.getMonitorServerId());
                rank.setAssetsId(realDatum.getAssetsId());
                rank.setId(realDatum.getId());
                rank.setAssetsStatus(realDatum.getAssetsStatus());
                rank.setIp(realDatum.getIp());
                rank.setName(realDatum.getName());
                rank.setType(realDatum.getType());
                rank.setAcceptLastValue(realDatum.getAcceptLastValue() == null?0:realDatum.getAcceptLastValue());
                rank.setSendLastValue(realDatum.getSendLastValue() == null?0:realDatum.getSendLastValue());
                rank.setSortTotalValue(new BigDecimal(rank.getAcceptLastValue()+rank.getSendLastValue()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue());
                rank.setUrl(realDatum.getUrl());
                rank.setParam(realDatum.getParam());
                ranks.add(rank);
            }
            Collections.sort(ranks, new Comparator<ItemNameRank>() {
                @Override
                public int compare(ItemNameRank o1, ItemNameRank o2) {
                    if(o2.getSortTotalValue() > o1.getSortTotalValue()){
                        return 1;
                    }
                    if(o2.getSortTotalValue() < o1.getSortTotalValue()){
                        return -1;
                    }
                    return 0;
                }
            });
            List<ItemNameRank> data = new ArrayList<>();
            if(ranks.size() > mwRankCount){
                data = ranks.subList(0, mwRankCount);
            }else{
                data = ranks;
            }
            newScreenTopNDto.setItemNameRankList(data);
            log.info("首页topN数据调用6"+itemNames+":::"+(System.currentTimeMillis() - startTime));
            return Reply.ok(newScreenTopNDto);
        }catch (Exception e){
            log.error("获取首页流量错误包数据排行失败",e);
            return Reply.fail("获取首页流量错误包数据排行失败");
        }
    }

    /**
     *获取流量带宽数据
     * @param param
     * @return
     */
    @Override
    public Reply getHomePageFlowBandWidthTopN(MWNewScreenAlertDevOpsEventDto param) throws Exception{
        long startTime = System.currentTimeMillis();
        List<String> names = new ArrayList<>();
        names.add("INTERFACE_IN_TRAFFIC");
        names.add("INTERFACE_OUT_TRAFFIC");
        Integer userId = param.getUserId();
        Integer modelId = param.getModelId();
        String modelDataId = param.getModelDataId();
        String key = genRedisKey("homePage", ScreenConstant.FLOW_BANDWIDTH_KEY);
        String redislist = redisTemplate.opsForValue().get(key);
        MWNewScreenTopNDto dto = new MWNewScreenTopNDto();
        MWNewScreenAssetsFilterDto filterAssetsParam = MWNewScreenAssetsFilterDto.builder().modelId(modelId == null?0:modelId).modelDataId(modelDataId).userId(userId).build();
        log.info("首页topN数据调用1"+names+":::"+(System.currentTimeMillis() - startTime));
        if (StringUtils.isNotBlank(redislist) && (param.getIsCache() == null || !param.getIsCache())) {
            dto = JSONObject.parseObject(redislist, MWNewScreenTopNDto.class);
            List<ItemNameRank> itemNameRankList = dto.getItemNameRankList();
            log.info("首页topN数据调用2"+names+":::"+(System.currentTimeMillis() - startTime));
            flowErrorDataFilter(itemNameRankList,filterAssetsParam);
            log.info("首页topN数据调用3"+names+":::"+(System.currentTimeMillis() - startTime));
            if(CollectionUtils.isNotEmpty(itemNameRankList) && itemNameRankList.size() > param.getMwRankCount()){
                dto.setItemNameRankList(itemNameRankList.subList(0,param.getMwRankCount()));
            }
            return Reply.ok(dto);
        }
        if(!schedulingEnabled){//直接返回，并手动执行任务
            log.info("MWNewScreenManageImpl{} getNewScreenLinkTopN()"+ScreenConstant.TOPBANDWIDTH_ACTION);
            timeTaskCommonsService.executeScreenTimeTask(ScreenConstant.TOPBANDWIDTH_ACTION);
            return Reply.ok(dto);
        }
        Map<Integer, List<String>> map = getAssetsFilterData(filterAssetsParam);
        ItemRank itemRank = mwModelManage.getScreenFlowNews(names, null, map,param.getMwRankCount());
        dto.setItemNameRankList(itemRank.getItemNameRankList());
        dto.setTitleNode(itemRank.getTitleNode());
        dto.setTitleRanks(itemRank.getTitleRanks());
        dataChange(dto,"INTERFACE_OUT_UTILIZATION");
        log.info("首页topN数据调用4"+names+":::"+(System.currentTimeMillis() - startTime));
        return Reply.ok(dto);
    }


    @Override
    public Reply getNewScreenModule() {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            //根据用户ID查询该用户模块
            List<MWNewScreenModuleDto> moduleDtos = newScreenManageDao.selectUserNewScreenModule(userId);
            List<Integer> bulkIds = new ArrayList<>();
            if(CollectionUtils.isEmpty(moduleDtos)){
                for (MWNewScreenModuleDto moduleDto : moduleDtos) {
                    bulkIds.add(moduleDto.getBulkId());
                }
                //查询初始化模块
                List<MWNewScreenModuleDto> initDtos = new ArrayList<>();
                List<MWNewScreenModuleDto> screenModuleDtos = newScreenManageDao.selectNewScreenInitModule(1);
                for (MWNewScreenModuleDto screenModuleDto : screenModuleDtos) {
                    if(!bulkIds.contains(screenModuleDto.getBulkId())){
                        String modelDateId = UuidUtil.getUid();
                        screenModuleDto.setModelDataId(modelDateId);
                        screenModuleDto.setUserId(userId);
                        screenModuleDto.setCount(5);
                        screenModuleDto.setDateType(1);
                        initDtos.add(screenModuleDto);
                    }
                }
                //插入用户的模块
                if(CollectionUtils.isNotEmpty(initDtos)){
                    newScreenManageDao.insertNewScreenUserModule(initDtos);
                    moduleDtos = newScreenManageDao.selectUserNewScreenModule(userId);
                }
            }
            if(!CollectionUtils.isEmpty(moduleDtos)){
                for (MWNewScreenModuleDto moduleDto : moduleDtos) {
                    Integer bulkId = moduleDto.getBulkId();
                    MWNewScreenModuleDto dto = newScreenManageDao.selectNewScreenInitModuleById(bulkId);
                    moduleDto.setModuleUrl(dto.getModuleUrl());
                    moduleDto.setDisplayTime(dto.getDisplayTime());
                    if(bulkId != null && bulkId == 2){
                        moduleDto.setName("CPU_UTILIZATION");
                    }
                    if(bulkId != null && bulkId == 5){
                        moduleDto.setName("MEMORY_UTILIZATION");
                    }
                    if(bulkId != null && bulkId == 3){
                        moduleDto.setName("ICMP_LOSS");
                    }
                    if(bulkId != null && bulkId == 8){
                        moduleDto.setName("DISK_UTILIZATION");
                    }
                    if(bulkId != null && bulkId == 6){
                        moduleDto.setName("ICMP_RESPONSE_TIME");
                    }
                }
            }
            //按照时间排序
            Collections.sort(moduleDtos, new Comparator<MWNewScreenModuleDto>() {
                @Override
                public int compare(MWNewScreenModuleDto o1, MWNewScreenModuleDto o2) {
                    return o1.getCreateDate().compareTo(o2.getCreateDate());
                }
            });
            return Reply.ok(moduleDtos);
        }catch (Exception e){
            log.error("查询新大屏模块失败",e);
            return Reply.fail("查询新大屏模块失败");
        }
    }


    private void dataChange(MWNewScreenTopNDto dto,String name){
        List<Map<String,Object>> listMap = new ArrayList<>();
        if("CPU_UTILIZATION".equals(name)){
            Map<String,Object> map = new HashMap<>();
            map.put("name","资产名称");
            map.put("prop","name");
            Map<String,Object> map2 = new HashMap<>();
            map2.put("name","CPU利用率");
            map2.put("prop","lastValue");
            Map<String,Object> map3 = new HashMap<>();
//            map3.put("name","");
//            map3.put("prop","progress");
            listMap.add(map);
            listMap.add(map2);
//            listMap.add(map3);
        }
        if("MEMORY_UTILIZATION".equals(name)){
            Map<String,Object> map = new HashMap<>();
            map.put("name","资产名称");
            map.put("prop","name");
            Map<String,Object> map2 = new HashMap<>();
            map2.put("name","内存使用率");
            map2.put("prop","lastValue");
            Map<String,Object> map3 = new HashMap<>();
//            map3.put("name","");
//            map3.put("prop","progress");
            listMap.add(map);
            listMap.add(map2);
//            listMap.add(map3);
        }
        if("ICMP_LOSS".equals(name)){
            Map<String,Object> map = new HashMap<>();
            map.put("name","资产名称");
            map.put("prop","name");
            Map<String,Object> map2 = new HashMap<>();
            map2.put("name","丢包率");
            map2.put("prop","lastValue");
//            Map<String,Object> map3 = new HashMap<>();
//            map3.put("name","延时率");
//            map3.put("prop","lossPingValue");
            listMap.add(map);
            listMap.add(map2);
//            listMap.add(map3);
        }
        if("DISK_UTILIZATION".equals(name)){
            Map<String,Object> map = new HashMap<>();
            map.put("name","资产名称");
            map.put("prop","name");
            Map<String,Object> map2 = new HashMap<>();
            map2.put("name","分区名称");
            map2.put("prop","type");
            Map<String,Object> map3 = new HashMap<>();
            map3.put("name","磁盘使用率");
            map3.put("prop","lastValue");
            listMap.add(map);
            listMap.add(map2);
            listMap.add(map3);
        }
        if("INTERFACE_ERRORS".equals(name)){
            Map<String,Object> map = new HashMap<>();
            map.put("name","资产名称");
            map.put("prop","name");
            Map<String,Object> map2 = new HashMap<>();
            map2.put("name","接口");
            map2.put("prop","type");
            Map<String,Object> map3 = new HashMap<>();
            map3.put("name","流量(入)");
            map3.put("prop","acceptLastValue");
            Map<String,Object> map4 = new HashMap<>();
            map4.put("name","流量(出)");
            map4.put("prop","sendLastValue");
            listMap.add(map);
            listMap.add(map2);
            listMap.add(map3);
            listMap.add(map4);

        }
        if("INTERFACE_OUT_UTILIZATION".equals(name)){
            Map<String,Object> map = new HashMap<>();
            map.put("name","资产名称");
            map.put("prop","name");
            Map<String,Object> map2 = new HashMap<>();
            map2.put("name","接口");
            map2.put("prop","type");
            Map<String,Object> map3 = new HashMap<>();
            map3.put("name","流量(入)");
            map3.put("prop","acceptStrLastValue");
            Map<String,Object> map4 = new HashMap<>();
            map4.put("name","流量(出)");
            map4.put("prop","sendStrLastValue");
            listMap.add(map);
            listMap.add(map2);
            listMap.add(map3);
            listMap.add(map4);
        }
        if("ICMP_RESPONSE_TIME".equals(name)){
            Map<String,Object> map = new HashMap<>();
            map.put("name","资产名称");
            map.put("prop","name");
            Map<String,Object> map2 = new HashMap<>();
            map2.put("name","延时");
            map2.put("prop","lastValue");
            listMap.add(map);
            listMap.add(map2);
        }
        dto.setTitle(listMap);
    }


    /**
     * 获取TOPN的资产状态
     * @param itemNameRankList
     */
    private void getAssetsStatus(List<ItemNameRank> itemNameRankList){
        if(CollectionUtils.isEmpty(itemNameRankList)){
            return;
        }
        Map<Integer, List<String>> serverIdAndAssetsId = new HashMap<>();
        for (ItemNameRank itemNameRank : itemNameRankList) {
            Integer serverId = itemNameRank.getMonitorServerId();
            String assetsId = itemNameRank.getAssetsId();
            if (serverIdAndAssetsId.isEmpty() || CollectionUtils.isEmpty(serverIdAndAssetsId.get(Integer.parseInt(serverId.toString())))) {
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId.toString());
                serverIdAndAssetsId.put(Integer.parseInt(serverId.toString()), assetsIds);
                continue;
            }
            if (!serverIdAndAssetsId.isEmpty() || !CollectionUtils.isEmpty(serverIdAndAssetsId.get(Integer.parseInt(serverId.toString())))) {
                List<String> assetsIds = serverIdAndAssetsId.get(Integer.parseInt(serverId.toString()));
                assetsIds.add(assetsId.toString());
                serverIdAndAssetsId.put(Integer.parseInt(serverId.toString()), assetsIds);
            }
        }
        if (serverIdAndAssetsId.isEmpty()) {
            return;
        }
        //查询zabbix资产状态
        Map<String, String> statusMap = new HashMap<>();
        Set<String> hostSets = new HashSet<>();
        for (Map.Entry<Integer, List<String>> value : serverIdAndAssetsId.entrySet()) {
            if (value.getKey() != null && value.getKey() > 0) {
                MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.NEW_ASSETS_STATUS, value.getValue());
                if (!statusData.isFail()) {
                    JsonNode jsonNode = (JsonNode) statusData.getData();
                    if (jsonNode.size() > 0) {
                        for (JsonNode node : jsonNode) {
                            Integer lastvalue = node.get("lastvalue").asInt();
                            String hostId = node.get("hostid").asText();
                            String name = node.get("name").asText();
                            if((ZabbixItemConstant.MW_HOST_AVAILABLE).equals(name)){
                                String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                statusMap.put(value.getKey() + ":" + hostId, status);
                                hostSets.add(hostId);
                            }
                            if(hostSets.contains(hostId)){
                                continue;
                            }
                            String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                            statusMap.put(value.getKey() + ":" + hostId, status);
                        }
                    }
                }
                /*statusMap.put(value.getKey() + ":" + value.getValue(), "ABNORMAL");*/
            }
        }
        for (ItemNameRank itemNameRank : itemNameRankList) {
            Integer serverId = itemNameRank.getMonitorServerId();
            String assetsId = itemNameRank.getAssetsId();
            String s = statusMap.get(serverId + ":" + assetsId);
            if (s != null && StringUtils.isNotEmpty(s)) {
                itemNameRank.setAssetsStatus(s);
            } else {
                itemNameRank.setAssetsStatus("UNKNOWN");
            }
        }
    }

    /**
     * 查询消息信息
     * @param userId 用户ID
     * @param modelId 模块ID
     * @param modelDataId
     * @param dto
     */
    private void getAlertMessage(Integer userId,Integer modelId,String modelDataId,MWNewScreenAlertDevOpsEventDto dto){
        StringBuffer sb = new StringBuffer();
        sb.append("messageStatistics").append(":").append("message")
                .append("_").append(userId);
        String key = sb.toString();
        String redisValue = redisTemplate.opsForValue().get(key);
        MessageDto messageDto = new MessageDto();
        if (null != redisValue && StringUtils.isNotEmpty(redisValue)) {
            messageDto = JSONObject.parseObject(redisValue, MessageDto.class);
        } else {
            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelId).modelDataId(modelDataId).userId(userId).type(DataType.INDEX.getName()).build();
            messageDto = mwModelManage.messageStatistics(filterAssetsParam);
        }
        boolean inspectModeInfo = mwInspectModeService.getInspectModeInfo();
        if(inspectModeInfo){
            MessageCount sumMessage = messageDto.getSumMessage();
            sumMessage.setFailedCount(0);
            sumMessage.setTotalCount(sumMessage.getSuccessCount());
            dto.setSumMessage(sumMessage);
            return;
        }
        if(messageDto != null){
            dto.setSumMessage(messageDto.getSumMessage());
            dto.setTodayMessage(messageDto.getTodayMessage());
        }
    }

    /**
     * 查询告警信息
     * @param dto
     */
    private void getCurrAlert(MWNewScreenAlertDevOpsEventDto mwNewScreenAlertDevOpsEventDto,MWNewScreenAlertDevOpsEventDto dto ){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //查询当前告警信息
        AlertParam alertParam = new AlertParam();
        alertParam.setPageSize(Integer.MAX_VALUE);
        int total;
        int acknowledgedCount = 0;
        int unAcknowledged = 0;
        Integer dateType = mwNewScreenAlertDevOpsEventDto.getDateType();
        if(dateType != null && dateType == 1){//今天时间
            List<Date> today = MWNewScreenDateUtil.getToday();
            alertParam.setStartTime(format.format(today.get(0)));
            alertParam.setEndTime(format.format(today.get(1)));
        }
        if(dateType != null && dateType == 0){//自定义时间
            alertParam.setStartTime(mwNewScreenAlertDevOpsEventDto.getStartTime());
            alertParam.setEndTime(mwNewScreenAlertDevOpsEventDto.getEndTime());
        }
        List<String> hostIds = alertHandler.handler();
        if(CollectionUtils.isNotEmpty(hostIds)){
            alertParam.setQueryHostIds(hostIds);
        }
        Reply reply = mwalertService.getCurrAlertPage(alertParam);
        if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS && reply.getData() != null){
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<ZbxAlertDto> list = pageInfo.getList();
            if(CollectionUtils.isNotEmpty(list)){
                for (ZbxAlertDto zbxAlertDto : list) {
                    String acknowledged = zbxAlertDto.getAcknowledged();
                    if(StringUtils.isNotBlank(acknowledged) && AlertAssetsEnum.unconfirmed.toString().equals(acknowledged)){
                        unAcknowledged++;
                    }
                    if(StringUtils.isNotBlank(acknowledged) && AlertAssetsEnum.confirmed.toString().equals(acknowledged)){
                        acknowledgedCount++;
                    }
                }
            }
        }
        List<Map<String,Object>> listMaps = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("key",AlertAssetsEnum.unconfirmed.toString());
        map.put("value",unAcknowledged);
        listMaps.add(map);
        Map<String,Object> map2 = new HashMap<>();
        map2.put("key",AlertAssetsEnum.confirmed.toString());
        map2.put("value",acknowledgedCount);
        listMaps.add(map2);
        dto.setAlertCount(listMaps);
        dto.setTolatAlertCount(unAcknowledged+acknowledgedCount);
    }

    private String genRedisKey(String methodName, String objectName) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append(objectName);
        return sb.toString();
    }

    /**
     * 获取资产状态
     */
    private void getAseetsStatus(List<MwScreenAssetsDto> screenAssetsDtos,MWNewScreenAssetsDto dto) {
        Map<String,String> typeIconMap = new HashMap<>();
        dto.setTotalAseetsAmount(screenAssetsDtos.size());
        List<String> downTimeAssets = new ArrayList<>();
        Map<String,String> typeMap = new HashMap<>();
        //正常资产
        List<String> normalAssets = new ArrayList<>();
        //异常资产
        List<String> unusualAssets = new ArrayList<>();
        Map<String, List<MwScreenAssetsDto>> assetsMap = new HashMap<>();
        for (MwScreenAssetsDto screenAssetsDto : screenAssetsDtos) {
            if (screenAssetsDto.getMonitorFlag() == null || !screenAssetsDto.getMonitorFlag()) {//说明该资产宕机了
                downTimeAssets.add(screenAssetsDto.getId());
                assetsStatusGroup(AssetsStatusEnum.SHUTDOWN.getName(),assetsMap,screenAssetsDto);
                continue;
            }
            if (StringUtils.isNotEmpty(screenAssetsDto.getAssetsStatus()) && AssetsStatusEnum.NORMAL.getName().equals(screenAssetsDto.getAssetsStatus()) && !downTimeAssets.contains(screenAssetsDto.getId())) {
                normalAssets.add(screenAssetsDto.getId());
                assetsStatusGroup(AssetsStatusEnum.NORMAL.getName(),assetsMap,screenAssetsDto);
            } else {
                if(!downTimeAssets.contains(screenAssetsDto.getId())){
                    log.info("MWNewScreenManageImpl{} getNewScreenAssets() ABNORMALDTO::"+screenAssetsDto);
                    unusualAssets.add(screenAssetsDto.getId());
                    assetsStatusGroup(AssetsStatusEnum.ABNORMAL.getName(),assetsMap,screenAssetsDto);
                }
            }
            if(StringUtils.isNotBlank(screenAssetsDto.getTypeName())){
                typeMap.put(screenAssetsDto.getAssetsId(),screenAssetsDto.getTypeName());
                if(StringUtils.isBlank(screenAssetsDto.getUrl())){
                    typeIconMap.put(screenAssetsDto.getTypeName(),"");
                    continue;
                }
                typeIconMap.put(screenAssetsDto.getTypeName(),screenAssetsDto.getUrl());
            }
        }
        //设置正常数量
        dto.setNormalAssets(normalAssets);
        dto.setNormalAseetsAmount(normalAssets.size());
        //设置异常资产
        dto.setUnusualAssets(unusualAssets);
        dto.setUnusualAseetsAmount(unusualAssets.size());
        //设置宕机资产
        dto.setDownTimeAssets(downTimeAssets);
        dto.setDownTimeAseetsAmount(downTimeAssets.size());
        //按照资产状态分组
        for (String status : assetsMap.keySet()) {
            List<MwScreenAssetsDto> dtos = assetsMap.get(status);
            setAssetsTypeClassifyInfo(dtos,dto,status);
        }
    }

    /**
     * 资产状态分组
     */
    private void assetsStatusGroup(String status,Map<String, List<MwScreenAssetsDto>> assetsMap,MwScreenAssetsDto screenAssetsDto){
        List<MwScreenAssetsDto> screenAssetsDtos = assetsMap.get(status);
        if(screenAssetsDtos == null){
            screenAssetsDtos = new ArrayList<>();
            screenAssetsDtos.add(screenAssetsDto);
            assetsMap.put(status,screenAssetsDtos);
            return;
        }
        screenAssetsDtos.add(screenAssetsDto);
    }

    /**
     * 设置资产类型分类数据
     */
    private void setAssetsTypeClassifyInfo(List<MwScreenAssetsDto> assetsDtos,MWNewScreenAssetsDto dto,String status){
        AssetsStatusEnum assetsStatusEnum = AssetsStatusEnum.valueOf(status);
        Map<String, List<MwScreenAssetsDto>> collect = assetsDtos.stream().collect(Collectors.groupingBy(item -> item.getTypeName()));
        List<MWNewScreenAssetsClassifyDto> classifyDtos = new ArrayList<>();
        for (String typeNmae : collect.keySet()) {
            List<MwScreenAssetsDto> screenAssetsDtos = collect.get(typeNmae);
            if(CollectionUtils.isEmpty(screenAssetsDtos)){continue;}
            MWNewScreenAssetsClassifyDto classifyDto = new MWNewScreenAssetsClassifyDto();
            classifyDto.extractFrom(typeNmae,screenAssetsDtos.get(0).getUrl(),screenAssetsDtos.size());
            classifyDtos.add(classifyDto);
        }
        switch (assetsStatusEnum){
            case NORMAL:
                dto.setNormalAssetsMap(classifyDtos);
                break;
            case ABNORMAL:
                dto.setUnusualAssetsMap(classifyDtos);
                break;
            case SHUTDOWN:
                dto.setDownTimeAssetsMap(classifyDtos);
        }
    }

    /**
     * 查询新大屏模块下拉数据
     * @return
     */
    @Override
    public Reply getNewScreenModuleDropDown() {
        try {
            List<MWNewScreenModuleDto> screenModuleDtos = newScreenManageDao.selectNewScreenInitModule(null);
            return Reply.ok(screenModuleDtos);
        }catch (Exception e){
            log.error("查询新大屏模块下拉失败",e);
            return Reply.fail("查询新大屏模块下拉失败");
        }
    }

    /**
     * 添加新大屏模块
     * @param screenModuleDto 参数信息
     * @return
     */
    @Override
    public Reply createNewScreenUserModule(MWNewScreenModuleDto screenModuleDto) {
        try {
            List<MWNewScreenModuleDto> dtoList = new ArrayList<>();
            String modelDataId = UuidUtil.getUid();
            screenModuleDto.setModelDataId(modelDataId);
            dtoList.add(screenModuleDto);
            newScreenManageDao.insertNewScreenUserModule(dtoList);
            int bulkId = screenModuleDto.getBulkId();
            //根据ID查询url
            MWNewScreenModuleDto dto = newScreenManageDao.selectNewScreenInitModuleById(bulkId);
            dto.setModelDataId(modelDataId);
            return Reply.ok(dto);
        }catch (Exception e){
            log.error("新增大屏模块失败",e);
            return Reply.fail("新增大屏模块失败"+e.getMessage());
        }
    }

    /**
     * 修改大屏模块数据
     * @param assetsFilterDto 修改大屏参数
     * @return
     */
    @Override
    public Reply updateNewScreenUserModule(MWNewScreenAssetsFilterDto assetsFilterDto) {
        try {
            Integer id = assetsFilterDto.getId();
            if(id == null){//ID为空，添加资产过滤
                newScreenManageDao.insertNewScreenAssetsFilter(assetsFilterDto);
            }else{//ID不为空，修改资产过滤
                newScreenManageDao.updateNewScreenAssetsFilter(assetsFilterDto);
            }
            //添加标签数据
            List<MwRuleSelectParam> labelDtos = assetsFilterDto.getLabelDtos();
            if(CollectionUtils.isNotEmpty(labelDtos)){
                for (MwRuleSelectParam labelDto : labelDtos) {
                    labelDto.setUuid("NEW_HOME_"+assetsFilterDto.getId());
                }
                //将原来的标签信息进行删除
                newScreenManageDao.deleteMwAlertRuleSelect("NEW_HOME_"+assetsFilterDto.getId());
                //在添加新的数据
                List<MwRuleSelectParam> newLabelDtos = new ArrayList<>();
                if(labelDtos != null && labelDtos.size() > 0){
                    for (MwRuleSelectParam s : labelDtos){
                        //判断是否选择了标签数据
                        List<MwRuleSelectParam> constituentElements = s.getConstituentElements();
                        List<MwRuleSelectParam> constituentElements1 = constituentElements.get(0).getConstituentElements();
                        if(CollectionUtils.isEmpty(constituentElements1)){continue;}
                        MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
                        ruleSelectDto.setCondition(s.getCondition());
                        ruleSelectDto.setDeep(s.getDeep());
                        ruleSelectDto.setKey(s.getKey());
                        ruleSelectDto.setName(s.getName());
                        ruleSelectDto.setParentKey(s.getParentKey());
                        ruleSelectDto.setRelation(s.getRelation());
                        ruleSelectDto.setValue(s.getValue());
                        ruleSelectDto.setUuid("NEW_HOME_"+assetsFilterDto.getId());
                        newLabelDtos.add(ruleSelectDto);
                        s.setUuid("NEW_HOME_"+assetsFilterDto.getId());
                        if(s.getConstituentElements() != null && s.getConstituentElements().size() > 0){
                            newLabelDtos.addAll(delMwRuleSelectList(s,assetsFilterDto.getId()));
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(newLabelDtos)){
                    newScreenManageDao.insertMwAlertRuleSelect(newLabelDtos);
                }
            }
            //修改卡片名称
            newScreenManageDao.updateNewScreenModuleName(assetsFilterDto.getBulkName(),assetsFilterDto.getModelDataId(),assetsFilterDto.getUserId());
            return Reply.ok("模块修改成功");
        }catch (Exception e){
            log.error("修改大屏模块失败",e);
            return Reply.fail("修改大屏模块失败"+e.getMessage());
        }
    }

    /**
     * 查询大屏资产过滤数据
     * @param assetsFilterDto 查询参数
     * @return
     */
    @Override
    public Reply selectNewScreenUserModule(MWNewScreenAssetsFilterDto assetsFilterDto) {
        try {
            //根据用户ID，模块ID，卡片ID查询资产过滤数据
            MWNewScreenAssetsFilterDto mwNewScreenAssetsFilterDto = newScreenManageDao.selectNewScreenAssetsFilterData(assetsFilterDto.getModelDataId(), assetsFilterDto.getUserId(), assetsFilterDto.getModelId());
            //根据ID查询标签
            if(mwNewScreenAssetsFilterDto != null && mwNewScreenAssetsFilterDto.getId() != null){
                Integer id = mwNewScreenAssetsFilterDto.getId();
                List<MwRuleSelectParam> mwRuleSelectParams = newScreenManageDao.selectMwAlertRuleSelect("NEW_HOME_" + id);
                List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                if(mwRuleSelectParams != null && mwRuleSelectParams.size() > 0){
                    for (MwRuleSelectParam s : mwRuleSelectParams){
                        if(s.getKey().equals("root")){
                            ruleSelectParams.add(s);
                        }
                    }
                    for(MwRuleSelectParam s : ruleSelectParams){
                        s.setConstituentElements(getChild(s.getKey(),mwRuleSelectParams));
                    }
                }
                mwNewScreenAssetsFilterDto.setLabelDtos(ruleSelectParams);
            }
            return Reply.ok(mwNewScreenAssetsFilterDto);
        }catch (Exception e){
            log.error("查询资产过滤数据失败",e);
            return Reply.fail("查询资产过滤数据失败"+e.getMessage());
        }
    }

    /**
     * 删除首页用户对应模块
     * @param assetsFilterDto
     * @return
     */
    @Override
    public Reply deleteNewScreenUserModule(MWNewScreenAssetsFilterDto assetsFilterDto) {
        try {
            //删除用户对应模块
            newScreenManageDao.deleteNewScreenUserModule(assetsFilterDto.getModelDataId(), assetsFilterDto.getUserId(), assetsFilterDto.getModelId());
            return Reply.ok("删除成功");
        }catch (Exception e){
            log.error("删除用户模块失败",e);
            return Reply.fail("删除用户模块失败"+e.getMessage());
        }
    }

    /**
     * 首页模块排序功能
     * @param screenModuleDto 参数列表
     * @return
     */
    @Override
    public Reply newScreenModuleSort(List<MWNewScreenModuleDto> screenModuleDto) {
        try {
            if(CollectionUtils.isEmpty(screenModuleDto))return Reply.ok("排序成功");
            //实现排序
            long time = 1000;
            for (MWNewScreenModuleDto dto : screenModuleDto) {
                Date currDate = new Date();
                currDate.setTime(currDate.getTime()+time);
                newScreenManageDao.updateNewScreenCreateDate(currDate,dto.getModelDataId(),dto.getUserId(),dto.getBulkId());
                time+=1000;
            }
            return Reply.ok("排序成功");
        }catch (Exception e){
            log.error("排序失败",e);
            return Reply.fail("排序失败"+e.getMessage());
        }
    }


    public ItemRank getHostRank(String name, MWNewScreenAssetsFilterDto filterAssetsParam){
        ItemRank itemRank = new ItemRank();
        Map<Integer, List<String>> map = getAssetsFilterData(filterAssetsParam);
        if (null != map && map.size() > 0) {
            for (Integer key : map.keySet()) {
                List<String> hostIds = map.get(key);
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(key, name, hostIds);
                log.info("new scrren{}"+key+":"+name+":"+result);
                if (result != null && result.code == 0) {
                    JsonNode itemData = (JsonNode) result.getData();
                    if (itemData.size() > 0) {
                        List<ItemNameRank> itemNameRanks = new ArrayList<>();
                        List<TitleRank> titleRanks = new ArrayList<>();
                        TitleRank titleRank1 = TitleRank.builder().name("资产名称").fieldName("name").build();
                        titleRanks.add(titleRank1);
                        ExecutorService executorService = Executors.newFixedThreadPool(40);
                        List<Future<ItemNameRank>> futureList = new ArrayList<>();
                        for (JsonNode item : itemData) {
                            GetDataByCallable<ItemNameRank> getDataByCallable = new GetDataByCallable<ItemNameRank>() {
                                @Override
                                public ItemNameRank call() throws Exception {
                                    ItemNameRank itemNameRank = new ItemNameRank();
                                    String hostId = item.get("hostid").asText();
                                    String itemName = item.get("name").asText();
                                    if(StringUtils.isBlank(itemName) || itemName.contains(ScreenConstant.VMEMORY_UTILIZATION)){return itemNameRank;}
                                    AssetsDto assets = mwModelViewCommonService.getAssetsById(hostId ,key);
                                    if (null != assets) {
                                        itemNameRank.setId(assets.getId());
                                        itemNameRank.setName(assets.getAssetsName());
                                        itemNameRank.setIp(assets.getAssetsIp());
                                        itemNameRank.setAssetsId(assets.getAssetsId());
                                        itemNameRank.setMonitorServerId(assets.getMonitorServerId());
                                        itemNameRank.setUrl(assets.getUrl());
                                        itemNameRank.setParam(assets.getParam());

                                        String lastvalue = item.get("lastvalue").asText();
                                        Map<String, String> map1 = UnitsUtil.getValueAndUnits(lastvalue, item.get("units").asText());
                                        Map<String, String> map2 = UnitsUtil.getConvertedValue(new BigDecimal(lastvalue), item.get("units").asText());
                                        if (null != map2) {
                                            String dataUnits = map2.get("units");
                                            String v = UnitsUtil.getValueMap(lastvalue, dataUnits, item.get("units").asText()).get("value");
                                            Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                            itemNameRank.setSortlastValue(Double.valueOf(lastvalue));
                                            itemNameRank.setLastValue(values);
                                            itemNameRank.setProgress(values);
                                            itemNameRank.setUnits(dataUnits);
                                        }
//                                if (null != map1) {
//                                    itemNameRank.setSortlastValue(Double.valueOf(lastvalue));
//                                    itemNameRank.setLastValue(Double.parseDouble(map1.get("value")));
//                                    itemNameRank.setUnits(map1.get("units"));
//                                }
                                        if (name.equals("DISK_UTILIZATION") ) {
                                            int i = item.get("name").asText().indexOf("]");
                                            if (i != -1) {
                                                itemNameRank.setType(item.get("name").asText().substring(1, i));
                                                itemNameRank.setName(assets.getAssetsName() + item.get("name").asText().substring(0, i + 1));
                                            }
                                        }
                                        if (name.equals("INTERFACE_IN_TRAFFIC") || name.equals("INTERFACE_OUT_TRAFFIC") || name.equals("INTERFACE_IN_UTILIZATION")
                                                || name.equals("INTERFACE_OUT_UTILIZATION") || name.equals("INTERFACE_OUT_ERRORS") || name.equals("INTERFACE_IN_ERRORS")) {
                                            int i = item.get("name").asText().indexOf("]");
                                            if (i != -1) {
                                                itemNameRank.setType(item.get("name").asText().substring(1, i));
                                                itemNameRank.setName(assets.getAssetsName());
                                            }
                                        }
//                                        itemNameRanks.add(itemNameRank);
                                    }
                                    return itemNameRank;
                                }
                            };
                            if(null!=getDataByCallable){
                                Future<ItemNameRank> f = executorService.submit(getDataByCallable);
                                futureList.add(f);
                            }
                        }
                        for (Future<ItemNameRank> itemNameRankFuture : futureList) {
                            try {
                                ItemNameRank itemNameRank = itemNameRankFuture.get(30, TimeUnit.SECONDS);
                                if(itemNameRank.getSortlastValue()>=0){
                                    itemNameRanks.add(itemNameRank);
                                }
                            } catch (Exception e) {
                                itemNameRankFuture.cancel(true);
                                executorService.shutdown();
                            }
                        }
                        executorService.shutdown();
                        log.info("getHostRank()::"+name+"::"+itemNameRanks);
                        //一个资产有多个cpu或者内存取平均值
                        if("CPU_UTILIZATION".equals(name) || "MEMORY_UTILIZATION".equals(name)){
                            Map<String, List<ItemNameRank>> collect = itemNameRanks.stream().collect(Collectors.groupingBy(ItemNameRank::getAssetsId));
                            log.info("getHostRank()22::"+name+"::"+collect);
                            ItemNameRank ite=new ItemNameRank();
                            List<ItemNameRank> list=new ArrayList<>();
                            for (List<ItemNameRank> ranks : collect.values()) {
                                if(ranks.size()>1){
                                    double asDouble = new BigDecimal(ranks.stream().filter(item->item.getLastValue() != null).mapToDouble(ItemNameRank::getLastValue).average().getAsDouble()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                                    ite = ranks.get(0);
                                    ite.setLastValue(asDouble);
                                    ite.setProgress(asDouble);
                                    ite.setValue(String.valueOf(asDouble));
                                    ite.setSortlastValue(asDouble);
                                }else {
                                    ite =ranks.get(0);
                                }
                                list.add(ite);
                            }
                            itemNameRanks=list;
                        }
//                        if(!name.equals("INTERFACE_OUT_ERRORS") && !name.equals("INTERFACE_IN_ERRORS")){
//                            Collections.sort(itemNameRanks, new ItemNameRank());//倒序排序
//                            if (itemNameRanks.size() > 20) {
//                                itemNameRanks = itemNameRanks.subList(0, 20);
//                            }
//                        }
                        log.info("getHostRank()33::"+name+"::"+itemNameRanks);
                        List<String> node = new ArrayList<>();
                        node.add("资产名称");
//                        node.add("IP地址");
                        switch (name) {
                            case "CPU_UTILIZATION":
                                node.add("CPU利用率");
                                TitleRank titleRank3 = TitleRank.builder().name("CPU利用率").fieldName("lastValue").build();
                                titleRanks.add(titleRank3);
                                break;
                            case "DISK_UTILIZATION":
                                node.add("磁盘分区名称");
                                node.add("磁盘利用率");
                                TitleRank titleRank4 = TitleRank.builder().name("磁盘分区名称").fieldName("type").build();
                                TitleRank titleRank5 = TitleRank.builder().name("磁盘利用率").fieldName("lastValue").build();
                                titleRanks.add(titleRank4);
                                titleRanks.add(titleRank5);
                                break;
                            case "MEMORY_UTILIZATION":
                                node.add("内存利用率");
                                TitleRank titleRank6 = TitleRank.builder().name("内存利用率").fieldName("lastValue").build();
                                titleRanks.add(titleRank6);
                                break;
                            case "ICMP_LOSS":
                                node.add("节点丢包率");
                                TitleRank titleRank7 = TitleRank.builder().name("节点丢包率").fieldName("lastValue").build();
                                titleRanks.add(titleRank7);
                                break;
                            case "INTERFACE_IN_TRAFFIC":
                                node.add("接口名称");
                                node.add("每秒接收流量");
                                TitleRank titleRank8 = TitleRank.builder().name("接口名称").fieldName("type").build();
                                TitleRank titleRank9 = TitleRank.builder().name("每秒接收流量").fieldName("lastValue").build();
                                titleRanks.add(titleRank8);
                                titleRanks.add(titleRank9);
                                break;
                            case "INTERFACE_OUT_TRAFFIC":
                                node.add("接口名称");
                                node.add("每秒发送流量");
                                TitleRank titleRank10 = TitleRank.builder().name("接口名称").fieldName("type").build();
                                TitleRank titleRank11 = TitleRank.builder().name("每秒发送流量").fieldName("lastValue").build();
                                titleRanks.add(titleRank10);
                                titleRanks.add(titleRank11);
                                break;
                            case "ICMP_RESPONSE_TIME":
                                node.add("节点延时");
                                TitleRank titleRank12 = TitleRank.builder().name("节点延时").fieldName("lastValue").build();
                                titleRanks.add(titleRank12);
                                break;
                            default:
                                break;
                        }
                        itemRank.setTitleNode(node);
                        List<ItemNameRank> itemNameRankList = itemRank.getItemNameRankList();
                        if(CollectionUtils.isNotEmpty(itemNameRankList)){
                            itemNameRankList.addAll(itemNameRanks);
                            itemRank.setItemNameRankList(itemNameRankList);
                        }else{
                            itemRank.setItemNameRankList(itemNameRanks);
                        }
                        List<TitleRank> titleRanks2 = itemRank.getTitleRanks();
                        if(CollectionUtils.isNotEmpty(titleRanks2)){
                            titleRanks2.addAll(titleRanks);
                            itemRank.setTitleRanks(titleRanks2);
                        }else{
                            itemRank.setTitleRanks(titleRanks);
                        }
                    }
                }
            }
        }
        return itemRank;
    }


    /**
     * 检查模式数据过滤
     * @param itemNameRanks
     */
    private void itemRankFilter(List<ItemNameRank> itemNameRanks){
        boolean inspectModeInfo = mwInspectModeService.getInspectModeInfo();
        if(CollectionUtils.isEmpty(itemNameRanks) || !inspectModeInfo){return;}
        Iterator<ItemNameRank> iterator = itemNameRanks.iterator();
        while(iterator.hasNext()){
            ItemNameRank itemNameRank = iterator.next();
            Double lastValue = itemNameRank.getLastValue();
            if(lastValue == null || lastValue >= 70){
                iterator.remove();
            }
        }
    }

//    private Map<Integer, List<String>> getAssetIdsByServerId(MWNewScreenAssetsFilterDto param) {
//        MwCommonAssetsDto mwCommonAssetsDto = newScreenManageDao.getNewScreenFilterAssets(param);
//        log.info("资产过滤条件："+mwCommonAssetsDto);
//        if (null == mwCommonAssetsDto) {
//            mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(param.getUserId()).assetsTypeId(param.getAssetsTypeId()).build();
//        }
//        Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
//        if(StringUtils.isNotBlank(mwCommonAssetsDto.getFilterOrgId())&&assets!=null){
//            assets= getAssetByOrgId(assets,mwCommonAssetsDto.getFilterOrgId());
//        }
//        log.info("首页assets：" + assets);
//        Map<Integer, List<String>> map = mwAssetsManager.getAssetsByServerId(assets);
//        return map;
//    }

//    public Map<String,Object> getAssetByOrgId(Map<String,Object> assets,String assetOrgId){
//        List<MwTangibleassetsTable> mwTangibleassetsDTOS;
//        List<String> assetIds =new ArrayList<>();
//        List<String> ids =new ArrayList<>();
//        List<MwTangibleassetsTable> result=new ArrayList<>();
//        List<List<Integer>> filterOrgIds = JSON.parseObject(assetOrgId, List.class);
//        if(assets.get("assetsList")!=null){
//            mwTangibleassetsDTOS= (List<MwTangibleassetsTable>) assets.get("assetsList");
//            if(mwTangibleassetsDTOS.size()>0){
//                ExecutorService executorService = Executors.newFixedThreadPool(20);
//                List<Future<MwTangibleassetsTable>> futureList = new ArrayList<>();
//                for(MwTangibleassetsTable mw:mwTangibleassetsDTOS){
//                    GetDataByCallable<MwTangibleassetsTable> getDataByCallable=new GetDataByCallable<MwTangibleassetsTable>() {
//                        @Override
//                        public MwTangibleassetsTable call() throws Exception {
//                            //根据资产id获取对应机构，一个资产至少包含一个机构
//                            MwTangibleassetsDTO assetsAndOrgs = mwAssetsManager.getAssetsAndOrgs(mw.getId());
//                            if(null!=assetsAndOrgs){
//                                if(assetsAndOrgs.getDepartment().size()>0){
//                                    List<OrgDTO> department = assetsAndOrgs.getDepartment();
//                                    //一个资产多个机构
//                                    List<Integer> ss=new ArrayList<>();
//                                    for (OrgDTO o : department) {
//                                        if(null!=o){
//                                            ss.add(o.getOrgId());
//                                        }
//                                    }
//                                    if(ss.size()>0){
//                                        for (List<Integer> filterOrgId : filterOrgIds) {
//                                            if(ss.contains(filterOrgId.get(filterOrgId.size()-1))){
//                                                return mw;
//                                            }
//                                        }
//                                        return null;
//                                    }
//                                }
//                            }
//                            return null;
//                        }
//                    };
//                    if(null!=getDataByCallable){
//                        Future<MwTangibleassetsTable> f = executorService.submit(getDataByCallable);
//                        futureList.add(f);
//                    }
//                }
//
//                for (Future<MwTangibleassetsTable> item : futureList) {
//                    try {
//                        MwTangibleassetsTable mt = item.get(30, TimeUnit.SECONDS);
//                        if(mt!=null){
//                            result.add(mt);
//                        }
//                    } catch (Exception e) {
//                        item.cancel(true);
//                        executorService.shutdown();
//                    }
//                }
//                executorService.shutdown();
//            }
//            if(result.size()>0){
//                result.forEach(s->assetIds.add(s.getAssetsId()));
//                result.forEach(s->ids.add(s.getId()));
//            }
//        }
//        mwTangibleassetsDTOS=result;
//        Map<String, Object> map = new HashMap<>();
//        map.put("assetsList", mwTangibleassetsDTOS);
//        map.put("assetIds", assetIds);
//        map.put("ids", ids);
//        return map;
//    }

    /**
     * 获取过滤后的资产数据
     * @param param 查询过滤数据参数
     */
    private  Map<Integer, List<String>> getAssetsFilterData(MWNewScreenAssetsFilterDto param){
        try {
            Map<Integer, List<String>> map = new HashMap<>();
            //根据用户ID，模块ID，卡片ID查询资产过滤数据
            MWNewScreenAssetsFilterDto mwNewScreenAssetsFilterDto = newScreenManageDao.selectNewScreenAssetsFilterData(param.getModelDataId(), param.getUserId(), param.getModelId());
            //根据ID查询标签
            List<MwRuleSelectParam> mwRuleSelectParams = new ArrayList<>();
            String assetsName = "";
            Integer assetsTypeId = null;
            Integer assetsTypeSubId = null;
            if(mwNewScreenAssetsFilterDto != null && mwNewScreenAssetsFilterDto.getId() != null){
                Integer id = mwNewScreenAssetsFilterDto.getId();
                mwRuleSelectParams = newScreenManageDao.selectMwAlertRuleSelect("NEW_HOME_" + id);
                assetsName = mwNewScreenAssetsFilterDto.getAssetsName();
                assetsTypeId = mwNewScreenAssetsFilterDto.getAssetsTypeId();
                assetsTypeSubId = mwNewScreenAssetsFilterDto.getAssetsTypeSubId();
            }
            QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
            assetsParam.setAssetsTypeId(assetsTypeId);
            assetsParam.setAssetsTypeSubId(assetsTypeSubId);
            assetsParam.setPageNumber(1);
            assetsParam.setPageSize(Integer.MAX_VALUE);
            assetsParam.setUserId(param.getUserId());
            assetsParam.setSkipDataPermission(true);
            assetsParam.setIsQueryAssetsState(false);
            //根据类型查询资产数据
            long startTime = System.currentTimeMillis();
            log.info("首页查询资产过滤"+startTime);
            List<MwTangibleassetsTable> mwTangibleassetsTables = mwAssetsManager.getAssetsTable(assetsParam);
            log.info("首页查询资产过滤2"+(System.currentTimeMillis() - startTime));
            if(CollectionUtils.isEmpty(mwTangibleassetsTables)){return map;}
            List<MWTangibleassetsDto> mwTangibleassetsDtos = new ArrayList<>();
            List<MWOrgDTO> mworgDtos = orgService.getAllOrgList();
            for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
                if(mwTangibleassetsTable.getMonitorFlag() == null || !mwTangibleassetsTable.getMonitorFlag()){continue;}
                MWTangibleassetsDto dto = new MWTangibleassetsDto();
                dto.setAssetsId(mwTangibleassetsTable.getAssetsId());
                dto.setAssetsName(mwTangibleassetsTable.getAssetsName());
                dto.setId(mwTangibleassetsTable.getId());
                dto.setMonitorServerId(mwTangibleassetsTable.getMonitorServerId());
                List<String> assetsOrgs = getAssetsOrgs(mwTangibleassetsTable, mworgDtos);
                dto.setOrgNames(assetsOrgs);
                mwTangibleassetsDtos.add(dto);
            }
            List<MWTangibleassetsDto> tangibleassetsDtos = handleAssetsLabelFilter(mwTangibleassetsDtos, mwRuleSelectParams);
            if(CollectionUtils.isEmpty(tangibleassetsDtos)){return map;}
            //获取当前登录用户的资产权限
            GlobalUserInfo globalUser = userService.getGlobalUser(param.getUserId());
            List<String> currLoginUserAssets = getAssetsPermission(globalUser);
            if(CollectionUtils.isEmpty(currLoginUserAssets) && !globalUser.isSystemUser()){return map;}
            for (MWTangibleassetsDto tangibleassetsDto : tangibleassetsDtos) {
                if(CollectionUtils.isNotEmpty(currLoginUserAssets) && !currLoginUserAssets.contains(tangibleassetsDto.getId())){continue;}
                String assetsId = tangibleassetsDto.getAssetsId();
                Integer monitorServerId = tangibleassetsDto.getMonitorServerId();
                if(monitorServerId == null || monitorServerId == 0 || StringUtils.isBlank(tangibleassetsDto.getAssetsId())){continue;}
                if(map.isEmpty() || CollectionUtils.isEmpty(map.get(monitorServerId))){
                    List<String> hostIds = new ArrayList<>();
                    hostIds.add(assetsId);
                    map.put(monitorServerId,hostIds);
                }else{
                    List<String> hostIds = map.get(monitorServerId);
                    hostIds.add(assetsId);
                    map.put(monitorServerId,hostIds);
                }
            }
            log.info("首页查询资产过滤3"+(System.currentTimeMillis() - startTime));
            return map;
        }catch (Exception e){
            log.error("查询过滤数据失败",e);
        }
        return null;
    }

    /**
     * 根据用户获取资产权限
     */
    private List<String> getAssetsPermission(GlobalUserInfo globalUser){
        boolean systemAssetsType = commonService.getSystemAssetsType();//获取是否模型资产
        if(systemAssetsType){
            return userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        }
        return userService.getAllTypeIdList(globalUser, DataType.ASSETS);
    }

    /**
     * 处理标签过滤数据
     * @param mwTangibleassetsDtos 资产数据
     * @param mwRuleSelectParams 标签数据
     */
    private  List<MWTangibleassetsDto> handleAssetsLabelFilter(List<MWTangibleassetsDto> mwTangibleassetsDtos,List<MwRuleSelectParam> mwRuleSelectParams) {
        if (CollectionUtils.isEmpty(mwRuleSelectParams)){return mwTangibleassetsDtos;}
        List<MWTangibleassetsDto> tangibleassetsDtos = new ArrayList<>();
        //查询到每个资产的标签
        for (MWTangibleassetsDto mwTangibleassetsDto : mwTangibleassetsDtos) {
            String id = mwTangibleassetsDto.getId();
            List<MwAssetsLabelDTO> labelBoard = mwLabelCommonServcie.getLabelBoard(id, DataType.ASSETS.getName());
            HashMap<String, Object> map = new HashMap<>();
            for (MwAssetsLabelDTO mwAssetsLabelDTO : labelBoard) {
                String inputFormat = mwAssetsLabelDTO.getInputFormat();
                if ("1".equals(inputFormat)) {
                    String labelName = mwAssetsLabelDTO.getProp();
                    String tagboard = mwAssetsLabelDTO.getTagboard();
                    map.put(labelName, tagboard);
                }
                if ("2".equals(inputFormat)) {
                    String labelName = mwAssetsLabelDTO.getProp();
                    Date dateTagboard = mwAssetsLabelDTO.getDateTagboard();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put(labelName, format.format(dateTagboard));
                }
                if ("3".equals(inputFormat)) {
                    String labelName = mwAssetsLabelDTO.getProp();
                    String dropValue = mwAssetsLabelDTO.getDropValue();
                    map.put(labelName, dropValue);
                }
            }
            MessageContext context = new MessageContext();
            context.setKey(map);
            map.put(AlertEnum.ASSETS.toString(), mwTangibleassetsDto.getAssetsName());
            map.put(AlertEnum.ASSETSTYPE.toString(), mwTangibleassetsDto.getTypeName());
            map.put(AlertEnum.ORG.toString(), mwTangibleassetsDto.getOrgNames());
            if (mwRuleSelectParams.size() > 2) {
                log.info("ruleSelectParams star");
                List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                for (MwRuleSelectParam s : mwRuleSelectParams) {
                    if (s.getParentKey().equals("root")) {
                        ruleSelectParams.add(s);
                    }
                }
                for (MwRuleSelectParam s : ruleSelectParams) {
                    s.setConstituentElements(getChild(s.getKey(), mwRuleSelectParams));
                }
                Boolean aBoolean = DelFilter.delFilter(ruleSelectParams, context, mwRuleSelectParams);
                if(aBoolean){
                    tangibleassetsDtos.add(mwTangibleassetsDto);
                }
            }
        }
        return tangibleassetsDtos;
    }


    /**
     * 获取资产机构
     * @param tangTable
     * @param mworgDtos
     * @return
     */
    private List<String> getAssetsOrgs(MwTangibleassetsTable tangTable,List<MWOrgDTO> mworgDtos){
        List<String> orgNames = new ArrayList<>();
        List<List<Integer>> modelViewOrgIds = tangTable.getModelViewOrgIds();
        if(CollectionUtils.isNotEmpty(modelViewOrgIds) && CollectionUtils.isNotEmpty(mworgDtos)){
            List<Integer> orgIds = new ArrayList<>();
            for(List<Integer> mo : modelViewOrgIds){
                orgIds.addAll(mo);
            }
            List<OrgDTO> orgDTOS = orgIds.parallelStream().map(mwOrgDTO ->{
                MWOrgDTO dto = mworgDtos.stream().filter(u -> u.getOrgId().equals(mwOrgDTO)).findFirst().orElse(null);
                OrgDTO reOrgDto = new OrgDTO();
                if(null != dto){
                    BeanUtils.copyProperties(dto,reOrgDto);
                }
                return reOrgDto;
            }).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(orgDTOS)){
                orgNames = orgDTOS.stream().filter(item->StringUtils.isNotBlank(item.getOrgName())).map(OrgDTO::getOrgName).collect(Collectors.toList());
            }
        }
        return orgNames;
    }

    private static List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList){
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for(MwRuleSelectParam s : rootList){
            if(s.getParentKey().equals(key)){
                childList.add(s);
            }
        }
        for(MwRuleSelectParam s : childList){
            s.setConstituentElements(getChild(s.getKey(),rootList));
        }
        if(childList.size() == 0){
            return null;
        }
        return childList;

    }


    @Resource
    private MwLabelManageTableDao mwLabelmanageTableDao;

    @Override
    public Reply newScreenLabelDrop() {
        //查询所有标签
        QueryLabelParam queryLabelParam = new QueryLabelParam();
        List<MwAllLabelDTO> allLabel = mwLabelmanageTableDao.selectLabelBrowse(queryLabelParam);
        if(CollectionUtils.isNotEmpty(allLabel)){
            for (MwAllLabelDTO mwAllLabelDTO : allLabel) {
                String inputFormat = mwAllLabelDTO.getInputFormat();
                if("3".equals(inputFormat)){
                    String prop = mwAllLabelDTO.getProp();
                    List<String> strings = newScreenManageDao.selectLabelValue(prop);
                    mwAllLabelDTO.setLabelValue(strings);
                }
            }
        }
        return Reply.ok(allLabel);
    }


    public List<MwRuleSelectParam> delMwRuleSelectList(MwRuleSelectParam param,Integer id){
        List<MwRuleSelectParam> paramList = new ArrayList<>();
        for (MwRuleSelectParam s : param.getConstituentElements()){
            MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
            ruleSelectDto.setCondition(s.getCondition());
            ruleSelectDto.setDeep(s.getDeep());
            ruleSelectDto.setKey(s.getKey());
            ruleSelectDto.setName(s.getName());
            ruleSelectDto.setParentKey(s.getParentKey());
            ruleSelectDto.setRelation(s.getRelation());
            ruleSelectDto.setValue(s.getValue());
            ruleSelectDto.setUuid(param.getUuid());
            paramList.add(ruleSelectDto);
            s.setUuid("NEW_HOME_"+id);
            if(s.getConstituentElements() != null && s.getConstituentElements().size() > 0){
                List<MwRuleSelectParam> temps = delMwRuleSelectList(s,id);
                paramList.addAll(temps);
            }
        }
        return paramList;
    }

    @Override
    public Reply getNewScreenActivityAlertCount(MWNewScreenAssetsCensusParam param) {
        try {
            Map<String,Integer> data = new HashMap<>();
            AlertParam alertParam = new AlertParam();
            Integer dateType = param.getDateType();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(dateType != null && dateType == 1){//今天时间
                List<Date> today = MWNewScreenDateUtil.getToday();
                alertParam.setStartTime(format.format(today.get(0)));
                alertParam.setEndTime(format.format(today.get(1)));
            }
            if(dateType != null && dateType == 0){//自定义时间
                alertParam.setStartTime(param.getStartTime());
                alertParam.setEndTime(param.getEndTime());
            }
            alertParam.setPageSize(Integer.MAX_VALUE);
            List<String> hostIds = alertHandler.handler();
            if(CollectionUtils.isNotEmpty(hostIds)){
                alertParam.setQueryHostIds(hostIds);
            }
            Reply reply = mwalertService.getCurrAlertPage(alertParam);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS && reply.getData() != null){
                PageInfo pageInfo = (PageInfo) reply.getData();
                List<ZbxAlertDto> list = pageInfo.getList();
                if(CollectionUtils.isNotEmpty(list)){
                    for (ZbxAlertDto zbxAlertDto : list) {
                        String severity = zbxAlertDto.getSeverity();
                        if(data.containsKey(severity)){
                            Integer count = data.get(severity);
                            data.put(severity,count+1);
                        }else{
                            data.put(severity,1);
                        }
                    }
                }
                data.put(ScreenConstant.ALL,list.size());
            }
            log.info("首页告警分类统计"+data);
            List<ActivityAlertClassifyDto> list = new ArrayList<>();
            ConcurrentHashMap<String, String> alertLevelMap = MWAlertLevelParam.alertLevelMap;
            log.info("首页告警分类统计告警分类"+alertLevelMap);
            for (String key : alertLevelMap.keySet()) {
                String level = alertLevelMap.get(key);
                ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
                Integer num = data.get(level);
                if(Integer.parseInt(key) == 0){continue;}
                dto.setValue(Integer.parseInt(key));
                dto.setLabel(level);
                dto.setNum(num==null?0:num);
                list.add(dto);
            }
            ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
            Integer allNum = data.get(ScreenConstant.ALL);
            dto.setValue(0);
            dto.setLabel(ScreenConstant.ALL);
            dto.setNum(allNum==null?0:allNum);
            list.add(dto);
            if(CollectionUtils.isNotEmpty(list)){
                Collections.sort(list, new Comparator<ActivityAlertClassifyDto>() {
                    @Override
                    public int compare(ActivityAlertClassifyDto o1, ActivityAlertClassifyDto o2) {
                        if(o1.getValue() > o2.getValue()){
                            return 1;
                        }
                        if(o1.getValue() < o2.getValue()){
                            return -1;
                        }
                        return 0;
                    }
                });
            }
            Map<String,List<ActivityAlertClassifyDto>> map = new HashMap<>();
            map.put("warnList",list);
            return Reply.ok(map);
        }catch (Exception e){
            log.error("查询新首页活动告警分类数据失败",e);
            return Reply.fail("查询新首页活动告警分类数据失败");
        }
    }

    @Override
    public Reply getAlertCount(MWAlertCountParam param) {
        try {

            //获取日期
            List<Map.Entry<String,Integer>> result = new ArrayList<Map.Entry<String,Integer>>();
            int dateType = param.getDateType();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String startTime = null;
            String endTime = null;
            switch (dateType){
                case 1://本周
                    List<Date> week = MWNewScreenDateUtil.getWeek();
                    startTime = format.format(week.get(0));
                    endTime = format.format(week.get(1));
                    break;
                case 2://本月
                    List<Date> month = MWNewScreenDateUtil.getMonth();
                    startTime = format.format(month.get(0));
                    endTime = format.format(month.get(1));
                    break;
                case 3://上月
                    List<Date> lastMonth = MWNewScreenDateUtil.getLastMonth();
                    startTime = format.format(lastMonth.get(0));
                    endTime = format.format(lastMonth.get(1));
                    break;
                case 0:
                    if(!param.getStartTime().equals("")){
                        startTime = param.getStartTime().substring(0,10);
                    }
                    if(!param.getEndTime().equals("")){
                        endTime = param.getEndTime().substring(0,10);
                    }
                    break;
                default:
                    break;
            }
            log.info("日期类型:" + dateType);
            log.info("开始日期：" + startTime);
            log.info("结束日期：" + endTime);
            Map<String,Integer> data = new HashMap<>();
            AlertParam alertParam = new AlertParam();
            alertParam.setPageSize(1000000000);
            alertParam.setPageNumber(0);
            alertParam.setStartTime(startTime);
            alertParam.setEndTime(endTime);
            Reply reply = mwalertService.getCurrAlertPage(alertParam);
            MWNewScreenTopNDto dto = new MWNewScreenTopNDto();
            List<String> titleNode = new ArrayList<>();
            List<Map<String,Object>> title = new ArrayList<>();
            Map<String,Object> mapTitle = new HashMap<>();
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS && reply.getData() != null){
                PageInfo pageInfo = (PageInfo) reply.getData();
                List<ZbxAlertDto> list = pageInfo.getList();
                if(CollectionUtils.isNotEmpty(list)){
                    int alertType = param.getModelId();
                    mapTitle.put("prop","name");
                    switch(alertType){
                        case 10://按标题分类统计次数
                            titleNode.add("告警标题");
                            mapTitle.put("name","告警标题");
                            for (ZbxAlertDto s: list){
                                Integer count = data.get(s.getName());
                                data.put(s.getName(),(count == null) ? 1 : count + 1);
                            }
                            break;
                        case 11://按资产分类
                            titleNode.add("告警对象");
                            mapTitle.put("name","告警对象");
                            for (ZbxAlertDto s: list){
                                Integer count = data.get(s.getObjectName());
                                data.put(s.getObjectName(),(count == null) ? 1 : count + 1);
                            }
                            break;
                        case 12://按告警级别分类
                            titleNode.add("告警级别");
                            mapTitle.put("name","告警级别");
                            for (ZbxAlertDto s: list){
                                Integer count = data.get(s.getSeverity());
                                data.put(s.getSeverity(),(count == null) ? 1 : count + 1);
                            }
                            break;
                        default:
                            break;
                    }
                    titleNode.add("告警次数");
                    title.add(mapTitle);
                    mapTitle = new HashMap<>();
                    mapTitle.put("prop","lastValue");
                    mapTitle.put("name","告警次数");
                    title.add(mapTitle);
                    dto.setTitleNode(titleNode);
                    dto.setTitle(title);
                    result = new ArrayList<Map.Entry<String,Integer>>(data.entrySet());
                    Collections.sort(result, new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            return o2.getValue().compareTo(o1.getValue());
                        }
                    });
                }
            }
            if(result.size() > param.getMwRankCount()){
                result = result.subList(0,param.getMwRankCount());
            }
            List<ItemNameRank> rank = new ArrayList<>();
            for(Map.Entry<String,Integer> rsc : result){
                ItemNameRank temp = new ItemNameRank();
                temp.setName(rsc.getKey());
                temp.setLastValue(rsc.getValue().doubleValue());
                temp.setAlertType(param.getModelId());
                rank.add(temp);
            }
            dto.setItemNameRankList(rank);
            return Reply.ok(dto);
        }catch (Exception e){
            log.error("告警统计次数失败",e);
            return Reply.fail("告警统计次数失败");
        }

    }


    /**
     * 修改首页模块
     * @param moduleDto
     * @return
     */
    @Override
    public Reply updateNewHomeModule(MWNewScreenModuleDto moduleDto) {
        int count = newScreenManageDao.updateNewHomeModule(moduleDto);
        if(count > 0){return Reply.ok(moduleDto);}
        return Reply.ok("修改失败");
    }


    /**
     * 获取当前登录用户所能看到的资产
     */
    private List<String> getCurrLoginUserAssets(){
        //获取当前登录用户信息
        GlobalUserInfo globalUser = userService.getGlobalUser();
        boolean systemAssetsType = commonService.getSystemAssetsType();//获取是否模型资产
        if(systemAssetsType){
            return userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        }
        return userService.getAllTypeIdList(globalUser, DataType.ASSETS);
    }

    /**
     * 进行流量错误包数据过滤，将不满足条件的数据删除
     * @param itemNameRankList
     * @param filterAssetsParam
     */
    private void flowErrorDataFilter(List<ItemNameRank> itemNameRankList,MWNewScreenAssetsFilterDto filterAssetsParam){
        Map<Integer, List<String>> map = getAssetsFilterData(filterAssetsParam);
        if(CollectionUtils.isEmpty(itemNameRankList) || map == null){return;}
        Iterator<ItemNameRank> iterator = itemNameRankList.iterator();
        while(iterator.hasNext()){
            ItemNameRank rank = iterator.next();
            Integer monitorServerId = rank.getMonitorServerId();
            String assetsId = rank.getAssetsId();
            if(monitorServerId == null || StringUtils.isBlank(assetsId)){continue;}
            List<String> assetsIds = map.get(monitorServerId);
            if(CollectionUtils.isEmpty(assetsIds) || !assetsIds.contains(assetsId)){
                iterator.remove();
            }
        }
    }

    @Autowired
    private MWUserCommonService mwUserCommonService;

    /**
     * 同步首页卡片信息
     * @return
     */
    @Override
    public Reply syncNewScreenCardInfo() {
        try {
            //获取当前登录用户
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            //根据当前登录用户查询该用户模块
            List<MWNewScreenModuleDto> moduleDtos = newScreenManageDao.selectUserNewScreenModule(userId);
            if(CollectionUtils.isEmpty(moduleDtos)){return Reply.fail("登录用户首页组件信息为空");}
            //同步所有用户
            List<Integer> allUserIds = mwUserCommonService.selectAllUserId();
            GlobalUserInfo globalUser = userService.getGlobalUser(userId);
            if(!globalUser.isSystemUser()){return Reply.fail("非管理员用户不能同步信息");}
            List<MWNewScreenModuleDto> allUserModuleDtos = new ArrayList<>();
            //查询用户过滤信息
            List<MWNewScreenAssetsFilterDto> assetsFilterDtos = newScreenManageDao.selectNewScreenAssetsFilterByUserId(userId);
            List<MWNewScreenAssetsFilterDto> allUserAssetsFilterDtos = new ArrayList<>();
            //设置需要添加的数据
            for (Integer allUserId : allUserIds) {
                if(userId.equals(allUserId)){continue;}
                //设置需要添加的数据
                for (MWNewScreenModuleDto moduleDto : moduleDtos) {
                    MWNewScreenModuleDto screenModuleDto = new MWNewScreenModuleDto();
                    BeansUtils.copyProperties(moduleDto,screenModuleDto);
                    screenModuleDto.setUserId(allUserId);
                    allUserModuleDtos.add(screenModuleDto);
                }
                if(CollectionUtils.isEmpty(assetsFilterDtos)){continue;}
                for (MWNewScreenAssetsFilterDto assetsFilterDto : assetsFilterDtos) {
                    MWNewScreenAssetsFilterDto screenAssetsFilterDto = new MWNewScreenAssetsFilterDto();
                    BeansUtils.copyProperties(assetsFilterDto,screenAssetsFilterDto);
                    screenAssetsFilterDto.setUserId(allUserId);
                    allUserAssetsFilterDtos.add(screenAssetsFilterDto);
                }
            }
            //先删除当前登录用户之外的卡片数据和过滤数据再添加新数据
            if(CollectionUtils.isNotEmpty(allUserModuleDtos)){
                newScreenManageDao.deleteModuleInfo(userId);
                newScreenManageDao.insertNewScreenUserModule(allUserModuleDtos);
            }
            if(CollectionUtils.isNotEmpty(allUserAssetsFilterDtos)){
                newScreenManageDao.deleteScreenFilterInfo(userId);
                newScreenManageDao.batchInsertNewScreenAssetsFilter(allUserAssetsFilterDtos);
            }
            return Reply.ok("同步成功");
        }catch (Throwable e){
            log.error("MWNewScreenManageImpl{} ::syncNewScreenCardInfo()",e);
            return Reply.fail("MWNewScreenManageImpl{} ::syncNewScreenCardInfo()"+e.getMessage());
        }
    }

    @Override
    public Reply getInterfaceRate(MWNewScreenAlertDevOpsEventDto param) {
       try {
           Integer userId = param.getUserId();
           Integer modelId = param.getModelId();
           String modelDataId = param.getModelDataId();
           String key = genRedisKey(ScreenConstant.HOME_PAGE_KEY, ScreenConstant.INTERFACE_RATE_KEY);
           String redislist = redisTemplate.opsForValue().get(key);
           MWNewScreenAssetsFilterDto filterAssetsParam = MWNewScreenAssetsFilterDto.builder().modelId(modelId == null?0:modelId).modelDataId(modelDataId).userId(userId).build();
           MWNewScreenTopNDto dto = new MWNewScreenTopNDto();
           if (StringUtils.isNotBlank(redislist)  && (param.getIsCache() == null || !param.getIsCache())) {
               dto = JSONObject.parseObject(redislist, MWNewScreenTopNDto.class);
               if(dto != null){
                   List<ItemNameRank> itemNameRankList = dto.getItemNameRankList();
                   flowErrorDataFilter(itemNameRankList,filterAssetsParam);
                   if(CollectionUtils.isNotEmpty(itemNameRankList) && itemNameRankList.size() > param.getMwRankCount()){
                       dto.setItemNameRankList(itemNameRankList.subList(0,param.getMwRankCount()));
                   }
               }
           }
           if(!schedulingEnabled){
               return Reply.ok(dto);
           }
           Map<Integer, List<String>> map = getAssetsFilterData(filterAssetsParam);
           ItemRank itemRank = mwModelManage.getScreenFlowNews(ScreenConstant.INTERFACE_RATE_NAMES, null, map,param.getMwRankCount());
           dto.setItemNameRankList(itemRank.getItemNameRankList());
           dto.setTitleNode(itemRank.getTitleNode());
           dto.setTitleRanks(itemRank.getTitleRanks());
           dataChange(dto,ScreenConstant.INTERFACE_OUT_UTILIZATION);
           return Reply.ok(dto);
       }catch (Throwable e){
            log.error("MWNewScreenManageImpl{} getInterfaceRate() error",e);
            return Reply.fail("查询接口丢包率错误");
       }
    }
}
