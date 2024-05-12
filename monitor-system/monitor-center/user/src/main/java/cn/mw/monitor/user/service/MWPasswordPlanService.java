package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.param.passwdPlan.AddUpdatePasswdPlanParam;
import cn.mw.monitor.api.param.passwdPlan.QueryPasswdPlanParam;
import cn.mw.monitor.api.param.passwdPlan.UpdatePasswdPlanStateParam;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface MWPasswordPlanService {

    /**
     * 添加密码策略
     */
    Reply addPasswordPlan(AddUpdatePasswdPlanParam passwordPlan);
    /**
     * 删除密码策略
     */
    Reply delete(List<Integer> passwdPlanIds);
    /**
     * 修改密码策略状态
     */
    Reply updatePasswdState(UpdatePasswdPlanStateParam uParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;
    /**
     * 更新密码策略
     */
    Reply updatePasswordPlan(AddUpdatePasswdPlanParam passwordPlan);
    /**
     * 分页查询密码策略
     */
    Reply selectList(QueryPasswdPlanParam qParam);
    /**
     * 根据ID获取密码策略信息
     */
    Reply selectPopupById(Integer passwdId);
    /**
     * 密码策略下拉框查询
     */
    Reply selectDropdownList();

    /**
     * 根据用户名获取生效的密码策略
     */
    Reply selectActiveByLoginName(String loginName);
    /**
     * 根据ID获取生效的密码策略
     */
    Reply selectActiveByUserId(Integer id);
    /**
     * 查询密码策略是否存在
     */
    int queryPasswdPlanCount(Integer id);
    Reply selectById(Integer id);
    //    Reply deleteByUserId(List<Integer> idList);

}
