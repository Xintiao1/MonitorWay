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
public class MwModelDigitalTwinLinkParam {
    //链路Id
    private String linkId;
    //链路路径
    private String multiNode;
    //链路类型
    private String linkType;
}
