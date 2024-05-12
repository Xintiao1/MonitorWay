package cn.mw.monitor.user.dao;

import cn.mw.monitor.api.param.passwdPlan.UpdatePasswdPlanStateParam;
import cn.mw.monitor.service.user.dto.MwSubUserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.model.MWPasswdPlan;
import cn.mw.monitor.user.dto.MwPasswdPlanDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MWPasswdDAO {

    /**
     * 增加密码策略
     */
    int insert(MWPasswdPlan passwdPlan);

    /**
     * 根据密码策略id查询密码策略名称
     */
    String selectPasswdNameById(@Param("passwdId") Integer passwdId);

    /**
     * 根据密码策略id查询关联用户的数量
     */
    Integer countUserById(@Param("passwdId") Integer passwdId);

    /**
     * 删除密码策略
     */
    int delete(@Param("idList") List<Integer> idList, @Param("modifier") String modifier);

    /**
     * 修改密码策略状态
     */
    int updatePasswdState(UpdatePasswdPlanStateParam uParam);

    /**
     * 查询密码策略
     */
    MWPasswdPlan selectById(@Param("passwdId") Integer passwdId);

    /**
     * 修改密码策略
     */
    int update(MWPasswdPlan passwdPlan);

    /**
     * 分页查找密码策略
     */
    List<MwPasswdPlanDTO> selectList(Map criteria);

    /**
     * 通过ID查询密码策略
     */
    MwPasswdPlanDTO selectPopupById(@Param("passwdId") Integer passwdId);

    /**
     * 密码策略下拉框查询
     */
    List<MWPasswdPlan> selectDropdownList();


    /**
     * 根据用户id查询生效密码策略
     */
    MWPasswdPlan selectActiveByUserId(@Param("userId") Integer userId);

    /**
     * 根据登录名查询生效密码策略
     */
    MWPasswdPlan selectActiveByLoginName(@Param("loginName") String loginName);

    /**
     * 查询密码策略是否存在
     */
    int queryPasswdPlanCount(@Param("id") Integer id);

    int selectByPasswdName(@Param("passwdName") String passwdName);

    /**
     * 根据密码策略ID获取机构数据
     *
     * @param passwordId 密码策略ID
     * @return
     */
    List<OrgDTO> selectOrg(@Param(value = "passwdId") int passwordId);

    /**
     * 根据密码策略ID获取用户数据
     *
     * @param passwordId 密码策略ID
     * @return
     */
    List<MwSubUserDTO> selectUser(@Param(value = "passwdId") int passwordId);

    /**
     * 根据用户ID获取用户所属的机构数据
     *
     * @param userId 用户ID
     * @return
     */
    @Deprecated
    List<OrgDTO> selectUserOrg(@Param(value = "userId") int userId);
}
