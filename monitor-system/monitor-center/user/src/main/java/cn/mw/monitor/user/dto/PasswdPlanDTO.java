package cn.mw.monitor.user.dto;

import cn.mw.monitor.service.user.model.MWPasswdPlan;
import lombok.Data;

import java.util.List;

@Data
public class PasswdPlanDTO extends MWPasswdPlan {

    private List<List<Integer>> department;

}
