package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.exception.CheckADAccountExceptinon;
import cn.mw.monitor.api.exception.CheckADConfigtExceptinon;
import cn.mw.monitor.api.exception.CheckADConnectException;
import cn.mw.monitor.api.param.aduser.*;
import cn.mw.monitor.api.param.org.AddUpdateOrgParam;
import cn.mw.monitor.api.param.org.BindUserOrgParam;
import cn.mw.monitor.api.param.user.BindUserGroupParam;
import cn.mw.monitor.api.param.user.ChangeUserParam;
import cn.mw.monitor.api.param.user.QueryUserParam;
import cn.mw.monitor.api.param.user.UpdateUserStateParam;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.customPage.dao.MwPagefieldTableDao;
import cn.mw.monitor.event.CustomColLoadEvent;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.exception.UserException;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWOrg;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.shiro.UserType;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.state.UserActiveState;
import cn.mw.monitor.user.common.ADUtils;
import cn.mw.monitor.user.common.AdDepartment;
import cn.mw.monitor.user.common.SortByLengthComparator;
import cn.mw.monitor.user.dao.*;
import cn.mw.monitor.user.dto.*;
import cn.mw.monitor.user.model.*;
import cn.mw.monitor.user.service.*;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by zy.quaee on 2021/4/27 9:33.
 **/
@Service
@Slf4j
@Transactional
public class MWADUserServiceImpl extends ListenerService implements MWADUserService {

    @Resource
    MwUserRoleMapperDao mwUserRoleMapperDao;

    @Resource
    private MWADUserDao mwadUserDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MWUserDao mwUserDao;

    @Resource
    private MWGroupService mwGroupService;

    @Resource
    private MWOrgService mwOrgService;

    @Resource
    MwPagefieldTableDao mwPagefieldTableDao;

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    @Resource
    private MWUserDao mwuserDao;

    @Autowired
    UserSync userSync;

    @Resource
    private MwUserGroupMapperDao mwUserGroupMapperDao;

    @Resource
    private MwUserOrgMapperDao mwUserOrgMapperDao;

    @Autowired
    private MWUserService userService;

    /**
     * redis前缀
     */
    private static String REDIS_PREFIX = "ldap-user-list-";

    /**
     * redis同步用户前缀
     */
    private static String REDIS_SYNC_USER_PREFIX = "ldap-sync-user-";

    /**
     * redis同步AD机构前缀
     */
    private static String REDIS_SYNC_AD_ORG_PREFIX = "ldap-sync-org-";

    private static ScheduledExecutorService service;

    @Value("${datasource.check}")
    private String check;

    @Value("${user.ldap.leave.url}")
    private String LDAP_SYNC_LEAVE_URL;

    @Value("${user.ldap.sync.param}")
    private String LDAP_SYNC_PARAM;

    /**
     * LDAP服务器密码是否显示
     */
    @Value("${user.ldap.server.password.visible}")
    private Boolean LDAP_SERVER_PASSWORD_VISIBLE;



    private AdUserSyncParam syncParam;

