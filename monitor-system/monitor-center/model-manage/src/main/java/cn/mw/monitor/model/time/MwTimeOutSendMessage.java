package cn.mw.monitor.model.time;

import cn.mw.monitor.model.service.MwModelInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author qzg
 * @date 2022/1/23
 */
//@Component
//@EnableScheduling //定时器类注解
////@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
//@Slf4j
//public class MwTimeOutSendMessage {
//
//    @Autowired
//    MwModelInstanceService mwModelInstanceService;
//
//    @Scheduled(cron = "${read.timer.parmas}") //每天晚上10点运行一下，过期时间提醒
//    public void saveKnowledgeLikedHistory() {
//        log.info(">>>>>>>过期提醒：getTimeOutInfo>>>>>>>>>>时间值" + "${read.timer.parmas}");
//        mwModelInstanceService.getTimeOutInfo();
//        log.info(">>>>>>>过期提醒：getTimeOutInfo>>>>>>>>>>");
//    }
//
//
//}
