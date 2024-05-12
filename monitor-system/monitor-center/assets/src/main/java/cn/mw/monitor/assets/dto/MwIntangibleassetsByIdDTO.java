package cn.mw.monitor.assets.dto;

import cn.mw.monitor.assets.model.MwIntangibleassetsTable;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/03/16
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwIntangibleassetsByIdDTO extends MwIntangibleassetsTable {
    @ApiModelProperty(value="资产类型")
    private String assetsTypeName;

    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value="负责人")
    private List<Integer> principal;

    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds;
}