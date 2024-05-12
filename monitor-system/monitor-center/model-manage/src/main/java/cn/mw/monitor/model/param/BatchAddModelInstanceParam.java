package cn.mw.monitor.model.param;

import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/25 9:11
 */
@Data
@ApiModel
@ToString
public class BatchAddModelInstanceParam extends SystemLogDTO {
    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    @ApiModelProperty("实例存在es中的id")
    private String esId;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型分组Id")
    private Integer modelGroupId;
    @ApiModelProperty("模型名称")
    private String modelName;
    @ApiModelProperty("模型索引")
    @NotNull(message = "模型索引不能为空！")
    private String modelIndex;
    //运维监控
    private Boolean operationMonitor;
    //自动化
    private Boolean autoManage;
    //日志管理
    private Boolean logManage;
    //配置管理
    private Boolean propManage;
    //是否纳管（zabbix关联管理）
    private boolean isManage;
    //添加纳管参数
    private AddUpdateTangAssetsParam manageParam;
    //实例资产数据，IP地址资产名称List
    private List<BatchAddMwModelInstanceParam> batchInsertAssetsList;
}
