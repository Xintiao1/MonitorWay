package cn.mw.monitor.service.assets.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/1/14 14:37
 * @Version 1.0
 */
@Data
@ToString
public class UpdateTangAssetsParam extends AddUpdateTangAssetsParam {

    @ApiModelProperty(value = "是否需要批量修改带外IP")
    private boolean outBandIpcheckbox;
    @ApiModelProperty(value = "是否需要批量修改描述")
    private boolean descriptioncheckbox;
    @ApiModelProperty(value = "是否需要批量修改资产状态")
    private boolean enablecheckbox;
    @ApiModelProperty(value = "是否需要批量修改监控状态")
    private boolean monitorFlagcheckbox;
    @ApiModelProperty(value = "是否需要批量修改配置状态")
    private boolean settingFlagcheckbox;
    @ApiModelProperty(value = "是否需要批量修改责任人")
    private boolean principalcheckbox;
    @ApiModelProperty(value = "是否需要批量修改机构")
    private boolean orgIdscheckbox;
    @ApiModelProperty(value = "是否需要批量修改用户组")
    private boolean groupIdscheckbox;
    @ApiModelProperty(value = "是否需要批量修改标签列表")
    private boolean assetsLabelcheckbox;
    @ApiModelProperty(value = "是否需要批量修改轮询引擎")
    private boolean pollingEnginecheckbox;
    @ApiModelProperty(value = "key值是监控服务器id, value值是轮询引擎对应pollingEngineId")
    private Map<Integer, String> pollingEngineList;

    @ApiModelProperty(value = "key值是监控服务器id, value值是监控服务器对应的代理id数组")
    private Map<Integer, List<String>> proxyIdList;
}
