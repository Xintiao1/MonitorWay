package cn.mw.monitor.event;

import cn.mw.monitor.service.user.dto.MWPasswordPlanDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import lombok.Data;

@Data
public class PostUpdUserEvent<T> extends Event<T> {
    private UserDTO oldUserdto;
    private UserDTO newUserdto;
    private MWPasswordPlanDTO passwordPlanDTO;

    public PostUpdUserEvent(UserDTO oldUserdto, UserDTO newUserdto, MWPasswordPlanDTO passwordPlanDTO){
        this.oldUserdto = oldUserdto;
        this.newUserdto = newUserdto;
        this.passwordPlanDTO = passwordPlanDTO;
    }
}
