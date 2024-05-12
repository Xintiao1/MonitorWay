package cn.mw.monitor.service.common;


/**
 * @author phzhou
 * @ClassName DateConstant
 * @CreateDate 2019/3/4
 * @Description
 */
public class MWDateConstant {

    /**
     * 标准日期格式：yyyy-MM-dd
     */
    public final static String NORM_DATE = "yyyy-MM-dd";

    /**
     * 标准日期格式：MM-dd
     */
    public final static String NORM_MONTH_DAY = "MM-dd";

    /**
     * 标准日期格式：yyyy-MM
     */
    public final static String NORM_YEAR_MONTH = "yyyy-MM";

    /**
     * 标准时间格式：HH:mm:ss
     */
    public final static String NORM_TIME = "HH:mm:ss";

    /**
     * 标准日期时间格式，精确到分：yyyy-MM-dd HH:mm
     */
    public final static String NORM_DATETIME_MINUTE = "yyyy-MM-dd HH:mm";

    /**
     * 标准日期时间格式，精确到秒：yyyy-MM-dd HH:mm:ss
     */
    public final static String NORM_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS
     */
    public final static String NORM_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 标准日期格式：yyyy年MM月dd日
     */
    public final static String CHINESE_DATE = "yyyy年MM月dd日";

    /**
     * 标准日期格式：yyyyMMdd
     */
    public final static String PURE_DATE = "yyyyMMdd";
    /**
     * 标准日期格式：yyyyMM
     */
    public final static String MOUTH_DATE = "yyyyMM";


    /**
     * 标准日期格式：HHmmss
     */
    public final static String PURE_TIME = "HHmmss";

    /**
     * 标准日期格式：yyyyMMddHHmmss
     */
    public final static String PURE_DATETIME = "yyyyMMddHHmmss";

    /**
     * 标准日期格式：yyyyMMddHHmmssSSS
     */
    public final static String PURE_DATETIME_MS = "yyyyMMddHHmmssSSS";

    /**
     * HTTP头中日期时间格式：EEE, dd MMM yyyy HH:mm:ss z
     */
    public final static String HTTP_DATETIME = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * JDK中日期时间格式：EEE MMM dd HH:mm:ss zzz yyyy
     */
    public final static String JDK_DATETIME = "EEE MMM dd HH:mm:ss zzz yyyy";

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public final static String UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public final static String BEGIN_TIME = " 00:00:00";

    public final static String END_TIME = " 23:59:59";
}
