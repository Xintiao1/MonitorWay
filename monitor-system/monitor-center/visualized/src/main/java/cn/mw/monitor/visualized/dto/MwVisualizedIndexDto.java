package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MwVisualizedIndexDto
 * @Description 可视化指标实体
 * @Author gengjb
 * @Date 2022/4/20 14:56
 * @Version 1.0
 **/
@Data
public class MwVisualizedIndexDto extends BaseParam {

    /**
     * 指标ID
     */
    private Integer indexId;

    /**
     * 指标父ID
     */
    private Integer indexParentId;

    /**
     * 数据源ID
     */
    private Integer dataSourceId;

    /**
     * 指标名称
     */
    private String indexName;

    /**
     * 指标监控项名称
     */
    private String indexMonitorItem;

    /**
     * 接口名称
     */
    private String interfaceName;


    /**
     * 数值类型
     */
    private String numberType;

    /**
     * 当前值
     */
    private String currValue;

    /**
     * 资产数据集合
     */
    private List<MwVisualizedAssetsDto> params;

    /**
     * 主机ID
     */
    private String assetsId;

    /**
     * 资产名称
     */
    private String assetsName;

    /**
     * 监控项ID
     */
    private String itemId;

    @ApiModelProperty("监控服务器id")
    private Integer monitorServerId;

    /**
     * zabbix类型
     */
    private Integer valueType;

    /**
     * 唯一标识
     */
    private Integer onlyId;

    /**
     * 原单位
     */
    private String originUnits;


    private String ipAddress;

    //选择的监控项名称集合
    private List<String> itemNames;

}
