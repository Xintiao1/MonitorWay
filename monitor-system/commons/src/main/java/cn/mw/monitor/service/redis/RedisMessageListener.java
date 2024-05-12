package cn.mw.monitor.service.redis;

import cn.mw.monitor.event.EventListner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RedisMessageListener extends KeyExpirationEventMessageListener {

    private List<EventListner> postProcessers = new ArrayList<EventListner>();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public void addchecks(List<RedisListener> redisListeners) {
        for (RedisListener redisListener : redisListeners) {
            log.info("RedisMessageListener add:" + redisListener.getClass().getCanonicalName());
            addListener(redisListener);
        }
    }

    private void addListener(RedisListener redisListener){
        postProcessers.add(redisListener);
    }

    private void publishExpireEvent(RedisExpireEvent redisExpireEvent){
        for (EventListner listener : postProcessers) {
            try {
                listener.handleEvent(redisExpireEvent);
            }catch (Throwable e){}
        }
    }

    public RedisMessageListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("收到 redis 过期异步通知：{}", message.toString());

        String redisKey = (String) redisTemplate.getValueSerializer().deserialize(message.getBody());
        log.info("redisKey : {}", redisKey);

        RedisExpireEvent redisExpireEvent = new RedisExpireEvent(redisKey);
        publishExpireEvent(redisExpireEvent);
    }
}
