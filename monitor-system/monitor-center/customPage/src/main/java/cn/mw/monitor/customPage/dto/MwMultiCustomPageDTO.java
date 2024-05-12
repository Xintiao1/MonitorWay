package cn.mw.monitor.customPage.dto;

import cn.mw.monitor.customPage.model.MwPageselectTable;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MwMultiCustomPageDTO {

    private Map<String, List<MwCustomColDTO>> mwCustomColDTOS;

    private Map<String, List<MwPageselectTable>> mwPageselectTables;

}
