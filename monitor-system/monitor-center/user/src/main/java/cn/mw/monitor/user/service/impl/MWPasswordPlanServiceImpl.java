package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.user.dto.MwSubUserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.api.exception.CheckDeletePasswdPlanException;
import cn.mw.monitor.api.exception.CheckInsertPasswdPlanException;
import cn.mw.monitor.api.exception.CheckUpdatePasswdPlanException;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.api.param.passwdPlan.AddUpdatePasswdPlanParam;
import cn.mw.monitor.api.param.passwdPlan.QueryPasswdPlanParam;
import cn.mw.monitor.api.param.passwdPlan.UpdatePasswdPlanStateParam;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.user.dao.MWPasswdDAO;
import cn.mw.monitor.user.dao.MwPasswdplanOrgMapperDao;
import cn.mw.monitor.service.user.dto.MWPasswordPlanDTO;
import cn.mw.monitor.user.dto.MwPasswdPlanDTO;
import cn.mw.monitor.user.dto.MwUserDTO;
import cn.mw.monitor.user.dto.PasswdPlanDTO;
import cn.mw.monitor.service.user.model.MWPasswdPlan;
import cn.mw.monitor.user.model.MwPasswdplanOrgMapper;
import cn.mw.monitor.user.service.MWPasswordPlanService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MWPasswordPlanServiceImpl implements MWPasswordPlanService {

    /**
     * 默认密码策略ID
     */
    public static final Integer DEFAULT_PASSWD_ID = 1;

    @Resource
    private MWPasswdDAO mwPasswdDAO;

    @Resource
    private MwPasswdplanOrgMapperDao mwPasswdplanOrgMapperDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWUserServiceImpl mwUserServiceImpl;

    @Resource
    private MWUserDao mwUserDao;

    private void addOrgPasswdMapper(List<List<Integer>> departments, Integer passwdId) {
        if (null != departments && departments.size() > 0) {
            List<MwPasswdplanOrgMapper> mwPasswdplanOrgMapperList = new ArrayList<>();
            List<Integer> orgIds = new ArrayList<>();
            departments.forEach(
                    department -> {
                        mwPasswdplanOrgMapperList.add(MwPasswdplanOrgMapper
                                .builder()
                                .orgId(department.get(department.size() - 1))
                                .passwdId(passwdId)
                                .build()
                        );
                        orgIds.add(department.get(department.size() - 1));
                    }
            );
            mwPasswdplanOrgMapperDao.createOrgPasswdMapper(mwPasswdplanOrgMapperList);
            // 新增的密码策略关联机构后，提醒关联机构下的所有用户变更密码
//                List<Integer> userIds = mwUserOrgMapperDao.selectUserIdByOrgId(orgIds);
//                if (userIds.size() > 0) {
//
//                }
        }
    }

    /**
     * 新增密码策略
     */
    @Override
    public Reply addPasswordPlan(AddUpdatePasswdPlanParam passwordPlan) {
        try {
            // 如果是永久有效就将时间置空
            if (passwordPlan.getPasswdExpireType()) {
                passwordPlan.setPasswdUpdateDate(null);
            }
            MWPasswdPlan mwPasswdPlan = CopyUtils.copy(MWPasswdPlan.class, passwordPlan);
            int count = mwPasswdDAO.selectByPasswdName(mwPasswdPlan.getPasswdName());
            if (count > 0) {
                throw new CheckInsertPasswdPlanException(ErrorConstant.PASSWDPLAN_100611,ErrorConstant.PASSWDPLAN_MSG_100611);
            }
            String loginName = iLoginCacheInfo.getLoginName();
            mwPasswdPlan.setVersion(0);
            mwPasswdPlan.setPasswdState("ACTIVE");
            mwPasswdPlan.setCreator(loginName);
            mwPasswdPlan.setModifier(loginName);
            mwPasswdPlan.setDeleteFlag(false);
            mwPasswdDAO.insert(mwPasswdPlan);
            addOrgPasswdMapper(passwordPlan.getDepartment(), mwPasswdPlan.getPasswdId());
            return Reply.ok("新增成功！");
        } catch (Exception e) {
            log.error("fail to addPasswordPlan with AddUpdatePasswdPlanParam=【{}】, cause:【{}】",
                    passwordPlan, e.getMessage());
            if (e instanceof CheckInsertPasswdPlanException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.PASSWDPLAN_100602, ErrorConstant.PASSWDPLAN_MSG_100602));
            }
        }
    }

    /**
     * 删除密码策略
     */
    @Override
    public Reply delete(List<Integer> passwdPlanIds) {
        try {
            List<Reply> faillist = new ArrayList<>();
            passwdPlanIds.forEach(
                    passwdId -> {
                        if (passwdId == DEFAULT_PASSWD_ID) {
                            throw new CheckDeletePasswdPlanException(ErrorConstant.PASSWDPLAN_100603, ErrorConstant.PASSWDPLAN_MSG_100603);
                        }
                        String name = mwPasswdDAO.selectPasswdNameById(passwdId);
                        int count = mwPasswdDAO.countUserById(passwdId);
                        if (count > 0) {
                            faillist.add(Reply.fail(ErrorConstant.PASSWDPLAN_MSG_100604, name));
                        }
                    }
            );
            StringBuffer msg = new StringBuffer();
            if (faillist.size() > 0) {
                faillist.forEach(
                        reply -> msg.append(reply.getMsg() + "【" + reply.getData() + "】")
                );
                throw new CheckDeletePasswdPlanException(ErrorConstant.PASSWDPLAN_MSG_100605, msg.toString());
            }
            mwPasswdDAO.delete(passwdPlanIds, iLoginCacheInfo.getLoginName());
            mwPasswdplanOrgMapperDao.deletePasswdOrgMapper(passwdPlanIds);
            return Reply.ok("删除成功！");
        } catch (Exception e) {
            log.error("fail to deletePasswdPlan with passwdPlanIds=【{}】, cause:【{}】",
                    passwdPlanIds, e.getMessage());
            if (e instanceof CheckDeletePasswdPlanException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.PASSWDPLAN_100605, ErrorConstant.PASSWDPLAN_MSG_100605));
            }
        }
    }

    /**
     * 修改密码策略状态
     */
    @Override
    public Reply updatePasswdState(UpdatePasswdPlanStateParam uParam){
        try {
            if (uParam.getPasswdId().equals(DEFAULT_PASSWD_ID)) {
                throw new CheckUpdatePasswdPlanException(ErrorConstant.PASSWDPLAN_100606, ErrorConstant.PASSWDPLAN_MSG_100606);
            }

            //关闭密码策略 密码策略下的用户密码策略为默认密码策略
            QueryPasswdPlanParam param = new QueryPasswdPlanParam();
            param.setPasswdId(uParam.getPasswdId());
            Map criteria = PropertyUtils.describe(param);
            MwPasswdPlanDTO mwPasswdPlanDTO = mwPasswdDAO.selectList(criteria).get(0);
            List<MwSubUserDTO> userDTOs = mwPasswdDAO.selectUser(mwPasswdPlanDTO.getPasswdId());
            for (MwSubUserDTO user : userDTOs) {
                UserDTO userDTO = mwUserDao.selectByLoginName(user.getLoginName());
                userDTO.setActivePasswdPlan(1);
                mwUserServiceImpl.insertPasswdInform(userDTO);
            }

            uParam.setModifier(iLoginCacheInfo.getLoginName());
            mwPasswdDAO.updatePasswdState(uParam);
//            List<MwUserDTO> mwUserDTOS = mwPasswdDAO.selectUserByPasswdId(uParam.getPasswdId());
        } catch (Exception e) {
            log.error("fail to updatePasswdPlanState with UpdatePasswdPlanStateParam=【{}】, cause:【{}】",
                    uParam, e.getMessage());
            if (e instanceof CheckUpdatePasswdPlanException) {
                try {
                    throw e;
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                    log.error("修改密码策略状态失败",ex);
                }
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.PASSWDPLAN_100607, ErrorConstant.PASSWDPLAN_MSG_100607));
            }
        }
        return Reply.ok("更新状态成功");

    }

    /**
     * 更新密码策略
     */
    @Override
    public Reply updatePasswordPlan(AddUpdatePasswdPlanParam passwordPlan) {
        try {
            if (passwordPlan.getPasswdId() == DEFAULT_PASSWD_ID) {
                throw new CheckUpdatePasswdPlanException(ErrorConstant.PASSWDPLAN_100606, ErrorConstant.PASSWDPLAN_MSG_100606);
            }
            if (StringUtils.isEmpty(passwordPlan.getSalt())) {
                passwordPlan.setSalt("");
            } else {
                passwordPlan.setSalt(passwordPlan.getSalt().trim());
            }
            String loginName = iLoginCacheInfo.getLoginName();
            MWPasswdPlan passwdPlan = mwPasswdDAO.selectById(passwordPlan.getPasswdId());
            passwdPlan.setPasswdId(null);
            passwdPlan.setDeleteFlag(true);
            mwPasswdDAO.insert(passwdPlan);
            // 如果是永久有效就将时间置空
            if (passwordPlan.getPasswdExpireType()) {
                passwordPlan.setPasswdUpdateDate(null);
            }
            MWPasswdPlan mwPasswdPlan = CopyUtils.copy(MWPasswdPlan.class, passwordPlan);
            mwPasswdPlan.setModifier(loginName);
            mwPasswdDAO.update(mwPasswdPlan);
            List<Integer> passwdIds = new ArrayList<>();
            passwdIds.add(mwPasswdPlan.getPasswdId());
            mwPasswdplanOrgMapperDao.deletePasswdOrgMapper(passwdIds);
            addOrgPasswdMapper(passwordPlan.getDepartment(), mwPasswdPlan.getPasswdId());
            return Reply.ok("修改成功！");
        } catch (Exception e) {
            log.error("fail to updatePasswordPlan with AddUpdatePasswdPlanParam=【{}】, cause:【{}】",
                    passwordPlan, e.getMessage());
            if (e instanceof CheckUpdatePasswdPlanException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.PASSWDPLAN_100610, ErrorConstant.PASSWDPLAN_MSG_100610));
            }
        }
    }

    /**
     * 分页查找密码策略
     */
    @Override
    public Reply selectList(QueryPasswdPlanParam qParam) {
        try {
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            Map criteria = PropertyUtils.describe(qParam);
            List<MwPasswdPlanDTO> passwdList = mwPasswdDAO.selectList(criteria);
            for (MwPasswdPlanDTO plan : passwdList) {
                List<MwSubUserDTO> userList = mwPasswdDAO.selectUser(plan.getPasswdId());
                for (MwSubUserDTO user : userList) {
                    List<OrgDTO> orgList = mwPasswdDAO.selectOrg(user.getUserId());
                    user.setDepartment(orgList);
                }
                plan.setUserDTOs(userList);
                List<OrgDTO> orgList = mwPasswdDAO.selectOrg(plan.getPasswdId());
                plan.setDepartment(orgList);
            }
            PageInfo pageInfo = new PageInfo<>(passwdList);
            pageInfo.setList(passwdList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectPasswdPlanList with QueryPasswdPlanParam=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.PASSWDPLAN_100609, ErrorConstant.PASSWDPLAN_MSG_100609));
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    /**
     * 通过ID查询密码策略信息
     */
    @Override
    public Reply selectPopupById(Integer passwdId) {
        try {
            MwPasswdPlanDTO passwdPlan = mwPasswdDAO.selectPopupById(passwdId);
            List<OrgDTO> orgList = mwPasswdDAO.selectOrg(passwdId);
            PasswdPlanDTO mwPasswdPlan = CopyUtils.copy(PasswdPlanDTO.class, passwdPlan);
            // department重新赋值使页面可以显示
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(orgList)) {
                orgList.forEach(
                        department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(
                                    node -> {
                                        if (!"".equals(node)) {
                                            orgIds.add(Integer.valueOf(node));
                                        }
                                    }
                            );
                            orgNodes.add(orgIds);
                        }
                );
                mwPasswdPlan.setDepartment(orgNodes);
            }
            if (passwdPlan.getPasswdUpdateDate() != null){
                mwPasswdPlan.setPasswdExpireType(false);
            }else {
                mwPasswdPlan.setPasswdExpireType(true);
            }
            return Reply.ok(mwPasswdPlan);
        } catch (Exception e) {
            log.error("fail to selectPopupById with passwdId=【{}】, cause:【{}】",
                    passwdId, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.PASSWDPLAN_100608, ErrorConstant.PASSWDPLAN_MSG_100608));
        }
    }

    /**
     * 密码策略下拉框查询
     */
    @Override
    public Reply selectDropdownList() {
        try {
            List<MWPasswdPlan> passwdPlan = mwPasswdDAO.selectDropdownList();
            return Reply.ok(passwdPlan);
        } catch (Exception e) {
            log.error("fail to selectOrgDropdownList cause:【{}】", e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.PASSWDPLAN_100601, ErrorConstant.PASSWDPLAN_MSG_100601));
        }
    }


    @Override
    public Reply selectById(Integer id) {
        try {
            MWPasswdPlan passwdPlan = mwPasswdDAO.selectById(id);
            MWPasswordPlanDTO passwdPlanDTO = CopyUtils.copy(MWPasswordPlanDTO.class, passwdPlan);
            log.info("ACCESS_LOG[]user[]用户管理[]根据ID取密码策略信息[]{}", id);
            return Reply.ok(passwdPlanDTO);
        } catch (Exception e) {
            log.error("fail to selectByUserId with userId={}, cause:{}", id, e.getMessage());
            return Reply.fail(ErrorConstant.USER_MSG_100115);
        }
    }

    @Override
    public Reply selectActiveByUserId(Integer id) {
        try {
            MWPasswdPlan passwdPlan = mwPasswdDAO.selectActiveByUserId(id);
            MWPasswordPlanDTO passwdPlanDTO = CopyUtils.copy(MWPasswordPlanDTO.class, passwdPlan);
            log.info("ACCESS_LOG[]user[]用户管理[]根据ID取密码策略信息[]{}", id);
            return Reply.ok(passwdPlanDTO);
        } catch (Exception e) {
            log.error("fail to selectByUserId with userId={}, cause:{}", id, e.getMessage());
            return Reply.fail(ErrorConstant.USER_100115, ErrorConstant.USER_MSG_100115);
        }
    }

    @Override
    public Reply selectActiveByLoginName(String loginName) {
        try {
            MWPasswdPlan passwdPlan = mwPasswdDAO.selectActiveByLoginName(loginName);
            MWPasswordPlanDTO passwdPlanDTO = CopyUtils.copy(MWPasswordPlanDTO.class, passwdPlan);
            log.info("ACCESS_LOG[]user[]用户管理[]根据用户名取密码策略信息[]{}", loginName);
            return Reply.ok(passwdPlanDTO);
        } catch (Exception e) {
            log.error("fail to selectActiveByLoginName with loginName={}, cause:{}", loginName, e.getMessage());
            return Reply.fail(ErrorConstant.USER_100115, ErrorConstant.USER_MSG_100115);
        }
    }

    @Override
    public int queryPasswdPlanCount(Integer id) {
        int count = mwPasswdDAO.queryPasswdPlanCount(id);
        log.info("ACCESS_LOG[]user[]用户管理[]根据ID取密码策略信息[]{}", id);
        return count;
    }

}
