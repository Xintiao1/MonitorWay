package cn.mw.monitor.service.user.dto;

import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/5/20 17:37
 */
@Data

public class DataPermissionDto {
    private Integer id;
    private String type;
    private String typeId;
    private Integer isUser;
    private Integer isGroup;
    private String description;

    private List<String> typeIds;
}
