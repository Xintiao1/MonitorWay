package cn.mw.monitor.common.constant;

/**
 * Created by yeshengqi on 2019/4/26.
 * <p>
 * 错误消息常量类
 * <p>
 * 常量规则
 * 10  01  01
 * 模  子  错
 * 块  模  误
 * 块  编
 * 号
 * 如 100101 表示系统管理下用户管理新增用户时用户已存在错误
 * <p>
 * 模块划分 系统管理      10
 * 首页         11
 * 监控大屏     12
 */
public class ErrorConstant {
    /**
     * 通用信息
     **/
    public static final int COMMON_200001 = 200001;
    public static final String COMMON_MSG_200001 = "#{1}不正确";

    public static final int COMMON_200002 = 200002;
    public static final String COMMON_MSG_200002 = "#{1}已存在";

    public static final int COMMON_200003 = 200003;
    public static final String COMMON_MSG_200003 = "参数错误";

    public static final int COMMON_200004 = 200004;
    public static final String COMMON_MSG_200004 = "执行异常";

    public static final int COMMON_200005 = 200005;
    public static final String COMMON_MSG_200005 = "新增失败";

    public static final int COMMON_200006 = 200006;
    public static final String COMMON_MSG_200006 = "更新失败";

    public static final int COMMON_200007 = 200007;
    public static final String COMMON_MSG_200007 = "删除失败";

    public static final int COMMON_200008 = 200008;
    public static final String COMMON_MSG_200008 = "查询失败";

    /**
     * 用户相关
     **/
    public static final int USER_100101 = 100101;
    public static final String USER_MSG_100101 = "该用户已存在！";

    public static final int USER_100102 = 100102;
    public static final String USER_MSG_100102 = "新增用户失败！";

    public static final int USER_100103 = 100103;
    public static final String USER_MSG_100103 = "修改用户失败！";

    public static final int USER_100104 = 100104;
    public static final String USER_MSG_100104 = "删除用户失败！";

    public static final int USER_100105 = 100105;
    public static final String USER_MSG_100105 = "根据用户名查询用户信息失败！";

    public static final int USER_100106 = 100106;
    public static final String USER_MSG_100106 = "分页查询用户信息失败！";

    public static final int USER_100107 = 100107;
    public static final String USER_MSG_100107 = "根据ID查询用户信息失败！";

    public static final int USER_100108 = 100108;
    public static final String USER_MSG_100108 = "根据用户ID查询用户角色失败！";

    public static final int USER_100109 = 100109;
    public static final String USER_MSG_100109 = "修改用户个人信息失败！";

    public static final int USER_100110 = 100110;
    public static final String USER_MSG_100110 = "密码长度低于";

    public static final int USER_100111 = 100111;
    public static final String USER_MSG_100111 = "Salt,散列类型和密码需一起修改";

    public static final int USER_100112 = 100112;
    public static final String USER_MSG_100112 = "#{1}不能为空";

    public static final int USER_100113 = 100113;
    public static final String USER_MSG_100113 = "新增密码策略失败";

    public static final int USER_100114 = 100114;
    public static final String USER_MSG_100114 = "修改密码策略失败";

    public static final int USER_100115 = 100115;
    public static final String USER_MSG_100115 = "查询密码策略失败";

    public static final int USER_100116 = 100116;
    public static final String USER_MSG_100116 = "不能更新登录名";

    public static final int USER_100117 = 100117;
    public static final String USER_MSG_100117 = "不能修改#{1}";

    public static final int USER_100118 = 100118;
    public static final String USER_MSG_100118 = "密码应有#{1}种或#{1}种以上的字符组成！(包含大写字母、小写字母、数字、符号)";

    public static final int USER_100119 = 100119;
    public static final String USER_MSG_100119 = "#{1}以前使用过";

    public static final int USER_100120 = 100120;
    public static final String USER_MSG_100120 = "密码#{1}过期,请在安全设置修改密码!";

    public static final int USER_100121 = 100121;
    public static final String USER_MSG_100121 = "请#{1}后再尝试";

    public static final int USER_100122 = 100122;
    public static final String USER_MSG_100122 = "密码已过期";

    public static final int USER_100123 = 100123;
    public static final String USER_MSG_100123 = "请重新设置密码";

    public static final int USER_100124 = 100124;
    public static final String USER_MSG_100124 = "用户已锁定";

    public static final int USER_100125 = 100125;
    public static final String USER_MSG_100125 = "登录失败";

    public static final int USER_100126 = 100126;
    public static final String USER_MSG_100126 = "用户名或密码错误";

    public static final int USER_100127 = 100127;
    public static final String USER_MSG_100127 = "用户已被禁用";

    public static final int USER_100128 = 100128;
    public static final String USER_MSG_100128 = "解锁失败";

    public static final int USER_100129 = 100129;
    public static final String USER_MSG_100129 = "用户没有操作权限";

    public static final int USER_100130 = 100130;
    public static final String USER_MSG_100130 = "用户访问受限";

    public static final int USER_100131 = 100131;
    public static final String USER_MSG_100131 = "获取用户数据异常";

    public static final int USER_100132 = 100132;
    public static final String USER_MSG_100132 = "删除密码策略失败";

    public static final int USER_100133 = 100133;
    public static final String USER_MSG_100133 = "查询密码策略下拉框失败";

    public static final int USER_100134 = 100134;
    public static final String USER_MSG_100134 = "登陆超时，请重新登陆！";

    public static final int USER_100135 = 100135;
    public static final String USER_MSG_100135 = "您的账号在另一台设备登录。如果不是本人操作，请及时修改密码！";

    public static final int USER_100136 = 100136;
    public static final String USER_MSG_100136 = "如果需要修改自己的信息，请使用《个人设置》功能！";

    public static final int USER_100137 = 100137;
    public static final String USER_MSG_100137 = "原始密码输入有误，请输入正确的原始密码！";

    public static final int USER_100138 = 100138;
    public static final String USER_MSG_100138 = "登录名已存在，不可重复添加！";

    public static final int USER_100139 = 100139;
    public static final String USER_MSG_100139 = "该用户的关联机构已停用，禁止变更状态！";

    public static final int USER_100140 = 100140;
    public static final String USER_MSG_100140 = "重置密码失败！";

    public static final int USER_100141 = 100141;
    public static final String USER_MSG_100141 = "用户密码已过期,请联系系统管理员!";

    /**
     * 密码策略
     */
    public static final int PASSWDPLAN_100601 = 100601;
    public static final String PASSWDPLAN_MSG_100601 = "查询密码策略下拉框失败！";

    public static final int PASSWDPLAN_100602 = 100602;
    public static final String PASSWDPLAN_MSG_100602 = "新增密码策略失败!";

    public static final int PASSWDPLAN_100603 = 100603;
    public static final String PASSWDPLAN_MSG_100603 = "默认密码不能被删除！";

    public static final int PASSWDPLAN_100604 = 100604;
    public static final String PASSWDPLAN_MSG_100604 = "以下密码策略已关联用户，禁止删除：";

    public static final int PASSWDPLAN_100605 = 100605;
    public static final String PASSWDPLAN_MSG_100605 = "删除密码策略失败！";

    public static final int PASSWDPLAN_100606 = 100606;
    public static final String PASSWDPLAN_MSG_100606 = "默认密码策略不能被修改！";

    public static final int PASSWDPLAN_100607 = 100607;
    public static final String PASSWDPLAN_MSG_100607 = "修改密码策略状态失败！";

    public static final int PASSWDPLAN_100608 = 100608;
    public static final String PASSWDPLAN_MSG_100608 = "根据密码策略ID查询密码策略失败！";

    public static final int PASSWDPLAN_100609 = 100609;
    public static final String PASSWDPLAN_MSG_100609 = "分页查询密码策略信息失败！";

    public static final int PASSWDPLAN_100610 = 100610;
    public static final String PASSWDPLAN_MSG_100610 = "修改密码策略失败";

    public static final int PASSWDPLAN_100611 = 100611;
    public static final String PASSWDPLAN_MSG_100611 = "密码策略名已存在,新增失败！";

    /**
     * AD域
     */
    public static final int AD_100701 = 100701;
    public static final String AD_MSG_100701 = "验证失败！";

    public static final int AD_100702 = 100702;
    public static final String AD_MSG_100702 = "获取信息失败！";

    public static final int AD_100703 = 100703;
    public static final String AD_MSG_100703 = "AD域用户只可在个人设置修改信息";

