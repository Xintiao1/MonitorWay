package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.report.dto.TrendDiskDto;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DiskUseHandler implements ReportHandler<PageInfo> {
    private TrendParam trendParam;
    private CalculitionTimeCallBack calculitionTimeCallBack;
    private List<MwTangibleassetsTable> mwTangibleassetsTables;
    private RedisUtils redisUtils;
    private long diskUseCacheTime;
    private DiskUseReportCacheDataFunc diskUseReportCacheDataFunc;
    private DiskNewsCallBack diskNewsCallBack;

    public DiskUseHandler(TrendParam trendParam, CalculitionTimeCallBack calculitionTimeCallBack
            , List<MwTangibleassetsTable> mwTangibleassetsTables, RedisUtils redisUtils, long diskUseCacheTime
            , DiskUseReportCacheDataFunc diskUseReportCacheDataFunc
            ,DiskNewsCallBack diskNewsCallBack
    ){
        this.trendParam = trendParam;
        this.calculitionTimeCallBack = calculitionTimeCallBack;
        this.mwTangibleassetsTables = mwTangibleassetsTables;
        this.redisUtils = redisUtils;
        this.diskUseCacheTime = diskUseCacheTime;
        this.diskUseReportCacheDataFunc = diskUseReportCacheDataFunc;
        this.diskNewsCallBack = diskNewsCallBack;
    }

    @Override
    public PageInfo handle() {
        Integer pageNumber = trendParam.getPageNumber();
        Integer pageSize = trendParam.getPageSize();
        List<String> chooseTime = new ArrayList<>();
        if(null != trendParam.getChooseTime() && trendParam.getChooseTime().size() > 0){
            chooseTime.addAll(trendParam.getChooseTime());
        }
        List<TrendDiskDto> diskTrend = null;

        //根据时间判断取数来源
        //1.从数据库查询
        DateTypeEnum dateType = DateTypeEnum.getDateTypeEnum(trendParam.getDateType());
        if(null != dateType) {
            log.info("DiskUseHandler dateType", dateType);
            if(dateType != DateTypeEnum.TODAY && (trendParam.getTimingType() == null || !trendParam.getTimingType())){//非今日条件查询数据库
                PageInfo retData = diskUseReportCacheDataFunc.getDiskUseReportCacheData(dateType, trendParam);
                chooseTime.clear();
                //只返回执行正常的数据
                if (null != retData && null != retData.getList() && retData.getList().size() > 0) {
                    TrendDiskDto trendDiskDto = ((List<TrendDiskDto>) retData.getList()).get(0);
                    if (trendDiskDto.isUpdateSuccess()) {
                        log.info("return data from db size:{}", retData.getList().size());
                        List<TrendDiskDto> diskDtos = retData.getList();
                        if(!CollectionUtils.isEmpty(diskDtos)){
                            Collections.sort(diskDtos, new Comparator<TrendDiskDto>() {
                                @Override
                                public int compare(TrendDiskDto o1, TrendDiskDto o2) {
                                    return (o1.getAssetsName()+o1.getTypeName()).compareTo(o2.getAssetsName()+o2.getTypeName());
                                }
                            });
                        }
                        return retData;
                    }
                }
            }
//            switch (dateType) {
//                case YESTERDAY://昨天从数据库进行取数
//                case LAST_WEEK://上周从数据库进行取数
//                case LAST_MONTH:
//                    PageInfo retData = diskUseReportCacheDataFunc.getDiskUseReportCacheData(dateType, trendParam);
//                    chooseTime.clear();
//                    //只返回执行正常的数据
//                    if (null != retData && null != retData.getList() && retData.getList().size() > 0) {
//                        TrendDiskDto trendDiskDto = ((List<TrendDiskDto>) retData.getList()).get(0);
//                        if (trendDiskDto.isUpdateSuccess()) {
//                            log.info("return data from db size:{}", retData.getList().size());
//                            return retData;
//                        }
//                    }
//            }
        }

        List<Long> times = calculitionTimeCallBack.calculitionTime(dateType, chooseTime);

        Long startTime = times.get(0);
        Long endTime = times.get(1);
        List<String> chooseTimes = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        date.setTime(startTime*1000);
        chooseTimes.add(format.format(date));
        Date date2 = new Date();
        date2.setTime(endTime*1000);
        chooseTimes.add(format.format(date2));
        trendParam.setParticle(null);
        trendParam.setDayType(DateTypeEnum.SELF_DEFINE.getType());
        trendParam.setChooseTime(chooseTimes);
        trendParam.setMwTangibleassetsDTOS(mwTangibleassetsTables);
        List<String> assetIds = new ArrayList<>();
        mwTangibleassetsTables.forEach(assets -> {
            assetIds.add(assets.getAssetsId());
        });

        //避免分页时重新查询,先把数据临时存入redis
        //2.从redis中查询
        String key = null;
        try {
            key = trendParam.getRedisKey();
            diskTrend = (List) redisUtils.get(key);
        }catch (Exception e){
            log.error("redisUtils", e);
        }
        if(null == diskTrend) {
            diskTrend = diskNewsCallBack.getDiskNews(trendParam);
        }

        //3.从zabbix查询
        if(!CollectionUtils.isEmpty(diskTrend)){
            if(null != key){
                redisUtils.set(key, diskTrend, diskUseCacheTime);
            }
        }
        //根据资产过滤数据
        if(!CollectionUtils.isEmpty(trendParam.getIds()) && !CollectionUtils.isEmpty(diskTrend)){
            List<String> ids = trendParam.getIds();
            Iterator<TrendDiskDto> iterator = diskTrend.iterator();
            while(iterator.hasNext()){
                TrendDiskDto next = iterator.next();
                if(!ids.contains(next.getAssetsId())){
                    iterator.remove();
                }
            }

        }
        //排序
        List<TrendDiskDto> dtos = new ArrayList<>();
        if(diskTrend != null && diskTrend.size() > 0){
            Collections.sort(diskTrend, new Comparator<TrendDiskDto>() {
                @Override
                public int compare(TrendDiskDto o1, TrendDiskDto o2) {
                    return (o1.getAssetsName()+o1.getTypeName()).compareTo(o2.getAssetsName()+o2.getTypeName());
                }
            });
        }
        dtos = diskTrend;
        if(trendParam.getTimingType() != null && trendParam.getTimingType()){
            PageInfo pageInfo = new PageInfo<>(dtos);
            pageInfo.setTotal(dtos.size());
            pageInfo.setList(dtos);
            return pageInfo;
        }
        int fromIndex = pageSize * (pageNumber -1);
        int toIndex = pageSize * pageNumber;
        if(toIndex > dtos.size()){
            toIndex = dtos.size();
        }
        List<TrendDiskDto> diskDtos = dtos.subList(fromIndex, toIndex);
        String dateRegion = MWReportHandlerDataLogic.getDateRegion(trendParam.getDateType(), trendParam.getChooseTime());
        if(!CollectionUtils.isEmpty(diskDtos) && StringUtils.isNotBlank(dateRegion)){
            diskDtos.forEach(data->{
                data.setTime(dateRegion);
            });
        }
        PageInfo pageInfo = new PageInfo<>(diskDtos);
        pageInfo.setTotal(dtos.size());
        pageInfo.setList(diskDtos);
        log.info("return data from zabbix size:{}",diskDtos.size());
        return pageInfo;
    }

}
