package cn.huaxing.service.impl;

import cn.huaxing.dao.HuaxingVisualizedDao;
import cn.huaxing.dto.HuaxingVisualizedDataDto;
import cn.huaxing.dto.HuaxingVisualizedFileTrendDto;
import cn.huaxing.param.HuaxingVisualizedParam;
import cn.huaxing.service.HuaxingVisualizedService;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 可视化插件接口实现
 * @date 2023/8/28 10:41
 */
@Service
@Slf4j
public class HuaxingVisualizedServiceImpl implements HuaxingVisualizedService {

    @Autowired
    private HuaxingVisualizedDao visualizedDao;

    /**
     * 获取华兴数据的数据信息
     * @return
     */
    public Reply getHuaxingDataBaseInfo(HuaxingVisualizedParam visualizedParam){
        try {
            List<Map<String,Object>> list = new ArrayList<>();
            HuaxingVisualizedDataDto visualizedDataDto = visualizedDao.selectHuaxingcacheData(visualizedParam.getChartType(), visualizedParam.getPartitionName());
            log.info("HuaxingVisualizedServiceImpl{} getHuaxingDataBaseInfo() visualizedDataDto:"+visualizedDataDto);
            if(visualizedDataDto == null){ return Reply.ok(list);}
            String dataStr = visualizedDataDto.getDataStr();
            list = JSON.parseObject(dataStr, List.class);
            if(visualizedParam.getChartType() == 103){
                Map<String, List<HuaxingVisualizedFileTrendDto>> realMap = handlerHuaxingFileTrend(list);
                return Reply.ok(realMap);
            }
            return Reply.ok(list);
        }catch (Throwable e){
            log.error("HuaxingVisualizedServiceImpl{} getHuaxingDataBaseInfo() error:",e);
            return Reply.fail("HuaxingVisualizedServiceImpl{} getHuaxingDataBaseInfo() error:",e);
        }
    }


    /**
     * 文件解析数量趋势需要处理结果
     * @param mapList
     */
    private Map<String,List<HuaxingVisualizedFileTrendDto>> handlerHuaxingFileTrend(List<Map<String, Object>> mapList) throws ParseException {
        Map<String,List<HuaxingVisualizedFileTrendDto>> fileTrendMap = new HashMap<>();
        if(CollectionUtils.isEmpty(mapList)){return fileTrendMap;}
        List<HuaxingVisualizedFileTrendDto> huaxingVisualizedFileTrendDtos = JSONArray.parseArray(JSONArray.toJSONString(mapList), HuaxingVisualizedFileTrendDto.class);
        if(CollectionUtils.isEmpty(huaxingVisualizedFileTrendDtos)){return fileTrendMap;}
        //按照名称分组
        Map<String, List<HuaxingVisualizedFileTrendDto>> collect = huaxingVisualizedFileTrendDtos.stream().collect(Collectors.groupingBy(item -> item.getName()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh");
        //按时间进行数据排序
        for (String name : collect.keySet()) {
            List<HuaxingVisualizedFileTrendDto> fileTrendDtos = collect.get(name);
            if(CollectionUtils.isEmpty(fileTrendDtos)){continue;}
            for (HuaxingVisualizedFileTrendDto fileTrendDto : fileTrendDtos) {
                fileTrendDto.setSortTime(format.parse(fileTrendDto.getTime()));
            }
            //排序
            sort(fileTrendDtos);
            fileTrendMap.put(name,fileTrendDtos);
        }
        return fileTrendMap;
    }


    private void sort(List<HuaxingVisualizedFileTrendDto> fileTrendDtos){
        Collections.sort(fileTrendDtos, new Comparator<HuaxingVisualizedFileTrendDto>() {
            @Override
            public int compare(HuaxingVisualizedFileTrendDto o1, HuaxingVisualizedFileTrendDto o2) {
                if(o1.getSortTime().compareTo(o2.getSortTime()) > 0){
                    return 1;
                }
                if(o1.getSortTime().compareTo(o2.getSortTime()) < 0){
                    return -1;
                }
                return 0;
            }
        });
    }
}
