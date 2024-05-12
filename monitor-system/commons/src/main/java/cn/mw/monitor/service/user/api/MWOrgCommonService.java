package cn.mw.monitor.service.user.api;

import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.state.DataType;

import java.util.List;

public interface MWOrgCommonService {

    /**
     * 查询机构所有公有权限的用户
     */
    List<Integer> selectPubUserIdByOrgId(List<Integer> orgIds);

    /**
     * 根据用户登录名称查询机构
     */
    List<Integer> getOrgIdsByNodes(String loginName);

    List<String> getOrgNamesByNodes(String loginName);

    /**
     * 根据用户登录名称查询机构
     *
     * @param loginName 登录名称
     * @return 所有组织机构ID列表
     */
    List<Integer> getAllOrgIdsByName(String loginName);

    /**
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 机构列表
     */
    List<OrgDTO> getAllOrgList(int typeId, DataType dataType);

    List<String> getOrgnamesByids(List<Integer> organizes);
}
