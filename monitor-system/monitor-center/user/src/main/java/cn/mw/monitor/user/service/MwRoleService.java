package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.param.role.*;
import cn.mw.monitor.service.user.dto.MwRoleDTO;

import java.util.List;

public interface MwRoleService {

    /**
     * 新增角色信息
     */
    Reply insert(AddUpdateRoleParam auParam);
    /**
     * 更新角色信息
     */
    Reply update(AddUpdateRoleParam auParam);
    /**
     * 删除角色信息
     */
    Reply delete(List<Integer> roleIds);
    /**
     * 角色修改状态
     */
    Reply updateRoleState(UpdateRoleStateParam dParam);
    /**
     * 分页查询角色列表信息
     */
    Reply selectList(QueryRoleParam qParam);
    /**
     * 根据角色ID查询角色信息
     */
    Reply selectByRoleId(Integer roleId);
    /**
     * 角色下拉框查询
     */
    Reply selectDorpdownList();
    /**
     * 根据用户ID取角色信息
     */
    MwRoleDTO selectByUserId(Integer userId);
    /**
     * 新增模块信息
     */
    Reply insertRoleModule(AddUpdateModuleParam mParam);

    Reply deleteRoleModule(List<Integer> ids);

    Reply updateRoleModule(AddUpdateModuleParam mParam);

    /**
     * 根据角色ID复制角色数据
     *
     * @param qParam 角色数据
     * @return
     */
    Reply copyRole(QueryRoleParam qParam);
}
