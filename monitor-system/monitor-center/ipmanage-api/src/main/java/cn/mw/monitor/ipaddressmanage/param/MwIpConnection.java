package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @author bkc
 * @date 2020/7/17
 */
@Data
@ApiModel("断网参数")
public class MwIpConnection {
    //主键
    private Integer id;
    private String ipAddress;
    private String orgName;
    private Integer operType;
    private Date operTime;
    private String operPlatform;
    private String oper;
    private Integer operStatus;
}
