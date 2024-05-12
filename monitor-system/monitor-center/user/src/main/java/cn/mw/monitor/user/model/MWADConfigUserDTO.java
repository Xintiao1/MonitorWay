package cn.mw.monitor.user.model;

import lombok.Data;

/**
 * Created by zy.quaee on 2021/8/30 10:18.
 **/
@Data
public class MWADConfigUserDTO {
    private Integer id;
    private Integer userId;
    private String userName;
    private String loginName;
    private String userState;

    //AD域名
    private String domainName;
}
