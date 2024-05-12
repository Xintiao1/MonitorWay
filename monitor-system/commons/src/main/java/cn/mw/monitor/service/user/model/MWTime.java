package cn.mw.monitor.service.user.model;

import lombok.Data;

/**
 * @description:TODO
 * @author:zy.quaee
 * @date:2020/12/22 9:19
 */
@Data
public class MWTime {
    private static final Integer time = 3;
    private Boolean checked;
    private String content;

    public MWTime(Boolean checked, String content) {
        this.checked = checked;
        this.content = content;
    }
}
