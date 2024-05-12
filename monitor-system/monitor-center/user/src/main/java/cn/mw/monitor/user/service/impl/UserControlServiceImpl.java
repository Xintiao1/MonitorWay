package cn.mw.monitor.user.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.user.advcontrol.*;
import cn.mw.monitor.user.dao.MwViewUserControlActionDao;
import cn.mw.monitor.user.dao.MwViewUserControlDao;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.user.model.MwViewUserControl;
import cn.mw.monitor.user.model.MwViewUserControlAction;
import cn.mw.monitor.user.service.IMWUserPostProcesser;
import cn.mw.monitor.user.service.IUserControlService;
import cn.mw.monitor.event.PostUpdUserEvent;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserControlServiceImpl implements ApplicationRunner, IUserControlService, IMWUserPostProcesser {

    private static final Logger logger = LoggerFactory.getLogger("component-" + MWNotCheckUrlServiceImpl.class.getName());

    private static final String redisGroup = UserControlServiceImpl.class.getSimpleName();

    @Value("${scheduling.enabled}")
    private boolean isTimer;

    @Resource
    MwViewUserControlActionDao mwViewUserControlActionDao;

    @Resource
    MwViewUserControlDao mwViewUserControlDao;

    @Autowired
    private RedisTemplate<String, Object> redisObjectTemplate;

    @Override
    public boolean check(RequestInfo requestInfo) {
        String loginName = requestInfo.getLoginName();
        String key = genRedisKey(loginName);
        MwViewUserControlAction mwViewUserControlAction = (MwViewUserControlAction)redisObjectTemplate.opsForValue().get(key);
        if(null == mwViewUserControlAction){
            return true;
        }

        String ruleKey = genRedisRuleKey(loginName);
        List<MwViewUserControl> mwViewUserControllist = (List<MwViewUserControl>)redisObjectTemplate.opsForValue().get(ruleKey);

        CondType condType = CondType.valueOf(mwViewUserControlAction.getCond());
        ConditionBase condition = null;
        switch (condType){
            case AllSatisfy :
                condition = new AllSatisfy();
                break;
            case AllNotSatisfy :
                condition = new AllNotSatisfy();
                break;
            default:
        }

        if(null == condition){
            return true;
        }

        if(null == mwViewUserControllist){
            return true;
        }
        for(MwViewUserControl mwViewUserControl : mwViewUserControllist){
            ControlType controlType = ControlType.valueOf(mwViewUserControl.getControlName());
            switch (controlType){
                case IP :
                    condition.add(new IPStrategy(mwViewUserControl.getRule()));
                    continue;
                case MAC :
                    condition.add(new MacStrategy(mwViewUserControl.getRule()));
                    continue;
                case TIME :
                    condition.add(new TimeStrategy(mwViewUserControl.getRule()));
                    continue;
                default:
            }
        }

        if(condition.isPassed(requestInfo)){
            ActionType actionType = ActionType.valueOf(mwViewUserControlAction.getOperation());
            switch (actionType){
                case Permit:
                    return true;
                case NotPermit:
                    return false;
            }
        }

        return false;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if(isTimer){
            return;
        }
        //应用启动清理缓存
        StringBuffer info = new StringBuffer();
        info.append("redis-").append(redisGroup).append(" clean");
        Date starttime = new Date();
        logger.info(info.toString() + " start");
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":*");
        Set<String> keys = redisObjectTemplate.keys(sb.toString());
        redisObjectTemplate.delete(keys);

        List<MwViewUserControlAction> list = mwViewUserControlActionDao.selectList();
        if(null != list && list.size()>0){
            for(MwViewUserControlAction mwViewUserControlAction : list){
                String key = genRedisKey(mwViewUserControlAction.getLoginName());
                saveToRedis(key,  mwViewUserControlAction);
            }
        }

        List<MwViewUserControl> userControlList = mwViewUserControlDao.selectList();
        if(null != userControlList && userControlList.size()>0) {
            saveUserControlList(userControlList);
        }

        Date endtime = new Date();
        Long diff = (endtime.getTime() - starttime.getTime()) / 1000;
        logger.info(info.toString() + " end, used time:" + diff.longValue());
    }

    private void saveToRedis(String key, Object mwViewUserControlAction){
        redisObjectTemplate.opsForValue().set(key, mwViewUserControlAction);
    }

    private String genRedisKey(String loginName){
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":").append(loginName);
        return sb.toString();
    }

    private String genRedisRuleKey(String loginName){
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":").append(loginName).append("-rule");
        return sb.toString();
    }

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {

        //用户更新时,更新缓存
        if(event instanceof PostUpdUserEvent){
            PostUpdUserEvent postUpdUserEvent = (PostUpdUserEvent) event;
            UserDTO userDTO = postUpdUserEvent.getOldUserdto();
            MwViewUserControlAction mwViewUserControlAction = mwViewUserControlActionDao.selectByPrimaryKey(userDTO.getUserId());
            // 禁用用户登陆控制的用户跳出这个check
            if(null == mwViewUserControlAction){
                return null;
            }
            String key = genRedisKey(mwViewUserControlAction.getLoginName());
            saveToRedis(key, mwViewUserControlAction);

            List<MwViewUserControl> mwViewUserControl = mwViewUserControlDao.selectByUserId(userDTO.getUserId());
            if (mwViewUserControl!=null && mwViewUserControl.size() > 0) {
                saveUserControlList(mwViewUserControl);
            }
        }
        return null;
    }

    private void saveUserControlList(List<MwViewUserControl> userControlList){
        List<MwViewUserControl> grouplist = new ArrayList<MwViewUserControl>();
        boolean first = true;
        int userid = 0;
        MwViewUserControl previous = null;

        for(MwViewUserControl mwViewUserControl : userControlList){
            if(null == mwViewUserControl){
                continue;
            }

            if (!first && userid != mwViewUserControl.getUserId()) {
                String key = genRedisRuleKey(previous.getLoginName());
                saveToRedis(key, grouplist);
                grouplist = new ArrayList<MwViewUserControl>();
            }

            grouplist.add(mwViewUserControl);
            userid = mwViewUserControl.getUserId();
            previous = mwViewUserControl;
            first = false;
        }

        String key = genRedisRuleKey(previous.getLoginName());
        saveToRedis(key, grouplist);
    }
}
