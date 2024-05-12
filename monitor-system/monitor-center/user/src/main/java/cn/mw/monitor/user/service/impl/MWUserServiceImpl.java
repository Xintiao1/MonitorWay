package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.api.param.user.*;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.model.param.InstanceShiftPowerParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.redis.RedisExpireEvent;
import cn.mw.monitor.service.redis.RedisListener;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.exception.UserLockedException;
import cn.mw.monitor.service.user.model.*;
import cn.mw.monitor.shiro.*;
import cn.mw.monitor.state.*;
import cn.mw.monitor.user.common.AuthUtil;
import cn.mw.monitor.user.common.KickOutDTO;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.dto.GroupDTO;
import cn.mw.monitor.util.ExcelUtils;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mw.monitor.util.RedisUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.event.*;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.api.exception.CheckUpdateUserStateException;
import cn.mw.monitor.api.exception.PasswdCheckException;
import cn.mw.monitor.api.exception.UserLoginException;
import cn.mw.monitor.api.exception.UserLoginInfoException;
import cn.mw.monitor.api.exception.*;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.exception.ChangePasswdException;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.IUserControllerLogin;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.api.param.org.BindUserOrgParam;
import cn.mw.monitor.service.user.param.LoginParam;
import cn.mw.monitor.customPage.dao.MwPagefieldTableDao;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.user.dao.*;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.dto.MwUserDTO;
import cn.mw.monitor.user.model.*;
import cn.mw.monitor.user.service.*;
import cn.mw.monitor.user.service.view.UserView;
import cn.mw.monitor.user.state.*;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.PhoneUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.subject.Subject;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.TransformException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dev on 2020/2/11.
 */
@Service
@Slf4j
@Transactional
public class MWUserServiceImpl extends ListenerService implements MWUserService, MWUserCommonService, RedisListener {

    private static final Logger logger = LoggerFactory.getLogger("business-" + MWUserServiceImpl.class.getName());

    @Value("${userDebug}")
    boolean userDebug;

    @Value("${enableCaptcha}")
    boolean enableCaptcha;

    @Value("${user.login.type}")
    private String loginType;

    @Value("${model.assets.enable}")
    private Boolean modelAssetEnable;

    @Value("${user.redirect.url}")
    private String redirectUrl;

    /**
     * 最大搜索数量（ORACLE环境，in查询超过1000个会报错）
     */
    private final static int SELECT_MAX = 300;

    /**
     * 系统角色ID
     */
    private final static int SYSTEM_ROLE_ID = 0;

    /**
     * 查询数据（全部查询）
     */
    private final static int ALL = 0;
    /**
     * 查询数据（根据机构查询）
     */
    private final static int ORG = 1;
    /**
     * 查询数据（根据用户组查询）
     */
    private final static int GROUP = 2;

    @Resource
    MwPagefieldTableDao mwPagefieldTableDao;

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    @Resource
    private MWUserDao mwuserDao;

    @Resource
    private MWPasswdDAO mwPasswdDAO;

    @Autowired
    private PasswordManage passwordManage;

    @Autowired
    private MWPasswordPlanService mwpasswordPlanService;

    @Autowired
    private List<IMWUserUnlock> userUnlockList;

    @Autowired
    UserSync userSync;

    @Autowired
    private MWGroupService mwGroupService;

    @Resource
    private MWADUserDao mwadUserDao;

    @Resource
    private MwUserGroupMapperDao mwUserGroupMapperDao;

    @Resource
    private MwUserOrgMapperDao mwUserOrgMapperDao;

    @Resource
    private MwGroupTableDao mwGroupTableDao;

    @Resource
    private MWOrgService mwOrgService;

    @Resource
    private MWOrgDao mworgDao;

    @Resource
    private MwUserRoleMapperDao mwUserRoleMapperDao;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Resource
    private MwCommonDao commonDao;

    @Resource
    private MWPasswCompTypeDao mwpasswCompTypeDao;

    @Resource
    private MwRoleDao mwRoleDao;

    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Lazy
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Value("${alert.level}")
    private String alertLevel;

    @Autowired
    private MwUserSessionService userSessionService;
    /**
     * 根据用户ID取用信息
     *
     * @param userId 用户ID
     * @return
     */
    @Override
    public Reply selectByUserId(Integer userId) {
        MWUser user = mwuserDao.selectByUserId(userId);
        return Reply.ok(user);
    }



    @Override
    public String getRolePermByUserId(Integer userId) {
        return mwuserDao.getRolePermByUserId(userId);
    }

    @Override
    public String getLoginNameByUserId(Integer userId) {
        return mwuserDao.getLoginNameByUserId(userId);
    }

    @Override
    public Integer getAdmin() {
        return mwuserDao.getAdmin().get(0);
    }

    @Override
    public List<MWUser> selectByStringGroup(String type, List<Integer> ids) {
        return mwuserDao.selectByStringGroup(type,ids);
    }

    @Override
    public List<String> getLoginNameByUserIds(List<Integer> userIds) {
        return mwuserDao.getLoginNameByUserIds(userIds);
    }

    @Override
    public List<Integer> selectAllUserId() {
        return mwuserDao.selectAllUserId();
    }


//    /**
//     * 根据角色ID取用户信息
//     *
//     * @param roleId 角色ID
//     * @return
//     */
//    @Override
    //     public Reply selectByRoleId(Integer roleId) {
//        try {
//
//            List<MWUser> users = mwuserDao.selectByRoleId(roleId);
//
//            List<UserDTO> userDTOs = CopyUtils.copyList(UserDTO.class, users);
//
//            logger.info("ACCESS_LOG[]user[]用户管理[]根据角色ID取用户信息[]{}[]", roleId);
//
//            return Reply.ok(userDTOs);
//
//        } catch (Exception e) {
//            log.error("fail to selectByRoleId with roleId={}, cause:{}", roleId, e.getMessage());
//            return Reply.fail(ErrorConstant.USER_100106, ErrorConstant.USER_MSG_100106);
//        }
//      }

    @Override
    public Reply selectById(Integer userId) {
        try {
            MwUserDTO user = mwuserDao.selectById(userId);
            //根据userId从密码策略临时表中查是否有数据 有则用临时表中的策略id替换user表中的策略id
            MWPasswdInform mwPasswdInform = mwuserDao.selectInformByUserId(userId);
            if (mwPasswdInform != null) {
                user.setActivePasswdPlan(mwPasswdInform.getInoperactivePasswdPlan());
            }

            UserDTO userById = CopyUtils.copy(UserDTO.class, user);
            // usergroup重新赋值使页面可以显示
            List<Integer> groupIds = new ArrayList<>();
            user.getUserGroup().forEach(
                    groupDTO -> groupIds.add(groupDTO.getGroupId())
            );
            userById.setUserGroup(groupIds);
            // department重新赋值使页面可以显示
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (user.getDepartment() != null) {
                user.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
                userById.setDepartment(orgNodes);
            }
            userById.setRoleId(user.getRole().getRoleId());
            userById.setRoleName(user.getRole().getRoleName());
            if (!StringUtils.isEmpty(userById.getMorePhones())) {
                userById.setPhoneNumber(userById.getPhoneNumber() + "," + userById.getMorePhones());
            }

            List<MwUserControl> usercontrolRule = mwuserDao.selectUserControlByUserId(userId);
            if (usercontrolRule != null) {
                MwUserControlSetting mcs = new MwUserControlSetting();
                MWIp mwIp = new MWIp(false, "");
                MWTime mwTime = new MWTime(false, "");
                MWMac mwMac = new MWMac(false, "");

                for (MwUserControl control : usercontrolRule) {
                    if (control.getControlTypeId() != null && control.getControlTypeId().equals("1")) {
                        mwIp.setChecked(true);
                        mwIp.setContent(control.getRule());
                    }
                    if (control.getControlTypeId() != null && control.getControlTypeId().equals("2")) {
                        mwMac.setChecked(true);
                        mwMac.setContent(control.getRule());
                    }
                    if (control.getControlTypeId() != null && control.getControlTypeId().equals("3")) {
                        mwTime.setChecked(true);
                        mwTime.setContent(control.getRule());
                    }
                }
                mcs.setMwIp(mwIp);
                mcs.setMwMac(mwMac);
                mcs.setMwTime(mwTime);
                userById.setControlSettings(mcs);
            }
            logger.info("ACCESS_LOG[]user[]用户管理[]根据用户ID取用户信息[]{}", userId);
            return Reply.ok(userById);
        } catch (Exception e) {
            log.error("fail to selectByUserId with userId={}", userId, e);
            return Reply.fail(ErrorConstant.USER_100107, ErrorConstant.USER_MSG_100107);
        }
    }

    /**
     * 根据登录名取用信息
     *
     * @param loginName 登录名
     * @return
     */
    @Override
    public Reply selectByLoginName(String loginName) {
        try {
            UserDTO user = mwuserDao.selectByLoginName(loginName);

            logger.info("ACCESS_LOG[]user[]用户管理[]根据用户名取用户信息[]{}", loginName);
            return Reply.ok(user);
        } catch (Exception e) {
            log.error("fail to selectByUserName with loginName={}", loginName, e);
            throw new UserBrowseException();
        }
    }

    @Override
    public Reply selectByOpenid(String openId) {
        try {
            UserDTO user = mwuserDao.selectByOpenid(openId);

            logger.info("ACCESS_LOG[]user[]用户管理[]根据用户微信openId取用户信息[]{}", openId);
            return Reply.ok(user);
        } catch (Exception e) {
            log.error("fail to selectByOpenid with openId={}", openId, e);
            return Reply.fail(ErrorConstant.USER_100105, "根据用户微信openId取用户信息失败");
        }
    }

    @Override
    public Reply selectListByPerm(List<Integer> orgIds) {

        String loginName = loginCacheInfo.getLoginName();
        MwRoleDTO roleDTO = loginCacheInfo.getRoleInfo();

        LoginContext loginContext = loginCacheInfo.getCacheInfo(loginName);
        LoginInfo loginInfo = loginContext.getLoginInfo();
        UserDTO userDTO = loginInfo.getUser();
        List<UserView> filterUsers = new ArrayList<UserView>();

        List<MWOrgDTO> orgs = loginInfo.getOrgs();
        List<String> nodePathList = new ArrayList<String>();

        List<Integer> groupIds = loginInfo.getUser().getUserGroup();

        try {
            DataPermission dataPermission = DataPermission.valueOf(roleDTO.getDataPerm());

            switch (dataPermission) {
                case PRIVATE:
                    filterUsers.add(UserView.builder()
                            .value(userDTO.getUserId())
                            .label(userDTO.getLoginName()).build());
                    break;
                case PUBLIC:
                    for (MWOrgDTO item : orgs) {
                        nodePathList.add(item.getNodes());
                    }
                    Map map = new HashMap();
                    map.put("nodes", nodePathList);
                    map.put("groupIds", groupIds);
                    map.put("orgIds", orgIds);
                    //暂时取消权限验证
                    // List<MWUser> users = mwuserDao.selectListByPerm(map);
                    List<MwUserDTO> users = mwuserDao.selectList(map);
                    users.forEach(value -> {
                        filterUsers.add(UserView.builder()
                                .value(value.getUserId())
                                .label(value.getLoginName()).build());
                        ;
                    });
                    break;
                default:
            }

            logger.info("ACCESS_LOG[]user[]用户管理[]根据机构路径获取用户信息[]{}", dataPermission);
            return Reply.ok(filterUsers);
        } catch (Exception e) {
            log.error("fail to selectListByPerm with nodepath={}", nodePathList, e);
            return Reply.fail(ErrorConstant.USER_100131, ErrorConstant.USER_MSG_100131);
        }
    }

