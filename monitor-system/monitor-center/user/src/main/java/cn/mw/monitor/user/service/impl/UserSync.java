package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.user.service.MwUserSessionService;
import cn.mw.monitor.user.state.LoginState;
import cn.mw.monitor.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@Slf4j
public class UserSync {

    @Resource
    private MWUserDao mwuserDao;
    @Autowired
    private MwUserSessionService userSessionService;


    public void updateUserState(LoginContext loginContext){
        log.info("进入updateUserState方法={}",loginContext.getLoginInfo().getUser().getLoginName());
        LoginInfo loginInfo = loginContext.getLoginInfo();
        MWUser mwuser = new MWUser();
        UserDTO user = loginInfo.getUser();
        mwuser.setUserId(user.getUserId());

        if (loginContext.getUserExpireState()!=null){
            //更新用户过期状态
            mwuser.setUserExpireState(loginContext.getUserExpireState().getName());
        }
        if (loginContext.getPasswdState() != null){
            //更新密码状态
            mwuser.setPasswdState(loginContext.getPasswdState().getName());
        }
        //更新登录状态
        mwuser.setLoginState(loginContext.getLoginState());

        mwuserDao.updateState(mwuser);
        Integer sessionId = userSessionService.saveUserSession(loginContext.getLoginInfo());
        log.info("updateUserState方法中 sessionId={}",sessionId);
        if (LoginState.ONLINE.getName().equals(loginContext.getLoginState())) {
            RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
            String key = "LOGIN_USER_INFO_MAP";
            String userSessionKey = "USER_SESSION";
            HashMap<String, Integer> loginUserMap = new HashMap<>();
            HashMap<String, Integer> userSessionMap = new HashMap<>();
            if (redisUtils.hasKey(key) ) {
                loginUserMap = (HashMap<String, Integer>) redisUtils.get(key);
                log.info("updateUserState方法中 loginUserMap={}",loginUserMap);
            }
            if(redisUtils.hasKey(userSessionKey)){
                userSessionMap = (HashMap<String, Integer>)redisUtils.get(userSessionKey);
                log.info("updateUserState方法中 userSessionMap={}",userSessionMap);
            }
            loginUserMap.put("shiro:session:" + loginContext.getLoginDTO().getToken(), user.getUserId());
            userSessionMap.put("shiro:session:"+loginContext.getLoginDTO().getToken(), sessionId);
            redisUtils.set(key, loginUserMap);
            redisUtils.set(userSessionKey,userSessionMap,12 * 60 * 60);
        }
    }
}
