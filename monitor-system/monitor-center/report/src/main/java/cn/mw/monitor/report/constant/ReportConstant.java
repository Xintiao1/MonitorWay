package cn.mw.monitor.report.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName
 * @Description ToDo
 * @Author gengjb
 * @Date 2023/3/14 14:30
 * @Version 1.0
 **/
public class ReportConstant {

    public static final List<String> IP_UTILIZATION_COLUMN = Arrays.asList("名称 ","地址段总数 ", "≤50%使用率", "50%~80%使用率 ","≥80%使用率 ");

    public static final List<String> IP_UTILIZATION_TOPN_COLUMN = Arrays.asList("分组名称 ","当前使用率");

    public static final List<String> IP_OPERATE_CLASSIFY_COLUMN = Arrays.asList("操作名称 ","操作次数");

    public static final List<String> IP_UPDATE_NUMBER_COLUMN = Arrays.asList("分组名称 ","次数");

    public static final String MW_INTERFACE_IN_TRAFFIC = "MW_INTERFACE_IN_TRAFFIC";

    public static final String MW_INTERFACE_OUT_TRAFFIC = "MW_INTERFACE_OUT_TRAFFIC";


    //暂时定义,后面需要做到页面上配置
    public static final List<String> alertAssets = Arrays.asList("13432");


    public static final String manufacturer = "品牌";

    public static final String assetsType = "资产类型";

    public static final String assetsOrg = "机构";
    public static final String assetsBusinessSystem = "业务系统";

    public static final List<String> DISK_ITEMS = Arrays.asList("MW_DISK_TOTAL","MW_DISK_USED","MW_DISK_FREE","MW_DISK_UTILIZATION");
}
