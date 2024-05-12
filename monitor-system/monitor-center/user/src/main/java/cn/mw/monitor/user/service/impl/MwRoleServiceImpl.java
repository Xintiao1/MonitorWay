package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.exception.*;
import cn.mw.monitor.api.param.role.AddUpdateModuleParam;
import cn.mw.monitor.api.param.role.AddUpdateRoleParam;
import cn.mw.monitor.api.param.role.QueryRoleParam;
import cn.mw.monitor.api.param.role.UpdateRoleStateParam;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.service.user.dto.MwSubUserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.Auth;
import cn.mw.monitor.service.user.model.MwRoleModulePermMapper;
import cn.mw.monitor.service.user.model.PageAuth;
import cn.mw.monitor.user.dao.*;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.model.MwModule;
import cn.mw.monitor.user.model.MwRole;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.user.service.MwModuleService;
import cn.mw.monitor.user.service.MwRoleService;
import cn.mw.monitor.util.ErrorMsgUtils;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.mw.monitor.common.constant.ErrorConstant.USER_100112;

@Service
@Slf4j
@Transactional
public class MwRoleServiceImpl implements MwRoleService {
    public static final Integer ROOT_ROLE = 0;
    @Resource
    MwRoleDao mwRoleDao;

    @Resource
    MwUserRoleMapperDao mwUserRoleMapperDao;

    @Resource
    MwRoleModulePermMapperDao mwRoleModulePermMapperDao;

    @Resource
    MwModuleDao mwModuleDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwModuleService mwModuleService;

    @Autowired
    private MWUserService mwUserService;

    @Resource
    private MWADUserDao mwadUserDao;

    private List<PageAuth> addPageAuth(List<PageAuth> pageAuths, PageAuth pageAuth) {
        pageAuths.add(pageAuth);
        if (pageAuth.getChildren() != null && pageAuth.getChildren().size() > 0) {
            pageAuth.getChildren().forEach(
                    children -> addPageAuth(pageAuths, children)
            );
        }
        return pageAuths;
    }

    private List<MwRoleModulePermMapper> getMwRoleModulePermMapperList(List<PageAuth> pageAuth,
                                                                       MwRole mwRole) {
        List<MwRoleModulePermMapper> mwRoleModulePermMapperList = new ArrayList<>();
        List<PageAuth> pageAuthList = new ArrayList<>();
        pageAuth.forEach(
                aPageAuth -> addPageAuth(pageAuthList, aPageAuth)
        );
        Field[] fields = Auth.class.getDeclaredFields();
        pageAuthList.forEach(
                pAuth -> {
                    for (Field field : fields) {
                        StringBuffer sb = new StringBuffer();
                        sb.append(mwRole.getId())
                                .append("-")
                                .append(pAuth.getPageId())
                                .append("-")
                                .append(field.getName());
                        MwRoleModulePermMapper mwRoleModulePermMapper = MwRoleModulePermMapper
                                .builder()
                                .id(sb.toString())
                                .roleId(mwRole.getId())
                                .moduleId(pAuth.getPageId())
                                .permName(field.getName())
                                .build();
                        if ("browse".equals(field.getName())) {
                            mwRoleModulePermMapper.setEnable(pAuth.getAuth().getBrowse());
                        } else if ("create".equals(field.getName())) {
                            mwRoleModulePermMapper.setEnable(pAuth.getAuth().getCreate());
                        } else if ("editor".equals(field.getName())) {
                            mwRoleModulePermMapper.setEnable(pAuth.getAuth().getEditor());
                        } else if ("delete".equals(field.getName())) {
                            mwRoleModulePermMapper.setEnable(pAuth.getAuth().getDelete());
                        } else if ("perform".equals(field.getName())) {
                            mwRoleModulePermMapper.setEnable(pAuth.getAuth().getPerform());
                        } else if ("secopassword".equals(field.getName())) {
                            mwRoleModulePermMapper.setEnable(pAuth.getAuth().getSecopassword());
                        }
                        mwRoleModulePermMapperList.add(mwRoleModulePermMapper);
                    }
                }
        );
        return mwRoleModulePermMapperList;
    }

