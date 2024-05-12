package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.ipaddressmanage.dto.MwIpAddresses1DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/17
 */
@Data
@ApiModel("断网参数")
public class AddDisConnection {
    //主键
    private String ip;
    private String mask;
    private String dst;
    private String daemon;
    private String extend;
    private String description;
    private String hash;
    private String index;
    private String code;
}
