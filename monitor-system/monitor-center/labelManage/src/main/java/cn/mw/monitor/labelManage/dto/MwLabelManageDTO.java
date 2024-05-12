package cn.mw.monitor.labelManage.dto;

import cn.mw.monitor.dropDown.dto.MwDropdownDTO;
import cn.mw.monitor.labelManage.model.MwLabelManageTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwLabelManageDTO extends MwLabelManageTable {

    private String inputFormatName;

    private List<MwAssetsTypeDTO> assetsType;

    private List<MwModuleTypeDTO> moduleType;

    private List<MwDropdownDTO> dropdownTable;

}
