package cn.mw.monitor.service.user.api;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.state.DataType;

import java.util.List;

public interface MWUserGroupCommonService {

    List<Integer> getGroupIdByLoginName(String loginName);

    String getUserIdIdByLoginName(String loginName);

    List<String> getWxOpenId(String loginName);

    /**
     * 获取所有的用户组信息
     *
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 所有的用户组数据
     */
    List<GroupDTO> getAllGroupList(int typeId, DataType dataType);

    List<String> getGroupnamesByids(List<Integer> groupIds);
}