    public static final int AD_100704 = 100704;
    public static final String AD_MSG_100704 = "AD域用户修改/重置密码功能未开放！";

    public static final int AD_100705 = 100705;
    public static final String AD_MSG_100705 = "查询AD配置失败！";

    public static final int AD_100706 = 100706;
    public static final String AD_MSG_100706 = "根据Id删除AD配置失败！";

    public static final int AD_100707 = 100707;
    public static final String AD_MSG_100707 = "帐号或密码为空或帐号格式不正确！";

    public static final int AD_100708 = 100708;
    public static final String AD_MSG_100708 = "AD信息存储失败！";

    public static final int AD_100709 = 100709;
    public static final String AD_MSG_100709 = "AD用户导入失败！";

    public static final int AD_100710 = 100710;
    public static final String AD_MSG_100710 = "LdapContext未正常关闭！";

    public static final int AD_100711 = 100711;
    public static final String AD_MSG_100711 = "映射配置已存在！";

    public static final int AD_100712 = 100712;
    public static final String AD_MSG_100712 = "删除AD配置失败，需要先删除用户数据！";
    /**
     * 菜单相关
     **/
    public static final int MENU_100201 = 100201;
    public static final String MENU_MSG_100201 = "新增菜单失败！";

    public static final int MENU_100202 = 100202;
    public static final String MENU_MSG_100202 = "删除菜单失败！";

    public static final int MENU_100203 = 100203;
    public static final String MENU_MSG_100203 = "修改菜单失败！";

    public static final int MENU_100204 = 100204;
    public static final String MENU_MSG_100204 = "根据菜单ID取菜单信息失败！";

    public static final int MENU_100205 = 100205;
    public static final String MENU_MSG_100205 = "分页查询菜单信息失败！";

    public static final int MENU_100206 = 100206;
    public static final String MENU_MSG_100206 = "查询菜单信息失败！";

    public static final int MENU_100207 = 100207;
    public static final String MENU_MSG_100207 = "根据有权限的菜单ID和父ID查询菜单信息失败！";

    public static final int USER_100208 = 100208;
    public static final String USER_MSG_100208 = "修改用户状态失败！";

    public static final int USER_100209 = 100209;
    public static final String USER_MSG_100209 = "默认密码策略不可以修改！";

    public static final int USER_100210 = 100210;
    public static final String USER_MSG_100210 = "用户登录控制时间格式输入错误！";

    public static final int USER_100211 = 100211;
    public static final String USER_MSG_100211 = "系统管理员无法删除/编辑";

    public static final int USER_100212 = 100212;
    public static final String USER_MSG_100212 = "系统管理员无法修改用户状态";

    public static final int USER_100213 = 100213;
    public static final String USER_MSG_100213 = "系统管理员无法修改角色";

    public static final int USER_100214 = 100214;
    public static final String USER_MSG_100214 = "系统管理员必须包含初始机构";


    /**
     * 角色相关
     **/
    public static final int ROLE_100301 = 100301;
    public static final String ROLE_MSG_100301 = "新增角色失败！";

    public static final int ROLE_100302 = 100302;
    public static final String ROLE_MSG_100302 = "删除角色失败！";

    public static final int ROLE_100303 = 100303;
    public static final String ROLE_MSG_100303 = "修改角色状态失败！";

    public static final int ROLE_100304 = 100304;
    public static final String ROLE_MSG_100304 = "根据角色ID取角色信息失败！";

    public static final int ROLE_100305 = 100305;
    public static final String ROLE_MSG_100305 = "分页查询角色信息失败！";

    public static final int ROLE_100307 = 100307;
    public static final String ROLE_MSG_100307 = "角色授权失败！";

    public static final int ROLE_100308 = 100308;
    public static final String ROLE_MSG_100308 = "根据角色ID查询角色菜单失败！";

    public static final int ROLE_100309 = 100309;
    public static final String ROLE_MSG_100309 = "根据角色ID查询角色菜单失败！";

    public static final int ROLE_100310 = 100310;
    public static final String ROLE_MSG_100310 = "查询角色列表信息失败！";

    public static final int ROLE_100311 = 100311;
    public static final String ROLE_MSG_100311 = "以下角色已绑定用户，无法删除：";

    public static final int ROLE_100312 = 100312;
    public static final String ROLE_MSG_100312 = "以下角色已绑定用户，无法改变状态：";

    public static final int ROLE_100313 = 100313;
    public static final String ROLE_MSG_100313 = "修改角色信息失败！";

    public static final int ROLE_100314 = 100314;
    public static final String ROLE_MSG_100314 = "角色名已存在,新增角色失败！";

    public static final int ROLE_100315 = 100315;
    public static final String ROLE_MSG_100315 = "当前角色已绑定用户，无法修改！";

    public static final int ROLE_100316 = 100316;
    public static final String ROLE_MSG_100316 = "以下角色已绑定外部认证，无法删除：";


    public static final int MODULE_100210 = 100210;
    public static final String MODULE_MSG_100210 = "重置模块权限信息失败！";

    public static final int MODULE_100211 = 100211;
    public static final String MODULE_MSG_100211 = "重置角色模块权限信息失败！";

    public static final int ROLE_100212 = 100212;
    public static final String ROLE_MSG_100212 = "查询角色模块权限信息失败！";

    public static final int MODULE_100213 = 100213;
    public static final String MODULE_MSG_100213 = "新增模块失败！";

    public static final int MODULE_100214 = 100214;
    public static final String MODULE_MSG_100214 = "以下该模块已有子模块，禁止删除：";

    public static final int MODULE_100215 = 100215;
    public static final String MODULE_MSG_100215 = "刪除模块失败！";

    public static final int MODULE_100216 = 100216;
    public static final String MODULE_MSG_100216 = "修改模块失败！";

    public static final int MODULE_100217 = 100217;
    public static final String MODULE_MSG_100217 = "查询模块失败！";

    public static final int MODULE_100218 = 100218;
    public static final String MODULE_MSG_100218 = "系统角色无法编辑/删除等操作！";

    /**
     * 机构相关
     **/
    public static final int ORG_100401 = 100401;
    public static final String ORG_MSG_100401 = "新增机构失败！";

    public static final int ORG_100402 = 100402;
    public static final String ORG_MSG_100402 = "删除机构失败！";

    public static final int ORG_100403 = 100403;
    public static final String ORG_MSG_100403 = "修改机构失败！";

    public static final int ORG_100404 = 100404;
    public static final String ORG_MSG_100404 = "根据机构ID取机构信息失败！";

    public static final int ORG_100405 = 100405;
    public static final String ORG_MSG_100405 = "分页查询机构信息失败！";

    public static final int ORG_100406 = 100406;
    public static final String ORG_MSG_100406 = "查询机构信息失败！";

    public static final int ORG_100407 = 100407;
    public static final String ORG_MSG_100407 = "以下该机构已关联用户，禁止删除：";

    public static final int ORG_100408 = 100408;
    public static final String ORG_MSG_100408 = "修改机构状态失败！";

    public static final int ORG_100409 = 100409;
    public static final String ORG_MSG_100409 = "机构绑定用户失败！";

    public static final int ORG_100410 = 100410;
    public static final String ORG_MSG_100410 = "以下该机构已关联资产，禁止删除：";

    public static final int ORG_100411 = 100411;
    public static final String ORG_MSG_100411 = "以下该机构已关联监控，禁止删除：";

    public static final int ORG_100412 = 100412;
    public static final String ORG_MSG_100412 = "以下该机构已关联引擎，禁止删除：";

    public static final int ORG_100413 = 100413;
    public static final String ORG_MSG_100413 = "以下该机构已有子机构，禁止删除：";

    public static final int ORG_100414 = 100414;
    public static final String ORG_MSG_100414 = "该机构的上级机构已停用，禁止变更状态！";

    public static final int ORG_100415 = 100415;
    public static final String ORG_MSG_100415 = "以下该机构已关联用户组，禁止删除：";

    public static final int ORG_100416 = 100416;
    public static final String ORG_MSG_100416 = "以下该机构已关联报表，禁止删除：";

    public static final int ORG_100417 = 100417;
    public static final String ORG_MSG_100417 = "以下该机构已关联密码策略，禁止删除：";

    public static final int ORG_100418 = 100418;
    public static final String ORG_MSG_100418 = "机构名已存在,新增机构失败";

    public static final int ORG_100419 = 100419;
    public static final String ORG_MSG_100419 = "系统机构无法删除";

