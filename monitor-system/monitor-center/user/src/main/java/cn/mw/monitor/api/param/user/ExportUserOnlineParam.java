package cn.mw.monitor.api.param.user;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author shenwenyi
 * @Date 2023/8/9 11:06
 * @PackageName:cn.mw.monitor.api.param.user
 * @ClassName: exportUserOnlineParam
 * @Description: TODD
 * @Version 1.0
 */
@Data
@ApiModel(description = "导出用户在线时长")
public class ExportUserOnlineParam extends BaseParam {

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("查询选用时间的类型 2:今天 1:昨天 5:上周 8:上月 11:自定义(自定义需传入开始时间及结束时间)")
    private Integer dateType;

    @ApiModelProperty("时间")
    private List<Date> chooseTime;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

}
