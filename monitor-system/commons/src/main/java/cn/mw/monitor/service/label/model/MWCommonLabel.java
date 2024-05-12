package cn.mw.monitor.service.label.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/8/27 16:21
 */
@Data
@Builder
public class MWCommonLabel {
    private String tableName;
    private String columnName;
    private String mapperTableName;
    private Integer labelId;
    private Integer InputFormat;
    private String labelValue;
    private Integer dropKey;
    private Date labelDateStart;
    private Date labelDateEnd;

}
