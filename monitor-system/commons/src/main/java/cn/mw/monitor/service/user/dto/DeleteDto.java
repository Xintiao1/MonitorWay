package cn.mw.monitor.service.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/6/1 11:50
 */
@Data
@Builder
public class DeleteDto {
    private String typeId;
    private String type;
    private List<String> typeIds;
}
