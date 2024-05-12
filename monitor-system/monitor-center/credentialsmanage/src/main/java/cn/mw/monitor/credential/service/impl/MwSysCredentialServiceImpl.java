package cn.mw.monitor.credential.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.credential.api.param.MwSysCredParam;
import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.common.MwModulesDTO;
import cn.mw.monitor.credential.dao.MwSysCredentialDao;
import cn.mw.monitor.credential.model.MwSysCredential;
import cn.mw.monitor.credential.service.MwSysCredentialService;
import cn.mw.monitor.credential.util.MapResultHandler;
import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaoy
 * @since 2021-05-31 14:16:07
 */
@Slf4j
@Transactional
@Service("mwSysCredentialService")
public class MwSysCredentialServiceImpl implements MwSysCredentialService {
    @Resource
    private MwSysCredentialDao mwSysCredentialDao;

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWUserService mwUserService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    /**
     * 系统凭据下拉框查询
     *
     * @return 实例对象
     */
    @Override
    public Reply selectCredDropDown(MwSysCredParam param) {

        MapResultHandler<String, String> credResultHandler = new MapResultHandler<>();
        try {
            Map criteria = new HashMap();
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> typeIdList = mwUserService.getAllTypeIdList(userInfo, DataType.CREDENTIAL);
            criteria.put("list", typeIdList);
            criteria.put("systemUser", userInfo.isSystemUser());
            criteria.put("moduleId", param.getModuleId());
            mwSysCredentialDao.selectCredDropDown(credResultHandler, criteria);
            Map<String, String> mappedResults = credResultHandler.getMappedResults();
            for (Map.Entry<String, String> entry : mappedResults.entrySet()) {
                mappedResults.put(entry.getKey(), RSAUtils.decryptData(entry.getValue(), RSAUtils.RSA_PRIVATE_KEY));
            }
            return Reply.ok(mappedResults);
        } catch (Exception e) {
            log.error(" fail to select sys credential dropdown list ", e);
            return Reply.fail(ErrorConstant.CRED_317005, ErrorConstant.CRED_MSG_317005);
        }
    }


