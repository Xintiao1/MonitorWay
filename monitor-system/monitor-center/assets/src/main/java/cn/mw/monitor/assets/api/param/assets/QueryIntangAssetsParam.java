package cn.mw.monitor.assets.api.param.assets;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsLabelParam;
import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/16
 */
@Data
@Builder
public class QueryIntangAssetsParam extends BaseParam {

    private String id;

    @ApiModelProperty(value = "资产编号")
    private String assetsNumber;

    /**
     *资产名称
     */
    @ApiModelProperty(value = "资产名称")
    private String assetsName;

    /**
     * 资产类型
     */
    @ApiModelProperty(value = "资产类型")
    private Integer assetsTypeId;

    @ApiModelProperty(value = "资产子类型")
    private Integer subAssetsTypeId;

    /**
     * 资产内容
     */
    @ApiModelProperty(value = "资产内容")
    private String assetsContent;

    /**
     * 资产状态
     */
    @ApiModelProperty(value = "资产状态")
    private String enable;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remarks;

    private String labelName;

    /**
     * 删除标识符
     */
    private Boolean deleteFlag;

    private String creator;

    private String modifier;

    private Date createDateStart;

    private Date createDateEnd;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private List<QueryTangAssetsLabelParam> labelList;

//    private List<MwAllLabelDTO> allLabelList;
//
//    private Boolean isSelectLabel = false;

    //逻辑标签查询条件
    private List<List<LogicalQueryLabelParam>> logicalQueryLabelParamList;
    //标签查询后的资产id
    private List<String> assetsIds;

    private String perm;

    private Integer userId;

    private List<Integer> groupIds;

    private List<Integer> orgIds;

    private Boolean isAdmin;

    @Tolerate
    QueryIntangAssetsParam() {
    }
}
