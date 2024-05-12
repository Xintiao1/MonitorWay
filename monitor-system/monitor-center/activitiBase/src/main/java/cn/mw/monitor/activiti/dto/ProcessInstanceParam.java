package cn.mw.monitor.activiti.dto;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

@Data
public class ProcessInstanceParam extends BaseParam {
    private boolean isCount = false;
}
