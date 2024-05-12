package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.service.user.param.LoginParam;
import cn.mwpaas.common.model.Reply;
import lombok.Data;

import java.util.List;

@Data
public class LoginDTO {

    private String token;

    private List<Reply> alertlist;

    private LoginParam loginParam;

}
