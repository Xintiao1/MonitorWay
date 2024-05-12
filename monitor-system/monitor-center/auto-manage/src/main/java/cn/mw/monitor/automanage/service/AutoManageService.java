package cn.mw.monitor.automanage.service;

import cn.mw.monitor.automanage.param.AutoManageParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author gui.quanwang
 * @className AutoManageService
 * @description 自动化运维服务
 * @date 2022/4/2
 */
public interface AutoManageService {

    /**
     * 获取服务列表
     *
     * @return
     */
    Reply getServerList();

    /**
     * 获取自动化参数
     *
     * @param param 请求参数
     * @return
     */
    Reply getAutoManageList(AutoManageParam param);

    /**
     * 根据服务名称搜索子服务
     *
     * @param serverName 服务名称
     */
    Reply searchServerInstance(String serverName);

    /**
     * 更新实例状态
     *
     * @param id     实例ID
     * @param enable true:上线  false:下线
     */
    Reply updateServerInstance(int id, boolean enable);
}
