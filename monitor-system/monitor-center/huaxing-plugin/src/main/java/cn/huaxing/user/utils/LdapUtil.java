package cn.huaxing.user.utils;

import cn.huaxing.user.entity.AdDepartment;
import cn.huaxing.user.entity.AdServerInfo;
import cn.huaxing.user.entity.AdUserSyncParam;
import cn.huaxing.user.entity.LdapUserInfo;
import cn.huaxing.user.enums.AdType;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.ReflectUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author guiquanwnag
 * @datetime 2023/8/24
 * @Description LDAP工具类
 */
@UtilityClass
@Slf4j
public class LdapUtil {

    private static List<String> attrList;

    static {
        attrList = new ArrayList<>();
        attrList.add("canonicalName");
        attrList.add("distinguishedName");
        attrList.add("id");
        attrList.add("name");
        attrList.add("userPrincipalName");
        attrList.add("departmentNumber");
        attrList.add("telephoneNumber");
        attrList.add("homePhone");
        attrList.add("mobile");
        attrList.add("department");
        attrList.add("sAMAccountName");
        attrList.add("whenChanged");
        attrList.add("surName");
        attrList.add("enabled");
        attrList.add("givenName");
        attrList.add("objectGUID");
    }

    /**
     * 每页最大获取数量
     */
    private final static int MAX_PAGE_SIZE = 1000;

    private final static int TIMEOUT = 6000;

    /**
     * 获取部门列表
     *
     * @param ctx
     * @param adType
     * @param base
     * @return
     * @throws NamingException
     * @throws IOException
     */
    public Set<AdDepartment> getAdDepartment(LdapContext ctx, String adType, String base) throws NamingException, IOException {
        Set<AdDepartment> set = new HashSet<>();
        //用于判断是否还有剩余数据（进行分页）
        byte[] cookie = null;
        //过滤器 组织单元 组
        String searchFilter = "";
        if (AdType.OU.name().equals(adType)) {
            searchFilter = "(objectCategory=organizationalUnit)";
        } else {
            searchFilter = "(objectCategory=group)";
        }
        //设置分页
        ctx.setRequestControls(new Control[]{new PagedResultsControl(MAX_PAGE_SIZE, Control.CRITICAL)});
        do {
            NamingEnumeration<SearchResult> answer = getSearchResult(ctx, searchFilter, base, null);
            while (answer.hasMoreElements()) {
                SearchResult sr = answer.next();
                AdDepartment adDepartment = new AdDepartment();
                String name = getAttrValue(sr, "name");
                if ("Domain Controllers".equals(name)) {
                    continue;
                }
                adDepartment.setName(getAttrValue(sr, "name"));
                adDepartment.setcName(getAttrValue(sr, "canonicalName"));
                adDepartment.setDistinguishedName(getAttrValue(sr, "distinguishedName"));
                set.add(adDepartment);
            }
            Control[] controls = ctx.getResponseControls();
            if (controls != null) {
                for (int i = 0; i < controls.length; i++) {
                    if (controls[i] instanceof PagedResultsResponseControl) {
                        PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                        cookie = prrc.getCookie();
                    }
                }
            }
            ctx.setRequestControls(new Control[]{new PagedResultsControl(MAX_PAGE_SIZE, cookie, Control.CRITICAL)});
        } while (cookie != null);
        log.info(" ldap get " + adType + " num ----> " + set.size());
        return set;
    }

    /**
     * 获取AD域用户数据信息
     *
     * @param ctx
     * @param search    搜索路径
     * @param syncParam
     * @return
     * @throws NamingException
     */
    public List<LdapUserInfo> getUsers(LdapContext ctx, String search, AdUserSyncParam syncParam) throws NamingException, IOException {
        //userList
        List<LdapUserInfo> list = new ArrayList<LdapUserInfo>();
        //LDAP搜索过滤器类
        String searchFilter;
        //用于判断是否还有剩余数据（进行分页）
        byte[] cookie = null;
        Map<String, String> map = getSyncParamList(syncParam);
        searchFilter = "(&(objectCategory=person)(objectclass=user))";
        //设置分页
        ctx.setRequestControls(new Control[]{new PagedResultsControl(MAX_PAGE_SIZE, Control.CRITICAL)});
        do {
            //AD域节点结构
            NamingEnumeration<SearchResult> answer = getSearchResult(ctx, searchFilter, search, Lists.newArrayList(map.values()));
            while (answer.hasMoreElements()) {
                SearchResult sr = answer.next();
                String name = getAttrValue(sr, "name");
                if ("Domain Controllers".equals(name)) {
                    continue;
                }
                LdapUserInfo user = new LdapUserInfo();
                user.setUserName(getAttrValue(sr, "canonicalName"));
                //同步自定义数据
                for (String key : map.keySet()) {
                    String value = map.get(key);
                    String attrValue = getAttrValue(sr, value);
                    try {
                        ReflectUtils.setFieldValue(user, key, attrValue);
                    } catch (Exception e) {
                        log.error("同步自定义数据，设置值失败", e);
                    }
                }
                list.add(user);
            }
            Control[] controls = ctx.getResponseControls();
            if (controls != null) {
                for (int i = 0; i < controls.length; i++) {
                    if (controls[i] instanceof PagedResultsResponseControl) {
                        PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                        cookie = prrc.getCookie();
                    }
                }
            }
            ctx.setRequestControls(new Control[]{new PagedResultsControl(MAX_PAGE_SIZE, cookie, Control.CRITICAL)});
        } while (cookie != null);
        log.info(" ldap get user num ----> " + list.size());
        return list;
    }


