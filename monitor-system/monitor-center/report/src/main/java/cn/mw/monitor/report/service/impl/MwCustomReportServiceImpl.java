package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.MwCustomReportDto;
import cn.mw.monitor.report.dto.MwCustomReportIndexDto;
import cn.mw.monitor.report.dto.MwReportIndexDto;
import cn.mw.monitor.report.dto.MwReportTrendCacheDto;
import cn.mw.monitor.report.param.MwCustomReportParam;
import cn.mw.monitor.report.service.MwCustomReportService;
import cn.mw.monitor.report.util.MwReportDateUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 自定义指标报表数据查询
 * @date 2023/10/12 16:21
 */
@Service
@Slf4j
public class MwCustomReportServiceImpl implements MwCustomReportService {

    @Resource
    private MwReportDao reportDao;

    @Autowired
    private MwAssetsManager assetsManager;

    @Autowired
    private MWUserCommonService userService;

    @Override
    public Reply getCustomReportInfo(MwCustomReportParam reportParam) {
        try {
            Map<String, String> indexMap = indexGroup();
            //判断是否是查询最新数据
            if(reportParam.getIsLatestData()){
                List<MwCustomReportDto> customReportDtos = getIndexLatestData(reportParam, indexMap);
                PageInfo pageInfo = handlerDataPaging(customReportDtos, reportParam.getPageNumber(), reportParam.getPageSize());
                return Reply.ok(pageInfo);
            }
            //查询历史数据
            //获取查询的开始与结束时间
            List<Long> times = getQueryTime(reportParam);
            //查询历史数据
            List<MwCustomReportDto> historyData = getIndexHistoryData(reportParam, indexMap, times);
            PageInfo pageInfo = handlerDataPaging(historyData, reportParam.getPageNumber(), reportParam.getPageSize());
            return Reply.ok(pageInfo);
        }catch (Throwable e){
            log.error("MwCustomReportServiceImpl{} getCustomReportInfo() ERROR:",e);
            return Reply.fail("MwCustomReportServiceImpl{} getCustomReportInfo() ERROR:",e);
        }
    }

    /**
     * 获取历史数据的查询开始与结束时间
     * @param reportParam
     * @return
     */
    private List<Long> getQueryTime(MwCustomReportParam reportParam){
        List<String> chooseTime = new ArrayList<>();
        if(StringUtils.isNotBlank(reportParam.getStartTime()) && StringUtils.isNotBlank(reportParam.getEndTime())){
            chooseTime.add(reportParam.getStartTime());
            chooseTime.add(reportParam.getEndTime());
        }
        List<Long> times = MwReportDateUtil.calculitionTime(reportParam.getDateType(), chooseTime);
        log.info("MwCustomReportServiceImpl{} getQueryTime() times::"+times);
        return times;
    }

