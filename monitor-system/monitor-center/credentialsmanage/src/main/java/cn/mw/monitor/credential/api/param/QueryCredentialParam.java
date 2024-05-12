package cn.mw.monitor.credential.api.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author zy.quaee
 * @date 2021/5/31 16:45
 **/
@Data
public class QueryCredentialParam extends BaseParam {

    /**
     * 凭据类型
     */
    @ApiModelProperty(value = "凭据类型")
    private String credType;

    /**
     * SNMP版本类型
     */
    @ApiModelProperty(value = "SNMP版本类型")
    private String snmpVersion;

    private Integer moduleId;

    private Integer credId;
}
