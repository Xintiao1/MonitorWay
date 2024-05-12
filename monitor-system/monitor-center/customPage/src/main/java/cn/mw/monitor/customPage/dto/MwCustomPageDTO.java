package cn.mw.monitor.customPage.dto;

import cn.mw.monitor.customPage.model.MwPageselectTable;
import lombok.Data;

import java.util.List;

@Data
public class MwCustomPageDTO {

    private List<MwCustomColDTO> mwCustomColDTOS;

    private List<MwPageselectTable> mwPageselectTables;

}
