package cn.mw.monitor.common.constant;

/**
 * Created by yeshengqi on 2019/5/13.
 */
public class Constants {

    public static final String APP_RESOURCE = "/app/**";

    public static final String PLUGINS_RESOURCE = "/plugins/**";

    public static final Integer YES = 1;

    public static final Integer NO = 0;

    public static final String SERVER_PROPERTIES_PATH = "file:/work1/monitor/home/config/monitor-server.properties";

    public static final String DB_PROPERTIES_PATH = "file:/work1/monitor/home/config/monitor-db.properties";


    /**
     * 启用
     */
    public static final Integer ENABLE = 1;

    /**
     * 禁用
     */
    public static final Integer DISABLE = -1;

    /**
     * 主机未连接信息
     */
    public static final String HOST_NOT_CONNECT = "** No Information **";
    /**
     * 默认密码策略id
     */
    public static final Integer defaultPasswdPlanId = 1;
    /**
     * 初始生效密码策略id
     */
    public static final Integer initActivePasswdPlanId = 0;
    /**
     * web监测默认hostid
     */
    public static final String WEB_MONITOR_HOSTID = "10939";

    /**
     * 系统角色
     */
    public static final Integer SYSTEM_ROLE = 1;

    /**
     * 系统管理机构
     */
    public static final Integer SYSTEM_ORG = 1;

    /**
     * 系统管理员的UserId
     */
    public static final Integer SYSTEM_ADMIN = 106;

    public static final String NODE_PROT_MODEL_MANAGE = "MODEL_MANAGE";

    /**
     * MYSQL数据库
     */
    public static final String DATABASE_MYSQL = "mysql";

    /**
     * ORACLE数据库
     */
    public static final String DATABASE_ORACLE = "oracle";
}
