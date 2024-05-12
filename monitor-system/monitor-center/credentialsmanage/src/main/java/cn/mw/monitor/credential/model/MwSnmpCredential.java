package cn.mw.monitor.credential.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * mw_snmp_credential
 * @author 
 */
@ApiModel(value="SNMP团体名凭据")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MwSnmpCredential implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(value="主键")
    private Integer id;

    /**
     * SNMP版本
     */
    @ApiModelProperty(value="SNMP版本")
    private String snmpVersion;

    /**
     * 团体名
     */
    @ApiModelProperty(value="团体名")
    private String commName;


    /**
     * 模块
     */
    @ApiModelProperty(value="模块")
    private String module;

    /**
     * 模块id
     */
    @ApiModelProperty(value="模块id")
    private String moduleId;

    /**
     * 创建人
     */
    @ApiModelProperty(value="创建人")
    private String creator;

    private static final long serialVersionUID = 1L;
}