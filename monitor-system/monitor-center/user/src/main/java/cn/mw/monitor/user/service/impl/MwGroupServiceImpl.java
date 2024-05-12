package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.exception.CheckDeleteGroupException;
import cn.mw.monitor.api.exception.CheckInsertGroupException;
import cn.mw.monitor.api.param.user.BindUserGroupParam;
import cn.mw.monitor.api.param.usergroup.AddUpdateGroupParam;
import cn.mw.monitor.api.param.usergroup.QueryGroupParam;
import cn.mw.monitor.api.param.usergroup.UpdateGroupStateParam;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.user.dao.MwGroupOrgMapperDao;
import cn.mw.monitor.user.dao.MwGroupTableDao;
import cn.mw.monitor.user.dao.MwUserGroupMapperDao;
import cn.mw.monitor.user.dao.MwUserOrgMapperDao;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.dto.UserGroupDTO;
import cn.mw.monitor.user.model.MwGroupOrgTable;
import cn.mw.monitor.user.model.MwGroupTable;
import cn.mw.monitor.user.model.MwUserGroupTable;
import cn.mw.monitor.user.model.MwUserOrgTable;
import cn.mw.monitor.user.service.MWGroupService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MwGroupServiceImpl implements MWGroupService, MWGroupCommonService {

    @Resource
    private MwGroupTableDao mwGroupTableDao;

    @Resource
    private MwUserOrgMapperDao mwUserOrgMapperDao;

    @Resource
    private MwGroupOrgMapperDao mwGroupOrgMapperDao;

    @Resource
    private MwUserGroupMapperDao mwUserGroupMapperDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    /**
     * 绑定用户
     */
    @Override
    public Reply bindUserGroup(BindUserGroupParam qParam) {
        try {
            if (qParam.getUserIds().size() > 0) {
                List<MwUserGroupTable> guList = new ArrayList<>();
                if (qParam.getUserIds().size() >= qParam.getGroupIds().size()) {
                    qParam.getUserIds().forEach(
                            userId -> guList.add(MwUserGroupTable
                                    .builder()
                                    .groupId(qParam.getGroupIds().get(0))
                                    .userId(userId)
                                    .build()
                            )
                    );
                } else {
                    qParam.getGroupIds().forEach(
                            groupId -> guList.add(MwUserGroupTable
                                    .builder()
                                    .groupId(groupId)
                                    .userId(qParam.getUserIds().get(0))
                                    .build()
                            )
                    );
                }
                if (qParam.getFlag() == 2 || qParam.getFlag() == 4) {
                    if (qParam.getFlag() == 4){
                        mwUserGroupMapperDao.deleteBatchByGroupId(qParam.getGroupIds());
                    } else {
                        mwUserGroupMapperDao.deleteBatchByUserId(qParam.getUserIds());
                    }
                }
                mwUserGroupMapperDao.insertBatch(guList);
            } else {
                mwUserGroupMapperDao.deleteBatchByGroupId(qParam.getGroupIds());
            }
            return Reply.ok("绑定成功");
        } catch (Exception e) {
            log.error("fail to bindUserGroup with BindUserGroupParam=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250110, ErrorConstant.USERGROUP_MSG_250110));
        }
    }

    /**
     * 新增用户组信息
     */
    @Override
    public Reply insert(AddUpdateGroupParam aParam) {
        try {
            MwGroupTable mwGroupTable = CopyUtils.copy(MwGroupTable.class, aParam);
            int count = mwGroupTableDao.selectByLoginName(mwGroupTable.getGroupName());
            if (count >0 ) {
                throw  new CheckInsertGroupException(ErrorConstant.USERGROUP_MSG_250114);
            }
            // 新增用户组数据
            String creator = iLoginCacheInfo.getLoginName();
            mwGroupTable.setCreator(creator);
            mwGroupTable.setModifier(creator);
            mwGroupTable.setEnable("ACTIVE");
            mwGroupTableDao.insert(mwGroupTable);
            // 绑定用户组和机构的关系
            List<MwUserOrgTable> userMappers = mwUserOrgMapperDao.selectByUserId(iLoginCacheInfo
                    .getCacheInfo(creator).getUserId());
            List<MwGroupOrgTable> groupOrgTables = new ArrayList<>();
            if (userMappers.size() > 0) {
                userMappers.forEach(
                        userMapper -> groupOrgTables.add(MwGroupOrgTable
                                .builder()
                                .groupId(mwGroupTable.getGroupId())
                                .orgId(userMapper.getOrgId())
                                .build()
                        )
                );
                mwGroupOrgMapperDao.insertBatch(groupOrgTables);
            }
            // 绑定用户组和用户的关系
            if (null != aParam.getUserIds() && aParam.getUserIds().size() > 0) {
                List<Integer> groupIds = new ArrayList<>();
                groupIds.add(mwGroupTable.getGroupId());
                bindUserGroup(BindUserGroupParam
                        .builder()
                        .flag(3)
                        .groupIds(groupIds)
                        .userIds(aParam.getUserIds())
                        .groupId(mwGroupTable.getGroupId())
                        .build()
                );
            }
            return Reply.ok("新增成功");
        } catch (CheckInsertGroupException e) {
            log.error("fail to insertUserGroup with AddUpdateGroupParam=【{}】, cause:【{}】",
                    aParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250114, ErrorConstant.USERGROUP_MSG_250114));
        }catch (Exception e) {
            log.error("fail to insertUserGroup with AddUpdateGroupParam=【{}】, cause:【{}】",
                    aParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250104, ErrorConstant.USERGROUP_MSG_250104));
        }
    }

    /**
     * 删除用户组信息
     */
    @Override
    public Reply delete(List<Integer> groupIds) {
        try {
            StringBuffer msg = new StringBuffer();
            StringBuffer msg1 = new StringBuffer();
            StringBuffer msg2 = new StringBuffer();
            StringBuffer msg3 = new StringBuffer();
            groupIds.forEach(
                    groupId -> {
                        String name = mwGroupTableDao.selectGroupNameById(groupId);
                        int count = mwUserGroupMapperDao.countByGroupId(groupId);
                        if (count > 0) {
                            msg1.append("、").append(name);
                        }
                      /*  count = mwGroupTableDao.countGroupAssetsByGroupId(groupId);
                        if (count > 0) {
                            msg2.append("、").append(name);
                        }
                          count = mwGroupTableDao.countGroupMonitorByGroupId(groupId);
                        if (count > 0) {
                            msg3.append("、").append(name);
                        }*/
                    }
            );
            if (StringUtils.isNotEmpty(msg1)) {
                msg.append(ErrorConstant.USERGROUP_MSG_250106)
                        .append("【")
                        .append(msg1.substring(1))
                        .append("】");
            }
            if (StringUtils.isNotEmpty(msg2)) {
                if (StringUtils.isNotEmpty(msg)) {
                    msg.append("；");
                }
                msg.append(ErrorConstant.USERGROUP_MSG_250108)
                        .append("【")
                        .append(msg2.substring(1))
                        .append("】");
            }
            if (StringUtils.isNotEmpty(msg3)) {
                if (StringUtils.isNotEmpty(msg)) {
                    msg.append("；");
                }
                msg.append(ErrorConstant.USERGROUP_MSG_250109)
                        .append("【")
                        .append(msg3.substring(1))
                        .append("】");
            }
            if (StringUtils.isNotEmpty(msg)) {
                throw new CheckDeleteGroupException(ErrorConstant.USERGROUP_MSG_250105, msg.toString());
            }
            mwGroupTableDao.delete(groupIds, iLoginCacheInfo.getLoginName());
            mwGroupOrgMapperDao.deleteBatchByGroupId(groupIds);
            // 删除用户组和资产关联关系
            mwGroupOrgMapperDao.deleteGroupMapper(groupIds);
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("fail to deleteUserGroup with groupIdList=【{}】, cause:【{}】",
                    groupIds, e.getMessage());
            if (e instanceof CheckDeleteGroupException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.USERGROUPCODE_250105, ErrorConstant.USERGROUP_MSG_250105));
            }
        }
    }

    /**
     * 用户组修改状态
     */
    @Override
    public Reply updateGroupState(UpdateGroupStateParam bParam) {
        try {
            MwGroupTable mwGroupTable = CopyUtils.copy(MwGroupTable.class, bParam);
            mwGroupTable.setModifier(iLoginCacheInfo.getLoginName());
            mwGroupTableDao.updateGroupState(mwGroupTable);
            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateUserGroupState with UpdateGroupStateParam=【{}】, cause:【{}】",
                    bParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250111, ErrorConstant.USERGROUP_MSG_250111));
        }
    }

    /**
     * 更新用户组信息
     */
    @Override
    public Reply update(AddUpdateGroupParam uParam) {
        try {
            MwGroupTable mwGroupTable = CopyUtils.copy(MwGroupTable.class, uParam);
            mwGroupTable.setModifier(iLoginCacheInfo.getLoginName());
            mwGroupTableDao.update(mwGroupTable);
            List<Integer> groupIds = new ArrayList<>();
            groupIds.add(uParam.getGroupId());
            bindUserGroup(BindUserGroupParam
                    .builder()
                    .flag(4)
                    .groupIds(groupIds)
                    .userIds(uParam.getUserIds())
                    .groupId(uParam.getGroupId())
                    .build()
            );
            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateUserGroup with AddUpdateGroupParam=【{}】, cause:【{}】",
                    uParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250103, ErrorConstant.USERGROUP_MSG_250103));
        }
    }

    /**
     * 分页查询用户组列表信息
     */
    @Override
    public Reply selectList(QueryGroupParam qsParam) {
        try {
            PageHelper.startPage(qsParam.getPageNumber(), qsParam.getPageSize());
            Map criteria = PropertyUtils.describe(qsParam);
            List<MwGroupDTO> mwScanList = mwGroupTableDao.selectList(criteria);
            //每个机构需要获取对应的机构信息
            for (MwGroupDTO group : mwScanList) {
                List<OrgDTO> orgList = mwGroupTableDao.selectOrg(group.getGroupId());
                group.setDepartment(orgList);
            }
            PageInfo pageInfo = new PageInfo<>(mwScanList);
            pageInfo.setList(mwScanList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectListGroup with QueryGroupParam=【{}】, cause:【{}】",
                    qsParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250102, ErrorConstant.USERGROUP_MSG_250102));
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    /**
     * 根据用户组ID获取用户组信息
     */
    @Override
    public Reply selectById(Integer groupId) {
        try {
            MwGroupDTO msDto = mwGroupTableDao.selectById(groupId);
            List<GroupUserDTO> userList = mwGroupTableDao.selectGroupUser(groupId);
            userList.sort(Comparator.comparingInt(GroupUserDTO::getSort));
            for (GroupUserDTO user : userList) {
                List<OrgDTO> orgList = mwGroupTableDao.selectUserOrg(user.getUserId());
                user.setUserDepartment(orgList);
            }
            msDto.setAttachUser(userList);
            return Reply.ok(msDto);
        } catch (Exception e) {
            log.error("fail to selectUserGroupById with groupId=【{}】, cause:【{}】",
                    groupId, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250101,ErrorConstant.USERGROUP_MSG_250101));
        }
    }

    /**
     * 用户组下拉框查询
     */
    @Override
    public Reply selectDropdown() {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            String roleId = iLoginCacheInfo.getRoleId(loginName);
            List<MwGroupDTO> mwScanList = new ArrayList<>();
            if ("0".equals(roleId)) {
                mwScanList = mwGroupTableDao.selectDropdown(null);
            } else {
                mwScanList = mwGroupTableDao.selectDropdown(loginName);
            }

            for (MwGroupDTO mwGroupDTO : mwScanList) {
                if (mwGroupDTO.getEnable().equals("ACTIVE")) {
                    mwGroupDTO.setEnableGroup("0");
                }else {
                    mwGroupDTO.setEnableGroup("1");
                }
            }
            return Reply.ok(mwScanList);
        } catch (Exception e) {
            log.error("fail to selectDropdown with cause:【{}】", e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250112, ErrorConstant.USERGROUP_MSG_250112));
        }
    }

    /**
     * 根据用户组ID获取用户组关联用户
     */
    @Override
    public Reply selectGroupUser(Integer groupId) {
        try {
            List<GroupUserDTO> guDTOlist = mwGroupTableDao.selectGroupUser(groupId);
            for (GroupUserDTO user : guDTOlist) {
                List<OrgDTO> orgList = mwGroupTableDao.selectUserOrg(user.getUserId());
                user.setUserDepartment(orgList);
            }
            return Reply.ok(guDTOlist);
        } catch (Exception e) {
            log.error("fail to selectGroupUserById with groupId=【{}】, cause:【{}】",
                    groupId, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250113,ErrorConstant.USERGROUP_MSG_250113));
        }
    }

    @Override
    public Reply selectGroupUsers(List<Integer> groupIds) {
        try {
            List<GroupUserDTO> guDTOlist = mwGroupTableDao.selectGroupUsers(groupIds);
            return Reply.ok(guDTOlist);
        } catch (Exception e) {
            log.error("fail to selectGroupUsers", e);
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250113,ErrorConstant.USERGROUP_MSG_250113));
        }
    }

    /**
     * 根据用户名取用户组信息
     */
    @Override
    public Reply selectByLoginName(String loginName) {
        try {
            List<MwGroupDTO> list = mwGroupTableDao.selectListByLoginName(loginName);
            log.info("Group_LOG[]Group[]用户组管理[]根据用户名取用户组信息[]{}", loginName);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to selectByLoginName , cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.USERGROUPCODE_250107, ErrorConstant.USERGROUP_MSG_250107);
        }
    }

    /**
     * 非管理员角色用户
     * @param param
     * @return
     */
    @Override
    public Reply selectCernetList(QueryGroupParam param) {
        try {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            List<MwGroupDTO> list = mwGroupTableDao.selectListByLoginName(param.getLoginName());
            for (MwGroupDTO group : list) {
                List<OrgDTO> orgList = mwGroupTableDao.selectOrg(group.getGroupId());
                group.setDepartment(orgList);
            }
            log.info("Group_LOG[]Group[]用户组管理[]根据用户名取用户组信息[]{}", param.getLoginName());
            PageInfo<?> pageInfo = new PageInfo(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectCernetList with QueryGroupParam=【{}】, cause:【{}】",
                    param, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250107, ErrorConstant.USERGROUP_MSG_250107));
        }
    }

    @Override
    public Reply getCernetGroup(QueryGroupParam param) {
        try {
            Reply reply = null;
            String loginName = loginCacheInfo.getLoginName();
            String roleId = loginCacheInfo.getRoleId(loginName);
            param.setLoginName(loginName);
            if (MWUtils.ROLE_TOP_ID.equals(roleId)) {
                reply = selectList(param);
            } else {
                reply = selectCernetList(param);
            }
            return Reply.ok(reply);
        } catch (Exception e) {
           return Reply.fail(ErrorConstant.USERGROUPCODE_250102, ErrorConstant.USERGROUP_MSG_250102);
        }
    }

    /**
     * 获取模糊查询内容
     * @param qParam 请求参数
     * @return 模糊查询列表数据
     */
    @Override
    public Reply getFuzzySearchContent(QueryGroupParam qParam) {
        try {
            //获取所有的用户数据
            Map criteria = PropertyUtils.describe(qParam);
            Map resultMap = new HashMap();
            List<MwGroupDTO> groupList = mwGroupTableDao.selectList(criteria);
            List<String> nameList = new ArrayList<>();
            for (MwGroupDTO group : groupList) {
                nameList.add(group.getGroupName());
            }
            //进行数据排序
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            nameList = nameList.stream()
                    .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1), pinyin4jUtil.getStringPinYin(o2)))
                    .collect(Collectors.toList());
            //保存数据
            resultMap.put("groupName", nameList);
            resultMap.put("allList", nameList);
            return Reply.ok(resultMap);
        } catch (Exception e) {
            log.error("获取用户组模糊查询数据失败", e);
            return Reply.fail(ErrorConstant.USERGROUPCODE_250102, ErrorConstant.USERGROUP_MSG_250102);
        }
    }

    @Override
    public Reply<Map<Integer, List<UserGroupDTO>>> getUserListByGroupIds(List<Integer> groupIds) {
        List<UserGroupDTO> userListByGroupIds = mwUserGroupMapperDao.getUserListByGroupIds(groupIds);
        Map<Integer, List<UserGroupDTO>> collect = userListByGroupIds.stream().collect(Collectors.groupingBy(UserGroupDTO::getGroupId));
        return Reply.ok(collect);
    }


