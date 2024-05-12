package cn.huaxing.time;

import cn.huaxing.dao.HuaxingVisualizedDao;
import cn.huaxing.dto.HuaxingVisualizedDataDto;
import cn.huaxing.dto.HuaxingVisualizedDataSourceDto;
import cn.huaxing.dto.HuaxingVisualizedDataSourceSqlDto;
import cn.huaxing.utils.HuaxingDataBaseConnectionUtil;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.plugin.user.UserPlugin;
import cn.mw.monitor.plugin.visualized.VisualizedPlugin;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 华兴可视化定时任务
 * @date 2023/9/12 14:20
 */
@Service
@Slf4j
public class HuaxingVisualizedTime implements VisualizedPlugin {

    @Autowired
    private HuaxingVisualizedDao visualizedDao;

    /**
     * 获取华兴数据库数据信息
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult saveCaheData(){
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            List<HuaxingVisualizedDataSourceDto> huaxingDataBaseConnectionInfo = visualizedDao.getHuaxingDataBaseConnectionInfo();
            log.info("HuaxingVisualizedTime{} getHuaxingDataInfo huaxingDataBaseConnectionInfo::"+huaxingDataBaseConnectionInfo);
            if(CollectionUtils.isEmpty(huaxingDataBaseConnectionInfo)){return result;}
            //根据ID与类型查询对应SQL
            List<HuaxingVisualizedDataSourceSqlDto> huaxingDataBaseSqlInfo = visualizedDao.getHuaxingDataBaseSqlInfo();
            log.info("HuaxingVisualizedTime{} getHuaxingDataInfo huaxingDataBaseSqlInfo::"+huaxingDataBaseSqlInfo);
            if(CollectionUtils.isEmpty(huaxingDataBaseSqlInfo)){return result;}
            //按照数据源ID进行分组
            Map<String, List<HuaxingVisualizedDataSourceSqlDto>> sqlMap = huaxingDataBaseSqlInfo.stream().collect(Collectors.groupingBy(item -> item.getDataSourceId()));
            for (HuaxingVisualizedDataSourceDto huaxingVisualizedDataSourceDto : huaxingDataBaseConnectionInfo) {
                List<HuaxingVisualizedDataSourceSqlDto> huaxingVisualizedDataSourceSqlDtos = sqlMap.get(huaxingVisualizedDataSourceDto.getId());
                if(CollectionUtils.isEmpty(huaxingVisualizedDataSourceSqlDtos)){continue;}
                huaxingVisualizedDataSourceDto.setDataQuerySqls(huaxingVisualizedDataSourceSqlDtos);
            }
            log.info("HuaxingVisualizedTime{} getHuaxingDataInfo huaxingDataBaseConnectionInfo2::"+huaxingDataBaseConnectionInfo);
            List<HuaxingVisualizedDataDto> huaxingVisualizedDataDtos = HuaxingDataBaseConnectionUtil.connectionDataBase(huaxingDataBaseConnectionInfo);
            log.info("HuaxingVisualizedTime{} getHuaxingDataInfo huaxingVisualizedDataDtos::"+huaxingVisualizedDataDtos);
            if(CollectionUtils.isEmpty(huaxingVisualizedDataDtos)){return result;}
            for (HuaxingVisualizedDataDto huaxingVisualizedDataDto : huaxingVisualizedDataDtos) {
                String jsonString = JSON.toJSONString(huaxingVisualizedDataDto.getDataList());
                huaxingVisualizedDataDto.setDataStr(jsonString);
            }
            visualizedDao.deleteHuaxingCacheData();
            visualizedDao.insertHuaxingcacheData(huaxingVisualizedDataDtos);
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存华兴可视化数据库数据:成功");
        }catch (Throwable e){
            log.error("HuaxingVisualizedTime{} getHuaxingDataInfo() ERROR::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存华兴可视化数据库数据:失败");
            result.setFailReason(e.getMessage());
        }
        return result;
    }
}
