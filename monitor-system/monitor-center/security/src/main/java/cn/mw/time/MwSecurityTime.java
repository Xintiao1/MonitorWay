package cn.mw.time;

import cn.mw.monitor.util.MWUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2020/9/17 9:24
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "elasticsearch", name = {"scheduling","enable"}, havingValue = "true")
@EnableScheduling
public class MwSecurityTime {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 每天定时获取es中的类型存入redis
     */
    //  @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点中执行一次
    //@Scheduled(cron = "0 */2 * * * ?") //2分钟执行一次 将最新的告警信息存入其中
    public void saveSecurityType() {
        log.info(">>>>>>>saveSecurityType>>>>start>>>>>>");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        SearchRequest searchRequest = new SearchRequest();

        SearchResponse search = null;
        HashSet set = new HashSet();
        try {
            GetAliasesResponse alias = restHighLevelClient.indices().getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT);
            List<String> list = new ArrayList<>();
            Map<String, Set<AliasMetadata>> map = alias.getAliases();
            map.forEach((k, v) -> {
                if (!k.startsWith(".")) {
                    list.add(k);
                }
            });
            for (String index : list) {
                searchRequest.indices(index);
                search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                for (SearchHit searchHit : search.getHits().getHits()) {
                    set.add(searchHit.getType());
                }
            }
        } catch (IOException e) {
            log.error("(saveSecurityType)IO异常{}", e);
        }
        redisTemplate.opsForValue().set(MWUtils.REDIS_SECURITY_TYPE, JSON.toJSONString(set), 1, TimeUnit.DAYS);
        log.info(">>>>>>>saveSecurityType>>>>>end>>>>>");
    }
}
