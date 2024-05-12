package cn.mw.monitor.service.user.model;

import lombok.Data;

/**
 * @description:TODO
 * @author:zy.quaee
 * @date:2020/12/22 9:19
 */
@Data
public class MWMac {
    private static final Integer mac = 2;
    private Boolean checked;
    private String content;

    public MWMac(Boolean checked, String content) {
        this.checked = checked;
        this.content = content;
    }
}
