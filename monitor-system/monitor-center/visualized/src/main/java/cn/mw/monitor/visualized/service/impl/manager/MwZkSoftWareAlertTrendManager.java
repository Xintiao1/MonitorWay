package cn.mw.monitor.visualized.service.impl.manager;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.util.MwVisualizedDateUtil;
import cn.mw.monitor.visualized.dto.MwZkSoftWareAlertTrendDto;
import cn.mw.monitor.visualized.param.MwVisualizedZkSoftWareParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description ToDo
 * @Author gengjb
 * @Date 2023/3/17 10:20
 * @Version 1.0
 **/
@Component
@Slf4j
public class MwZkSoftWareAlertTrendManager {

    @Autowired
    private MWAlertService mwalertService;

    /**
     * 获取告警趋势
     * @param param
     */
    public MwZkSoftWareAlertTrendDto getAlertTrend(MwVisualizedZkSoftWareParam param){
        try {
            log.info("中控告警趋势数据"+param);
            MwZkSoftWareAlertTrendDto alertTrendDto = new MwZkSoftWareAlertTrendDto();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime;
            String endTime;
            //时间转换
            if(StringUtils.isNotBlank(param.getStartTime()) && StringUtils.isNotBlank(param.getEndTime())){
                startTime = param.getStartTime();
                endTime = param.getEndTime();
            }else{
                List<Date> dateList = MwVisualizedDateUtil.getDates(param.getType(), param.getDateType());
                startTime = format.format(dateList.get(0));
                endTime = format.format(dateList.get(1));
            }
            AlertParam alertParam = new AlertParam();
            alertParam.setPageSize(Integer.MAX_VALUE);
            alertParam.setStartTime(startTime);
            alertParam.setEndTime(endTime);
            Reply reply = mwalertService.getCurrAlertPage(alertParam);
            log.info("中控告警趋势数据2"+alertParam);
            log.info("中控告警趋势数据3"+reply);
            if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS){ return alertTrendDto;}
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<ZbxAlertDto> zbxAlertDtos = pageInfo.getList();
            //数据处理
            alertTrendDto = alertDataHandle(zbxAlertDtos,startTime,endTime);
            return alertTrendDto;
        }catch (Throwable e){
            log.error("中控查询告警趋势失败",e);
            return null;
        }
    }

    /**
     * 告警数据处理
     * @param zbxAlertDtos
     */
    private MwZkSoftWareAlertTrendDto alertDataHandle(List<ZbxAlertDto> zbxAlertDtos,String startTime,String endTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        MwZkSoftWareAlertTrendDto alertTrendDto = new MwZkSoftWareAlertTrendDto();
        if(CollectionUtils.isEmpty(zbxAlertDtos)){return alertTrendDto;}
        for (ZbxAlertDto zbxAlertDto : zbxAlertDtos) {
            zbxAlertDto.setAlertDate(format.parse(zbxAlertDto.getClock()));
        }
        alertSort(zbxAlertDtos);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate start = LocalDate.parse(startTime, dateTimeFormatter);
        LocalDate end = LocalDate.parse(endTime, dateTimeFormatter);
        long day = end.toEpochDay() - start.toEpochDay();
        Map<String, List<ZbxAlertDto>> alertMap = new HashMap<>();
        if(day > 1){
            alertMap = zbxAlertDtos.stream().collect(Collectors.groupingBy(item -> new SimpleDateFormat("yyyy-MM-dd").format(item.getAlertDate())));
        }else{
            alertMap = zbxAlertDtos.stream().collect(Collectors.groupingBy(item -> new SimpleDateFormat("yyyy-MM-dd HH").format(item.getAlertDate())));
        }
        log.info("中控告警趋势数据4"+alertMap);
        List<String> times = new ArrayList<>();//时间坐标
        List<Integer> datas = new ArrayList<>();//数据信息
        log.info("中控告警趋势数据5"+day);
        for (Map.Entry<String, List<ZbxAlertDto>> entry : alertMap.entrySet()) {
            String key = entry.getKey();
            List<ZbxAlertDto> alertDtos = entry.getValue();
            if(day > 1){
                times.add(key.substring(0,10));
                datas.add(alertDtos.size());
            }else{
                times.add(key.substring(0,13)+":00");
                datas.add(alertDtos.size());
            }
        }
        log.info("中控告警趋势数据6"+times);
        log.info("中控告警趋势数据7"+datas);
        alertTrendDto.setTimes(times);
        alertTrendDto.setDatas(datas);
        return alertTrendDto;
    }

    /**
     * 告警按时间排序
     */
    private void alertSort(List<ZbxAlertDto> zbxAlertDtos){
        Collections.sort(zbxAlertDtos, new Comparator<ZbxAlertDto>() {
            @Override
            public int compare(ZbxAlertDto o1, ZbxAlertDto o2) {
                if(o1.getAlertDate().compareTo(o2.getAlertDate()) > 0){
                    return -1;
                }
                if(o1.getAlertDate().compareTo(o2.getAlertDate()) < 0){
                    return 1;
                }
                return 0;
            }
        });
    }
}
