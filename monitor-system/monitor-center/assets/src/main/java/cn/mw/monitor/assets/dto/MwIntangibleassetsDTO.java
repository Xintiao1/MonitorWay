package cn.mw.monitor.assets.dto;

import cn.mw.monitor.assets.model.MwIntangibleassetsTable;
import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
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
public class MwIntangibleassetsDTO extends MwIntangibleassetsTable {

    /**
     * 资产类型
     */
    private String assetsTypeName;

    /**
     * 资产类型
     */
    private String subAssetsTypeName;

    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value="机构")
    private List<OrgDTO> department;

    @ApiModelProperty(value="负责人")
    private List<UserDTO> principal;

    @ApiModelProperty(value="用户组")
    private List<GroupDTO> group;
}