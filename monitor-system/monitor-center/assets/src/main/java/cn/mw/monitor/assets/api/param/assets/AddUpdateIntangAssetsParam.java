package cn.mw.monitor.assets.api.param.assets;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/16
 */
@Data
public class AddUpdateIntangAssetsParam {
    private String id;
    /**
     *资产编号
     */
    @ApiModelProperty(value = "资产编号")
    private String assetsNumber;

    /**
     *资产名称
     */
    @ApiModelProperty(value = "资产名称")
    private String assetsName;

    /**
     *资产类型
     */
    @ApiModelProperty(value = "资产类型")
    private Integer assetsTypeId;

    /**
     *资产类型
     */
    @ApiModelProperty(value = "资产类型")
    private Integer subAssetsTypeId;

    /**
     *资产内容
     */
    @ApiModelProperty(value = "资产内容")
    private String assetsContent;

    /**
     *资产状态
     */
    @ApiModelProperty(value = "资产状态")
    private String enable;

    /**
     *备注
     */
    @ApiModelProperty(value = "备注")
    private String remarks;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;


    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;
}
