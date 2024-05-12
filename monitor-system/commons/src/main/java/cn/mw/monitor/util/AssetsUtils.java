package cn.mw.monitor.util;

import cn.mw.monitor.api.common.Constants;

import java.util.Date;

public class AssetsUtils {
    //一天的分钟数
    private static final int DAY_LEN = 1440;
    public static String genGroupKey(Integer assetType, Integer serverId){
        return serverId.toString() + "-" + assetType.toString();
    }

    public static String scanRuleTime(Date scanStartTime, Date scanEndTime){
        if(null == scanEndTime){
            return Constants.UNKNOWN;
        }
        long period = scanEndTime.getTime() - scanStartTime.getTime();
        int length = (int)(period / (1000 * 60));

        if (length <= 0){
            length = (int)(period / 1000);
            return (length + "秒");
        }

        if(length >= DAY_LEN){
            int days = (int) (length / DAY_LEN);
            int minutes = length % DAY_LEN;
            return (days + "天" + minutes + "分钟");
        }
        return (length + "分钟");
    }
}