    /**
     * 部门相关
     **/
    public static final int DEPT_100501 = 100501;
    public static final String DEPT_MSG_100501 = "新增部门失败！";

    public static final int DEPT_100502 = 100502;
    public static final String DEPT_MSG_100502 = "删除部门失败！";

    public static final int DEPT_100503 = 100503;
    public static final String DEPT_MSG_100503 = "修改部门失败！";

    public static final int DEPT_100504 = 100504;
    public static final String DEPT_MSG_100504 = "根据部门ID取部门信息失败！";

    public static final int DEPT_100505 = 100505;
    public static final String DEPT_MSG_100505 = "分页查询部门信息失败！";

    public static final int DEPT_100506 = 100506;
    public static final String DEPT_MSG_100506 = "查询部门列表信息失败！";


    /**
     * 分组相关
     **/
    public static final int GROUP_190101 = 190101;
    public static final String GROUP_MSG_190101 = "新增分组失败！";

    public static final int GROUP_190102 = 190102;
    public static final String GROUP_MSG_190102 = "删除分组失败！";

    public static final int GROUP_190103 = 190103;
    public static final String GROUP_MSG_190103 = "修改分组失败！";

    public static final int GROUP_190104 = 190104;
    public static final String GROUP_MSG_190104 = "查询分组列表失败！";

    public static final int GROUP_190105 = 190105;
    public static final String GROUP_MSG_190105 = "查询分组列表失败！";

    public static final int GROUP_190106 = 190106;
    public static final String GROUP_MSG_190106 = "修改分组状态失败！";

    /**
     * 知识库相关
     **/
    public static final int KNOWLEDGE_190105 = 190105;
    public static final String KNOWLEDGE_MSG_190105 = "新增失败！";

    public static final int KNOWLEDGE_190106 = 190106;
    public static final String KNOWLEDGE_MSG_190106 = "删除失败！";

    public static final int KNOWLEDGE_190107 = 190107;
    public static final String KNOWLEDGE_MSG_190107 = "修改失败！";

    public static final int KNOWLEDGE_190108 = 190108;
    public static final String KNOWLEDGE_MSG_190108 = "根据ID查询信息失败！";

    public static final int KNOWLEDGE_190109 = 190109;
    public static final String KNOWLEDGE_MSG_190109 = "分页查询失败！";

    public static final int KNOWLEDGE_190110 = 190110;
    public static final String KNOWLEDGE_MSG_190110 = "根据分组ID统计知识库数量失败！";

    /**
     * ip
     **/
    public static final int IPADDRESSCODE_200100 = 200100;
    public static final String IPADDRESS_MSG_200100 = "创建IP地址失败！";

    /**
     * 有形资产
     **/
    public static final int TANGASSETSCODE_210100 = 210100;
    public static final String TANGASSETS_MSG_210100 = "根据用户ID查询资产数据失败！";

    public static final int TANGASSETSCODE_210102 = 210102;
    public static final String TANGASSETS_MSG_210102 = "查询资产信息列表失败！";

    public static final int TANGASSETSCODE_210103 = 210103;
    public static final String TANGASSETS_MSG_210103 = "修改资产信息失败！";

    public static final int TANGASSETSCODE_210104 = 210104;
    public static final String TANGASSETS_MSG_210104 = "新增资产信息失败！";

    public static final int TANGASSETSCODE_210105 = 210105;
    public static final String TANGASSETS_MSG_210105 = "删除资产信息失败！";

    public static final int TANGASSETSCODE_210112 = 210112;
    public static final String TANGASSETS_MSG_210112 = "以下数据已存在：";

    public static final int TANGASSETSCODE_210118 = 210118;
    public static final String TANGASSETS_MSG_210118 = "修改资产状态信息失败！";

    public static final int TANGASSETSCODE_210119 = 210119;
    public static final String TANGASSETS_MSG_210119 = "查询模板宏值信息失败！";

    public static final int TANGASSETSCODE_210120 = 210120;
    public static final String TANGASSETS_MSG_210120 = "查询模板信息失败！";

    public static final int TANGASSETSCODE_210121 = 210121;
    public static final String TANGASSETS_MSG_210121 = "#{1}对应设备已存在";

    public static final int TANGASSETSCODE_210122 = 210122;
    public static final String TANGASSETS_MSG_210122 = "请从资源管理添加资产";

    //无形资产
    public static final int TANGASSETSCODE_210106 = 210106;
    public static final String TANGASSETS_MSG_210106 = "根据用户ID查询无形资产数据失败！";

    public static final int TANGASSETSCODE_210107 = 210107;
    public static final String TANGASSETS_MSG_210107 = "分页查询无形资产信息失败！";

    public static final int TANGASSETSCODE_210108 = 210108;
    public static final String TANGASSETS_MSG_210108 = "查询无形资产信息列表失败！";

    public static final int TANGASSETSCODE_210109 = 210109;
    public static final String TANGASSETS_MSG_210109 = "修改无形资产信息失败！";

    public static final int TANGASSETSCODE_210110 = 210110;
    public static final String TANGASSETS_MSG_210110 = "新增无形资产信息失败！";

    public static final int TANGASSETSCODE_210111 = 210111;
    public static final String TANGASSETS_MSG_210111 = "删除无形资产信息失败！";
    //带外资产
    public static final int OUTBAND_ASSETSCODE_210112 = 210112;
    public static final String OUTBAND_ASSETS_MSG_210112 = "根据资产ID查询资产数据失败！";

    public static final int OUTBAND_ASSETSCODE_210113 = 210113;
    public static final String OUTBAND_ASSETS_MSG_210113 = "分页查询资产信息失败！";

    public static final int OUTBAND_ASSETSCODE_210114 = 210114;
    public static final String OUTBAND_ASSETS_MSG_210114 = "查询资产信息列表失败！";

    public static final int OUTBAND_ASSETSCODE_210115 = 210115;
    public static final String OUTBAND_ASSETS_MSG_210115 = "修改资产信息失败！";

    public static final int OUTBAND_ASSETSCODE_210116 = 210116;
    public static final String OUTBAND_ASSETS_MSG_210116 = "新增资产信息失败！";

    public static final int OUTBAND_ASSETSCODE_210117 = 210117;
    public static final String OUTBAND_ASSETS_MSG_210117 = "删除资产信息失败！";

    /**
     * 扫描规则资产
     **/
    public static final int SCANRULECODE_220100 = 220100;
    public static final String SCANRULE_MSG_220100 = "根据用户ID查询扫描规则数据失败！";

    public static final int SCANRULECODE_220101 = 220101;
    public static final String SCANRULE_MSG_220101 = "分页查询扫描规则信息失败！";

    public static final int SCANRULECODE_220102 = 220102;
    public static final String SCANRULE_MSG_220102 = "查询扫描规则信息列表失败！";

    public static final int SCANRULECODE_220103 = 220103;
    public static final String SCANRULE_MSG_220103 = "修改扫描规则信息失败！";

    public static final int SCANRULECODE_220104 = 220104;
    public static final String SCANRULE_MSG_220104 = "新增扫描规则信息失败！";

    public static final int SCANRULECODE_220105 = 220105;
    public static final String SCANRULE_MSG_220105 = "删除扫描规则信息失败！";

    public static final int SCANRULECODE_220106 = 220106;
    public static final String SCANRULE_MSG_220106 = "有设备访问超时或登录异常！";

    public static final int SCANRULECODE_220107 = 220107;
    public static final String SCANRULE_MSG_220107 = "扫描失败！";

    public static final int SCANRULECODE_220108 = 220108;
    public static final String SCANRULE_MSG_220108 = "已经存在扫描线程！";

    /**
     * 标签管理
     **/
    public static final int LABELMANAGECODE_220100 = 220100;
    public static final String LABELMANAGE_MSG_220100 = "根据用户ID查询标签数据失败！";

    public static final int LABELMANAGECODE_220101 = 220101;
    public static final String LABELMANAGE_MSG_220101 = "分页查询标签失败！";

    public static final int LABELMANAGECODE_220102 = 220102;
    public static final String LABELMANAGE_MSG_220102 = "查询标签列表失败！";

    public static final int LABELMANAGECODE_220103 = 220103;
    public static final String LABELMANAGE_MSG_220103 = "修改标签失败！";

    public static final int LABELMANAGECODE_220104 = 220104;
    public static final String LABELMANAGE_MSG_220104 = "新增标签失败！";

    public static final int LABELMANAGECODE_220105 = 220105;
    public static final String LABELMANAGE_MSG_220105 = "删除标签信息失败！";

