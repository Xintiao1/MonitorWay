package cn.mw.monitor.api.common;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpV6Util {
    /**
     * 功能：根据位数返回IP总数
     * 格式：isIP("2001:db8:a583:64:c68c:d6df:600c:ee9a")
     */
    public static boolean isIP(String ipAddr) {
        StringBuffer sb = new StringBuffer();
        sb.append("(^((([0-9A-Fa-f]{1,4}:){7}(([0-9A-Fa-f]{1,4}){1}|:))")
                .append("|(([0-9A-Fa-f]{1,4}:){6}((:[0-9A-Fa-f]{1,4}){1}|")
                .append("((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|")
                .append("([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|")
                .append("[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|")
                .append("(([0-9A-Fa-f]{1,4}:){5}((:[0-9A-Fa-f]{1,4}){1,2}|")
                .append(":((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|")
                .append("([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|")
                .append("[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|")
                .append("(([0-9A-Fa-f]{1,4}:){4}((:[0-9A-Fa-f]{1,4}){1,3}")
                .append("|:((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|")
                .append("([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|[0-1][0-9][0-9]|")
                .append("([0-9]){1,2})){3})|:))|(([0-9A-Fa-f]{1,4}:){3}((:[0-9A-Fa-f]{1,4}){1,4}|")
                .append(":((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|")
                .append("([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|")
                .append("[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|")
                .append("(([0-9A-Fa-f]{1,4}:){2}((:[0-9A-Fa-f]{1,4}){1,5}|")
                .append(":((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|")
                .append("([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|")
                .append("[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))")
                .append("|(([0-9A-Fa-f]{1,4}:){1}((:[0-9A-Fa-f]{1,4}){1,6}")
                .append("|:((22[0-3]|2[0-1][0-9]|[0-1][0-9][0-9]|")
                .append("([0-9]){1,2})([.](25[0-5]|2[0-4][0-9]|")
                .append("[0-1][0-9][0-9]|([0-9]){1,2})){3})|:))|")
                .append("(:((:[0-9A-Fa-f]{1,4}){1,7}|(:[fF]{4}){0,1}:((22[0-3]|2[0-1][0-9]|")
                .append("[0-1][0-9][0-9]|([0-9]){1,2})")
                .append("([.](25[0-5]|2[0-4][0-9]|[0-1][0-9][0-9]|([0-9]){1,2})){3})|:)))$)");

        if (ipAddr == null) {
            return false;
        }
        ipAddr = Normalizer.normalize(ipAddr, Form.NFKC);
        Pattern pattern = Pattern.compile(sb.toString());
        Matcher matcher = pattern.matcher(ipAddr);

        boolean match = matcher.matches();
        return match;
    }

    public static List<String> parseIpRange(String ipfrom, String ipto, int currentCount, int maxCount) throws Exception {
        List<String> ips = new ArrayList<String>();
        String[] ipfromd = ipfrom.split(":");
        String[] iptod = ipto.split(":");
        int[] int_ipf = new int[8];
        int[] int_ipt = new int[8];
        for (int i = 0; i < 8; i++) {
            int_ipf[i] = Integer.parseInt(ipfromd[i],16);
            int_ipt[i] = Integer.parseInt(iptod[i],16);
        }
        for (int A = int_ipf[0]; A <= int_ipt[0]; A++) {
            for (int B = (A == int_ipf[0] ? int_ipf[1] : 0); B <= (A == int_ipt[0] ? int_ipt[1]
                    : 65535); B++) {
                for (int C = (B == int_ipf[1] ? int_ipf[2] : 0); C <= (B == int_ipt[1] ? int_ipt[2]
                        : 65535); C++) {
                    for (int D = (C == int_ipf[2] ? int_ipf[3] : 0); D <= (C == int_ipt[2] ? int_ipt[3]
                            : 65535); D++) {
                        for (int E = (D == int_ipf[3] ? int_ipf[4] : 0); E <= (D == int_ipt[3] ? int_ipt[4]
                                : 65535); E++) {
                            for (int F = (E == int_ipf[4] ? int_ipf[5] : 0); F <= (E == int_ipt[4] ? int_ipt[5]
                                    : 65535); F++) {
                                for (int G = (F == int_ipf[5] ? int_ipf[6] : 0); G <= (F == int_ipt[5] ? int_ipt[6]
                                        : 65535); G++) {
                                    for (int H = (G == int_ipf[6] ? int_ipf[7] : 0); H <= (G == int_ipt[6] ? int_ipt[7]
                                            : 65535); H++) {
                                        currentCount++;
                                        if(currentCount > maxCount){
                                            throw new IPCountException(maxCount);
                                        }
                                        ips.add(Integer.toString(A,16) + ":" + Integer.toString(B,16) + ":" + Integer.toString(C,16) + ":" + Integer.toString(D,16)
                                                + ":" + Integer.toString(E,16) + ":" + Integer.toString(F,16) + ":" + Integer.toString(G,16) + ":" + Integer.toString(H,16));
                                    }

                                }

                            }

                        }
                    }
                }
            }
        }
        return ips;

    }

    public static List<String> parseIpMaskRange(String s, String s1, int currentCount, int maxCount) {
        return null;
    }
}
