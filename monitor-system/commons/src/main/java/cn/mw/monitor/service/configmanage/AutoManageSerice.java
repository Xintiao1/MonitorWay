package cn.mw.monitor.service.configmanage;

import java.util.Map;

/**
 * @author lumingming
 * @createTime 2023504 9:19
 * @description
 */
public interface AutoManageSerice {
    String triggerAuto(Map<String,String> map) throws Exception;
}
