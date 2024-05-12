package cn.mw.monitor.ipaddressmanage.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;


/**
 * @author baochengbin
 * @date 2020/3/30
 */
@Data
@Accessors(chain = true)
public class IpamOperHistoryDTO {


    private Integer type;

    private Integer applicant;

    private Boolean ipType;

    private Integer rlistId;

    private String creator;

   private Date createDate;

    private String desc;

    private String descript;
    @ApiModelProperty(value="ip关系ip")
    private String  bangDistri;
}
