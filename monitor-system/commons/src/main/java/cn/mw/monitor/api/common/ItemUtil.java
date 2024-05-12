package cn.mw.monitor.api.common;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/5/4 12:43
 */
public class ItemUtil {
    public static final Map<Integer, String> DISKITEMNAME = new HashMap<>();
    public static final Map<Integer, String> METITEMNAME = new HashMap<>();

    static {
        DISKITEMNAME.put(0, "MW_DISK_UTILIZATION");
        DISKITEMNAME.put(1, "MW_DISK_USED");
        DISKITEMNAME.put(2, "MW_DISK_FREE");
        DISKITEMNAME.put(3, "MW_DISK_TOTAL");

        METITEMNAME.put(0, "MW_INTERFACE_NAME");
        METITEMNAME.put(1, "MW_INTERFACE_IN_TRAFFIC");
        METITEMNAME.put(2, "MW_INTERFACE_OUT_TRAFFIC");
        METITEMNAME.put(3, "MW_INTERFACE_OUT_DROPPED");
        METITEMNAME.put(4, "MW_INTERFACE_IN_DROPPED");
        METITEMNAME.put(5, "MW_INTERFACE_SPEED");
        METITEMNAME.put(6, "MW_INTERFACE_STATUS");
    }

    public static Long getStartTime(Integer dataType, Calendar calendar) {
        Long startTime = 0L;
        if (dataType == 1) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
            startTime = calendar.getTimeInMillis() / 1000L;
        } else if (dataType == 2) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
            startTime = calendar.getTimeInMillis() / 1000L;
        } else if (dataType == 3) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.WEEK_OF_YEAR) - 1);
            startTime = calendar.getTimeInMillis() / 1000L;
        } else if (dataType == 4) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.MONTH) - 1);
            startTime = calendar.getTimeInMillis() / 1000L;
        }
        return startTime;
    }


}
