package cn.mw.xiangtai.plugin.service;

import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;

/**
 * @author gengjb
 * @description 祥泰可视化组件接口
 * @date 2023/10/17 9:35
 */
public interface XiangtaiVisualizedModule {

    int[] getType();

    Object getData(XiangtaiVisualizedParam visualizedParam);
}
