package cn.mw.zbx.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/5/15 4:05
 * @Version 1.0
 */
@Data
public class ItemHistoryDto {
    private String itemId;

    private String name;

    private String chName;

    private Integer history;

    private String units;
}
