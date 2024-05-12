package cn.mw.monitor.TPServer.dto;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.TPServer.model.MwTPServerTable;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * @author syt
 * @Date 2020/10/30 12:07
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwTPServerDTO extends MwTPServerTable {

    @ApiModelProperty(value="机构")
    private List<OrgDTO> department;

    @ApiModelProperty(value="负责人")
    private List<UserDTO> principal;

    @ApiModelProperty(value="用户组")
    private List<GroupDTO> group;
}
