package cn.mw.monitor.service.assets.api;

import cn.mwpaas.common.model.Reply;

/**
 * @author gengjb
 * @description 猫维检查模式接口
 * @date 2023/7/19 9:24
 */
public interface MwInspectModeService {

    /**
     * 开启或者关闭检查模式
     * @param enable  是否启用
     * @return
     */
    Reply openOrCloseInspectMode(boolean enable);

    /**
     * 获取当前环境是否是检查模式
     * @return
     */
    boolean getInspectModeInfo();
}
