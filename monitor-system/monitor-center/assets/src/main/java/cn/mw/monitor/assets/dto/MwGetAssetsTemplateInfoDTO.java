package cn.mw.monitor.assets.dto;

import lombok.Data;

/**
 * 查询时获取表AssetsTemplate部分字段数据
 * @author qzg
 * @Version 1.0
 */
@Data
public class MwGetAssetsTemplateInfoDTO {
    private Integer assetsTypeId;//资产类型
    private Integer subAssetsTypeId;//资产子类型
    private Integer monitorMode;//监控协议
    private String brand;//厂商
    private String specification;//型号规格
    private String description;//描述
}
