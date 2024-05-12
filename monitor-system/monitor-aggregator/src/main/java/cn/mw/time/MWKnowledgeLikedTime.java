package cn.mw.time;

import cn.mw.monitor.knowledgeBase.service.MwKnowledgeLoveActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author syt
 * @Date 2020/9/14 11:42
 * @Version 1.0
 */
@Component
@EnableScheduling //定时器类注解
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@Slf4j
public class MWKnowledgeLikedTime {
    @Autowired
    private MwKnowledgeLoveActionService mwKnowledgeLoveActionService;


    //  @Scheduled(cron = "0 */60  * * * ?") //1小时执行一次 将redis的点赞信息存到数据库
    public void saveKnowledgeLikedHistory() {
        log.info(">>>>>>>saveKnowledgeLikedToMysql>>>>>>>>>>");
        mwKnowledgeLoveActionService.transLikedFromRedisToMysql();
        mwKnowledgeLoveActionService.transLikedCountFromRedisToMysql();
        log.info(">>>>>>>saveKnowledgeLikedToMysql>>>>>>>>>>");
    }
}