    @Override
    public Reply pageUser(QueryUserParam qParam) {
        try {
            Map criteria = PropertyUtils.describe(qParam);

            log.info("用户列表查询 --start");
            List<MwUserDTO> users = mwuserDao.selectList(criteria);
            log.info("用户列表查询 --end");
            List<MwUserDTO> userList = new ArrayList<>();

            if (!CollectionUtils.isEmpty(users)) {
                for (MwUserDTO user : users) {
                    //去除AD用户名显示非中文
                    if (UserType.LDAP.getType().equals(user.getUserType())) {
                        user.setUserName(user.getUserName().replaceAll("[^\u4E00-\u9FA5]", "").trim());
                    }
                }
                Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                users = users.stream()
                        .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getUserName()),
                                pinyin4jUtil.getStringPinYin(o2.getUserName())))
                        .collect(Collectors.toList());
                if (qParam.getPageNumber() * qParam.getPageSize() >= users.size()) {
                    if (qParam.getPageSize() >= users.size()) {
                        userList.addAll(users);
                    } else {
                        userList = users.subList((qParam.getPageNumber() - 1) * qParam.getPageSize(), users.size());
                    }
                } else {
                    userList = users.subList((qParam.getPageNumber() - 1) * qParam.getPageSize(), qParam.getPageNumber() * qParam.getPageSize());
                }
            }

            for (MwUserDTO mwd : userList) {
                //根据userId从密码策略临时表中查是否有数据 有则用临时表中的策略id替换user表中的策略id
                MWPasswdInform mwPasswdInform = mwuserDao.selectInformByUserId(mwd.getUserId());
                if (mwPasswdInform != null) {
                    mwd.setActivePasswdPlan(mwPasswdInform.getInoperactivePasswdPlan());
                    String passwdName = mwPasswdDAO.selectPasswdNameById(mwPasswdInform.getInoperactivePasswdPlan());
                    mwd.setActivePasswdPlanName(passwdName);
                }
                List<MwUserControl> usercontrolRule = mwuserDao.selectUserControlByUserId(mwd.getUserId());
                if (usercontrolRule != null) {
                    MwUserControlSetting mcs = new MwUserControlSetting();
                    MWIp mwIp = new MWIp(false, "");
                    MWMac mwMac = new MWMac(false, "");
                    MWTime mwTime = new MWTime(false, "");
                    for (MwUserControl control : usercontrolRule) {
                        if (control.getControlTypeId() != null && control.getControlTypeId().equals("1")) {
                            mwIp.setChecked(true);
                            mwIp.setContent(control.getRule());
                        }
                        if (control.getControlTypeId() != null && control.getControlTypeId().equals("2")) {
                            mwMac.setChecked(true);
                            mwMac.setContent(control.getRule());
                        }

                        if (control.getControlTypeId() != null && control.getControlTypeId().equals("3")) {
                            mwTime.setChecked(true);
                            mwTime.setContent(control.getRule());
                        }

                        mcs.setMwIp(mwIp);
                        mcs.setMwMac(mwMac);
                        mcs.setMwTime(mwTime);
                        mwd.setControlSettings(mcs);
                    }
                }

                //添加用户组数据
                List<GroupDTO> groupList = mwuserDao.selectGroup(mwd.getUserId());
                mwd.setUserGroup(groupList);
                StringBuffer stringBuffer = new StringBuffer();
                for (GroupDTO group : groupList) {
                    stringBuffer.append(group.getGroupName()).append(",");
                }
                if (stringBuffer.length() > 0) {
                    mwd.setUserGroupString(stringBuffer.toString().substring(0, stringBuffer.length() - 1));
                }

                stringBuffer = new StringBuffer();
                for (OrgDTO org : mwd.getDepartment()) {
                    stringBuffer.append(org.getOrgName()).append(",");
                }
                if (stringBuffer.length() > 0) {
                    mwd.setDepartmentString(stringBuffer.toString().substring(0, stringBuffer.length() - 1));
                }
            }

            PageInfo pageInfo = new PageInfo<>(users);
            pageInfo.setList(userList);
            pageInfo.setTotal(users.size());

            logger.info("ACCESS_LOG[]user[]用户管理[]分页查询用户信息[]{}[]", qParam);

            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to pageUser with param={}", qParam, e);
            return Reply.fail(ErrorConstant.USER_100106, ErrorConstant.USER_MSG_100106);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    /**
     * 根据机构ID查询用户
     *
     * @param orgId
     * @return
     */
    @Override
    @Deprecated
    public Reply getUserByOrgId(Integer orgId) {
        try {
            List<MWUser> users = mwuserDao.getUserByOrgId(orgId);
            List<UserDTO> list = CopyUtils.copyList(UserDTO.class, users);
            logger.info("ACCESS_LOG[]user[]用户管理[]查询用户信息[]{}[]", orgId);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to pageUser with param={}", orgId, e);
            return Reply.fail(ErrorConstant.USER_100106, ErrorConstant.USER_MSG_100106);
        }
    }

    @Override
    public Reply addUser(UserDTO userdto) throws Throwable {
        List<Reply> faillist = new ArrayList<Reply>();

        // 如果validityType 值为 1  就是永久有效，设置时间为2099-12-31
        Calendar calendar = new GregorianCalendar(2099, 12, 31);
        if (userdto.getValidityType() != null && userdto.getValidityType().equals("1")) {
            userdto.setUserExpiryDate(calendar.getTime());
        }

        String loginName = loginCacheInfoInfo.getLoginName();

        try {
//            int userId = userdto.getUserId();
//            int roleId = userdto.getRoleId();
//            mwUserRoleMapperDao.insertUserRoleMapper(MwUserRoleMap
//                    .builder()
//                    .userId(userId)
//                    .roleId(roleId)
//                    .build()
//            );
//            MWPasswdHis mwpasswdHis = new MWPasswdHis(userdto.getUserId(), userdto.getPassword());
//            mwpasswdHisDao.insert(mwpasswdHis);
            faillist = publishCheckEvent(new AddUserEvent(userdto.getPassword(), userdto));
            List<String> errerMsg = new ArrayList<>();
            if (faillist.size() > 0) {
                faillist.forEach(fail -> errerMsg.add(fail.getMsg()));
                return Reply.warn(StringUtil.join(errerMsg, ","));
            }
            List<UserDTO> userCheck = mwuserDao.getUserByName(userdto.getLoginName());
            if (userCheck.size() > 0) {
                return Reply.warn(userdto.getLoginName() + "已存在，不可重复添加！");
            }

            Reply reply = mwpasswordPlanService.selectById(userdto.getActivePasswdPlan());

            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                faillist.add(reply);
                throw new ServiceException(faillist);
            }
            MWPasswordPlanDTO defaultPasswordPlan = (MWPasswordPlanDTO) reply.getData();
            // 设置密码过期时间
            if (null != defaultPasswordPlan.getPasswdUpdateDate()) {
                calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, defaultPasswordPlan.getPasswdUpdateDate());
            }
            userdto.setPasswdExpiryDate(calendar.getTime());
            Credential credential = new Credential(userdto.getLoginName(), userdto.getPassword(),
                    defaultPasswordPlan.getSalt(), defaultPasswordPlan.getHashTypeId());
            String password = passwordManage.encryptPassword(credential);

            userdto.setPassword(password);
            userdto.setLoginState(LoginState.OFFLINE.getName());

            //产生密码生成事件
            faillist = publishCheckEvent(new GenPasswdEvent(password, userdto));

            //支持多手机号功能
            if (!StringUtils.isEmpty(userdto.getPhoneNumber())) {
                int index = userdto.getPhoneNumber().indexOf(",");
                if (index > -1) {
                    String phoneNumber = userdto.getPhoneNumber().substring(0, index);
                    String morePhone = userdto.getPhoneNumber().substring(index + 1);
                    userdto.setPhoneNumber(phoneNumber);
                    userdto.setMorePhones(morePhone);
                }
            }
//            userdto.setActivePasswdPlan(defaultPasswordPlan.getId());
            MWUser mwUser = CopyUtils.copy(MWUser.class, userdto);
            mwUser.setLoginState(LoginState.DEFAULT.getName());
            mwUser.setUserState(UserActiveState.DEFAULT.getName());
            mwUser.setUserExpireState(UserExpireState.DEFAULT.getName());
            mwUser.setPasswdState(PasswdState.DEFAULT.getName());
            mwUser.setDefaultPasswdPlan(mwUser.getActivePasswdPlan());
            mwUser.setCreator(loginName);
            mwUser.setModifier(loginName);
            mwUser.setUserType(UserType.MW.getType());
            mwuserDao.insert(mwUser);

            mwUser.setInoperactivePasswdPlan(mwUser.getActivePasswdPlan());
            mwUser.setModifyType(MWPasswdInform.userAddModify);
            mwuserDao.insertInform(mwUser);

            UserDTO userDTO = mwuserDao.selectByLoginName(mwUser.getLoginName());
            if (userdto.getUserControlEnable()) {
                MwUserControlSetting mwUserControlSetting = userdto.getControlSettings();
                Integer userId = userDTO.getUserId();
                String cond = userdto.getConditionsValue().equals("1") ? "AllSatisfy" : "AllNotSatisfy";
                String operation = userdto.getActionValue().equals("1") ? "Permit" : "NotPermit";

                mwuserDao.insertUserControlAction(userId, cond, operation);

                if (mwUserControlSetting.getMwIp().getChecked()) {
                    Integer controlTypeId = 1;
                    String rule = mwUserControlSetting.getMwIp().getContent();
                    mwuserDao.insertUserControl(userId, controlTypeId, rule);
                }
                if (mwUserControlSetting.getMwMac().getChecked()) {
                    Integer controlTypeId = 2;
                    String rule = mwUserControlSetting.getMwMac().getContent();
                    mwuserDao.insertUserControl(userId, controlTypeId, rule);
                }
                if (mwUserControlSetting.getMwTime().getChecked()) {
                    Integer controlTypeId = 3;
                    String rule = mwUserControlSetting.getMwTime().getContent();
                    mwuserDao.insertUserControl(userId, controlTypeId, rule);
                }
            }


