package cn.mw.monitor.server.serverdto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/6/5 22:35
 */
@Data
@Builder
public class InterfaceItemBaseDto {
    private String itemName;
    private String lastValue;
}
