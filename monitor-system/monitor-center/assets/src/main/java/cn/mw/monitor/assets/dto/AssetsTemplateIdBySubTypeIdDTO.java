package cn.mw.monitor.assets.dto;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/6/29
 */
@Data
public class AssetsTemplateIdBySubTypeIdDTO {
    private Integer assetsTypeSubId;
    private String assetsTypeSubName;
    private Integer monitorMode;
    private String manufacturer;
    private String specification;
    private String templateId;
    private Integer monitorServerId;
}
