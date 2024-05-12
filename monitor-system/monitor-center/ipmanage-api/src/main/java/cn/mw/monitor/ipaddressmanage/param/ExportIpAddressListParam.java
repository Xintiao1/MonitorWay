package cn.mw.monitor.ipaddressmanage.param;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ExportIpAddressListParam {

    private Integer linkId;
    private List<Integer> ids;

    //导出的数据有那些字段
    private Set<String> fields;

    //导出文件名
    private String name;
}
