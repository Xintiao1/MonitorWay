package cn.mw.monitor.logManage.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParsingLibSearchParam extends BaseParam {

    private String fuzzyQuery;

    private String parsingLibName;

    private String parsingLibType;

}
