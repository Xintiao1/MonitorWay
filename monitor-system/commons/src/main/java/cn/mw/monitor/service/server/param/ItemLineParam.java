package cn.mw.monitor.service.server.param;

import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/11/25 14:52
 * @Version 1.0
 */
@Data
public class ItemLineParam extends AssetsBaseDTO {
    private String itemId;
    //数据的单位
    private String units;

    private String itemName;
    //值是"NUMERAL" : "NOTNUMERAL"
    private String value_type;
    /**
     * 这个字段相当于item中的value_type
     */
    private int history;
    //准确监控项名称
    private List<String> itemNames;
}
