package cn.mw.monitor.server.serverdto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/5/21 9:26
 * @Version 1.0
 */
@Data
public class ItemNameDto {
    private int id;
    private String requestName;
    private String templateId;
    private String itemName;
    private String descr;
    private String templateName;
}