    /**
     * 处理分页数据
     */
    private PageInfo handlerDataPaging(List<MwCustomReportDto> customReportDtos,Integer pageNumber,Integer pageSize){
        if(CollectionUtils.isNotEmpty(customReportDtos)){
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > customReportDtos.size()){
                toIndex = customReportDtos.size();
            }
            List<MwCustomReportDto> mwCustomReportDtos = customReportDtos.subList(fromIndex, toIndex);
            List<String> assetsIds = mwCustomReportDtos.stream().map(MwCustomReportDto::getAssetsId).distinct().collect(Collectors.toList());
            Map<String, String> assetsBusinessSystem = getAssetsBusinessSystem(assetsIds);
            for (MwCustomReportDto mwCustomReportDto : mwCustomReportDtos) {
                if(StringUtils.isNotBlank(assetsBusinessSystem.get(mwCustomReportDto.getAssetsId()))){
                    mwCustomReportDto.setBusinessSystem(assetsBusinessSystem.get(mwCustomReportDto.getAssetsId()));
                }
            }
            PageInfo pageInfo = new PageInfo<>(customReportDtos);
            pageInfo.setPageSize(customReportDtos.size());
            pageInfo.setList(mwCustomReportDtos);
            pageInfo.setPageNum(customReportDtos.size());
            return pageInfo;
        }
        return new PageInfo();
    }

    private Map<String,String> indexGroup(){
        List<MwReportIndexDto> mwReportIndexDtos = reportDao.selectReportIndex();
        Map<String,String> indexMap = new HashMap<>();
        for (MwReportIndexDto mwReportIndexDto : mwReportIndexDtos) {
            indexMap.put(mwReportIndexDto.getItemName(),mwReportIndexDto.getChnName());
        }
        return indexMap;
    }

    /**
     * 查询历史记录数据
     * @param reportParam
     * @param indexMap
     * @param times
     * @return
     */
    private List<MwCustomReportDto> getIndexHistoryData(MwCustomReportParam reportParam,Map<String, String> indexMap,List<Long> times){
        List<MwCustomReportDto> customReportDtos = new ArrayList<>();
        //查询数据库缓存的历史数据
        List<MwReportTrendCacheDto> trendCacheDtos = reportDao.selectReporHistoryData(reportParam.getAssetsIds(), reportParam.getIndexs(), times.get(0), times.get(1));
        if(CollectionUtils.isEmpty(trendCacheDtos)){return customReportDtos;}
        for (MwReportTrendCacheDto trendCacheDto : trendCacheDtos) {
            MwCustomReportDto customReportDto = new MwCustomReportDto();
            customReportDto.extractFrom(trendCacheDto);
            MwCustomReportIndexDto reportIndexDto = new MwCustomReportIndexDto();
            reportIndexDto.extractFrom(trendCacheDto,indexMap.get(trendCacheDto.getItemName()));
            customReportDto.setReportIndexDtos(Arrays.asList(reportIndexDto));
            customReportDtos.add(customReportDto);
        }
        return customReportDtos;
    }

    /**
     * 获取指标的最新数据
     * @param reportParam
     */
    private List<MwCustomReportDto> getIndexLatestData(MwCustomReportParam reportParam,Map<String, String> indexMap) throws Exception {
        List<MwCustomReportDto> customReportDtos = new ArrayList<>();
        List<MwReportTrendCacheDto> trendCacheDtos = reportDao.selectReportLatestData(reportParam.getAssetsIds(), reportParam.getIndexs());
        if(CollectionUtils.isEmpty(trendCacheDtos)){return customReportDtos;}
        //按照资产ID进行数据分组
        Map<String, List<MwReportTrendCacheDto>> listMap = trendCacheDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsId()));
        for (Map.Entry<String, List<MwReportTrendCacheDto>> entry : listMap.entrySet()) {
            List<MwReportTrendCacheDto> value = entry.getValue();
            //同监控项汇总
            Map<String, List<MwReportTrendCacheDto>> itemNameMap = value.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
            List<MwReportTrendCacheDto> cacheDtos = dataMerge(itemNameMap);
            if(CollectionUtils.isEmpty(cacheDtos)){continue;}
            MwCustomReportDto customReportDto = new MwCustomReportDto();
            customReportDto.extractFrom(cacheDtos.get(0));
            List<MwCustomReportIndexDto> customReportIndexDtos = new ArrayList<>();
            for (MwReportTrendCacheDto cacheDto : cacheDtos) {
                MwCustomReportIndexDto reportIndexDto = new MwCustomReportIndexDto();
                reportIndexDto.extractFrom(cacheDto,indexMap.get(cacheDto.getItemName()));
                customReportIndexDtos.add(reportIndexDto);
            }
            customReportDto.setReportIndexDtos(customReportIndexDtos);
            customReportDtos.add(customReportDto);
        }
        return customReportDtos;
    }

    private List<MwReportTrendCacheDto> dataMerge(Map<String, List<MwReportTrendCacheDto>> itemNameMap) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        List<MwReportTrendCacheDto> cacheDtos = new ArrayList<>();
        //值总和
        for (Map.Entry<String, List<MwReportTrendCacheDto>> itemEntry : itemNameMap.entrySet()) {
            double sum = itemEntry.getValue().stream().filter(item->StringUtils.isNotBlank(item.getLastValue())).mapToDouble(item -> Double.parseDouble(item.getLastValue())).sum();
            MwReportTrendCacheDto cacheDto = new MwReportTrendCacheDto();
            PropertyUtils.copyProperties(cacheDto, itemEntry.getValue().get(0));
            cacheDto.setLastValue(String.valueOf(sum));
            cacheDtos.add(cacheDto);
        }
        return cacheDtos;
    }

    @Override
    public Reply getReportIndexDropDown() {
        try {
            List<MwReportIndexDto> mwReportIndexDtos = reportDao.selectReportIndex();
            return Reply.ok(mwReportIndexDtos);
        }catch (Throwable e){
            log.error("MwCustomReportServiceImpl{} getReportIndexDropDown() ERROR::",e);
            return Reply.fail("MwCustomReportServiceImpl{} getReportIndexDropDown() ERROR");
        }
    }

    private Map<String,String> getAssetsBusinessSystem(List<String> assetsIds){
        Map<String,String> assetsMap = new HashMap<>();
        List<Integer> ids = assetsIds.stream().map(Integer::parseInt).collect(Collectors.toList());
        QueryTangAssetsParam qParam = new QueryTangAssetsParam();
        qParam.setPageSize(Integer.MAX_VALUE);
        qParam.setIsQueryAssetsState(false);
        qParam.setUserId(userService.getAdmin());
        qParam.setAssetsIds(assetsIds);
        qParam.setInstanceIds(ids);
        List<MwTangibleassetsTable> assetsTable = assetsManager.getAssetsTable(qParam);
        if(CollectionUtils.isEmpty(assetsTable)){return assetsMap;}
        assetsMap = assetsTable.stream().filter(item -> StringUtils.isNotBlank(item.getModelSystem())).collect(Collectors.toMap(MwTangibleassetsTable::getId, item -> item.getModelSystem() + "-" + item.getModelClassify()));
        return assetsMap;
    }
}
