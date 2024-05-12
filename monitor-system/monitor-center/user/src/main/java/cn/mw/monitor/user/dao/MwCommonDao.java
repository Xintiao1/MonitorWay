package cn.mw.monitor.user.dao;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.user.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/6/1 9:30
 */
public interface MwCommonDao {
    int insertSuperiorUser(UserMapper userMapper);

    int insertUserMapper(List<UserMapper> userMapper);

    int insertSuperiorGroup(GroupMapper groupMapper);

    int insertGroupMapper(List<GroupMapper> groupMapper);

    int insertSuperiorOrg(OrgMapper orgMapper);

    int insertOrgMapper(List<OrgMapper> orgMapper);

    void insertPermissionMapper(List<DataPermissionDto> permissionMapper);

    int insertDataPermission(DataPermissionDto dto);

    int deleteUserMapper(DeleteDto deleteDto);

    int deleteGroupMapper(DeleteDto deleteDto);

    int deleteOrgMapper(DeleteDto deleteDto);

    int deleteDataPermission(DeleteDto deleteDto);

    int deleteUserMappers(DeleteDto deleteDto);

    int deleteGroupMappers(DeleteDto deleteDto);

    int deleteOrgMappers(DeleteDto deleteDto);

    int deleteDataPermissions(DeleteDto deleteDto);

    int updateDataPermission(UpdateDTO updateDTO);

    int updateDataPermissions(DataPermissionDto dto);

    List<String> getOrgNameByTypeId(@Param("typeId") String typeId, @Param("type") String type);

    /**
     * 根据参数获取类别ID列表
     *
     * @param criteria 查询参数
     * @return
     */
    List<String> getAllTypeIdList(Map criteria);

    /**
     * 根据ID获取所有的负责人ID列表
     *
     * @param typeId   ID
     * @param dataType 类别
     * @return
     */
    List<Integer> getUserListByTypeId(@Param(value = "typeId") String typeId,
                                      @Param(value = "dataType") String dataType);

    /**
     * 根据类别获取所有的负责人ID列表
     *
     * @param dataType 类别
     * @return
     */
    List<Integer> getUserListByDataType(@Param(value = "dataType") String dataType);

    /**
     * 根据ID列表获取所有的负责人ID列表
     *
     * @param typeIdList 类别ID列表
     * @param dataType   类别
     * @return
     */
    List<Map> getUserListByTypeIds(@Param(value = "list") List<String> typeIdList,
                                   @Param(value = "dataType") String dataType);

    /**
     * 根据ID获取所有的用户组ID列表
     *
     * @param typeId   ID
     * @param dataType 类别
     * @return
     */
    List<Integer> getGroupListByTypeId(@Param(value = "typeId") String typeId,
                                       @Param(value = "dataType") String dataType);

    /**
     * 根据ID获取所有的用户组ID列表
     *
     * @param typeIdList ID列表
     * @param dataType   类别
     * @return
     */
    List<Map> getGroupListByTypeIds(@Param(value = "list") List<String> typeIdList,
                                    @Param(value = "dataType") String dataType);

    /**
     * 根据ID获取所有的机构ID列表
     *
     * @param typeId   ID
     * @param dataType 类别
     * @return
     */
    List<OrgDTO> getOrgListByTypeId(@Param(value = "typeId") String typeId,
                                    @Param(value = "dataType") String dataType);

    /**
     * 根据ID获取所有的机构ID列表
     *
     * @param typeIdList ID列表
     * @param dataType   类别
     * @return
     */
    List<Map> getOrgListByTypeIds(@Param(value = "list") List<String> typeIdList,
                                  @Param(value = "dataType") String dataType);

    List<cn.mw.monitor.service.assets.model.UserDTO> getUserList(@Param(value = "typeId") String typeId,
                                                                 @Param(value = "dataType") String dataType);

    List<GroupDTO> getGroupList(@Param(value = "typeId") String typeId,
                                @Param(value = "dataType") String dataType);

    /**
     * 根据类别ID列表获取用户组数据
     *
     * @param typeIdList 类别ID列表
     * @param dataType   类别
     * @return
     */
    List<Map> getGroupListByIds(@Param(value = "list") List<String> typeIdList,
                                @Param(value = "dataType") String dataType);

    /**
     * 根据类别ID列表获取用户数据
     *
     * @param typeIdList 类别ID列表
     * @param dataType   类别
     * @return
     */
    List<Map> getUserListByIds(@Param(value = "list") List<String> typeIdList,
                               @Param(value = "dataType") String dataType);

    /**
     * 更新负责人数据权限
     *
     * @param dataType      数据类型
     * @param userId        原用户ID
     * @param changedUserId 更改后用户ID
     */
    void changeUserDataPermission(@Param(value = "dataType") String dataType,
                                  @Param(value = "userId") int userId,
                                  @Param(value = "changedUserId") int changedUserId);

    /**
     * 计算用户已关联数据数量
     *
     * @param userId       原用户ID
     * @param dataTypeList 数据类型列表
     * @return
     */
    int countUserType(@Param(value = "userId") int userId,
                      @Param(value = "list") List<String> dataTypeList);
}
