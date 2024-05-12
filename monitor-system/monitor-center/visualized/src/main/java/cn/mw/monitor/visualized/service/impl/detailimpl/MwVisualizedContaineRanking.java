package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dto.*;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName
 * @Description 容器排行
 * @Author gengjb
 * @Date 2023/6/7 15:47
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedContaineRanking implements MwVisualizedModule {
    @Override
    public int[] getType() {
        return new int[]{74};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            List<MwVisualizedPrometheusDropDto> prometheusParam = moduleParam.getPrometheusParam();
            if(CollectionUtils.isEmpty(prometheusParam)){return null;}
            Map<String,List<MwVisualizedContaineRankingDto>> rankingDtoMap = new HashMap<>();
            for (MwVisualizedPrometheusDropDto mwVisualizedPrometheusDropDto : prometheusParam) {
                MwPrometheusResult prometheusResult = MwVisualizedUtil.getHttpPrometheusGet(mwVisualizedPrometheusDropDto);
                log.info("MwVisualizedContaineRanking{}  getData() prometheusResult::"+prometheusResult);
                List<MwVisualizedContaineRankingDto> containeRankingDtos = handleDataInfo(prometheusResult, mwVisualizedPrometheusDropDto.getUnits());
                log.info("MwVisualizedContaineRanking{}  getData() containeRankingDtos::"+containeRankingDtos);
                //数据排序
                valueSort(containeRankingDtos);
                if(CollectionUtils.isNotEmpty(containeRankingDtos) && containeRankingDtos.size() > 10){
                    rankingDtoMap.put(mwVisualizedPrometheusDropDto.getDesc(),containeRankingDtos.subList(0,10));
                }else{
                    rankingDtoMap.put(mwVisualizedPrometheusDropDto.getDesc(),containeRankingDtos);
                }
            }
            List<MwVisualizedFoldLineDto> foldLineDtos = new ArrayList<>();
            for (String name : rankingDtoMap.keySet()) {
                MwVisualizedFoldLineDto foldLineDto = new MwVisualizedFoldLineDto();
                foldLineDto.setName(name);
                foldLineDto.setValues(rankingDtoMap.get(name));
                foldLineDtos.add(foldLineDto);
            }
            return foldLineDtos;
        }catch (Throwable e){
            log.error("MwVisualizedContaineRanking{}  getData()",e);
            return null;
        }
    }


    private List<MwVisualizedContaineRankingDto> handleDataInfo(MwPrometheusResult results,String units){
        List<MwVisualizedContaineRankingDto> containeRankingDtos = new ArrayList<>();
        MwPromDataInfo data = results.getData();
        log.info("MwVisualizedContaineRanking{}  handleDataInfo() data::"+data);
        List<MwPromMetric> metrics = data.getResult();
        log.info("MwVisualizedContaineRanking{}  handleDataInfo() metrics::"+metrics);
        if(CollectionUtils.isEmpty(metrics)){return containeRankingDtos;}
        for (MwPromMetric metric : metrics) {
            List<MwPromValueDto> promValue = metric.getPromValue();
            if(CollectionUtils.isEmpty(promValue)){continue;}
            String value = promValue.get(0).getValue();
            MwPromMetricInfo info = metric.getMetric();
            MwVisualizedContaineRankingDto rankingDto = new MwVisualizedContaineRankingDto();
            rankingDto.extractFrom(info,value,units);
            if(StringUtils.isBlank(rankingDto.getName())){continue;}
            containeRankingDtos.add(rankingDto);
        }
        return containeRankingDtos;
    }

    private void valueSort( List<MwVisualizedContaineRankingDto> containeRankingDtos){
        Collections.sort(containeRankingDtos, new Comparator<MwVisualizedContaineRankingDto>() {
            @Override
            public int compare(MwVisualizedContaineRankingDto o1, MwVisualizedContaineRankingDto o2) {
                if(o1.getSortValue() > o2.getSortValue()){
                    return -1;
                }
                if(o1.getSortValue() < o2.getSortValue()){
                    return 1;
                }
                return 0;
            }
        });
    }
}
