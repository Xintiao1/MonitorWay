package cn.mw.monitor.service.user.model;

import lombok.Data;

@Data
public class Auth {

    // 浏览
    private Boolean browse;
    // 创建
    private Boolean create;
    // 编辑
    private Boolean editor;
    // @MwPermit(moduleName = "ip_manage")
    private Boolean delete;
    // 执行
    private Boolean perform;
    // 二级密码
    private Boolean secopassword;

}
