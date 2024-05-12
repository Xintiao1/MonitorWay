package cn.mw.monitor.smartdisc.model;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class MWNmapFingerNodeGroup {

    private Integer id;
    private String fingerDetectName;
    private String fingerDetectNodeGroup;
    private Boolean deleteFlag;
}