//            //更新用户使用的密码策略
            logger.info("ACCESS_LOG[]user[]用户管理[]添加用户[]{}", userdto.getLoginName());

            UserDTO newuserDTO = CopyUtils.copy(UserDTO.class, mwUser);
            newuserDTO.setRoleId(userdto.getRoleId());
            faillist = publishPostEvent(new AddUserEvent(userdto.getPassword(), newuserDTO));
            if (faillist.size() > 0) {
                throw new ServiceException(faillist);
            }

            List<Integer> userIds = new ArrayList<>();
            userIds.add(newuserDTO.getUserId());
            //绑定用户和用户组
            if (userdto.getUserGroup() != null && userdto.getUserGroup().size() > 0) {
                mwGroupService.bindUserGroup(BindUserGroupParam.builder().flag(1).groupIds(userdto.getUserGroup()).userIds(userIds).build());
            }
            //绑定用户和机构
            if (userdto.getDepartment() != null) {
                List<Integer> orgIdList = new ArrayList<>();
                userdto.getDepartment().forEach(orgIds -> {
                            orgIdList.add(orgIds.get(orgIds.size() - 1));
                        }
                );
                mwOrgService.bindUserOrg(BindUserOrgParam.builder().flag(1).orgIds(orgIdList).userIds(userIds).build());
            }
            //生成初始化的表头和查询头信息
            List<Integer> userId = new ArrayList<>();
            userId.add(newuserDTO.getUserId());
            faillist = publishPostEvent(new CustomColLoadEvent(userId));
            if (faillist.size() > 0) {
                throw new ServiceException(faillist);
            }

            return Reply.ok("用户新增成功！");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to add user with userName={}", userdto.getLoginName(), e);
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
    }

    @Override
    public List<MwUserDTO> selectResponser(String typeId, String type) {
        Map criteria = new HashMap();
        criteria.put("typeId", typeId);
        criteria.put("type", type);
        List<MwUserDTO> mwUsers = mwuserDao.selectResponser(criteria);
        return mwUsers;
    }

    @Override
    protected void addCheckLists(List listners) {
        super.addCheckLists(listners);
    }


    /**
     * 更新用户数据信息
     *
     * @param userdto 用户数据
     * @return
     * @throws Throwable
     */
    @Override
    public Reply updateUser(UserDTO userdto) throws Throwable {
        List<Reply> faillist = new ArrayList<>();
        String loginName = loginCacheInfoInfo.getLoginName();
        try {
            if (StringUtils.isEmpty(userdto.getUserId().toString())) {
                String msg = Reply.replaceMsg(ErrorConstant.USER_MSG_100112, new String[]{"用户id"});
                faillist.add(Reply.fail(ErrorConstant.USER_100112, msg));
                throw new ServiceException(faillist);
            }
            //查询用户信息
            MWUser user = mwuserDao.selectByUserId(userdto.getUserId());
            if (user == null) {
                return Reply.fail(ErrorConstant.USER_100107, ErrorConstant.USER_MSG_100107);
            }
            //禅道NO.1816
            if (Constants.SYSTEM_ADMIN.equals(userdto.getUserId())) {
                if (UserActiveState.DISACTIVE.getName().equals(userdto.getUserState())) {
                    return Reply.fail(ErrorConstant.USER_100212, ErrorConstant.USER_MSG_100212);
                }
                if (SYSTEM_ROLE_ID !=userdto.getRoleId()) {
                    return Reply.fail(ErrorConstant.USER_100213, ErrorConstant.USER_MSG_100213);
                }
                boolean hasSystemDept = false;
                org:
                for (List<Integer> list : userdto.getDepartment()) {
                    if (list.contains(Constants.SYSTEM_ORG)) {
                        hasSystemDept = true;
                        break org;
                    }
                }
                if (!hasSystemDept) {
                    return Reply.fail(ErrorConstant.USER_100214, ErrorConstant.USER_MSG_100214);
                }
            }
            boolean isADUser = UserType.LDAP.getType().equals(user.getUserType());
            //原先用户数据
            UserDTO olduserDTO = CopyUtils.copy(UserDTO.class, user);
            //更新用户密码策略
            MWPasswordPlanDTO mwpasswordPlanDTO = updatePasswordPlan(userdto, olduserDTO);

            //支持多手机号功能
            if (!StringUtils.isEmpty(userdto.getPhoneNumber())) {
                int index = userdto.getPhoneNumber().indexOf(",");
                if (index > -1) {
                    String phoneNumber = userdto.getPhoneNumber().substring(0, index);
                    String morePhone = userdto.getPhoneNumber().substring(index + 1);
                    userdto.setPhoneNumber(phoneNumber);
                    userdto.setMorePhones(morePhone);
                } else {
                    userdto.setMorePhones("");
                }
            } else {
                userdto.setPhoneNumber("");
                userdto.setMorePhones("");
            }

            /*修改用户之前 判断用户关联机构的状态 如果禁用 则不能修改用户状态为启用 */
            MWUser mwUser = CopyUtils.copy(MWUser.class, userdto);
            //AD用户只允许修改个人基础数据
            if (isADUser) {
                //更新手机号
                user.setPhoneNumber(userdto.getPhoneNumber());
                user.setMorePhones(userdto.getMorePhones());
                //更新微信号
                user.setWechatId(userdto.getWechatId());
                //更新钉钉号
                user.setDdId(userdto.getDdId());
                //更新邮箱数据
                user.setEmail(userdto.getEmail());
                user.setOa(userdto.getOa());
                //修改用户有效期
                if (mwUser.getUserExpiryDate() != null && mwUser.getUserExpiryDate().after(new Date())) {
                    user.setUserExpireState("NORMAL");
                }
                //修改更新人
                user.setModifier(loginName);
                // 如果validityType 值为 1  就是永久有效，设置时间为2099-12-31
                Calendar calendar = new GregorianCalendar(2099, 12, 31);
                if (userdto.getValidityType() != null && userdto.getValidityType().equals("1")) {
                    user.setUserExpiryDate(calendar.getTime());
                    user.setValidityType("1");
                } else {
                    user.setUserExpiryDate(userdto.getUserExpiryDate());
                    user.setValidityType("2");
                }
                user.setModifier(loginName);
                //设置用户状态
                user.setUserState(userdto.getUserState());

                mwuserDao.update(user);
                //更新用户的组织和部门数据
                updateUserOrgAndDept(userdto);
            } else {
                mwUser.setModifier(loginName);
                //修改用户有效期
                if (mwUser.getUserExpiryDate() != null && mwUser.getUserExpiryDate().after(new Date())) {
                    mwUser.setUserExpireState("NORMAL");
                }
                //检查用户组织的有效性
                checkOrgValid(mwUser);
                if (userdto.getActivePasswdPlan() != olduserDTO.getActivePasswdPlan()) {
                    mwUser.setActivePasswdPlan(olduserDTO.getActivePasswdPlan());
                }
                // 如果validityType 值为 1  就是永久有效，设置时间为2099-12-31
                Calendar calendar = new GregorianCalendar(2099, 12, 31);
                if (userdto.getValidityType() != null && userdto.getValidityType().equals("1")) {
                    mwUser.setUserExpiryDate(calendar.getTime());
                }
                mwuserDao.update(mwUser);
                //更新用户登录控制数据
                updateUserControl(userdto);
                //更新用户的组织和部门数据
                updateUserOrgAndDept(userdto);
            }
            logger.info("ACCESS_LOG[]user[]用户管理[]更新用户[]{}", userdto.getLoginName());
            //下发更新消息到各类监听器
            PostUpdUserEvent postPasswdEvent = new PostUpdUserEvent(olduserDTO, userdto, mwpasswordPlanDTO);
            publishPostEvent(postPasswdEvent);
        } catch (CheckUpdateUserStateException e) {
            throw e;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to update user with userName={}", userdto.getLoginName(), e);
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
        return Reply.ok("更新成功！");
    }


    /**
     * 更新用户登录控制信息
     *
     * @param userdto 用户信息
     */
    private void updateUserControl(UserDTO userdto) {
        if (userdto.getUserControlEnable() != null
                && userdto.getUserControlEnable() && userdto.getControlSettings() != null) {
            MwUserControlSetting mwUserControlSetting = userdto.getControlSettings();
            Integer userId = userdto.getUserId();

            String cond = "1".equals(userdto.getConditionsValue()) ? "AllSatisfy" : "AllNotSatisfy";
            String operation = "1".equals(userdto.getActionValue()) ? "Permit" : "NotPermit";

            int countAction = mwuserDao.selectUserControlAction(userId);
            //如果没有数据则新增 有则修改
            if (countAction == 0) {
                mwuserDao.insertUserControlAction(userId, cond, operation);
            } else {
                //更新用户动作和条件
                mwuserDao.updateUserControlAction(userId, cond, operation);
            }

            Integer controlTypeIdByIp = 1;
            Integer controlTypeIdByMac = 2;
            Integer controlTypeIdByTime = 3;
            String ipRule = mwUserControlSetting.getMwIp().getContent();
            String macRule = mwUserControlSetting.getMwMac().getContent();
            String timeRule = mwUserControlSetting.getMwTime().getContent();

            int countIp = mwuserDao.selectCountUserControl(userId, controlTypeIdByIp);
            int countTime = mwuserDao.selectCountUserControl(userId, controlTypeIdByTime);
            int countMac = mwuserDao.selectCountUserControl(userId, controlTypeIdByMac);
            if (mwUserControlSetting.getMwIp().getChecked()) {
                if (countIp == 0) {
                    mwuserDao.insertUserControl(userId, controlTypeIdByIp, ipRule);
                } else if (countIp == 1) {
                    mwuserDao.updateUserControl(userId, controlTypeIdByIp, ipRule);
                }
            } else {
                if (countIp == 1) {
                    mwuserDao.delUserControl(userId, controlTypeIdByIp);
                }
            }
            if (mwUserControlSetting.getMwTime().getChecked()) {
                if (countTime == 0) {
                    mwuserDao.insertUserControl(userId, controlTypeIdByTime, timeRule);
                } else if (countTime == 1) {
                    mwuserDao.updateUserControl(userId, controlTypeIdByTime, timeRule);
                }
            } else {
                if (countTime == 1) {
                    mwuserDao.delUserControl(userId, controlTypeIdByTime);
                }
            }
            if (mwUserControlSetting.getMwMac().getChecked()) {
                if (countMac == 0) {
                    mwuserDao.insertUserControl(userId, controlTypeIdByMac, macRule);
                } else if (countMac == 1) {
                    mwuserDao.updateUserControl(userId, controlTypeIdByMac, macRule);
                }
            } else {
                if (countMac == 1) {
                    mwuserDao.delUserControl(userId, controlTypeIdByMac);
                }
            }

            List<MwUserControl> mwUserControls = mwuserDao.selectUserControlByUserId(userId);

            for (MwUserControl mwUserControl : mwUserControls) {
                if (!mwUserControlSetting.getMwIp().getChecked() && mwUserControl.getControlTypeId().equals(controlTypeIdByIp.toString())) {
                    mwuserDao.delUserControl(userId, controlTypeIdByIp);
                }
                if (!mwUserControlSetting.getMwMac().getChecked() && mwUserControl.getControlTypeId().equals(controlTypeIdByMac.toString())) {
                    mwuserDao.delUserControl(userId, controlTypeIdByMac);
                }
                if (!mwUserControlSetting.getMwTime().getChecked() && mwUserControl.getControlTypeId().equals(controlTypeIdByTime.toString())) {
                    mwuserDao.delUserControl(userId, controlTypeIdByTime);
                }
            }
        }
    }

    /**
     * 更新用户的组织和部门数据
     *
     * @param userdto 用户数据
     */
    private void updateUserOrgAndDept(UserDTO userdto) {
        List<Integer> userIds = new ArrayList<>();
        userIds.add(userdto.getUserId());
        //绑定用户和用户组
        if (userdto.getUserGroup() != null) {
            if (userdto.getUserGroup().size() > 0) {
                mwGroupService.bindUserGroup(BindUserGroupParam.builder().flag(2).groupIds(userdto.getUserGroup()).userIds(userIds).build());
            } else {
                //空列表为全部解绑
                mwUserGroupMapperDao.deleteBatchByUserId(userIds);
            }
        }

        //绑定用户和机构
        if (userdto.getDepartment() != null) {
            List<Integer> orgIdList = new ArrayList<>();
            if (userdto.getDepartment().size() > 0) {
                userdto.getDepartment().forEach(orgIds -> {
                            orgIdList.add(orgIds.get(orgIds.size() - 1));
                        }
                );
            } else {
                //空列表为全部解绑
                mwUserOrgMapperDao.deleteBatchByUserId(userIds);
            }
            mwOrgService.bindUserOrg(BindUserOrgParam.builder().flag(2).orgIds(orgIdList).userIds(userIds).build());
        }
    }

    /**
     * 检查用户绑定组织是否存在无效组织
     *
     * @param mwUser 用户数据
     */
    private void checkOrgValid(MWUser mwUser) {
        List<OrgDTO> orgDTOList = mwUserOrgMapperDao.selectOrgNameByUserId(mwUser.getUserId());
        boolean flag = false;
        for (OrgDTO orgDTO : orgDTOList) {
            MWOrg mwOrg = mworgDao.selectByOrgId(orgDTO.getOrgId());
            if (UserActiveState.DISACTIVE.getName().equals(mwOrg.getEnable()) &&
                    UserActiveState.ACTIVE.getName().equals(mwUser.getUserState())) {
                flag = true;
                break;
            }
        }
        if (flag) {
            throw new CheckUpdateUserStateException(
                    ErrorConstant.USER_100139, ErrorConstant.USER_MSG_100139);
        }
    }


    /**
     * 更新用户密码策略
     *
     * @param userdto    待更新的用户数据信息
     * @param oldUserDTO 数据库存储的用户数据信息
     */
    private MWPasswordPlanDTO updatePasswordPlan(UserDTO userdto, UserDTO oldUserDTO) throws Throwable {
        List<Reply> failList = new ArrayList<>();
        Reply reply;
        //查询用戶密码策略
        if (userdto.getActivePasswdPlan() != null) {
            reply = mwpasswordPlanService.selectById(userdto.getActivePasswdPlan());
        } else {
            //修改密码的情况
            reply = mwpasswordPlanService.selectById(oldUserDTO.getActivePasswdPlan());
        }

        if (reply.getRes() != PaasConstant.RES_SUCCESS) {
            failList.add(Reply.fail(ErrorConstant.USER_MSG_100115));
            throw new ServiceException(failList);
        }
        MWPasswordPlanDTO mwpasswordPlanDTO = (MWPasswordPlanDTO) reply.getData();

        log.info("原密码策略:" + oldUserDTO.getActivePasswdPlan() + ",    现密码策略:" + userdto.getActivePasswdPlan());
        log.info("是否修改了密码策略>>>>>>" + oldUserDTO.getActivePasswdPlan().equals(userdto.getActivePasswdPlan()));

        if (userdto.getActivePasswdPlan() != null &&
                (!oldUserDTO.getActivePasswdPlan().equals(userdto.getActivePasswdPlan()))) {
            //生成更新事件,进行密码校验等处理
            UpdUserEvent updUserEvent = new UpdUserEvent(userdto.getPassword()
                    , oldUserDTO, userdto, mwpasswordPlanDTO);
            failList = publishCheckEvent(updUserEvent);
            if (failList.size() > 0) {
                throw new ServiceException(failList);
            }
            //修改了密码策略 如果临时表没有当前用户数据 则将密码策略插入到密码策略临时表
            insertPasswdInform(userdto);
        }
        return mwpasswordPlanDTO;
    }

    @Override
    public Reply updateUserOpenId(UserDTO userdto) throws Throwable {
        try {
            mwuserDao.updateUserOpenId(userdto.getUserId(), userdto.getOpenId());
        } catch (Exception e) {
            log.error("fail to update user with UserId={}", userdto.getUserId(), e);
            return Reply.fail("数据库更新失败！");
        }
        return Reply.ok("更新成功！");

    }

    /*向密码策略临时表中添加*/
    public void insertPasswdInform(UserDTO userdto) {

        MWUser newMwUser = CopyUtils.copy(MWUser.class, userdto);
        newMwUser.setInoperactivePasswdPlan(userdto.getActivePasswdPlan());
        MWPasswdInform mwPasswdInform = mwuserDao.selectInformByUserId(newMwUser.getUserId());
        if (mwPasswdInform != null) {
            mwuserDao.updateInform(newMwUser.getUserId());
        }
        newMwUser.setInoperactivePasswdPlan(userdto.getActivePasswdPlan());
        newMwUser.setModifyType(MWPasswdInform.passwdPlanModify);
        mwuserDao.insertInform(newMwUser);

    }

    @Override
    public Reply unlock(UserDTO userdto) throws Throwable {
        List<Reply> faillist = new ArrayList<>();
        try {
            MWUser mwUser = CopyUtils.copy(MWUser.class, userdto);
            mwUser.setUserExpireState(UserExpireState.DEFAULT.getName());
            int ret = mwuserDao.updateState(mwUser);

            for (EventListner listener : userUnlockList) {
                UnlockEvent unlockEvent = new UnlockEvent(userdto.getLoginName());
                List<Reply> checkresult = listener.handleEvent(unlockEvent);
                if (null != checkresult && checkresult.size() > 0)
                    faillist.addAll(checkresult);
            }
        } catch (Exception e) {
            return Reply.fail("解锁失败", userdto);
        }

        if (null != faillist && faillist.size() > 0) {
            Reply.fail(ErrorConstant.USER_MSG_100128, faillist);
        }
        return Reply.ok("解锁成功");
    }

    //登录事件处理器
    @Autowired
    private List<IUserControllerLogin> loginProcessers;

    //登录上下文信息获取接口
    @Autowired
    private ILoginCacheInfo loginCacheInfoInfo;

    private List<Reply> publishUserLogin(Event event) throws Throwable {
        List<Reply> list = new ArrayList<Reply>();
        for (EventListner listener : loginProcessers) {
            List<Reply> checkresult = listener.handleEvent(event);
            if (null != checkresult && checkresult.size() > 0)
                list.addAll(checkresult);
        }
        return list;
    }

    public Reply ldapUserLogin(LoginParam loginParam, Boolean unPass) throws Throwable {
        List<Reply> faillist = null;
        loginParam.setLdapRight(true);
        //发布登录前事件
        try {
            faillist = loginBefore(loginParam);
        } catch (Exception e) {
            throw e;
        }

        Subject currentUser = SecurityUtils.getSubject();
        String loginName = loginParam.getLoginName();
        LoginDTO loginDTO = null;


        UserDTO lastLoginDTO = (UserDTO) currentUser.getPrincipal();
        //用户使用在同一台主机登录不同的用户时，需要登出之前的用户
        kickOut(loginParam);

        if (!currentUser.isAuthenticated()) {
            LoginContext loginContext = loginCacheInfoInfo.getThreadLocalInfo();
            try {

                CustomUserNamePasswdToken token = new CustomUserNamePasswdToken(
                        loginParam.getLoginName(), loginParam.getPassword(), loginParam.getLoginType());
                currentUser.login(token);

                Serializable gentoken = currentUser.getSession().getId();

                loginDTO = loginContext.getLoginDTO();
                loginDTO.setToken(gentoken.toString());
                loginContext.setLoginState(LoginState.ONLINE.getName());
            } catch (IncorrectCredentialsException e) {
                loginContext.setLoginState(LoginState.OFFLINE.getName());
                ADConfigDTO ad = mwadUserDao.select();
                loginName = loginName + "@" + ad.getDomainName();
                LoginFailEvent loginFailEvent = new LoginFailEvent(loginName);
                publishUserLogin(loginFailEvent);
                throw new IncorrectUserException();
            } catch (Exception e) {
                throw e;
            } finally {
                //登录后发布事件
                try {
                    log.info("即将进入updateUserState");
                    //同步用户状态
                    userSync.updateUserState(loginContext);
                    Event event = new LoginAfterEvent();
                    publishUserLogin(event);
                } catch (Exception e) {
                    log.error("同步用户状态异常={}",e);
                    log.info("同步用户状态异常={}",e);
                }
            }
        } else {
            // 当验证码重新输入正确的情况下，将已经生成的token传回前台
            LoginContext lc = loginCacheInfoInfo.getCacheInfo(loginName);
            loginDTO = lc.getLoginDTO();
        }
        return Reply.ok(loginDTO);
    }

    @Override
    public Reply userlogin(LoginParam loginParam) throws Throwable {
        if (LoginType.LDAP_LOGIN.getType().equals(loginParam.getLoginType())) {
            return ldapUserLogin(loginParam, false);
        }
        return userlogin(loginParam, false);
    }

    public List<Reply> loginBefore(LoginParam loginParam) throws Throwable {
        List<Reply> faillist = null;
        LoginBeforeEvent loginBeforeEvent = new LoginBeforeEvent(loginParam);
        try {
            faillist = publishUserLogin(loginBeforeEvent);
        } catch (Exception e) {
            //验证异常时,同步logincontext到数据库
            try {
                //同步用户状态
                LoginContext loginContext = loginCacheInfoInfo.getCacheInfo(loginParam.getLoginName());
                loginContext.setLoginState(LoginState.OFFLINE.getName());
                userSync.updateUserState(loginContext);
            } catch (Exception e1) {
                logger.error("userlogin", e1);
                log.info("同步用户状态异常={}",e);
            }
            log.error("user login before exception", e);
            throw e;
        }
        return faillist;
    }

    @Override
    public Reply userlogin(LoginParam loginParam, Boolean unPass) throws Throwable {

        List<Reply> faillist = null;
        loginParam.setLdapRight(false);
        //发布登录前事件
        try {
            faillist = loginBefore(loginParam);
        } catch (Exception e) {
            log.info("zy--  login before event :"+e);
            throw e;
        }

        Subject currentUser = SecurityUtils.getSubject();
        String loginName = loginParam.getLoginName();

        Event event = null;
        LoginDTO loginDTO = null;
        UserDTO lastLoginDTO = (UserDTO) currentUser.getPrincipal();
        //用户使用在同一台主机登录不同的用户时，需要登出之前的用户
        kickOut(loginParam);

        if (!currentUser.isAuthenticated()) {
            // token.setRememberMe(true);
            //获取状态更新信息
            LoginContext loginContext = loginCacheInfoInfo.getThreadLocalInfo();

            try {
                if (unPass) {
                    /*UsernameToken token = new UsernameToken(loginParam.getLoginName());
                    token.setUnpass(unPass);*/
                    CustomUserNamePasswdToken token = new CustomUserNamePasswdToken(
                            loginParam.getLoginName(), "", LoginType.Common.getType(), unPass);
                    currentUser.login(token);
                } else {
                    CustomUserNamePasswdToken token = new CustomUserNamePasswdToken(
                            loginParam.getLoginName(), loginParam.getPassword(), LoginType.Common.getType());
                    currentUser.login(token);
                }

                Serializable gentoken = currentUser.getSession().getId();

                loginDTO = loginContext.getLoginDTO();
                loginDTO.setToken(gentoken.toString());
                loginContext.setLoginState(LoginState.ONLINE.getName());

            } catch (IncorrectCredentialsException e) {
                log.info("zy-- this is IncorrectCredentialsException :",e);
                loginContext.setLoginState(LoginState.OFFLINE.getName());
                LoginFailEvent loginFailEvent = new LoginFailEvent(loginName);
                publishUserLogin(loginFailEvent);
                throw new IncorrectUserException();
            } catch (Exception e) {
                if (e instanceof AuthenticationException) {
                    throw new UserLockedException(loginName, loginContext.getRetrySec());
                }
                loginContext.setLoginState(LoginState.OFFLINE.getName());
                throw new UserLoginException();
            } finally {
                //登录后发布事件
                try {
                    //同步用户状态
                    userSync.updateUserState(loginContext);

                    event = new LoginAfterEvent();
                    publishUserLogin(event);
                } catch (Exception e) {
                    log.error("同步用户状态异常={}",e);
                    log.info("同步用户状态异常={}",e);
                }
            }
        } else {
            // 当验证码重新输入正确的情况下，将已经生成的token传回前台
            LoginContext loginContext = loginCacheInfoInfo.getCacheInfo(loginName);
            loginDTO = loginContext.getLoginDTO();
        }

        if (null != faillist && faillist.size() > 0) {
            loginDTO.setAlertlist(faillist);
        }
        MWPasswdInform mwPasswdInform = mwuserDao.selectInformByUserId(Integer.parseInt(loginParam.getUserId()));
        if (mwPasswdInform != null) {
            Reply reply = mwpasswordPlanService.selectById(mwPasswdInform.getInoperactivePasswdPlan());
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                faillist.add(Reply.fail(ErrorConstant.USER_MSG_100115));
                throw new ServiceException(faillist);
            }
            MWPasswordPlanDTO mwpasswordPlanDTO = (MWPasswordPlanDTO) reply.getData();
            if (mwpasswordPlanDTO != null && mwpasswordPlanDTO.getFirstPasswdEnable() && MWPasswdInform.userAddModify.equals(mwPasswdInform.getModifyType())) {
                faillist.add(Reply.fail(ErrorConstant.USER_MSG_100123));
                throw new ChangePasswdException("用户" + loginName + "首次登录，", loginDTO);
            }
            if (mwpasswordPlanDTO != null && MWPasswdInform.passwdPlanModify.equals(mwPasswdInform.getModifyType())) {
                faillist.add(Reply.fail(ErrorConstant.USER_MSG_100123));
                throw new ChangePasswdException("用户" + loginName + "修改了密码策略，", loginDTO);
            }
        }
        return Reply.ok(loginDTO);
    }

    @Override
    public Reply userlogout(String token) throws Throwable {
        List<Reply> faillist = new ArrayList<>();
        String loginName = loginCacheInfoInfo.getNameTokenMap(token);
        UserDTO userdto = new UserDTO();
        userdto.setLoginState(LoginState.OFFLINE.getName());
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        try {
            //获取当前用户
            GlobalUserInfo userInfo = getGlobalUser();
            MWUser mwuser = new MWUser();
            mwuser.setLoginState(LoginState.OFFLINE.getName());
            mwuser.setUserId(userInfo.getUserId());
            //更新用户状态
            mwuserDao.updateState(mwuser);
            String sessionkey = "USER_SESSION";
            HashMap<String, Integer> userSessionMap = new HashMap<>();
            if(redisUtils.hasKey(sessionkey)) {
                userSessionMap = (HashMap<String, Integer>)redisUtils.get(sessionkey);
            }
            if (userSessionMap.containsKey("shiro:session:"+token)) {
                userSessionService.saveLogoutTime(userSessionMap.get("shiro:session:"+token));
                userSessionMap.remove("shiro:session:"+token);
                redisUtils.set(sessionkey, userSessionMap);
            }
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
            LogoutEvent logoutEvent = new LogoutEvent(loginName);
            publishUserLogin(logoutEvent);
        } catch (Exception e) {
            log.error("fail to update user with userName={}", loginName, e);
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
        return Reply.ok(userdto);
    }

    @Override
    public Reply loginInfo(String token) {
        LoginInfo loginInfo = null;
        String loginName = loginCacheInfoInfo.getNameTokenMap(token);
        try {
            LoginContext loginContext = loginCacheInfoInfo.getCacheInfo(loginName);
            Map<String, MwRoleModulePermMapper> mwRoleModulePermMapper = loginContext.getMwRoleModulePermMapper();
            if (userDebug) {
                log.info("当前用户" + loginName + "的角色模块权限信息--------->" + JSONObject.toJSONString(mwRoleModulePermMapper));
            }
            MwRoleDTO roleInfo = loginCacheInfoInfo.getRoleInfo();
            String dataPerm = roleInfo.getDataPerm();

            if (userDebug) {
                log.info("当前用户" + loginName + "的数据权限------>" + dataPerm);
            }
            loginInfo = loginContext.getLoginInfo();
            loginInfo.setDataPerm(dataPerm);
            //判断环境是否启用资源中心
            loginInfo.setModelAssetEnable(modelAssetEnable);
        } catch (Exception e) {
            log.info("fail to loginInfo with userName={}，token ={}", loginName, token, e);
            /*throw new UserLoginInfoException();*/
        }
        return Reply.ok(loginInfo);
    }

    /**
     * 踢出用户
     */
    public KickOutDTO kickOut(LoginParam loginParam) {
        Subject currentUser = SecurityUtils.getSubject();
        String loginName = loginParam.getLoginName();


        UserDTO lastLoginDTO = (UserDTO) currentUser.getPrincipal();
        //用户使用在同一台主机登录不同的用户时，需要登出之前的用户
        if (null != lastLoginDTO && !loginName.equals(lastLoginDTO.getLoginName())) {
            currentUser.logout();
            currentUser = SecurityUtils.getSubject();
        }
        KickOutDTO kick = KickOutDTO.builder()
                .currentUser(currentUser)
                .loginName(loginName).build();
        return kick;
    }

    /**
     * 用户下拉框查询
     */
    @Override
    public Reply getDropDownUser() {
        try {
            String loginName = loginCacheInfo.getLoginName();
            String perm = loginCacheInfo.getRoleInfo().getDataPerm();
            List<MwGroupDTO> groupList = mwGroupTableDao.selectDropdown(loginName);
            List<Integer> groupIds = null;
            if (null != groupList && groupList.size() > 0) {
                List<Integer> finalGroupIds = new ArrayList<>();
                groupList.forEach(
                        groups -> finalGroupIds.add(groups.getGroupId())
                );
                groupIds = finalGroupIds;
            }
            //查询当前用户所在机构的节点
            List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
            List<MwUserDTO> mwScanList = mwuserDao.selectDropdown(loginName, groupIds, perm, nodes);
            Map<Integer,List<OrgDTO>> userOrgMap = getUserOrgMap();
            mwScanList.forEach(
                    mwUserDTO -> {
                        mwUserDTO.setUserName(mwUserDTO.getLoginName()
                                + "(" + mwUserDTO.getUserName() + ")");
                        mwUserDTO.setDepartment(userOrgMap.get(mwUserDTO.getUserId()));
                    }
            );
            return Reply.ok(mwScanList);
        } catch (Exception e) {
            log.error("fail to getDropDownUser", e);
            return Reply.fail(ErrorConstant.USER_100131, ErrorConstant.USER_MSG_100131);
        }
    }

    /**
     * 获取所有用户及所属机构的数据
     *
     * @return
     */
    private Map<Integer, List<OrgDTO>> getUserOrgMap() {
        Map<Integer, List<OrgDTO>> map = new HashMap<>();
        List<UserOrgDto> userOrgList = mwUserOrgMapperDao.selectAllOrgWithUserInfo();
        OrgDTO orgInfo;
        List<OrgDTO> orgList;
        int userId;
        for (UserOrgDto dto : userOrgList) {
            userId = dto.getUserId();
            if (map.containsKey(userId)) {
                orgList = map.get(userId);
            } else {
                orgList = new ArrayList<>();
            }
            orgInfo = new OrgDTO();
            orgInfo.setOrgId(dto.getOrgId());
            orgInfo.setOrgName(dto.getOrgName());
            orgList.add(orgInfo);
            map.put(userId, orgList);
        }
        return map;
    }

    /**
     * 删除用户信息
     *
     * @param idList
     * @return
     */
    @Override
    public Reply delete(List<Integer> idList) {

        try {
            //判断是否存在系统管理员
            if (idList.contains(Constants.SYSTEM_ADMIN)) {
                return Reply.fail(ErrorConstant.USER_100211, ErrorConstant.USER_MSG_100211);
            }
            //判断用户是否存在绑定资产
            List<Integer> userIdList = new ArrayList<>();
            int count;
            for (Integer userId : idList) {
                if (modelAssetEnable) {
                    count = commonDao.countUserType(userId, Arrays.asList(DataType.INSTANCE_MANAGE.getName()));
                } else {
                    count = commonDao.countUserType(userId, Arrays.asList(DataType.ASSETS.getName(), DataType.OUTBANDASSETS.getName(), DataType.INASSETS.getName()));
                }
                if (count > 0) {
                    userIdList.add(userId);
                }
            }
            if (!CollectionUtils.isEmpty(userIdList)) {
                List<MwUserDTO> userList = mwuserDao.selectDropdownByIdList(userIdList);
                StringBuffer sb = new StringBuffer();
                for (MwUserDTO user : userList) {
                    sb.append(",").append(user.getUserName());
                }
                if (sb.length() > 0) {
                    return Reply.fail("以下用户已绑定资产，无法删除：" + sb.substring(1));
                }
            }
            //若是AD用户，则需要先删除映射关系数据
            List<String> adUserList = mwuserDao.selectADUsersNameByIds(idList);
            if (!CollectionUtils.isEmpty(adUserList)){
                mwadUserDao.deleteByLoginName(adUserList);
            }

            mwuserDao.delete(idList);
            mwPagefieldTableDao.deleteByUserId(idList);
            mwUserGroupMapperDao.deleteBatchByUserId(idList);
            mwUserOrgMapperDao.deleteBatchByUserId(idList);
            mwUserRoleMapperDao.deleteUserRoleByUserId(idList);
            mwuserDao.deleteUserActionMapper(idList);
            mwuserDao.deleteUserMapper(idList);
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("fail to deleteOrg with ids={}", idList, e);
            return Reply.fail(ErrorConstant.USER_100104, ErrorConstant.USER_MSG_100104);

        }
    }

    @Override
    public Reply updateState(UpdateUserStateParam updateUserStateParam) {
        try {
            //禅道NO.1816
            if (Constants.SYSTEM_ADMIN.equals(updateUserStateParam.getId())){
                return Reply.fail(ErrorConstant.USER_100212, ErrorConstant.USER_MSG_100212);
            }
            List<OrgDTO> orgDTOList = mwUserOrgMapperDao.selectOrgNameByUserId(updateUserStateParam.getId());
            boolean flag = false;
            for (OrgDTO orgDTO : orgDTOList) {
                MWOrg mwOrg = mworgDao.selectByOrgId(orgDTO.getOrgId());
                if (UserActiveState.DISACTIVE.getName().equals(mwOrg.getEnable())) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                throw new CheckUpdateUserStateException(
                        ErrorConstant.USER_100139, ErrorConstant.USER_MSG_100139);
            }
            mwuserDao.updateUserState(updateUserStateParam);
        } catch (CheckUpdateUserStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to updateState with ids={}", updateUserStateParam.getId());
            return Reply.fail(ErrorConstant.USER_100208, ErrorConstant.USER_MSG_100208);
        }
        return Reply.ok("更新成功");
    }


    @Override
    public Reply insertSettings(SettingDTO settingDTO) {
        try {
            mwUserOrgMapperDao.insertSettings(settingDTO);
        } catch (Exception e) {
            return Reply.fail("设置失败");
        }
        return Reply.ok("设置成功");
    }

    @Override
    public Reply selectSettingsInfo() {
        try {
            SettingDTO settingInfo = mwUserOrgMapperDao.selectSettingsInfo();
            settingInfo.setLoginType(loginType);
            settingInfo.setRedirectUrl(redirectUrl == null ? "" : redirectUrl);
            SettingCaptchaDTO dto = SettingCaptchaDTO.builder()
                    .settingDTO(settingInfo)
                    .enableCaptcha(this.enableCaptcha).build();
            return Reply.ok(dto);
        } catch (Exception e) {
            log.error("fail to selectSettingsInfo , cause:{}", e.getMessage());
            return Reply.fail(e.getMessage());

        }

    }

    @Override
    public Reply customColLoad(UserDTO userdto) throws Throwable {
        List<Reply> faillist = new ArrayList<Reply>();
        try {
            //获取用户表所有用户id
            List<Integer> userIds = mwuserDao.selectAllUserId();
            //生成初始化的表头和查询头信息
            faillist = publishPostEvent(new CustomColLoadEvent(userIds));
            if (faillist.size() > 0) {
                throw new ServiceException(faillist);
            }
            return Reply.ok("初始化表头成功！");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to custom pagefield ", e);
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
    }

    @Override
    public Reply updatePassword(UserDTO userdto) {

        List<Reply> faillist = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(userdto.getUserId().toString())) {

                String msg = Reply.replaceMsg(ErrorConstant.USER_MSG_100112, new String[]{"用户id"});
                faillist.add(Reply.fail(ErrorConstant.USER_100112, msg));
                throw new ServiceException(faillist);
            }
            //查询用户信息
            MWUser user = mwuserDao.selectByUserId(userdto.getUserId());
            if (UserType.LDAP.getType().equals(user.getUserType())) {
                throw new CheckADUserEditorException(
                        ErrorConstant.AD_100704, ErrorConstant.AD_MSG_100704);
            }
            String loginName = user.getLoginName();
            UserDTO olduserDTO = CopyUtils.copy(UserDTO.class, user);

            Reply reply = mwpasswordPlanService.selectById(olduserDTO.getActivePasswdPlan());
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                faillist.add(Reply.fail(ErrorConstant.USER_MSG_100115));
                throw new ServiceException(faillist);
            }
            MWPasswordPlanDTO mwpasswordPlanDTO = (MWPasswordPlanDTO) reply.getData();
            if (null != userdto.getPassword() && !"".equals(userdto.getPassword())) {
                //生成更新事件，进行密码校验等处理
                UpdUserEvent updUserEvent = new UpdUserEvent(userdto.getPassword(),
                        olduserDTO, userdto, mwpasswordPlanDTO);
                faillist = publishCheckEvent(updUserEvent);
                if (null != faillist && faillist.size() > 0) {
                    throw new ServiceException(faillist);
                }
                //生成密码
                UserDTO puserDTO = CopyUtils.copy(UserDTO.class, userdto);
                if (null != userdto.getOldPassword()) {
                    puserDTO.setLoginName(olduserDTO.getLoginName());
                    Credential credential = new Credential(puserDTO.getLoginName(), puserDTO.getOldPassword(),
                            mwpasswordPlanDTO.getSalt(), mwpasswordPlanDTO.getHashTypeId());
                    String password = passwordManage.encryptPassword(credential);
                    if (!password.equals(olduserDTO.getPassword())) {
                        throw new PasswdCheckException(ErrorConstant.USER_MSG_100137);
                    }
                }
                MWPasswordPlanDTO md;
                if (null != userdto.getPassword()) {
                    puserDTO.setLoginName(olduserDTO.getLoginName());
                    MWPasswdInform mwPasswdInform = mwuserDao.selectInformByUserId(puserDTO.getUserId());
                    if (mwPasswdInform != null) {
                        Reply reply1 = mwpasswordPlanService.selectById(mwPasswdInform.getInoperactivePasswdPlan());
                        if (reply1.getRes() != PaasConstant.RES_SUCCESS) {
                            faillist.add(Reply.fail(ErrorConstant.USER_MSG_100115));
                            throw new ServiceException(faillist);
                        }
                        md = (MWPasswordPlanDTO) reply1.getData();
                    } else {
                        Reply reply1 = mwpasswordPlanService.selectById(olduserDTO.getActivePasswdPlan());
                        md = (MWPasswordPlanDTO) reply1.getData();
                    }
                    Credential credential = new Credential(puserDTO.getLoginName(), puserDTO.getPassword(),
                            md.getSalt(), md.getHashTypeId());
                    String password = passwordManage.encryptPassword(credential);
                    userdto.setPassword(password);

                    //产生密码生成事件
                    faillist = publishCheckEvent(new GenPasswdEvent(password, olduserDTO));
                    if (null != faillist && faillist.size() > 0) {
                        throw new ServiceException(faillist);
                    }
                }
            }
            // 设置密码过期时间
            Calendar calendar = Calendar.getInstance();
            if (null != mwpasswordPlanDTO.getPasswdUpdateDate()) {
                calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, mwpasswordPlanDTO.getPasswdUpdateDate());
            }
            MWUser mwUser = CopyUtils.copy(MWUser.class, userdto);
            mwUser.setModifier(loginName);
            mwUser.setPasswdState(PasswdState.DEFAULT.getName());
            mwUser.setPasswdExpiryDate(calendar.getTime());

            MWPasswdInform mwPasswdInform = mwuserDao.selectInformByUserId(mwUser.getUserId());
            if (mwPasswdInform != null) {
                mwuserDao.updateInform(mwUser.getUserId());
                mwUser.setActivePasswdPlan(mwPasswdInform.getInoperactivePasswdPlan());
                mwUser.setDefaultPasswdPlan(mwPasswdInform.getInoperactivePasswdPlan());
            }
            mwuserDao.update(mwUser);

            logger.info("ACCESS_LOG[]user[]用户管理[]更新用户[]{}", userdto.getLoginName());
            PostUpdUserEvent postUpdUserEvent = new PostUpdUserEvent(olduserDTO, userdto, mwpasswordPlanDTO);
            publishPostEvent(postUpdUserEvent);
            Subject currentUser = SecurityUtils.getSubject();
            currentUser.logout();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to update user with userName={}, cause:{}", userdto.getLoginName(), e.getMessage());
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.error("密码修改失败",throwable);
        }

        return Reply.ok("密码修改成功！");
    }

    @Override
    public Reply updateUserInfo(UserDTO userdto) {

        String loginName = loginCacheInfo.getLoginName();
        try {
            //支持多手机号功能
            if (!StringUtils.isEmpty(userdto.getPhoneNumber())) {
                int index = userdto.getPhoneNumber().indexOf(",");
                if (index > -1) {
                    String phoneNumber = userdto.getPhoneNumber().substring(0, index);
                    String morePhone = userdto.getPhoneNumber().substring(index + 1);
                    userdto.setPhoneNumber(phoneNumber);
                    userdto.setMorePhones(morePhone);
                }
            } else {
                userdto.setMorePhones("");
            }
            MWUser mwUser = CopyUtils.copy(MWUser.class, userdto);
            mwUser.setModifier(loginName);
            mwuserDao.update(mwUser);
            if(alertLevel.equals(AlertEnum.HUAXING.toString())){
                userSubscribe(userdto);
            }
            logger.info("ACCESS_LOG[]user[]用户管理[]更新用户[]{}", userdto.getLoginName());
        } catch (Exception e) {
            log.error("fail to update user with userName={}, cause:{}", userdto.getLoginName(), e.getMessage());
        }
        return Reply.ok("用户修改成功！");
    }

    private void userSubscribe(UserDTO param){
        try{
            mwuserDao.deleteUserSubRuleId(param.getUserId());
            mwuserDao.deleteUserSubModelSystem(param.getUserId());
            if(!CollectionUtils.isEmpty(param.getSubscribeModelSystem())){
                mwuserDao.insertUserSubModelSystem(param.getUserId(), param.getSubscribeModelSystem());
            }
            if(!CollectionUtils.isEmpty(param.getSubscribeRuleIds())){
                mwuserDao.insertUserSubRuleId(param.getUserId(), param.getSubscribeRuleIds());
            }
        }catch (Exception e){
            log.error("华星订阅消息失败:{}",e);
        }

    }

    @Override
    public Reply selectCurrUserInfo(UserDTO userdto) {
        try {
            MwUserDTO mwUserDTO = mwuserDao.selectCurrUserInfo(userdto.getUserId());
            if (!StringUtils.isEmpty(mwUserDTO.getMorePhones())) {
                mwUserDTO.setPhoneNumber(mwUserDTO.getPhoneNumber() + "," + mwUserDTO.getMorePhones());
            }
            if(alertLevel.equals(AlertEnum.HUAXING.toString())){
                mwUserDTO.setSubscribeModelSystem(mwuserDao.selectUserSubModelSystem(userdto.getUserId()));
                mwUserDTO.setSubscribeRuleIds(mwuserDao.selectUserSubRuleId(userdto.getUserId()));
            }
            logger.info("ACCESS_LOG[]user[]用户管理[]根据用户ID取用户信息[]{}", userdto.getUserId());
            return Reply.ok(mwUserDTO);
        } catch (Exception e) {
            log.error("fail to selectByUserId with userId={}, cause:{}", userdto.getUserId(), e.getMessage());
            return Reply.fail(ErrorConstant.USER_100107, ErrorConstant.USER_MSG_100107);
        }
    }

    @Override
    public Reply resetPassword(UserDTO userdto) throws Throwable {

        List<Reply> faillist = new ArrayList<>();
        String loginName = loginCacheInfoInfo.getLoginName();

        try {
            //查询用户信息
            MWUser user = mwuserDao.selectByUserId(userdto.getUserId());
            if (UserType.LDAP.getType().equals(user.getUserType())) {
                throw new CheckADUserEditorException(
                        ErrorConstant.AD_100704, ErrorConstant.AD_MSG_100704);
            }
            UserDTO olduserDTO = CopyUtils.copy(UserDTO.class, user);
            Reply reply;
            //查询用戶密码策略
            reply = mwpasswordPlanService.selectById(olduserDTO.getActivePasswdPlan());

            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                faillist.add(Reply.fail(ErrorConstant.USER_MSG_100115));
                throw new ServiceException(faillist);
            }
            MWPasswordPlanDTO mwpasswordPlanDTO = (MWPasswordPlanDTO) reply.getData();

            //生成更新事件,进行密码校验等处理
            UpdUserEvent updUserEvent = new UpdUserEvent(userdto.getPassword()
                    , olduserDTO, userdto, mwpasswordPlanDTO);
            faillist = publishCheckEvent(updUserEvent);
            if (faillist.size() > 0) {
                throw new ServiceException(faillist);
            }

            UserDTO puserDTO = CopyUtils.copy(UserDTO.class, userdto);
            puserDTO.setLoginName(olduserDTO.getLoginName());
            log.info("userdto:{}",userdto);
            log.info("重置密码 用户名:{}",userdto.getLoginName()+userdto.getPassword());
            Credential credential = new Credential(puserDTO.getLoginName(), puserDTO.getPassword(),
                    mwpasswordPlanDTO.getSalt(), mwpasswordPlanDTO.getHashTypeId());
            String password = passwordManage.encryptPassword(credential);
            log.info("重置密码 加密后的密码:{}",password);
            userdto.setPassword(password);

            //产生密码生成事件
            faillist = publishCheckEvent(new GenPasswdEvent(password, olduserDTO));
            if (null != faillist && faillist.size() > 0) {
                throw new ServiceException(faillist);
            }
            MWUser mwUser = CopyUtils.copy(MWUser.class, userdto);
            mwUser.setModifier(loginName);
            mwuserDao.update(mwUser);

            PostUpdUserEvent postPasswdEvent = new PostUpdUserEvent(olduserDTO, userdto, mwpasswordPlanDTO);
            publishPostEvent(postPasswdEvent);

        } catch (CheckUpdateUserStateException e) {
            throw e;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to update user with userName={}", userdto.getLoginName(), e);
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }

        return Reply.ok("更新成功！");
    }

    /**
     * 批量更新用户信息
     *
     * @param userDTO 用户数据
     * @return
     */
    @Override
    public Reply batchUpdateUsers(UserDTO userDTO) {
        List<Integer> batchUserIds = userDTO.getBatchUserIds();
        if (batchUserIds.contains(Constants.SYSTEM_ADMIN)){
            return Reply.fail(ErrorConstant.USER_100211, ErrorConstant.USER_MSG_100211);
        }
        //校验数据
        Reply resultReply;
        resultReply = checkUserParam(userDTO);
        if (!PaasConstant.RES_SUCCESS.equals(resultReply.getRes())) {
            return resultReply;
        }
        batchUserIds.forEach(id -> {
            MWUser mwUser = mwuserDao.selectByUserId(id);
            UserDTO oldUser = CopyUtils.copy(UserDTO.class, mwUser);
            if (userDTO.getCheckDepartment()) {
                oldUser.setDepartment(userDTO.getDepartment());
            }
            if (userDTO.getCheckUserGroup()) {
                oldUser.setUserGroup(userDTO.getUserGroup());
            }
            if (userDTO.getCheckValidityType()) {
                oldUser.setValidityType(userDTO.getValidityType());
                oldUser.setUserExpiryDate(userDTO.getUserExpiryDate());
            }
            if (userDTO.getCheckUserState()) {
                oldUser.setUserState(userDTO.getUserState());
            }
            if (userDTO.getCheckRoleId()) {
                oldUser.setRoleId(userDTO.getRoleId());
            }
            if (userDTO.getCheckUserControlEnable()) {
                oldUser.setUserControlEnable(userDTO.getUserControlEnable());
                oldUser.setControlSettings(userDTO.getControlSettings());
                oldUser.setActionValue(userDTO.getActionValue());
                oldUser.setConditionsValue(userDTO.getConditionsValue());
            }
            if (userDTO.getCheckActivePasswdPlan()) {
                oldUser.setActivePasswdPlan(userDTO.getActivePasswdPlan());
            }
            try {
                updateUser(oldUser);
            } catch (Throwable throwable) {
//                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                log.error("----------batch editor failed>:", throwable);
            }
        });
        return Reply.ok("更新成功！");
    }

    /**
     * 校验数据
     * @param userDTO 用户信息
     * @return
     */
    private Reply checkUserParam(UserDTO userDTO) {
        if (userDTO.getCheckDepartment()) {
            if (CollectionUtils.isEmpty(userDTO.getDepartment())){
                return Reply.fail("请选择机构");
            }
        }
        if (userDTO.getCheckRoleId()) {
            if (userDTO.getRoleId() == null || userDTO.getRoleId() < 0){
                return Reply.fail("请选择角色");
            }
        }
        return Reply.ok();
    }

    @Override
    public Reply selectAdUser(String loginName) {
        try {
            String userType = AdType.AD.name();
            UserDTO user = mwuserDao.selectADUserByType(loginName, userType);

            logger.info("ACCESS_LOG[]user[]用户管理[]根据用户名取用户信息[]{}", loginName);
            return Reply.ok(user);
        } catch (Exception e) {
            log.error("fail to selectByUserName with loginName={}", loginName, e);
            throw new UserBrowseException();
        }
    }

    @Override
    public Reply getUserList(QueryUserParam param) {
        try {
            Reply reply = null;
            String loginName = loginCacheInfo.getLoginName();
            String roleId = loginCacheInfo.getRoleId(loginName);
            UserDTO userDTO = mwuserDao.selectByLoginName(loginName);
            param.setUserId(userDTO.getUserId());
            if (MWUtils.ROLE_TOP_ID.equals(roleId)) {
                reply = pageUser(param);
            } else {
                reply = pageCernetUser(param);
            }
            return Reply.ok(reply);
        } catch (Exception e) {
            return Reply.fail(ErrorConstant.USER_100106, ErrorConstant.USER_MSG_100106);
        }
    }

    /**
     * 获取模糊查询内容
     *
     * @param qParam 请求参数
     * @return 模糊查询列表数据
     */
    @Override
    public Reply getFuzzySearchContent(QueryUserParam qParam) {
        try {
            //获取所有的用户数据
            Map criteria = PropertyUtils.describe(qParam);
            Map resultMap = new HashMap();
            List<MwUserDTO> userList = mwuserDao.selectAllUserList(criteria);
            List<String> loginNameList = new ArrayList<>();
            List<String> userNameList = new ArrayList<>();
            List<String> allList = new ArrayList<>();
            for (MwUserDTO user : userList) {
                loginNameList.add(user.getLoginName());
                userNameList.add(user.getUserName());
                allList.add(user.getUserName());
                allList.add(user.getLoginName());
            }
            //进行数据排序
            Comparator<Object> com = Collator.getInstance(Locale.CHINA);
            Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
            allList = allList.stream().distinct().collect(Collectors.toList());
            allList = allList.stream()
                    .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1), pinyin4jUtil.getStringPinYin(o2)))
                    .collect(Collectors.toList());
            userNameList = userNameList.stream()
                    .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1), pinyin4jUtil.getStringPinYin(o2)))
                    .collect(Collectors.toList());
            loginNameList = loginNameList.stream()
                    .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1), pinyin4jUtil.getStringPinYin(o2)))
                    .collect(Collectors.toList());
            //保存数据
            resultMap.put("allList", allList);
            resultMap.put("loginName", loginNameList);
            resultMap.put("userName", userNameList);
            return Reply.ok(resultMap);
        } catch (Exception e) {
            log.error("获取用户模糊查询数据失败", e);
            return Reply.fail(ErrorConstant.USER_100106, ErrorConstant.USER_MSG_100106);
        }
    }

    /**
     * 获取当前登录用户的信息
     *
     * @return 用户信息
     */
    @Override
    public GlobalUserInfo getGlobalUser() {
        GlobalUserInfo userInfo = new GlobalUserInfo();
        try {
            String loginName = loginCacheInfo.getLoginName();
            Integer userId = loginCacheInfo.getCacheInfo(loginName).getUserId();
            //数据权限：private public
            String perm = loginCacheInfo.getRoleInfo().getDataPerm();
            DataPermission dataPermission = DataPermission.valueOf(perm);
            //用户角色是否为系统管理员
            Boolean isAdmin = false;
            //用户所在的用户组id
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            if (null != groupIds && groupIds.size() > 0) {
                userInfo.setUserGroupIdList(groupIds);
            }
            //获取用户组织机构数据
            String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
            List<Integer> orgIds = new ArrayList<>();
            if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                isAdmin = true;
            }
            if (!isAdmin) {
                orgIds = mwOrgCommonService.getAllOrgIdsByName(loginName);
            }
            //获取用户姓名等信息
            MwUserDTO user = mwuserDao.selectById(userId);
            userInfo.setUserName(user.getUserName());
            if (null != orgIds && orgIds.size() > 0) {
                userInfo.setOrgIdList(orgIds);
            }
            userInfo.setLoginName(loginName);
            userInfo.setDataPermission(dataPermission);
            userInfo.setUserId(userId);
            userInfo.setSystemUser(isAdmin);
            userInfo.setRoleId(roleId);
            return userInfo;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return new GlobalUserInfo();
        }
    }



    /**
     * 根据用户ID获取用户的信息
     *
     * @return 用户信息
     */
    @Override
    public GlobalUserInfo getGlobalUser(Integer userId) {
        GlobalUserInfo userInfo = new GlobalUserInfo();
        String loginName = mwUserCommonService.getLoginNameByUserId(userId);

        //数据权限：private public
        String perm = mwUserCommonService.getRolePermByUserId(userId);
        DataPermission dataPermission = DataPermission.valueOf(perm);
        //用户角色是否为系统管理员
        Boolean isAdmin = false;
        //用户所在的用户组id
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            userInfo.setUserGroupIdList(groupIds);
        }
        //获取用户组织机构数据
        String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
        List<Integer> orgIds = new ArrayList<>();
        if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
            isAdmin = true;
        }
        if (!isAdmin) {
            orgIds = mwOrgCommonService.getAllOrgIdsByName(loginName);
        }
        if (null != orgIds && orgIds.size() > 0) {
            userInfo.setOrgIdList(orgIds);
        }
        //获取用户姓名等信息
        MwUserDTO user = mwuserDao.selectById(userId);
        userInfo.setUserName(user.getUserName());
        userInfo.setLoginName(loginName);
        userInfo.setDataPermission(dataPermission);
        userInfo.setUserId(userId);
        userInfo.setSystemUser(isAdmin);
        userInfo.setRoleId(roleId);
        return userInfo;
    }

    /**
     * 获取所有的用户列表
     *
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 获取所有的用户列表
     */
    @Override
    public List<cn.mw.monitor.service.assets.model.UserDTO> getAllUserList(int typeId, DataType dataType) {
        return mwuserDao.getAllUserList(typeId, dataType.getName());
    }

    /**
     * 获取所有的类别ID
     *
     * @param userInfo 用户信息
     * @param dataType 类别
     * @return 当前用户在该类别下的所有ID
     */
    @Override
    public List<String> getAllTypeIdList(GlobalUserInfo userInfo, DataType dataType) {
        return getTypeIdList(userInfo,dataType,ALL);
    }


    /**
     * 获取所有的类别ID
     *
     * @param userInfo 用户信息
     * @param dataType 类别
     * @return 当前用户在该类别下的所有ID
     */
    @Override
    public List<String> getTypeIdListByOrgIds(GlobalUserInfo userInfo, DataType dataType) {
        return getTypeIdList(userInfo, dataType, ORG);
    }

    /**
     * 获取所有的类别ID
     *
     * @param userInfo 用户信息
     * @param dataType 类别
     * @return 当前用户在该类别下的所有ID
     */
    @Override
    public List<String> getTypeIdListByGroupIds(GlobalUserInfo userInfo, DataType dataType) {
        return getTypeIdList(userInfo, dataType, GROUP);
    }

    /**
     * 获取所有的类别ID
     *
     * @param userInfo 用户信息
     * @param dataType 类别
     * @param type     0:全部  1：机构  2：用户组
     * @return 当前用户在该类别下的所有ID
     */
    private List<String> getTypeIdList(GlobalUserInfo userInfo, DataType dataType,int type){
        if (userInfo.isSystemUser()) {
            return new ArrayList<>();
        }
        List<String> resultList = new ArrayList<>();
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("userId", userInfo.getUserId());
        queryMap.put("dataType", dataType.getName());
        queryMap.put("queryType", type);
        queryMap.put("dataPermission", userInfo.getDataPermission().getName());
        //针对ORACLE环境超过1000条数据，使用in查询会报错。将查询逻辑更改为:若list不为null。则分割查询
        List<List<Integer>> orgLists = null;
        List<List<Integer>> groupLists = null;
        if (!CollectionUtils.isEmpty(userInfo.getOrgIdList())) {
            orgLists = Lists.partition(userInfo.getOrgIdList(), SELECT_MAX);
        } else {
            queryMap.put("orgIdList", null);
        }
        if (!CollectionUtils.isEmpty(userInfo.getUserGroupIdList())) {
            groupLists = Lists.partition(userInfo.getUserGroupIdList(), SELECT_MAX);
        } else {
            queryMap.put("userGroupIdList", null);
        }
        List<String> list;
        if (!CollectionUtils.isEmpty(orgLists) && ((type == ALL) || (type == ORG))) {
            queryMap.put("userGroupIdList", null);
            for (List<Integer> orgList : orgLists) {
                if (CollectionUtils.isEmpty(orgList)) {
                    continue;
                }
                queryMap.put("orgIdList", orgList);
                list = commonDao.getAllTypeIdList(queryMap);
                if (!CollectionUtils.isEmpty(list)) {
                    resultList.addAll(list);
                }
            }
        }
        if (!CollectionUtils.isEmpty(groupLists) && ((type == ALL) || (type == GROUP))) {
            for (List<Integer> groupList : groupLists) {
                if (CollectionUtils.isEmpty(groupList)) {
                    continue;
                }
                queryMap.put("userGroupIdList", groupList);
                list = commonDao.getAllTypeIdList(queryMap);
                if (!CollectionUtils.isEmpty(list)) {
                    resultList.addAll(list);
                }
            }
        }
        //去重返回
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public Reply pageCernetUser(QueryUserParam qParam) {

        try {
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            MwUserDTO mwUserDTO = mwuserDao.selectById(qParam.getUserId());
            List<OrgDTO> department = mwUserDTO.getDepartment();
            List<Integer> ids = new ArrayList<>();
            List<MwUserDTO> users = new ArrayList<>();
            department.forEach(
                    dto -> ids.addAll(mworgDao.selectChildUserIdByOrgId(dto.getOrgId()))
            );
            List<Integer> userIds = ids.stream().distinct().collect(Collectors.toList());
            userIds.forEach(
                    id -> users.add(mwuserDao.selectById(id))
            );
            PageInfo<?> pageInfo = new PageInfo<>(users);

            logger.info("ACCESS_LOG[]user[]用户管理[]分页查询赛尔移动端用户信息[]{}[]", qParam);
            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to pageCernetUser with QueryUserParam=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USER_100106, ErrorConstant.USER_MSG_100106));
        }
    }

    @Autowired
    public void addchecks(List<IMWUserListener> checklisteners) {
        addCheckLists(checklisteners);
    }

    @Autowired
    public void addPostProcessors(List<IMWUserPostProcesser> postlisteners) {
        addPostProcessorList(postlisteners);
    }

    @Override
    public String getQyWeixinAccessToken(String appid, String appsecret) throws IOException {
        String token = null;
        synchronized (MWUserService.class){
            RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
            boolean isHas = redisUtils.hasKey(appid + "QyWeixinAccessToken");
            if (!isHas) {
                //获取access_token
                String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?" +
                        "corpid=" + appid +
                        "&corpsecret=" + appsecret;
                JSONObject jsonObject = AuthUtil.doGetJson(url);
                String actoken = jsonObject.getString("access_token");
                String expireIn = jsonObject.getString("expires_in");
                //将token放入redis中保存
                RedisUtils redis = SpringUtils.getBean(RedisUtils.class);
                redis.set("QyWeixinAccessToken", actoken, Long.parseLong(expireIn));
            }
            token = (String) redisUtils.get(appid + "QyWeixinAccessToken");
        }
        return token;
    }

    /**
     * 导出用户导入excel模板
     *
     * @param response 导出数据
     */
    @Override
    public void excelTemplateExport(HttpServletResponse response) {
        List<ExportUserParam> list = new ArrayList<>();
        ExportUserParam importUserParam = ExportUserParam.builder()
                .loginName("test")
                .userName("张三(导入前该行请删除)")
                .phoneNumber("13056675668")
                .password("1234qwer")
                .orgs("测试机构/测试子机构（导入前请删除该行，多层用/区分）")
                .groups("终端用户")
                .roleName("普通用户")
                .build();
        list.add(importUserParam);
        Set<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("loginName");
        includeColumnFiledNames.add("userName");
        includeColumnFiledNames.add("phoneNumber");
        includeColumnFiledNames.add("password");
        includeColumnFiledNames.add("orgs");
        includeColumnFiledNames.add("groups");
        includeColumnFiledNames.add("roleName");
        ExcelWriter excelWriter = null;
        try {
            excelWriter = ExcelUtils.getExcelWriter("exportUserTemplate", response, ExportUserParam.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(list, sheet);
            logger.info("导出成功");
        } catch (IOException e) {
            logger.error("导出失败{}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 导出用户导入excel模板
     *
     * @param response 导出数据
     * @param qParam   用户数据
     */
    @Override
    public void exportUserExcel(HttpServletResponse response, QueryUserParam qParam) {
        //导出列表数据
        List<UserExport> list = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        Set<String> includeColumnFiledNames = new HashSet<>();
        try {
            users = mwuserDao.listUserIdByParams(qParam.getOrgId());
        } catch (Exception e) {
            log.error("导出用户失败", e);
            UserExport error = UserExport.builder()
                    .exportErrorMsg("导出失败")
                    .build();
            list.add(error);
            includeColumnFiledNames.add("exportErrorMsg");
        }
        if (!CollectionUtils.isEmpty(users)){
            includeColumnFiledNames.add("loginName");
            includeColumnFiledNames.add("userName");
            includeColumnFiledNames.add("phoneNumber");
            includeColumnFiledNames.add("password");
            includeColumnFiledNames.add("orgs");
            includeColumnFiledNames.add("groups");
            includeColumnFiledNames.add("roleName");
            includeColumnFiledNames.add("weChatNo");
            includeColumnFiledNames.add("dingTalkNo");
            includeColumnFiledNames.add("emailNo");
            includeColumnFiledNames.add("validTime");
            includeColumnFiledNames.add("userState");
            includeColumnFiledNames.add("userType");
            for (int userId : users) {
                UserExport importUserParam= geneExportUser(userId);
                list.add(importUserParam);
            }
        }
        ExcelWriter excelWriter = null;
        try {
            excelWriter = ExcelUtils.getExcelWriter("exportUserTemplate", response, UserExport.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(list, sheet);
            logger.info("导出成功");
        } catch (IOException e) {
            logger.error("导出失败{}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }
    }

    private UserExport geneExportUser(int userId) {
        UserExport userExport = new UserExport();
        try {
            MwUserDTO user = mwuserDao.selectById(userId);
            userExport.setLoginName(user.getLoginName());
            userExport.setUserName(user.getUserName());
            userExport.setPhoneNumber(user.getPhoneNumber());
            userExport.setPassword("******");
            if (CollectionUtils.isEmpty(user.getDepartment())) {
                userExport.setOrgs("");
            } else {
                String dept = "";
                for (OrgDTO org : user.getDepartment()) {
                    dept += org.getOrgName() + ",";
                }
                userExport.setOrgs(dept.substring(0, dept.length() - 1));
            }
            if (CollectionUtils.isEmpty(user.getUserGroup())) {
                userExport.setGroups("");
            } else {
                String group = "";
                for (GroupDTO groupInfo : user.getUserGroup()) {
                    group += groupInfo.getGroupName() + ",";
                }
                userExport.setGroups(group.substring(0, group.length() - 1));
            }
            userExport.setRoleName(user.getRole().getRoleName());
            userExport.setWeChatNo(user.getWechatId());
            userExport.setDingTalkNo(user.getDdId());
            userExport.setEmailNo(user.getEmail());
            userExport.setValidTime(DateUtils.formatDate(user.getUserExpiryDate()));
            userExport.setUserState(user.getUserState().equals("ACTIVE") ? "正常" : "禁用");
            userExport.setUserType(user.getUserType());
        } catch (Exception e) {
            log.error("构建用户数据失败", e);
        }
        return userExport;
    }

    /**
     * 用户批量导入
     *
     * @param file     excel文件数据
     * @param response 失败数据返回
     */
    @Override
    public void excelImport(MultipartFile file, HttpServletResponse response) {
        try {
            String fileName = file.getOriginalFilename();
            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                EasyExcel.read(file.getInputStream(), ExportUserParam.class,
                        new UserExcelImportListener(response, fileName)).sheet().doRead();
            } else {
                logger.error("没有传入正确的excel文件名", file);
            }
        } catch (Exception e) {
            logger.error("fail to excelImport with MultipartFile={}, cause:{}", file, e);
        }
    }

    /**
     * 校验用户导入数据
     *
     * @param userParam 用户导入数据
     * @return 用户导入数据
     * @throws TransformException 校验异常数据
     */
    @Override
    public RegisterParam transform(ExportUserParam userParam) throws TransformException {
        //待导入的用户信息
        RegisterParam registerParam = new RegisterParam();
        //错误信息
        List<String> errorMsg = new ArrayList<>();

        //校验登录名
        String loginName = userParam.getLoginName();
        if (cn.mwpaas.common.utils.StringUtils.isNotEmpty(loginName)) {
            //判断用户名称是否重复，重复则无法导入
            UserDTO user = mwuserDao.selectByLoginName(loginName);
            if (user != null) {
                errorMsg.add("用户名重复");
            } else {
                registerParam.setLoginName(loginName);
            }
        } else {
            errorMsg.add("用户名不能为空");
        }

        //校验用户姓名
        String userName = userParam.getUserName();
        if (cn.mwpaas.common.utils.StringUtils.isEmpty(userName)) {
            errorMsg.add("姓名不能为空");
        } else {
            registerParam.setUserName(userName);
        }

        //校验手机号码
        String phone = userParam.getPhoneNumber();
        if (cn.mwpaas.common.utils.StringUtils.isNotEmpty(phone)) {
            if (!PhoneUtils.checkMobileNumber(phone)) {
                errorMsg.add("手机号错误");
            } else {
                registerParam.setPhoneNumber(phone);
            }
        }

        //校验登录密码
        String password = userParam.getPassword();
        //检查密码长度
        MWPasswdPlan mwPasswdPlan = mwPasswdDAO.selectById(Constants.defaultPasswdPlanId);
        if (password.length() < mwPasswdPlan.getPasswdMinLen()) {
            errorMsg.add(ErrorConstant.USER_MSG_100110 + mwPasswdPlan.getPasswdMinLen());
        }
        //检查密码复杂度
        MWPassCompType mwpassCompType = mwpasswCompTypeDao.selectById(mwPasswdPlan.getPasswdComplexId());
        PasswdComCheck passwdComCheck = new PasswdComCheckImpl(mwpassCompType.getTypeNum());
        List<Reply> retlist = passwdComCheck.checkComplex(password);
        if (cn.mwpaas.common.utils.CollectionUtils.isNotEmpty(retlist)) {
            errorMsg.add(retlist.get(0).getMsg());
        }
        registerParam.setPassword(password);

        //校验机构信息
        String orgName = userParam.getOrgs();
        List<List<Integer>> orgNodes = new ArrayList<>();
        if (cn.mwpaas.common.utils.StringUtils.isNotEmpty(orgName)) {
            orgName = orgName.trim();
        } else {
            errorMsg.add("机构不能为空");
        }
        orgName = orgName.replaceAll("；", ";");
        String[] splitOrg = orgName.split(";");
        for (String org : splitOrg) {
            int pid = 0;
            //如果是子部门，则通过/分割
            String[] orgNameArray = org.split("/");
            OrgDTO deptInfo = null;
            //获取部门信息
            for (int i = 0; i < orgNameArray.length; i++) {
                String nodeName = orgNameArray[i];
                OrgDTO orgDTO = mworgDao.selectOrgByName(nodeName, pid);
                if (orgDTO == null) {
                    errorMsg.add(org + "  不存在");
                    break;
                } else {
                    pid = orgDTO.getOrgId();
                }
                if (i == orgNameArray.length - 1) {
                    deptInfo = orgDTO;
                }
            }
            if (deptInfo != null) {
                List<Integer> orgList = new ArrayList<>();
                List<String> nodes = Arrays.stream(deptInfo.getNodes().split(",")).collect(Collectors.toList());
                nodes.forEach(node -> {
                    if (cn.mwpaas.common.utils.StringUtils.isNotEmpty(node)) {
                        orgList.add(Integer.valueOf(node));
                    }
                });
                orgNodes.add(orgList);
            }
        }
        registerParam.setDepartment(orgNodes);

        //转换用户组
        String groupName = userParam.getGroups();
        if (groupName != null) {
            groupName = groupName.trim();
        }
        if (groupName != null && cn.mwpaas.common.utils.StringUtils.isNotEmpty(groupName)) {
            groupName = groupName.replaceAll("；", ";");
            String[] splitGroup = groupName.split(";");
            List<Integer> groupIds = mwGroupTableDao.selectGroupIdsByGroupNames(Arrays.asList(splitGroup));
            if (groupIds == null || groupIds.size() <= 0) {
                errorMsg.add("用户组名称错误或者不存在该用户组");
            } else {
                registerParam.setUserGroup(groupIds);
            }
        }

        //校验用户角色信息
        String roleName = userParam.getRoleName();
        if (cn.mwpaas.common.utils.StringUtils.isNotEmpty(roleName)) {
            MwRole role = mwRoleDao.selectRoleByRoleName(roleName);
            if (role != null) {
                registerParam.setRoleId(role.getRoleId());
            } else {
                errorMsg.add("角色名称不存在");
            }
        } else {
            errorMsg.add("角色名称不能为空");
        }

        if (errorMsg.size() > 0) {
            throw new TransformException(cn.mwpaas.common.utils.StringUtils.join(errorMsg, ";"));
        }

        //更新用户其他基础信息
        registerParam.setUserState(UserActiveState.ACTIVE.getName());
        registerParam.setActivePasswdPlan(Constants.defaultPasswdPlanId);
        registerParam.setValidityType("1");
        registerParam.setUserControlEnable(false);
        return registerParam;
    }

    /**
     * 获取所有的负责人列表信息
     *
     * @param param 参数
     * @return
     */
    @Override
    public Reply getAuthUserList(ChangeUserParam param) {
        try {
            DataType dataType = DataType.getDataTypeByName(param.getDataType());
            if (dataType == null){
                return Reply.fail("获取失败，没有对应的数据类型");
            }
            List<Integer> idList = commonDao.getUserListByDataType(dataType.getName());
            List<MwUserDTO> userList =  mwuserDao.selectDropdownByIdList(idList);
            return Reply.ok(userList);
        } catch (Exception e) {
            log.error("获取所有的负责人列表信息失败",e);
        }
        return Reply.fail("获取所有的负责人列表信息失败");
    }

    /**
     * 更新负责人信息
     *
     * @param param 参数
     * @return
     */
    @Override
    public Reply changeUserAuth(ChangeUserParam param) {
        try {
            DataType dataType = DataType.getDataTypeByName(param.getDataType());
            if (dataType == null) {
                return Reply.fail("获取失败，没有对应的数据类型");
            }
            //现支持1对1修改
            int userId = param.getUserId();
            int changedUserId = param.getChangedUserId();
//            int userId = param.getUserList().get(0);
//            int changedUserId = param.getChangedUserList().get(0);
            commonDao.changeUserDataPermission(dataType.getName(), userId, changedUserId);
            //资源中心数据更新（es）
            InstanceShiftPowerParam shiftParam = new InstanceShiftPowerParam();
            shiftParam.setBeforeUserId(userId);
            shiftParam.setAfterUserId(changedUserId);
            shiftParam.setSkipDataPermission(true);
            mwModelViewCommonService.batchShiftPowerByUser(shiftParam);
            return Reply.ok();
        } catch (Exception e) {
            log.error("更新负责人信息失败", e);
        }
        return Reply.fail("更新负责人信息失败");
    }

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        List<Reply> replyList = new ArrayList<>();
        replyList.add(Reply.ok());
        //redis过期更新信息
        if (event instanceof RedisExpireEvent) {
            RedisExpireEvent redisExpireEvent = (RedisExpireEvent) event;
            log.info("redisExpireEvent={}",redisExpireEvent);
            try {
                RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
                String key = "LOGIN_USER_INFO_MAP";
                String userSessionKey = "USER_SESSION";
                HashMap<String, Integer> loginUserMap = new HashMap<>();
                HashMap<String, Integer> userSessionMap = new HashMap<>();
                if (redisUtils.hasKey(key)) {
                    loginUserMap = (HashMap<String, Integer>) redisUtils.get(key);
                }
                if(redisUtils.hasKey(userSessionKey)){
                    userSessionMap = (HashMap<String, Integer>) redisUtils.get(userSessionKey);
                }
                if (loginUserMap.containsKey(redisExpireEvent.getKey()) && userSessionMap.containsKey(redisExpireEvent.getKey())) {
                    log.info("redisExpireEvent.getKey()={}",redisExpireEvent.getKey());
                    log.info("loginUserMap={}",loginUserMap);
                    MWUser mwuser = new MWUser();
                    mwuser.setUserId(loginUserMap.get(redisExpireEvent.getKey()));
                    mwuser.setLoginState(LoginState.OFFLINE.getName());
                    log.info("mwuser={}",mwuser);
                    mwuserDao.updateState(mwuser);
                    userSessionService.timeOutLogout(userSessionMap.get(redisExpireEvent.getKey()));
                    loginUserMap.remove(redisExpireEvent.getKey());
                    redisUtils.set(key, loginUserMap);
                }
            } catch (Exception e) {
                log.error("RedisExpireEvent", e);
            }
        }
        return replyList;
    }
}
