package cn.mw.monitor.logManage.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ModelSearchParam extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fuzzyQuery;

    private String modelType;

    private String modelName;

}
