package cn.mw.monitor.visualized.time;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.*;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 容器告警缓存
 * @date 2023/9/18 15:16
 */
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwVisualizedContainerAlertTime {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    private final String ALERT = "Level";

    @Autowired
    private ModuleIDManager idManager;

    /**
     * 存储容器的告警信息
     * @return
     */
//    @Scheduled(cron = "0 0/3 * * * ?")
    public TimeTaskRresult getContainerAlertRecord(){
        log.info("MwVisualizedContainerAlertTime{} getContainerAlertRecord() start>>>>>>");
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //查询数据库的容器告警url
            List<MwVisualizedPrometheusDropDto> mwVisualizedPrometheusDropDtos = visualizedManageDao.selectVisualizedContaineByItemName(ALERT);
            log.info("MwVisualizedContainerAlertTime{} getContainerAlertRecord() mwVisualizedPrometheusDropDtos::"+mwVisualizedPrometheusDropDtos);
            if(CollectionUtils.isEmpty(mwVisualizedPrometheusDropDtos)){return result;}
            //按照分区进行数据分组
            Map<String, List<MwVisualizedPrometheusDropDto>> listMap =
                    mwVisualizedPrometheusDropDtos.stream().collect(Collectors.groupingBy(item -> item.getPartitionName()));
            List<MwVisualizedContainerDto> visualizedContainerDtos = new ArrayList<>();
            for (Map.Entry<String, List<MwVisualizedPrometheusDropDto>> entry : listMap.entrySet()) {
                List<MwVisualizedPrometheusDropDto> prometheusDropDtos = entry.getValue();
                if(CollectionUtils.isEmpty(prometheusDropDtos)){continue;}
                for (MwVisualizedPrometheusDropDto prometheusDropDto : prometheusDropDtos) {
                    MwPrometheusResult prometheusResult = MwVisualizedUtil.getHttpPrometheusGet(prometheusDropDto);
                    log.info("MwVisualizedContainerAlertTime{} getContainerAlertRecord() prometheusResult::"+prometheusResult);
                    MwVisualizedContainerDto containerDto = new MwVisualizedContainerDto();
                    containerDto.extractFrom(prometheusDropDto);
                    //数据解析
                    MwPromDataInfo data = prometheusResult.getData();
                    List<MwPromMetric> metrics = data.getResult();
                    if(CollectionUtils.isEmpty(metrics)){
                        containerDto.setAlertCount(0);
                    }else{
                        containerDto.setAlertCount(metrics.size());
                    }
                    containerDto.setId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                    visualizedContainerDtos.add(containerDto);
                }
            }
            if(CollectionUtils.isEmpty(visualizedContainerDtos)){return result;}
            //数据缓存到数据库
            int count = visualizedManageDao.visualizedCacheContaineAlertInfo(visualizedContainerDtos);
            log.info("MwVisualizedContainerAlertTime{} getContainerAlertRecord() end>>>>>>"+count);
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存可视化容器告警数据:成功");
        }catch (Throwable e){
            log.error("MwVisualizedContainerAlertTime{} getContainerAlertRecord() ERROR::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存可视化容器告警数据:失败");
            result.setFailReason(e.getMessage());
        }
        return result;
    }

}
