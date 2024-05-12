package cn.mwpaas.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author phzhou
 * @ClassName PhoneUtils
 * @CreateDate 2019/3/18
 * @Description
 */
public class PhoneUtils {

    /**
     * 校验手机号
     * @param phone
     * @return
     */
    public static boolean checkMobileNumber(String phone) {
        String regex = "^((13[0-9])|(14[0-9])|(15([0-3]|[5-9]))|(16[2,6,7])|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[1|8|9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            return m.matches();
        }
    }

    /**
     * 隐藏手机号码
     *
     * @param str
     * @return
     */
    public static String hidePhone(String str) {
        try {
            if (str.length() != 11) {
                return str;
            }
            return str.substring(0, str.length() - (str.substring(3)).length())
                    + "****" + str.substring(7);
        } catch (Exception e) {
            return str;
        }
    }

    public static void main(String[] args) {
        ////System.out.println(checkMobileNumber("19188515516"));
    }
}
