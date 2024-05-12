package cn.mw.monitor.event;

import cn.mw.monitor.service.user.dto.MWPasswordPlanDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import lombok.Data;

@Data
public class UpdUserEvent<T> extends Event<T> {
    private String password;
    private UserDTO olduserdto;
    private UserDTO newuserdto;
    private MWPasswordPlanDTO mwpasswordPlanDTO;

    public UpdUserEvent(String password, UserDTO olduserdto, UserDTO newuserdto, MWPasswordPlanDTO mwpasswordPlanDTO){
        this.password = password;
        this.olduserdto = olduserdto;
        this.newuserdto = newuserdto;
        this.mwpasswordPlanDTO = mwpasswordPlanDTO;
    }
}