    public static final int LABELMANAGECODE_220106 = 220106;
    public static final String LABELMANAGE_MSG_220106 = "查询资产类型信息失败！";

    public static final int LABELMANAGECODE_220107 = 220107;
    public static final String LABELMANAGE_MSG_220107 = "以下标签已关联资产，禁止删除:";

    public static final int LABELMANAGECODE_220108 = 220108;
    public static final String LABELMANAGE_MSG_220108 = "修改标签状态失败！";

    public static final int LABELMANAGECODE_220109 = 220109;
    public static final String LABELMANAGE_MSG_220109 = "以下标签已启用删除限制，禁止删除：";

    public static final int LABELMANAGECODE_220110 = 220110;
    public static final String LABELMANAGE_MSG_220110 = "查询模块类型信息失败！";

    public static final int LABELMANAGECODE_220111 = 220111;
    public static final String LABELMANAGE_MSG_220111 = "以下标签开启删除限制，禁止删除:";

    /**
     * 下拉框管理
     **/
    public static final int DROPDOWNCODE_230101 = 230101;
    public static final String DROPDOWN_MSG_230101 = "下拉框信息查询失败！";

    public static final int DROPDOWNCODE_230102 = 230102;
    public static final String DROPDOWN_MSG_230102 = "下拉框信息新增失败！";

    public static final int DROPDOWNCODE_230103 = 230103;
    public static final String DROPDOWN_MSG_230103 = "下拉框信息删除失败！";
    /**
     * 引擎管理
     **/
    public static final int ENGINEMANAGECODE_240101 = 240101;
    public static final String ENGINEMANAGE_MSG_240101 = "根据ID查询引擎信息失败！";

    public static final int ENGINEMANAGECODE_240102 = 240102;
    public static final String ENGINEMANAGE_MSG_240102 = "查询引擎信息失败！";

    public static final int ENGINEMANAGECODE_240103 = 240103;
    public static final String ENGINEMANAGE_MSG_240103 = "修改引擎信息失败！";

    public static final int ENGINEMANAGECODE_240104 = 240104;
    public static final String ENGINEMANAGE_MSG_240104 = "新增引擎信息失败！";

    public static final int ENGINEMANAGECODE_240105 = 240105;
    public static final String ENGINEMANAGE_MSG_240105 = "删除引擎信息失败！";

    /**
     * 引擎管理
     **/
    public static final int USERGROUPCODE_250101 = 250101;
    public static final String USERGROUP_MSG_250101 = "根据ID查询用户组信息失败！";

    public static final int USERGROUPCODE_250102 = 250102;
    public static final String USERGROUP_MSG_250102 = "分页查询查询用户组信息失败！";

    public static final int USERGROUPCODE_250103 = 250103;
    public static final String USERGROUP_MSG_250103 = "修改用户组信息失败！";

    public static final int USERGROUPCODE_250104 = 250104;
    public static final String USERGROUP_MSG_250104 = "新增用户组信息失败！";

    public static final int USERGROUPCODE_250105 = 250105;
    public static final String USERGROUP_MSG_250105 = "删除用户组信息失败！";

    public static final int USERGROUPCODE_250106 = 250106;
    public static final String USERGROUP_MSG_250106 = "以下用户组已关联用户，禁止删除:";

    public static final int USERGROUPCODE_250107 = 250107;
    public static final String USERGROUP_MSG_250107 = "根据用户名查询用户组信息失败！";

    public static final int USERGROUPCODE_250108 = 250108;
    public static final String USERGROUP_MSG_250108 = "以下用户组已关联资产，禁止删除:";

    public static final int USERGROUPCODE_250109 = 250109;
    public static final String USERGROUP_MSG_250109 = "以下用户组已关联监控，禁止删除:";

    public static final int USERGROUPCODE_250110 = 250110;
    public static final String USERGROUP_MSG_250110 = "用户组绑定用户失败！";

    public static final int USERGROUPCODE_250111 = 250111;
    public static final String USERGROUP_MSG_250111 = "修改用户组状态失败！";

    public static final int USERGROUPCODE_250112 = 250112;
    public static final String USERGROUP_MSG_250112 = "查询用户组下拉框信息失败！";

    public static final int USERGROUPCODE_250113 = 250113;
    public static final String USERGROUP_MSG_250113 = "查询用户组关联用户信息失败！";

    public static final int USERGROUPCODE_250114 = 250114;
    public static final String USERGROUP_MSG_250114 = "用户组名称已使用，新增用户组失败！";

    /**
     * 自定义列信息
     **/
    public static final int CUSTOMCOLCODE_260101 = 260101;
    public static final String CUSTOMCOL_MSG_260101 = "根据用户ID查询列信息失败！";

    public static final int CUSTOMCOLCODE_260102 = 260102;
    public static final String CUSTOMCOL_MSG_260102 = "新增列信息失败！";

    public static final int CUSTOMCOLCODE_260103 = 260103;
    public static final String CUSTOMCOL_MSG_260103 = "修改列信息失败！";

    public static final int CUSTOMCOLCODE_260104 = 260104;
    public static final String CUSTOMCOL_MSG_260104 = "还原列信息失败！";

    /**
     * 子资产类型信息
     **/
    public static final int ASSETSSUBTEOYCODE_270101 = 270101;
    public static final String ASSETSSUBTEOY_MSG_270101 = "根据用户ID查询子资产类型失败！";

    public static final int ASSETSSUBTEOYCODE_270102 = 270102;
    public static final String ASSETSSUBTEOY_MSG_270102 = "增加子资产类型信息失败！";

    public static final int ASSETSSUBTEOYCODE_270103 = 270103;
    public static final String ASSETSSUBTEOY_MSG_270103 = "修改子资产类型信息失败！";

    public static final int ASSETSSUBTEOYCODE_270104 = 270104;
    public static final String ASSETSSUBTEOY_MSG_270104 = "查询子资产类型信息失败！";

    public static final int ASSETSSUBTEOYCODE_270105 = 270105;
    public static final String ASSETSSUBTEOY_MSG_270105 = "删除子资产类型信息失败！";

    public static final int ASSETSSUBTEOYCODE_270106 = 270106;
    public static final String ASSETSSUBTEOY_MSG_270106 = "批量更新类型资产GroupId出现错误！";

    /**
     * 资产模板
     **/
    public static final int ASSETSTEMPLATECODE_280101 = 280101;
    public static final String ASSETSTEMPLATE_MSG_280101 = "新增资产模板信息失败！";

    public static final int ASSETSTEMPLATECODE_280102 = 280102;
    public static final String ASSETSTEMPLATE_MSG_280102 = "修改资产模板信息失败！";

    public static final int ASSETSTEMPLATECODE_280103 = 280103;
    public static final String ASSETSTEMPLATE_MSG_280103 = "删除资产模板信息失败！";

    public static final int ASSETSTEMPLATECODE_280104 = 280104;
    public static final String ASSETSTEMPLATE_MSG_280104 = "批量更新资产模板信息失败！";

    public static final int ASSETSTEMPLATECODE_280105 = 280105;
    public static final String ASSETSTEMPLATE_MSG_280105 = "资产模板信息导出失败！";

    public static final int ASSETSTEMPLATECODE_280106 = 280106;
    public static final String ASSETSTEMPLATE_MSG_280106 = "资产模板信息导入失败！";

    public static final int ASSETSTEMPLATECODE_280107 = 280107;
    public static final String ASSETSTEMPLATE_MSG_280107 = "查询资产模板信息失败！";
    /**
     * 数据权限
     **/
    public static final int DATAPERMISSIONCODE_290001 = 290001;
    public static final String DATAPERMISSIONCODE_MSG_290001 = "用户#{1}的数据权限异常#{2}！";

