package cn.mw.monitor.model.util;

import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qzg
 * @date 2023/4/20
 */
public class MatcherUtils {
    public static Long getNumByStr(String str) {
        Long intNum = 0l;
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String strNum = m.replaceAll("").trim();
        if (!Strings.isNullOrEmpty(strNum)) {
            intNum = Long.valueOf(strNum);
        }
        return intNum;
    }
}
