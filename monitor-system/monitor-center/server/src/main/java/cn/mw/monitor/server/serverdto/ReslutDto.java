package cn.mw.monitor.server.serverdto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/29 11:50
 */
@Data
@Builder
public class ReslutDto {
    private String value;
    private String uints;//单位
    private String status;
    private String name;
}