//    /**
//     * 根据用户名取用户组信息
//     */
//    @Override
//    public Reply selectByLoginName() {
//        try {
//            String loginName= iLoginCacheInfo.getLoginName();
//            List<MwGroupDTO> list = mwGroupTableDao.selectListByLoginName(loginName);
//            log.info("Group_LOG[]Group[]用户组管理[]根据用户名取用户组信息[]{}", loginName);
//            return Reply.ok(list);
//        } catch (Exception e) {
//            log.error("fail to selectByLoginName , cause:{}", e.getMessage());
//            return Reply.fail(ErrorConstant.USERGROUPCODE_250107, ErrorConstant.USERGROUP_MSG_250107);
//        }
//    }
//
//    @Override
//    public Reply selectDropdownGroupView() {
//        try {
//            String loginName = iLoginCacheInfo.getLoginName();
//            List<MwGroupDTO> mwScanList = mwGroupTableDao.selectDropdown(loginName);
//            List<UserGroupView> userGroupViewList = new ArrayList<UserGroupView>();
//            if (null != mwScanList && mwScanList.size() > 0 ){
//                mwScanList.forEach(
//                        value -> {
//                            userGroupViewList.add(UserGroupView
//                                    .builder()
//                                    .value(value.getGroupId())
//                                    .label(value.getGroupName())
//                                    .build()
//                            );
//                        }
//                );
//            }
//            log.info("Group_LOG[]Group[]用户组管理[]用户组下拉框信息[]{}[]", mwScanList);
//            return Reply.ok(userGroupViewList);
//        } catch (Exception e) {
//            log.error("fail to selectListGroup with qsParam={}, cause:{}", e.getMessage());
//            return Reply.fail(ErrorConstant.USERGROUPCODE_250102, ErrorConstant.USERGROUP_MSG_250102);
//        }
//    }

}
