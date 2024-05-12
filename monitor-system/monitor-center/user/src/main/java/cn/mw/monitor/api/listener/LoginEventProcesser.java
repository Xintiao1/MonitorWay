package cn.mw.monitor.api.listener;

import cn.mw.monitor.api.exception.LoginNameException;
import cn.mw.monitor.api.exception.UnknownUserException;
import cn.mw.monitor.api.exception.UserControlException;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.event.*;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.service.user.exception.UserException;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.IUserControllerLogin;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.service.user.model.MwRoleModulePermMapper;
import cn.mw.monitor.state.PasswdState;
import cn.mw.monitor.state.UserActiveState;
import cn.mw.monitor.state.UserExpireState;
import cn.mw.monitor.user.advcontrol.RequestInfo;
import cn.mw.monitor.user.dao.MWADUserDao;
import cn.mw.monitor.user.dao.MWPasswdHisDao;
import cn.mw.monitor.user.dao.MwRoleDao;
import cn.mw.monitor.user.dao.MwRoleModulePermMapperDao;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.model.ADConfigDTO;
import cn.mw.monitor.user.model.MwRole;
import cn.mw.monitor.user.service.*;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class LoginEventProcesser implements ApplicationRunner, IUserControllerLogin
        , IMWUserUnlock, ILoginCacheInfo, IMWUserPostProcesser {

    private static final Logger logger = LoggerFactory.getLogger("component" + LoginEventProcesser.class.getName());

    private static final String redisGroup = LoginEventProcesser.class.getSimpleName();

    private ThreadLocal<LoginContext> userContext = new ThreadLocal();

    private static ThreadLocal<MwLoginUserDto> timerTaskUser = new ThreadLocal();

    private static final String CURRENT_LOGIN_NAME_KEY = "CURRENT_LOGIN_NAME_KEY";

    private static final String roleInfoKey = "roleInfo";

    private static final String permInfoKey = "permInfo";

    private static final String tokenNameKey = redisGroup+":tokenNameMap";

    private static final String BROWSE_PERM = "browse";

    private static final String shiroKey = "shiro:*";

    private static final String logoUrl="logoUrl";

    private static final String logoBaseCode="logoUrl";

    @Value("${scheduling.enabled}")
    private boolean isTimer;

    @Autowired
    MWGroupService mwGroupService;

    @Resource
    private MWADUserDao mwadUserDao;

    @Resource
    MWPasswdHisDao mwpasswdHisDao;

    @Autowired
    private MWOrgService mwOrgService;

    @Autowired
    MWPasswordPlanService mwpasswordPlanService;

    @Autowired
    MWUserService mwuserService;

    @Autowired
    private IUserPermission userPermission;

    @Autowired
    MwRoleService mwRoleService;

    @Autowired
    private RedisTemplate<String, Object> redisObjectTemplate;

    @Autowired
    IUserControlService userControlService;

    @Resource
    MwRoleModulePermMapperDao mwRoleModulePermMapperDao;

    @Resource
    MwRoleDao mwRoleDao;

    @Autowired
    MwUserSessionService userSessionService;
    private Map<String ,Subject> shiroSubjectMap = new HashMap<String, Subject>();

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {

        //用户登录前事件处理
        if(event instanceof LoginBeforeEvent) {
            LoginBeforeEvent loginBeforeEvent = (LoginBeforeEvent) event;
            List<Reply> faillist = processLoginBeforeEvent(loginBeforeEvent);
            return faillist;
        }

        //用户登录后事件处理
        if(event instanceof LoginAfterEvent){
            LoginAfterEvent loginAfterEvent = (LoginAfterEvent) event;
            processLoginAfterEvent(loginAfterEvent);
        }

        //用户登出事件处理
        if(event instanceof LogoutEvent){
            LogoutEvent logoutEvent = (LogoutEvent) event;
            processLogoutEvent(logoutEvent);
        }

        //用户解锁事件处理
        if(event instanceof UnlockEvent){
            UnlockEvent unlockEvent = (UnlockEvent) event;
            processUnlockEvent(unlockEvent);
        }

        //用户状态更新事件处理
        if(event instanceof PostUpdUserEvent){
            PostUpdUserEvent postUpdUserEvent = (PostUpdUserEvent) event;
            processPostUpdUserEvent(postUpdUserEvent);
        }

        //用户登陆失败事件处理
        if(event instanceof LoginFailEvent){
            LoginFailEvent loginFailEvent = (LoginFailEvent) event;
            processLoginFailEvent(loginFailEvent);
        }

        //刷新权限事件处理
        if(event instanceof RefreshPermEvent){
            RefreshPermEvent refreshPermEvent = (RefreshPermEvent) event;
            processRefreshPermEvent(refreshPermEvent);
        }

        return null;
    }

    private List<Reply> processLoginBeforeEvent(LoginBeforeEvent event) throws Throwable {
        List<Reply> faillist = new ArrayList<Reply>();
        // 获取用户名
        String loginName = event.getLoginParam().getLoginName();
        //查询用户信息
        Reply reply = null;
        if (!event.getLoginParam().getLdapRight()) {
            reply = mwuserService.selectByLoginName(loginName);
        }else {
            ADConfigDTO ad = mwadUserDao.select();
            loginName = loginName+"@"+ad.getDomainName();
            reply = mwuserService.selectAdUser(loginName);
        }
        log.info("zy-- full login name :"+loginName);
        // 获取缓存信息的key
        String key = genKey(loginName);
        // 第一次登录为null
        LoginContext loginContext = (LoginContext)redisObjectTemplate.opsForValue().get(key);

        if (reply.getData() == null){
            throw new UnknownUserException();
        }
        UserDTO user = (UserDTO)reply.getData();
        //判断用户的角色是否允许登录，如果不允许，则抛出未知用户异常
        MwRole role = mwRoleDao.selectByRoleId(user.getRoleId());
        if (role == null || role.getAllowLogin() == 0) {
            throw new UnknownUserException();
        }
        if (null == loginContext) {
            //
            if(StringUtils.isEmpty(loginName)){
                throw new LoginNameException();
            }

            //查询机构信息
            List<MWOrgDTO> orgList = mwOrgService.selectByLoginName(loginName);

            //查询系统设置信息
            Reply settingsInfo=mwuserService.selectSettingsInfo();
            SettingCaptchaDTO sc=(SettingCaptchaDTO)settingsInfo.getData();
            SettingDTO sinfo = sc.getSettingDTO();
            //查询密码策略信息
            reply = mwpasswordPlanService.selectActiveByLoginName(loginName);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                faillist.add(Reply.fail(ErrorConstant.USER_MSG_100115));
                throw new ServiceException(faillist);
            }
            MWPasswordPlanDTO mwpasswordPlanDTO = (MWPasswordPlanDTO) reply.getData();

            //创建登录并设置上下文
            //保存密码策略到线程上线文,用于shiro检查
            loginContext = CopyUtils.copy(LoginContext.class,mwpasswordPlanDTO);
            loginContext.setDbpasswd(user.getPassword());
            loginContext.setMwpasswordPlanDTO(mwpasswordPlanDTO);

            //重复登录次数初始化
            AtomicInteger retryCount = new AtomicInteger(0);
            loginContext.setRetryCount(retryCount);

            //初始化用户数据相关信息
            loginContext.setUserId(user.getUserId());
            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            Date logindate = dateFormat1.parse("1970-01-01");
            loginContext.setLoginTime(logindate);
            loginContext.setLoginState(user.getLoginState());
            loginContext.setUserExpiryDate(user.getUserExpiryDate());
            loginContext.setPasswdExpiryDate(user.getPasswdExpiryDate());
            loginContext.setLoginName(user.getLoginName());
            loginContext.setUserControlEnable(user.getUserControlEnable());

            //用户状态
            String userstate = user.getUserState();
            loginContext.setUserState(UserActiveState.valueOf(userstate));

            //用户过期状态
            String expireState = user.getUserExpireState();
            if (expireState != null) {
                loginContext.setUserExpireState(UserExpireState.valueOf(expireState));
            }

            //密码状态
            String passwdstate = user.getPasswdState();
            if(passwdstate != null) {
                loginContext.setPasswdState(PasswdState.valueOf(passwdstate));
            }

            LoginDTO loginDTO = new LoginDTO();
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setUser(user);
            loginInfo.setOrgs(orgList);
            if (sinfo != null) {
                sinfo.setUserImg(user.getUserImg());
            }
            loginInfo.setSettings(sinfo);
            loginContext.setLoginDTO(loginDTO);
            loginContext.setLoginInfo(loginInfo);

            //初始化权限信息
            initUserPermission(loginContext,user.getUserId(), loginName);
        }

        if (!event.getLoginParam().getLdapRight()) {
            //登录校验
            try {
                List<Reply> ret = loginCheck(loginContext);
                if(null != ret && ret.size() > 0){
                    faillist.addAll(ret);
                }
                saveToRedis(key, loginContext);
            }catch (Exception e){
                log.error("zy--  failed to login check : ",e);
                throw e;
            }
        }else {
            try {
                Method process = UserActiveState.class.getDeclaredMethod("process", LoginContext.class);
                process.invoke(loginContext.getUserState(),loginContext);
            }catch (Exception e) {
                if (e instanceof  InvocationTargetException) {
                    log.error(loginName+"--has been disabled !!! ",e);
                    throw ((InvocationTargetException)e).getTargetException();
                }
                throw e;
            }
            saveToRedis(key, loginContext);
        }

        //保存登录上下文到本地线程变量
        userContext.set(loginContext);

        return faillist;
    }

    private void processRefreshPermEvent(RefreshPermEvent refreshPermEvent){
        //检查缓存重的权限信息
        String loginName = refreshPermEvent.getLoginName();
        String permKey = genPermKey(loginName);
        log.info("recheck permission key:" + permKey);
        if(!keyIsExists(permKey)){
            //初始化权限信息
            LoginContext loginContext = refreshPermEvent.getLoginContext();
            initUserPermission(loginContext,refreshPermEvent.getUserId(), loginName);
            log.info("reset permission key:" + permKey);
        }
    }

    private Set<String> initUserPermission(LoginContext loginContext, Integer userId, String loginName){
        //查询用户权限信息
        List<MwRoleModulePermMapper> rmpMapperList = mwRoleModulePermMapperDao.selectByUserId(userId);
        Map<String, MwRoleModulePermMapper> rmpMap = new HashMap<>();
        Set<String> moduleIds = new HashSet<>();
        if(null != rmpMapperList){
            for(MwRoleModulePermMapper rmpMapper:rmpMapperList){
                if(rmpMapper.getEnable()){
                    rmpMap.put(rmpMapper.getId(), rmpMapper);
                    // 拥有浏览权限的页面才可以用来展示数据
                    if(BROWSE_PERM.equals(rmpMapper.getPermName())){
                        moduleIds.add(rmpMapper.getModuleId().toString());
                    }
                }
            }
        }

        //缓存权限信息
        String permKey = genPermKey(loginName);
        loginContext.setMwRoleModulePermMapper(rmpMap);
        LoginInfo loginInfo = loginContext.getLoginInfo();
        loginInfo.setModuleIds(moduleIds);
        savePermsToRedis(permKey, rmpMap);
        return moduleIds;
    }

    private void processLoginAfterEvent(LoginAfterEvent event) throws Exception{

        LoginContext loginContext = userContext.get();
        LoginInfo loginInfo = loginContext.getLoginInfo();
        UserDTO userDTO = loginInfo.getUser();
        String loginname = userDTO.getLoginName();
        String key = genKey(loginname);

        //查询用户组信息
        Reply reply = mwGroupService.selectByLoginName(loginname);
        if(null != reply && reply.getRes() == PaasConstant.RES_SUCCESS){
            List<MwGroupDTO> list = (List<MwGroupDTO> )reply.getData();
            List<Integer> groupsIds = new ArrayList<Integer>();
            list.forEach(value ->{
                groupsIds.add(value.getGroupId());
            });
            userDTO.setUserGroup(groupsIds);
        }

        //缓存登录上下文
        saveTokenToRedis(loginContext.getLoginDTO().getToken(),loginname);
        saveToRedis(key, loginContext);
        //登录成功后,清空线程信息
        userContext.remove();

        //查询用户角色信息
        MwRoleDTO mwRoleDTO = mwRoleService.selectByUserId(userDTO.getUserId());
        //保存用户信息到session
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setAttribute(CURRENT_LOGIN_NAME_KEY, loginname);
        session.setAttribute(roleInfoKey, mwRoleDTO);

        //保存当前用户访问subject
        shiroSubjectMap.put(loginname, currentUser);
    }

    private void processLogoutEvent(LogoutEvent logoutEvent){
        String loginName = logoutEvent.getLoginName();

        cleanUserRedisInfo(loginName);
    }

    private void processLoginFailEvent(LoginFailEvent loginFailEvent){
        String loginName = loginFailEvent.getLoginName();

        cleanUserRedisInfo(loginName);
    }

    private void processUnlockEvent(UnlockEvent unlockEvent){
        //获取当前用户session信息
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        String currentLoginName = (String)session.getAttribute(CURRENT_LOGIN_NAME_KEY);

        //获取更新目标
        String loginName = unlockEvent.getLoginName();
        String key = genKey(loginName);

        UpdPermitEvent updPermitEvent = new UpdPermitEvent(currentLoginName, loginName);
        //判断处理权限
        if(userPermission.ispermitted(updPermitEvent)){
            cleanUserInfo(loginName);
        }
    }

    private void processPostUpdUserEvent(PostUpdUserEvent postUpdUserEvent) throws Throwable{
        //获取更新目标
        UserDTO userDTO = postUpdUserEvent.getOldUserdto();
        String loginName = userDTO.getLoginName();

        // 如果更新目标未登陆则不进行后面的操作
        if(null == redisObjectTemplate.opsForValue().get(genKey(loginName))){
            return;
        }

        //获取当前用户session信息
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        String currentLoginName = (String)session.getAttribute(CURRENT_LOGIN_NAME_KEY);

        //如果更新目标和当前用户不是同一个用户 返回
        if(!currentLoginName.equals(loginName)){
            return;
        }

        UpdPermitEvent updPermitEvent = new UpdPermitEvent(currentLoginName, loginName);
        //判断处理权限
        //如果是管理员修改用户，需要用户重新登陆
        if(userPermission.ispermitted(updPermitEvent)){
            cleanUserInfo(loginName);
        }

        // 判断修改内容
        //属于自己修改自己
        syncRedisFromDB(loginName);
    }

    //判断key存在
    private boolean keyIsExists(String key) {
        return redisObjectTemplate.hasKey(key);
    }

    private void cleanUserInfo(String loginName){

        cleanUserRedisInfo(loginName);
        Subject destSubject = shiroSubjectMap.get(loginName);
        if(null != destSubject){
            shiroSubjectMap.remove(loginName);
            destSubject.logout();
        }
    }

    private void cleanUserRedisInfo(String loginName){
        LoginContext ret = (LoginContext)redisObjectTemplate.opsForValue().get(genKey(loginName));
        String token = ret.getLoginDTO().getToken();
        redisObjectTemplate.delete(genKey(token));

        String key = genKey(loginName);
        redisObjectTemplate.delete(key);

        String permKey = genPermKey(loginName);
        redisObjectTemplate.delete(permKey);
    }

    private String genKey(String loginName){
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":").append(loginName);
        return sb.toString();
    }

    private String genPermKey(String loginName){
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":").append(loginName).append(permInfoKey);
        return sb.toString();
    }

    @Override
    public LoginContext getCacheInfo(String loginName){
        String key = genKey(loginName);
        LoginContext ret = (LoginContext)redisObjectTemplate.opsForValue().get(key);
        return ret;
    }

    @Override
    public LoginContext getThreadLocalInfo() {
        return userContext.get();
    }

    @Override
    public MwRoleDTO getRoleInfo() {
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        MwRoleDTO roleDTO = (MwRoleDTO)session.getAttribute(roleInfoKey);
        return roleDTO;
    }

    @Override
    public String getLoginName() {
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        String loginName = (String)session.getAttribute(CURRENT_LOGIN_NAME_KEY);
        return loginName;
    }

    @Override
    public String getRoleId(String loginName) {
        String key = genKey(loginName);
        LoginContext ret = (LoginContext)redisObjectTemplate.opsForValue().get(key);
        String roleId = ret.getLoginInfo().getUser().getRoleId().toString();
        return roleId;
    }

    @Override
    public MwRoleModulePermMapper getMwRoleModulePermMapper(String key, String loginName) {
        String redisKey = genPermKey(loginName);
        MwRoleModulePermMapper mwRoleModulePermMapper = (MwRoleModulePermMapper)redisObjectTemplate.opsForHash().get(redisKey, key);
        return mwRoleModulePermMapper;
    }

    @Override
    public String getNameTokenMap(String token){
        return (String) redisObjectTemplate.opsForHash().get(genKey(token),token);
    }

    private void saveToRedis(String key, LoginContext loginContext){
        //清除密码
        LoginInfo loginInfo = loginContext.getLoginInfo();
        UserDTO userDTO = loginInfo.getUser();
        userDTO.setPassword("***********");
        redisObjectTemplate.opsForValue().set(key, loginContext);
    }

    private void savePermsToRedis(String key, Map<String, MwRoleModulePermMapper> rmpMap){
        redisObjectTemplate.opsForHash().putAll(key, rmpMap);
    }

    private void saveTokenToRedis(String token, String name){
        Map<String, String> tokenNameMap = new HashMap<>();
        tokenNameMap.put(token,name);
        redisObjectTemplate.opsForHash().putAll(genKey(token),tokenNameMap);
    }

    private List<Reply> loginCheck(LoginContext loginContext) throws Throwable {
        Class[] param  = {LoginContext.class};
        List<Reply> faillist = new ArrayList<Reply>();
        //检查用户状态
        UserActiveState.DISACTIVE.process(loginContext);

        //检查并设置用户过期状态
        for (UserExpireState state : UserExpireState.values()){
            String methodname = "checkAndSet" + state.getName();
            Method method = UserExpireState.class.getDeclaredMethod(methodname,param);
            method.invoke(state, loginContext);
        }

        //检查并设置密码状态
        for (PasswdState state : PasswdState.values()){
            String methodname = "checkAndSet" + state.getName();
            Method method = PasswdState.class.getDeclaredMethod(methodname,param);
            method.invoke(state, loginContext);
        }

        try {
            //根据用户过期状态进行处理
            for (UserExpireState state : UserExpireState.values()) {
                String methodname = "process" + state.getName();
                Method method = UserExpireState.class.getDeclaredMethod(methodname, param);
                method.invoke(state, loginContext);
            }

            //根据密码状态进行处理
            for (PasswdState state : PasswdState.values()) {
                String methodname = "process" + state.getName();
                Method method = PasswdState.class.getDeclaredMethod(methodname, param);
                List<Reply> retlist = (List<Reply>)method.invoke(state, loginContext);
                if(null != retlist && retlist.size() > 0){
                    faillist.addAll(retlist);
                }
            }

            //判断用户访问控制状态
            boolean isUserControlEnable = loginContext.getUserControlEnable();
            if(isUserControlEnable){
                Subject currentUser = SecurityUtils.getSubject();
                Session session = currentUser.getSession();
                RequestInfo requestInfo = (RequestInfo)session.getAttribute(IUserControlService.REQINFO);
                requestInfo.setLoginName(loginContext.getLoginName());
                boolean isperm = userControlService.check(requestInfo);
                if(!isperm){
                    throw new UserControlException();
                }
            }

        }catch (UserException e) {
            log.info("zy--  login check error messages :",e);
            throw e;
        }catch (InvocationTargetException e){
            log.info("zy--  login check error messages :",e);
            throw e.getTargetException();
        }catch (Exception e){
            log.info("zy--  login check error messages :",e);
            throw e;
        }finally{
            UserExpireState.resetState(loginContext);
        }

        loginContext.setLoginTime(new Date());

        return faillist;
    }

    private void syncRedisFromDB(String loginName) throws Throwable{
        //查询用户信息
        Reply reply = mwuserService.selectByLoginName(loginName);
        if(!reply.getRes().equals(PaasConstant.RES_SUCCESS)){
            throw new UnknownUserException();
        }
        UserDTO user = (UserDTO)reply.getData();
        //查询用户组信息
        reply = mwGroupService.selectByLoginName(loginName);
        if(null != reply && reply.getRes() == PaasConstant.RES_SUCCESS){
            List<MwGroupDTO> list = (List<MwGroupDTO> )reply.getData();
            List<Integer> groupsIds = new ArrayList<Integer>();
            list.forEach(value -> groupsIds.add(value.getGroupId()));
            user.setUserGroup(groupsIds);
        }
        //获取缓存中的登陆上下文
        LoginContext oldLoginContext = getCacheInfo(loginName);
        //重载密码
        oldLoginContext.setDbpasswd(user.getPassword());
        //重载用户基本信息
        oldLoginContext.getLoginInfo().setUser(user);

        reply = mwpasswordPlanService.selectActiveByLoginName(loginName);
        oldLoginContext.setMwpasswordPlanDTO((MWPasswordPlanDTO)reply.getData());

        //保存登陆信息
        oldLoginContext.setLoginDTO(oldLoginContext.getLoginDTO());
        String key = genKey(loginName);

        saveToRedis(key, oldLoginContext);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(isTimer){
            return;
        }
        //应用启动清理缓存
        StringBuffer info = new StringBuffer();
        info.append("redis-").append(redisGroup).append(" clean");
        Date starttime = new Date();
        logger.info(info.toString() + " start");
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":*");
        Set<String> keys = redisObjectTemplate.keys(sb.toString());
        keys.addAll(redisObjectTemplate.keys(shiroKey));
        redisObjectTemplate.delete(keys);

        Date endtime = new Date();
        Long diff = (endtime.getTime() - starttime.getTime()) / 1000;
        logger.info(info.toString() + " end, used time:" + diff.longValue());
    }

    private ThreadLocal<MwLoginUserDto> threadLocal = new ThreadLocal();

    @Override
    public void createLocalTread(MwLoginUserDto userDto) {
        threadLocal.set(userDto);
    }

    @Override
    public MwLoginUserDto getLocalTread() {
        return threadLocal.get();
    }

    @Override
    public void removeLocalTread() {
        logger.info("开始清除本地线程");
        threadLocal.remove();
        logger.info("清除本地线程成功");
    }

    @Override
    public void createTimeTaskUser(MwLoginUserDto mwLoginUserDto) {
        timerTaskUser.set(mwLoginUserDto);
    }

    @Override
    public MwLoginUserDto getTimeTaskUser() {
        return timerTaskUser.get();
    }

    @Override
    public void removeTimeTaskUser() {
        timerTaskUser.remove();
    }

    @Override
    public List<MWOrgDTO> getLoginOrg() {
        List<MWOrgDTO> orgList = mwOrgService.selectByLoginName(getLoginName());
        return orgList;
    }
}
