package cn.mw.monitor.user.dto;

import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.user.model.MwGroupTable;
import cn.mw.monitor.service.user.dto.OrgDTO;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwGroupDTO extends MwGroupTable {

    private List<GroupUserDTO> attachUser;

    private List<OrgDTO> department;

    //用户组状态 0为可用  1为禁用
    private String enableGroup;

}
