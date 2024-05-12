/*
package cn.mw.monitor.credential.util;

import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

import java.nio.charset.StandardCharsets;

*/
/**
 * Created by zy.quaee on 2021/5/31 16:01.
 **//*

public class AESSysCredentialUtil {

    */
/**
     * 随机生成密钥
     *//*

    byte[] key = KeyUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
    */
/**
     * 构建
     *//*

    SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES,key);

    */
/**
     * AES加密
     *//*

    public String encryPasswd(String passwd) {

        //加密密码为16进制表示
        return aes.encryptHex(passwd);
    }

    */
/**
     * 解密
     *//*

    public String decryPasswd(String passwd) {
        return aes.decryptStr(passwd, StandardCharsets.UTF_8);
    }
}
*/