    /**告警管理*/
    public static final int ALARM_NOW_CODE_300001 = 300001;
    public static final String ALARM_NOW_MSG_300001 = "获取当前告警失败！";
    public static final int ALARM_HIST_CODE_300002 = 300002;
    public static final String ALARM_HIST_MSG_300002 = "获取历史告警失败！";
    public static final int ALARM_SHOW_CODE_300003 = 300003;
    public static final String ALARM_SHOW_MSG_300003 = "告警显示详情失败！";
    public static final int ALARM_HANDLER_CODE_300004 = 300004;
    public static final String ALARM_HANDLER_MSG_300004 = "告警处理失败！";
    public static final int ALERT_RULE_SELECT_CODE_300005 = 300005;
    public static final String ALERT_RULE_SELECT_MAG_300005 = "告警规则查询失败！";
    public static final int ALERT_RULE_INSERT_CODE_300006 = 300006;
    public static final String ALERT_RULE_INSERT_MAG_300006 = "告警规则添加失败！";
    public static final int ALERT_RULE_UPDATE_CODE_300007 = 300007;
    public static final String ALERT_RULE_UPDATE_MAG_300007 = "告警规则修改失败！";
    public static final int ALERT_RULE_DELETE_CODE_300008 = 300008;
    public static final String ALERT_RULE_DELETE_MAG_300008 = "告警规则删除失败！";
    public static final int ALERT_ACTION_TYPE_CODE_300009 = 300009;
    public static final String ALERT_ACTION_TYPE_MAG_300009 = "告警通知方式查询失败！";
    public static final int ALERT_ACTION_INSERT_CODE_300010 = 300010;
    public static final String ALERT_ACTION_INSERT_MAG_300010 = "告警通知新增失败！";
    public static final int ALERT_ACTION_BROWSE_CODE_300011 = 300011;
    public static final String ALERT_ACTION_BROWSE_MAG_300011 = "告警通知查询失败！";
    public static final int ALERT_ACTION_EDITOR_CODE_300012 = 300012;
    public static final String ALERT_ACTION_EDITOR_MAG_300012 = "告警通知编辑失败！";
    public static final int ALERT_ACTION_DELETE_CODE_300013 = 300013;
    public static final String ALERT_ACTION_DELETE_MAG_300013 = "告警通知删除失败！";
    public static final int ALERT_ACTION_POPUP_BROWSE_CODE_300014 = 300014;
    public static final String ALERT_ACTION_POPUP_BROWSE_MAG_300014 = "告警编辑前通知查询失败！";
    public static final int ALERT_ACTION_RULE_BROWSE_CODE_300015 = 300015;
    public static final String ALERT_ACTION_RULE_BROWSE_MAG_300015 = "根据通知方式查询规则名称失败！";
    public static final int ALERT_ACTION_LUCENE_CODE_300016 = 300016;
    public static final String ALERT_ACTION_LUCENE_MAG_300016 = "告警标题查询知识库失败！";


    /**
     * web监测
     **/
    public static final int WEBMONITORCODE_301001 = 301001;
    public static final String WEBMONITOR_MSG_301001 = "查询web监测信息失败！";

    public static final int WEBMONITORCODE_301002 = 301002;
    public static final String WEBMONITOR_MSG_301002 = "新增web监测信息失败！";

    public static final int WEBMONITORCODE_301003 = 301003;
    public static final String WEBMONITOR_MSG_301003 = "更新web监测信息失败！";

    public static final int WEBMONITORCODE_301004 = 301004;
    public static final String WEBMONITOR_MSG_301004 = "删除web监测信息失败！";

    public static final int WEBMONITORCODE_301005 = 301005;
    public static final String WEBMONITOR_MSG_301005 = "修改web监测状态失败！";

    public static final int WEBMONITORCODE_301006 = 301006;
    public static final String WEBMONITOR_MSG_301006 = "查询web监测下载速度历史数据失败！";

    public static final int WEBMONITORCODE_301007 = 301007;
    public static final String WEBMONITOR_MSG_301007 = "查询web监测响应时间历史数据失败！";

    public static final int WEBMONITORCODE_301008 = 301008;
    public static final String WEBMONITOR_MSG_301008 = "查询web监测历史数据失败！";

    /*服务器监测*/
    public static final int SERVER_RANK_CODE_302001 = 302001;
    public static final String SERVER_RANK_MSG_302001 = "排行榜信息获取失败！";
    public static final int SERVER_HISTORY_CODE_302002 = 302002;
    public static final String SERVER_HISTORY_MSG_302002 = "服务器的历史数据获取失败！";
    public static final int SERVER_DISK_CODE_302003 = 302003;
    public static final String SERVER_DISK_MSG_302003 = "磁盘数据获取失败！";
    public static final int SERVER_NET_CODE_302004 = 302004;
    public static final String SERVER_NET_MSG_302004 = "网络接口数据获取失败！";
    public static final int SERVER_APPLICATION_CODE_302004 = 302004;
    public static final String SERVER_APPLICATION_MSG_302004 = "应用集数据获取失败！";
    public static final int SERVER_ALARM_CODE_302005 = 302005;
    public static final String SERVER_ALARM_MSG_302005 = "服务器告警信息获取失败！";
    public static final int SERVER_NAVIGATION_BAR_CODE_302006 = 302006;
    public static final String SERVER_NAVIGATION_BAR_MSG_302006 = "详情信息导航栏信息获取失败！";
    public static final int SERVER_RECORD_CODE_302007 = 302007;
    public static final String SERVER_RECORD_MSG_302007 = "资产详情档案获取失败！";
    public static final int SERVER_TABLEDETAIL_CODE_302008 = 302008;
    public static final String SERVER_TABLEDETAIL_MSG_302008 = "服务器标签获取失败！";
    public static final int HOST_MONITORINGITEM_CODE_302009 = 302009;
    public static final String HOST_MONITORINGITEM_MSG_302009 = "主机监控项获取失败！";
    public static final int HOST_MONITORINGITEMHISTORY_CODE_302010 = 302010;
    public static final String HOST_MONITORINGITEMHISTORY_MSG_302010 = "获取主机监控项历史数据失败！";
    public static final int SERVER_RUNSERVEROBJECT_CODE_302011 = 302011;
    public static final String SERVER_RUNSERVEROBJECT_MSG_302011 = "获取所有拥有相同IP的资产数据失败！";
    public static final int SERVER_DURATION_AND_STATUS_CODE_302012 = 302012;
    public static final String SERVER_DURATION_AND_STATUS_MSG_302012 = "查询持续运行时间和资产状态（正常或异常）失败！";
    public static final int SERVER_INNODB_INFO_CODE_302013 = 302013;
    public static final String SERVER_INNODB_INFO_MSG_302013 = "查询INNODB信息失败！";
    public static final int SERVER_INNODB_INFO_CODE_302014 = 302014;
    public static final String SERVER_INNODB_INFO_MSG_302014 = "查询每秒查询信息失败！";
    public static final int SERVER_TYPENAME_LIST_INFO_CODE_302015 = 302015;
    public static final String SERVER_TYPENAME_LIST_INFO_MSG_302015 = "查询分区名称下拉框数据失败！";
    public static final int SERVER_STORAGE_VOL_INFO_CODE_302016 = 302016;
    public static final String SERVER_STORAGE_VOL_INFO_MSG_302016 = "查询分区存储卷信息数据失败！";
    public static final int SERVER_ITEMNAMELIKES_INFO_CODE_302017 = 302017;
    public static final String SERVER_ITEMNAMELIKES_INFO_MSG_302017 = "查询存储监控项下拉信息数据失败！";
    public static final int SERVER_HARDWARE_INFO_CODE_302018 = 302018;
    public static final String SERVER_HARDWARE_INFO_MSG_302018 = "查询硬件信息数据失败！";
    public static final int MYMONITOR_SAVE_COMPONENT_INFO_CODE_302019 = 302019;
    public static final String MYMONITOR_SAVE_COMPONENT_INFO_MSG_302019 = "保存组件布局信息失败！";
    public static final int MYMONITOR_SELECT_LINECHART_INFO_CODE_302020 = 302020;
    public static final String MYMONITOR_SELECT_LINECHART_INFO_MSG_302020 = "查询折线图信息失败！";
    public static final int MYMONITOR_SELECT_ITEMS_INFO_CODE_302021 = 302021;
    public static final String MYMONITOR_SELECT_ITEMS_INFO_MSG_302021 = "查询监控项信息失败！";
    public static final int MYMONITOR_SELECT_COMPONENTS_INFO_CODE_302022 = 302022;
    public static final String MYMONITOR_SELECT_COMPONENTS_INFO_MSG_302022 = "查询组件信息失败！";
    public static final int MYMONITOR_SELECT_COMPONENTLAYOUT_INFO_CODE_302023 = 302023;
    public static final String MYMONITOR_SELECT_COMPONENTLAYOUT_INFO_MSG_302023 = "查询组件布局信息失败！";
    public static final int MYMONITOR_SELECT_Available_INFO_CODE_302024 = 302024;
    public static final String MYMONITOR_SELECT_Available_INFO_MSG_302024 = "查询可用性信息失败！";
    public static final int MYMONITOR_SELECT_ITEMS_INFO_CODE_302025 = 302025;
    public static final String MYMONITOR_SELECT_ITEMS_INFO_MSG_302025 = "查询应用集信息失败！";
    public static final int MYMONITOR_SELECT_ITEMS_INFO_CODE_302026 = 302026;
    public static final String MYMONITOR_SELECT_ITEMS_INFO_MSG_302026 = "查询高级表格数据失败！";
    public static final int MYMONITOR_SELECT_ITEMS_INFO_CODE_302027 = 302027;
    public static final String MYMONITOR_SELECT_ITEMS_INFO_MSG_302027 = "指标详情数据查询失败！";
    public static final int MYMONITOR_SELECT_COMPONENTLAYOUT_INFO_CODE_302028 = 302028;
    public static final String MYMONITOR_SELECT_COMPONENTLAYOUT_INFO_MSG_302028 = "查询回撤组件布局信息失败！";
    /*报表*/
    public static final int REPORT_DETAIL_CODE_303001 = 303001;
    public static final String REPORT_DETAIL_MSG_303001 = "获取报表详情失败！";

