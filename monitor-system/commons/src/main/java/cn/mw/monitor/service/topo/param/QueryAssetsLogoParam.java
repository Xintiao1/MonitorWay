package cn.mw.monitor.service.topo.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

@Data
public class QueryAssetsLogoParam extends BaseParam {
    /**
     * Logo id
     */
    private int id;

    /**
     * 资产类型
     */
    private int assetType;

    /**
     * logo描述
     */
    private String desc;
    /**
     * 基础图例类型
     */
    private String typeName;
    //扩展图例类型
    private String subTypeName;
}
