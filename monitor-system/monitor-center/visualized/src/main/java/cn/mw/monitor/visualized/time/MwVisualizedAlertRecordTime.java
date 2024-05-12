package cn.mw.monitor.visualized.time;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedAlertRecordDto;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description 每日记录告警分类统计
 * @Author gengjb
 * @Date 2023/6/6 14:18
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwVisualizedAlertRecordTime {

    @Autowired
    private MWAlertService mwalertService;

    @Autowired
    private ModuleIDManager idManager;

    @Value("${visualized.group.count}")
    private Integer groupCount;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MWUserCommonService commonService;

    /**
     * 获取当前告警记录并存数据库
     * @return
     */
//    @Scheduled(cron = "0 0/3 * * * ?")
    public TimeTaskRresult getCurrAlertRecord(){
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //获取当前告警信息
            AlertParam alertParam = new AlertParam();
            alertParam.setPageSize(Integer.MAX_VALUE);
            alertParam.setUserId(commonService.getAdmin());
            alertParam.setStartTime(DateUtils.formatDate(new Date()));
            alertParam.setEndTime(DateUtils.formatDate(new Date()));
            Reply reply = mwalertService.getHistAlertPage(alertParam);
            if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS){ return null;}
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<ZbxAlertDto> zbxAlertDtos = pageInfo.getList();
            if(CollectionUtils.isEmpty(zbxAlertDtos)){return null;}
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String time = format.format(new Date());
            List<MwVisualizedAlertRecordDto> alertRecordDtos = new ArrayList<>();
            for (ZbxAlertDto zbxAlertDto : zbxAlertDtos) {
                MwVisualizedAlertRecordDto recordDto = new MwVisualizedAlertRecordDto();
                recordDto.setCacheId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                recordDto.extractFrom(zbxAlertDto,time);
                alertRecordDtos.add(recordDto);
            }
            List<List<MwVisualizedAlertRecordDto>> partition = Lists.partition(alertRecordDtos, groupCount);
            for (List<MwVisualizedAlertRecordDto> mwVisualizedAlertRecordDtos : partition) {
                visualizedManageDao.visualizedCacheAlertInfo(mwVisualizedAlertRecordDtos);
            }
            //数据添加到数据库
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("当前告警记录存数据库:成功");
        }catch (Throwable e){
            log.error("MwVisualizedAlertRecordTime{} getCurrAlertRecord::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("当前告警记录存数据库:失败");
            result.setFailReason(e.getMessage());
        }
        return result;
    }
}
