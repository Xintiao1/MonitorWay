package cn.mw.monitor.plugin.user;

import cn.mw.monitor.bean.TimeTaskRresult;

/**
 * @author guiquanwnag
 * @datetime 2023/8/23
 * @Description 用户插件
 */
public interface UserPlugin {

    /**
     * 存储用户信息
     */
    TimeTaskRresult saveLdapUser();

}
