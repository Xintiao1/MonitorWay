package cn.mw.monitor.engineManage.api.param.engineManage;


import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
public class DeleteEngineManageParam {

    private List<String> idList;
    //key值是监控服务器id, value值是监控服务器对应的代理id数组
    private Map<Integer, List<String>> proxyIdList;
}
