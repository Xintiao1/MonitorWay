package cn.mw.monitor.service.assets.param;

import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date 20204/15
 */
@Data
public class QueryTangAssetsLabelParam {

    private String labelCode;

    private String labelValue;

    private Date labelDateStart;

    private Date labelDateEnd;

    private String formatValue;

    private String selectValue;
}
