package cn.mw.monitor.logManage.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DictionarySearchParam extends BaseParam {

    private String fuzzyQuery;

    private String dictionaryType;

    private String dictionaryName;
}
