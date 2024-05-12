package cn.mw.time;


import cn.mw.module.security.service.EsSysLogAuditService;
import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.alert.service.impl.MWAlertServiceImpl;;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author xhy
 * @date 2020/4/17 17:07
 */
@Component
@EnableScheduling
@Slf4j
public class MWZbxAlertNowTime {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private MWAlertAssetsDao assetsDao;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWAlertServiceImpl mwAlertService;

    @Autowired
    MWGroupCommonService mwGroupCommonService;

    @Autowired
    EsSysLogAuditService esSysLogAuditService;

    private static final String saveAlertGetNow = "saveAlertGetNow";

    private static final String getList = "get_alert_list";

    private static final String saveAlertGetHist = "saveAlertGetHist";

    //@Scheduled(cron = "0 */4 * * * ?")
    public TimeTaskRresult saveAlertGetNow() {
        log.info(">>>>>>>saveAlertGetNow>>>>>>>>>>");
        log.info(">>>>>>>当前告警启动>>>>>>>>>>");
        //系统管理员数据
        Integer uid = 106;
        AlertParam alertParam = new AlertParam();
        alertParam.setUserId(uid);
        alertParam.setIsRedis(false);
        String key = genRedisKey(saveAlertGetNow, getList, uid);
        List<ZbxAlertDto> list = mwAlertService.getCurrAlertList(alertParam);
        saveToRedis(key, JSON.toJSONString(list));
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        //进行数据添加
        taskRresult.setSuccess(true);
        taskRresult.setResultType(0);
        taskRresult.setResultContext("当前告警成功");
        log.info(">>>>>>>saveAlertGetNow end>>>>>>>>>>");
        return taskRresult;
    }

    private String genRedisKey(String methodName, String objectName, Integer uid) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append(":").append(objectName)
                .append("_").append(uid);
        return sb.toString();
    }

    private void saveToRedis(String key, String value) {
        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
        }
        redisTemplate.opsForValue().set(key, value, 60 * 15, TimeUnit.SECONDS);
    }

    //@Scheduled(cron = "0 0 2 * * ?")
    public TimeTaskRresult saveAlertGetHist() {
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try{
            log.info(">>>>>>>saveAlertGetHist>>>>>>>>>>"+ new Date());
            log.info(">>>>>>>历史告警启动>>>>>>>>>>");
            //系统管理员数据
            List<ZbxAlertDto> alertHist = new ArrayList<>();
            Integer uid = 106;
            String adminKey = genRedisKey(saveAlertGetHist, getList, uid);
            AlertParam alertParam = new AlertParam();
            alertParam.setUserId(uid);
            alertParam.setIsRedis(false);
            alertHist = mwAlertService.getHistAlertList(alertParam);
            //saveToRedis(adminKey, JSON.toJSONString(alertHist));
            if (redisTemplate.hasKey(adminKey)) {
                redisTemplate.delete(adminKey);
            }
            redisTemplate.opsForValue().set(adminKey, JSON.toJSONString(alertHist),1, TimeUnit.DAYS);

            //进行数据添加
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("历史告警成功");
            log.info(">>>>>>>saveAlertGetHist end>>>>>>>>>>" + new Date());
            return taskRresult;
        }catch (Exception e){
            log.error("saveAlertGetHist e:" + e);
        }
        taskRresult.setSuccess(false);
        return taskRresult;
    }



}
