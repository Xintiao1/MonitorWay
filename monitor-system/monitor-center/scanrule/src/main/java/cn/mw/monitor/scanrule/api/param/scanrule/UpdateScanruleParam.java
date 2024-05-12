package cn.mw.monitor.scanrule.api.param.scanrule;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
public class UpdateScanruleParam extends AddScanruleParam {

    /**
     * 自增主键
     */
    private Integer id;
}
