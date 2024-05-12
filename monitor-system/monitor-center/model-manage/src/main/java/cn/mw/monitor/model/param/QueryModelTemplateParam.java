package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author qzg
 * @date 2020/5/05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryModelTemplateParam extends BaseParam {

    private String assetsTypeName;

    private String subAssetsTypeName;

    private Integer id;
    /**
     * 模板名称
     */
    private String templateName;

    private String systemObjid;

    /**
     * 描述
     */
    private String description;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 资产类型
     */
    private Integer assetsTypeId;

    /**
     * 资产子类型
     */
    private Integer subAssetsTypeId;

    private String creator;

    private Date createDateStart;

    private Date createDateEnd;

    private String modifier;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private boolean selectAssetsFlag;

    private String fuzzyQuery;
}
