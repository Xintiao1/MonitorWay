package cn.mw.monitor.plugin.visualized;

import cn.mw.monitor.bean.TimeTaskRresult;

/**
 * @author gengjb
 * @description TODO
 * @date 2023/9/13 16:49
 */
public interface VisualizedPlugin {

    /**
     * 存储缓存数据
     * @return
     */
    TimeTaskRresult saveCaheData();
}
