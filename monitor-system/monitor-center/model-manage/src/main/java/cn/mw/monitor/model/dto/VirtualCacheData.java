package cn.mw.monitor.model.dto;

import cn.mw.monitor.annotation.ESString;
import lombok.Data;

@Data
public class VirtualCacheData {
    private String id;

    @ESString(hasKeyword = false)
    private String data;
}
