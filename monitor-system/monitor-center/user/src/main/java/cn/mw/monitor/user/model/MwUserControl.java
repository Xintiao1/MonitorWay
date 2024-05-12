package cn.mw.monitor.user.model;

import lombok.Data;

/**
 * @description:TODO
 * @author:zy.quaee
 * @date:2020/12/24 11:26
 */
@Data
public class MwUserControl {
    private Integer id;
    private Integer userId;
    private String controlTypeId;
    private String rule;
}
