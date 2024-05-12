package cn.mw.monitor.service.user.api;

public interface MWUserOrgCommonService {

    String getRolePermByUserId(Integer userId);

    String getRoleIdByLoginName(String loginName);

}
