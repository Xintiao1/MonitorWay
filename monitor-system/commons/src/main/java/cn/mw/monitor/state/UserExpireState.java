package cn.mw.monitor.state;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.service.user.exception.RetrySecException;
import cn.mw.monitor.service.user.exception.UserExpireException;
import cn.mw.monitor.service.user.exception.UserLockedException;
import cn.mw.monitor.service.user.listener.LoginContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public enum UserExpireState {

    NORMAL(0,"NORMAL",0),
    TRYOUT(3,"TRYOUT",1),
    EXPIRE(2,"EXPIRE",2),
    LOCK(1,"LOCK",3);

    private int code;

    private String name;

    private int priority;

    public static UserExpireState DEFAULT;

    static {
        DEFAULT = NORMAL;
    }

    UserExpireState(int code, String name, int priority) {
        this.code = code;
        this.name = name;
        this.priority = priority;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getPriority() { return priority; }



    public void checkAndSetNORMAL(LoginContext loginContext){
        return;
    }

    public void checkAndSetLOCK(LoginContext loginContext){
       /* AtomicInteger retryCount = loginContext.getRetryCount();
        //尝试多次锁定用户
        if(loginContext.getIsRefuseAcc()){
            if(retryCount.incrementAndGet() > loginContext.getRetryNum()
            &&(LOCK.getPriority() > loginContext.getUserExpireState().getPriority())){
                //锁定用户
                loginContext.setUserExpireState(LOCK);
            }
        }*/
        return;
    }

    public void checkAndSetTRYOUT(LoginContext loginContext){
       /* //检查输入间隔
        if(loginContext.getIsRefuseAcc()){
            long curTime = System.currentTimeMillis();
            long loginTime = loginContext.getLoginTime().getTime();
            long diffTime = (curTime - loginTime) / 1000;

            if(diffTime < loginContext.getRetrySec()
                    && (TRYOUT.getPriority() > loginContext.getUserExpireState().getPriority())){
                loginContext.setUserExpireState(TRYOUT);
            }
        }*/
        return;
    }

    public void checkAndSetEXPIRE(LoginContext loginContext){
        Date today = new Date();
        if(null!= loginContext.getUserExpiryDate()) {
            Date expireDate = loginContext.getUserExpiryDate();
            if (today.after(expireDate)
                    && EXPIRE.getPriority() > loginContext.getUserExpireState().getPriority()) {
                loginContext.setUserExpireState(EXPIRE);
            }
        }
        return;
    }

    public List<Reply> processNORMAL(LoginContext loginContext){
        return null;
    }

    public List<Reply> processLOCK(LoginContext loginContext){
        UserExpireState userExpireState = loginContext.getUserExpireState();
        boolean lockType = loginContext.getLockType();
        if (LOCK == userExpireState){
           if (lockType) {
               throw new UserLockedException(loginContext.getLoginName(),loginContext.getRetrySec());
           }else {
               throw new UserLockedException(loginContext.getLoginName());
           }
        }

        return null;
    }

    public List<Reply> processEXPIRE(LoginContext loginContext){
        UserExpireState userExpireState = loginContext.getUserExpireState();
        if (EXPIRE == userExpireState){
            throw new UserExpireException(loginContext.getLoginName());
        }
        return null;
    }

    public List<Reply> processTRYOUT(LoginContext loginContext){
        List<Reply> faillist = new ArrayList<Reply>();
        UserExpireState userExpireState = loginContext.getUserExpireState();
        Integer secs = loginContext.getRetrySec();
        if (TRYOUT == userExpireState){
            throw new RetrySecException(loginContext.getLoginName(), loginContext.getRetrySec());
        }
        return null;
    }

    public static void resetState(LoginContext loginContext){
        UserExpireState userExpireState = loginContext.getUserExpireState();
        if(TRYOUT == userExpireState){
            loginContext.setUserExpireState(UserExpireState.DEFAULT);
        }
    }
}
