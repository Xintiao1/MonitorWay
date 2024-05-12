package cn.mw.monitor.api.dataview;

import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AssetsResultSuccView implements AssetsResultView{
    @ApiModelProperty(value="序号")
    @JSONField(ordinal = 1)
    private int id;

    @ApiModelProperty(value="已发现主机")
    @JSONField(ordinal = 2)
    private String host;

    @ApiModelProperty(value="IP地址")
    @JSONField(ordinal = 3)
    private String ip;

    @ApiModelProperty(value="品牌")
    @JSONField(ordinal = 4)
    private String brand;

    @ApiModelProperty(value="描述")
    @JSONField(ordinal = 5)
    private String desc;

    @ApiModelProperty(value="规格类型")
    @JSONField(ordinal = 6)
    private String specification;

    @ApiModelProperty(value="资产分组类型")
    @JSONField(ordinal = 7)
    private String groupTypeName;

    @ApiModelProperty(value="扫描成功id")
    @JSONField(ordinal = 8)
    private Integer scanSuccResultId;

    @ApiModelProperty(value="资产是否添加")
    @JSONField(ordinal = 9)
    private String isExist;

    @ApiModelProperty(value="资产类型")
    @JSONField(ordinal = 9)
    private Integer assetsTypeId;

    @ApiModelProperty(value="资产类型名称")
    @JSONField(ordinal = 9)
    private String assetsTypeName;

    @ApiModelProperty(value="监控服务器")
    @JSONField(ordinal = 9)
    private Integer monitorServerId;

    @ApiModelProperty(value="OID")
    @JSONField(ordinal = 9)
    private String sysObjId;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;
    //实例列表批量纳管使用
    private String instanceName;

    public void init(ScanResultSuccess value){
        this.host = value.getHostName();
        this.ip = value.getIpAddress();
        this.brand = value.getBrand();
        this.desc = value.getDescription();
        this.specification = value.getSpecifications();
        this.groupTypeName = value.getGroupTypeName();
        this.scanSuccResultId = value.getId();
        this.assetsTypeId = value.getAssetsTypeId();
        this.monitorServerId = value.getMonitorServerId();
        this.sysObjId = value.getSysObjId();
        this.assetsTypeName = value.getAssetsTypeName();
        this.groupIds = value.getGroupIds();
        this.userIds = value.getUserIds();
        this.orgIds = value.getOrgIds();
        this.instanceName = value.getInstanceName();
        if(null != value && value.getScanSuccessIdInAssets() > 0){
            isExist = AssetAddType.EXSIST.getChnName();
        }else{
            isExist = AssetAddType.NOTEXSIST.getChnName();
        }
    }
}
