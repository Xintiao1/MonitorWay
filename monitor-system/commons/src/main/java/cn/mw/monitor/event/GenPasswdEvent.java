package cn.mw.monitor.event;

import cn.mw.monitor.service.user.dto.UserDTO;
import lombok.Data;

@Data
public class GenPasswdEvent<T> extends Event<T> {
    private String password;
    UserDTO userdto;

    public GenPasswdEvent(String password, UserDTO userdto){
        this.password = password;
        this.userdto = userdto;
    }
}
