package cn.mw.monitor.service.license.param;

import lombok.Data;


@Data
public class LicenseAssetsModuleStatusParam {

    private Boolean operationState;

    private Integer operationCount;

    private Boolean autoState;

    private Integer autoCount;

    private Boolean logState;

    private Integer logCount;

    private Boolean propState;

    private Integer propCount;

}
