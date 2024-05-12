package cn.mw.monitor.assets.service.impl;

import cn.mw.zbx.MWTPServerAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author syt
 * @Date 2021/6/3 7:21
 * @Version 1.0
 */
@Component
public class MwAssetsAPIManager {
    @Autowired
    private MWTPServerAPI mwtpServerAPI;

}
