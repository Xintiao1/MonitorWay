package cn.mw.monitor.server.serverdto;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/11/26 16:59
 * @Version 1.0
 */
@Data
public class ApplicationDTO {
    private String itemid;
    private String name;

    private List<ApplicationDTO> items;

    private String chName;
    private Integer count;

    private List<String> itemIds;

    public void setItems(List<ApplicationDTO> items) {
        this.items = items;
        this.itemIds = items.stream().map(item -> item.getItemid()).collect(Collectors.toList());
    }
}
