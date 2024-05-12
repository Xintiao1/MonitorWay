package cn.mw.monitor.user.dao;

import cn.mw.monitor.api.param.aduser.AdCommonParam;
import cn.mw.monitor.api.param.aduser.QueryADInfoParam;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.user.dto.MwLdapAuthenticInfoDTO;
import cn.mw.monitor.user.dto.MwTempConfigDTO;
import cn.mw.monitor.user.dto.MwTempUserDTO;
import cn.mw.monitor.user.model.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zy.quaee on 2021/4/28 9:25.
 **/
public interface MWADUserDao {


    /**
     * 增加AD用户
     *
     * @param mwUser
     */
    void insertUser(MWUser mwUser);

    /**
     * 增加外部认证匹配关联信息
     *
     * @param ma
     */
    void insertInfo(MWDomainInfoDTO ma);

    /**
     * 获取匹配关联信息
     *
     * @param param
     * @return
     */
    List<MWADInfoDTO> selectConfig(QueryADInfoParam param);

    /**
     * 删除匹配信息
     *
     * @param id
     */
    void deleteConfig(Integer id);

    /**
     * 删除用户信息
     *
     * @param loginName 登录名称
     * @param userType  用户类别
     */
    void updateUser(@Param("loginName") String loginName, @Param("userType") String userType);

    /**
     * 获取外部认证服务器信息
     *
     * @return
     */
    ADConfigDTO select();

    /**
     * 获取当前映射关系下，所有的用户信息
     *
     * @param configId
     * @return
     */
    List<MWUser> selectGroupUserById(@Param("configId") Integer configId);

    /**
     * 插入用户和映射关系的关联信息
     *
     * @param us
     */
    void insertConfigUser(ADUserDetailDTO us);

    /**
     * 获取外部认证服务器配置信息
     *
     * @return
     */
    MwLdapAuthenticInfoDTO selectSyAdInfo();

    /**
     * 增加服务器认证信息
     *
     * @param param
     */
    void insertAdInfo(AdCommonParam param);

    /**
     * 获取当前映射下的所用用户信息
     *
     * @param param
     * @return
     */
    List<MWADConfigUserDTO> selectConfigInfo(QueryADInfoParam param);

    /**
     * 删除映射对应的用户关联信息
     *
     * @param idList
     */
    void deleteConfigUser(List<Integer> idList);

    /**
     * 将用户插入临时用户表
     *
     * @param userTemp
     */
    void insertTempUser(MwTempUserDTO userTemp);

    /**
     * 临时映射关联表
     *
     * @param configDTO
     */
    void insertTempConfig(MwTempConfigDTO configDTO);

    /**
     * 清空临时用户表
     */
    void truncateTempUser();

    /**
     * 清空临时映射信息表
     */
    void truncateTempConfig();

    /**
     * 获取临时映射表的映射ID
     *
     * @return
     */
    List<Integer> selectTempConfigId();

    /**
     * 获取临时用户表的用户ID
     *
     * @return
     */
    List<Integer> selectTempUserId();

    /**
     * 删除映射信息
     *
     * @param configIdList
     */
    void deleteByConfigList(List<Integer> configIdList);

    /**
     * 清空外部认证服务器认证信息
     */
    void truncateAuthent();

    /**
     * 根据登录名删除用户信息
     *
     * @param dtos
     */
    void deleteByLoginName(List<String> dtos);

    /**
     * 删除映射配置下的所有AD用户
     *
     * @param id 配置ID
     */
    void deleteADConfigUser(Integer id);

    /**
     * 获取用户信息
     *
     * @param ma
     * @return
     */
    List<MWADInfoDTO> selectByADInfo(MWDomainInfoDTO ma);

    /**
     * 通过配置ID获取用户数量
     *
     * @param configId 配置ID
     * @return AD用户数
     */
    int countByConfigId(int configId);

    /**
     * 更新配置
     *
     * @param id   配置ID
     * @param desc 备注描述
     */
    void updateDesc(@Param(value = "id") Integer id,
                    @Param(value = "desc") String desc);

    /**
     * 删除用户经理信息
     * @param userId 用户ID
     */
    void deleteUserManager(@Param(value = "userId") Integer userId);

    /**
     * 创建用户经理信息
     *
     * @param userId 用户ID
     * @param userManager  用户manager西悉尼
     */
    void insertUserManager(@Param(value = "userId") Integer userId,
                           @Param(value = "userManager") String userManager);

    /**
     * 获取用户经理信息
     * @param userId 用户ID
     * @return
     */
    String getUserManager(@Param(value = "userId") Integer userId);

    /**
     * 根据角色ID获取是否存在关联外部认证的角色
     *
     * @param roleId 　角色ID
     * @return
     */
    int countAdRoleMapper(@Param(value = "roleId") int roleId);
}
