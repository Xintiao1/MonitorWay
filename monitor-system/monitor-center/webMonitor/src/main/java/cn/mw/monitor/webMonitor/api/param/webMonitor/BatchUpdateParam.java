package cn.mw.monitor.webMonitor.api.param.webMonitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author syt
 * @Date 2021/9/6 14:54
 * @Version 1.0
 */
@Data
@ToString
public class BatchUpdateParam extends AddUpdateWebMonitorParam {
    @ApiModelProperty(value = "是否需要批量修改更新间隔")
    private boolean updateIntervalcheckbox;
    @ApiModelProperty(value = "是否需要批量修改尝试次数")
    private boolean attemptscheckbox;
    @ApiModelProperty(value = "是否需要批量修改启用状态")
    private boolean enablecheckbox;
    @ApiModelProperty(value = "是否需要批量修改超时")
    private boolean timeOutcheckbox;
    @ApiModelProperty(value = "是否需要批量修改要求的状态码")
    private boolean statusCodecheckbox;
    @ApiModelProperty(value = "是否需要批量修改责任人")
    private boolean principalcheckbox;
    @ApiModelProperty(value = "是否需要批量修改机构")
    private boolean orgIdscheckbox;
    @ApiModelProperty(value = "是否需要批量修改用户组")
    private boolean groupIdscheckbox;
//    @ApiModelProperty(value = "是否需要批量修改标签列表")
//    private boolean assetsLabelcheckbox;
//    @ApiModelProperty(value = "是否需要批量修改轮询引擎")
//    private boolean pollingEnginecheckbox;
    @ApiModelProperty(value = "web监测的主键")
    private List<Integer> ids;

}
