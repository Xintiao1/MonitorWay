package cn.mw.monitor.scanrule.api.param.scanrule;


import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
public class QueryScanruleParam extends BaseParam {

    private Integer scanruleId;

    private String scanruleName;

    private String ipAddresses;

    private String creator;

    private Date createDateStart;

    private Date createDateEnd;

    private String  modifier;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private Date scanStartTimeStart;

    private Date scanStartTimeEnd;

    private String fuzzyQuery;
}
