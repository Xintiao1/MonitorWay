package cn.mw.monitor.labelManage.dto;

import cn.mw.monitor.dropDown.model.MwDropdownTable;
import cn.mw.monitor.labelManage.model.MwLabelManageTable;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwLabelManageByIdDTO extends MwLabelManageTable {

    private List<Integer> modeList;

    private List<Integer> assetsTypeIdList;

    private List<MwDropdownTable> dropdownTable;

    /**
     * 已关联的下拉值
     */
    private List<Integer> associatedDropIds;

}
