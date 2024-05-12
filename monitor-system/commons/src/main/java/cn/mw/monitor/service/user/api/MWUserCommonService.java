package cn.mw.monitor.service.user.api;

import cn.mw.monitor.service.user.model.MWUser;
import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface MWUserCommonService {

    Reply selectByUserId(Integer userId);

    String getRolePermByUserId(Integer userId);

    String getLoginNameByUserId(Integer userId);

    Integer getAdmin();

    List<MWUser> selectByStringGroup(String type, List<Integer> ids);

    List<String> getLoginNameByUserIds(List<Integer> userIds);

    List<Integer> selectAllUserId();
}
