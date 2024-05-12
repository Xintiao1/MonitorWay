package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @author bkc
 * @date 2020/7/17
 */
@Data
@ApiModel("ip字典主表")
public class IpDictionaryTableParam {
    //主键
    private Integer id;

    private Integer key;
    private String value;

    private Integer typeof;
    private String descri;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;


}
