package cn.mw.monitor.service.user.listener;


import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.dto.MwLoginUserDto;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.service.user.model.MwRoleModulePermMapper;

import java.util.List;

public interface ILoginCacheInfo {

    LoginContext getCacheInfo(String loginName);

    LoginContext getThreadLocalInfo();

    String getNameTokenMap(String token);

    String getLoginName();

    String getRoleId(String loginName);

    MwRoleDTO getRoleInfo();

    MwRoleModulePermMapper getMwRoleModulePermMapper(String key, String loginName);

    /**
     * 启动一个本地线程存储用户信息
     * @param userDto
     */
    void createLocalTread(MwLoginUserDto userDto);

    /**
     * 获取本地线程存储的用户信息
     * @return
     */
    MwLoginUserDto getLocalTread();

    /**
     * 删除本地线程
     */
    void removeLocalTread();


    /**
     * 创建定时任务用户
     * @return
     */
    void createTimeTaskUser(MwLoginUserDto mwLoginUserDto);

    /**
     * 获取定时任务用户
     * @return
     */
    MwLoginUserDto getTimeTaskUser();

    /**
     * 删除定时任务用户
     */
    void removeTimeTaskUser();

    List<MWOrgDTO> getLoginOrg();
}
