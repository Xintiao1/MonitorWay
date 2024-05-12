package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.exception.CommonException;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWOrg;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dao.MWOrgDao;
import cn.mw.monitor.user.dao.MwCommonDao;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/6/1 9:53
 */
@Service
@Slf4j
public class CommonServiceImpl implements MWCommonService {
    private static final Logger logger = LoggerFactory.getLogger("service-" + CommonServiceImpl.class.getName());

    @Value("${model.assets.enable}")
    private Boolean modelAssetEnable;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Resource
    private MwCommonDao commonDao;

    @Resource
    private MWOrgDao mwOrgDao;

    @Override
    public void addMapperAndPerm(InsertDto commonDto) {
        try {
            DataPermissionDto dto = new DataPermissionDto();
            dto.setType(commonDto.getType());     //类型
            dto.setTypeId(commonDto.getTypeId());  //数据主键
            String type = commonDto.getType();
            dto.setDescription(DataType.valueOf(type).getDesc()); //描述
            MwLoginUserDto localTread = iLoginCacheInfo.getLocalTread();
            Integer roleId;
            Integer userId;
            String dataPerm;
            if(localTread == null){
                roleId = iLoginCacheInfo.getRoleInfo().getId();//角色
                userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();//当前登陆人id
                dataPerm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限
            }else{
                roleId =localTread.getRoleId();//角色
                userId = localTread.getUserId();//当前登陆人id
                dataPerm = localTread.getDataPerm(); //数据权限
            }
//            Integer roleId = iLoginCacheInfo.getRoleInfo().getId();//角色
//            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();//当前登陆人id
            log.info("addMapperAndPerm,userid:{},loginname:{}", userId);
//            String dataPerm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限
            dto.setIsUser(1);
            List<UserMapper> userMapper = new ArrayList<>();
            //绑定用户
            //1选择了用户
            if (null != commonDto.getUserIds() && commonDto.getUserIds().size() > 0) {
                List<Integer> userIdList = commonDto.getUserIds();
                //将当前登录人添加进入负责人中
                if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                    userIdList.add(userId);
                }
                //对负责人去重
                userIdList = MWUtils.removeDuplicate(userIdList);
                userIdList.forEach(userIds -> {
                            log.info("userMapper.add,userid:{}", userIds);
                            userMapper.add(UserMapper.builder().typeId(commonDto.getTypeId()).userId(userIds).type(commonDto.getType()).build());
                        }
                );

            } else {
                //未选择用户
                userMapper.add(UserMapper.builder().typeId(commonDto.getTypeId()).userId(userId).type(commonDto.getType()).build());
            }

            commonDao.insertUserMapper(userMapper);

            //绑定用户组
            if (null != commonDto.getGroupIds() && commonDto.getGroupIds().size() != 0) {
                //添加用户组映射
                dto.setIsGroup(1);
                List<GroupMapper> groupMapper = new ArrayList<>();
                commonDto.getGroupIds().forEach(
                        groupId -> groupMapper.add(GroupMapper.builder().typeId(commonDto.getTypeId()).groupId(groupId).type(commonDto.getType()).build())
                );
                commonDao.insertGroupMapper(groupMapper);
            } else {
                dto.setIsGroup(0);
            }

            //绑定机构
            List<OrgMapper> orgMapper = new ArrayList<>();
            //无论有没有选择机构，先把非系统管理员自的机构添加进去
            if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                List<MWOrg> mwOrgs = mwOrgDao.selectByUserId(userId);
                mwOrgs.forEach(mwOrg -> {
                    orgMapper.add(OrgMapper.builder().typeId(commonDto.getTypeId()).orgId(mwOrg.getOrgId()).type(commonDto.getType()).build());
                });
            }
            //有选择机构的情况下，添加机构
            if (null != commonDto.getOrgIds() && commonDto.getOrgIds().size() > 0 && dataPerm.equals("PUBLIC")) {
                commonDto.getOrgIds().forEach(
                        orgId -> orgMapper.add(OrgMapper.builder().typeId(commonDto.getTypeId()).orgId(orgId.get(orgId.size() - 1)).type(commonDto.getType()).build())
                );

            }
            //对机构进行去重
            if (orgMapper.size() > 0) {
                List<OrgMapper> newOrgMapper = MWUtils.removeDuplicate(orgMapper);
                commonDao.insertOrgMapper(newOrgMapper);
            }