    /**
     * 查询系统和凭据列表
     *
     * @return 对象列表
     */
    @Override
    public Reply pageCredential(QueryCredentialParam param) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            Map<String, Object> describe = PropertyUtils.describe(param);
            List<String> typeIdList = mwUserService.getAllTypeIdList(userInfo,DataType.CREDENTIAL);
            describe.put("list",typeIdList);
            describe.put("systemUser", userInfo.isSystemUser());
            List<MwSysCredential> mscs = new ArrayList<>();
            try {
                PageHelper.startPage(param.getPageNumber(), param.getPageSize());
                mscs = mwSysCredentialDao.select(describe);
                mscs.forEach(
                        m -> m.setMwPasswd("********")
                );
            } catch (Exception e) {
                log.error("select sys credential page  failed :", e);
                return Reply.fail(ErrorConstant.CRED_317004, ErrorConstant.CRED_MSG_317004);
            }
            PageInfo<MwSysCredential> pageInfo = new PageInfo<>(mscs);
            return Reply.ok(pageInfo);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(" fail to browse sys credential list ", e);
            return Reply.fail(ErrorConstant.CRED_317004, ErrorConstant.CRED_MSG_317004);
        }
    }

    /**
     * 判断当前用户是不是系统管理员角色
     */
    public boolean isRoleTopId() {
        String loginName = loginCacheInfo.getLoginName();
        String roleId = loginCacheInfo.getRoleId(loginName);
        return MWUtils.ROLE_TOP_ID.equals(roleId);
    }

    /**
     * 新增系统凭据
     */
    @Override
    public Reply insert(MwSysCredential msc) {
        try {
            String creator = loginCacheInfo.getLoginName();
            msc.setCreator(creator);
            msc.setCredDetails(getCredDetails(msc));
            mwSysCredentialDao.insert(msc);
            //新增数据权限
            addMapperAndPerm(msc);
        } catch (Exception e) {
            log.error(" fail to insert sys credential ", e);
            return Reply.fail(ErrorConstant.CRED_MSG_317001, ErrorConstant.CRED_MSG_317001);
        }
        return Reply.ok("新增系统凭据成功!");
    }

    public String getCredDetails(MwSysCredential msc) {
        String credDetails = null;
        if (StringUtils.isNotBlank(msc.getCredDesc())) {
            credDetails = msc.getMwAccount() + " " + "(" + msc.getCredDesc() + ")";
        } else {
            credDetails = msc.getMwAccount();
        }
        return credDetails;
    }

    /**
     * 修改数据
     *
     * @param mwSysCredential 实例对象
     * @return 实例对象
     */
    @Override
    public Reply update(MwSysCredential mwSysCredential) {
        try {
            mwSysCredential.setCredDetails(getCredDetails(mwSysCredential));
            mwSysCredentialDao.updateByPrimaryKeySelective(mwSysCredential);
            //保存数据权限数据
            deleteMapperAndPerm(mwSysCredential);
            addMapperAndPerm(mwSysCredential);
        } catch (Exception e) {
            log.error(" fail to update sys credential ", e);
            return Reply.fail(ErrorConstant.CRED_MSG_317002, ErrorConstant.CRED_MSG_317002);
        }
        return Reply.ok("修改系统凭据成功!");
    }

    /**
     * 通过主键删除数据
     *
     * @param ids 主键集合
     * @return 是否成功
     */
    @Override
    public Reply deleteById(List<Integer> ids) {
        try {
            MwSysCredential credential = new MwSysCredential();
            ids.forEach(
                    id -> {
                        credential.setId(id);
                        mwSysCredentialDao.deleteByPrimaryKey(id);
                        deleteMapperAndPerm(credential);
                    }
            );
        } catch (Exception e) {
            log.error("fail to delete sys credential with ids={}", ids, e);
            return Reply.fail(ErrorConstant.CRED_MSG_317003, ErrorConstant.CRED_MSG_317003);
        }
        return Reply.ok("删除系统凭据成功!");
    }

    @Override
    public Reply getModulesDropDown() {
        List<MwModulesDTO> modules = new ArrayList<>();
        try {
            modules = mwSysCredentialDao.selectAllModules();
            return Reply.ok(modules);
        } catch (Exception e) {
            log.error("fail to select all modules", e);
            return null;
        }
    }

    @Override
    public Reply selectCredById(Integer credId) {

        try {
            MwSysCredential sysCred = mwSysCredentialDao.selectByPrimaryKey(credId);
            sysCred.setMwPasswd("********");
            //更新用户的负责人，机构，用户组
            updateCredentialInfo(sysCred);
            return Reply.ok(sysCred);
        } catch (Exception e) {
            log.error("fail to select sys cred by id :", e);
            return Reply.fail(ErrorConstant.CRED_MSG_317006, ErrorConstant.CRED_317006);
        }
    }

    /**
     * 更新凭证信息
     *
     * @param sysCred 凭证数据
     */
    private void updateCredentialInfo(MwSysCredential sysCred) {
        //获取组织列表
        List<OrgDTO> orgList = mwOrgCommonService.getAllOrgList(sysCred.getId(), DataType.CREDENTIAL);
        if (CollectionUtils.isNotEmpty(orgList)) {
            List<List<Integer>> orgNodes = new ArrayList<>();
            orgList.forEach(department -> {
                        List<Integer> orgIds = new ArrayList<>();
                        List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                        nodes.forEach(node -> {
                            if (!"".equals(node)) {
                                orgIds.add(Integer.valueOf(node));
                            }
                        });
                        orgNodes.add(orgIds);
                    }
            );
            sysCred.setOrgIds(orgNodes);
        }
        //获取负责人列表
        List<UserDTO> userList = mwUserService.getAllUserList(sysCred.getId(), DataType.CREDENTIAL);
        if (CollectionUtils.isNotEmpty(userList)) {
            List<Integer> userIdList = new ArrayList<>();
            userList.forEach(userDTO -> {
                userIdList.add(userDTO.getUserId());
            });
            sysCred.setPrincipal(userIdList);
        }
        //获取用户组列表
        List<GroupDTO> groupList = mwUserGroupCommonService.getAllGroupList(sysCred.getId(), DataType.CREDENTIAL);
        if (CollectionUtils.isNotEmpty(groupList)) {
            List<Integer> groupIdList = new ArrayList<>();
            groupList.forEach(groupDTO -> {
                groupIdList.add(groupDTO.getGroupId());
            });
            sysCred.setGroupIds(groupIdList);
        }
    }

    /**
     * 删除负责人，用户组，机构 权限关系
     *
     * @param mwSysCredential 凭据数据
     */
    private void deleteMapperAndPerm(MwSysCredential mwSysCredential) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(mwSysCredential.getId()))
                .type(DataType.CREDENTIAL.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 添加負責人
     *
     * @param mwSysCredential 凭据数据
     */
    private void addMapperAndPerm(MwSysCredential mwSysCredential) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(mwSysCredential.getGroupIds())  //用户组
                .userIds(mwSysCredential.getPrincipal()) //责任人
                .orgIds(mwSysCredential.getOrgIds()) //机构
                .typeId(String.valueOf(mwSysCredential.getId()))    //凭据数据主键
                .type(DataType.CREDENTIAL.getName())  //类别为凭据
                .desc(DataType.CREDENTIAL.getDesc()).build(); //凭据
        mwCommonService.addMapperAndPerm(insertDto);
    }
}
