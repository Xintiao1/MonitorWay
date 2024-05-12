package cn.mw.monitor.assets.api.param.assets;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class BatchAddTangAssetsParam {

    @ApiModelProperty(value = "扫描成功id")
    private List<Integer> scanSuccessId;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;

    @ApiModelProperty(value = "资产状态")
    private String enable;

    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value = "启动监控状态")
    private Boolean monitorFlag;

    @ApiModelProperty(value = "启动配置状态")
    private Boolean settingFlag;

    @ApiModelProperty(value = "关联的轮询引擎")
    private String pollingEngine;

    /**
     * 启动立即执行
     */
    @ApiModelProperty(value = "启动立即执行")
    private boolean checkNowFlag;

    //是否新版本（模型管理设备资产添加）
    private Boolean isNewVersion;

}
