package cn.mw.monitor.assets.param;

import lombok.Data;

/**
 * 资产监控项没有数据查询
 * @Author qzg
 */
@Data
public class AssetsNoDataByItemParam {
    private String assetsName;
    private String ip;
    private String specifications;
    private String orgName;
    private String itemName;

}
