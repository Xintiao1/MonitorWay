package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.param.user.*;
import cn.mw.monitor.api.param.usergroup.AddUpdateGroupParam;
import cn.mw.monitor.api.param.usergroup.QueryGroupParam;
import cn.mw.monitor.api.param.usergroup.UpdateGroupStateParam;

import java.util.List;

public interface MWGroupService {

    final static String GROUPID_SEPERATOR = ",";

    /**
     * 绑定用户
     */
    Reply bindUserGroup(BindUserGroupParam qParam);
    /**
     * 新增用户组信息
     */
    Reply insert(AddUpdateGroupParam ausDTO);
    /**
     * 删除用户组信息
     */
    Reply delete(List<Integer> ids);
    /**
     * 用户组修改状态
     */
    Reply updateGroupState(UpdateGroupStateParam bParam);
    /**
     * 更新用户组信息
     */
    Reply update(AddUpdateGroupParam ausDTO);
    /**
     * 分页查询用户组列表信息
     */
    Reply selectList(QueryGroupParam qsDTO);
    /**
     * 根据用户组ID获取用户组信息
     */
    Reply selectById(Integer groupId);
    /**
     * 用户组下拉框查询
     */
    Reply selectDropdown();
    /**
     * 根据用户名取用户组信息
     */
    Reply selectByLoginName(String loginName);

    /**
     * 获取赛尔移动端用户组列表信息
     */
    Reply selectCernetList(QueryGroupParam param);

    Reply getCernetGroup(QueryGroupParam param);

    /**
     * 获取模糊查询内容
     * @param qParam 请求参数
     * @return 模糊查询列表数据
     */
    Reply getFuzzySearchContent(QueryGroupParam qParam);

    /**
     * 批量查询用户组下所属用户
     * @param groupIds
     * @return
     */
    Reply getUserListByGroupIds(List<Integer> groupIds);
}
