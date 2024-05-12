package cn.mw.monitor.event;

import cn.mw.monitor.service.user.dto.UserDTO;
import lombok.Data;

@Data
public class AddUserEvent<T> extends Event<T> {
    private String password;
    private UserDTO userDTO;

    public AddUserEvent(String password, UserDTO userDTO){
        this.password = password;
        this.userDTO = userDTO;
    }

    public AddUserEvent(UserDTO userDTO){
        this.userDTO = userDTO;
    }
}
