package cn.mwpaas.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;

/**
 * @author phzhou
 * @ClassName Md5Utils
 * @CreateDate 2019/2/26
 * @Description
 */
@Slf4j
public class Md5Utils {

    private static byte[] md5(String s) {
        MessageDigest algorithm;
        try {
            algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(s.getBytes("UTF-8"));
            byte[] messageDigest = algorithm.digest();
            return messageDigest;
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
        }
        return null;
    }

    private static final String toHex(byte[] hash) {
        if (hash == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++) {
            if ((hash[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString(hash[i] & 0xff, 16));
        }
        return buf.toString();
    }

    /**
     * 将string进行md5编码
     * @param s
     * @return
     */
    public static String encode(String s) {
        try {
            return new String(toHex(md5(s)).getBytes("UTF-8"), "UTF-8");
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
            return s;
        }
    }

}
