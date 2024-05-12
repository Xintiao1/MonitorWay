package cn.mw.monitor.model.util;

import com.googlecode.ipv6.IPv6Network;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

/**
 * @author qzg
 * @date 2023/3/25
 */
public class ModelIPUtil {
    /**
     * 判断IPV4是否在指定范围
     * @param ipStart
     * @param ipEnd
     * @param ip
     * @return
     */
    public static boolean ipIsValid(String ipStart,String ipEnd, String ip) throws Exception {
        if (StringUtils.isEmpty(ipStart)) {
            throw new Exception("起始IP不能为空！");
        }
        if (StringUtils.isEmpty(ipEnd)) {
            throw new Exception("结束IP不能为空！");
        }
        if (StringUtils.isEmpty(ip)) {
            throw new Exception("IP不能为空！");
        }
        ipStart = ipStart.trim();
        ipEnd = ipEnd.trim();
        ip = ip.trim();
        final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
        final String REGX_IPB = REGX_IP + "\\-" + REGX_IP;
        if (!ipStart.matches(REGX_IP) || !ip.matches(REGX_IP) || !ipEnd.matches(REGX_IP)) {
            return false;
        }
        String[] sips = ipStart.split("\\.");
        String[] sipe = ipEnd.split("\\.");
        String[] sipt = ip.split("\\.");
        long ips = 0L, ipe = 0L, ipt = 0L;
        for (int i = 0; i < 4; ++i) {
            ips = ips << 8 | Integer.parseInt(sips[i]);
            ipe = ipe << 8 | Integer.parseInt(sipe[i]);
            ipt = ipt << 8 | Integer.parseInt(sipt[i]);
        }
        if (ips > ipe) {
            long t = ips;
            ips = ipe;
            ipe = t;
        }
        return ips <= ipt && ipt <= ipe;
    }

    public static boolean IpContain(String ipAddresses, String ipAddress){
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

    public static BigInteger Ipv6IptoBigInteger(String ipAddresses){

        String getip = getFullIPv6(ipAddresses);
        getip = getip.replaceAll(":","");
        BigInteger integer = new BigInteger(getip,16);
        return integer;
    }

    public static String getFullIPv6(String ipv6){
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

}
