package cn.mw.monitor.user.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.user.service.PasswdComCheck;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PasswdComCheckImpl implements PasswdComCheck {

    private int charTypeNum;

    private static List<Pattern> patterns;

    private static String[] regs = new String[]{".*[a-z].*",".*[A-Z].*",".*\\d+.*"};

    private static String[] replaceRegs = new String[]{"[a-z]+","[A-Z]+","[\\d]"};

    static {
        patterns = new ArrayList<Pattern>();
        for (String reg : regs) {
            patterns.add(Pattern.compile(reg));
        }
    }

    @Override
    public List<Reply> checkComplex(String passwd) {
        List<Reply> faillist = new ArrayList<Reply>();
        int check = 0;
        int i = 0;
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(passwd);
            if (matcher.matches()) {
                passwd = passwd.replaceAll(replaceRegs[i],"");
                check++;
            }
            if ("".equals(passwd)) {
                break;
            }
            i++;
        }
        if (passwd.length() > 0) {
            check++;
        }
        if (0 == this.charTypeNum || check >= this.charTypeNum) {
            return faillist;
        }
        faillist.add(Reply.warn(ErrorConstant.USER_MSG_100118, new String[]{String.valueOf(charTypeNum)}));
        return faillist;
    }

    public PasswdComCheckImpl(int charTypeNum){
        this.charTypeNum = charTypeNum;
    }

    /*
    public static void  main(String[] args){
        PasswdComCheck passwdComCheck = new PasswdComCheckImpl(3);
        String test = "12a3r23$ASD234E234E&";
        passwdComCheck.checkComplex(test);
    }
     */
}
