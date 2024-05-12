package cn.mw.monitor.netflow.constant;

/**
 * @author guiquanwnag
 * @datetime 2023/6/19
 * @Description
 */
public interface FlowConstant {

    /**
     * 抓包的数据库
     */
    String CAP_DATABASE = "cap";

    /**
     * 流量的数据库
     */
    String NETFLOW_DATABASE = "netflow";


    /**
     * 空格
     */
    String SPACE = " ";

    String QUERY_ALL = "SELECT * FROM ";

    String SEPERATOR = ",";

    String NETFLOW_SEP = "-";

    String EMPTY_STRING = "";

    String IP_GROUP_RANGE_SEP = "/";

    String IP_GROUP_PHASE_SEP = "-";

    /**
     * redis前缀
     */
    String REDIS_PREFIX = "redis-netflow";

    /**
     * clcikhouse的表名前缀
     */
    String CIICKHOUSE_INDEX_PREFIX = "captcp";

    /**
     * 查询所有的前缀
     */
    String ALL_PREFIX = "*";

    String AND = " and";

    String OR = " or";

    String EQUAL = "=";

    String PLUS = "+";

    String REAL_PLUS = "\\+";

    /**
     * 存储类型为clickhouse
     */
    int STORAGE_CLICKHOUSE = 2;

    String TABLE_SEP = "_";

    String DATABASE_LINK = ".";

    String YEAR_FORMAT = "yyyy";

    String MONTH_FORMAT = "yyyyMM";

    String DAY_FORMAT = "yyyyMMdd";

    String DAY_FORMAT_SIMPLE = "yyyy-MM-dd";

    /**
     * 流量数据查询
     */
    String CAP_DATA_SEARCH = "SELECT %s FROM %s where %s";

    String CAP_DATA_SEARCH_PAGE = "SELECT * from (%s) order by %s %s limit %d , %d";

    /**
     * 流量数据统计查询
     */
    String CAP_DATA_COUNT_SEARCH = "SELECT count(1) as allCount FROM %s where %s";

    String CAP_DATA_COUNT_SEARCH_ALL = "select sum(allCount) from (%s)";

    /**
     * 统计交互流量图表数据（根据出入流量的查询结果进行查询）
     */
    String CAP_CHART_NETFLOW = "SELECT FLOOR((createTime - %d) / %d) AS timeInterval, count(1) AS times  FROM %s where %s GROUP BY timeInterval order by timeInterval asc";

    /**
     * 统计交互流量数据（根据出入流量排序）出入流量
     */
    String NEW_STAT_NETFLOW_TOP_ALL = "SELECT srcIp AS sourceIp, destIp AS dstIp, srcPort AS sourcePort, destPort AS dstPort, SUM(length) AS compareData  FROM (%s) GROUP BY srcIp, destIp, srcPort, destPort";

    /**
     * 统计交互流量数据（根据出入流量排序）入流量
     */
    String NEW_STAT_NETFLOW_TOP_IN = "SELECT srcIp AS srcIp, srcPort AS srcPort, SUM(length) AS compareData  FROM (%s) GROUP BY srcIp, srcPort";


    /**
     * 统计交互流量数据（根据出入流量排序）出流量
     */
    String NEW_STAT_NETFLOW_TOP_OUT = "SELECT destIp AS destIp, destPort AS destPort, SUM(length) AS compareData  FROM (%s) GROUP BY  destIp,  destPort";


    String NEW_NETFLOW_SEARCH = "SELECT srcIp , destIp , srcPort , destPort, length FROM %s where %s";

    String NEW_NETFLOW_CHART = "SELECT FLOOR((createTime - %d) / %d) AS timeInterval, sum(length) AS sumBytes ,srcIp AS sourceIp, destIp AS dstIp FROM (%s) GROUP BY srcIp, destIp, timeInterval order by timeInterval asc";

    String NEW_NETFLOW_CHART_SEARCH = "SELECT length ,srcIp, destIp, createTime FROM %s where %s";

    String NEW_NETFLOW_APP_CHART_SEARCH = "SELECT length ,srcIp, destIp, createTime FROM %s where %s";

    /**
     * 流量数据统计查询
     */
    String CAP_SUM_SEARCH = "SELECT sum(length) as allCount FROM %s where %s";

    String CAP_SUM_SEARCH_ALL = "select sum(allCount) from (%s)";

    /**
     * 流量数据统计查询
     */
    String CAP_MAX_SEARCH = "SELECT FLOOR((createTime - 1000) / 1000) AS timeInterval, sum(length) AS maxLength FROM %s where %s GROUP BY timeInterval";

    String CAP_MAX_SEARCH_ALL = "select max(maxLength) from (%s)";
}
