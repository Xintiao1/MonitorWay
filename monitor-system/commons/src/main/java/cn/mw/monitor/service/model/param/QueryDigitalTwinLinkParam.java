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
public class QueryDigitalTwinLinkParam {
    private int linkNum;//链路数量
    private int RJ45Num;//RJ45数量
    private int MPONum;//MPO-MPO数量
    private int LCNum;//lc-lc数量
    private int upNum;//接口状态Up数量
    private int downNum;//接口状态down数量
    private String cabinetCode;//机柜号
    private List<MwModelLinkDeviceParam> linkInfoList;//链路数据信息
}
