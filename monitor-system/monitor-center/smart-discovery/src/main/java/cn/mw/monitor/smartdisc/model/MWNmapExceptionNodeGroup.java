package cn.mw.monitor.smartdisc.model;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class MWNmapExceptionNodeGroup {

    private Integer id;
    private String exceptionName;
    private String exceptionIP;
    private Boolean deleteFlag;
}
