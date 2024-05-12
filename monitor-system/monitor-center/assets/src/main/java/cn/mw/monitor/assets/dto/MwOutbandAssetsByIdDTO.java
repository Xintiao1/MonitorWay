package cn.mw.monitor.assets.dto;

import cn.mw.monitor.assets.model.MwOutbandAssetsTable;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwPortAssetsDTO;
import cn.mw.monitor.service.assets.param.MwIPMIAssetsDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * @author syt
 * @Date 2020/6/22 15:47
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ApiModel(value = "带外资产查询ById返回结果集")
public class MwOutbandAssetsByIdDTO extends MwOutbandAssetsTable {
    @ApiModelProperty(value="IPMI接口")
    private MwIPMIAssetsDTO mwIPMIAssetsDTO;

    @ApiModelProperty(value="端口扫描")
    private MwPortAssetsDTO portAssetsDTO;

    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value="负责人")
    private List<Integer> principal;

    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds;
}
