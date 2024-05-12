package cn.mw.monitor.user.common;

import lombok.Data;

@Data
public class UsernameToken extends org.apache.shiro.authc.UsernamePasswordToken{

    private static final long serialVersionUID = 12344;
    //是否免密登录
    private boolean unpass = false;


    public UsernameToken(){
        super();
    }
    //免密登录
    public UsernameToken(String userName){
        super(userName,"",false,null);
    }

    public boolean getUnpass() {
        return this.unpass;
    }
    public void setUnpass(boolean password) {
        this.unpass = password;
    }

}
