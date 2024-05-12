package cn.mw.monitor.model.param;

import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.model.param.CabinetLayoutDataParam;
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
public class AddAndUpdateModelInstanceParam extends SystemLogDTO {
    @ApiModelProperty("实例存在es中的id")
    private String esId;
    @ApiModelProperty("模型实例主键")
    private Integer instanceId;
    @ApiModelProperty("关联依赖实例Id")
    private Integer relationInstanceId;
    @ApiModelProperty("模型实例名称")
    private String instanceName;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型分组Id")
    private Integer modelGroupId;
    @ApiModelProperty("模型名称")
    private String modelName;
    @ApiModelProperty("模型实例类型")
    private String instanceType;
    @ApiModelProperty("模型实例拓扑信息")
    private String topoInfo;
    @ApiModelProperty("机柜坐标信息")
    private CabinetLayoutDataParam cabinetCoordinate;
    @ApiModelProperty("机房坐标信息")
    private List<Integer> roomCoordinate;

    @ApiModelProperty("资产的主键Id")
    private List<String> tangibleIds;

    @ApiModelProperty("模型视图类型，0：普通，1：机房，2机柜")
    private Integer modelViewType;
    @ApiModelProperty("外部关联modelIndex")
    private String relationModelIndex;

    @ApiModelProperty("模型索引")
    @NotNull(message = "模型索引不能为空！")
    private String modelIndex;

    @ApiModelProperty("模型的属性和属性值")
    List<AddModelInstancePropertiesParam> propertiesList;

    @ApiModelProperty("导入数据的模型属性和属性值")
    List<ModeInstanceExportParam> exportPropertiesList;

    //实例新增类型，0为走流程新增，1为走表单处理
    private int createType;

    @ApiModelProperty("目标模型Id")
    private Integer targetModelId;
    @ApiModelProperty("目标模型信息")
    private List<ModelInstanceShiftParam> targetModelInfo;
    @ApiModelProperty("目标模型名称")
    private String targetModelName;
    @ApiModelProperty("目标分组Id")
    private Integer targetGroupId;
    @ApiModelProperty("目标分组名称")
    private String targetGroupName;
    @ApiModelProperty("源模型名称")
    private String ownModelName;
    @ApiModelProperty("源分组名称")
    private String ownGroupName;
    @ApiModelProperty("模型Ids")
    private List<Integer> modelIds;
    //删除使用
    private List<String> esIdList;

    @ApiModelProperty("源模型实例主键")
    private Integer ownInstanceId;
    @ApiModelProperty("目标模型实例主键")
    private Integer targetInstanceId;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    private List<UserDTO> principal;

    private List<OrgDTO> department;

    private List<GroupDTO> groups;

    //是否同步（凭证信息）
    private boolean isSync;

    //同步时代理扫描id
    private String proxyId;

    //是否纳管（zabbix关联管理）
    private boolean isManage;

    //是否修改zabbix服务
    private boolean editorZabbixServer;

    //是否高级设置
    private boolean isSetting;
    //事件关联 模块Id
    private Integer workflowMoudleId;

    //同步凭证参数
    private List<MwModelMacrosValInfoParam> syncParams;
    //添加纳管参数
    private AddUpdateTangAssetsParam manageParam;
    //高级设置参数
    private MwInstanceAdvancedSetting setParam;

    //批量导入时 插入deviceCode使用
    private Integer monitorMode;
    //批量导入时 插入deviceCode使用
    private String deviceCode;
    //批量导入时 插入deviceCode使用
    private String assetsId;
    //批量导入时 插入deviceCode使用
    private String inBandIp;

}
