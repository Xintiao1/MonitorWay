package cn.mw.monitor.state;

import cn.mw.monitor.service.user.exception.PasswdExpireException;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.ChangePasswdException;
import cn.mw.monitor.service.user.listener.LoginContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public enum PasswdState {

    NORMAL(0,"NORMAL", 0),
    NOTICE(1,"NOTICE", 2),
    EXPIRE(2,"EXPIRE",3),
    RESET(3,"RESET",4),
    INIT(4,"INIT",5);

    private int code;

    private String name;

    private int priority;

    public static PasswdState DEFAULT;

    static {
        DEFAULT = NORMAL;
    }

    PasswdState(int code, String name, int priority) {
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

    public int getPriority() {
        return priority;
    }

    public void checkAndSetNORMAL(LoginContext loginContext){
        return;
    }

    public void checkAndSetEXPIRE(LoginContext loginContext){
        Integer afterResetDay = loginContext.getAfterResetDay();
        Date expireDate = loginContext.getPasswdExpiryDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expireDate);
        calendar.add(Calendar.DATE, afterResetDay);
        Date afterExpireDate = calendar.getTime();

        Date today = new Date();
        //判断是否强制修改密码
        if(today.after(afterExpireDate)){
            if(loginContext.getResetEnable()
            && (RESET.getPriority() > loginContext.getPasswdState().getPriority())) {
                loginContext.setPasswdState(RESET);
            }
        }

        //判断是否锁定
        calendar.setTime(expireDate);
        Integer afterLockDay = loginContext.getAfterLockDay();
        calendar.add(Calendar.DATE, afterLockDay);
        Date afterLockDate = calendar.getTime();
        if(today.after(afterLockDate)){
            if(loginContext.getLockEnable()
            && (UserExpireState.LOCK.getPriority() > loginContext.getUserExpireState().getPriority())) {
                loginContext.setLockType(false);
                loginContext.setUserExpireState(UserExpireState.LOCK);
            }
        }

        return;
    }

    public void checkAndSetNOTICE(LoginContext loginContext){
        Date today = new Date();
        Integer expireAlertDay = loginContext.getExpireAlertDay();
        Calendar calendar = Calendar.getInstance();
        Date expireDate = loginContext.getPasswdExpiryDate();
        calendar.setTime(expireDate);
        calendar.add(Calendar.DATE, -expireAlertDay);
        Date noticeDate = calendar.getTime();
        if(today.after(noticeDate)
        && (NOTICE.getPriority() > loginContext.getPasswdState().getPriority())){
            loginContext.setPasswdState(NOTICE);
        }else {
            loginContext.setPasswdState(DEFAULT);
        }
    }

    public void checkAndSetRESET(LoginContext loginContext){

        return;
    }

    public void checkAndSetINIT(LoginContext loginContext){
        return;
    }

    public List<Reply> processEXPIRE(LoginContext loginContext){
        List<Reply> faillist = new ArrayList<Reply>();
        String loginname = loginContext.getLoginName();
        PasswdState passwdState = loginContext.getPasswdState();
        if (EXPIRE == passwdState){
            faillist.add(Reply.fail(ErrorConstant.USER_MSG_100122));
        }

        return faillist;
    }

    public List<Reply> processNORMAL(LoginContext loginContext){
        return null;
    }

    public List<Reply> processNOTICE(LoginContext loginContext){

        List<Reply> faillist = new ArrayList<Reply>();
        String loginname = loginContext.getLoginName();
        PasswdState passwdState = loginContext.getPasswdState();
        Date expireDay = loginContext.getPasswdExpiryDate();
        Date today = new Date();
        long diff = expireDay.getTime() - today.getTime();
        long days = diff / (1000 * 60 * 60 * 24);

        if (NOTICE == passwdState){
            String replace = "";
            if (days < 0) {
                days = Math.abs(days);
                replace = days + "天前已经";
            }else {
                replace = days + "天后";
            }
            String msg = Reply.replaceMsg(ErrorConstant.USER_MSG_100120,new String[]{replace});
            faillist.add(Reply.fail(ErrorConstant.USER_100120, msg));
        }

        return faillist;
    }

    public List<Reply> processRESET(LoginContext loginContext) throws Exception {
        List<Reply> faillist = new ArrayList<Reply>();
        String loginname = loginContext.getLoginName();
        PasswdState passwdState = loginContext.getPasswdState();
        if (RESET == passwdState){
            if(loginContext.getChangePasswdEnable()){
                throw new PasswdExpireException(loginname);
            }

            faillist.add(Reply.fail(ErrorConstant.USER_MSG_100123));
        }

        return faillist;
    }

    public List<Reply> processINIT(LoginContext loginContext) throws Exception {
        List<Reply> faillist = new ArrayList<Reply>();
        String loginname = loginContext.getLoginName();
        PasswdState passwdState = loginContext.getPasswdState();
        if (INIT == passwdState){
            faillist.add(Reply.fail(ErrorConstant.USER_MSG_100123));

            if(loginContext.getFirstPasswdEnable()){
                throw new ChangePasswdException(loginname);
            }
        }

        return faillist;
    }
}
