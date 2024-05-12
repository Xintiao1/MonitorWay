package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.param.org.*;
import cn.mw.monitor.service.user.dto.MWOrgDTO;

import java.util.List;
import java.util.Map;

public interface MWOrgService {

    /**
     * 绑定用户
     */
    Reply bindUserOrg(BindUserOrgParam qParam);

    /**
     * 新增机构信息
     */
    Reply insert(AddUpdateOrgParam aParam);

    /**
     * 新增机构信息
     * @param aParam
     * @return orgId
     */
    int insertOrg(AddUpdateOrgParam aParam);

    /**
     * 删除机构信息
     */
    Reply delete(List<Integer> orgIds);

    /**
     * 更新机构状态信息
     */
    Reply updateOrgState(UpdateOrgStateParam bParam);

    /**
     * 更新机构信息
     */
    Reply update(AddUpdateOrgParam uParam);

    /**
     * 查询机构列表
     */
    Reply selectList(QueryOrgParam qParam);

    /**
     * 根据机构ID取机构信息
     */
    Reply selectByOrgId(Integer orgId);

    /**
     * 机构下拉框查询
     */
    Reply selectDorpdownList(QueryOrgForDropDown qParam);

    /**
     * 根据数据权限,查询机构列表
     */
    Reply selectListFilterByPerm(QueryOrgParam qParam);


    /**
     * 根据用户查询所在机构
     */
//    Reply selectListByUserId(Integer userId);

    /**
     * 根据用户名查询所在机构
     */
    List<MWOrgDTO> selectByLoginName(String loginName);

    Reply getOrgList(QueryOrgParam qParam);

    Reply selectCernetList(QueryOrgParam qParam);

//    Reply selectByLoginName();

    /**
     * 批量查询组织信息
     *
     * @param qParam 查询组织参数
     * @return
     */
    Reply batchQueryOrg(QueryOrgParam qParam);

    /**
     * 根据组织ID获取组织数据
     *
     * @param orgId 组织ID
     * @return 组织数据
     */
    MWOrgDTO getByOrgId(int orgId);

    /**
     * 根据资产ID获取组织信息
     *
     * @param assetsId 资产id
     * @return 组织数据
     */
    List<MWOrgDTO> selectByAssetsId(String assetsId);

    /**
     * 获取所有的组织列表数据
     *
     * @return 所有组织列表数据
     */
    List<MWOrgDTO> getAllOrgList();

    /**
     * 获取机构的经纬度数据
     * @param qParam 查询对应地址的经纬度
     * @return
     */
    Reply getOrgLongitudeDropDown(QueryOrgParam qParam);

    /**
     * 业务模块关联的机构信息
     * @param map ids关联业务模块id, moduleType业务模块标识
     * @return
     */
    Reply selectOrgByParamsAndIds(Map map);
    Reply selectOrgMapByParamsAndIds(Map map);


    /**
     * 批量查询用户组下所属用户
     * @param orgIds
     * @return
     */
    Reply getUserListByOrgIds(List<Integer> orgIds);
}
