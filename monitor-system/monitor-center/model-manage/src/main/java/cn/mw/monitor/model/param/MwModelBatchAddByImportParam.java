package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/05/15 9:25
 */
@Data
@ApiModel
public class MwModelBatchAddByImportParam {
    private String instanceName;
    private String inBandIp;
}
