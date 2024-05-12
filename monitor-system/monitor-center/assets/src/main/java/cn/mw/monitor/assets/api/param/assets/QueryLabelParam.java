package cn.mw.monitor.assets.api.param.assets;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/5/8
 */
@Data
public class QueryLabelParam {

    private String labelName;

    private Integer assetsTypeId;

    private Integer moduleId;


}
