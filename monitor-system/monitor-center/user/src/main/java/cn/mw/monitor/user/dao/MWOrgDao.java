package cn.mw.monitor.user.dao;

import cn.mw.monitor.api.param.org.QueryOrgForDropDown;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.OrgMapperDTO;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.dto.MwSubUserDTO;
import cn.mw.monitor.service.user.model.MWOrg;
import cn.mw.monitor.user.dto.MwOrgLongitudeDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MWOrgDao {

    /**
     * 更新机构子节点
     */
    int updateIsNoteById(@Param("pid") Integer pid, @Param("isNote") boolean isNote, @Param("modifier") String modifier);
    /**
     * 根据机构id查询机构深度和节点id
     */
    Map<String, Object> selectDeepNodesById(@Param("pid") Integer pid);
    /**
     * 新增机构
     */
    int insert(MWOrg mwOrg);
    /**
     * 更新结构节点信息
     */
    int updateNoteByOrgId(@Param("orgId") Integer orgId, @Param("nodes") String nodes);
    /**
     * 根据机构id查询机构名称
     */
    String selectOrgNameById(@Param("orgId") Integer orgId);
    /**
     * 根据pid查询机构数量
     */
    Integer countOrgByPid(@Param("orgId") Integer orgId);
    /**
     * 删除机构信息
     */
    int delete(@Param("orgIds") List<Integer> orgIds, @Param("modifier") String modifier);
    /**
     * 更新下级机构状态信息
     */
    int updateChildOrgState(MWOrg mwOrg);
    /**
     * 查询当前机构及下级机构的所有用户id
     */
    List<Integer> selectUserIdByOrgId(@Param("orgId") Integer orgId);

    /**
     *查询当前机构子机构下所有用户
     */
    List<Integer> selectChildUserIdByOrgId(@Param("orgId") Integer orgId);
    /**
     * 更新用户状态信息
     */
    int updateUserState(@Param("userIds") List<Integer> userIds, @Param("enable") String enable, @Param("modifier") String modifier);
    /**
     * 根据子机构的id查询上级机构的状态
     */
    String selectOrgEnableByChildOrgId(@Param("orgId") Integer orgId);
    /**
     * 更新机构状态信息
     */
    int updateOrgState(MWOrg mwOrg);
    /**
     * 修改机构
     */
    int update(MWOrg mwOrg);
    /**
     * 查询机构列表
     */
    List<MWOrgDTO> selectList(Map criteria);

    /**
     * 查询机构列表
     */
    List<MWOrgDTO> selectListByXZYCSys(Map criteria);

    /**
     * 根据机构ID查询机构信息
     */
    MWOrg selectByOrgId(@Param("orgId") Integer orgId);

    /**
     * 查询下拉框机构列表
     */
    @Deprecated
    List<MWOrgDTO> selectDropdownList(QueryOrgForDropDown qParam);
    /**
     * 根据用户ID查询机构信息
     */
    List<MWOrg> selectByUserId(Integer userId);
    /**
     * 根据用户名查询机构信息
     */
    List<MWOrg> selectListByLoginName(String loginName);

    List<Integer> selectPubUserIdByOrgId(@Param("orgIds") List<Integer> orgIds);

    @Deprecated
    Integer selectOrgByOrgName(String orgName);

    /**
     * 根据机构id查询机构及子机构信息
     */
    List<MWOrg> selectOrgById(@Param("orgId") Integer orgId);


    List<MWOrg> selectOrgByOrgId(@Param("orgIds") List<Integer> orgIds);

    /**
     * 查询一级机构名有没有重复
     */
    Integer selectOneOrgByName(String orgName);

    Integer  selectPidCount(String orgName, Integer pid);

    /**
     * 获取当前资产所属的组织机构数据
     *
     * @param criteria 查询参数
     * @return 所属机构列表
     */
    List<OrgDTO> selectOrgByParams(Map criteria);

    /**
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 机构列表
     */
    List<OrgDTO> getAllOrgList(@Param(value = "typeId") int typeId,
                               @Param(value = "dataType") String dataType);

    /**
     * 根据名称获取组织信息
     *
     * @param orgName 组织名称
     * @param pid     父ID
     * @return 组织信息
     */
    cn.mw.monitor.service.user.dto.OrgDTO selectOrgByName(@Param(value = "orgName") String orgName,
                                                          @Param(value = "pid") int pid);

    List<String> getOrgnamesByids(@Param("organizes") List<Integer> organizes);

    /**
     * 查询机构地址的经纬度数据
     * @param orgName 地址模糊搜索名称
     * @return
     */
    List<MwOrgLongitudeDto> selectOrgLongitudeDropDown(@Param("orgName") String orgName, @Param("level") Integer level);

    /**
     * 根据业务模块id,查询关联机构信息
     * @param map 查询条件
     * @return
     */
    List<OrgMapperDTO> selectOrgByParamsAndIds(Map map);

    // TODO: 2023/1/10 19点09分  需要前端配合更改下用户获取方式
    /**
     *  根据机构ID获取当前机构拥有多少
     * @param orgId
     * @return
     */
    List<MwSubUserDTO> selectUser(@Param(value = "orgId") int orgId);
}