    /**
     * 连接ad域
     *
     * @author zhaoy
     */
    public static Hashtable<String, String> getEnv(AdServerInfo server) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        //username
        String adminName = server.getAdAccount();
        //password
        String adminPassword = server.getAdPasswd();
        //ip
        String host = server.getIpAddress();
        //port，一般默认389
        String port = server.getPort();

        String ldapUrl = new String("ldap://" + host + ":" + port);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        //LDAP访问安全级别："none","simple","strong"
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        // AD User
        env.put(Context.SECURITY_PRINCIPAL, adminName);
        // AD Password
        env.put(Context.SECURITY_CREDENTIALS, adminPassword);
        // LDAP工厂类
        env.put(Context.PROVIDER_URL, ldapUrl);
        //连接超时设置为6秒
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(TIMEOUT));
        return env;
    }

    /**
     * @param ctx
     * @param searchFilter
     * @param searchBase
     * @param syncParamList 自定义同步数据
     * @return
     * @throws NamingException
     */
    public NamingEnumeration<SearchResult> getSearchResult(LdapContext ctx,
                                                           String searchFilter,
                                                           String searchBase,
                                                           List<String> syncParamList) throws NamingException {
        //搜索控制器
        SearchControls searchCols = new SearchControls();
        //创建搜索控制器
        searchCols.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // 定制返回属性
        if (CollectionUtils.isNotEmpty(syncParamList)) {
            //重复属性不再添加
            for (String param : syncParamList) {
                if (!attrList.contains(param)) {
                    attrList.add(param);
                }
            }
        }
        searchCols.setReturningAttributes(listToArray(attrList));
        return ctx.search(searchBase, searchFilter, searchCols);
    }

    public LdapContext getContext(Hashtable<String, String> hashtable) throws NamingException {
        return new InitialLdapContext(hashtable, null);
    }

    /**
     * 获取字段属性
     *
     * @param sr
     * @param attr
     * @return
     * @throws NamingException
     */
    private static String getAttrValue(SearchResult sr, String attr) throws NamingException {
        Attributes attrs = sr.getAttributes();
        if (attrs.get(attr) == null) {
            return null;
        }
        return attrs.get(attr).getAll().next().toString();
    }

    private static String[] listToArray(List<String> list) {
        String[] arr = new String[list.size()];
        String str;
        for (int i = 0; i < list.size(); i++) {
            str = list.get(i);
            arr[i] = str;
        }
        return arr;
    }

    public static AdDepartment getTreeAdDepartment(Set<AdDepartment> treeSet, String domainName) {
        AdDepartment root = new AdDepartment();
        root.setName(domainName);
        root.setcName(domainName);
        for (AdDepartment ad : treeSet) {
            AdDepartment parentAdDepartment = null;
            if ((parentAdDepartment = root.getParentAdDepartmentBycName(ad.getcName())) != null) {
                parentAdDepartment.addChildren(ad);
            } else {
                root.addChildren(ad);
            }
        }
        return root;
    }

    /**
     * 获取自定义同步数据列表
     *
     * @param syncParam 同步数据
     * @return
     */
    private static Map<String, String> getSyncParamList(AdUserSyncParam syncParam) {
        Map<String, String> map = new HashMap<>();
        try {
            Class clazz = syncParam.getClass();
            Field[] fields = clazz.getDeclaredFields();
            Method method;
            String value;
            for (Field field : fields) {
                method = clazz.getMethod(getGetMethodName(field));
                value = String.valueOf(method.invoke(syncParam));
                if (StringUtils.isNotEmpty(value)) {
                    map.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            log.error("获取自定义同步数据列表fail", e);
        }
        return map;
    }

    /**
     * 获取属性的get方法
     *
     * @param field
     * @return
     */
    private static String getGetMethodName(Field field) {
        char[] nameArr = field.getName().toCharArray();
        char first = nameArr[0];
        if (first >= 97 && first <= 122) {
            first ^= 32;
        }
        nameArr[0] = first;
        return "get" + String.valueOf(nameArr);
    }


}
