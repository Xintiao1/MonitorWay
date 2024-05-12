package cn.mw.monitor.netflow.param;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className NetFlowRequestParam
 * @description 流量监控请求参数
 * @date 2022/8/4
 */
@Data
public class NetFlowRequestParam {

    /**
     * 节点ID
     */
    private Integer id;

    /**
     * 节点ID集合
     */
    private List<Integer> ids;

    /**
     * 流量监控类别（1：入  2：出  3：出+入）
     */
    private Integer netFlowType;

    /**
     * 时间类别（1：最近一小时  2：最近一天  3：最近一周  4：最近一月  5：自定义  6:最近5分钟）
     */
    private Integer dateType;

    /**
     * 开始时间（时间类别为自定义生效）
     */
    private Date startTime;

    /**
     * 结束时间（时间类别为自定义生效）
     */
    private Date endTime;

    /**
     * 获取前几名
     */
    private Integer topSize;

    /**
     * 数据源类别， 1：ES数据源  2：clickhouse数据源
     */
    private Integer databaseType;
}
