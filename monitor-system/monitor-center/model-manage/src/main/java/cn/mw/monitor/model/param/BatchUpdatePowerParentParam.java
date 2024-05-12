package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.param.MwModelInstanceParam;
import lombok.Data;

import java.util.List;

@Data
public class BatchUpdatePowerParentParam {
    private String type;
    private List<MwModelInstanceParam> instanceParams;
}
