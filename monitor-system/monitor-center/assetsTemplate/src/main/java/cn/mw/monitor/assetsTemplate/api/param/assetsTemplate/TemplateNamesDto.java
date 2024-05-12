package cn.mw.monitor.assetsTemplate.api.param.assetsTemplate;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/6/15 16:02
 * @Version 1.0
 */
@Data
public class TemplateNamesDto {
    /**
     * 模板id
     */
    private Integer id;
    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 资产类型id
     */
    private Integer assetsTypeId;
    /**
     * 资产类型名称
     */
    private String assetsTypeName;
}
