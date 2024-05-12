package cn.mw.monitor.user.common;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MWPasswdCheckUtil {

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

    public MWPasswdCheckUtil(int charTypeNum){
        this.charTypeNum = charTypeNum;
    }

    public Reply checkComplex(String passwd) {
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
            return null;
        }
        return Reply.warn(ErrorConstant.USER_MSG_100118, new String[]{String.valueOf(charTypeNum)});
    }

}
