package cn.mw.monitor.labelManage.api.param;

import lombok.Data;

@Data
public class UpdateLabelStateParam {

    private Integer labelId;

    private String enable;

    private String modifier;

}