    /**
     * 新增角色信息
     */
    @Override
    public Reply insert(AddUpdateRoleParam auParam) {
        try {
            if (null == auParam.getPageAuth() || auParam.getPageAuth().size() == 0) {
                String msg = Reply.replaceMsg(ErrorConstant.USER_MSG_100112, new String[]{"模块数据"});
                throw new CheckInsertRoleException(USER_100112, msg);
            }
            /*
             * 新建角色时 如果首页浏览权限没有被选中 当其他模块的浏览权限也都没有选中时 绑定该角色的用户登录 会报404
             * */
            if (auParam.getPageAuth().get(0).getAuth().getBrowse() == false) {
                String msg = Reply.replaceMsg(ErrorConstant.USER_MSG_100112, new String[]{auParam.getPageAuth().get(0).getPageName() + "浏览权限"});
                throw new CheckInsertRoleException(USER_100112, msg);
            }

            //角色名不能重复
            int count = mwRoleDao.selectByRoleName(auParam.getRoleName());
            if (count > 0) {
                throw new CheckInsertRoleException(ErrorConstant.ROLE_100314, ErrorConstant.ROLE_MSG_100314);
            }

            MwRole mwRole = CopyUtils.copy(MwRole.class, auParam);
            String creator = iLoginCacheInfo.getLoginName();
            mwRole.setCreator(creator);
            mwRole.setModifier(creator);
            mwRoleDao.insert(mwRole);
            List<MwRoleModulePermMapper> mwRoleModulePermMapperList =
                    getMwRoleModulePermMapperList(auParam.getPageAuth(), mwRole);
            mwRoleModulePermMapperDao.insert(mwRoleModulePermMapperList);
            return Reply.ok("新增成功");
        } catch (Exception e) {
            log.error("fail to insertRole with AddUpdateRoleParam=【{}】, cause:【{}】",
                    auParam, e.getMessage());
            if (e instanceof CheckInsertRoleException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.ROLE_100301, ErrorConstant.ROLE_MSG_100301));
            }
        }
    }

    /**
     * 更新角色信息
     */
    @Override
    public Reply update(AddUpdateRoleParam auParam) {
        try {
            //判断是否为系统角色
            MwRole roleInfo = mwRoleDao.selectByRoleId(auParam.getRoleId());
            if (Constants.SYSTEM_ROLE.equals(roleInfo.getRoleType())) {
                throw new CheckInsertRoleException(ErrorConstant.MODULE_100218, ErrorConstant.MODULE_MSG_100218);
            }

            if (null == auParam.getPageAuth() || auParam.getPageAuth().size() == 0) {
                String msg = Reply.replaceMsg(ErrorConstant.USER_MSG_100112, new String[]{"模块数据"});
                throw new CheckInsertRoleException(USER_100112, msg);
            }

            /*
             * 编辑角色时 如果首页浏览权限没有被选中 当其他模块的浏览权限也都没有选中时 绑定该角色的用户登录 会报404
             * */
            if (auParam.getPageAuth().get(0).getAuth().getBrowse() == false) {
                String msg = Reply.replaceMsg(ErrorConstant.USER_MSG_100112, new String[]{auParam.getPageAuth().get(0).getPageName() + "浏览权限"});
                throw new CheckInsertRoleException(USER_100112, msg);
            }

            MwRole mwRole = CopyUtils.copy(MwRole.class, auParam);
          /*  List<MwSubUserDTO> mwSubUserDTOS = mwRoleDao.selectMwUserByRoleId(mwRole.getRoleId());
            if (mwSubUserDTOS != null && mwSubUserDTOS.size() > 0) {
                throw new CheckUpdateRoleException(ErrorConstant.ROLE_100315,ErrorConstant.ROLE_MSG_100315);
            }*/
            mwRole.setId(mwRole.getRoleId());
            mwRole.setModifier(iLoginCacheInfo.getLoginName());
            mwRoleDao.update(mwRole);
            mwRoleModulePermMapperDao.deleteByRoleId(auParam.getRoleId());
            List<MwRoleModulePermMapper> mwRoleModulePermMapperList =
                    getMwRoleModulePermMapperList(auParam.getPageAuth(), mwRole);
            mwRoleModulePermMapperDao.insert(mwRoleModulePermMapperList);
            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateRole with AddUpdateRoleParam=【{}】, cause:【{}】",
                    auParam, e.getMessage());
            if (e instanceof CheckInsertRoleException || e instanceof CheckUpdateRoleException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.ROLE_100313, ErrorConstant.ROLE_MSG_100313));
            }
        }
    }

    /**
     * 删除角色信息
     */
    @Override
    public Reply delete(List<Integer> roleIds) {
        try {
            List<Reply> faillist = new ArrayList<Reply>();
            roleIds.forEach(
                    roleId -> {
                        String name = mwRoleDao.selectRoleNameById(roleId);
                        Integer count = mwUserRoleMapperDao.countByRoleId(roleId);
                        if (count > 0) {
                            faillist.add(Reply.fail(ErrorConstant.ROLE_MSG_100311, name));
                        } else {
                            int adMapperCount = mwadUserDao.countAdRoleMapper(roleId);
                            if (adMapperCount > 0) {
                                faillist.add(Reply.fail(ErrorConstant.ROLE_MSG_100316, name));
                            }
                        }
                        MwRole mwRole = mwRoleDao.selectByRoleId(roleId);
                        if (Constants.SYSTEM_ROLE.equals(mwRole.getRoleType())) {
                            throw new CheckDeleteRoleException(ErrorConstant.MODULE_MSG_100218, name);
                        }
                    }
            );
            StringBuffer msg = new StringBuffer();
            if (faillist.size() > 0) {
                faillist.forEach(
                        reply -> msg.append("【" + reply.getData() + "】")
                );
                String errorMsg = faillist.get(0).getMsg();
                throw new CheckDeleteRoleException(errorMsg, msg.toString());
            }
            mwRoleDao.delete(roleIds, iLoginCacheInfo.getLoginName());
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("fail to deleteRole with roleId=【{}】, cause:【{}】",
                    roleIds, e.getMessage());
            if (e instanceof CheckDeleteRoleException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.ROLE_100302, ErrorConstant.ROLE_MSG_100302));
            }
        }
    }

    /**
     * 角色修改状态
     */
    @Override
    public Reply updateRoleState(UpdateRoleStateParam dParam) {
        try {
            MwRole mwRole = mwRoleDao.selectByRoleId(dParam.getRoleId());
            Integer count = mwUserRoleMapperDao.countByRoleId(dParam.getRoleId());
            if (count > 0) {
                throw new CheckChangeRoleException(ErrorConstant.ROLE_MSG_100312, mwRole.getRoleName());
            }
            if (Constants.SYSTEM_ROLE.equals(mwRole.getRoleType())) {
                throw new CheckChangeRoleException(ErrorConstant.MODULE_MSG_100218, mwRole.getRoleName());
            }
            dParam.setModifier(iLoginCacheInfo.getLoginName());
            mwRoleDao.updateUserState(dParam);
            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateRoleState with UpdateRoleStateParam=【{}】, cause:【{}】",
                    dParam, e.getMessage());
            if (e instanceof CheckChangeRoleException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.ROLE_100303, ErrorConstant.ROLE_MSG_100303));
            }
        }
    }

    /**
     * 分页查询角色列表信息
     */
    @Override
    public Reply selectList(QueryRoleParam qParam) {
        try {
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            Map criteria = PropertyUtils.describe(qParam);
            List<MwRoleDTO> roles = mwRoleDao.selectList(criteria);
            for (MwRoleDTO role : roles) {
                List<MwSubUserDTO> userList = mwRoleDao.selectUser(role.getRoleId());
                for (MwSubUserDTO user : userList) {
                    List<OrgDTO> orgList = mwRoleDao.selectOrg(user.getUserId());
                    user.setDepartment(orgList);
                }
                role.setUserDTOS(userList);
            }
            PageInfo<?> pageInfo = new PageInfo<>(roles);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectList with QueryRoleParam=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ROLE_100310, ErrorConstant.ROLE_MSG_100310));
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    /**
     * 根据角色ID查询角色信息
     */
    @Override
    public Reply selectByRoleId(Integer roleId) {
        try {
            MwRole mwrole = mwRoleDao.selectByRoleId(roleId);
            return Reply.ok(mwrole);
        } catch (Exception e) {
            log.error("fail to selectByRoleId with roleId=【{}】, cause:【{}】",
                    roleId, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ROLE_100310, ErrorConstant.ROLE_MSG_100310));
        }
    }

    /**
     * 角色下拉框查询
     */
    @Override
    public Reply selectDorpdownList() {
        try {
            //非系统管理员用户只能选中自身
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<MwRoleDTO> mworges = mwRoleDao.selectDropdownList();
            if (!userInfo.isSystemUser()) {
                for (MwRoleDTO role : mworges) {
                    if (userInfo.getRoleId().equals(String.valueOf(role.getRoleId()))) {
                        mworges.clear();
                        mworges.add(role);
                        break;
                    }
                }
            }
            return Reply.ok(mworges);
        } catch (Exception e) {
            log.error("fail to selectDorpdownList with cause:【{}】", e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ROLE_100310, ErrorConstant.ROLE_MSG_100310));
        }
    }

    /**
     * 根据用户ID取角色信息
     */
    @Override
    public MwRoleDTO selectByUserId(Integer userId) {
        try {
            MwRole mwRole = mwRoleDao.selectByUserId(userId);
            MwRoleDTO mwRoleDTO = CopyUtils.copy(MwRoleDTO.class, mwRole);
            return mwRoleDTO;
        } catch (Exception e) {
            log.error("fail to selectByUserId with cause:【{}】", e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ROLE_100310, ErrorConstant.ROLE_MSG_100310));
        }
    }

    /**
     * 新增模块信息
     */
    @Override
    public Reply insertRoleModule(AddUpdateModuleParam mParam) {
        try {
            MwModule mwModule = CopyUtils.copy(MwModule.class, mParam);
            //新增模块信息
            int count = mwModuleDao.selectMaxModuleId() + 1;
            mwModule.setId(count);
            //判断是否不为一级模块
            if (null != mwModule.getPid() && mwModule.getPid() != ROOT_ROLE) {
                mwModuleDao.updateIsNoteById(mwModule.getPid(), false);
                Map<String, Object> map = mwModuleDao.selectDeepNodesById(mwModule.getPid());
                mwModule.setDeep((Integer) map.get("deep") + 1);
                mwModule.setNodes(map.get("nodes").toString() + count + ",");
            } else {
                mwModule.setDeep(1);
                mwModule.setPid(ROOT_ROLE);
                mwModule.setNodes("," + count + ",");
            }
            //编辑模块内部信息
            mwModule.setEnable("1");
            mwModule.setVersion(0);
            mwModule.setIsNode(true);
            mwModule.setDeleteFlag(false);
            mwModuleDao.insert(mwModule);
            return Reply.ok("新增成功！");
        } catch (Exception e) {
            log.error("fail to insertRoleModele with AddUpdateModuleParam=【{}】, cause:【{}】",
                    mParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100213, ErrorConstant.MODULE_MSG_100213));
        }
    }

    @Override
    public Reply deleteRoleModule(List<Integer> ids) {
        try {
            Map<String, StringBuffer> maps = new HashMap<>();
            ids.forEach(
                    id -> {
                        MwModule mwModule = mwModuleDao.selectModuleById(id);
                        String name = mwModule.getModuleName();
                        int count = mwModuleDao.countModuleByPid(id);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.MODULE_MSG_100214) == null) {
                                maps.put(ErrorConstant.MODULE_MSG_100214, new StringBuffer());
                            }
                            maps.get(ErrorConstant.MODULE_MSG_100214).append("、").append(name);
                        }
                    }
            );

            String msg = ErrorMsgUtils.getErrorMsg(maps);
            if (StringUtils.isNotEmpty(msg)) {
                throw new CheckDeleteModuleException(ErrorConstant.MODULE_MSG_100215, msg);
            }
            mwModuleDao.deleteModuleByIds(ids);
            return Reply.ok("删除成功！");
        } catch (Exception e) {
            log.error("fail to deleteModule with moduleIds=【{}】, cause:【{}】",
                    ids, e.getMessage());
            if (e instanceof CheckDeleteModuleException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.MODULE_100215, ErrorConstant.MODULE_MSG_100215));
            }
        }
    }

    @Override
    public Reply updateRoleModule(AddUpdateModuleParam mParam) {
        try {
            MwModule mwModule = CopyUtils.copy(MwModule.class, mParam);
            mwModuleDao.updateModule(mwModule);
            return Reply.ok("更新成功！");
        } catch (Exception e) {
            log.error("fail to updateModule with AddUpdateModuleParam=【{}】, cause:【{}】",
                    mParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100216, ErrorConstant.MODULE_MSG_100216));
        }
    }

    /**
     * 根据角色ID复制角色数据
     *
     * @param qParam 角色数据
     * @return
     */
    @Override
    public Reply copyRole(QueryRoleParam qParam) {
        try {
            //先获取角色数据
            MwRole mwrole = mwRoleDao.selectByRoleId(qParam.getRoleId());
            //获取权限配置
            Reply modulReply = mwModuleService.roleModulePermMapperBrowse(qParam.getRoleId());
            List<PageAuth> pageAuths = (List<PageAuth>) modulReply.getData();
            //复制角色
            AddUpdateRoleParam addParam = new AddUpdateRoleParam();
            addParam.setPageAuth(pageAuths);
            addParam.setRoleName(mwrole.getRoleName() + "_复制");
            addParam.setRoleDesc(mwrole.getRoleDesc());
            addParam.setAllowLogin(mwrole.getAllowLogin());
            addParam.setDataPerm(mwrole.getDataPerm());
            return this.insert(addParam);
        } catch (Exception e) {
            log.error("复制角色失败", e);
            return Reply.fail(e.getMessage());
        }
    }
}

