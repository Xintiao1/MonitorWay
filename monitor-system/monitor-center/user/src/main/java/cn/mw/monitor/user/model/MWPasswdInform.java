package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MWPasswdInform {

    // 自增序列
    private Integer id;
    // 用户id
    private Integer userId;
    // 即将生效密码策略id
    private Integer inoperactivePasswdPlan;
    // 删除标识
    private Boolean deleteFlag;
    // 修改类型标识
    private Integer modifyType;

    //修改类型  新增用户 -- 1   密码策略修改 -- 2
    public static Integer userAddModify = 1;
    public static Integer passwdPlanModify = 2;


}
