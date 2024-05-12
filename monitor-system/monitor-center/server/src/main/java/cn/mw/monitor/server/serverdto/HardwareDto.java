package cn.mw.monitor.server.serverdto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/5/25 11:14
 * @Version 1.0
 */
@Data
public class HardwareDto {
    private String name;
    //    0：正常；1：不支持
    private String state;
    private String value;
    private String chName;
}
