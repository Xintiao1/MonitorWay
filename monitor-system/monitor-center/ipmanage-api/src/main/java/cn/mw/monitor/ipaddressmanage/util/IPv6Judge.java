package cn.mw.monitor.ipaddressmanage.util;

import cn.mwpaas.common.utils.StringUtils;
import com.googlecode.ipv6.IPv6Network;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lumingming
 * @createTime 2021062424 9:32
 * @description ipv6地址段操作类
 */
public class IPv6Judge {
    private static final IPv6Judge mUtil=new IPv6Judge();
    //getInstance
    public static IPv6Judge getInstance(){
        return mUtil;
    }

    public  String getFullIPv6(String ipv6){
        //入参为::时，此时全为0
        if (ipv6.equals("::")){
            return "0000:0000:0000:0000:0000:0000:0000:0000";
        }
        //入参已::结尾时，直接在后缀加0
        if (ipv6.endsWith("::")) {
            ipv6 += "0";
        }
        String[] arrs=ipv6.split(":");
        String symbol="::";
        int arrleng=arrs.length;
        while (arrleng<8){
            symbol+=":";
            arrleng++;
        }
        ipv6=ipv6.replace("::",symbol);
        String fullip="";
        for (String ip:ipv6.split(":")){
            while (ip.length()<4){
                ip="0"+ip;
            }
            fullip+=ip+':';
        }
        return fullip.substring(0,fullip.length()-1);
    }

    public  String getShortIPv6(String ipv6){
        String shortIP="";
        ipv6=getFullIPv6(ipv6);
        String[] arr = ipv6.split(":");
        //去掉每组数据前的0
        for (int i = 0; i < arr.length; i++){
            arr[i] = arr[i].replaceAll("^0{1,3}", "");
        }
        //最长的连续0
        String[] arr2 = arr.clone();
        for (int i = 0; i < arr2.length; i++){
            if (!"0".equals(arr2[i])){
                arr2[i] = "-";
            }
        }
        Pattern pattern = Pattern.compile("0{2,}");
        Matcher matcher = pattern.matcher(StringUtils.join(Arrays.asList(arr2), ""));
        String maxStr= "";
        int start = -1;
        int end = -1;
        while (matcher.find()) {
            if(maxStr.length()<matcher.group().length()) {
                maxStr=matcher.group();
                start = matcher.start();
                end = matcher.end();
            }
        }
        // 组合IPv6简写地址
        if(maxStr.length()>0) {
            for (int i = start; i < end; i++){
                arr[i] = ":";
            }
        }
        shortIP = StringUtils.join(Arrays.asList(arr), ":");
        shortIP= shortIP.replaceAll(":{2,}", "::");
        return shortIP;
    }

    public  boolean IpContain(String ipAddresses,String ipAddress){
        IPv6Network network = IPv6Network.fromString(ipAddresses);
        String getip = getFullIPv6(network.getFirst().toLongString());
        String getip2 = getFullIPv6(network.getLast().toLongString());
        String getip3 = getFullIPv6(ipAddress);
        getip = getip.replaceAll(":","");
        getip2 = getip2.replaceAll(":","");
        getip3 = getip3.replaceAll(":","");
        BigInteger integer = new BigInteger(getip,16);
        BigInteger integer2 = new BigInteger(getip2,16);
        BigInteger integer3 = new BigInteger(getip3,16);
        ////System.out.println(integer);
        if((integer3.compareTo(integer)==1||integer3.compareTo(integer)==0)&&integer3.compareTo(integer2)!=1){
            return true;
        }else {
            return false;
        }
    }

    public List<String> Ipv6toBigInteger(String ipAddresses){
        List<String> bigIntegerList = new ArrayList<>();
        IPv6Network network = IPv6Network.fromString(ipAddresses);
        String getip = getFullIPv6(network.getFirst().toLongString());
        String getip2 = getFullIPv6(network.getLast().toLongString());
        getip = getip.replaceAll(":","");
        getip2 = getip2.replaceAll(":","");
        BigInteger integer = new BigInteger(getip,16);
        BigInteger integer2 = new BigInteger(getip2,16);
        bigIntegerList.add(integer.toString());
        bigIntegerList.add(integer2.toString());
        return bigIntegerList;
    }

    public BigInteger Ipv6IptoBigInteger(String ipAddresses){

        String getip = getFullIPv6(ipAddresses);
        getip = getip.replaceAll(":","");
        BigInteger integer = new BigInteger(getip,16);
        return integer;
    }

    public static String getFullIPv6Two(String ipv6){
        //入参为::时，此时全为0
        if (ipv6.equals("::")){
            return "0000:0000:0000:0000:0000:0000:0000:0000";
        }
        //入参已::结尾时，直接在后缀加0
        if (ipv6.endsWith("::")) {
            ipv6 += "0";
        }
        String[] arrs=ipv6.split(":");
        String symbol="::";
        int arrleng=arrs.length;
        while (arrleng<8){
            symbol+=":";
            arrleng++;
        }
        ipv6=ipv6.replace("::",symbol);
        String fullip="";
        for (String ip:ipv6.split(":")){
            while (ip.length()<4){
                ip="0"+ip;
            }
            fullip+=ip+':';
        }
        return fullip.substring(0,fullip.length()-1);
    }

    public static boolean IpContainStatus(String ipAddresses, String ipAddress){
        IPv6Network network = IPv6Network.fromString(ipAddresses);
        String getip = getFullIPv6Two(network.getFirst().toLongString());
        String getip2 = getFullIPv6Two(network.getLast().toLongString());
        String getip3 = getFullIPv6Two(ipAddress);
        getip = getip.replaceAll(":","");
        getip2 = getip2.replaceAll(":","");
        getip3 = getip3.replaceAll(":","");
        BigInteger integer = new BigInteger(getip,16);
        BigInteger integer2 = new BigInteger(getip2,16);
        BigInteger integer3 = new BigInteger(getip3,16);
        ////System.out.println(integer.toString());
        ////System.out.println(integer2.toString());
        if((integer3.compareTo(integer)==1||integer3.compareTo(integer)==0)&&integer3.compareTo(integer2)!=1){
            return true;
        }else {
            return false;
        }
    }



    public static void main(String[] args) {
        IPv6Network network = IPv6Network.fromString("");
    }


}
