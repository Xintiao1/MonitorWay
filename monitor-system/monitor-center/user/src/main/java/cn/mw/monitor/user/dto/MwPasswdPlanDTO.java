package cn.mw.monitor.user.dto;

import cn.mw.monitor.service.user.model.MWPasswdPlan;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.dto.MwSubUserDTO;
import lombok.Data;

import java.util.List;

@Data
public class MwPasswdPlanDTO extends MWPasswdPlan {

    private List<OrgDTO> department;

    private List<MwSubUserDTO> userDTOs;

}