    public static final int TREND_CPUANDMEMORY_CODE_303001 = 303001;
    public static final String TREND_CPUANDMEMORY_MSG_303001 = "获取cpu和内存的报表失败！";
    public static final int TREND_DISK_CODE_303002 = 303002;
    public static final String TREND_DISK_MSG_303002 = "获取磁盘的报表失败！";
    public static final int TREND_NET_CODE_303003 = 303003;
    public static final String TREND_NET_MSG_303003 = "获取网络接口的报表失败！";

    public static final int REPORT_TYPE_CODE_303004 = 303004;
    public static final String REPORT_TYPE_MSG_303004 = "查询报表类型失败！";
    public static final int REPORT_TIME_TASK_CODE_303005 = 303005;
    public static final String REPORT_TIME_TASK_MSG_303005 = "查询报表定时任务失败！";
    public static final int REPORT_INSERT_CODE_303006 = 303006;
    public static final String REPORT_INSERT_MSG_303006 = "新增报表失败！";
    public static final int REPORT_UPDATE_CODE_303007 = 303007;
    public static final String REPORT_UPDATE_MSG_303007 = "修改报表失败！";
    public static final int REPORT_DELETE_CODE_303008 = 303008;
    public static final String REPORT_DELETE_MSG_303008 = "删除报表失败！";
    public static final int REPORT_SELECT_CODE_303009 = 303009;
    public static final String REPORT_SELECT_MSG_303009 = "查询报表失败！";
    public static final int REPORT_ACTION_CODE_303010 = 303010;
    public static final String REPORT_ACTION_MSG_303010 = "查询报表定时任务通知方式失败！";

    /*大屏*/
    public static final int SCREEN_LAYOUT_CODE_304001 = 304001;
    public static final String SCREEN_LAYOUT_MSG_304001 = "获取大屏布局基础数据失败! ";
    public static final int SCREEN_MODEL_CODE_304002 = 304002;
    public static final String SCREEN_MODEL_MSG_304002 = "获取大屏组件基础数据失败! ";
    public static final int SCREEN_INSERT_CODE_304003 = 304003;
    public static final String SCREEN_INSERT_MSG_304003 = "大屏创建失败! ";
    public static final int SCREEN_MODEL_TYPE_CODE_304004 = 304004;
    public static final String SCREEN_MODEL_TYPE_MSG_304004 = "获取大屏组件类型数据失败! ";
    public static final int SCREEN_INSERT_MODEL_DATA_CODE_304005 = 304005;
    public static final String SCREEN_INSERT_MODEL_DATA_MSG_304005 = "添加组件数据失败! ";
    public static final int SCREEN_SAVE_IMG_CODE_304006 = 304006;
    public static final String SCREEN_SAVE_IMG_MSG_304006 = "保存大屏的缩略图失败! ";
    public static final int SCREEN_IMG_ENCODE_CODE_304007 = 304007;
    public static final String SCREEN_IMG_ENCODE_MSG_304007 = "图片转码失败! ";
    public static final int SCREEN_MODEL_DATA_CODE_304008 = 304008;
    public static final String SCREEN_MODEL_DAT_MSG_304008 = "查询大屏对应的组件数据失败! ";
    public static final int SCREEN_LIST_CODE_304009 = 304009;
    public static final String SCREEN_LIST_MSG_304009 = "查询大屏列表失败! ";
    public static final int SCREEN_CREATOR_PERM_CODE_304010 = 304010;
    public static final String SCREEN_CREATOR_PERM_MSG_304010 = "查询创建人用户权限失败! ";
    public static final int SCREEN_UPDATE_CODE_304011 = 304011;
    public static final String SCREEN_UPDATE_MSG_304011 = "修改大屏数据失败 ";
    public static final int SCREEN_DELETE_CODE_304012 = 304012;
    public static final String SCREEN_DELETE_MSG_304012 = "删除大屏失败 ";
    public static final int SCREEN_UPDATE_MODEL_DATA_CODE_304013 = 304013;
    public static final String SCREEN_UPDATE_MODEL_DATA_MSG_304013 = "修改大屏组件数据失败 ";
    public static final int SCREEN_DELETE_MODEL_DATA_CODE_304014 = 304014;
    public static final String SCREEN_DELETE_MODEL_DATA_MSG_304014 = "删除大屏组件数据失败 ";
    public static final int SCREEN_UPDATE_NAME_CODE_304015 = 304015;
    public static final String SCREEN_UPDATE_NAME_DATA_MSG_304015 = "修改大屏名称失败";
    public static final int FILTER_INSERT_CODE_304016 = 304016;
    public static final String FILTER_INSERT_MSG_304016 = "编辑资产过滤条件失败";

    /**
     * IOT资产
     */
    public static final int ASSETS_IOT_BROWSE_TYPE_CODE_305000 = 305000;
    public static final String ASSETS_IOT_BROWSE_TYPE_MSG_305000 = "获取IOT类型列表失败";
    public static final int ASSETS_IOT_UPDATE_CODE_305001 = 305001;
    public static final String ASSETS_IOT_UPDATE_MSG_305001 = "修改温湿度阈值失败 ";
    public static final int ASSETS_IOT_BROWSE_CODE_305002 = 305002;
    public static final String ASSETS_IOT_BROWSE_MSG_305002 = "查询温湿度资产告警失败 ";
    public static final int ASSETS_IOT_UPDATE_VOICE_CODE_305003 = 305003;
    public static final String ASSETS_IOT__UPDATE_VOICE_MSG_305003 = "修改声音失败 ";
    public static final int ASSETS_IOT_BROWSE_ASSETS_CODE_305004 = 305004;
    public static final String ASSETS_IOT_BROWSE_ASSETS_MSG_305004 = "获取IOT资产列表失败";

