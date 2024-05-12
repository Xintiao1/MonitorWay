package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.Pinyin4jUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 告警分类统计DTO
 * @Author gengjb
 * @Date 2023/4/17 14:12
 * @Version 1.0
 **/
@Data
@ApiModel("告警分类统计DTO")
public class MwVisualizedModuleAlertClassifyDto {

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("类型实例总数")
    private Integer totalCount;

    @ApiModelProperty("在线数量")
    private Integer onLineCount;

    @ApiModelProperty("告警等级数据")
    private List<MwVisualizedModuleAlertLevelDto> alertLevelDtos;

    public void extractFrom( Map<String,Integer> alertCountMap){
       if(alertCountMap == null || alertCountMap.isEmpty()){return;}
       if(alertLevelDtos == null){
           alertLevelDtos = new ArrayList<>();
       }
        ConcurrentHashMap<String, String> alertLevelMap = MWAlertLevelParam.alertLevelMap;
        for (Map.Entry<String, Integer> entry : alertCountMap.entrySet()) {
            MwVisualizedModuleAlertLevelDto alertLevelDto = new MwVisualizedModuleAlertLevelDto();
            alertLevelDto.setLevelName(entry.getKey());
            for (String levelCode : alertLevelMap.keySet()) {
                String levelName = alertLevelMap.get(levelCode);
                if(entry.getKey().equals(levelName)){
                    alertLevelDto.setSortLevelType(levelCode);
                }
            }
            alertLevelDto.setAlertCount(entry.getValue());
            alertLevelDtos.add(alertLevelDto);
        }
        //等级排序
        Comparator<Object> com = Collator.getInstance(Locale.CHINA);
        Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
        alertLevelDtos = alertLevelDtos.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o2.getSortLevelType()), pinyin4jUtil.getStringPinYin(o1.getSortLevelType()))).collect(Collectors.toList());
    }

}
