package cn.mw.xiangtai.plugin.service;

import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author gengjb
 * @description 祥泰可视化组件获取数据
 * @date 2023/10/19 19:37
 */
public interface XiangtaiVisualizedModuleService {

    /**
     * 根据组件获取祥泰可视化的组件数据
     * @return
     */
    Reply getXiangtaiVisualizedData(XiangtaiVisualizedParam param);
}
