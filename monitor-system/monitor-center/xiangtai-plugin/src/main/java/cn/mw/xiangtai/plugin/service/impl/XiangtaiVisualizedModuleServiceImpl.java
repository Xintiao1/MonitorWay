package cn.mw.xiangtai.plugin.service.impl;

import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModuleService;
import cn.mw.xiangtai.plugin.service.impl.manager.XiangtaiVisualizedModuleManager;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gengjb
 * @description 祥泰可视化组件获取数据
 * @date 2023/10/19 19:38
 */
@Service
@Slf4j
public class XiangtaiVisualizedModuleServiceImpl implements XiangtaiVisualizedModuleService {

    @Autowired
    private XiangtaiVisualizedModuleManager visualizedModuleManager;

    /**
     * 祥泰可视化大屏数据获取
     * @param param
     * @return
     */
    @Override
    public Reply getXiangtaiVisualizedData(XiangtaiVisualizedParam param) {
        try {
            Object data = visualizedModuleManager.getDataByType(param.getChartType(), param);
            return Reply.ok(data);
        }catch (Throwable e){
            log.error("XiangtaiVisualizedModuleServiceImpl{} getXiangtaiVisualizedData() ERROR::",e);
            return Reply.fail("XiangtaiVisualizedModuleServiceImpl{} getXiangtaiVisualizedData() ERROR::",e);
        }
    }
}
