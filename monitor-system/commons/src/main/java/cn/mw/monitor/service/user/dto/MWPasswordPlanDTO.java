package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.service.user.model.MWPasswdPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MWPasswordPlanDTO extends MWPasswdPlan {

    private Integer userId;

    private Integer orgId;

    private Integer deptId;

}
