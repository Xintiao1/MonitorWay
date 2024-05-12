package cn.mw.monitor.smartdisc.common;

import java.util.Date;
import java.util.UUID;

public class UUIDUtil {
    /**
     * 生成唯一ID，UUID在一定程度上可能会重复的，所以拼接了时间戳，保证唯一
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "") + new Date().getTime();
    }
}
