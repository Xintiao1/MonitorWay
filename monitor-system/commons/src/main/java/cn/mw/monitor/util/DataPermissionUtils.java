package cn.mw.monitor.util;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.bean.DataPermissionDTO;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限工具类
 * @author zy
 * @date 2021/7/8 14:24
 **/
public class DataPermissionUtils {

    public static DataPermissionDTO getDataByPermission() {
        ILoginCacheInfo iLoginCacheInfo = SpringUtils.getBean(ILoginCacheInfo.class);
        MWOrgCommonService mwOrgService =  SpringUtils.getBean(MWOrgCommonService.class);
        MWUserOrgCommonService mwUserOrgCommonService = SpringUtils.getBean(MWUserOrgCommonService.class);
        MWUserGroupCommonService mwUserGroupCommonService = SpringUtils.getBean(MWUserGroupCommonService.class);

        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
        List<Integer> orgIds = mwOrgService.getOrgIdsByNodes(loginName);

        DataPermissionDTO data = null;
        boolean topRoleRight = roleId.equals(MWUtils.ROLE_TOP_ID);

        if (topRoleRight) {
            data = DataPermissionDTO.builder()
                    .userId(userId)
                    .loginName(loginName)
                    .groupIds(groupIds)
                    .dataPermission(dataPermission)
                    .adminRoleRight(true).build();
        } else {
            data = processingDataPermission(dataPermission, groupIds, userId, orgIds,loginName);
        }
        return data;
    }

    /**
     * 处理共享私有数据权限问题
     */
    public static DataPermissionDTO processingDataPermission(DataPermission permission, List<Integer> groupIds,
                                                             Integer userId, List<Integer> orgIds,String loginName) {

        List<Integer> realOrgList = new ArrayList<>();

        if (permission.equals(DataPermission.PUBLIC)) {
            //非管理且共享
            realOrgList.addAll(orgIds);
        }

        return DataPermissionDTO.builder()
                .userId(userId)
                .loginName(loginName)
                .groupIds(groupIds)
                .orgIds(realOrgList)
                .adminRoleRight(false)
                .dataPermission(permission).build();
    }

}