            //添加权限数据
            commonDao.insertDataPermission(dto);
        } catch (CommonException e) {
            logger.error("fail to insert with addMapperAndPerm={}, cause:{}", commonDto, e);
        }
    }

    @Override
    public void extendPerm(InsertDto commonDto,List<Integer> idList) {
        try {
            DataPermissionDto dto = new DataPermissionDto();
            dto.setType(commonDto.getType());     //类型
            dto.setTypeId(commonDto.getTypeId());  //数据主键
            String type = commonDto.getType();
            dto.setDescription(DataType.valueOf(type).getDesc()); //描述
            Integer roleId = iLoginCacheInfo.getRoleInfo().getId();//角色
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();//当前登陆人id
            log.info("addMapperAndPerm,userid:{},loginname:{}", userId, iLoginCacheInfo.getLoginName());
            String dataPerm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限
            dto.setIsUser(1);
            List<UserMapper> userMapper = new ArrayList<>();
            //绑定用户
            //1选择了用户
            if (null != commonDto.getUserIds() && commonDto.getUserIds().size() > 0) {
                List<Integer> userIdList = commonDto.getUserIds();
                //将当前登录人添加进入负责人中
                if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                    userIdList.add(userId);
                }
                //对负责人去重
                userIdList = MWUtils.removeDuplicate(userIdList);
                userIdList.forEach(userIds -> {
                            log.info("userMapper.add,userid:{}", userIds);
                            userMapper.add(UserMapper.builder().typeId(commonDto.getTypeId()).userId(userIds).type(commonDto.getType()).build());
                        }
                );

            } else {
                //未选择用户
                userMapper.add(UserMapper.builder().typeId(commonDto.getTypeId()).userId(userId).type(commonDto.getType()).build());
            }

            commonDao.insertUserMapper(userMapper);

            //上级所属用户修改 下级跟着添加
            idList.stream().forEach(integer -> {
                userMapper.stream().forEach(u->{
                    u.setTypeId(integer.toString());
                    commonDao.insertSuperiorUser(u);
                });
            });

            //绑定用户组
            if (null != commonDto.getGroupIds() && commonDto.getGroupIds().size() != 0) {
                //添加用户组映射
                dto.setIsGroup(1);
                List<GroupMapper> groupMapper = new ArrayList<>();
                commonDto.getGroupIds().forEach(
                        groupId -> groupMapper.add(GroupMapper.builder().typeId(commonDto.getTypeId()).groupId(groupId).type(commonDto.getType()).build())
                );

                commonDao.insertGroupMapper(groupMapper);
                //上级所属用户组修改 下级跟着添加
                idList.stream().forEach(integer -> {
                    groupMapper.stream().forEach(g->{
                        g.setTypeId(integer.toString());
                        commonDao.insertSuperiorGroup(g);
                    });
                });
            } else {
                dto.setIsGroup(0);
            }

            //绑定机构
            List<OrgMapper> orgMapper = new ArrayList<>();
            //无论有没有选择机构，先把非系统管理员自的机构添加进去
            if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                List<MWOrg> mwOrgs = mwOrgDao.selectByUserId(userId);
                mwOrgs.forEach(mwOrg -> {
                    orgMapper.add(OrgMapper.builder().typeId(commonDto.getTypeId()).orgId(mwOrg.getOrgId()).type(commonDto.getType()).build());
                });
            }
            //有选择机构的情况下，添加机构
            if (null != commonDto.getOrgIds() && commonDto.getOrgIds().size() > 0 && dataPerm.equals("PUBLIC")) {
                commonDto.getOrgIds().forEach(
                        orgId -> orgMapper.add(OrgMapper.builder().typeId(commonDto.getTypeId()).orgId(orgId.get(orgId.size() - 1)).type(commonDto.getType()).build())
                );

            }
            //对机构进行去重
            if (orgMapper.size() > 0) {
                List<OrgMapper> newOrgMapper = MWUtils.removeDuplicate(orgMapper);
                commonDao.insertOrgMapper(newOrgMapper);
                //上级所属机构修改 下级跟着添加
                idList.stream().forEach(integer -> {
                    newOrgMapper.stream().forEach(n->{
                        n.setTypeId(integer.toString());
                        commonDao.insertSuperiorOrg(n);
                    });
                });
            }

            //添加权限数据
            commonDao.insertDataPermission(dto);
        } catch (CommonException e) {
            logger.error("fail to insert with addMapperAndPerm={}, cause:{}", commonDto, e);
        }
    }

    @Override
    public void deleteMapperAndPerm(DeleteDto deleteDto) {
        try {
            commonDao.deleteUserMapper(deleteDto);
            commonDao.deleteGroupMapper(deleteDto);
            commonDao.deleteOrgMapper(deleteDto);
            commonDao.deleteDataPermission(deleteDto);
        } catch (CommonException e) {
            logger.error("fail to delete with deleteMapperAndPerm={}, cause:{}", deleteDto, e.getMessage());
        }
    }

    @Override
    public void deleteMapperAndPerms(DeleteDto deleteDto) {
        try {
            commonDao.deleteUserMappers(deleteDto);
            commonDao.deleteGroupMappers(deleteDto);
            commonDao.deleteOrgMappers(deleteDto);
            commonDao.deleteDataPermissions(deleteDto);
        } catch (CommonException e) {
            logger.error("fail to delete with deleteMapperAndPerms={}, cause:{}", deleteDto, e.getMessage());
        }
    }

    @Override
    public void editorMapperAndPerms(UpdateDTO updateDTO) {
        try {
            DataPermissionDto dto = new DataPermissionDto();
            dto.setType(updateDTO.getType());     //类型
            dto.setTypeIds(updateDTO.getTypeIds());  //数据主键
            String type = updateDTO.getType();
            dto.setDescription(DataType.valueOf(type).getDesc()); //描述
            //先把选中的所有typeIds关联的有需要的进行删除
            DeleteDto deleteDto = DeleteDto.builder()
                    .typeIds(updateDTO.getTypeIds())    //批量资产数据主键
                    .type(updateDTO.getType()).build();  //ASSETS

            Integer roleId = iLoginCacheInfo.getRoleInfo().getId();//角色
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();//当前登陆人id
            log.info("addMapperAndPerm,userid:{},loginname:{}", userId, iLoginCacheInfo.getLoginName());
            String dataPerm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限
            List<UserMapper> userMapper = new ArrayList<>();
            if (updateDTO.isUser()) {//批量修改勾选了用户
                //不管之前有没有关联都先进行删除
                commonDao.deleteUserMappers(deleteDto);
            }
            List<Integer> userIdList = updateDTO.getUserIds();
            //给用户添加新的用户，最少添加一个当前登录用户
            //将当前登录人添加进入负责人中
            if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                userIdList.add(userId);
                dto.setIsUser(1);
            }
            if (userIdList != null && userIdList.size() > 0) {
                //对负责人去重
                userIdList = MWUtils.removeDuplicate(userIdList);
                userIdList.forEach(uId -> {
                    updateDTO.getTypeIds().forEach(typeId -> {
                        userMapper.add(UserMapper.builder().typeId(typeId).userId(uId).type(updateDTO.getType()).build());
                    });
                });
                commonDao.insertUserMapper(userMapper);
                dto.setIsUser(1);
            }

            if (updateDTO.isGroup()) {//批量修改勾选了用户组
                //不管之前有没有关联都先进行删除
                commonDao.deleteGroupMappers(deleteDto);
                //绑定用户组
                if (null != updateDTO.getGroupIds() && updateDTO.getGroupIds().size() != 0) {
                    //添加用户组映射
                    dto.setIsGroup(1);
                    List<GroupMapper> groupMapper = new ArrayList<>();
                    updateDTO.getGroupIds().forEach(
                            groupId -> {
                                updateDTO.getTypeIds().forEach(typeId -> {
                                    groupMapper.add(GroupMapper.builder().typeId(typeId).groupId(groupId).type(updateDTO.getType()).build());
                                });
                            });
                    commonDao.insertGroupMapper(groupMapper);
                } else {
                    dto.setIsGroup(0);
                }
            }

            if (updateDTO.isOrg()) {//批量修改勾选了机构
                //不管之前有没有关联都先进行删除
                commonDao.deleteOrgMappers(deleteDto);
                //绑定机构
                List<OrgMapper> orgMapper = new ArrayList<>();
                //非超级管理员添加机构会把自身所在所有机构添加
                if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                    List<MWOrg> mwOrgs = mwOrgDao.selectByUserId(userId);
                    mwOrgs.forEach(mwOrg -> {
                        updateDTO.getTypeIds().forEach(typeId -> {
                            orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(mwOrg.getOrgId()).type(updateDTO.getType()).build());
                        });
                    });
                }
                //当共有权限选择了机构（因为私有权限的机构已经再上一步添加了）
                if (null != updateDTO.getOrgIds() && updateDTO.getOrgIds().size() > 0 && dataPerm.equals("PUBLIC")) {
                    updateDTO.getOrgIds().forEach(orgId -> {
                        updateDTO.getTypeIds().forEach(typeId -> {
                            orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(orgId.get(orgId.size() - 1)).type(updateDTO.getType()).build());
                        });
                    });
                }
                //去重
                List<OrgMapper> newOrgMapper = MWUtils.removeDuplicate(orgMapper);
                if (newOrgMapper != null && newOrgMapper.size() > 0) {
                    commonDao.insertOrgMapper(newOrgMapper);
                }
            }

            if( dto.getIsGroup() != null || dto.getIsUser() != null) {
                //添加权限数据
                commonDao.updateDataPermissions(dto);
            }
        } catch (CommonException e) {
            logger.error("fail to editor with editorMapperAndPerms={}, cause:{}", updateDTO, e);
        }
    }

    @Override
    public List<String> getOrgNameByTypeId(String typeId, String type) {
        return commonDao.getOrgNameByTypeId(typeId, type);
    }

    /**
     * 根据id获取数据权限
     *
     * @param dataType 类别
     * @param ids       数据ID列表
     * @return
     */
    @Override
    public List<DataAuthorityDTO> getDataAuthById(DataType dataType, List<String> ids) {
        List<DataAuthorityDTO> authList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)){
            return authList;
        }
        //获取负责人ID列表，用户组ID列表，机构ID列表
        List<Map> userIdsList = commonDao.getUserListByTypeIds(ids, dataType.getName());
        List<Map> groupIdList = commonDao.getGroupListByTypeIds(ids, dataType.getName());
        List<Map> orgList = commonDao.getOrgListByTypeIds(ids, dataType.getName());
        //主键对应的数据集合，包含负责人集合，用户组集合，机构集合，机构节点集合
        Map<String, List<Integer>> userIdListMap = new HashMap<>();
        Map<String, List<Integer>> groupIdListMap = new HashMap<>();
        Map<String, List<Integer>> orgIdListMap = new HashMap<>();
        Map<String, List<List<Integer>>> orgNodeListMap = new HashMap<>();
        //临时数据
        String typeId;
        int userId;
        int groupId;
        int orgId;
        String orgNodes;
        List<Integer> userIdList;
        List<Integer> groupIds;
        List<Integer> orgIdList;
        List<List<Integer>> orgNodesList;
        List<String> nodes;
        List<Integer> orgNodeList;
        //将列表数据全部整合到数据对应集合中，包含负责人集合，用户组集合，机构集合，机构节点集合
        for (Map map : userIdsList) {
            typeId = (String) map.get("typeId");
            userId =  Integer.parseInt(String.valueOf(map.get("userId")));
            if (userIdListMap.containsKey(typeId)) {
                userIdList = userIdListMap.get(typeId);
                userIdList.add(userId);
            } else {
                userIdList = new ArrayList<>();
                userIdList.add(userId);
                userIdListMap.put(typeId, userIdList);
            }
        }
        for (Map map : groupIdList) {
            typeId = (String) map.get("typeId");
            groupId = Integer.parseInt(String.valueOf(map.get("groupId")));
            if (groupIdListMap.containsKey(typeId)) {
                groupIds = groupIdListMap.get(typeId);
                groupIds.add(groupId);
            } else {
                groupIds = new ArrayList<>();
                groupIds.add(groupId);
                groupIdListMap.put(typeId, groupIds);
            }
        }
        for (Map map : orgList) {
            typeId = (String) map.get("typeId");
            orgNodes = (String) map.get("nodes");
            orgId = Integer.parseInt(String.valueOf(map.get("orgId")));
            orgNodeList = new ArrayList<>();
            nodes = Arrays.stream(orgNodes.split(",")).collect(Collectors.toList());
            for (String node : nodes) {
                if (StringUtils.isNotBlank(node)) {
                    orgNodeList.add(Integer.parseInt(node));
                }
            }
            if (orgIdListMap.containsKey(typeId)) {
                orgIdList = orgIdListMap.get(typeId);
                orgIdList.add(orgId);
            } else {
                orgIdList = new ArrayList<>();
                orgIdList.add(orgId);
                orgIdListMap.put(typeId, orgIdList);
            }
            if (orgNodeListMap.containsKey(typeId)) {
                orgNodesList = orgNodeListMap.get(typeId);
                orgNodesList.add(orgNodeList);
            } else {
                orgNodesList = new ArrayList<>();
                orgNodesList.add(orgNodeList);
                orgNodeListMap.put(typeId, orgNodesList);
            }
        }
        //处理数据，将每个查询输出组装
        for (String key : ids) {
            DataAuthorityDTO auth = new DataAuthorityDTO();
            auth.setId(key);
            auth.setDataType(dataType);
            if (userIdListMap.containsKey(key)) {
                userIdList = userIdListMap.get(key);
            } else {
                userIdList = new ArrayList<>();
            }
            if (groupIdListMap.containsKey(key)) {
                groupIds = groupIdListMap.get(key);
            } else {
                groupIds = new ArrayList<>();
            }
            if (orgIdListMap.containsKey(key)) {
                orgIdList = orgIdListMap.get(key);
            } else {
                orgIdList = new ArrayList<>();
            }
            if (orgNodeListMap.containsKey(key)) {
                orgNodesList = orgNodeListMap.get(key);
            } else {
                orgNodesList = new ArrayList<>();
            }
            auth.setUserIdList(userIdList);
            auth.setGroupIdList(groupIds);
            auth.setOrgIdList(orgIdList);
            auth.setOrgNodeList(orgNodesList);
            authList.add(auth);
        }
        return authList;
    }

    /**
     * 根据id获取数据权限
     *
     * @param dataType 类别
     * @param ids      数据ID
     * @return
     */
    @Override
    public List<DataPermission> getDataAuthByIds(DataType dataType, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<DataPermission> dataList = new ArrayList<>();
        //获取负责人ID列表，用户组ID列表，机构ID列表
        List<Map> userList = commonDao.getUserListByIds(ids, dataType.getName());
        List<Map> groupList = commonDao.getGroupListByIds(ids, dataType.getName());
        List<Map> orgList = commonDao.getOrgListByTypeIds(ids, dataType.getName());
        //主键对应的数据集合，包含负责人集合，用户组集合，机构集合，机构节点集合
        Map<String, List<cn.mw.monitor.service.assets.model.UserDTO>> userListMap = new HashMap<>();
        Map<String, List<GroupDTO>> groupListMap = new HashMap<>();
        Map<String, List<OrgDTO>> orgListMap = new HashMap<>();
        Map<String, List<Integer>> userIdListMap = new HashMap<>();
        Map<String, List<Integer>> groupIdListMap = new HashMap<>();
        Map<String, List<Integer>> orgIdListMap = new HashMap<>();
        Map<String, List<List<Integer>>> orgNodeListMap = new HashMap<>();
        //临时数据
        String typeId;
        int userId;
        int groupId;
        int orgId;
        String orgNodes;
        String loginName;
        String userName;
        String groupName;
        String orgName;
        cn.mw.monitor.service.assets.model.UserDTO userInfo;
        GroupDTO groupInfo;
        OrgDTO orgInfo;
        List<cn.mw.monitor.service.assets.model.UserDTO> userInfoList;
        List<GroupDTO> groupInfoList;
        List<OrgDTO> orgInfoList;
        List<Integer> userIdList;
        List<Integer> groupIds;
        List<Integer> orgIdList;
        List<List<Integer>> orgNodesList;
        List<String> nodes;
        List<Integer> orgNodeList;
        //将列表数据全部整合到数据对应集合中，包含负责人集合，用户组集合，机构集合，机构节点集合
        for (Map map : userList) {
            typeId = (String) map.get("typeId");
            loginName = (String) map.get("loginName");
            userName = (String) map.get("userName");
            userId = Integer.parseInt(map.get("userId").toString());
            userInfo = new cn.mw.monitor.service.assets.model.UserDTO();
            userInfo.setUserId(userId);
            userInfo.setLoginName(loginName);
            userInfo.setUserName(userName);
            if (userListMap.containsKey(typeId)) {
                userInfoList = userListMap.get(typeId);
                userInfoList.add(userInfo);
            } else {
                userInfoList = new ArrayList<>();
                userInfoList.add(userInfo);
                userListMap.put(typeId, userInfoList);
            }
            if (userIdListMap.containsKey(typeId)) {
                userIdList = userIdListMap.get(typeId);
                userIdList.add(userId);
            } else {
                userIdList = new ArrayList<>();
                userIdList.add(userId);
                userIdListMap.put(typeId, userIdList);
            }
        }
        for (Map map : groupList) {
            typeId = (String) map.get("typeId");
            groupId = Integer.parseInt(String.valueOf(map.get("groupId")));
            groupName = (String) map.get("groupName");
            groupInfo = new GroupDTO();
            groupInfo.setGroupId(groupId);
            groupInfo.setGroupName(groupName);
            if (groupIdListMap.containsKey(typeId)) {
                groupIds = groupIdListMap.get(typeId);
                groupIds.add(groupId);
            } else {
                groupIds = new ArrayList<>();
                groupIds.add(groupId);
                groupIdListMap.put(typeId, groupIds);
            }
            if (groupListMap.containsKey(typeId)) {
                groupInfoList = groupListMap.get(typeId);
                groupInfoList.add(groupInfo);
            } else {
                groupInfoList = new ArrayList<>();
                groupInfoList.add(groupInfo);
                groupListMap.put(typeId, groupInfoList);
            }
        }
        for (Map map : orgList) {
            typeId = (String) map.get("typeId");
            orgNodes = (String) map.get("nodes");
            orgId =  Integer.parseInt(String.valueOf(map.get("orgId")));
            orgName = (String) map.get("orgName");
            orgInfo = new OrgDTO();
            orgInfo.setOrgId(orgId);
            orgInfo.setNodes(orgNodes);
            orgInfo.setOrgName(orgName);
            orgNodeList = new ArrayList<>();
            nodes = Arrays.stream(orgNodes.split(",")).collect(Collectors.toList());
            for (String node : nodes) {
                if (StringUtils.isNotBlank(node)) {
                    orgNodeList.add(Integer.parseInt(node));
                }
            }
            if (orgIdListMap.containsKey(typeId)) {
                orgIdList = orgIdListMap.get(typeId);
                orgIdList.add(orgId);
            } else {
                orgIdList = new ArrayList<>();
                orgIdList.add(orgId);
                orgIdListMap.put(typeId, orgIdList);
            }
            if (orgNodeListMap.containsKey(typeId)) {
                orgNodesList = orgNodeListMap.get(typeId);
                orgNodesList.add(orgNodeList);
            } else {
                orgNodesList = new ArrayList<>();
                orgNodesList.add(orgNodeList);
                orgNodeListMap.put(typeId, orgNodesList);
            }
            if (orgListMap.containsKey(typeId)) {
                orgInfoList = orgListMap.get(typeId);
                orgInfoList.add(orgInfo);
            } else {
                orgInfoList = new ArrayList<>();
                orgInfoList.add(orgInfo);
                orgListMap.put(typeId, orgInfoList);
            }
        }
        //处理数据，将每个查询输出组装
        for (String key : ids) {
            DataPermission data = new DataPermission();
            data.setId(key);
            data.setDataType(dataType);
            if (userIdListMap.containsKey(key)) {
                userIdList = userIdListMap.get(key);
            } else {
                userIdList = new ArrayList<>();
            }
            if (groupIdListMap.containsKey(key)) {
                groupIds = groupIdListMap.get(key);
            } else {
                groupIds = new ArrayList<>();
            }
            if (orgIdListMap.containsKey(key)) {
                orgIdList = orgIdListMap.get(key);
            } else {
                orgIdList = new ArrayList<>();
            }
            if (orgNodeListMap.containsKey(key)) {
                orgNodesList = orgNodeListMap.get(key);
            } else {
                orgNodesList = new ArrayList<>();
            }
            if (userListMap.containsKey(key)) {
                userInfoList = userListMap.get(key);
            } else {
                userInfoList = new ArrayList<>();
            }
            if (groupListMap.containsKey(key)) {
                groupInfoList = groupListMap.get(key);
            } else {
                groupInfoList = new ArrayList<>();
            }
            if (orgListMap.containsKey(key)) {
                orgInfoList = orgListMap.get(key);
            } else {
                orgInfoList = new ArrayList<>();
            }
            data.setUserIds(userIdList);
            data.setGroupIds(groupIds);
            data.setOrgIds(orgIdList);
            data.setOrgNodes(orgNodesList);
            data.setPrincipal(userInfoList);
            data.setGroups(groupInfoList);
            data.setDepartment(orgInfoList);
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 获取数据权限（各类ID列表）
     *
     * @param dataType 类别
     * @param typeId   主键ID
     * @return
     */
    @Override
    public DataPermission getDataPermission(DataType dataType, String typeId) {
        return getDataPermission(dataType, typeId, false);
    }

    /**
     * 获取数据权限详情（各类详细信息）
     *
     * @param dataType 类别
     * @param typeId   主键ID
     * @return
     */
    @Override
    public DataPermission getDataPermissionDetail(DataType dataType, String typeId) {
        return getDataPermission(dataType, typeId, true);
    }

    /**
     * 增加用户数据权限
     *
     * @param baseParam
     */
    @Override
    public void addMapperAndPerm(DataPermissionParam baseParam) {
        DataType dataType = baseParam.getBaseDataType();
        String typeId = baseParam.getBaseTypeId();
        if (dataType == null || StringUtils.isEmpty(typeId)) {
            logger.error("增加用户数据权限失败", JSON.toJSONString(baseParam));
            return;
        }
        try {
            DataPermissionDto dto = new DataPermissionDto();
            dto.setType(dataType.getName());
            dto.setTypeId(typeId);
            dto.setDescription(dataType.getDesc());
            Integer roleId = iLoginCacheInfo.getRoleInfo().getId();
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            log.info("addMapperAndPerm,userid:{},loginname:{}", userId, iLoginCacheInfo.getLoginName());
            String dataPerm = iLoginCacheInfo.getRoleInfo().getDataPerm();
            dto.setIsUser(1);
            List<UserMapper> userMapper = new ArrayList<>();
            //绑定用户
            //选择了负责人
            if (CollectionUtils.isNotEmpty(baseParam.getPrincipal())) {
                List<Integer> userIdList = baseParam.getPrincipal();
                //将当前登录人添加进入负责人中
                if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                    userIdList.add(userId);
                }
                //对负责人去重
                userIdList = MWUtils.removeDuplicate(userIdList);
                userIdList.forEach(userIds -> {
                            log.info("userMapper.add,userid:{}", userIds);
                            userMapper.add(UserMapper.builder().typeId(typeId).userId(userIds).type(dataType.getName()).build());
                        }
                );

            } else {
                //未选择用户，则将当前用户设置为负责人
                userMapper.add(UserMapper.builder().typeId(typeId).userId(userId).type(dataType.getName()).build());
            }

            commonDao.insertUserMapper(userMapper);

            //绑定用户组
            if (CollectionUtils.isNotEmpty(baseParam.getGroupIds())) {
                //添加用户组映射
                dto.setIsGroup(1);
                List<GroupMapper> groupMapper = new ArrayList<>();
                baseParam.getGroupIds().forEach(
                        groupId -> groupMapper.add(GroupMapper.builder().typeId(typeId).groupId(groupId).type(dataType.getName()).build())
                );
                commonDao.insertGroupMapper(groupMapper);
            } else {
                dto.setIsGroup(0);
            }

            //绑定机构
            List<OrgMapper> orgMapper = new ArrayList<>();
            //无论有没有选择机构，先把非系统管理员自的机构添加进去
            if (!String.valueOf(roleId).equals(MWUtils.ROLE_TOP_ID)) {
                List<MWOrg> mwOrgs = mwOrgDao.selectByUserId(userId);
                mwOrgs.forEach(mwOrg -> {
                    orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(mwOrg.getOrgId()).type(dataType.getName()).build());
                });
            }
            //有选择机构的情况下，添加机构
            if (CollectionUtils.isNotEmpty(baseParam.getOrgIds()) && dataPerm.equals("PUBLIC")) {
                baseParam.getOrgIds().forEach(
                        orgId -> orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(orgId.get(orgId.size() - 1)).type(dataType.getName()).build())
                );

            }
            //对机构进行去重
            if (orgMapper.size() > 0) {
                List<OrgMapper> newOrgMapper = MWUtils.removeDuplicate(orgMapper);
                commonDao.insertOrgMapper(newOrgMapper);
            }
            //添加权限数据
            commonDao.insertDataPermission(dto);
        } catch (CommonException e) {
            logger.error("fail to insert with addMapperAndPerm={}, cause:{}", baseParam, e);
        }
    }

    /**
     * 修改用户数据权限
     *
     * @param baseParam 基础数据
     */
    @Override
    public void updateMapperAndPerm(DataPermissionParam baseParam) {
        // TODO: 2022/12/21 需要考虑到非系统管理员修改数据时，只能提交自己能看到的用户数据，这样会引起数据覆盖
        deleteMapperAndPerm(baseParam);
        addMapperAndPerm(baseParam);
    }

    /**
     * 删除用户数据权限
     *
     * @param baseParam 基础数据
     */
    @Override
    public void deleteMapperAndPerm(DataPermissionParam baseParam) {
        DataType dataType = baseParam.getBaseDataType();
        if (CollectionUtils.isNotEmpty(baseParam.getDeleteIdList())) {
            if (dataType == null) {
                logger.error("删除用户数据权限失败", JSON.toJSONString(baseParam));
                return;
            }
            DeleteDto deleteDto = DeleteDto.builder().typeIds(baseParam.getDeleteIdList()).type(dataType.getName()).build();
            deleteMapperAndPerms(deleteDto);
        } else {
            String typeId = baseParam.getBaseTypeId();
            if (dataType == null || StringUtils.isEmpty(typeId)) {
                logger.error("删除用户数据权限失败", JSON.toJSONString(baseParam));
                return;
            }
            DeleteDto deleteDto = DeleteDto.builder()
                    .typeId(typeId)
                    .type(dataType.getName())
                    .build();
            deleteMapperAndPerm(deleteDto);
        }
    }

    /**
     * 获取数据权限（各类ID列表）
     *
     * @param baseParam 基础数据
     * @return
     */
    @Override
    public DataPermission getDataPermission(DataPermissionParam baseParam) {
        DataType dataType = baseParam.getBaseDataType();
        String typeId = baseParam.getBaseTypeId();
        if (dataType == null || StringUtils.isEmpty(typeId)) {
            logger.error("获取数据权限失败", JSON.toJSONString(baseParam));
            return new DataPermission();
        }
        return getDataPermission(dataType, typeId, false);
    }

    @Override
    public void insertGroupMapper(List<GroupMapper> groupMappers) {
        commonDao.insertGroupMapper(groupMappers);
    }

    @Override
    public void insertUserMapper(List<UserMapper> userMappers) {
        commonDao.insertUserMapper(userMappers);
    }

    @Override
    public void insertOrgMapper(List<OrgMapper> orgMappers) {
        commonDao.insertOrgMapper(orgMappers);
    }

    @Override
    public void insertPermissionMapper(List<DataPermissionDto> permissionMapper) {
        commonDao.insertPermissionMapper(permissionMapper);
    }

    /**
     * 获取当前系统是否启用模型资产管理
     *
     * @return true：启用，使用资源中心模块  false：不启用，使用老资产模块
     */
    @Override
    public boolean getSystemAssetsType() {
        return modelAssetEnable == null ? false : modelAssetEnable;
    }

    /**
     * 获取数据权限
     *
     * @param dataType 数据类型
     * @param typeId   类别ID
     * @param more     是否展示详情
     * @return
     */
    private DataPermission getDataPermission(DataType dataType, String typeId, boolean more) {
        DataPermission dataPermission = new DataPermission();
        //获取负责人ID列表
        List<Integer> userIdList = commonDao.getUserListByTypeId(typeId, dataType.getName());
        //获取机构ID列表
        List<OrgDTO> orgList = commonDao.getOrgListByTypeId(typeId, dataType.getName());
        //获取用户组ID列表
        List<Integer> groupIdList = commonDao.getGroupListByTypeId(typeId, dataType.getName());
        //机构ID列表
        List<Integer> orgIdList = new ArrayList<>();
        List<List<Integer>> orgNodeList = new ArrayList<>();
        for (OrgDTO org : orgList) {
            List<Integer> orgIds = new ArrayList<>();
            List<String> nodes = Arrays.stream(org.getNodes().split(",")).collect(Collectors.toList());
            nodes.forEach(node -> {
                if (!"".equals(node)) {
                    orgIds.add(Integer.valueOf(node));
                }
            });
            orgNodeList.add(orgIds);
            orgIdList.add(org.getOrgId());
        }
        dataPermission.setUserIds(userIdList);
        dataPermission.setOrgIds(orgIdList);
        dataPermission.setOrgNodes(orgNodeList);
        dataPermission.setGroupIds(groupIdList);
        if (more){
            List<GroupDTO> groups = commonDao.getGroupList(typeId,dataType.getName());
            List<cn.mw.monitor.service.assets.model.UserDTO> principal = commonDao.getUserList(typeId,dataType.getName());
            dataPermission.setPrincipal(principal);
            dataPermission.setGroups(groups);
            dataPermission.setDepartment(orgList);
        }
        return dataPermission;
    }
}
