package cn.mw.monitor.link.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/8/23 14:48
 */
@Builder
@Data
public class CommonLabelDto {
    private Integer id;
    private List<LinkLabelDto> linkLabelDtos;
    private String tableName;
    private String columnName;
}
