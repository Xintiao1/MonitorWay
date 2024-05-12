package cn.mw.monitor.logManage.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class DataProcessingDTO {

    private String name;

    private List<String> types;

    private String desc;

    private List<DataConversion> dataConversions;

    private List<DataAggregate> dataAggregates;

    private List<DataFilter> dataFilters;

    public void validation() {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("名称为空");
        }
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("类型为空");
        }
    }
}
