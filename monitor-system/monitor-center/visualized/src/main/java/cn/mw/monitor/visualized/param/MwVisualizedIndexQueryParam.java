package cn.mw.monitor.visualized.param;

import cn.mw.monitor.visualized.dto.MwVisualizedAssetsDto;
import cn.mw.monitor.visualized.dto.MwVisualizedIndexDto;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwVisualizedIndexQueryParam
 * @Author gengjb
 * @Date 2022/4/25 15:11
 * @Version 1.0
 **/
@Data
@ApiModel("可视化查询监控数据参数")
public class MwVisualizedIndexQueryParam {

    @ApiModelProperty("时间分类类型")
    private Integer type;

    @ApiModelProperty("时间取值类型")
    private Integer dateType;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("数据源")
    private Integer dataSource;

    @ApiModelProperty("指标名称")
    private String indexName;

    @ApiModelProperty("指标英文名称")
    private String indexNameEng;

    @ApiModelProperty("图类型")
    private Integer chartType;

    @ApiModelProperty("过滤规则")
    List<MwRuleSelectParam> mwRuleSelectListParam;

    @ApiModelProperty("资产ID集合")
    private List<String> assetsIds;

    @ApiModelProperty("选择的指标信息")
    private List<MwVisualizedIndexDto> indexDtos;

    //前端回显参数
    private Map reshowParams;

    @ApiModelProperty("是否导出  0：非导出 1：导出")
    private int isExport;
}
