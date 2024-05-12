package cn.mw.monitor.service.systemLog.param;

import lombok.Data;

@Data
public class UpdateAttribute {
    private String name;
    private String old;
    private String now;
}
