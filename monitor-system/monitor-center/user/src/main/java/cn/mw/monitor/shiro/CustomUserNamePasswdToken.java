package cn.mw.monitor.shiro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.stereotype.Component;

/**
 * Created by zy.quaee on 2021/4/30 0:13.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class CustomUserNamePasswdToken extends UsernamePasswordToken implements AuthenticationToken {

    private final static long serialVersionUID = -1L;
    //是否免密登录
    private boolean unpass = false;
    /**
     * 登录类型  1 ldap
     *
     */
    private String loginType ;

    public CustomUserNamePasswdToken(String loginName, String password, String loginType,Boolean unPass) {
        super(loginName,password.toCharArray());
        this.loginType = loginType;
        this.unpass = unPass;
    }
    public CustomUserNamePasswdToken(String loginName, String password, String loginType) {
        super(loginName,password.toCharArray());
        this.loginType = loginType;
    }


    public String getLoginType() {
        return loginType;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean getUnpass() {
        return this.unpass;
    }

    public CustomUserNamePasswdToken(String userName,char[] passwd,String loginType) {
        super(userName,passwd);
        this.loginType = loginType;
    }

}
