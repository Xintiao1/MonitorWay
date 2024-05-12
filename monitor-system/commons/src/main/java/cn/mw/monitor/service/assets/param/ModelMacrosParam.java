package cn.mw.monitor.service.assets.param;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/12/24 16:08
 * @Version 1.0
 */
@Data
public class ModelMacrosParam {
    //宏值的键
    private String macro;
    //值
    private String macroVal;

    private Integer macroType;
    //值
    private String value;

    private int type;
}
