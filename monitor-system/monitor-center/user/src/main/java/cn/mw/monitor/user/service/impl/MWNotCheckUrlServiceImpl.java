package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.user.dao.MwNotCheckUrlDao;
import cn.mw.monitor.user.model.MwNotCheckUrl;
import cn.mw.monitor.user.service.IMWNotCheckUrlService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service("mWNotCheckUrlServiceImpl")
@Slf4j
public class MWNotCheckUrlServiceImpl implements ApplicationRunner, IMWNotCheckUrlService {

    private static final Logger logger = LoggerFactory.getLogger("component" + MWNotCheckUrlServiceImpl.class.getName());

    private static final String redisGroup = MWNotCheckUrlServiceImpl.class.getSimpleName();

    @Value("${scheduling.enabled}")
    private boolean isTimer;

    @Value("${userDebug}")
    boolean userDebug;

    @Autowired
    private RedisTemplate<String, Object> redisObjectTemplate;

    @Resource
    private MwNotCheckUrlDao mwNotCheckUrlDao;

    @Override
    public boolean isNeedCheck(String url) {
        String key = genRedisKey(url);
        log.info("isNeedCheck url ---->"+url);
        log.info("get key ---->"+key);
        Integer id = (Integer)redisObjectTemplate.opsForValue().get(key);
        log.info("from key get value ---->"+id);
        if(null == id){
            return true;
        }
        return false;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if(isTimer){
            return;
        }
        notCheckUrlToRedis();
    }

    public void notCheckUrlToRedis() {
        //应用启动清理缓存
        StringBuffer info = new StringBuffer();
        info.append("redis-").append(redisGroup).append(" clean");
        Date starttime = new Date();
        logger.info(info.toString() + " start");
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":*");
        Set<String> keys = redisObjectTemplate.keys(sb.toString());
        redisObjectTemplate.delete(keys);

        List<MwNotCheckUrl> list = mwNotCheckUrlDao.selectList();
        if (userDebug) {
            log.info("项目启动时刷进redis的NotCheck信息--------->"+ JSONObject.toJSONString(list));
        }
        if(null != list ){
            for(MwNotCheckUrl mwNotCheckUrl : list){
                String key = genRedisKey(mwNotCheckUrl.getUrl());
                saveToRedis(key,  mwNotCheckUrl.getId());
            }
        }

        Date endtime = new Date();
        Long diff = (endtime.getTime() - starttime.getTime()) / 1000;
        logger.info(info.toString() + " end, used time:" + diff.longValue());
    }

    private void saveToRedis(String key, Integer id){
        redisObjectTemplate.opsForValue().set(key, id);
    }

    private String genRedisKey(String url){
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":").append(url);
        return sb.toString();
    }
}
