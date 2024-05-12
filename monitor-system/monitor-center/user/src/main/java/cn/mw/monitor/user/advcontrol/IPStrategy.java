package cn.mw.monitor.user.advcontrol;

import cn.mw.monitor.api.common.IpV4Util;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class IPStrategy implements UserControlStra<IPMessage>{
    private String ipRule;
    private  ControlType controlType = ControlType.IP;

    public IPStrategy(String ipRule){
        this.ipRule = ipRule;
    }

    @Override
    public boolean check(IPMessage ipMessage) {
        String ip = ipMessage.getIp();

        //访问标识，默认为false，限制访问
        boolean flag = false;

        if (!ipRule.equals("")) {
            String[] allowIpRanges = ipRule.split(",");
            for (String allowIp : allowIpRanges) {
                Map<String, Object> ipAll = getIpFromString(allowIp);

                // ipSigleIp:单个IP  ipSigle：IP地址段  ipPeriod：IP地址范围
                List<String> ipSigleIp = (List<String>) ipAll.get("ipSigleIp");
                List<String> ipSigle = (List<String>) ipAll.get("ipSigle");
                List<String> ipPeriod = (List<String>) ipAll.get("ipPeriod");

                if (ipPeriod.size() > 1) {
                    String from = ipPeriod.get(0); //起始Ip
                    String to = ipPeriod.get(1);   //结束Ip

                    String share = from.substring(0, from.lastIndexOf(".") + 1);
                    int start = Integer.parseInt(from.substring(from.lastIndexOf(".") + 1, from.length()));
                    int end = Integer.parseInt(to.substring(to.lastIndexOf(".") + 1, to.length()));

                    //获取受检ip的前三段及最后一段
                    String checkIp = ip.substring(0, ip.lastIndexOf(".") + 1);
                    int checkIpLast = Integer.parseInt(ip.substring(ip.lastIndexOf(".") + 1, ip.length()));

                    if ((share.equals(checkIp) && start <= checkIpLast && checkIpLast <= end)) {
                        //ip没有被限制，正常访问
                        flag = true;
                        break;
                    }
                }else {
                    if (ipSigle !=null && ipSigle.size()>0) {
                        if (IpV4Util.isInRange(ip,ipSigle.get(0))) {
                            flag = true;
                            break;
                        }
                    }

                    if (ipSigleIp !=null && ipSigleIp.size()>0) {
                        if (ipSigleIp.contains(ip)) {
                            flag = true;
                            break;
                        }
                    }
                }
            }
        }else {
            flag = true;
        }
        return flag;
    }


    /*
     * 字符串中提取数字 - ：
     * ipSigle  单个ip
     * ipPeriod  ip范围区间
     * */
    public Map<String, Object> getIpFromString(String ipField) {

        Pattern p = Pattern.compile("[^0-9-./]");
        Matcher matcher = p.matcher(ipField);
        String ipFromString = matcher.replaceAll("").trim();
        Map<String, Object> ipAll = new HashMap<>();
        List<String> ipSigleIp = new ArrayList<>();
        List<String> ipSigle = new ArrayList<>();
        List<String> ipPeriod = new ArrayList<>();
        if (!ipFromString.contains("-")) {
            if (!ipFromString.contains("/")) {
                ipSigleIp.add(ipFromString);
            }else {
                ipSigle.add(ipFromString);
            }
        }else {
            ipPeriod = Arrays.asList(ipFromString.split("-"));
        }
        ipAll.put("ipSigleIp", ipSigleIp);
        ipAll.put("ipSigle", ipSigle);
        ipAll.put("ipPeriod", ipPeriod);

        return ipAll;
    }

}
