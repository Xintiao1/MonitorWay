package cn.mw.monitor.util;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Map;

@Slf4j
public class HostUtils {
    private static String mwSecurityFile = "hostfile";

    private static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVrjrHLvVFVxjy9oStaDYNItmg9iZBoueF+g4Qwwwk6kOUNHryaIIMFxqHlIdPR7yPSwCp7GsEnPfSEM5CgBl82Rhu2AzHes5yTQ0tFeuugMl0583ijVJbxL3DKlgp9HdBx9weWyt4JWm7apt733lltrSgzhZN01RGDojGaOQCbQIDAQAB";

    private static String PRIVTE_KEY = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJWuOscu9UVXGPL2hK1oNg0i2aD2JkGi54X6DhDDDCTqQ5Q0evJoggwXGoeUh09HvI9LAKnsawSc99IQzkKAGXzZGG7YDMd6znJNDS0V666AyXTnzeKNUlvEvcMqWCn0d0HH3B5bK3glabtqm3vfeWW2tKDOFk3TVEYOiMZo5AJtAgMBAAECgYAk36wpP586us8xo5th4MdYCxrT9W/NQaWJsuVeKb7HJVMKorcbGrXW5qYpemSvGjwQjOiX898VXtoKTQuTBF3Cq6z/gIYJ5uwt0YKO70T1pqnHCwvVM2j8cXyGwJRf+KiDbN0tyFIje9xra4dLR1Dn44J9URKPBvzKJ/ettOPrYQJBAN84oezkZlguVouieZwbpjlj1X2zJirKSRC7o/L8wECycqZAw15Yd4cJ7TR4RpSN13Aw7/CSA2JXze7kDjwfnHkCQQCrqQu7pMf+5l8udGHZrnc/Ta8xvLxdfHwDYqrMLqxszdyaaAzO37jmjbycnxCXbIMG2GnybuYoTccv3fdCEXCVAkEA1yw6OqZ4WWxhlLyLyYWFmDC6LS8yDXFtOSoHgkVN9Y+OoOAw85MHtS5Gb5u5PZ31HHqv8BBwHq5RHoTLi/1U0QJBAKKY/AkgW2RB2DSLfsgPbtqifekRyiaPLHSUeA3xw9dH48bJ+h/WYDDaXqbIMJIN8cqBIVHG4/GXQq4/hN6q4EUCQQC0qwNDG3lcjuYoUE22RYjCQg5WPln//tmlRHdOZH8oR0Aem9l+4yBnQ/Ot9BP23pUydSoDUS5F2B/cM8+j+k6L";
    private static String sn = null;

    private static String secFilePath;

    static{
        secFilePath = System.getProperty("user.dir") + File.separator + ".security" + File.separator + mwSecurityFile;
    }

    public static String getHostMac(InetAddress ia) throws SocketException {
        NetworkInterface byInetAddress = NetworkInterface.getByInetAddress(ia);
        byte[] mac = byInetAddress.getHardwareAddress();
        if(null == mac){
            log.info("getHostMac is null ip:{}" ,ia.getHostAddress());
        }
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i <mac.length ; i++) {
            if(i!=0){
                sb.append("—");
            }
            int temp=mac[i]&0xff;
            String s = Integer.toHexString(temp);
            if(s.length()==1){
                sb.append("0"+s);
            }else{
                sb.append(s);
            }
        }
        return sb.toString().toUpperCase();
    }

    //生成机器码
    public static String getSn(boolean genSecurity) throws Exception {
        if(StringUtils.isNotEmpty(sn)){
            return sn;
        }

        //判断指定文件是否存在,如果存在则读取文件的sn
        if(genSecurity){
            sn = readLocalFile();
            if(null != sn){
                return sn;
            }
        }


        InetAddress localHost = getLocalHostExactAddress();
        String[] split = localHost.toString().split("/");
        String ip = split[1];
        String hostMac = getHostMac(localHost);
        String hostKey=ip+hostMac;
        log.error("获取到的主机IP与mac记录：" + hostKey);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] bytes = hostKey.getBytes("utf-8");
        byte[] digest = md5.digest(bytes);
        StringBuffer hex = new StringBuffer(digest.length * 2);
        for (byte i:digest
        ) {
            if((i&0xFF)<0x10)hex.append("0");
            hex.append(Integer.toHexString(i&0xFF));
        }
        char[] chars = hex.toString().toCharArray();
        String val="";
        for (int i = 0; i <chars.length ; i++) {
            if(i%4==0&&i>0){
                val+="-";
            }
            val+=chars[i];
        }

        if(StringUtils.isNotEmpty(val)){
            sn = val;
            if(genSecurity){
                saveLocalFile(sn);
            }
        }
        return val;
    }

    private static String readLocalFile(){
        File file = new File(secFilePath);
        if(file.exists()){
            try {
                Path path = Paths.get(secFilePath);
                byte[] data = Files.readAllBytes(path);
                byte[] snBytes = RSAUtils.decryptByPrivateKey(data ,PRIVTE_KEY);
                String sn = new String(snBytes ,"utf-8");
                return sn;
            }catch (Exception e){
                log.error("readLocalFile" ,e);
            }
        }

        return null;
    }

    private static void saveLocalFile(String sn){
        try {
            File file = new File(secFilePath);
            createFile(file);
            byte[] snBytes = RSAUtils.encryptByPublicKey(sn.getBytes("utf-8") ,PUBLIC_KEY);
            Path encryptedFile = Paths.get(secFilePath);
            Files.write(encryptedFile, snBytes);
        }catch (Exception e){
            log.error("saveLocalFile" ,e);
        }
    }

    /**
     * 判断文件是否存在，不存在就创建
     * @param file
     */
    private static void createFile(File file) {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                //在上级目录里创建文件
                file.createNewFile();
            } catch (IOException e) {
                log.error("createFile", e);
            }
        }
    }

    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();

            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if(localHost.equals(inetAddr) && null != iface.getHardwareAddress()){
                        return localHost;
                    }

                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            return inetAddr;
                        }

                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }

                    }
                }
            }

            // 如果出去loopback回环地之外无其它地址了，就回退到原始方案
            return candidateAddress == null ? localHost : candidateAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
