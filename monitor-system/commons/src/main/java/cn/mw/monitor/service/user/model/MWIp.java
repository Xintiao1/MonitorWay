package cn.mw.monitor.service.user.model;

import lombok.Data;

/**
 * @description:TODO
 * @author:zy.quaee
 * @date:2020/12/21 14:12
 */
@Data
public class MWIp {
    private static final Integer ip = 1;
    private Boolean checked;
    private String content;

    public MWIp(Boolean checked, String content) {
        this.checked = checked;
        this.content = content;
    }

}
