package cn.mw.monitor.screen.timer;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.constant.ScreenConstant;
import cn.mw.monitor.screen.dto.*;
import cn.mw.monitor.screen.service.MWNewScreenManage;
import cn.mw.monitor.service.assets.model.AssetTypeIconDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MwNewScreenCacheTime
 * @Description 首页TopN数据缓存
 * @Author gengjb
 * @Date 2023/3/26 23:26
 * @Version 1.0
 **/
@Component
@Slf4j(topic = "timerController")
public class MwNewScreenCacheTime {

    @Autowired
    private MWNewScreenManage newScreenManage;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Value("${screen.timetask}")
    private boolean isExecuteTimeTask;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWCommonService commonService;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    /**
     * 获取TopN数据
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult cacheTopNData(){
        log.info("MwNewScreenCacheTime{} cacheTopNData>>>>>>start");
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try {
            if(!isExecuteTimeTask){return null;}
            Map<String,MWNewScreenTopNDto> topNDtoMap = new HashMap<>();
            for (String itemName : ScreenConstant.TOPN_ITEM) {
                MWNewScreenAlertDevOpsEventDto devOpsEventDto = new MWNewScreenAlertDevOpsEventDto();
                devOpsEventDto.setName(itemName);
                devOpsEventDto.setUserId(Constants.SYSTEM_ADMIN);
                devOpsEventDto.setIsCache(true);
                ItemRank itemRank = new ItemRank();
                Reply reply = newScreenManage.getNewScreenAssetsTopN(devOpsEventDto);
                if(reply == null && reply.getRes() != PaasConstant.RES_SUCCESS){continue;}
                MWNewScreenTopNDto topNDto = (MWNewScreenTopNDto) reply.getData();
                topNDtoMap.put(itemName,topNDto);
            }
            if(topNDtoMap == null || topNDtoMap.isEmpty()){return null;}
            for (String itemName : topNDtoMap.keySet()) {
                MWNewScreenTopNDto topNDto = topNDtoMap.get(itemName);
                if(topNDto == null || topNDto.getItemNameRankList() == null){continue;}
                //数据排序
                valueSort(topNDto.getItemNameRankList());
                log.info("MwNewScreenCacheTime{} cacheTopNData::"+itemName+"::"+topNDto.getItemNameRankList().size());
                //将数据存入redis
                //先删除原来的缓存
                redisTemplate.delete("homePage" + itemName);
                redisTemplate.opsForValue().set("homePage"+itemName, JSONObject.toJSONString(topNDto), 60, TimeUnit.MINUTES);
                log.info("缓存首页TopN数据成功");

            }
            log.info("MwNewScreenCacheTime{} cacheTopNData>>>>>>end");
            //进行数据添加
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("缓存首页TopN数据成功");
        }catch (Throwable e){
            log.error("MwNewScreenCacheTime{} cacheTopNData::",e);
            //进行数据添加
            taskRresult.setSuccess(false);
            taskRresult.setResultType(0);
            taskRresult.setFailReason(e.getMessage());
            taskRresult.setResultContext("缓存首页TopN数据失败");
        }
        return taskRresult;
    }

    /**
     * 缓存接口流量数据
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult cacheInterfaceFlowData(){
        if(!isExecuteTimeTask){return null;}
        MWNewScreenAlertDevOpsEventDto devOpsEventDto = new MWNewScreenAlertDevOpsEventDto();
        devOpsEventDto.setUserId(Constants.SYSTEM_ADMIN);
        devOpsEventDto.setIsCache(true);
        Reply reply = newScreenManage.getNewScreenLinkTopN(devOpsEventDto);
        if(reply == null && reply.getRes() != PaasConstant.RES_SUCCESS){return null;}
        MWNewScreenTopNDto dto = (MWNewScreenTopNDto) reply.getData();
        //先删除原来的缓存
        redisTemplate.delete("homePage" + ScreenConstant.INTERFACE_FLOW_KEY);
        redisTemplate.opsForValue().set("homePage"+ScreenConstant.INTERFACE_FLOW_KEY, JSONObject.toJSONString(dto), 60, TimeUnit.MINUTES);
        log.info("缓存首页流量数据成功");
        //进行数据添加
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        taskRresult.setSuccess(true);
        taskRresult.setResultType(0);
        taskRresult.setResultContext("缓存首页流量数据成功");
        return taskRresult;
    }



    /**
     * 缓存首页带宽数据
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult cacheFlowBandWidthData(){
        try {
            if(!isExecuteTimeTask){return null;}
            MWNewScreenAlertDevOpsEventDto devOpsEventDto = new MWNewScreenAlertDevOpsEventDto();
            devOpsEventDto.setUserId(Constants.SYSTEM_ADMIN);
            devOpsEventDto.setIsCache(true);
            Reply reply = newScreenManage.getHomePageFlowBandWidthTopN(devOpsEventDto);
            if(reply == null && reply.getRes() != PaasConstant.RES_SUCCESS){return null;}
            MWNewScreenTopNDto dto = (MWNewScreenTopNDto) reply.getData();
            //先删除原来的缓存
            redisTemplate.delete("homePage" + ScreenConstant.FLOW_BANDWIDTH_KEY);
            redisTemplate.opsForValue().set("homePage"+ScreenConstant.FLOW_BANDWIDTH_KEY, JSONObject.toJSONString(dto), 60, TimeUnit.MINUTES);
            log.info("缓存首页带宽数据成功");
            //进行数据添加
            TimeTaskRresult taskRresult = new TimeTaskRresult();
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("缓存首页带宽数据成功");
            return taskRresult;
        }catch (Throwable e){
            log.error("缓存首页带宽数据失败",e);
            return null;
        }
    }



//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult cacheNewScreenAssetsGroup(){
        try {
            if(!isExecuteTimeTask){return null;}
            List<MwScreenAssetsDto> screenAssetsDtos = new ArrayList<>();
            QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
            assetsParam.setPageNumber(1);
            assetsParam.setPageSize(Integer.MAX_VALUE);
            assetsParam.setIsQueryAssetsState(true);
            assetsParam.setUserId(Constants.SYSTEM_ADMIN);
            //根据类型查询资产数据
            List<MwTangibleassetsTable> mwTangibleassetsTables = mwAssetsManager.getAssetsTable(assetsParam);
            Map<Integer , AssetTypeIconDTO> typeIconDTOMap = mwModelViewCommonService.selectAllAssetsTypeIcon();
            if(CollectionUtils.isNotEmpty(mwTangibleassetsTables)){
                for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables){
                    MwScreenAssetsDto screenAssetsDto = new MwScreenAssetsDto();
                    screenAssetsDto.extractFrom(mwTangibleassetsTable,typeIconDTOMap,commonService.getSystemAssetsType());
                    screenAssetsDtos.add(screenAssetsDto);
                }
            }
            //先删除原来的缓存
            redisTemplate.delete("homePage" + ScreenConstant.ASSETS_GROUP);
            log.info("MwNewScreenCacheTime{} cacheNewScreenAssetsGroup::"+screenAssetsDtos.size());
            redisTemplate.opsForValue().set("homePage"+ScreenConstant.ASSETS_GROUP, JSONObject.toJSONString(screenAssetsDtos), 10, TimeUnit.MINUTES);
            //进行数据添加
            TimeTaskRresult taskRresult = new TimeTaskRresult();
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("缓存首页资产分组数据成功");
            return taskRresult;
        }catch (Throwable e){
            log.error("缓存首页资产分组数据失败",e);
            return null;
        }
    }

    /**
     * 缓存接口丢包率数据
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult cacheInterfaceRateData(){
        try {
            MWNewScreenAlertDevOpsEventDto devOpsEventDto = new MWNewScreenAlertDevOpsEventDto();
            devOpsEventDto.setUserId(Constants.SYSTEM_ADMIN);
            devOpsEventDto.setIsCache(true);
            Reply reply = newScreenManage.getInterfaceRate(devOpsEventDto);
            if(reply == null && reply.getRes() != PaasConstant.RES_SUCCESS){return null;}
            MWNewScreenTopNDto dto = (MWNewScreenTopNDto) reply.getData();
            //先删除原来的缓存
            redisTemplate.delete("homePage" + ScreenConstant.INTERFACE_RATE_KEY);
            redisTemplate.opsForValue().set("homePage"+ScreenConstant.INTERFACE_RATE_KEY, JSONObject.toJSONString(dto), 10, TimeUnit.MINUTES);
            log.info("缓存首页接口丢包率数据成功");
            //进行数据添加
            TimeTaskRresult taskRresult = new TimeTaskRresult();
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("缓存首页接口丢包率数据成功");
            return taskRresult;
        }catch (Throwable e){
            log.error("缓存首页接口丢包率数据失败",e);
            return null;
        }
    }

    private void valueSort(List<ItemNameRank> itemNameRankList){
        for (ItemNameRank rank : itemNameRankList) {
            if(rank.getSortlastValue() == null){
                rank.setSortlastValue(0.0);
            }
        }
        Collections.sort(itemNameRankList, new Comparator<ItemNameRank>() {
            @Override
            public int compare(ItemNameRank o1, ItemNameRank o2) {
                if(o1.getSortlastValue() > o2.getSortlastValue()){
                    return -1;
                }
                if(o1.getSortlastValue() < o2.getSortlastValue()){
                    return 1;
                }
                return 0;
            }
        });
    }
}
