package cn.mw.monitor.assets.dto;

import cn.mw.monitor.assets.model.MwOutbandAssetsTable;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.MwIPMIAssetsDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * @author syt
 * @Date 2020/5/21 15:55
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ApiModel(value = "资产返回结果集")
public class MwOutbandAssetsDTO extends MwOutbandAssetsTable {

    @ApiModelProperty(value = "IPMI协议")
    private MwIPMIAssetsDTO mwIPMIAssetsDTO;

    @ApiModelProperty(value = "端口扫描")
    private MwPortAssetsDTO portAssetsDTO;

    @ApiModelProperty(value = "标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value = "机构")
    private List<OrgDTO> department;

    @ApiModelProperty(value = "负责人")
    private List<UserDTO> principal;

    @ApiModelProperty(value = "用户组")
    private List<GroupDTO> group;
}
