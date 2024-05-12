package cn.mw.monitor.util;

/**
 * @author syt
 * @Date 2021/1/11 16:03
 * @Version 1.0
 */
public class EnDecryptionUtils {

    //位异或密钥
    private static final int KEY = 5;
    /**
     * 位异或加密解密算法
     *
     * @param str 明文（密文）
     * @return 密文（明文）
     */
    public static String entrypt(String str) {
        if (str != null) {
            char[] in = str.toCharArray();
            char[] out = new char[in.length];
            for (int i = 0; i < in.length; i++) {
                out[i] = (char) (in[i] ^ KEY);
            }
            return new String(out);
        }
        return "";
    }
}
