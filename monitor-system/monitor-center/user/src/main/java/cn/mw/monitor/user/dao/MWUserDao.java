package cn.mw.monitor.user.dao;

import cn.mw.monitor.api.param.user.UpdateUserStateParam;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.user.dto.GroupDTO;
import cn.mw.monitor.user.dto.MwUserDTO;
import cn.mw.monitor.user.model.MWPasswdInform;
import cn.mw.monitor.user.model.MwUserControl;
import cn.mw.monitor.user.model.MwUserRoleMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dev on 2020/2/11.
 */
public interface MWUserDao {

    /**
     * 新增用户
     *
     * @param user
     * @return
     */
    Integer insert(MWUser user);

    /**
     * 删除用户
     *
     * @param userIds 用户ID
     * @return
     */
    Integer delete(List<Integer> userIds);

    /**
     * 修改用户
     *
     * @param user 用户信息
     * @return
     */
    Integer update(MWUser user);

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return
     */
    Integer updateState(MWUser user);

    /**
     * 根据用户名查询用户信息
     *
     * @param loginName 用户名
     * @return
     */
    UserDTO selectByLoginName(String loginName);

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return
     */
    MWUser selectByUserId(Integer userId);

    /**
     * 根据用户微信openID查询用户信息
     *
     * @param openId 用户ID
     * @return
     */
    UserDTO selectByOpenid(String openId);

    /**
     * 根据用户ID查询用户信息
     *
     * @param criteria
     * @return
     */
    List<MwUserDTO> selectResponser(Map criteria);

    /**
     * 根据机构及子机构, 用户组，查询用户信息
     *
     * @param map 查询参数
     * @return
     */
    @Deprecated
    List<MWUser> selectListByPerm(Map map);

    /**
     * 根据用户ID查询用户信息
     */
    MwUserDTO selectById(Integer userId);

    /**
     * 查询用户列表
     *
     * @param criteria
     * @return
     */
    List<MwUserDTO> selectList(Map criteria);

    /**
     * 根据机构ID查询用户
     *
     * @param orgId
     * @return
     */
    @Deprecated
    List<MWUser> getUserByOrgId(Integer orgId);

    /**
     * 新增用户角色映射
     *
     * @param userRoleMap
     * @return
     */
    @Deprecated
    int insertUserRoleMapper(MwUserRoleMap userRoleMap);

    List<MwUserDTO> selectDropdown(@Param("loginName") String loginName,
                                   @Param("groupIds") List<Integer> groupIds,
                                   @Param("perm") String perm,
                                   @Param("nodes") List<String> nodes);

    /**
     *
     * @param userIdList
     * @return
     */
    List<MwUserDTO> selectDropdownByIdList(@Param("list") List<Integer> userIdList);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    int updateUserState(UpdateUserStateParam updateUserStateParam);

    @Deprecated
    int deteleUserRoleMapper(List<Integer> userIds);

    int updateUserRoleMapper(MwUserRoleMap userRoleMap);

    List<UserDTO> getUserByName(String loginName);

    @Deprecated
    int deleteUserAssetsMapper(List<Integer> userIds);

    @Deprecated
    int deleteUserMonitorMapper(List<Integer> idList);

    /**
     * 私有权限用户登录所选的负责人用户下拉列表的集合
     * @param loginName
     * @return
     */
    Set<String> getPrivateUserByLoginName(String loginName);
    /**
     * 公有权限用户登录所选的负责人用户下拉列表的集合
     * @param loginName
     * @return
     */
    @Deprecated
    List<String> getPublicUserByLoginName(String loginName);

    /**
     * 获得用户的权限信息 PRIVATE/PUBLIC
     * @param userId
     * @return
     */
    String getRolePermByUserId(Integer userId);

    String getLoginNameByUserId(Integer userId);


    int deleteUserActionMapper(List<Integer> idList);

    int deleteUserMapper(List<Integer> idList);

    MwUserDTO selectCurrUserInfo(Integer userId);

    Integer insertUserControlAction(@Param("userId") Integer userId, @Param("cond") String cond, @Param("operation") String operation);

    Integer insertUserControl(@Param("userId") Integer userId, @Param("controlTypeId") Integer controlTypeId, @Param("rule") String rule);

    Integer updateUserControlAction(@Param("userId") Integer userId, @Param("cond") String cond, @Param("operation") String operation);

    Integer updateUserControl(@Param("userId") Integer userId, @Param("controlTypeId") Integer controlTypeId, @Param("rule") String rule);

    Integer updateUserOpenId(Integer userId, String openId);

    List<MwUserControl> selectUserControlByUserId(Integer userId);

    Integer delUserControl(@Param("userId") Integer userId, @Param("controlTypeId") Integer controlTypeId);

    Integer selectCountUserControl(@Param("userId") Integer userId, @Param("controlTypeId") Integer controlTypeId);

    Integer selectUserControlAction(Integer userId);
    //记录新增用户密码策略信息
    Integer insertInform(MWUser mwUser);


     //从密码策略临时表查询密码策略信息
    MWPasswdInform selectInformByUserId(@Param("userId") Integer userId);

    Integer updateInform(@Param("userId") Integer userId);

    @Deprecated
    Integer updateInformPasswdPlan(MWUser newMwUser);

    @Deprecated
    Integer selectADUserByLoginName(@Param("loginName") String loginName, @Param("userType") String userType);

    UserDTO selectADUserByType(@Param("loginName") String loginName, @Param("userType") String userType);

    List<Integer> selectAllUserId();

    /**
     * 通过用户ID列表获取AD用户数据
     * @param idList 用户数据
     * @return
     */
    List<String> selectADUsersNameByIds(List<Integer> idList);

    List<MwUserDTO> selectAllUserList(Map criteria);

    /**
     * 获取所有的用户列表
     *
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 获取所有的用户列表
     */
    List<cn.mw.monitor.service.assets.model.UserDTO> getAllUserList(@Param(value = "typeId") int typeId,
                                                                    @Param(value = "dataType") String dataType);

    List<Integer> getAdmin();

    List<MWUser> selectByStringGroup(@Param(value = "type") String type, @Param(value = "ids") List<Integer> ids);

    List<String> getLoginNameByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * 获取用户ID
     * @param orgId 机构ID
     * @return
     */
    List<Integer> listUserIdByParams(@Param(value = "orgId") int orgId);

    /**
     * 根据用户ID获取用户组数据
     * @param userId 用户ID
     * @return
     */
    List<GroupDTO> selectGroup(@Param(value = "userId") int userId);

    void deleteUserSubModelSystem(@Param(value = "userId") Integer userId);

    void deleteUserSubRuleId(@Param(value = "userId") Integer userId);

    void insertUserSubModelSystem(@Param(value = "userId") Integer userId,@Param(value = "list") List<String> param);

    void insertUserSubRuleId(@Param(value = "userId") Integer userId,@Param(value = "list") List<String> param);

    List<String> selectUserSubRuleId(@Param(value = "userId") Integer userId);

    List<String> selectUserSubModelSystem(@Param(value = "userId") Integer userId);

}