    /**
     * SolarReport报表
     */
    public static final int SOLAR_REPORT_PARAMETER_CODE_306000 = 306000;
    public static final String SOLAR_REPORT_PARAMETER_MSG_306000 = "solar前台传参有误！";
    public static final int SOLAR_REPORT_SELECT_CODE_306001 = 306001;
    public static final String SOLAR_REPORT_SELECT_MSG_306001 = "查询solar报表失败！";
    public static final int SOLAR_CARRIERNAME_SELECT_CODE_306002 = 306002;
    public static final String SOLAR_CARRIERNAME_SELECT_MSG_306002 = "查询CARRIERNAME名称失败！";
    public static final int SOLAR_TIME_EDITOR_CODE_306003 = 306003;
    public static final String SOLAR_TIME_EDITOR_MSG_306003 = "编辑solar时间段失败！";
    public static final int SOLAR_TIME_SELECT_CODE_306004 = 306004;
    public static final String SOLAR_TIME_SELECT_MSG_306004 = "查询solar时间段失败！";
    public static final int SOLAR_HISTORY_SELECT_CODE_306005 = 306005;
    public static final String SOLAR_HISTORY_SELECT_MSG_306005 = "查询solar历史数据失败！";
    public static final int SOLAR_GROUP_SELECT_CODE_306006 = 306006;
    public static final String SOLAR_GROUP_SELECT_MSG_306006 = "查询solar分组数据失败！";
    public static final int SOLAR_CAPTION_SELECT_CODE_306007 = 306007;
    public static final String SOLAR_CAPTION_SELECT_MSG_306007 = "查询caption数据失败！";
    public static final int SOLAR_INPUT_CODE_306008 = 306008;
    public static final String SOLAR_INPUT_MSG_306008 = "导入数据到数据库中失败！";
    /**
     * 虚拟化
     */
    public static final int ASSETS_VCENTER_SELECT_TREE_CODE_307001 = 307001;
    public static final String ASSETS_VCENTER_SELECT_TREE_MSG_307001 = "查询虚拟化树形结构数据失败";
    public static final int ASSETS_VCENTER_SELECT_TABLE_CODE_307002 = 307002;
    public static final String ASSETS_VCENTER_SELECT_TABLE_MSG_307002 = "查询虚拟化table数据失败";
    public static final int ASSETS_STORE_SELECT_TREE_CODE_307003 = 307003;
    public static final String ASSETS_STORE_SELECT_TREE_MSG_307003 = "查询存储树形结构数据失败";
    public static final int SET_VIRTUAL_USER_CODE_307004 = 307004;
    public static final String SET_VIRTUAL_USER_MSG_307004 = "设置虚拟化资产负责人失败";
    public static final int SET_VIRTUAL_USER_CODE_307005 = 307005;
    public static final String SET_VIRTUAL_USER_MSG_307005 = "查询虚拟化资产负责人失败";
    public static final int SET_VIRTUAL_EXPORT_CODE_307006 = 307006;
    public static final String SET_VIRTUAL_EXPORT_MSG_307006 = "虚拟化资产导出失败";
    /**
     * 链路
     */
    public static final int NETWORK_LINK_SELECT_CODE_308001 = 308001;
    public static final String NETWORK_LINK_SELECT_MSG_308001 = "查询链路table失败";
    public static final int NETWORK_LINK_INSERT_CODE_308002 = 308002;
    public static final String NETWORK_LINK_INSERT_MSG_308002 = "添加链路失败";
    public static final int NETWORK_LINK_SELECT_ASSETS_CODE_308003 = 308003;
    public static final String NETWORK_LINK_SELECT_ASSETS_MSG_308003 = "查询链路可以添加的资产失败";
    public static final int NETWORK_LINK_SELECT_IP_ADDRESS_CODE_308034 = 308004;
    public static final String NETWORK_LINK_SELECT_IP_ADDRESS_MSG_308004 = "查询链路可以添加的Ip地址失败";
    public static final int NETWORK_LINK_EDITOR_CODE_308005 = 308005;
    public static final String NETWORK_LINK_EDITOR_MSG_308005 = "编辑链路失败";
    public static final int NETWORK_LINK_DELETE_CODE_308006 = 308006;
    public static final String NETWORK_LINK_DELETE_MSG_308006 = "删除链路失败";
    public static final int NETWORK_ENABLE_ACTIVE_CODE_308007 = 308007;
    public static final String NETWORK_ENABLE_ACTIVE_MSG_308007 = "修改启动方式失败";

    /**
     * 知识库
     */
    public static final int KNOWLEDGE_BASE_SELECT_TYPE_TREE_CODE_309001 = 309001;
    public static final String KNOWLEDGE_BASE_SELECT_TYPE_TREE_MSG_309001 = "查询知识库全部分类失败";
    public static final int KNOWLEDGE_BASE_SELECT_TABLE_CODE_309002 = 309002;
    public static final String KNOWLEDGE_BASE_SELECT_TABLE_MSG_309002 = "查询知识库表失败";
    public static final int KNOWLEDGE_BASE_INSERT_CODE_309003 = 309003;
    public static final String KNOWLEDGE_BASE_INSERT_MSG_309003 = "添加知识库失败";
    public static final int KNOWLEDGE_BASE_UPDATE_CODE_309004 = 309004;
    public static final String KNOWLEDGE_BASE_UPDATE_MSG_309004 = "修改知识库失败";
    public static final int KNOWLEDGE_BASE_DELETE_CODE_309005 = 309005;
    public static final String KNOWLEDGE_BASE_DELETE_MSG_309005 = "删除知识库失败";
    public static final int KNOWLEDGE_BASE_SELECT_BY_ID_CODE_309006 = 309006;
    public static final String KNOWLEDGE_BASE_SELECT_BY_ID_MSG_309006 = "查询知识失败";
    public static final int KNOWLEDGE_LIKED_STATUS_REDIS_SAVE_CODE_309007 = 309007;
    public static final String KNOWLEDGE_LIKED_STATUS_REDIS_SAVE_MSG_309007 = "redis保存点赞状态失败";
    public static final int KNOWLEDGE_LIKED_REDIS_DELETE_CODE_309008 = 309008;
    public static final String KNOWLEDGE_LIKED_REDIS_DELETE_MSG_309008 = "redis删除一条点赞数据失败";
    public static final int KNOWLEDGE_LIKED_REDIS_UPDATE_CODE_309009 = 309009;
    public static final String KNOWLEDGE_LIKED_REDIS_UPDATE_MSG_309009 = "redis修改点赞数量失败";
    public static final int KNOWLEDGE_LIKED_REDIS_SELECTALL_CODE_309010 = 309010;
    public static final String KNOWLEDGE_LIKED_REDIS_SELECTALL_MSG_309010 = "获取redis中存储的所有点赞数据失败";
    public static final int KNOWLEDGE_LIKED_REDIS_SELECT_COUNTALL_CODE_309011 = 309011;
    public static final String KNOWLEDGE_LIKED_REDIS_SELECT_COUNTALL_MSG_309011 = "获取redis中存储的所有点赞数量失败";
    public static final int KNOWLEDGE_LIKED_SAVE_MYSQL_CODE_309012 = 309012;
    public static final String KNOWLEDGE_LIKED_SAVE_MYSQL_MSG_309012 = "点赞数据写入数据库失败";


    /**
     * 日志
     */
    public static final int LOGGER_BROWSE_CODE_310001 = 310001;
    public static final String LOGGER_BROWSE_MSG_310001 = "查询es中的日志失败";
    public static final int LOGGER_UPDATE_CODE_310002 = 310002;
    public static final String LOGGER_UPDATE_MSG_310002 = "修改es中的日志失败";
    public static final int LOGGER_CREATE_CODE_310003 = 310003;
    public static final String LOGGER_CREATE_MSG_310003 = "添加es中的日志失败";
    public static final int LOGGER_MESSAGE_BROWSE_CODE_310004 = 310004;
    public static final String LOGGER_MESSAGE_BROWSE_MSG_310004 = "查询es中message的日志失败";

    /**
     * TPServer管理
     **/
    public static final int ZABBIX_SERVER_SELECT_CODE_311001 = 311001;
    public static final String ZABBIX_SERVER_SELECT_MSG_311001 = "查询TPServer信息失败！";

    public static final int ZABBIX_SERVER_UPDATE_CODE_311002 = 311002;
    public static final String ZABBIX_SERVER_UPDATE_MSG_311002 = "修改TPServer信息失败！";

    public static final int ZABBIX_SERVER_INSERT_CODE_311003 = 311003;
    public static final String ZABBIX_SERVER_INSERT_MSG_311003 = "新增TPServer信息失败！";

    public static final int ZABBIX_SERVER_DELETE_CODE_311004 = 311004;
    public static final String ZABBIX_SERVER_DELETE_MSG_311004 = "删除TPServer信息失败！";

    public static final int ZABBIX_SERVER_DELETE_CODE_311005 = 311005;
    public static final String ZABBIX_SERVER_DELETE_MSG_311005 = "刷新TPServer信息失败！";

    /**
     * 厂商规格型号管理
     */
    public static final int VENDOR_MANAGE_SELECT_CODE_312001 = 312001;
    public static final String VENDOR_MANAGE_SELECT_MSG_312001 = "查询厂商规格型号信息失败！";

    public static final int VENDOR_MANAGE_UPDATE_CODE_312002 = 312002;
    public static final String VENDOR_MANAGE_UPDATE_MSG_312002 = "修改厂商规格型号信息失败！";

    public static final int VENDOR_MANAGE_INSERT_CODE_312003 = 312003;
    public static final String VENDOR_MANAGE_INSERT_MSG_312003 = "新增厂商规格型号信息失败！";

    public static final int VENDOR_MANAGE_DELETE_CODE_312004 = 312004;
    public static final String VENDOR_MANAGE_DELETE_MSG_312004 = "删除厂商规格型号信息失败！";

    public static final int VENDOR_MANAGE_DROPDOWN_CODE_312005 = 312005;
    public static final String VENDOR_MANAGE_DROPDOWN_MSG_312005 = "查询厂商/品牌下拉信息失败！";

