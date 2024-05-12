package cn.mw.monitor.customPage.dto;

import cn.mw.monitor.customPage.model.MwPagefieldTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MwCustomColDTO extends MwPagefieldTable {

    // id
    private Integer customId;
    // 列ID
    private Integer colId;
    // 用户ID
    private Integer userId;
    // 是否排序
    private Boolean sortable;
    // 宽度
    private Integer width;
    // 是否可见
    private Boolean visible;
    // 顺序数
    private Integer orderNumber;
    //是否还原
    private  Integer deleteFlag;

}
