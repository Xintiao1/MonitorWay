package cn.mw.module.security.dto;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author qzg
 * @date 2021/12/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsSysLogAuditQueryDTO extends BaseParam {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("日志时间")
    private Date timestamp;
    @ApiModelProperty("设备类型")
    private String facility_label;
    @ApiModelProperty("日志等级")
    private String severity_label;
    @ApiModelProperty("主机IP")
    private String host;
    @ApiModelProperty("日志信息")
    private String message;
    @ApiModelProperty("查询索引")
    private String queryEsIndex;
    @ApiModelProperty("返回的特定字段")
    private List<String> fieldList;
    @ApiModelProperty("查询字段")
    private List<EsQueryParam> queryFieldList;
    //1:10分钟、2:1小时、3:12小时、4:1天、5:一周 6:一个月 7：自定义
    @ApiModelProperty("时间类型")
    private Integer dateType;
    @ApiModelProperty("开始时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("排序类型  0升序，1降序")
    private Integer sortType;

    private String sortField;
}
