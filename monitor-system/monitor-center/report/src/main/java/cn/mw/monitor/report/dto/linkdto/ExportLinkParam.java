package cn.mw.monitor.report.dto.linkdto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ExportLinkParam {

    //导出的数据有那些字段
    private Set<String> fields;

    //导出文件名
    private String name;

    private List<InterfaceReportDto> list;
}
