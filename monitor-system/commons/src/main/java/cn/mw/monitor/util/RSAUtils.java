package cn.mw.monitor.util;

import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAUtils {

    /**
     * 加密公钥
     */
    public final static String RSA_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC7o3kArWblfxjJ8fY9Vm83cuqxiH07Yu0jNTE4TFV+ETRwz02lvA/Fi6TLQRBBJgTsnl6bhbnssDG1yILK/v9lZoNLo3rCgw3Pg28i3lKWU3KC8sNBiHJSSikQ7GAiSSbSBy3ihYbMoIjC9AwHpsgz9yjQM4qeNiZstLiM9dfF/QIDAQAB";

    /**
     * 解密私钥
     */
    public final static String RSA_PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALujeQCtZuV/GMnx9j1Wbzdy6rGIfTti7SM1MThMVX4RNHDPTaW8D8WLpMtBEEEmBOyeXpuFueywMbXIgsr+/2Vmg0ujesKDDc+DbyLeUpZTcoLyw0GIclJKKRDsYCJJJtIHLeKFhsygiML0DAemyDP3KNAzip42Jmy0uIz118X9AgMBAAECgYBwmM1Awc1wRA99B4xrDFYa4QPC2xPDMj75FK9fiDb5OpvMYinHHY4dTgnqyjVV0tm7n+FH1DxYsvZxjkwmQlTA2+1kcsKK3HJbayFlw77rCoHnFYFzWLUCOi/1zEIUAzZDEc82HGNoMhgRUwZuvEWIz26otgJQbnJ1Zi7eFwmlpQJBAPu4hJhyZVHgnSju5Q/goTBY5jt9DWnl/PFMJrtde5KdizcDDubetbnuHkInIiev5oaabQo5JGkMVqoPkwVArEsCQQC+1BIXI/Qv4uDbja2CR39RiGgrG5LdSbPMjIkKuol2v2Ubpl8Ta8m20OJskb0nAZ3EdFKBdQB8GR3/pOtLQ1nXAkBZktKUUPjfBvfF7iTS3pNdWfUrrBdO3MmCsB+EQwLLLIayn8L4wr2WGTbpQXW0+7IjqeYtYiIjw4E/aOVfVCE7AkBfE/CzgOFwXn0kRxHVIGRqLimgmMty3/7qgnbhVpGZDcGFpj4mwgStxqOZ7n8tCRwfPsnA4JfOPQF6VyQ4yqTfAkBNLG33fHMNiyxJAHZVQswsJOUrBOpK8ZvTNsDxWxWuuMGApQMuhyIiQeOU4HM5e9stUinmurbxK0UmVqpRH0sn";


    public static final String KEY_ALGORITHM = "RSA";

    public static final String PUBLIC_KEY = "RSAPublicKey";

    public static final String PRIVATE_KEY = "RSAPrivateKey";

    public static final int MAX_ENCRYPT_BLOCK = 117;

    public static final int MAX_DECRYPT_BLOCK = 128;

    public static final int INITIALIZE_LENGHT = 1024;

    public static Map<String, Object> getKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(INITIALIZE_LENGHT);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    public static String getPublicKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return Base64.encodeBase64String(key.getEncoded());
    }

    public static String getPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return Base64.encodeBase64String(key.getEncoded());
    }

    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publick = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publick);
        int inputLength = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        while (inputLength - offset > 0) {
            if (inputLength - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offset, inputLength - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privatek = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privatek);
        int inputLength = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        while (inputLength - offset > 0) {
            if (inputLength - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offset, inputLength - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publick = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publick);
        int inputLength = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        while (inputLength - offset > 0) {
            if (inputLength - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offset, inputLength - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privatek = keyFactory.generatePrivate(pkcs8KeySpec);
        //  RSA/ECB/PKCS1Padding
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privatek);
        int inputLength = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        while (inputLength - offset > 0) {
            if (inputLength - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offset, inputLength - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    public static String encryptData(String data, String publicKey) {
        try {
            return Base64.encodeBase64String(encryptByPublicKey(data.getBytes(), publicKey));
        } catch (Exception e) {
            return null;
        }
    }

    public static String decryptData(String encryptData, String privateKey) {
        try {
            return new String(decryptByPrivateKey(Base64.decodeBase64(encryptData), privateKey),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decryptDataBytes(String encryptData, String privateKey) {
        try {
            return decryptByPrivateKey(Base64.decodeBase64(encryptData), privateKey);
        } catch (Exception e) {
            return null;
        }
    }
//RSA/ECB/PKCS1Padding


    public static void main(String[] args) throws Exception {

        Map map = RSAUtils.getKeyPair();
        String pub = RSAUtils.getPublicKey(map);
        String pri = RSAUtils.getPrivateKey(map);

        String test = "hello world";

        byte[] data = encryptByPublicKey(test.getBytes("utf-8") ,pub);
        Path encryptedFile = Paths.get("encrypted_data");
        Files.write(encryptedFile, data);

        byte[] data1 = Files.readAllBytes(encryptedFile);
        byte[] decData = decryptByPrivateKey(data1 ,pri);
        String result = new String(decData ,"utf-8");
        System.out.println(result);
        //System.out.println(encryptData);
//        String encryptData = RSAUtils.encryptData("Dev20$uiyD7", RSA_PUBLIC_KEY);
//        //System.out.println(encryptData);
//        String decryptData = RSAUtils.decryptData(encryptData, RSA_PRIVATE_KEY);
//        //System.out.println(decryptData);
    }

}
