package cn.mw.monitor.model.dto;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/9/23
 */
@Data
public class MWModelPropertiesInfoDto {
    //es中属性Id
    private String indexId;
    //属性名称
    private String propertiesName;
    //属性名称
    private String propertiesTypeId;
    //模型id
    private String modelId;
    //模型属性默认值下拉框数组
    private String dropOP;

}
