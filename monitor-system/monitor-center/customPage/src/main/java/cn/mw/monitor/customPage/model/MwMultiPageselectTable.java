package cn.mw.monitor.customPage.model;

import lombok.Data;

import java.util.List;

@Data
public class MwMultiPageselectTable {

    private Integer pageId;

    private List<MwPageselectTable> pagelist;

}