package cn.mw.monitor.assets.dto;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/7/15 15:42
 * @Version 1.0
 */
@Data
public class OutbandWithAssetsDTO {
    private String outbandId;
    private String outbandAssetsName;
    //所关联的资产
    private List<AssetsDTO> assetsList;
}
