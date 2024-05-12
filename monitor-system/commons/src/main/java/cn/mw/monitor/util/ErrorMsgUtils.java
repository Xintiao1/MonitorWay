package cn.mw.monitor.util;

import cn.mwpaas.common.utils.StringUtils;

import java.util.Iterator;
import java.util.Map;

public class ErrorMsgUtils {

    public static String getErrorMsg(Map<String, StringBuffer> map) {
        StringBuffer msg = new StringBuffer();
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (StringUtils.isNotEmpty(msg)) {
                msg.append("；");
            }
            msg.append(entry.getKey())
                    .append("【")
                    .append(entry.getValue().toString().substring(1))
                    .append("】");
        }
        return msg.toString();
    }

}
