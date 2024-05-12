package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class QueryModelLinkDeviceInfo {
    private String id;
    private List<MwModelLinkDeviceParam> list;
}
