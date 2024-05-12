package cn.mw.monitor.plugin.alert;

import java.util.List;

public interface AlertPlugin {
    /**
     * @param wechatIdLits 企业微信号ID
     * @return 用户名
     *
     **/
    List<String> getUserNameByQyWechatId(String[] wechatIdLits);
}
