package cn.mw.monitor.assetsTemplate.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/5/22
 */
@Data
@Builder
public class UpdateTemplateDTO {

    private String templateName;

    private String templateId;

    private String groupId;

    private Integer assetsTypeId;
}
