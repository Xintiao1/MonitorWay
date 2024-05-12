package cn.mw.monitor.scanrule.model;

import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 *
 * @date 2020/3/20
 */

@Data
public class MwScanruleTable {
    /**
     * 自增主键
     */
    private Integer scanruleId;

    /**
     * 规则名称
     */
    private String scanruleName;

    /**
     * 监控服务器id
     */
    private Integer monitorServerId;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;
}