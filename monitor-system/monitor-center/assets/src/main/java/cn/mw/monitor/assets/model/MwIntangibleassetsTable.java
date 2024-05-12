package cn.mw.monitor.assets.model;

import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/03/16
 *
 */
@Data
public class MwIntangibleassetsTable{

    /**
     * 自增主键
     */
    private String id;

    /**
     *资产编号
     */
    private String assetsNumber;

    /**
     *资产名称
     */
    private String assetsName;

    /**
     *资产类型
     */
    private Integer assetsTypeId;

    /**
     *资产类型
     */
    private Integer subAssetsTypeId;

    /**
     *资产类型
     */
    private String assetsContent;

    /**
     *资产状态
     */
    private String enable;
    /**
     *备注
     */
    private String remarks;

    /**
     *删除标识符
     */
    private Boolean deleteFlag;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;
}