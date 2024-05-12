package cn.mw.monitor.util.lucene;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author syt
 * @Date 2020/9/8 20:10
 * @Version 1.0
 */
@Component
@Slf4j
public class CrmProperties {
    @Value("${lucene.url}")
    private String indexUrl;

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }


    // 初始化任务
    @Bean
    public void TestCrmProperties() {
        log.info("1111111111121313123123"+indexUrl);
    }
}
