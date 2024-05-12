package cn.mw.monitor.service.label.model;

import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/5/8
 */
@Data
public class QueryLabelParam {

    private String labelName;

    private Integer assetsTypeId;

    private Integer moduleId;

    private Boolean isRequired;

    private List<Integer> assetsTypeList;

}
