package cn.mw.monitor.service.user.api;

import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.state.DataType;

import java.util.List;

/**
 * @author xhy
 * @date 2020/6/1 9:44
 */
public interface MWCommonService {


    /**
     * 通用用户权限单个添加
     * @param commonDto
     */
    void addMapperAndPerm(InsertDto commonDto);

    /**
     * 上级权限增加 下级同时增加
     * @param commonDto
     */
    void extendPerm(InsertDto commonDto,List<Integer> idList);
    /**
     * 通用用户权限单个删除
     * @param deleteDto
     */
    void deleteMapperAndPerm(DeleteDto deleteDto);
    /**
     * 通用用户权限批量删除
     * @param deleteDto
     */
    void deleteMapperAndPerms(DeleteDto deleteDto);

//    /**
//     * 关联用户权限单个修改（机构，用户，用户组前端勾选的才做修改）
//     * @param updateDTO
//     */
//    void editorMapperAndPerm(UpdateDTO updateDTO);

    /**
     * 关联用户权限批量修改（机构，用户，用户组前端勾选的才做修改）
     * @param updateDTO
     */
    void editorMapperAndPerms(UpdateDTO updateDTO);

    /**
     * 根据主键和类型查询对应的机构名称
     * @param typeId
     * @param type
     * @return
     */
    List<String> getOrgNameByTypeId(String typeId, String type);

    /**
     * 根据id获取数据权限
     *
     * @param dataType 类别
     * @param id       数据ID
     * @return
     */
    List<DataAuthorityDTO> getDataAuthById(DataType dataType, List<String> id);

    /**
     * 根据id获取数据权限
     *
     * @param dataType 类别
     * @param id       数据ID
     * @return
     */
    List<DataPermission> getDataAuthByIds(DataType dataType, List<String> id);

    /**
     * 获取数据权限（各类ID列表）
     *
     * @param dataType 类别
     * @param typeId   主键ID
     * @return
     */
    DataPermission getDataPermission(DataType dataType, String typeId);

    /**
     * 获取数据权限详情（各类详细信息）
     *
     * @param dataType 类别
     * @param typeId   主键ID
     * @return
     */
    DataPermission getDataPermissionDetail(DataType dataType, String typeId);

    /**
     * 增加用户数据权限
     *
     * @param baseParam 基础数据
     */
    void addMapperAndPerm(DataPermissionParam baseParam);

    /**
     * 修改用户数据权限
     *
     * @param baseParam 基础数据
     */
    void updateMapperAndPerm(DataPermissionParam baseParam);

    /**
     * 删除用户数据权限
     *
     * @param baseParam 基础数据
     */
    void deleteMapperAndPerm(DataPermissionParam baseParam);

    /**
     * 获取数据权限（各类ID列表）
     *
     * @param baseParam 基础数据
     * @return
     */
    DataPermission getDataPermission(DataPermissionParam baseParam);

    void insertGroupMapper(List<GroupMapper> groupMappers);
    void insertUserMapper(List<UserMapper> userMappers);
    void insertOrgMapper(List<OrgMapper> orgMappers);
    void insertPermissionMapper(List<DataPermissionDto> permissionMapper);

    /**
     * 获取当前系统是否启用模型资产管理
     *
     * @return true：启用，使用资源中心模块  false：不启用，使用老资产模块
     */
    boolean getSystemAssetsType();
}
