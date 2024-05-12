package cn.mw.monitor.customPage.dto;

import cn.mw.monitor.customPage.model.MwPagefieldTable;
import lombok.Data;

import java.util.List;

@Data
public class MwCustomMultiColDTO extends MwPagefieldTable {

    // 页面id
    private Integer pageId;

    private List<MwCustomColDTO> pagelist;

}
