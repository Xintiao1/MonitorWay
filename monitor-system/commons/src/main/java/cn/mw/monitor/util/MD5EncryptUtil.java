package cn.mw.monitor.util;

import org.apache.commons.codec.Charsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MD5EncryptUtil {

    public static String sign(String strSrc) {
        // 进行Base64编码
        return Base64.getEncoder().encodeToString(encrypt(strSrc).getBytes(Charsets.UTF_8));
    }

    /**
     * Md5加密
     */
    public static String encrypt(String strSrc) {
        String strDes = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] bt = strSrc.getBytes();
            md.update(bt);
            strDes = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
        }
        return strDes;
    }
    private static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

}