    @Override
    @Transactional
    public Reply authenticAdmin(ADAuthenticParam param) {

        String adName = param.getAdAdminAccount();
        MWDomainAuthenDTO mda = null;
        if (StringUtils.isBlank(adName) ||
                StringUtils.isBlank(param.getAdAdminPasswd()) || !adName.contains("@")) {
            log.error("帐号或密码为空或帐号格式不正确");
            return Reply.fail(ErrorConstant.AD_MSG_100707, "帐号或密码为空或帐号格式不正确");
        }
        LdapContext ctx = null;
        AdDepartment a = null;
        try {
            String domainName = param.getAdAdminAccount().split("@")[1];
            param.setDomainName(domainName);
            Hashtable<String, String> env = ADUtils.getEnv(param);
            ctx = ADUtils.getContext(env);
            log.info("AD域服务器身份验证成功!");
 /*           int count = mwadUserDao.count(param);
            MWDomainAuthenDTO ma = CopyUtils.copy(MWDomainAuthenDTO.class, param);
            if (count > 0) {
                mwadUserDao.update(param);
            }
            mwadUserDao.insert(ma);
            mda = mwadUserDao.selectInfo(param);*/
        } catch (AuthenticationException e) {
            log.error("AD域服务器信息不正确", e);
            throw new CheckADAccountExceptinon();
        } catch (javax.naming.CommunicationException e) {
            log.error("AD域连接失败", e);
            throw new CheckADConnectException();
        } catch (Exception e) {
            log.error("AD信息保存失败", e);
            return Reply.fail(ErrorConstant.AD_MSG_100708, "AD信息保存失败");
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("--------------ad域链接 LdapContext未正常关闭!", e);
                }
            }
        }
        return Reply.ok(mda);
    }

    @Override
    public Reply select(QueryADInfoParam param) {
        String adType = param.getAdType();
        String domainName = param.getAdAdminAccount().split("@")[1];
        String base = "";
        if (param.isRuleCreate()) {
            base = searchBase(param.getAdAdminAccount());
        }else {
            base = param.getSearchNodes();
        }
        Set<AdDepartment> as = new TreeSet<>();
        LdapContext ctx = null;
        AdDepartment ad = null;
        try {
            ADAuthenticParam ap = CopyUtils.copy(ADAuthenticParam.class, param);
            ctx = getContext(ap);
            as = new TreeSet<>(new SortByLengthComparator());
            Set<AdDepartment> a = ADUtils.getAdDepartment(ctx, adType, base);
            as.addAll(a);
            ad = ADUtils.getTreeAdDepartment(as, domainName);
            return Reply.ok(ad);
        } catch (Exception e) {
            log.error("get ad department fail ", e);
            return Reply.fail("查询失败");
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("--------------ad域链接 LdapContext未正常关闭!", e);
                }
            }
        }
    }

    @Override
    @Transactional
    public Reply addADUser(AddADUserParam param) throws Throwable {
        List<ADUserDetailDTO> userList = param.getUserList();
        AtomicReference<Integer> adUserCount = new AtomicReference<>(0);
        String domainName =getDomainName(param.getAdAdminAccount());
        MWDomainInfoDTO ma = CopyUtils.copy(MWDomainInfoDTO.class, param);
        String userType =AdType.AD.name();
        List<String> collect = userList.stream().map(ADUserDetailDTO::getGroupName).collect(Collectors.toList());
        HashSet<String> hs = new HashSet<>(collect);
        List<String> groupList = new ArrayList<>();
        try {
            ADAuthenticParam ap = CopyUtils.copy(ADAuthenticParam.class, param);
            getContext(ap);
            log.info("AD OU|GROUP Info ---------:" + param.getSearch());
            {
                log.info("ad user name list ------->" + userList);
                String creator = param.getAdAdminAccount();
                Calendar calendar = new GregorianCalendar(2099, 12, 31);
                Boolean userState;
                for (ADUserDetailDTO user : userList) {
                    userState = StringUtils.isEmpty(user.getEnabled()) || Boolean.parseBoolean(user.getEnabled());
                    UserActiveState userActiveState = userState ? UserActiveState.ACTIVE : UserActiveState.DISACTIVE;
                    MWUser mwUser = MWUser.builder()
                            .phoneNumber(user.getPhone())
                            .email(user.getMail())
                            .defaultPasswdPlan(1)
                            .activePasswdPlan(1)
                            .userName(user.getUserName().replaceAll(" ",""))
                            .loginName(user.getLoginName() + "@" + domainName)
                            .userType(AdType.AD.name())
                            .creator(creator)
                            .adUserGroupName(user.getGroupName())
                            .userExpiryDate(calendar.getTime())
                            .modifier(creator)
                            .wechatId(user.getWxNo())
                            .ddId(user.getDingdingNo())
                            .userState(userActiveState.getName())
                            .build();
                    // 若已经存在用户信息，则先硬删除
                    UserDTO u = mwUserDao.selectADUserByType(mwUser.getLoginName(), userType);
                    if (u != null) {
                        mwadUserDao.updateUser(mwUser.getLoginName(), userType);
                    }
                    // 新增AD用户信息
                    mwadUserDao.insertUser(mwUser);

                    adUserCount.getAndSet(adUserCount.get() + 1);
                    UserDTO userDTO = mwUserDao.selectADUserByType(mwUser.getLoginName(),userType);
                    //新增用户用户id存储到临时表中
                    MwTempUserDTO userTemp = MwTempUserDTO.builder()
                            .userId(userDTO.getUserId()).creator(creator).build();
                    mwadUserDao.insertTempUser(userTemp);

                    mwUserRoleMapperDao.insertUserRoleMapper(MwUserRoleMap
                            .builder()
                            .userId(userDTO.getUserId())
                            .roleId(Integer.valueOf(param.getRoleId()))
                            .build()
                    );
                    //生成初始化的表头和查询头信息
                    List<Reply> faillist = null;
                    try {
                        List<Integer> userId = new ArrayList<>();
                        userId.add(userDTO.getUserId());
                        faillist = publishPostEvent(new CustomColLoadEvent(userId));
                    } catch (Throwable throwable) {
                        log.error("生成初始化的表头和查询头信息失败",throwable);
                    }

                    if (faillist != null && faillist.size() > 0) {
                        throw new ServiceException(faillist);
                    }
                    List<Integer> userIds = new ArrayList<>();
                    userIds.add(userDTO.getUserId());
                    //绑定用户和用户组
                    if (param.getUserGroup() != null && param.getUserGroup().size() > 0) {
                        param.getUserGroup().forEach(
                                g -> {
                                    Reply reply = mwGroupService.selectById(g);
                                    MwGroupDTO mgd = (MwGroupDTO) reply.getData();
                                    ma.setGroupInfo(mgd.getGroupName());
                                }
                        );
                        ma.setUserGroup(param.getUserGroup().stream().map(String::valueOf).collect(Collectors.joining(",")));
                        mwGroupService.bindUserGroup(BindUserGroupParam.builder().flag(1)
                                .groupIds(param.getUserGroup()).userIds(userIds).build());
                    }
                    //绑定用户和机构
                    List<Integer> orgIdList = new ArrayList<>();
                    if (!param.isConfigAddUser()) {
                        if (param.getDepartment() != null) {
                            param.getDepartment().forEach(orgIds -> {
                                        orgIdList.add(orgIds.get(orgIds.size() - 1));
                                    }
                            );
                            StringBuffer stringBuffer = new StringBuffer();
                            orgIdList.forEach(o ->
                                    {
                                        Reply reply = mwOrgService.selectByOrgId(o);
                                        MWOrg mwOrg = (MWOrg) reply.getData();
                                        if (mwOrg != null && StringUtils.isNotEmpty(mwOrg.getOrgName())){
                                            stringBuffer.append(mwOrg.getOrgName()).append(",");
                                        }
                                    }
                            );
                            ma.setLocalInfo(stringBuffer.substring(0, stringBuffer.length() - 1));
                            ma.setDepartment(orgIdList.stream().map(String::valueOf).collect(Collectors.joining(",")));
                        }
                    } else {
                        orgIdList.addAll(param.getOrgIdList());
                    }
                    mwOrgService.bindUserOrg(BindUserOrgParam.builder().flag(1).orgIds(orgIdList).userIds(userIds).build());
                    groupList.add(user.getGroupName());
                }
            }
            List<String> groups = removeDuplicate(groupList);
            log.info("remove duplicate gorup success ------");


            groups.forEach(
                    group->{
                        //映射配置
                        ma.setAdInfo(group);
                        log.info("insert config info  ----->" + ma);
                        if (!param.isConfigAddUser()) {
                            List<MWADInfoDTO> exisitsInfo = mwadUserDao.selectByADInfo(ma);
                            AtomicBoolean flag = new AtomicBoolean(false);
                            if (exisitsInfo!=null && exisitsInfo.size()>0) {
                                exisitsInfo.forEach(
                                        info-> {
                                            flag.set(info.getLocalInfo().equals(ma.getLocalInfo()));
                                            if (flag.get()) {
                                                throw new CheckADConfigtExceptinon();
                                            }
                                        }
                                );
                            }
                            mwadUserDao.insertInfo(ma);
                            MwTempConfigDTO configDTO = MwTempConfigDTO.builder()
                                    .configId(ma.getId()).creator(param.getAdAdminAccount()).build();
                            mwadUserDao.insertTempConfig(configDTO);
                        }else {
                            ma.setId(param.getConfigId());
                        }
                        AtomicReference<Integer> id = new AtomicReference<>(ma.getId());
                        List<ADUserDetailDTO> users = userList.stream().filter(
                                u -> u.getGroupName().equals(group)).collect(Collectors.toList()
                        );
                        users.forEach(
                                us->{
                                    us.setLoginName(us.getLoginName()+"@"+domainName);
                                    us.setId(id.get());
                                    //将原有的用户数据删除，再重新添加
                                    mwadUserDao.deleteByLoginName(Arrays.asList(us.getLoginName()));
                                    mwadUserDao.insertConfigUser(us);
                                    log.info("insert config user success ---");
                                }
                        );
                        id.getAndSet(id.get() + 1);
                        ma.setId(id.get());
                    }
            );
            ADImportSuccDTO as = ADImportSuccDTO.builder()
                    .adCount(hs.size())
                    .adUserCount(adUserCount.get()).build();
            return Reply.ok(as);
        }catch (UserException e) {
            throw e;
        }catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("fail to add ad user :", e);
            return Reply.fail("导入失败");
        }
    }

    @Override
    public Reply configBrowse(QueryADInfoParam param) {
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());
        try {
            List<MWADInfoDTO> infos = mwadUserDao.selectConfig(param);
            infos.forEach(
                    info ->{
                        if (info.getGroupId() != null) {
                            info.setUserGroup(Arrays.stream(info.getGroupId().split(","))
                                    .map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList()));
                        } else {
                            info.setGroupId("");
                        }
                        if (info.getOrgId() != null) {
                            info.setDepartment(Arrays.stream(info.getOrgId().split(","))
                                    .map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList()));
                        } else {
                            info.setGroupId("");
                        }
                    }

            );
            PageInfo<?> pageInfo = new PageInfo<>(infos);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to browse ad config :", e);
            return Reply.fail(ErrorConstant.AD_MSG_100705, "查询AD配置失败");
        }
    }

    /**
     * 删除AD用户配置数据
     * @param param
     * @return
     */
    @Override
    public Reply deleteById(QueryADInfoParam param) {
        try {
            //先判断是否存在用户，若存在用户数据，则无法删除。
            if (mwadUserDao.countByConfigId(param.getId()) > 0) {
                return Reply.fail(ErrorConstant.AD_MSG_100712);
            }
            //删除AD用户映射数据
            mwadUserDao.deleteConfig(param.getId());
            //获取当前映射配置下的所有用户数据
            List<MWUser> users = mwadUserDao.selectGroupUserById(param.getId());
            //删除AD用户数据
            mwadUserDao.deleteADConfigUser(param.getId());
            if (users != null && users.size() > 0) {
                List<Integer> userIds = new ArrayList<>();
                users.forEach(
                        user -> userIds.add(user.getUserId())
                );
                deleteCommon(userIds);
            }
        } catch (Exception e) {
            log.error("fail to delete ad config :", e);
            return Reply.fail(ErrorConstant.AD_MSG_100706, "删除AD配置失败");
        }
        return Reply.ok();
    }

    /**
     * 获取AD用户信息
     *
     * @param param 请求参数
     * @return 返回包含AD用户数据的信息
     */
    @Override
    public Reply selectByName(AddADUserParam param) {
        String redisKey = REDIS_PREFIX + JSON.toJSONString(param.getSearch());
        PageInfo<ADUserDetailDTO> pageInfo = new PageInfo<>();
        List<ADUserDetailDTO> userList = new ArrayList<>();
        int fromIndex = param.getPageSize() * (param.getPageNumber() - 1);
        int toIndex = param.getPageSize() * param.getPageNumber();
        try {
            //翻页不会和删除动作冲突，首页数据获取按照最新一页
            if (redisTemplate.hasKey(redisKey) && param.getPageNumber() > 1) {
                userList = (List<ADUserDetailDTO>) redisTemplate.opsForValue().get(redisKey);
            } else {
                //获取AD用户数据
                userList = getLDAPUsers(param);
                if (CollectionUtils.isNotEmpty(userList)) {
                    saveToRedis(redisKey, userList);
                }
            }
        } catch (NamingException e) {
            log.info("get ad users  failed :", e);
            return Reply.fail("查询AD用户失败", e);
        }
        List<String> collect = new ArrayList<>();
        List<ADUserDetailDTO> result = new ArrayList<>();
        List<ADUserDetailDTO> pageInfoList = new ArrayList<>();

        //如果用户选中了登录名选项
        if (StringUtils.isNotBlank(param.getFuzzyLoginName())
                || (param.getFuzzyNormal() != null && "loginName".equalsIgnoreCase(param.getFuzzyNormal()))) {
            String newRedisKey = redisKey + "-loginName";
            if (redisTemplate.hasKey(newRedisKey)) {
                collect = (List<String>) redisTemplate.opsForValue().get(newRedisKey);
            } else {
                collect = userList.stream().filter(user -> user.getLoginName() != null).map(ADUserDetailDTO::getLoginName)
                        .collect(Collectors.toList());
                Collections.sort(collect);
                saveToRedis(newRedisKey, collect);
            }
        }
        //如果用户选中了姓名选项
        if (StringUtils.isNotBlank(param.getFuzzyUserName())
                || (param.getFuzzyNormal() != null && "userName".equalsIgnoreCase(param.getFuzzyNormal()))) {
            String newRedisKey = redisKey + "-userName";
            if (redisTemplate.hasKey(newRedisKey)) {
                collect = (List<String>) redisTemplate.opsForValue().get(newRedisKey);
            } else {
                collect = userList.stream().filter(user -> user.getUserName() != null).map(ADUserDetailDTO::getUserName)
                        .collect(Collectors.toList());
                Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                collect = collect.stream()
                        .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1), pinyin4jUtil.getStringPinYin(o2)))
                        .collect(Collectors.toList());
                saveToRedis(newRedisKey, collect);
            }
        }

        List<String> temp = new ArrayList<>();
        String newRedisKey = redisKey + "-all";
        if (redisTemplate.hasKey(newRedisKey)) {
            temp = (List<String>) redisTemplate.opsForValue().get(newRedisKey);
        } else {
            if (userList != null && userList.size() > 0) {
                for (ADUserDetailDTO user : userList) {
                    if (StringUtils.isNotBlank(user.getLoginName())) {
                        temp.add(user.getLoginName());
                    }
                    if (StringUtils.isNotBlank(user.getUserName())) {
                        temp.add(user.getUserName());
                    }
                }
                Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                temp = temp.stream()
                        .sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1), pinyin4jUtil.getStringPinYin(o2)))
                        .collect(Collectors.toList());
                saveToRedis(newRedisKey, temp);
            }
        }

        List<String> list = new ArrayList<>();
        if (param.isFuzzyQuery()) {
            if (StringUtils.isBlank(param.getFuzzyLoginName()) && StringUtils.isBlank(param.getFuzzyUserName())) {
                result = userList.stream().filter(user ->
                        (user.getUserName().contains(param.getFuzzyNothing()) || user.getLoginName().contains(param.getFuzzyNothing())))
                        .collect(Collectors.toList());
            } else if (StringUtils.isNotBlank(param.getFuzzyLoginName())) {
                result = userList.stream().filter(user ->
                        (user.getLoginName().contains(param.getFuzzyLoginName())))
                        .collect(Collectors.toList());
            } else if (StringUtils.isNotBlank(param.getFuzzyUserName())) {
                result = userList.stream().filter(user ->
                        (user.getUserName().contains(param.getFuzzyUserName())))
                        .collect(Collectors.toList());
            }
            if (fromIndex < 0 || fromIndex > result.size()) {
                fromIndex = 0;
                toIndex = param.getPageSize();
            }
            if (toIndex > result.size()) {
                toIndex = result.size();
            }
            pageInfoList.addAll(result.subList(fromIndex, toIndex));
            pageInfo.setTotal(result.size());
        } else {
            if ("loginName".equalsIgnoreCase(param.getFuzzyNormal())
                    || "userName".equalsIgnoreCase(param.getFuzzyNormal())) {
                list.addAll(collect);
            } else {
                list.addAll(temp);
            }
            if (fromIndex < 0 || fromIndex > userList.size()) {
                fromIndex = 0;
                toIndex = param.getPageSize();
            }
            if (toIndex > userList.size()) {
                toIndex = userList.size();
            }
            pageInfoList.addAll(userList.subList(fromIndex, toIndex));
            pageInfo.setTotal(userList.size());
        }
        pageInfo.setList(pageInfoList);
        MwFuzzyDTO mwFuzzyDTO = MwFuzzyDTO.builder()
                .pageInfo(pageInfo)
                .list(list).build();
        return Reply.ok(mwFuzzyDTO);
    }

    /**
     * 获取AD用户数据集合
     * @param param 请求参数
     * @return AD用户列表
     */
    private List<ADUserDetailDTO> getLDAPUsers(AddADUserParam param) throws NamingException {
        if (syncParam == null){
            syncParam = generateSyncParam(LDAP_SYNC_PARAM);
        }
        String filter = param.getType().toLowerCase() + "_user";
        List<ADUserDetailDTO> userList = new ArrayList<>();
        ADAuthenticParam ap = CopyUtils.copy(ADAuthenticParam.class, param);
        LdapContext finalCtx = getContext(ap);
        param.getSearch().forEach(
                search -> {
                    List<MWDomainUserDTO> md = null;
                    try {
                        md = ADUtils.getUsers(finalCtx, search, filter,syncParam);
                        log.info(" 获取AD用户信息 ------> " + md);
                    } catch (Exception e) {
                        log.info("get ad users  failed :", e);
                    }
                    if (md != null) {
                        md.forEach(
                                u -> {
                                    String name = u.getCName().substring(u.getCName().lastIndexOf("/") + 1);
                                    Pattern pattern = Pattern.compile("[\\d]");
                                    Matcher matcher = pattern.matcher(name);
                                    String trimName = matcher.replaceAll("").trim();
                                    String group = u.getCName().substring(0, u.getCName().lastIndexOf("/"));
                                    String groupName = group.substring(group.lastIndexOf("/") + 1);
                                    ADUserDetailDTO add = ADUserDetailDTO.builder()
                                            .userName(StringUtils.isNotEmpty(u.getUserName())?u.getUserName():trimName)
                                            .loginName(u.getLoginName())
                                            .mail(u.getMail())
                                            .phone(u.getPhone())
                                            .groupName(groupName)
                                            .wxNo(u.getWxNo())
                                            .enabled(u.getEnabled())
                                            .manager(u.getManager())
                                            .dingdingNo(u.getDingdingNo())
                                            .build();
                                    String fullLoginName = getFullLoginName(u.getLoginName());
                                    UserDTO user = mwUserDao.selectADUserByType(fullLoginName, AdType.AD.name());
                                    if (user != null) {
                                        add.setNeedAdd(false);
                                    }else {
                                        add.setNeedAdd(true);
                                    }
                                    userList.add(add);
                                }
                        );
                    }
                    log.info("ad user name list ------->" + userList);
                }
        );
        return userList;
    }

    /**
     * 构建AD用户同步数据
     *
     * @param param
     * @return
     */
    private AdUserSyncParam generateSyncParam(String param) {
        AdUserSyncParam sync;
        try {
            if (StringUtils.isEmpty(param)) {
                sync = new AdUserSyncParam();
                sync.setUserName("displayname");
                sync.setLoginName("sAMAccountName");
                sync.setPhone("mobile");
                sync.setMail("mail");
            }else {
                sync = JSON.parseObject(param, AdUserSyncParam.class);
            }
            return sync;
        } catch (Exception e) {
            log.error("构建AD用户同步数据fail", e);
            return null;
        }
    }

    /**
     * 域名的获取
     * @return domainName
     */
    public  String getDomainName() {
        ADConfigDTO ad = mwadUserDao.select();
        return ad.getDomainName();
    }

    public  String getDomainName(String account) {
        return  account.contains("@")?account.split("@")[1]:account;
    }
    /**
     * 完整登录名
     * @return loginName
     */
    public String getFullLoginName(String loginName) {
        return loginName+"@"+getDomainName();
    }

    @Override
    public Reply selectGroupUser(ADGroupUserParam param) {
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());
        List<MWUser> mwUsers = new ArrayList<>();
        try {
            mwUsers  = mwadUserDao.selectGroupUserById(param.getConfigId());
            mwUsers.forEach(m->m.setDomainName(getDomainName()));
            PageInfo<?> pageInfo = new PageInfo<>(mwUsers);
            return Reply.ok(pageInfo);
        }catch (Exception e) {
            log.error(" fail to select ad group user ",e);
            return Reply.fail("根据AD组名查询用户失败",e);
        }
    }

    /**
     * sy AD服务器信息查询
     * @return
     */
    @Override
    public Reply seletSyAdInfo() {
        try {
           MwLdapAuthenticInfoDTO info = mwadUserDao.selectSyAdInfo();
           info.setPasswordVisible(LDAP_SERVER_PASSWORD_VISIBLE);
           return Reply.ok(info);
        }catch (Exception e) {
            log.error("select ad server fail :",e);
            return null;
        }
    }

    @Transactional
    @Override
    public Reply insertAdInfo(AdCommonParam param) {
        List<Integer> configIdList = new ArrayList<>();
        List<Integer> userIdList = new ArrayList<>();
        try {
            if (param.isSaveConfig()) {
                mwadUserDao.truncateAuthent();
                param.setDomainName(getDomainName(param.getAdAdminAccount()));
                 mwadUserDao.insertAdInfo(param);
            }else {
               configIdList =  mwadUserDao.selectTempConfigId();
               userIdList =  mwadUserDao.selectTempUserId();
               if (configIdList!=null&&configIdList.size()>0) {
                   mwadUserDao.deleteByConfigList(configIdList);
               }

               if (userIdList!=null&&userIdList.size()>0) {
                   List<String> dtos = new ArrayList<>();
                   userIdList.forEach(
                           id-> {
                               MwUserDTO mwUserDTO = mwuserDao.selectById(id);
                               dtos.add(mwUserDTO.getLoginName());
                           }
                   );
                   mwadUserDao.deleteByLoginName(dtos);
                   deleteCommon(userIdList);
               }
            }
            //清空临时表数据
            mwadUserDao.truncateTempUser();
            mwadUserDao.truncateTempConfig();
            return Reply.ok("保存配置成功");
        }catch (Exception e) {
            log.error(" ad server fail to insert :",e);
            return null;
        }
    }

    @Transactional
    @Override
    public Reply deleteADUser(DeleteADUserParam param) {
        List<Integer> idList = param.getUserIdList();
        try {
            mwadUserDao.deleteConfigUser(param.getIdList());
            deleteCommon(idList);
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("fail to deleteOrg with ids={}", idList, e);
            return Reply.fail(ErrorConstant.USER_100104, ErrorConstant.USER_MSG_100104);

        }
    }

    /**
     * 删除AD用户关联数据
     *
     * @param idList AD用户ID集合（id为mw_sys_user的主键）
     */
    private void deleteCommon(List<Integer> idList) {
        //删除用户表的数据（软删除）
        mwuserDao.delete(idList);
        //删除用户自定义栏目数据（硬删除）
        mwPagefieldTableDao.deleteByUserId(idList);
        //删除模型管理用户自定义的栏目数据
        mwPagefieldTableDao.deleteByModelUserId(idList);
        //删除用户组和用户关联关系(软删除)
        mwUserGroupMapperDao.deleteBatchByUserId(idList);
        //删除机构和用户关联关系（软删除）
        mwUserOrgMapperDao.deleteBatchByUserId(idList);
        //删除用户和角色的关联关系（软删除）
        mwUserRoleMapperDao.deleteUserRoleByUserId(idList);
        //删除用户和告警动作关联关系
        mwuserDao.deleteUserActionMapper(idList);
        //删除用户模板映射关系（硬删除）
        mwuserDao.deleteUserMapper(idList);
    }

    @Override
    public Reply browseUser(QueryADInfoParam param) {
        try {
            PageHelper.startPage(param.getPageNumber(),param.getPageSize());
            List<MWADConfigUserDTO> users = mwadUserDao.selectConfigInfo(param);
            users.forEach(m->m.setDomainName(getDomainName()));
            PageInfo<?> infos = new PageInfo<>(users);
            return Reply.ok(infos);
        }catch (Exception e) {
            log.error(" ad server fail to browse :",e);
            return null;
        }
    }

    /**
     * 同步LDAP用户
     *
     * @param param 同步参数
     * @return
     */
    @Override
    public Reply syncADUser(SyncUserParam param) {
        //立即同步
        if (1 == param.getSyncNow()) {
            String key = REDIS_SYNC_USER_PREFIX + "now";
            if (redisTemplate.hasKey(key)) {
                return Reply.fail("LDAP用户正在同步中，请稍等...");
            } else {
                redisTemplate.opsForValue().set(key, "1");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        syncADUser(key);
                    }
                });
                thread.start();
            }
        } else {
            //定时同步
            if (param.getUseFlag() == 1) {
                if (param.getIntervalTime() <= 0 || param.getIntervalTime() > 99999999){
                    return Reply.fail("请填写正确的时间间隔");
                }
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        syncADUser("");
                    }
                };
                //先删除之前的定时任务，重新启用任务
                if (service != null) {
                    service.shutdownNow();
                    service = null;
                }
                if (service == null) {
                    service = Executors.newSingleThreadScheduledExecutor();
                }
                service.scheduleAtFixedRate(runnable, 1, param.getIntervalTime(),
                        TimeUnit.values()[param.getTimeUnit() + 2]);
            } else {
                //先删除之前的定时任务，不启用任务
                if (service != null) {
                    service.shutdownNow();
                    service = null;
                }
            }
        }
        return Reply.ok();
    }

    /**
     * 修改配置备注
     *
     * @param param 参数
     * @return
     */
    @Override
    public Reply updateConfigDesc(AddADUserParam param) {
        try {
            //修改AD配置备注
            mwadUserDao.updateDesc(param.getConfigId(), param.getConfigDesc());
            return Reply.ok();
        } catch (Exception e) {
            log.error("修改配置备注失败", e);
            return Reply.fail("修改失败");
        }
    }

    /**
     * 同步AD用户
     *
     * @return
     */
    @Override
    public TimeTaskRresult syncADUser() {
        TimeTaskRresult result = new TimeTaskRresult();
        String key = REDIS_SYNC_USER_PREFIX + "syncByTimeTask";
        if (redisTemplate.hasKey(key)){
            result.setSuccess(false).setFailReason("同步任务正在同步中").setResultContext("同步失败");
            return result;
        }
        redisTemplate.opsForValue().set(key, "1");
        syncADUser(key);
        result.setSuccess(true).setResultContext("同步AD用户成功");
        return result;
    }

    /**
     * 同步AD域机构数据
     *
     * @return
     */
    @Override
    public Reply syncADOrg() {
        String key = REDIS_SYNC_AD_ORG_PREFIX + "now";
        if (redisTemplate.hasKey(key)) {
            return Reply.fail("LDAP机构正在同步中，请稍等...");
        } else {
            redisTemplate.opsForValue().set(key, "1");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    syncADOrg(key);
                }
            });
            thread.start();
        }
        return Reply.ok("开始同步，请稍后");
    }

    /**
     * 同步机构数据
     * @param key redisKey
     */
    private void syncADOrg(String key) {
        try {
            MwLdapAuthenticInfoDTO authenticInfo = mwadUserDao.selectSyAdInfo();
            QueryADInfoParam queryParam = new QueryADInfoParam();
            queryParam.setAdServerName(authenticInfo.getServerName());
            queryParam.setAdAdminAccount(authenticInfo.getAdAccount());
            queryParam.setAdAdminPasswd(authenticInfo.getAdPasswd());
            queryParam.setAdServerIpAdd(authenticInfo.getIpAddress());
            queryParam.setAdPort(authenticInfo.getPort());
            queryParam.setAdType(AdType.OU.name());
            queryParam.setRuleCreate(true);
            Reply reply = this.select(queryParam);
            if ( PaasConstant.RES_SUCCESS.equals( reply.getRes()) ){
                AdDepartment adDepartment = (AdDepartment) reply.getData();
                //同步机构数据
                for (AdDepartment child : adDepartment.getChildren()) {
                    insertAdOrg(MWOrgServiceImpl.ROOT_ORG, child);
                }
            }
        } catch (Exception e) {
            log.error("同步AD机构数据失败",e);
        } finally {
            //删除redis缓存
            if (StringUtils.isNotEmpty(key)) {
                //删除redis
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * 插入AD机构数据
     *
     * @param pid   机构PID
     * @param child 机构数据
     */
    private void insertAdOrg(int pid, AdDepartment child) {
        AddUpdateOrgParam orgParam = new AddUpdateOrgParam();
        orgParam.setPid(pid);
        orgParam.setOrgName(child.getName());
        int newPid = mwOrgService.insertOrg(orgParam);
        if (CollectionUtils.isNotEmpty(child.getChildren())) {
            for (AdDepartment department : child.getChildren()) {
                insertAdOrg(newPid, department);
            }
        }
    }

    public LdapContext getContext(ADAuthenticParam ap) throws NamingException {
        Hashtable<String, String> env = ADUtils.getEnv(ap);
        return ADUtils.getContext(env);
    }

    /**
     * 根据域名 解析节点
     *
     * @param adName
     * @return
     */
    private static String searchBase(String adName) {
        return getString(adName);
    }

    public static String getString(String adName) {
        StringBuilder searchBase = new StringBuilder();
        String[] split = adName.split("@");
        String domain = split[1];
        String[] dc = domain.split("\\.");
        for (String s : dc) {
            searchBase.append("DC=").append(s).append(",");
        }
        return searchBase.substring(0, searchBase.length() - 1);
    }

    private String genRedisKey(String domainName, String loginType) {
        StringBuffer sb = new StringBuffer();
        sb.append(loginType).append(":").append(domainName);
        return sb.toString();
    }

    /**
     * 保存至redis,每个只存储5分钟
     *
     * @param key
     * @param value
     */
    private void saveToRedis(String key, Object value) {
        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
        }
        redisTemplate.opsForValue().set(key, value, 5 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * groupList  去重
     * @param list 待去重集合
     */
    public List<String> removeDuplicate(List<String> list) {
        HashSet<String> hs = new HashSet<>(list);
        list.clear();
        list.addAll(hs);
        return list;
    }

    @Autowired
    public void addchecks(List<IMWUserListener> checklisteners) {
        addCheckLists(checklisteners);
    }

    @Autowired
    public void addPostProcessors(List<IMWUserPostProcesser> postlisteners) {
        addPostProcessorList(postlisteners);
    }

    /**
     * 同步用户
     *
     * @param redisKey redis缓存key
     */
    private void syncADUser(String redisKey) {
        try {
            //获取所有的配置数据
            List<MWADInfoDTO> allConfigList = mwadUserDao.selectConfig(new QueryADInfoParam());
            //获取LDAP的用户认证信息
            MwLdapAuthenticInfoDTO authInfo = mwadUserDao.selectSyAdInfo();
            AddADUserParam param = new AddADUserParam();
            param.setAdServerIpAdd(authInfo.getIpAddress());
            param.setAdPort(authInfo.getPort());
            param.setAdAdminAccount(authInfo.getAdAccount());
            param.setAdAdminPasswd(authInfo.getAdPasswd());
            for (MWADInfoDTO config : allConfigList) {
                try {
                    //获取当前映射配置下的所有用户信息
                    param.setType(config.getAdType());
                    param.setAdType(config.getAdType());
                    param.setSearch(Arrays.asList(config.getSearchNodes()));
                    List<ADUserDetailDTO> userList = getLDAPUsers(param);
                    param.setUserList(userList);
                    param.setRoleId(config.getRoleId());
                    if (StringUtils.isNotEmpty(config.getOrgId())) {
                        String[] idArray = config.getOrgId().split(",");
                        List<Integer> orgIdList = new ArrayList<>();
                        for (String id : idArray) {
                            if (NumberUtils.isNumber(id)) {
                                orgIdList.add(Integer.parseInt(id));
                            }
                        }
                        param.setOrgIdList(orgIdList);
                    } else {
                        param.setOrgIdList(new ArrayList<>());
                    }
                    if (NumberUtils.isNumber(config.getGroupId())) {
                        param.setUserGroup(Arrays.asList(Integer.parseInt(config.getGroupId())));
                    }
                    param.setConfigId(config.getId());
                    //同步当前用户
                    syncAddADUser(param);
                } catch (Exception e) {
                    log.error("同步AD用户失败", e);
                }
            }
            //更新离职用户
            syncLeaveAdUser();
        } catch (Exception e) {
            log.error("同步AD用户失败", e);
        } finally {
            if (StringUtils.isNotEmpty(redisKey)) {
                //删除redis
                redisTemplate.delete(redisKey);
            }
        }
    }

    /**
     * 同步离职用户信息
     */
    private void syncLeaveAdUser() {
        //非ORACLE环境不做处理
        if (!(Constants.DATABASE_ORACLE.equalsIgnoreCase(check))) {
            return;
        }
        if (StringUtils.isEmpty(LDAP_SYNC_LEAVE_URL)) {
            return;
        }
        try {
            //获取LDAP的用户认证信息
            MwLdapAuthenticInfoDTO authInfo = mwadUserDao.selectSyAdInfo();
            AddADUserParam param = new AddADUserParam();
            param.setAdServerIpAdd(authInfo.getIpAddress());
            param.setAdPort(authInfo.getPort());
            param.setAdAdminAccount(authInfo.getAdAccount());
            param.setAdAdminPasswd(authInfo.getAdPasswd());
            String domainName = getDomainName(param.getAdAdminAccount());
            param.setType(AdType.OU.name());
            param.setAdType(AdType.OU.name());
            param.setSearch(Arrays.asList(LDAP_SYNC_LEAVE_URL));
            //获取已经离职的用户列表
            List<ADUserDetailDTO> userList = getLDAPUsers(param);
            //离职用户数据
            Set<Integer> leaveUserSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(userList)) {
                log.info("同步离职用户信息,size is {}", userList.size());
                Set<String> loginNameSet = new HashSet<>();
                for (ADUserDetailDTO user : userList) {
                    loginNameSet.add(user.getLoginName() + "@" + domainName);
                }
                //获取当前所有的AD用户列表，确认需要更新的用户数据
                QueryUserParam queryParam = new QueryUserParam();
                queryParam.setUserType(UserType.LDAP.getType());
//                queryParam.setUserState(UserActiveState.ACTIVE.getName());
                Map criteria = PropertyUtils.describe(queryParam);
                List<MwUserDTO> users = mwuserDao.selectList(criteria);
                for (MwUserDTO adUser : users) {
                    if (loginNameSet.contains(adUser.getLoginName())) {
                        leaveUserSet.add(adUser.getUserId());
                    }
                }
                log.info("同步离职用户信息,size is {}", leaveUserSet.size());
                //用户同步离职状态，同时进行资产转移
                syncUserStateAndChangePerm(leaveUserSet);
            }
        } catch (Exception e) {
            log.error("同步离职用户信息失败", e);
        }
    }

    /**
     * 用户同步离职状态，同时进行资产转移
     * @param leaveUserSet 离职用户ID集合
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void syncUserStateAndChangePerm(Set<Integer> leaveUserSet) {
        if (CollectionUtils.isEmpty(leaveUserSet)){
            return;
        }
        UpdateUserStateParam updateStateUser = new UpdateUserStateParam();
        for (Integer userId : leaveUserSet) {
            log.info("syncUserStateAndChangePerm userid is "+ userId);
            //更新用户状态
            updateStateUser.setId(userId);
            updateStateUser.setEnable(UserActiveState.DISACTIVE.getName());
            mwUserDao.updateUserState(updateStateUser);
            //AD用户资产转移
            changeAdUserPermission(userId, DataType.INSTANCE_MANAGE);
        }
    }

    /**
     * 转移AD用户的资产数据
     * @param userId 用户ID
     * @param dataType 数据类型
     */
    private void changeAdUserPermission(Integer userId, DataType dataType) {
        String managerContent = mwadUserDao.getUserManager(userId);
        if (StringUtils.isEmpty(managerContent)) {
            return;
        }
        try {
            //获取AD用户信息
            MwLdapAuthenticInfoDTO authInfo = mwadUserDao.selectSyAdInfo();
            AddADUserParam param = new AddADUserParam();
            param.setAdServerIpAdd(authInfo.getIpAddress());
            param.setAdPort(authInfo.getPort());
            param.setAdAdminAccount(authInfo.getAdAccount());
            param.setAdAdminPasswd(authInfo.getAdPasswd());
            //获取当前映射配置下的所有用户信息
            param.setType(AdType.OU.name());
            param.setAdType(AdType.OU.name());
            param.setSearch(Arrays.asList(managerContent));
            String domainName = getDomainName(param.getAdAdminAccount());
            List<ADUserDetailDTO> userList = getLDAPUsers(param);
            String loginName;
            if (CollectionUtils.isEmpty(userList)) {
                //如果查询LDAP失败，则在猫维查询。使用正则表达式来提取CN值
                Pattern pattern = Pattern.compile("CN=([^,]+)");
                Matcher matcher = pattern.matcher(managerContent);
                if (matcher.find()) {
                    loginName = matcher.group(1);
                } else {
                    log.error("获取用户名称失败 user Id :" + userId);
                    return;
                }
            } else {
                //现支持单个经理转移
                ADUserDetailDTO managerInfo = userList.get(0);
                loginName = managerInfo.getLoginName();
            }
            //获取经理用户信息
            UserDTO managerUser = mwUserDao.selectADUserByType(loginName + "@" + domainName, UserType.LDAP.getType());
            if (managerUser == null) {
                log.error("用户经理数据获取失败 userId is {} , manager info is {}", userId, managerContent);
                return;
            }
            //资产转移
            ChangeUserParam changeUserParam = new ChangeUserParam();
            changeUserParam.setDataType(dataType.getName());
            changeUserParam.setUserId(userId);
            changeUserParam.setChangedUserId(managerUser.getUserId());
            userService.changeUserAuth(changeUserParam);
        } catch (Exception e) {
            log.error("转移AD用户的资产数据失败", e);
        }
    }

    /**
     * 同步添加AD用户
     *
     * @param param 用户参数
     */
    private void syncAddADUser(AddADUserParam param) {
        List<ADUserDetailDTO> userList = param.getUserList();
        AtomicReference<Integer> adUserCount = new AtomicReference<>(0);
        String domainName = getDomainName(param.getAdAdminAccount());
        MWDomainInfoDTO ma = CopyUtils.copy(MWDomainInfoDTO.class, param);
        String userType = AdType.AD.name();
        List<String> groupList = new ArrayList<>();
        List<ADUserDetailDTO> newUserList = new ArrayList<>();
        log.info("AD OU|GROUP Info ---------:" + param.getSearch());
        Boolean userState;
        String creator = param.getAdAdminAccount();
        Calendar calendar = new GregorianCalendar(2099, 12, 31);
        for (ADUserDetailDTO user : userList) {
            userState = StringUtils.isEmpty(user.getEnabled()) || Boolean.parseBoolean(user.getEnabled());
            UserActiveState userActiveState = userState ? UserActiveState.ACTIVE : UserActiveState.DISACTIVE;
            MWUser mwUser = MWUser.builder()
                    .phoneNumber(user.getPhone())
                    .email(user.getMail())
                    .defaultPasswdPlan(1)
                    .activePasswdPlan(1)
                    .userName(user.getUserName().replaceAll(" ", ""))
                    .loginName(user.getLoginName() + "@" + domainName)
                    .userType(AdType.AD.name())
                    .creator(creator)
                    .adUserGroupName(user.getGroupName())
                    .userExpiryDate(calendar.getTime())
                    .modifier(creator)
                    .wechatId(user.getWxNo())
                    .ddId(user.getDingdingNo())
                    .userState(userActiveState.getName())
                    .build();
            // 若已经存在用户信息，则不做任何变更
            UserDTO u = mwUserDao.selectADUserByType(mwUser.getLoginName(), userType);
            if (u != null) {
                //先更新用户信息
                MWUser updateUser = new MWUser();
                updateUser.setUserId(u.getUserId());
                if (StringUtils.isNotEmpty(user.getWxNo())) {
                    updateUser.setWechatId(user.getWxNo());
                }
                if (StringUtils.isNotEmpty(user.getMail())) {
                    updateUser.setEmail(user.getMail());
                }
                if (StringUtils.isNotEmpty(user.getPhone())) {
                    updateUser.setPhoneNumber(user.getPhone());
                }
                if (StringUtils.isNotEmpty(user.getDingdingNo())) {
                    updateUser.setDdId(user.getDingdingNo());
                }
                updateUser.setUserName(user.getUserName() == null ? u.getUserName() : user.getUserName());
                updateUser.setModifier(creator);
                mwUserDao.update(updateUser);
                //更新用户状态
                if (!userActiveState.getName().equalsIgnoreCase(u.getUserState()) && UserActiveState.DISACTIVE == userActiveState) {
                    UpdateUserStateParam updateStateUser = new UpdateUserStateParam();
                    updateStateUser.setId(u.getUserId());
                    updateStateUser.setEnable(UserActiveState.DISACTIVE.getName());
                    mwUserDao.updateUserState(updateStateUser);
                }
                //更新用户经理信息（华星项目）
                updateUserManager(u.getUserId(), user.getManager());
                continue;
            } else {
                newUserList.add(user);
            }
            // 新增AD用户信息
            mwadUserDao.insertUser(mwUser);

            adUserCount.getAndSet(adUserCount.get() + 1);
            UserDTO userDTO = mwUserDao.selectADUserByType(mwUser.getLoginName(), userType);
            //新增用户用户id存储到临时表中
            MwTempUserDTO userTemp = MwTempUserDTO.builder()
                    .userId(userDTO.getUserId()).creator(creator).build();
            mwadUserDao.insertTempUser(userTemp);

            mwUserRoleMapperDao.insertUserRoleMapper(MwUserRoleMap
                    .builder()
                    .userId(userDTO.getUserId())
                    .roleId(Integer.valueOf(param.getRoleId()))
                    .build()
            );
            //生成初始化的表头和查询头信息
            List<Reply> faillist = null;
            try {
                List<Integer> userId = new ArrayList<>();
                userId.add(userDTO.getUserId());
                faillist = publishPostEvent(new CustomColLoadEvent(userId));
            } catch (Throwable throwable) {
                log.error("用户生成自定义栏目失败,user 为 :" + JSON.toJSONString(userDTO), throwable);
            }
            if (faillist != null && faillist.size() > 0) {
                throw new ServiceException(faillist);
            }

            List<Integer> userIds = new ArrayList<>();
            userIds.add(userDTO.getUserId());
            //绑定用户和用户组
            if (param.getUserGroup() != null && param.getUserGroup().size() > 0) {
                param.getUserGroup().forEach(
                        g -> {
                            Reply reply = mwGroupService.selectById(g);
                            MwGroupDTO mgd = (MwGroupDTO) reply.getData();
                            ma.setGroupInfo(mgd.getGroupName());
                        }
                );
                ma.setUserGroup(param.getUserGroup().stream().map(String::valueOf).collect(Collectors.joining(",")));
                mwGroupService.bindUserGroup(BindUserGroupParam.builder().flag(1)
                        .groupIds(param.getUserGroup()).userIds(userIds).build());
            }
            //绑定用户和机构
            List<Integer> orgIdList = new ArrayList<>(param.getOrgIdList());
            mwOrgService.bindUserOrg(BindUserOrgParam.builder().flag(1).orgIds(orgIdList).userIds(userIds).build());
            groupList.add(user.getGroupName());
            updateUserManager(userDTO.getUserId(),user.getManager());
        }

        List<String> groups = removeDuplicate(groupList);
        log.info("remove duplicate gorup success ------");

        groups.forEach(
                group -> {
                    //映射配置
                    ma.setAdInfo(group);
                    log.info("insert config info  ----->" + ma);
                    ma.setId(param.getConfigId());
                    AtomicReference<Integer> id = new AtomicReference<>(ma.getId());
                    List<ADUserDetailDTO> users = newUserList.stream().filter(
                            u -> u.getGroupName().equals(group)).collect(Collectors.toList()
                    );
                    users.forEach(
                            us -> {
                                us.setLoginName(us.getLoginName() + "@" + domainName);
                                us.setId(id.get());
                                //将原有的用户数据删除，再重新添加
                                mwadUserDao.deleteByLoginName(Arrays.asList(us.getLoginName()));
                                mwadUserDao.insertConfigUser(us);
                                log.info("insert config user success ---");
                            }
                    );
                    id.getAndSet(id.get() + 1);
                    ma.setId(id.get());
                }
        );
    }

    /**
     * 更新用户的经理信息
     *
     * @param userId  用户ID
     * @param manager LDAP的经理信息
     */
    private void updateUserManager(Integer userId, String manager) {
        //非ORACLE环境不做处理
        if (!(Constants.DATABASE_ORACLE.equalsIgnoreCase(check))) {
            return;
        }
        if (StringUtils.isEmpty(manager)) {
            mwadUserDao.deleteUserManager(userId);
        } else {
            mwadUserDao.deleteUserManager(userId);
            mwadUserDao.insertUserManager(userId, manager);
        }
    }
}
