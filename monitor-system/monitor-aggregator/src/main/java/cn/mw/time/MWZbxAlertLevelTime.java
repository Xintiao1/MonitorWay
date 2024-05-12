package cn.mw.time;

import cn.mw.monitor.accountmanage.entity.AlertRecordTableDTO;
import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.alert.dao.MwAlertActionDao;
import cn.mw.monitor.alert.param.ActionLevelParam;
import cn.mw.monitor.alert.param.ActionLevelRuleParam;
import cn.mw.monitor.alert.service.manager.MWAlertManager;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.weixin.service.WxPortalService;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.SimpleFormatter;


/**
 * @author xhy
 * @date 2020/4/17 17:07
 */
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MWZbxAlertLevelTime {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MWAlertManager alertManager;

    @Resource
    private MwAlertActionDao mwAlertActionDao;

    private String redisKey;
    @Autowired
    private MwAssetsManager mwAssetsManager;
    @Autowired
    private WxPortalService wxPortalService;
    @Autowired
    private MWAlertAssetsDao assetsDao;

    @Value("${file.url}")
    private String filePath;

    private static final String saveAlertGetNow = "saveAlertGetNow";

    private static final String getList = "get_alert_list";

    private static final Integer uid = 106;


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
        redisTemplate.opsForValue().set(key, value, 60 * 10, TimeUnit.SECONDS);
    }


    //@Scheduled(cron = "0 0/3 * * * ?") //分级告警///
    public TimeTaskRresult levelAlert() {
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        try{
            log.info(">>>>>>>被动启动后>>>>>>>>>>");
            log.info(">>>>>>>分级告警启动>>>>>>>>>>");
            redisKey = genRedisKey(saveAlertGetNow, getList, uid);
            String redislist = redisTemplate.opsForValue().get(redisKey);
            List<ZbxAlertDto> list = new ArrayList<>();
            list = JSONArray.parseArray(redislist, ZbxAlertDto.class);
            log.info("分级告警list:" + list.size());
            List<ActionLevelRuleParam> actionLevelRuleList = new ArrayList<>();
            actionLevelRuleList = mwAlertActionDao.selectActionLevelRule();
            if(actionLevelRuleList.size() == 0 || actionLevelRuleList == null){
                taskRresult.setSuccess(true);
                taskRresult.setResultType(0);
                return taskRresult;
            }
            for (ZbxAlertDto s: list) {
                if(s.getAcknowledged().equals(AlertAssetsEnum.unconfirmed.toString())){
                    long longTime = SeverityUtils.getTime(s.getLongTime());
                    //获取action_id
                    for(ActionLevelRuleParam alr : actionLevelRuleList){
                        log.info("分级告警alr:" + alr);
                        if(alr.getState() == 0 || alr.getSelectLevel() == 0){
                            log.info("分级告警关闭actionid：" + alr.getActionId());
                            log.info("分级告警关闭处理状态：" + s.getAcknowledged());
                            continue;
                        }
                        alr.setEventId(s.getEventid());
                        if(alr.getSelectLevel() >= 1){
                            log.info("分级告警一级");
                            sendMessage(s, alr, 1,longTime );
                        }
                        if(alr.getSelectLevel() >= 2){
                            log.info("分级告警二级");
                            sendMessage(s, alr, 2,longTime );
                        }
                        if(alr.getSelectLevel() >= 3){
                            log.info("分级告警三级");
                            sendMessage(s, alr, 3,longTime );
                        }

                    }

                }
            }

            //进行数据添加
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("分级告警成功");
            log.info(">>>>>>>levelAlert>>>>>>>>>>");
        }catch (Exception e){
            log.error("分级告警错误信息error:{}",e);
        }

        return taskRresult;
    }
    private void sendMessage(ZbxAlertDto s,ActionLevelRuleParam alr, Integer level, long longTime){
        alr.setLevel(level);
        log.info("分级告警alr:" + alr);
        List<ActionLevelParam> timeUnit = mwAlertActionDao.getLevelInfo(alr);
        log.info("分级告警timeUnit:" + timeUnit);
        if(timeUnit.get(0).getIsSendPerson() != null && timeUnit.get(0).getIsSendPerson()){
            alr.setIsActionLevel(true);
        }
        float num = 0;
        if(timeUnit.get(0).getTimeUnit() == 0){
            if(level == 1){
                num = alr.getDate() * 60;
            }
            if(level == 2){
                num = alr.getDateTwo() * 60;
            }
            if(level == 3){
                num = alr.getDateThree() * 60;
            }
        }
        if(timeUnit.get(0).getTimeUnit() == 1){
            if(level == 1){
                num =  alr.getDate() * 60 * 60;
            }
            if(level == 2){
                num = alr.getDateTwo() * 60 * 60;
            }
            if(level == 3){
                num = alr.getDateThree() * 60 * 60;
            }

        }
        log.info("分级告警num：" + num);
        log.info("分级告警longTime：" + longTime);
         if(num <= longTime && longTime < num * 2){
            List<Integer> levelEvent = mwAlertActionDao.selectActionLevelEventMapper(alr);
            log.info("分级告警levelEvent：" + levelEvent.size());
            List<Integer> userId = mwAlertActionDao.selectActionLevelUserId(alr);
             log.info("分级告警userId：" + userId);
            if(levelEvent.size() == 0){
                HashSet<Integer> userIds = new HashSet<Integer>(userId);
                log.info("分级告警userIds：" + userIds);
                String msg = wxPortalService.converUnicodeToChar(s.getMessage());
                List<String> msgs = new ArrayList<>();
                msgs.add(msg);
                wxPortalService.dealMessage(msgs,alr,userIds);
                mwAlertActionDao.addActionLevelEventMapper(alr);
            }
        }
    }

    public TimeTaskRresult sendInfoWriteTxt() {
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        Boolean isSucess = false;
        try{
            int count = assetsDao.selectCountRecordTable(null, null);
            if(count <= 1000){
                taskRresult.setSuccess(true);
                return taskRresult;
            }
            count = count - 1000;
            int num = count % 1000 == 0 ? count/1000 : (count/1000 + 1);
            List<AlertRecordTableDTO> sendInfos = new ArrayList<>();
            for(int i=0; i<num;i++){
                int startNum = i * 1000 + 1001;
                int endNum = (i + 1) * 2000;
                List<AlertRecordTableDTO> temp = assetsDao.getSendInfoList(null, null,startNum,endNum);
                sendInfos.addAll(temp);
            }
            if(CollectionUtils.isEmpty(sendInfos)) {
                taskRresult.setSuccess(true);
                return taskRresult;
            }
            HashSet<Integer> ids = new HashSet<>();
            for(AlertRecordTableDTO dto : sendInfos){
                ids.add(dto.getId());
            }
            List<AlertRecordTableDTO> recordTableDTOS = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(ids)){
                List<Integer> idList = new ArrayList<>(ids);
                recordTableDTOS = assetsDao.getAlertRecordUserIds(idList);
            }

            for(AlertRecordTableDTO dto : sendInfos){
                dto.setUserName(getUserName(dto.getId(),recordTableDTOS));
            }
            for (AlertRecordTableDTO s : sendInfos){
                if(s.getIsSuccess() == 0){
                    s.setResultState("成功");
                }else{
                    s.setResultState("失败");
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String name = sdf.format(date);
            String pathName = filePath + "/" + name + ".txt";
           /* File file = new File(pathName);
            if(!file.exists()){
                file.createNewFile();
            }*/
            try (FileWriter fileWriter = new FileWriter(pathName, true);
                 BufferedWriter writer = new BufferedWriter(fileWriter)) {
                for(AlertRecordTableDTO dto : sendInfos){
                    writer.write(dto.toString());
                    writer.newLine();
                }
                assetsDao.deleteRecordInfo();
            } catch (IOException e) {
                log.error("写入文件报错error:{}",e);
            }
            isSucess = true;
        }catch (Exception e){
            log.error("消息告警报错error:{}",e);
        }
        //进行数据添加
        taskRresult.setSuccess(isSucess);
        taskRresult.setResultType(0);
        return taskRresult;
    }

    private String getUserName(Integer id,List<AlertRecordTableDTO> recordTableDTOS){
        StringBuffer sb = new StringBuffer();
        for(AlertRecordTableDTO dto : recordTableDTOS){
            if(id.equals(dto.getId())){
                sb.append(dto.getUserName()).append(",");
            }
        }
        return sb.toString();
    }



}
