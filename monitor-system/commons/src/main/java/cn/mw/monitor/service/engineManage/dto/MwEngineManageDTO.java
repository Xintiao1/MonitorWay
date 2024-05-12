package cn.mw.monitor.service.engineManage.dto;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.engineManage.model.MwEngineManageTable;
import cn.mw.monitor.service.user.dto.OrgDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwEngineManageDTO extends MwEngineManageTable {

    /**
     *模式
     */
    private String modeName;

    /**
     *加密方式
     */
    private String encryptionName;

    @ApiModelProperty(value="机构")
    private List<OrgDTO> department;

    @ApiModelProperty(value="负责人")
    private List<UserDTO> principal;

    @ApiModelProperty(value="用户组")
    private List<GroupDTO> group;
}
