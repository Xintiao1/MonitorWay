package cn.mw.monitor.service.user.api;

import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface MWGroupCommonService {

    /**
     * 根据用户组ID获取用户组关联用户
     */
    Reply selectGroupUser(Integer groupId);

    Reply selectGroupUsers(List<Integer> groupIds);
}
