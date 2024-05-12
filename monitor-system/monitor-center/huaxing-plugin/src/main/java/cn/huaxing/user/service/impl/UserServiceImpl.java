package cn.huaxing.user.service.impl;

import cn.huaxing.user.dao.UserMapper;
import cn.huaxing.user.entity.*;
import cn.huaxing.user.enums.AdType;
import cn.huaxing.user.service.UserService;
import cn.huaxing.user.utils.LdapUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guiquanwnag
 * @datetime 2023/8/24
 * @Description 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {

    @Value("${user.ldap.sync.param}")
    private String LDAP_SYNC_PARAM;

    @Autowired
    private UserMapper userMapper;

//    private final String IGNORE_DEPT = "inactive";
    private final String IGNORE_DEPT = "OU=inactive,OU=CSOT,DC=csot,DC=TCL,DC=com";


    private final int ORACLE_BATCH_MAX_SIZE = 100;

    private final int MAX = 500;

    private AdUserSyncParam syncParam;

    @Override
    @Transactional
    public void saveAndUpdateUser() {
        try {
            //先获取AD域数据
            AdServerInfo serverInfo = userMapper.selectAdServerInfo();
            if (serverInfo == null) {
                log.error("serverInfo is null");
                return;
            }
            if (syncParam == null) {
                log.error("syncParam is null");
                syncParam = generateSyncParam(LDAP_SYNC_PARAM);
            }
            //根据AD域信息，获取LDAP的组织信息
            AdDepartment dept = getServerDept(serverInfo);
            if (dept == null) {
                log.error("dept is null");
            }
            //由于华星项目的inactive目录就处于二级目录，所以遍历children即可
            List<String> weChatIdList;
            List<UserInfo> existedUsers;
            List<UserInfo> waitInsertUsers = new ArrayList<>();
            List<UserInfo> waitDeleteUsers = new ArrayList<>();
            List<UserInfo> waitUpdateUsers = new ArrayList<>();
            List<UserInfo> filterList;
            for (AdDepartment child : dept.getChildren()) {
                //inactive在第三层，数据将在这个阶段处理
                if (CollectionUtils.isNotEmpty(child.getChildren())){
                    for (AdDepartment department : child.getChildren()) {
                        if (IGNORE_DEPT.equals(department.getDistinguishedName())) {
                            continue;
                        }
                        //非inactive的组织，将分批次获取用户信息，并且增量更新
                        List<UserInfo> users = getLDAPUsers(serverInfo, department.getDistinguishedName(), syncParam);
                        log.info("user size is ", users.size());
                        if (CollectionUtils.isEmpty(users)) {
                            continue;
                        }
                        List<List<UserInfo>> userLists = Lists.partition(users, MAX);
                        for (List<UserInfo> userList : userLists) {
                            //先排除无企业微信号数据用户
                            filterList = userList.stream().filter(obj -> StringUtils.isNotEmpty(obj.getWeChatId())).collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(filterList)) {
                                continue;
                            }
                            //根据企业微信ID判断用户是否已经插入，如果已经插入。则需要判断是否需要增量更新
                            weChatIdList = filterList.stream().map(UserInfo::getWeChatId).collect(Collectors.toList());
                            QueryWrapper wrapper = new QueryWrapper();
                            wrapper.in("WECHAT_ID", weChatIdList);
                            wrapper.eq("DELETE_FLAG", false);
                            existedUsers = this.list(wrapper);
                            if (CollectionUtils.isEmpty(existedUsers)) {
                                waitInsertUsers.addAll(filterList);
                            } else {
                                Map<String, UserInfo> map1 = existedUsers.stream()
                                        .collect(Collectors.toMap(UserInfo::getWeChatId, obj -> obj));

                                Map<String, UserInfo> map2 = filterList.stream()
                                        .collect(Collectors.toMap(UserInfo::getWeChatId, obj -> obj));

                                // 获取相同项
                                List<UserInfo> existedcommonItems = existedUsers.stream()
                                        .filter(obj -> map2.containsKey(obj.getWeChatId()))
                                        .collect(Collectors.toList());
                                List<UserInfo> commonItems = filterList.stream()
                                        .filter(obj -> map1.containsKey(obj.getWeChatId()))
                                        .collect(Collectors.toList());
                                waitUpdateUsers.addAll(compareDiffUsers(existedcommonItems, commonItems));

                                //获取待删除的用户信息
                                List<UserInfo> differentItems = existedUsers.stream()
                                        .filter(obj -> !map2.containsKey(obj.getWeChatId()))
                                        .collect(Collectors.toList());
                                waitDeleteUsers.addAll(differentItems);
                                //获取未插入的数据
                                List<UserInfo> unExistedUsers = filterList.stream()
                                        .filter(obj -> !map1.containsKey(obj.getWeChatId()))
                                        .collect(Collectors.toList());
                                waitInsertUsers.addAll(unExistedUsers);
                            }
                        }
                    }
                }
            }
            //根据待删除和待插入以及带比较的数据，进行批量处理
            if (CollectionUtils.isNotEmpty(waitUpdateUsers)) {
                this.updateBatchById(waitUpdateUsers, ORACLE_BATCH_MAX_SIZE);
            }
            for (UserInfo user : waitDeleteUsers) {
                user.setDeleteFlag(true);
                user.setUpdateTime(new Date());
            }
            if (CollectionUtils.isNotEmpty(waitDeleteUsers)) {
                this.updateBatchById(waitDeleteUsers, ORACLE_BATCH_MAX_SIZE);
            }
            int maxId = getMaxId();
            for (UserInfo user : waitInsertUsers) {
                user.setId(++maxId);
                user.setUpdateTime(new Date());
                user.setDeleteFlag(false);
                user.setCreateTime(new Date());
            }
            if (CollectionUtils.isNotEmpty(waitInsertUsers)) {
                this.saveBatch(waitInsertUsers, ORACLE_BATCH_MAX_SIZE);
            }
        } catch (Exception e) {
            log.error("同步用户信息失败", e);
        }
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
            } else {
                sync = JSON.parseObject(param, AdUserSyncParam.class);
            }
            return sync;
        } catch (Exception e) {
            log.error("构建AD用户同步数据fail", e);
            return null;
        }
    }

    private int getMaxId() {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.select(" max(USER_ID) as maxId ");
        UserInfo userInfo = this.getOne(wrapper);
        return userInfo == null ? 0 : userInfo.getId();
    }

    private List<UserInfo> compareDiffUsers(List<UserInfo> existedItems, List<UserInfo> commonItems) {
        List<UserInfo> userList = new ArrayList<>();
        Map<String, UserInfo> existedMap = existedItems.stream().collect(Collectors.toMap(UserInfo::getWeChatId, obj -> obj));
        Map<String, UserInfo> userMap = commonItems.stream().collect(Collectors.toMap(UserInfo::getWeChatId, obj -> obj));
        UserInfo existedUser;
        UserInfo userInfo;
        for (String key : existedMap.keySet()) {
            existedUser = existedMap.get(key);
            userInfo = userMap.get(key);
            if (existedUser == null || userInfo == null) {
                continue;
            }
            //比较数据是否一致
            if (!existedUser.equals(userInfo)) {
                existedUser.setUserName(userInfo.getUserName());
                existedUser.setEmail(userInfo.getEmail());
                existedUser.setLoginName(userInfo.getLoginName());
                existedUser.setPhoneNumber(userInfo.getPhoneNumber());
                existedUser.setUpdateTime(new Date());
                existedUser.setWeChatId(userInfo.getWeChatId());
                userList.add(existedUser);
            }
        }
        return userList;
    }

    private AdDepartment getServerDept(AdServerInfo serverInfo) {
        String adType = AdType.OU.name();
        String domainName = serverInfo.getAdAccount().split("@")[1];
        String base = searchBase(serverInfo.getAdAccount());
        Set<AdDepartment> as = new TreeSet<>();
        LdapContext ctx = null;
        AdDepartment ad = null;
        try {
            ctx = getContext(serverInfo);
            as = new TreeSet<>();
            Set<AdDepartment> a = LdapUtil.getAdDepartment(ctx, adType, base);
            as.addAll(a);
            ad = LdapUtil.getTreeAdDepartment(as, domainName);
            return ad;
        } catch (Exception e) {
            log.error("get ad department fail ", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("--------------ad域链接 LdapContext未正常关闭!", e);
                }
            }
        }
        return null;
    }

    /**
     * 获取AD用户数据集合
     *
     * @param serverInfo server服务器信息
     * @param search     检索信息
     * @param syncParam
     * @return
     * @throws NamingException
     */
    private List<UserInfo> getLDAPUsers(AdServerInfo serverInfo, String search, AdUserSyncParam syncParam) throws NamingException {
        List<UserInfo> userList = new ArrayList<>();
        List<LdapUserInfo> users = new ArrayList<>();
        LdapContext finalCtx = getContext(serverInfo);
        try {
            users = LdapUtil.getUsers(finalCtx, search, syncParam);
        } catch (Exception e) {
            log.info("get ad users  failed :", e);
        }
        if (users != null) {
            users.forEach(
                    u -> {
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUserName(StringUtils.isNotEmpty(u.getUserName()) ? u.getUserName() : "");
                        userInfo.setLoginName(u.getLoginName());
                        userInfo.setEmail(u.getMail());
                        userInfo.setPhoneNumber(u.getPhone());
                        userInfo.setWeChatId(u.getWxNo());
                        userList.add(userInfo);
                    }
            );
        }
        log.info("ad user list count is " + userList.size());
        return userList;
    }

    private LdapContext getContext(AdServerInfo serverInfo) throws NamingException {
        Hashtable<String, String> env = LdapUtil.getEnv(serverInfo);
        return LdapUtil.getContext(env);
    }

    /**
     * 根据域名 解析节点
     *
     * @param adName
     * @return
     */
    private String searchBase(String adName) {
        return getString(adName);
    }

    private String getString(String adName) {
        StringBuilder searchBase = new StringBuilder();
        String[] split = adName.split("@");
        String domain = split[1];
        String[] dc = domain.split("\\.");
        for (String s : dc) {
            searchBase.append("DC=").append(s).append(",");
        }
        return searchBase.substring(0, searchBase.length() - 1);
    }
}
