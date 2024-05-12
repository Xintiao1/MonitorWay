package cn.mw.monitor.configmanage.entity;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/5/8
 */
@Data
public class QueryLabelParam {

    private String labelName;

    private Integer assetsTypeId;

    private Boolean screen;
}