    public static final int VENDOR_MANAGE_MODEL_DROPDOWN_CODE_312006 = 312006;
    public static final String VENDOR_MANAGE_MODEL_DROPDOWN_MSG_312006 = "根据厂商查询规格型号下拉信息失败！";

    /**
     *模型管理
     */

    public static final int MODEL_SELECT_CODE_313001 = 313001;
    public static final String MODEL_SELECT_MSG_313001 = "查询模型列表信息失败！";
    public static final int RELATION_MODEL_SELECT_CODE_313002 = 313002;
    public static final String RELATION_MODEL_SELECT_MSG_313002 = "查询模型关系列表信息失败！";
    public static final int RELATION_GROUP_MODEL_SELECT_CODE_313003 = 313003;
    public static final String RELATION_GROUP_MODEL_SELECT_MSG_313003 = "查询模型关系分组列表信息失败！";
    public static final int MODEL_PROPERTIES_SELECT_CODE_313004 = 313004;
    public static final String MODEL_PROPERTIES_SELECT_MSG_313004 = "查询模型属性列表信息失败！";
    public static final int MODEL_INSTANCE_SELECT_CODE_313005 = 313005;
    public static final String MODEL_INSTANCE_SELECT_MSG_313005 = "查询模型实例信息失败！";
    public static final int MODEL_CHART_SELECT_CODE_313006 = 313006;
    public static final String MODEL_CHART_SELECT_MSG_313006 = "查询模型实例图谱信息失败！";
    public static final int MODEL_INSTANCE_CODE_313007 = 313007;
    public static final String MODEL_INSTANCE_MSG_313007 = "更新模型实例数据失败！";
    public static final int MODEL_INSTANCE_CODE_313008 = 313008;
    public static final String MODEL_INSTANCE_MSG_313008 = "删除模型实例数据失败！";
    public static final int MODEL_INSTANCE_CODE_313009 = 313009;
    public static final String MODEL_INSTANCE_MSG_313009 = "请从资源中心添加资产！";


    /**
     * activiti
     */
    public static final int ACTIVITI_SELECT_MODEL_BY_LIMIT_CODE_314001 = 314001;
    public static final String ACTIVITI_SELECT_MODEL_BY_LIMIT_MSG_314001 = "获取流程模板列表失败！";
    public static final int ACTIVITI_SELECT_ACT_BY_LIMIT_CODE_314002 = 314002;
    public static final String ACTIVITI_SELECT_ACT_BY_LIMIT_MSG_314002 = "获取待办列表失败！";
    public static final int ACTIVITI_DELETE_BY_IDS_CODE_314003 = 314003;
    public static final String ACTIVITI_DELETE_BY_IDS_MSG_314003 = "删除流程失败！";
    public static final int ACTIVITI_CREATE_BY_KEY_CODE_314004 = 314004;
    public static final String ACTIVITI_CREATE_BY_KEY_MSG_314004 = "提交流程失败！";
    public static final int ACTIVITI_SELECT_APPLY_BY_LIMIT_CODE_314005 = 314005;
    public static final String ACTIVITI_SELECT_APPLY_BY_LIMIT_MSG_314005 = "查看当前登录用户的申请列表失败！";
    public static final int ACTIVITI_SELECT_APPLY_VIEW_CODE_314006 = 314006;
    public static final String ACTIVITI_SELECT_APPLY_VIEW_MSG_314006 = "查看当前申请的流程图进度失败！";
    public static final int ACTIVITI_DELETE_PROCESS_CODE_314007 = 314007;
    public static final String ACTIVITI_DELETE_PROCESS_MSG_314007 = "撤回知识的发布失败！";

    /*
     * Nmap扫描
     * */
    public static final int NMAP_315001 = 315001;
    public static final String NMAP_MSG_315001 = "新增扫描任务失败！";

    public static final int NMAP_315002 = 315002;
    public static final String NMAP_MSG_315002 = "查询扫描失败！";

    public static final int NMAP_315003 = 315003;
    public static final String NMAP_MSG_315003 = "查询NMAP任务详情失败！";

    public static final int NMAP_315004 = 315004;
    public static final String NMAP_MSG_315004 = "获取NMAP扫描结果失败！";

    public static final int NMAP_315005 = 315005;
    public static final String NMAP_MSG_315005 = "更新扫描任务失败！";

    public static final int NMAP_315006 = 315006;
    public static final String NMAP_MSG_315006 = "开启扫描任务失败！";

    public static final int NMAP_315007 = 315007;
    public static final String NMAP_MSG_315007 = "任务名称已存在,新增扫描任务失败！";

    public static final int NMAP_315008 = 315008;
    public static final String NMAP_MSG_315008 = "Ipv4格式有误,请重新输入！";

    public static final int NMAP_315009 = 315009;
    public static final String NMAP_MSG_315009 = "删除NMAP任务失败！";

    public static final int NMAP_315010 = 315010;
    public static final String NMAP_MSG_315010 = "NMAP扫描失败！";

    /*
    * NMAP节点组查询
    * */
    public static final int NMAP_316001 = 316001;
    public static final String NMAP_MSG_316001 = "指纹探测节点组查询失败！";

    public static final int NMAP_316002 = 316002;
    public static final String NMAP_MSG_316002 = "例外IP组查询失败！";

    public static final int NMAP_316003 = 316003;
    public static final String NMAP_MSG_316003 = "端口组查询失败！";

    public static final int NMAP_316004 = 316004;
    public static final String NMAP_MSG_316004 = "存活探测节点组查询失败！";

    public static final int NMAP_316005 = 316005;
    public static final String NMAP_MSG_316005 = "节点组查询失败！";

    /**
     * 凭据管理
     */
    public static final int CRED_317001 = 317001;
    public static final String CRED_MSG_317001 = "新增凭据失败！";

    public static final int CRED_317002 = 317002;
    public static final String CRED_MSG_317002 = "修改凭据失败！";

    public static final int CRED_317003 = 317003;
    public static final String CRED_MSG_317003 = "删除凭据失败！";

    public static final int CRED_317004 = 317004;
    public static final String CRED_MSG_317004 = "查询凭据列表失败！";

    public static final int CRED_317005 = 317005;
    public static final String CRED_MSG_317005 = "查询凭据下拉框失败！";

    public static final int CRED_317006 = 317006;
    public static final String CRED_MSG_317006 = "查询凭据失败！";
    /**
     * 混合云
     */
    public static final int ASSETS_HYBRIDCLOUD_SELECT_TREE_CODE_318001 = 318001;
    public static final String ASSETS_HYBRIDCLOUD_SELECT_TREE_MSG_318001 = "查询混合云树形结构数据失败";
    public static final int ASSETS_HYBRIDCLOUD_SELECT_TABLE_CODE_318002 = 318002;
    public static final String ASSETS_HYBRIDCLOUD_SELECT_TABLE_MSG_318002 = "查询混合云table数据失败";
    public static final int ASSETS_HYBRIDCLOUD_SELECT_BASE_CODE_318003 = 318003;
    public static final String ASSETS_HYBRIDCLOUD_SELECT_BASE_MSG_318003 = "查询混合云基础数据失败";

    /**
     * 无线监控
     */
    public static final int WIRELESS_AP_SELECT_CODE_319001 = 319001;
    public static final String WIRELESS_AP_SELECT_MSG_319001 = "查询无线AP数据失败";
//    public static final int WIRELESS_AP_SELECT_TABLE_CODE_319002 = 319002;
//    public static final String WIRELESS_AP_SELECT_TABLE_MSG_319002 = "查询混合云table数据失败";
//    public static final int ASSETS_HYBRIDCLOUD_SELECT_BASE_CODE_318003 = 318003;
//    public static final String ASSETS_HYBRIDCLOUD_SELECT_BASE_MSG_318003 = "查询混合云基础数据失败";

    public static final int AUTO_MANAGE_SELECT_BASE_CODE_320001 = 320001;
    public static final String AUTO_MANAGE_SELECT_BASE_MSG_320001 = "查询混合云基础数据失败";

    /**
     * prometheus
     */
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_321001 = "查询prometheus显示面板失败";
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_322001 = "查询prometheus面板数据失败";
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_323001 = "新增面板数据失败";
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_324001 = "修改面板数据失败";
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_325001 = "删除面板数据失败";
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_326001 = "新增布局数据失败";
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_327001 = "获取指标脚本失败";
    public static final String PROMETHEUS_QUERY_PANEL_ERROR_MSG_328001 = "获取字段映射失败";

}
