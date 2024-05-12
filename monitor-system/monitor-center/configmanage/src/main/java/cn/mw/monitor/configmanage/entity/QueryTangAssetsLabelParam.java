package cn.mw.monitor.configmanage.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author bkc
 * @date 2020/9/7
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
