package cn.mw.monitor.service.server.api.dto;

import cn.mwpaas.common.utils.StringUtils;
import lombok.Builder;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/4/9 9:37
 * @Version 1.0
 */
@Data
@Builder
public class DropDownNamesDesc {
    //接口或者磁盘分区名称
    private String name;
    //接口描述或者磁盘分区描述
    private String description;

    public void setDescription(String description) {

        this.description = StringUtils.isEmpty(description) ? null : description;
    }

    public DropDownNamesDesc(String name, String description) {
        this.name = name;
        this.description = StringUtils.isEmpty(description) ? null : description;
    }

    public DropDownNamesDesc() {
    }
}
