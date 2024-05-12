package cn.mw.monitor.screen.service.impl;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.screen.dao.MWLagerScreenDao;
import cn.mw.monitor.screen.model.MapAlert;
import cn.mw.monitor.screen.model.MapAlertConfig;
import cn.mw.monitor.service.link.api.LinkLifeCycleListener;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mw.monitor.service.redis.RedisExpireEvent;
import cn.mw.monitor.service.redis.RedisListener;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.GzipTool;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.monitor.weixinapi.NotifyAlertMessage;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class LargeScreenMessageMange implements NotifyAlertMessage ,LinkLifeCycleListener ,FinishProcessCallBack , RedisListener
        , InitializingBean {

    private static final String PREFIX ="screen:map";
    private static final String CURRENT_MAP = PREFIX + ":current";

    @Value("${screen.cacheTime}")
    private int cacheTime;

    @Value("${screen.debug}")
    private boolean debug;

    @Value("#{'${screen.mapKey}'.split(',')}")
    private Set<String> mapKeys;

    @Value("${screen.messageQueue.size}")
    private int messageQueueSize;

    @Value("${screen.hostip.startKey}")
    private String hostipStartKey;

    @Autowired
    private MapAlertConfig mapAlertConfig;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWOrgService mwOrgService;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private MWLagerScreenDao mwLagerScreenDao;

    private AtomicBoolean acceptMessage = new AtomicBoolean(false);
    private AtomicBoolean executing = new AtomicBoolean(false);
    private BlockingQueue<String> messageQueue;
    private Queue<MapAlert> respQueue;
    private Map<String , Queue<MapAlert>> userMessageMap = new ConcurrentHashMap<>();
    private Thread executeThread;
    private Date mapChangeTime;
    private Date mapCacheTime;

    public MapAlertConfig getMapAlertConfig() {
        return mapAlertConfig;
    }

    @Override
    public void sendMessage(String message) {
        //判断消息是否为ICMP格式
        if(acceptMessage.get() && StringUtils.isNotEmpty(message)){
            if(checkValidMessage(message)){
                try {
                    if (debug) {
                        log.info("sendMessage {}", message);
                    }
                    messageQueue.add(message);
                } catch (Exception e) {
                    log.error("sendMessage", e);
                }
            }
        }
    }

    public void saveCurrentMap(List<MapAlert> list){
        if(null != list){
            String mapStr = JSON.toJSONString(list);
            String zipStr = GzipTool.gzip(mapStr);
            String loginName = iLoginCacheInfo.getLoginName();
            String key = CURRENT_MAP + ":" + loginName;
            redisUtils.set(key ,zipStr ,cacheTime);
            mapCacheTime = new Date();
        }
    }

    public Map<String ,MapAlert> getCurrentMap(){
        String loginName = iLoginCacheInfo.getLoginName();
        String key = CURRENT_MAP + ":" + loginName;
        String zipStr = (String)redisUtils.get(key);
        if(StringUtils.isNotEmpty(zipStr)) {
            String unZipStr = GzipTool.gunzip(zipStr);
            List<MapAlert> list =  JSON.parseArray(unZipStr ,MapAlert.class);
            Map<String ,MapAlert> map = new HashMap<>();
            for(MapAlert mapAlert:list){
                map.put(mapAlert.getKey() ,mapAlert);
            }
            return map;
        }

        return null;
    }

    private void refreshCurrentMap(String loginName){
        String key = CURRENT_MAP + ":" + loginName;
        if(redisUtils.hasKey(key)){
            redisUtils.expire(key ,cacheTime);
        }
    }

    private boolean checkValidMessage(String message){
        boolean check = true;
        for(String key : mapKeys){
            if(message.indexOf(key) < 0){
                check = false;
                break;
            }
        }
        return check;
    }

    public List<MapAlert> getMapEvent(){

        //如果未缓存
        if(null == mapCacheTime){
            return null;
        }

        //当线路数据有更新时,返回空
        if(null != mapChangeTime){
            long interval = DateUtils.between(mapCacheTime ,mapChangeTime , DateUnitEnum.SECOND);
            if(interval > 0){
                return null;
            }
        }

        String loginName = iLoginCacheInfo.getLoginName();

        if(debug){
            log.info("{} getMapEvent executing {}" ,loginName ,executing.get());
        }

        acceptMessage();

        refreshTimer(loginName);

        Queue<MapAlert> userMessageQueue = userMessageMap.get(loginName);
        if(null == userMessageQueue) {
            synchronized (userMessageMap) {
                userMessageQueue = userMessageMap.get(loginName);
                if (null == userMessageQueue) {
                    userMessageMap.put(loginName, new ArrayBlockingQueue<MapAlert>(messageQueueSize));
                }
            }
        }

        if(!executing.get()){
            MessageProcessTask messageProcessTask = new MessageProcessTask(messageQueue
                    ,messageQueueSize ,this ,hostipStartKey ,mapAlertConfig ,mwLagerScreenDao ,mwOrgService);
            messageProcessTask.setDebug(debug);
            executeThread = new Thread(messageProcessTask);
            executing.set(true);
            executeThread.start();
        }

        //分发信息到各个用户消息响应队列
        synchronized (respQueue) {
            try {
                while (true) {
                    MapAlert mapAlert = respQueue.remove();
                    for (Queue<MapAlert> queue : userMessageMap.values()) {
                        try {
                            queue.add(mapAlert);
                        } catch (IllegalStateException e) {

                        }
                    }
                }
            } catch (NoSuchElementException e) {

            }
        }

        Map<String ,MapAlert> map = getCurrentMap();

        if(null != map) {
            List<MapAlert> list = new ArrayList<>();
            //获取当前用户信息,并返回
            boolean mapChange = false;
            Queue<MapAlert> userQueue = userMessageMap.get(loginName);
            if (null != userQueue) {
                while (!userQueue.isEmpty()) {
                    MapAlert mapAlert = userQueue.remove();
                    MapAlert chgMapAlert = map.get(mapAlert.getKey());
                    chgMapAlert.setColor(mapAlert.getColor());
                    mapChange = true;
                }
            }

            if (!map.isEmpty()) {
                list = new ArrayList<>(map.values());

                //当前地图变化,需要重新保存
                if(mapChange){
                    saveCurrentMap(list);
                }
            }
            return list;
        }

        return null;
    }

    public void acceptMessage(){
        boolean start = acceptMessage.compareAndSet(false ,true);
        if(start){
            log.info("acceptMessage");
        }
    }

    public void stopAccept(){
        boolean stop =  acceptMessage.compareAndSet(true ,false);
        if(stop){
            log.info("stopAccept");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        respQueue = new ArrayBlockingQueue<MapAlert>(messageQueueSize);
        messageQueue = new LinkedBlockingQueue(messageQueueSize);
    }

    @Override
    public void finish(List<MapAlert> mapAlertList) {
         try {
            if (null != mapAlertList) {
                for (MapAlert mapAlert : mapAlertList) {
                    respQueue.add(mapAlert);
                }
            }
         } catch (Exception e) {
             log.warn("finish", e);
        }

        executing.set(false);
    }

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        if(event instanceof RedisExpireEvent) {
            RedisExpireEvent redisExpireEvent = (RedisExpireEvent) event;
            checkAndClean(redisExpireEvent.getKey());
        }
        return null;
    }

    private void checkAndClean(String key){
        if(key.indexOf(PREFIX) < 0) {
            return;
        }

        String[] values = key.split(RedisUtils.SEP);
        if(values.length > 2){
            userMessageMap.remove(values[2]);
        }else{
            stopAccept();
            userMessageMap.clear();
        }

    }

    public void refreshTimer(String loginName){
        redisUtils.set(PREFIX ,true ,cacheTime);
        String userVisit = PREFIX + RedisUtils.SEP + loginName;
        redisUtils.set(userVisit ,true ,cacheTime);

        refreshCurrentMap(loginName);
    }

    @Override
    public void add(AddAndUpdateParam addAndUpdateParam) {
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(addAndUpdateParam.getLinkTargetIp())) {
            mapChangeTime = new Date();
        }
    }

    @Override
    public void modify(AddAndUpdateParam addAndUpdateParam) {
        mapChangeTime = new Date();
    }

    @Override
    public void delete(List<String> linkIds) {
        if(null != linkIds){
            mapChangeTime = new Date();
        }
    }
}
